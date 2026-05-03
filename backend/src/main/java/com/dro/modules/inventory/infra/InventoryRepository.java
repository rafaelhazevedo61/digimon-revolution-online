package com.dro.modules.inventory.infra;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    List<InventoryItem> findByDigimonId(UUID digimonId);

    Optional<InventoryItem> findByDigimonIdAndItemType(UUID digimonId, ItemType itemType);

    Optional<InventoryItem> findByDigimonIdAndItemDefinitionId(
            UUID digimonId, Long itemDefinitionId);
}
