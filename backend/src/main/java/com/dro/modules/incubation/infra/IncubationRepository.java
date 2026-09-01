package com.dro.modules.incubation.infra;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Incubação.
 */
public interface IncubationRepository extends JpaRepository<Incubation, UUID> {

    List<Incubation> findByPlayerIdAndStatusNotOrderBySlotNumberAsc(UUID playerId, IncubationStatus status);

    Optional<Incubation> findByPlayerIdAndSlotNumberAndStatusNot(UUID playerId, int slotNumber, IncubationStatus status);

    @Query("SELECT i.id FROM Incubation i WHERE i.status IN :statuses AND i.autoClaimEnabled = true AND i.finishAt <= :finishAt ORDER BY i.finishAt ASC")
    List<UUID> findIdsReadyForAutomation(@Param("statuses") List<IncubationStatus> statuses, @Param("finishAt") LocalDateTime finishAt, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Incubation i WHERE i.id = :id")
    Optional<Incubation> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Incubation i WHERE i.id = :id AND i.playerId = :playerId")
    Optional<Incubation> findByIdAndPlayerIdForUpdate(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId
    );
}
