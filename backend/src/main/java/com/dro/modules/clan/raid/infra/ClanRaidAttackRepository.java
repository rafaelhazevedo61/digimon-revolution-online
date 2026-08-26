package com.dro.modules.clan.raid.infra;

import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Clãs.
 */
@Repository
public interface ClanRaidAttackRepository extends JpaRepository<ClanRaidAttack, UUID> {

    long countByClanRaidIdAndPlayerIdAndCreatedAtGreaterThanEqual(UUID clanRaidId, UUID playerId, Instant startOfDay);

    long countByClanRaidIdAndCreatedAtGreaterThanEqual(UUID clanRaidId, Instant startOfDay);

    long deleteByCreatedAtGreaterThanEqual(Instant since);

    List<ClanRaidAttack> findByClanRaidIdOrderByCreatedAtDesc(UUID clanRaidId);

    Optional<ClanRaidAttack> findFirstByClanRaidIdAndPlayerIdOrderByCreatedAtDesc(UUID clanRaidId, UUID playerId);

    @Query("SELECT a.playerId, COALESCE(SUM(a.damage), 0) " +
            "FROM ClanRaidAttack a " +
            "WHERE a.clanRaidId = :clanRaidId " +
            "GROUP BY a.playerId " +
            "ORDER BY COALESCE(SUM(a.damage), 0) DESC")
    List<UUID[]> sumDamageByPlayer(@Param("clanRaidId") UUID clanRaidId);
}
