package com.dro.modules.loot.infra;

import com.dro.modules.loot.domain.ChestOpeningEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acesso aos registros idempotentes de abertura de baús.
 */
public interface ChestOpeningRepository extends JpaRepository<ChestOpeningEntity, Long> {

    Optional<ChestOpeningEntity> findByRequestId(String requestId);
}
