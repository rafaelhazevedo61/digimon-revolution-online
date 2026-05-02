package com.dro.modules.shop.api.dto.response;

import com.dro.modules.shop.domain.ShopProductType;

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
