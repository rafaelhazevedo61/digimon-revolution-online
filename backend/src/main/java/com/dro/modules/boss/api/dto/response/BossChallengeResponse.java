package com.dro.modules.boss.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record BossChallengeResponse(
        String bossCode,
        String bossName,
        String result,
        int winChance,
        double digimonPower,
        double bossPower,
        int xpGained,
        int bitsGained,
        String chestCode,
        String chestName,
        List<DropRewardResponse> drops
) {}
