package com.dro.modules.inventory.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UseItemUseCase {

    private final InventoryRepository inventoryRepository;
    private final DigimonRepository digimonRepository;

    public void execute(String token, ItemType type) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        InventoryItem item = inventoryRepository
                .findByPlayerIdAndItemType(playerId, type)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getQuantity() <= 0) {
            throw new RuntimeException("No item available");
        }

        Digimon digimon = digimonRepository
                .findByPlayerId(playerId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        // Remove 1 item
        item.setQuantity(item.getQuantity() - 1);

        // Concede XP
        digimon.gainExperience(50);

        inventoryRepository.save(item);
        digimonRepository.save(digimon);
    }
}
