package com.dro.modules.clan.raid.api.dto.response;

import java.util.UUID;

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
        int dailyAttacksRemaining
) {
}
