package com.dro.modules.arena.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

/**
 * Contrato de dados do módulo de Arena.
 */
public record BuyArenaShopResponse(
        String productCode,
        String productName,
        ItemType itemType,
        int quantity,
        int totalPrice,
        int arenaCoinsBalance,
        String message
) {}
