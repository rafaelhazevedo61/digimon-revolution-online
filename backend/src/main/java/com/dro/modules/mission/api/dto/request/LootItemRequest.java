package com.dro.modules.mission.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootRarity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LootItemRequest(
        @NotNull LootRarity rarity,
        @NotNull ItemType itemType,
        @Min(1) int quantity
) {}
