package com.dro.modules.incubation.infra;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncubationRepository extends JpaRepository<Incubation, UUID> {

    Optional<Incubation> findByPlayerIdAndStatus(UUID playerId, IncubationStatus status);
}
