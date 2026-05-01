package com.dro.modules.player.api.dto;

import com.dro.modules.inventory.domain.ItemType;

public record InventorySummaryResponse(
        ItemType itemType,
        int quantity
) {}
