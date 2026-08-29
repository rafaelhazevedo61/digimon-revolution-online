package com.dro.modules.digimon.infra;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Digimon.
 */
public interface DigimonRepository extends JpaRepository<Digimon, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Digimon d WHERE d.id = :id")
    java.util.Optional<Digimon> findByIdForUpdate(@Param("id") UUID id);

    List<Digimon> findByPlayerId(UUID playerId);

    List<Digimon> findByPlayerIdAndStatus(UUID playerId, DigimonStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Digimon d WHERE d.playerId = :playerId AND d.id IN :digimonIds")
    List<Digimon> findAllByIdForUpdate(@Param("playerId") UUID playerId, @Param("digimonIds") List<UUID> digimonIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Digimon d WHERE d.playerId = :playerId AND d.status = :status")
    List<Digimon> findByPlayerIdAndStatusForUpdate(@Param("playerId") UUID playerId, @Param("status") DigimonStatus status);

    long countByPlayerIdAndStatus(UUID playerId, DigimonStatus status);

    long countByPlayerIdIsNull();

    long countByStatus(DigimonStatus status);

    @Query("SELECT d FROM Digimon d WHERE d.status = :status AND d.bot = false " +
            "ORDER BY d.level DESC, d.experience DESC")
    Page<Digimon> findByStatusOrderByLevelDescExperienceDesc(@Param("status") DigimonStatus status, Pageable pageable);

    @Query("SELECT d FROM Digimon d WHERE d.status = :status AND d.bot = false ORDER BY " +
            "CASE d.grade WHEN 'SSS' THEN 0 WHEN 'SS' THEN 1 WHEN 'S' THEN 2 " +
            "WHEN 'A' THEN 3 WHEN 'B' THEN 4 WHEN 'C' THEN 5 WHEN 'D' THEN 6 WHEN 'E' THEN 7 END ASC, " +
            "d.level DESC")
    Page<Digimon> findByStatusOrderByGradeQualityAscLevelDesc(@Param("status") DigimonStatus status, Pageable pageable);

    @Query("SELECT d FROM Digimon d WHERE d.status = :status AND d.bot = false AND d.rebirthCount > :rebirthCount " +
            "ORDER BY d.rebirthCount DESC, d.level DESC")
    Page<Digimon> findByStatusAndRebirthCountGreaterThanOrderByRebirthCountDescLevelDesc(
            @Param("status") DigimonStatus status, @Param("rebirthCount") int rebirthCount, Pageable pageable);

    List<Digimon> findByStatusAndPlayerIdNot(DigimonStatus status, UUID playerId);

    @Query("SELECT d FROM Digimon d WHERE d.status = :status AND d.bot = false " +
            "ORDER BY d.arenaRating DESC, d.level DESC")
    Page<Digimon> findByStatusOrderByArenaRatingDescLevelDesc(@Param("status") DigimonStatus status, Pageable pageable);

}
