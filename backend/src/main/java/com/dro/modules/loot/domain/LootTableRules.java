package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

/**
 * Validações puras para a configuração de loot tables.
 */
public final class LootTableRules {

    private static final EnumSet<LootRarity> OFFICIAL_RARITIES = EnumSet.allOf(LootRarity.class);

    private LootTableRules() {
    }

    /**
     * Valida pesos de raridade antes de persistir ou usar uma tabela.
     *
     * @param weights pesos por raridade
     * @throws IllegalArgumentException quando houver raridade ausente, peso
     *                                  inválido ou soma não positiva
     */
    public static void validateRarityWeights(Map<LootRarity, Integer> weights) {
        Objects.requireNonNull(weights, "Rarity weights cannot be null");

        if (!weights.keySet().containsAll(OFFICIAL_RARITIES)) {
            throw new IllegalArgumentException("All official rarities must have a configured weight");
        }

        for (LootRarity rarity : OFFICIAL_RARITIES) {
            Integer weight = weights.get(rarity);
            if (weight == null || weight <= 0) {
                throw new IllegalArgumentException("Rarity weight must be positive for " + rarity);
            }
        }
    }

    /**
     * Valida uma entrada individual de loot.
     *
     * @param itemType tipo do item
     * @param materialCode código obrigatório para material nomeado ou baú
     * @param weight peso positivo da entrada
     * @param minQuantity menor quantidade possível
     * @param maxQuantity maior quantidade possível
     */
    public static void validateEntry(
            ItemType itemType,
            String materialCode,
            int weight,
            int minQuantity,
            int maxQuantity
    ) {
        Objects.requireNonNull(itemType, "Item type cannot be null");

        if (weight <= 0) {
            throw new IllegalArgumentException("Loot entry weight must be positive");
        }
        if (minQuantity <= 0 || maxQuantity < minQuantity) {
            throw new IllegalArgumentException("Loot entry quantity range is invalid");
        }

        boolean requiresCode = itemType == ItemType.EVOLUTION_MATERIAL
                || itemType == ItemType.LOOT_CHEST;
        if (requiresCode && (materialCode == null || materialCode.isBlank())) {
            throw new IllegalArgumentException("Material code is required for " + itemType);
        }
    }
}
