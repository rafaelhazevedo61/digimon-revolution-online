package com.dro.modules.boss.application;

import com.dro.modules.boss.api.dto.response.BossCooldownResponse;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossAttemptRepository;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetBossCooldownsUseCase {

    private final BossDefinitionRepository bossDefinitionRepository;
    private final BossAttemptRepository bossAttemptRepository;

    public List<BossCooldownResponse> execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        List<BossDefinitionEntity> bosses = bossDefinitionRepository.findAllActive().stream()
                .filter(boss -> boss.getBossType() != BossType.CLAN && boss.getBossType() != BossType.WORLD)
                .toList();

        return bosses.stream().map(boss -> {
            var lastAttempt = bossAttemptRepository
                    .findFirstByPlayerIdAndBossIdOrderByCreatedAtDesc(playerId, boss.getId());

            Long cooldownRemaining = null;
            boolean available = true;

            if (lastAttempt.isPresent()) {
                Instant lastTime = lastAttempt.get().getCreatedAt();
                Instant cooldownEnd = lastTime.plus(boss.getCooldownMinutes(), ChronoUnit.MINUTES);
                Instant now = Instant.now();

                if (now.isBefore(cooldownEnd)) {
                    cooldownRemaining = now.until(cooldownEnd, ChronoUnit.SECONDS);
                    available = false;
                }
            }

            return new BossCooldownResponse(
                    boss.getCode(),
                    boss.getName(),
                    boss.getBossType().name(),
                    available,
                    cooldownRemaining
            );
        }).toList();
    }
}
