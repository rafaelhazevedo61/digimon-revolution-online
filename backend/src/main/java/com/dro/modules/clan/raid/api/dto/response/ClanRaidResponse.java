package com.dro.modules.clan.raid.api.dto.response;

import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.boss.api.dto.response.BossDefeatSummaryResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanRaidResponse(
        UUID id,
        UUID clanId,
        String bossCode,
        String bossName,
        String bossImageUrl,
        int maxHp,
        int remainingHp,
        ClanRaidStatus status,
        Instant createdAt,
        Instant defeatedAt,
        int attackCooldownMinutes,
        boolean cooldownEnabled,
        Instant nextAttackAvailableAt,
        long myTotalDamage,
        List<ClanRaidRankingEntryResponse> ranking,
        List<ClanRaidAttackResponse> recentAttacks,
        BossDefeatSummaryResponse defeatSummary
) {
}
