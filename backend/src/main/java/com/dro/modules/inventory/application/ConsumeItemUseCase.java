package com.dro.modules.inventory.application;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsumeItemUseCase {

    private final InventoryRepository inventoryRepository;

    public void execute(UUID digimonId, ItemType itemType, int quantity) {

        InventoryItem item = inventoryRepository
                .findByDigimonIdAndItemType(digimonId, itemType)
                .orElseThrow(() ->
                        new RuntimeException("Item not found in inventory"));

        if (item.getQuantity() < quantity) {
            throw new RuntimeException("Not enough items");
        }

        item.setQuantity(item.getQuantity() - quantity);

        if (item.getQuantity() == 0) {
            inventoryRepository.delete(item);
        } else {
            inventoryRepository.save(item);
        }
    }
}
