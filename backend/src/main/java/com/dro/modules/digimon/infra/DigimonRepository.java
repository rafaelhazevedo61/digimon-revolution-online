package com.dro.modules.digimon.infra;

import com.dro.modules.digimon.domain.Digimon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DigimonRepository extends JpaRepository<Digimon, UUID> {

    Optional<Digimon> findByPlayerId(UUID playerId);

}
