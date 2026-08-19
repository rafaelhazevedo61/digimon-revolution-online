package com.dro.modules.shop.api.dto.response;

import com.dro.modules.shop.domain.ShopProductType;

/**
 * Contrato de dados do módulo de Loja.
 */
public record SellShopProductResponse(
        String productCode,
        String name,
        ShopProductType productType,
        int quantity,
        int totalSellPrice,
        int remainingBits,
        String message
) {
}
