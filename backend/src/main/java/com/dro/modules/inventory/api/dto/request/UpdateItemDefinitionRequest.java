package com.dro.modules.inventory.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Campos administrativos editáveis do catálogo de itens.
 */
public record UpdateItemDefinitionRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 40) String category,
        @NotNull Boolean stackable,
        @Min(0) Integer buyPrice,
        @Min(0) Integer sellPrice,
        @NotNull Boolean tradable,
        @NotNull Boolean sellable,
        @NotNull Boolean usable,
        @Min(1) Integer maxStack,
        @NotBlank @Size(max = 20) String rarity,
        @Size(max = 120) String icon
) {
}
