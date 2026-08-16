package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidAttackResponse;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidRankingEntryResponse;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidResponse;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import com.dro.modules.clan.raid.domain.ClanRaidRules;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
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

@Component
@RequiredArgsConstructor
public class ClanRaidResponseMapper {

    private final BossDefinitionRepository bossDefinitionRepository;
    private final ClanRaidAttackRepository clanRaidAttackRepository;
    private final PlayerRepository playerRepository;

    public ClanRaidResponse toResponse(ClanRaid raid, UUID viewerPlayerId) {
        BossDefinitionEntity boss = bossDefinitionRepository.findById(raid.getBossId())
                .orElseThrow(() -> new NotFoundException("Boss not found"));

        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        int usedToday = (int) clanRaidAttackRepository
                .countByClanRaidIdAndPlayerIdAndCreatedAtGreaterThanEqual(raid.getId(), viewerPlayerId, startOfDay);
        long myTotalDamage = clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raid.getId()).stream()
                .filter(a -> a.getPlayerId().equals(viewerPlayerId))
                .mapToLong(ClanRaidAttack::getDamage)
                .sum();

        List<ClanRaidAttackResponse> recentAttacks = clanRaidAttackRepository
                .findByClanRaidIdOrderByCreatedAtDesc(raid.getId()).stream()
                .limit(20)
                .map(this::toAttackResponse)
                .toList();

        return new ClanRaidResponse(
                raid.getId(),
                raid.getClanId(),
                boss.getCode(),
                boss.getName(),
                boss.getImageUrl(),
                raid.getMaxHp(),
                raid.getRemainingHp(),
                raid.getStatus(),
                raid.getCreatedAt(),
                raid.getDefeatedAt(),
                usedToday,
                Math.max(0, ClanRaidRules.DAILY_ATTACK_LIMIT - usedToday),
                myTotalDamage,
                buildRanking(raid.getId()),
                recentAttacks
        );
    }

    private List<ClanRaidRankingEntryResponse> buildRanking(UUID raidId) {
        Map<UUID, Long> damageByPlayer = clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raidId).stream()
                .collect(Collectors.groupingBy(
                        ClanRaidAttack::getPlayerId,
                        Collectors.summingLong(ClanRaidAttack::getDamage)
                ));

        List<UUID> playerIds = damageByPlayer.keySet().stream().toList();
        Map<UUID, String> usernames = playerIds.isEmpty()
                ? Collections.emptyMap()
                : playerRepository.findAllById(playerIds).stream()
                        .collect(Collectors.toMap(Player::getId, Player::getUsername));

        List<Map.Entry<UUID, Long>> sorted = damageByPlayer.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .toList();

        List<ClanRaidRankingEntryResponse> ranking = new ArrayList<>();
        int position = 1;
        for (Map.Entry<UUID, Long> entry : sorted) {
            ranking.add(new ClanRaidRankingEntryResponse(
                    position++,
                    entry.getKey(),
                    usernames.getOrDefault(entry.getKey(), "Unknown"),
                    entry.getValue()
            ));
        }
        return ranking;
    }

    private ClanRaidAttackResponse toAttackResponse(ClanRaidAttack attack) {
        Player player = playerRepository.findById(attack.getPlayerId()).orElse(null);
        return new ClanRaidAttackResponse(
                attack.getId(),
                attack.getPlayerId(),
                player != null ? player.getUsername() : "Unknown",
                attack.getDamage(),
                attack.getCreatedAt()
        );
    }
}
