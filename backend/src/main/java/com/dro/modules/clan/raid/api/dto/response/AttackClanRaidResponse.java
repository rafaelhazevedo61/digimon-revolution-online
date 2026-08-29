package com.dro.modules.clan.raid.api.dto.response;

import com.dro.modules.boss.api.dto.response.BossDefeatSummaryResponse;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record AttackClanRaidResponse(
        UUID raidId,
        String bossCode,
        String bossName,
        int damage,
        int remainingHp,
        int maxHp,
        boolean defeated,
        int winChance,
        int xpGained,
        int bitsGained,
        int clanHonorMarksGained,
        int clanXpGained,
        BossDefeatSummaryResponse defeatSummary
) {
}
