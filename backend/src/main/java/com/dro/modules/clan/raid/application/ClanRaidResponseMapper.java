package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.api.dto.response.BossDefeatSummaryResponse;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidAttackResponse;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidRankingEntryResponse;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidResponse;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import com.dro.modules.clan.raid.domain.ClanRaidRules;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
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
 * Componente da camada de conversor entre domínio e contratos da API do módulo de Clãs.
 */
@Component
public class ClanRaidResponseMapper {
    private static final int RECENT_ATTACK_LIMIT = 5;
    private final BossDefinitionRepository bossDefinitionRepository;
    private final ClanRaidAttackRepository clanRaidAttackRepository;
    private final PlayerRepository playerRepository;
    private final GameplayConfig gameplayConfig;

    public ClanRaidResponse toResponse(ClanRaid raid, UUID viewerPlayerId) {
        BossDefinitionEntity boss = bossDefinitionRepository.findById(raid.getBossId()).orElseThrow(() -> new NotFoundException("Boss not found"));
        List<ClanRaidAttack> attacks = clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raid.getId());
        List<ClanRaidAttack> myAttacks = attacks.stream().filter(attack -> attack.getPlayerId().equals(viewerPlayerId)).toList();
        long myTotalDamage = myAttacks.stream().mapToLong(ClanRaidAttack::getDamage).sum();
        int attackCooldownMinutes = ClanRaidRules.attackCooldownMinutes(boss.getCooldownMinutes());
        boolean cooldownEnabled = gameplayConfig.isClanRaidCooldownEnabled();
        Instant nextAttackCandidate = myAttacks.isEmpty() || myAttacks.get(0).getCreatedAt() == null
                ? null
                : myAttacks.get(0).getCreatedAt().plus(Duration.ofMinutes(attackCooldownMinutes));
        Instant nextAttackAvailableAt = cooldownEnabled
                && raid.getStatus() == ClanRaidStatus.ACTIVE
                && nextAttackCandidate != null
                && nextAttackCandidate.isAfter(Instant.now())
                ? nextAttackCandidate
                : null;
        List<ClanRaidAttackResponse> recentAttacks = attacks.stream().limit(RECENT_ATTACK_LIMIT).map(this::toAttackResponse).toList();
        return new ClanRaidResponse(
                raid.getId(), raid.getClanId(), boss.getCode(), boss.getName(), boss.getImageUrl(),
                raid.getMaxHp(), raid.getRemainingHp(), raid.getStatus(), raid.getCreatedAt(), raid.getDefeatedAt(),
                attackCooldownMinutes, cooldownEnabled, nextAttackAvailableAt,
                myTotalDamage, buildRanking(raid.getId()), recentAttacks,
                buildDefeatSummary(raid, attacks)
        );
    }

    private BossDefeatSummaryResponse buildDefeatSummary(ClanRaid raid, List<ClanRaidAttack> attacks) {
        if (raid.getStatus() != ClanRaidStatus.DEFEATED || attacks.isEmpty()) return null;
        ClanRaidAttack finalBlow = attacks.stream().max(Comparator.comparing(ClanRaidAttack::getCreatedAt)).orElseThrow();
        Map<UUID, Long> totals = attacks.stream().collect(Collectors.groupingBy(ClanRaidAttack::getPlayerId, Collectors.summingLong(ClanRaidAttack::getDamage)));
        UUID topPlayer = totals.entrySet().stream().max(Map.Entry.<UUID, Long>comparingByValue()).orElseThrow().getKey();
        String finalName = playerRepository.findById(finalBlow.getPlayerId()).map(Player::getUsername).orElse("Desconhecido");
        String topName = playerRepository.findById(topPlayer).map(Player::getUsername).orElse("Desconhecido");
        long duration = raid.getDefeatedAt() == null ? 0 : Math.max(0, Duration.between(raid.getCreatedAt(), raid.getDefeatedAt()).getSeconds());
        return new BossDefeatSummaryResponse(finalBlow.getPlayerId(), finalName, topPlayer, topName, totals.get(topPlayer), attacks.size(), duration, raid.getDefeatedAt().plus(Duration.ofHours(1)));
    }

    private List<ClanRaidRankingEntryResponse> buildRanking(UUID raidId) {
        Map<UUID, Long> damageByPlayer = clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raidId).stream().collect(Collectors.groupingBy(ClanRaidAttack::getPlayerId, Collectors.summingLong(ClanRaidAttack::getDamage)));
        List<UUID> playerIds = damageByPlayer.keySet().stream().toList();
        Map<UUID, String> usernames = playerIds.isEmpty() ? Collections.emptyMap() : playerRepository.findAllById(playerIds).stream().collect(Collectors.toMap(Player::getId, Player::getUsername));
        List<Map.Entry<UUID, Long>> sorted = damageByPlayer.entrySet().stream().sorted(Map.Entry.<UUID, Long>comparingByValue().reversed()).toList();
        List<ClanRaidRankingEntryResponse> ranking = new ArrayList<>();
        int position = 1;
        for (Map.Entry<UUID, Long> entry : sorted) {
            ranking.add(new ClanRaidRankingEntryResponse(position++, entry.getKey(), usernames.getOrDefault(entry.getKey(), "Unknown"), entry.getValue()));
        }
        return ranking;
    }

    private ClanRaidAttackResponse toAttackResponse(ClanRaidAttack attack) {
        Player player = playerRepository.findById(attack.getPlayerId()).orElse(null);
        return new ClanRaidAttackResponse(attack.getId(), attack.getPlayerId(), player != null ? player.getUsername() : "Unknown", attack.getDamage(), attack.getCreatedAt());
    }

    public ClanRaidResponseMapper(final BossDefinitionRepository bossDefinitionRepository, final ClanRaidAttackRepository clanRaidAttackRepository, final PlayerRepository playerRepository, final GameplayConfig gameplayConfig) {
        this.bossDefinitionRepository = bossDefinitionRepository;
        this.clanRaidAttackRepository = clanRaidAttackRepository;
        this.playerRepository = playerRepository;
        this.gameplayConfig = gameplayConfig;
    }
}
