package com.dro.modules.inventory.application;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddItemUseCase {

    private final InventoryRepository repository;

    public void execute(UUID playerId, ItemType type, int quantity) {

        var existing = repository.findByPlayerIdAndItemType(playerId, type);

        if (existing.isPresent()) {
            InventoryItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            repository.save(item);
        } else {
            InventoryItem item = InventoryItem.builder()
                    .id(UUID.randomUUID())
                    .playerId(playerId)
                    .itemType(type)
                    .quantity(quantity)
                    .build();

            repository.save(item);
        }
    }
}
