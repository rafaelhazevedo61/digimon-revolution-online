package com.dro.modules.boss.application;

import com.dro.modules.boss.api.dto.response.BossAttemptResponse;
import com.dro.modules.boss.domain.BossAttemptEntity;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossAttemptRepository;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Boss Mundial.
 */
@Service
@RequiredArgsConstructor
public class GetBossHistoryUseCase {

    private final BossAttemptRepository bossAttemptRepository;
    private final BossDefinitionRepository bossDefinitionRepository;

    public List<BossAttemptResponse> execute(String token, int page, int size) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        List<BossAttemptEntity> attempts = bossAttemptRepository
                .findByPlayerIdOrderByCreatedAtDesc(playerId, PageRequest.of(page, size));

        Map<Long, BossDefinitionEntity> bossMap = bossDefinitionRepository.findAll()
                .stream().collect(Collectors.toMap(BossDefinitionEntity::getId, b -> b));

        return attempts.stream().map(a -> {
            BossDefinitionEntity boss = bossMap.get(a.getBossId());
            return new BossAttemptResponse(
                    a.getId(),
                    boss != null ? boss.getCode() : "UNKNOWN",
                    boss != null ? boss.getName() : "Unknown",
                    a.getStatus().name(),
                    a.getXpGained(),
                    a.getBitsGained(),
                    a.getCreatedAt()
            );
        }).toList();
    }
}
