package com.dro.modules.loot.domain;

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
 * <p>A primeira etapa escolhe a raridade pelos pesos da tabela. A segunda etapa
 * escolhe entre uma e quatro entradas distintas, também ponderadas, e sorteia a
 * quantidade de cada entrada dentro do intervalo configurado.</p>
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

        LootRarity rarity = rollRarity(lootTable);
        List<LootTableEntryEntity> eligibleEntries = lootTable.getEntries().stream()
                .filter(LootTableEntryEntity::isActive)
                .filter(entry -> entry.getRarity() == rarity)
                .toList();

        if (eligibleEntries.size() < lootTable.getMinItems()) {
            throw new UnprocessableException(
                    "Loot table does not have enough active entries for rarity " + rarity);
        }

        int requestedItemCount = lootTable.getMinItems()
                + random.nextInt(lootTable.getMaxItems() - lootTable.getMinItems() + 1);
        int itemCount = Math.min(requestedItemCount, eligibleEntries.size());

        List<LootTableEntryEntity> available = new ArrayList<>(eligibleEntries);
        List<ChestLootItem> items = new ArrayList<>(itemCount);
        for (int index = 0; index < itemCount; index++) {
            LootTableEntryEntity entry = removeWeightedEntry(available);
            int quantity = entry.getMinQuantity()
                    + random.nextInt(entry.getMaxQuantity() - entry.getMinQuantity() + 1);
            items.add(new ChestLootItem(
                    entry.getItemType(),
                    entry.getMaterialCode(),
                    quantity
            ));
        }

        return new ChestLootRoll(rarity, items);
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

        if (lootTable.getEntries().stream().noneMatch(LootTableEntryEntity::isActive)) {
            throw new UnprocessableException("Loot table has no active entries");
        }
    }

    private LootRarity rollRarity(LootTableEntity lootTable) {
        long totalWeight = lootTable.getRarityWeights().stream()
                .mapToLong(LootTableRarityWeightEntity::getWeight)
                .sum();
        long roll = random.nextLong(totalWeight);
        long accumulated = 0;

        for (LootTableRarityWeightEntity weight : lootTable.getRarityWeights()) {
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

    /** Item sorteado com seu tipo, código opcional e quantidade. */
    public record ChestLootItem(ItemType itemType, String materialCode, int quantity) {
    }
}
