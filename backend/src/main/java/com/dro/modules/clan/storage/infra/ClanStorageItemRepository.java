package com.dro.modules.clan.storage.infra;

import com.dro.modules.clan.storage.domain.ClanStorageItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClanStorageItemRepository extends JpaRepository<ClanStorageItem, UUID> {
    List<ClanStorageItem> findByClanIdOrderByCreatedAtAsc(UUID clanId);

    long countByClanId(UUID clanId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM ClanStorageItem item WHERE item.clanId = :clanId ORDER BY item.createdAt ASC")
    List<ClanStorageItem> findByClanIdForUpdate(@Param("clanId") UUID clanId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM ClanStorageItem item WHERE item.clanId = :clanId AND item.itemDefinition.id = :itemDefinitionId ORDER BY item.createdAt ASC")
    List<ClanStorageItem> findByClanIdAndItemDefinitionIdForUpdate(
            @Param("clanId") UUID clanId,
            @Param("itemDefinitionId") Long itemDefinitionId
    );
}
