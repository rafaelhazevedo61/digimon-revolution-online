package com.dro.modules.boss.world.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.world.api.dto.response.WorldBossAttackResponse;
import com.dro.modules.boss.world.api.dto.response.WorldBossRankingEntryResponse;
import com.dro.modules.boss.world.api.dto.response.WorldBossResponse;
import com.dro.modules.boss.api.dto.response.BossDefeatSummaryResponse;
import com.dro.modules.boss.world.domain.WorldBossAttack;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossRules;
import com.dro.modules.boss.world.infra.WorldBossAttackRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente da camada de conversor entre domínio e contratos da API do módulo de Boss Mundial.
 */
@Component
public class WorldBossResponseMapper {
    private static final int RECENT_ATTACK_LIMIT = 5;
    private final BossDefinitionRepository bossDefinitionRepository;
    private final WorldBossAttackRepository worldBossAttackRepository;
    private final PlayerRepository playerRepository;
    private final WorldBossRewardService worldBossRewardService;
    private final GameplayConfig gameplayConfig;

    public WorldBossResponse toResponse(WorldBossInstance instance, UUID viewerPlayerId) {
        BossDefinitionEntity boss = bossDefinitionRepository.findById(instance.getBossId()).orElseThrow(() -> new NotFoundException("Boss not found"));
        List<WorldBossAttack> myAttacks = worldBossAttackRepository.findByWorldBossIdAndPlayerIdOrderByCreatedAtDesc(instance.getId(), viewerPlayerId);
        long myTotalDamage = myAttacks.stream().mapToLong(WorldBossAttack::getDamage).sum();
        int attackCooldownMinutes = WorldBossRules.attackCooldownMinutes(boss.getCooldownMinutes());
        boolean cooldownEnabled = gameplayConfig.isWorldBossCooldownEnabled();
        Instant nextAttackCandidate = myAttacks.isEmpty() || myAttacks.get(0).getCreatedAt() == null ? null : myAttacks.get(0).getCreatedAt().plus(Duration.ofMinutes(attackCooldownMinutes));
        Instant nextAttackAvailableAt = cooldownEnabled && instance.getStatus() == com.dro.modules.boss.world.domain.WorldBossStatus.ACTIVE && nextAttackCandidate != null && nextAttackCandidate.isAfter(Instant.now()) ? nextAttackCandidate : null;
        List<WorldBossAttackResponse> recentAttacks = worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(instance.getId()).stream().limit(RECENT_ATTACK_LIMIT).map(this::toAttackResponse).toList();
        return new WorldBossResponse(instance.getId(), boss.getCode(), boss.getName(), boss.getImageUrl(), instance.getMaxHp(), instance.getRemainingHp(), instance.getStatus(), instance.getCreatedAt(), instance.getDefeatedAt(), attackCooldownMinutes, cooldownEnabled, nextAttackAvailableAt, myTotalDamage, buildRanking(instance.getId()), recentAttacks, worldBossRewardService.findPlayerRewards(instance.getId(), viewerPlayerId), buildDefeatSummary(instance, worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(instance.getId())));
    }

    private BossDefeatSummaryResponse buildDefeatSummary(WorldBossInstance instance, List<WorldBossAttack> attacks) {
        if (instance.getStatus() != com.dro.modules.boss.world.domain.WorldBossStatus.DEFEATED || attacks.isEmpty()) return null;
        WorldBossAttack finalBlow = attacks.stream().max(Comparator.comparing(WorldBossAttack::getCreatedAt)).orElseThrow();
        Map<UUID, Long> totals = attacks.stream().collect(Collectors.groupingBy(WorldBossAttack::getPlayerId, Collectors.summingLong(WorldBossAttack::getDamage)));
        UUID topPlayer = totals.entrySet().stream().max(Map.Entry.<UUID, Long>comparingByValue()).orElseThrow().getKey();
        String finalName = playerRepository.findById(finalBlow.getPlayerId()).map(Player::getUsername).orElse("Desconhecido");
        String topName = playerRepository.findById(topPlayer).map(Player::getUsername).orElse("Desconhecido");
        long duration = instance.getDefeatedAt() == null ? 0 : Math.max(0, Duration.between(instance.getCreatedAt(), instance.getDefeatedAt()).getSeconds());
        return new BossDefeatSummaryResponse(finalBlow.getPlayerId(), finalName, topPlayer, topName, totals.get(topPlayer), attacks.size(), duration);
    }

    private List<WorldBossRankingEntryResponse> buildRanking(UUID worldBossId) {
        Map<UUID, Long> damageByPlayer = worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(worldBossId).stream().collect(Collectors.groupingBy(WorldBossAttack::getPlayerId, Collectors.summingLong(WorldBossAttack::getDamage)));
        List<UUID> playerIds = damageByPlayer.keySet().stream().toList();
        Map<UUID, String> usernames = playerIds.isEmpty() ? Collections.emptyMap() : playerRepository.findAllById(playerIds).stream().collect(Collectors.toMap(Player::getId, Player::getUsername));
        List<Map.Entry<UUID, Long>> sorted = damageByPlayer.entrySet().stream().sorted(Map.Entry.<UUID, Long>comparingByValue().reversed()).toList();
        List<WorldBossRankingEntryResponse> ranking = new ArrayList<>();
        int position = 1;
        for (Map.Entry<UUID, Long> entry : sorted) {
            ranking.add(new WorldBossRankingEntryResponse(position++, entry.getKey(), usernames.getOrDefault(entry.getKey(), "Unknown"), entry.getValue()));
        }
        return ranking;
    }

    private WorldBossAttackResponse toAttackResponse(WorldBossAttack attack) {
        Player player = playerRepository.findById(attack.getPlayerId()).orElse(null);
        return new WorldBossAttackResponse(attack.getId(), attack.getPlayerId(), player != null ? player.getUsername() : "Unknown", attack.getDamage(), attack.getCreatedAt());
    }

    public WorldBossResponseMapper(final BossDefinitionRepository bossDefinitionRepository, final WorldBossAttackRepository worldBossAttackRepository, final PlayerRepository playerRepository, final WorldBossRewardService worldBossRewardService, final GameplayConfig gameplayConfig) {
        this.bossDefinitionRepository = bossDefinitionRepository;
        this.worldBossAttackRepository = worldBossAttackRepository;
        this.playerRepository = playerRepository;
        this.worldBossRewardService = worldBossRewardService;
        this.gameplayConfig = gameplayConfig;
    }
}
