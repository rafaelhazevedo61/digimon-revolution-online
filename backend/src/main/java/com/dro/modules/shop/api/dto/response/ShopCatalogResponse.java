package com.dro.modules.shop.api.dto.response;

import java.util.List;

/**
 * Contrato de dados do módulo de Loja.
 */
public record ShopCatalogResponse(
        List<ShopProductResponse> potions,
        List<ShopProductResponse> materials,
        List<ShopProductResponse> fragments,
        List<ShopProductResponse> consumables,
        List<ShopProductResponse> equipments,
        List<ShopProductResponse> chests
) {
}