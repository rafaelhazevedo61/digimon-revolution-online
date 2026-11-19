package com.dro.modules.loot.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.loot.domain.LootRarity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Configuração completa de uma Loot Table administrável.
 *
 * @param code código estável e único da tabela
 * @param name nome público da configuração administrativa
 * @param description descrição operacional da tabela
 * @param minItems quantidade mínima de tipos distintos na abertura
 * @param maxItems quantidade máxima de tipos distintos na abertura
 * @param rarityWeights pesos das raridades oficiais
 * @param entries entradas catalogadas da pool
 * @param active indica se a tabela pode ser usada pelo jogo
 */
public record LootTableAdminRequest(
        @NotBlank
        @Size(max = 80)
        @Pattern(regexp = "[A-Z0-9_]+", message = "O código deve usar apenas A-Z, 0-9 e underscore.")
        String code,
        @NotBlank
        @Size(max = 120)
        String name,
        @Size(max = 5000)
        String description,
        @NotNull
        @Min(1)
        @Max(4)
        Integer minItems,
        @NotNull
        @Min(1)
        @Max(4)
        Integer maxItems,
        @NotEmpty
        List<@Valid LootTableRarityWeightRequest> rarityWeights,
        @NotEmpty
        List<@Valid LootTableEntryRequest> entries,
        Boolean active
) {

    /** Peso de uma raridade dentro da tabela. */
    public record LootTableRarityWeightRequest(
            @NotNull LootRarity rarity,
            @NotNull @Min(1) Integer weight
    ) {
    }

    /** Entrada individual apontando para um item catalogado. */
    public record LootTableEntryRequest(
            @NotNull LootRarity rarity,
            @NotNull ItemType itemType,
            @Size(max = 80) String materialCode,
            @Size(max = 120) String equipmentTemplateName,
            EquipmentRarity equipmentRarity,
            @NotNull @Min(1) Integer weight,
            @NotNull @Min(1) Integer minQuantity,
            @NotNull @Min(1) Integer maxQuantity,
            Boolean active
    ) {
        public LootTableEntryRequest(
                LootRarity rarity,
                ItemType itemType,
                String materialCode,
                Integer weight,
                Integer minQuantity,
                Integer maxQuantity,
                Boolean active
        ) {
            this(rarity, itemType, materialCode, null, null, weight, minQuantity, maxQuantity, active);
        }
    }
}
