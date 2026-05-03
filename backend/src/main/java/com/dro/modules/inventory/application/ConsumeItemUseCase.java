package com.dro.modules.inventory.application;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
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
                        new NotFoundException("Item not found in inventory"));

        if (item.getQuantity() < quantity) {
            throw new UnprocessableException("Not enough items");
        }

        item.setQuantity(item.getQuantity() - quantity);

        if (item.getQuantity() == 0) {
            inventoryRepository.delete(item);
        } else {
            inventoryRepository.save(item);
        }
    }

    public void consumeMaterial(UUID digimonId, Long itemDefinitionId, int quantity) {

        InventoryItem item = inventoryRepository
                .findByDigimonIdAndItemDefinitionId(digimonId, itemDefinitionId)
                .orElseThrow(() ->
                        new NotFoundException("Material not found in inventory"));

        if (item.getQuantity() < quantity) {
            throw new UnprocessableException(
                    "Not enough material. Required: " + quantity + ", has: " + item.getQuantity());
        }

        item.setQuantity(item.getQuantity() - quantity);

        if (item.getQuantity() == 0) {
            inventoryRepository.delete(item);
        } else {
            inventoryRepository.save(item);
        }
    }
}
