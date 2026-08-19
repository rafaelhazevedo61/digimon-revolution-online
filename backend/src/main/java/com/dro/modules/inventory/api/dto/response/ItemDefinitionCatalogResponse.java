package com.dro.modules.inventory.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Inventário.
 */
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