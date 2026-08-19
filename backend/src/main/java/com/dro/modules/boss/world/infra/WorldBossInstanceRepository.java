package com.dro.modules.boss.world.infra;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Boss Mundial.
 */
@Repository
public interface WorldBossInstanceRepository extends JpaRepository<WorldBossInstance, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WorldBossInstance> findFirstByBossDateOrderByCreatedAtDesc(LocalDate bossDate);
}
