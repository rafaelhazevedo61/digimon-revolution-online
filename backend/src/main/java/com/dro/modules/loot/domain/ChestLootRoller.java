package com.dro.modules.loot.domain;

import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.shared.exception.UnprocessableException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Executa o sorteio em duas etapas de uma abertura de baú.
 *
 * <p>A abertura define entre uma e quatro entradas distintas. Para cada entrada,
 * a raridade é sorteada pelos pesos das raridades que ainda possuem entradas
 * ativas disponíveis; em seguida, a entrada é sorteada dentro da pool daquela
 * raridade. Assim, uma abertura pode combinar Common, Rare, Epic e Legendary.</p>
 */
@Component
public class ChestLootRoller {

    private final RandomGenerator random;

    public ChestLootRoller() {
        this(new SecureRandom());
    }

    ChestLootRoller(RandomGenerator random) {
        this.random = random;
    }

    /**
     * Sorteia o resultado de uma abertura sem alterar o inventário.
     *
     * @param lootTable tabela ativa carregada com pesos e entradas
     * @return raridade e itens sorteados
     * @throws UnprocessableException quando a tabela não possui configuração
     *                                elegível para a abertura
     */
    public ChestLootRoll roll(LootTableEntity lootTable) {
        validateTable(lootTable);

        int requestedItemCount = lootTable.getMinItems()
                + random.nextInt(lootTable.getMaxItems() - lootTable.getMinItems() + 1);
        List<LootTableEntryEntity> available = lootTable.getEntries().stream()
                .filter(LootTableEntryEntity::isActive)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        int itemCount = Math.min(requestedItemCount, available.size());

        List<ChestLootItem> items = new ArrayList<>(itemCount);
        LootRarity firstRarity = null;
        for (int index = 0; index < itemCount; index++) {
            LootRarity rarity = rollRarity(lootTable, available);
            if (firstRarity == null) {
                firstRarity = rarity;
            }
            List<LootTableEntryEntity> eligibleEntries = new ArrayList<>(available.stream()
                    .filter(entry -> entry.getRarity() == rarity)
                    .toList());
            LootTableEntryEntity entry = removeWeightedEntry(eligibleEntries);
            available.remove(entry);
            int quantity = entry.getMinQuantity()
                    + random.nextInt(entry.getMaxQuantity() - entry.getMinQuantity() + 1);
            items.add(new ChestLootItem(
                    rarity,
                    entry.getItemType(),
                    entry.getMaterialCode(),
                    entry.getEquipmentTemplateName(),
                    entry.getEquipmentRarity(),
                    quantity
            ));
        }

        return new ChestLootRoll(firstRarity, items);
    }

    private void validateTable(LootTableEntity lootTable) {
        if (lootTable == null || !lootTable.isActive()) {
            throw new UnprocessableException("Loot table is not active");
        }

        Map<LootRarity, Integer> weights = new EnumMap<>(LootRarity.class);
        lootTable.getRarityWeights().forEach(weight ->
                weights.put(weight.getRarity(), weight.getWeight()));
        try {
            LootTableRules.validateRarityWeights(weights);
            LootTableRules.validateItemCount(lootTable.getMinItems(), lootTable.getMaxItems());
        } catch (IllegalArgumentException exception) {
            throw new UnprocessableException("Loot table configuration is invalid");
        }

        long activeEntryCount = lootTable.getEntries().stream()
                .filter(LootTableEntryEntity::isActive)
                .count();
        if (activeEntryCount == 0) {
            throw new UnprocessableException("Loot table has no active entries");
        }
        if (activeEntryCount < lootTable.getMinItems()) {
            throw new UnprocessableException("Loot table does not have enough active entries for the configured minimum");
        }
    }

    private LootRarity rollRarity(
            LootTableEntity lootTable,
            List<LootTableEntryEntity> availableEntries
    ) {
        List<LootTableRarityWeightEntity> eligibleWeights = lootTable.getRarityWeights().stream()
                .filter(weight -> weight.getWeight() > 0)
                .filter(weight -> availableEntries.stream()
                        .anyMatch(entry -> entry.getRarity() == weight.getRarity()))
                .toList();
        long totalWeight = eligibleWeights.stream()
                .mapToLong(LootTableRarityWeightEntity::getWeight)
                .sum();
        if (totalWeight <= 0) {
            throw new UnprocessableException("No active loot entries match the configured rarity weights");
        }

        long roll = random.nextLong(totalWeight);
        long accumulated = 0;
        for (LootTableRarityWeightEntity weight : eligibleWeights) {
            accumulated += weight.getWeight();
            if (roll < accumulated) {
                return weight.getRarity();
            }
        }

        throw new UnprocessableException("Could not draw loot rarity");
    }

    private LootTableEntryEntity removeWeightedEntry(List<LootTableEntryEntity> available) {
        long totalWeight = available.stream()
                .mapToLong(LootTableEntryEntity::getWeight)
                .sum();
        if (totalWeight <= 0) {
            throw new UnprocessableException("Loot table entry weights are invalid");
        }

        long roll = random.nextLong(totalWeight);
        long accumulated = 0;
        for (int index = 0; index < available.size(); index++) {
            LootTableEntryEntity entry = available.get(index);
            accumulated += entry.getWeight();
            if (roll < accumulated) {
                available.remove(index);
                return entry;
            }
        }

        throw new UnprocessableException("Could not draw loot entry");
    }

    /** Resultado completo do sorteio, ainda não persistido. */
    public record ChestLootRoll(LootRarity rarity, List<ChestLootItem> items) {
    }

    /** Item sorteado com raridade, tipo, código opcional e quantidade. */
    public record ChestLootItem(
            LootRarity rarity,
            ItemType itemType,
            String materialCode,
            String equipmentTemplateName,
            EquipmentRarity equipmentRarity,
            int quantity
    ) {
        public ChestLootItem(LootRarity rarity, ItemType itemType, String materialCode, int quantity) {
            this(rarity, itemType, materialCode, null, null, quantity);
        }
    }
}
