package com.dro.modules.inventory.api.dto.response;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Inventário.
 */
public record GrantItemResponse(
        UUID digimonId,
        String itemCode,
        int quantity,
        String message
) {
}
