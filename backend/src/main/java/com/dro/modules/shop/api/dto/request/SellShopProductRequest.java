package com.dro.modules.shop.api.dto.request;

import jakarta.validation.constraints.Min;

import java.util.UUID;

public record SellShopProductRequest(
        String productCode,
        UUID equipmentId,
        String itemType,
        @Min(1) int quantity
) {
}
