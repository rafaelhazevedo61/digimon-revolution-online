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

        int totalWeight = 0;
        for (LootRarity rarity : OFFICIAL_RARITIES) {
            Integer weight = weights.get(rarity);
            if (weight == null || weight < 0) {
                throw new IllegalArgumentException("Rarity weight cannot be negative for " + rarity);
            }
            totalWeight += weight;
        }
        if (totalWeight <= 0) {
            throw new IllegalArgumentException("At least one rarity weight must be positive");
        }
    }

    /**
     * Valida o intervalo de tipos distintos que uma abertura pode entregar.
     *
     * @param minItems quantidade mínima de tipos
     * @param maxItems quantidade máxima de tipos
     */
    public static void validateItemCount(int minItems, int maxItems) {
        if (minItems < 1 || maxItems < minItems || maxItems > 4) {
            throw new IllegalArgumentException("Loot table item count must be between 1 and 4");
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
