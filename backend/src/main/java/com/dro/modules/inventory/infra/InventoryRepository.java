package com.dro.modules.inventory.infra;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Inventário.
 */
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    List<InventoryItem> findByDigimonId(UUID digimonId);

    Optional<InventoryItem> findByDigimonIdAndItemType(UUID digimonId, ItemType itemType);

    Optional<InventoryItem> findByDigimonIdAndItemDefinitionId(
            UUID digimonId, Long itemDefinitionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.digimonId = :digimonId AND item.itemDefinition.id = :itemDefinitionId")
    Optional<InventoryItem> findByDigimonIdAndItemDefinitionIdForUpdate(
            @Param("digimonId") UUID digimonId,
            @Param("itemDefinitionId") Long itemDefinitionId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.digimonId = :digimonId AND item.itemType = :itemType")
    Optional<InventoryItem> findByDigimonIdAndItemTypeForUpdate(
            @Param("digimonId") UUID digimonId,
            @Param("itemType") ItemType itemType
    );
}
