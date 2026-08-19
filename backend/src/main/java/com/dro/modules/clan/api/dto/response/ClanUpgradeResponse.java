package com.dro.modules.clan.api.dto.response;

import java.math.BigDecimal;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanUpgradeResponse(
        String code,
        String name,
        String description,
        int unlockedAtClanLevel,
        int currentLevel,
        int maxLevel,
        int nextCostHonorMarks,
        BigDecimal effectPerLevel,
        double totalEffect,
        boolean unlocked,
        boolean maxed
) {
}
