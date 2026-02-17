package com.dro.modules.inventory.api;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.NotNull;

public record UseItemRequest(
        @NotNull
        ItemType itemType
) {}