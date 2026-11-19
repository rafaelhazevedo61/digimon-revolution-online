package com.dro.modules.mission.api.dto.request;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.mission.domain.Area;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Contrato de dados do módulo de Missões.
 */
public record UpdateMissionRequest(
        @NotBlank String name,
        String description,
        @NotNull Area area,
        @NotNull Stage requiredStage,
        @Min(1) int requiredLevel,
        @Min(0) int baseXp,
        @Min(0) int baseBits,
        @Min(1) int energyCost,
        @Min(1) int durationSeconds,
        @Size(max = 100) String chestCode,
        @Valid List<RewardRequest> rewards,
        @Valid List<LootChanceRequest> lootChances,
        @Valid List<LootItemRequest> lootItems
) {}
