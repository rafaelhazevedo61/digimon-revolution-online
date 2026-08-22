package com.dro.modules.boss.world.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.world.api.dto.response.WorldBossAttackResponse;
import com.dro.modules.boss.world.api.dto.response.WorldBossRankingEntryResponse;
import com.dro.modules.boss.world.api.dto.response.WorldBossResponse;
import com.dro.modules.boss.world.domain.WorldBossAttack;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossRules;
import com.dro.modules.boss.world.infra.WorldBossAttackRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente da camada de conversor entre domínio e contratos da API do módulo de Boss Mundial.
 */
@Component
@RequiredArgsConstructor
public class WorldBossResponseMapper {

    private final BossDefinitionRepository bossDefinitionRepository;
    private final WorldBossAttackRepository worldBossAttackRepository;
    private final PlayerRepository playerRepository;
    private final WorldBossRewardService worldBossRewardService;

    public WorldBossResponse toResponse(WorldBossInstance instance, UUID viewerPlayerId) {
        BossDefinitionEntity boss = bossDefinitionRepository.findById(instance.getBossId())
                .orElseThrow(() -> new NotFoundException("Boss not found"));

        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant resetCutoff = instance.getDailyResetAt() != null && instance.getDailyResetAt().isAfter(startOfDay)
                ? instance.getDailyResetAt()
                : startOfDay;

        int usedToday = (int) worldBossAttackRepository
                .countByWorldBossIdAndPlayerIdAndCreatedAtGreaterThanEqual(instance.getId(), viewerPlayerId, resetCutoff);
        long myTotalDamage = worldBossAttackRepository.findByWorldBossIdAndPlayerIdOrderByCreatedAtDesc(instance.getId(), viewerPlayerId).stream()
                .mapToLong(WorldBossAttack::getDamage)
                .sum();

        List<WorldBossAttackResponse> recentAttacks = worldBossAttackRepository
                .findByWorldBossIdOrderByCreatedAtDesc(instance.getId()).stream()
                .limit(20)
                .map(this::toAttackResponse)
                .toList();

        return new WorldBossResponse(
                instance.getId(),
                boss.getCode(),
                boss.getName(),
                boss.getImageUrl(),
                instance.getMaxHp(),
                instance.getRemainingHp(),
                instance.getStatus(),
                instance.getCreatedAt(),
                instance.getDefeatedAt(),
                usedToday,
                Math.max(0, WorldBossRules.DAILY_ATTACK_LIMIT - usedToday),
                myTotalDamage,
                buildRanking(instance.getId()),
                recentAttacks,
                worldBossRewardService.findPlayerRewards(instance.getId(), viewerPlayerId)
        );
    }

    private List<WorldBossRankingEntryResponse> buildRanking(UUID worldBossId) {
        Map<UUID, Long> damageByPlayer = worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(worldBossId).stream()
                .collect(Collectors.groupingBy(
                        WorldBossAttack::getPlayerId,
                        Collectors.summingLong(WorldBossAttack::getDamage)
                ));

        List<UUID> playerIds = damageByPlayer.keySet().stream().toList();
        Map<UUID, String> usernames = playerIds.isEmpty()
                ? Collections.emptyMap()
                : playerRepository.findAllById(playerIds).stream()
                        .collect(Collectors.toMap(Player::getId, Player::getUsername));

        List<Map.Entry<UUID, Long>> sorted = damageByPlayer.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .toList();

        List<WorldBossRankingEntryResponse> ranking = new ArrayList<>();
        int position = 1;
        for (Map.Entry<UUID, Long> entry : sorted) {
            ranking.add(new WorldBossRankingEntryResponse(
                    position++,
                    entry.getKey(),
                    usernames.getOrDefault(entry.getKey(), "Unknown"),
                    entry.getValue()
            ));
        }
        return ranking;
    }

    private WorldBossAttackResponse toAttackResponse(WorldBossAttack attack) {
        Player player = playerRepository.findById(attack.getPlayerId()).orElse(null);
        return new WorldBossAttackResponse(
                attack.getId(),
                attack.getPlayerId(),
                player != null ? player.getUsername() : "Unknown",
                attack.getDamage(),
                attack.getCreatedAt()
        );
    }
}
