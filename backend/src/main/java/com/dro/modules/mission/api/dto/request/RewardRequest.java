package com.dro.modules.mission.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RewardRequest(
        @NotNull ItemType itemType,
        @Min(1) int baseQuantity
) {}
