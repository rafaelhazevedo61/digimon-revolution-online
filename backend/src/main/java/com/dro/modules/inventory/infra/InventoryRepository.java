package com.dro.modules.inventory.infra;

import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Componente da camada de repositório de persistência do módulo de Inventário. */
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {
    List<InventoryItem> findByPlayerId(UUID playerId);
    void deleteByPlayerId(UUID playerId);

    long countByPlayerIdIsNull();

    @Query("SELECT COALESCE(SUM(item.quantity), 0) FROM InventoryItem item")
    long sumQuantities();
    /** @deprecated Inventário não deve ser excluído ao remover um Digimon. */
    @Deprecated
    default void deleteByDigimonId(UUID digimonId) { /* no-op: ownership is player-scoped */ }
    Optional<InventoryItem> findByPlayerIdAndItemType(UUID playerId, ItemType itemType);
    Optional<InventoryItem> findByPlayerIdAndItemDefinitionId(UUID playerId, Long itemDefinitionId);

    /** Compatibilidade durante a migração: resolve a posse através do Digimon proprietário. */
    @Query("SELECT item FROM InventoryItem item WHERE item.playerId = (SELECT digimon.playerId FROM Digimon digimon WHERE digimon.id = :digimonId)")
    List<InventoryItem> findByDigimonId(@Param("digimonId") UUID digimonId);

    @Query("SELECT item FROM InventoryItem item WHERE item.playerId = (SELECT digimon.playerId FROM Digimon digimon WHERE digimon.id = :digimonId) AND item.itemType = :itemType")
    Optional<InventoryItem> findByDigimonIdAndItemType(@Param("digimonId") UUID digimonId, @Param("itemType") ItemType itemType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.playerId = (SELECT digimon.playerId FROM Digimon digimon WHERE digimon.id = :digimonId) AND item.itemType = :itemType")
    Optional<InventoryItem> findByDigimonIdAndItemTypeForUpdate(@Param("digimonId") UUID digimonId, @Param("itemType") ItemType itemType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.playerId = (SELECT digimon.playerId FROM Digimon digimon WHERE digimon.id = :digimonId) AND item.itemDefinition.id = :itemDefinitionId")
    Optional<InventoryItem> findByDigimonIdAndItemDefinitionIdForUpdate(@Param("digimonId") UUID digimonId, @Param("itemDefinitionId") Long itemDefinitionId);

    @Query("SELECT item FROM InventoryItem item WHERE item.playerId = (SELECT digimon.playerId FROM Digimon digimon WHERE digimon.id = :digimonId) AND item.itemDefinition.id = :itemDefinitionId")
    Optional<InventoryItem> findByDigimonIdAndItemDefinitionId(@Param("digimonId") UUID digimonId, @Param("itemDefinitionId") Long itemDefinitionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.playerId = :playerId AND item.itemDefinition.id = :itemDefinitionId")
    Optional<InventoryItem> findByPlayerIdAndItemDefinitionIdForUpdate(
            @Param("playerId") UUID playerId,
            @Param("itemDefinitionId") Long itemDefinitionId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM InventoryItem item WHERE item.playerId = :playerId AND item.itemType = :itemType")
    Optional<InventoryItem> findByPlayerIdAndItemTypeForUpdate(
            @Param("playerId") UUID playerId,
            @Param("itemType") ItemType itemType
    );
}
