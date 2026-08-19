package com.dro.modules.player.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record InventorySummaryResponse(
        ItemType itemType,
        int quantity
) {}
