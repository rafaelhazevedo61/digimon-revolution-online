package com.dro.modules.shop.api.dto;

import com.dro.modules.shop.domain.ShopProductType;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Loja.
 */
public record BuyShopProductResponse(
        String productCode,
        String name,
        ShopProductType productType,
        int quantity,
        int totalPrice,
        int remainingBits,
        UUID equipmentId,
        String message
) {
}