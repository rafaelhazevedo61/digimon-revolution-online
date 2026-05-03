package com.dro.modules.inventory.application;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            repository.save(item);
        } else {
            InventoryItem item = InventoryItem.builder()
                    .id(UUID.randomUUID())
                    .digimonId(digimonId)
                    .itemType(ItemType.EVOLUTION_MATERIAL)
                    .itemDefinition(itemDefinition)
                    .quantity(quantity)
                    .build();

            repository.save(item);
        }
    }
}
