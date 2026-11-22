package com.dro.modules.achievement.application;

import com.dro.modules.achievement.infra.AchievementQueueRepository;
import com.dro.modules.achievement.infra.AchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AchievementProcessor {
    private final AchievementQueueRepository queue;
    private final AchievementRepository repository;

    public AchievementProcessor(AchievementQueueRepository queue, AchievementRepository repository) {
        this.queue = queue; this.repository = repository;
    }

    // Não bloqueia Player: ações do jogo bloqueiam Player antes de enfileirar.
    // Manter essa ordem evita deadlock entre o worker e ações de gameplay.
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 15)
    public boolean processNext() {
        var next = queue.lockNext();
        if (next.isEmpty()) return false;
        UUID playerId = next.get();
        try {
            // Carrega após obter a pendência: uma edição concorrente do catálogo não pode ser perdida.
            var catalog = repository.definitions();
            var stored = repository.progress(playerId);
            var incomplete = catalog.stream().filter(d -> !stored.containsKey(d.code())
                    || stored.get(d.code()).completedAt() == null).toList();
            if (!incomplete.isEmpty()) {
                var metrics = repository.metrics(playerId);
                for (var definition : incomplete) {
                    long observed = Math.max(0, Math.min(definition.target(), metrics.getOrDefault(definition.metric(), 0L)));
                    var previous = stored.get(definition.code());
                    if (previous == null || observed > previous.progress()
                            || observed >= definition.target() && previous.completedAt() == null) {
                        repository.observe(playerId, definition, observed);
                    }
                }
            }
            queue.complete(playerId);
            return true;
        } catch (RuntimeException exception) {
            // Propaga para reverter progresso, timestamp e remoção da fila juntos.
            throw new ProcessingFailure(playerId, exception);
        }
    }

    public static class ProcessingFailure extends RuntimeException {
        private final UUID playerId;
        public ProcessingFailure(UUID playerId, Throwable cause) {
            super("Falha no processamento de conquistas", cause); this.playerId = playerId;
        }
        public UUID playerId() { return playerId; }
    }
}
