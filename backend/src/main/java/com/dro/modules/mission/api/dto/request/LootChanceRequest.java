package com.dro.modules.mission.api.dto.request;

import com.dro.modules.loot.domain.LootRarity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Missões.
 */
public record LootChanceRequest(
        @NotNull LootRarity rarity,
        @Min(1) int chance
) {}
