package com.dro.modules.boss.api.dto.response;

import java.util.List;

public record BossChallengeResponse(
        String bossCode,
        String bossName,
        String result,
        int winChance,
        double digimonPower,
        double bossPower,
        int xpGained,
        int bitsGained,
        List<DropRewardResponse> drops
) {}
