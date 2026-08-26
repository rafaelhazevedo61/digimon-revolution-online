package com.dro.modules.incubation.infra;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Incubação.
 */
public interface IncubationRepository extends JpaRepository<Incubation, UUID> {

    List<Incubation> findByPlayerIdAndStatusNotOrderBySlotNumberAsc(UUID playerId, IncubationStatus status);

    Optional<Incubation> findByPlayerIdAndSlotNumberAndStatusNot(UUID playerId, int slotNumber, IncubationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Incubation i WHERE i.id = :id AND i.playerId = :playerId")
    Optional<Incubation> findByIdAndPlayerIdForUpdate(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId
    );
}
