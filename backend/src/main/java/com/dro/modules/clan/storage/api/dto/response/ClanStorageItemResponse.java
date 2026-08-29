package com.dro.modules.clan.storage.api.dto.response;

import java.util.UUID;

public record ClanStorageItemResponse(
        UUID id,
        Long itemDefinitionId,
        String code,
        String name,
        String description,
        String icon,
        String category,
        boolean stackable,
        Integer maxStack,
        String rarity,
        int quantity
) {
}
