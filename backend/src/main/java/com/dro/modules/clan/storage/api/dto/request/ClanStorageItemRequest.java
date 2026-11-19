package com.dro.modules.clan.storage.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ClanStorageItemRequest(
        @NotNull
        Long itemDefinitionId,
        @NotNull
        @Min(1)
        @Max(999)
        Integer quantity
) {
}
