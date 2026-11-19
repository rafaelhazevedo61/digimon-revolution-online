package com.dro.modules.achievement.application;

import com.dro.modules.achievement.api.AchievementDtos.*;
import com.dro.modules.achievement.infra.AchievementRepository;
import com.dro.modules.achievement.infra.AchievementRepository.Definition;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.audit.TransactionAuditPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AchievementService {
    private final AchievementRepository repository;
    private final PlayerRepository players;
    private final ItemDefinitionRepository items;
    private final AddItemUseCase grant;
    private final TransactionAuditPublisher audit;

    public AchievementService(AchievementRepository repository, PlayerRepository players,
                              ItemDefinitionRepository items, AddItemUseCase grant, TransactionAuditPublisher audit) {
        this.repository = repository; this.players = players; this.items = items; this.grant = grant;
        this.audit = audit;
    }

    @Transactional
    public Page list(UUID playerId) {
        lockPlayer(playerId);
        List<Definition> definitions = synchronize(playerId);
        var stored = repository.progress(playerId);
        List<Entry> entries = definitions.stream().map(d -> {
            var p = stored.get(d.code());
            return new Entry(d.code(), d.category(), d.name(), d.description(), p.progress(), d.target(),
                    p.claimedAt() != null ? "CLAIMED" : p.completedAt() != null ? "AVAILABLE" : "IN_PROGRESS",
                    p.completedAt(), p.claimedAt(), d.reward());
        }).toList();
        long claimed = entries.stream().filter(e -> "CLAIMED".equals(e.status())).count();
        long available = entries.stream().filter(e -> "AVAILABLE".equals(e.status())).count();
        return new Page(new Summary(entries.size(), claimed + available, available, claimed), entries);
    }

    @Transactional
    public Claim claim(UUID playerId, String code) {
        Player player = lockPlayer(playerId);
        Definition definition = synchronize(playerId).stream().filter(d -> d.code().equals(code)).findFirst()
                .orElseThrow(() -> new NotFoundException("Conquista não encontrada."));
        var progress = repository.progress(playerId).get(code);
        if (progress.claimedAt() != null) throw new ConflictException("Recompensa já resgatada.");
        if (progress.completedAt() == null) throw new BadRequestException("A meta desta conquista ainda não foi atingida.");
        var reward = definition.reward();
        var item = items.findByCode(reward.itemCode())
                .orElseThrow(() -> new ConflictException("Item de recompensa indisponível."));
        if ((long) player.getBits() + reward.bits() > Integer.MAX_VALUE) {
            throw new BadRequestException("Seu saldo de Bits não comporta esta recompensa.");
        }
        if (!repository.claim(playerId, code)) throw new ConflictException("Recompensa já resgatada.");
        // Tudo participa da mesma transação: stack cheio reverte Bits e marca de resgate.
        grant.addMaterialToPlayer(playerId, item, reward.quantity());
        player.addBits(reward.bits());
        players.save(player);
        audit.success("achievement:" + playerId + ":" + code, "ACHIEVEMENT_CLAIMED", "Achievement", code,
                Map.of("playerId", playerId.toString(), "achievementCode", code, "bits", reward.bits(),
                        "itemCode", reward.itemCode(), "quantity", reward.quantity()));
        return new Claim("Recompensa resgatada! Os itens estão no inventário.", reward);
    }

    private Player lockPlayer(UUID playerId) {
        return players.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
    }

    private List<Definition> synchronize(UUID playerId) {
        var definitions = repository.definitions();
        var metrics = repository.metrics(playerId);
        definitions.forEach(d -> repository.observe(playerId, d, metrics.getOrDefault(d.metric(), 0L)));
        return definitions;
    }
}
