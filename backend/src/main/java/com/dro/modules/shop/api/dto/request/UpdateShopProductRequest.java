package com.dro.modules.shop.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.shop.domain.ShopProductType;
import com.dro.modules.shop.domain.enums.ShopProductCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Loja.
 */
public record UpdateShopProductRequest(
        @NotBlank String name,
        String description,
        @NotNull ShopProductType productType,
        @NotNull ShopProductCategory category,
        ItemType itemType,
        String equipmentTemplateName,
        @Min(0) int price,
        @Min(0) int sellPrice
) {}
