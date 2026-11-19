package com.dro.modules.collection.infra;

import com.dro.modules.collection.domain.CollectionEntry;
import com.dro.modules.digimon.domain.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface CollectionEntryRepository extends JpaRepository<CollectionEntry, UUID> {
    List<CollectionEntry> findByPlayerIdOrderByDiscoveredAtDesc(UUID playerId);
    boolean existsByPlayerIdAndDigimonInfoIdAndRarity(UUID playerId, Long digimonInfoId, Rarity rarity);
    @Query("select count(e) from CollectionEntry e where e.playerId = :playerId")
    long countByPlayer(@Param("playerId") UUID playerId);
    @Query("select count(distinct e.rarity) from CollectionEntry e where e.playerId = :playerId")
    long countDistinctRarities(@Param("playerId") UUID playerId);
    @Query("select count(distinct e.rarity) from CollectionEntry e where e.playerId = :playerId and e.digimonInfoId = :infoId")
    long countRaritiesForSpecies(@Param("playerId") UUID playerId, @Param("infoId") Long infoId);
    @Query("select count(distinct e.digimonInfoId) from CollectionEntry e where e.playerId = :playerId")
    long countAddedDigimons(@Param("playerId") UUID playerId);
    @Query(value = "select count(*) from (select digimon_info_id from digimon_collection_entries where player_id = :playerId group by digimon_info_id having count(distinct rarity) = 4) complete", nativeQuery = true)
    long countCompletedDigimons(@Param("playerId") UUID playerId);
}
