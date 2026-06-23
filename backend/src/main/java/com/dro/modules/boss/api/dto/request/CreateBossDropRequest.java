package com.dro.modules.boss.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBossDropRequest(
        @NotBlank String dropType,
        String itemCode,
        String templateName,
        String equipmentRarity,
        @NotNull Integer chance,
        @NotNull Integer minQuantity,
        @NotNull Integer maxQuantity
) {}
