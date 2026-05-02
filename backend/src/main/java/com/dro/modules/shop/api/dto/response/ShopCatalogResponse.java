package com.dro.modules.shop.api.dto.response;

import java.util.List;

public record ShopCatalogResponse(
        List<ShopProductResponse> potions,
        List<ShopProductResponse> materials,
        List<ShopProductResponse> fragments,
        List<ShopProductResponse> consumables,
        List<ShopProductResponse> equipments
) {
}