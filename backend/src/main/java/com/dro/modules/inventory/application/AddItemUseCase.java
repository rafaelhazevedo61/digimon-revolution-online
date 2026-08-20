package com.dro.modules.inventory.application;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.shared.exception.UnprocessableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Inventário.
 */
@Service
@RequiredArgsConstructor
public class AddItemUseCase {

    private final InventoryRepository repository;

    public void execute(UUID digimonId, ItemType type, int quantity) {

        var existing = repository.findByDigimonIdAndItemType(digimonId, type);

        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            repository.save(item);
        } else {
            InventoryItem item = InventoryItem.builder()
                    .id(UUID.randomUUID())
                    .digimonId(digimonId)
                    .itemType(type)
                    .quantity(quantity)
                    .build();

            repository.save(item);
        }
    }

    public void addMaterial(UUID digimonId, ItemDefinition itemDefinition, int quantity) {

        var existing = repository.findByDigimonIdAndItemDefinitionId(
                digimonId, itemDefinition.getId());

        int currentQuantity = existing.map(InventoryItem::getQuantity).orElse(0);
        int newQuantity = currentQuantity + quantity;

        if (itemDefinition.getMaxStack() != null && newQuantity > itemDefinition.getMaxStack()) {
            throw new UnprocessableException(
                    "Cannot exceed max stack of " + itemDefinition.getMaxStack()
                            + " for item " + itemDefinition.getCode()
                            + ". Current: " + currentQuantity + ", adding: " + quantity);
        }

        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.setQuantity(newQuantity);
            repository.save(item);
        } else {
            InventoryItem item = InventoryItem.builder()
                    .id(UUID.randomUUID())
                    .digimonId(digimonId)
                    .itemType(resolveItemType(itemDefinition))
                    .itemDefinition(itemDefinition)
                    .quantity(newQuantity)
                    .build();

            repository.save(item);
        }
    }

    private ItemType resolveItemType(ItemDefinition itemDefinition) {
        if ("CHEST".equalsIgnoreCase(itemDefinition.getCategory())) {
            return ItemType.LOOT_CHEST;
        }

        try {
            return ItemType.valueOf(itemDefinition.getCode());
        } catch (IllegalArgumentException e) {
            return ItemType.EVOLUTION_MATERIAL;
        }
    }
}
