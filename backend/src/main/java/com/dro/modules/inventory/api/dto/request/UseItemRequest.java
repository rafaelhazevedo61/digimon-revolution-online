package com.dro.modules.inventory.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.NotNull;

public record UseItemRequest(
        @NotNull
        ItemType itemType
) {}