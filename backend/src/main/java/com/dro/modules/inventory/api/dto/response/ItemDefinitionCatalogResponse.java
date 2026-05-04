package com.dro.modules.inventory.api.dto.response;

import java.util.List;

public record ItemDefinitionCatalogResponse(
        int total,
        List<ItemDefinitionResponse> items
) {
    public static ItemDefinitionCatalogResponse from(List<ItemDefinitionResponse> items) {
        return new ItemDefinitionCatalogResponse(
                items.size(),
                items
        );
    }
}