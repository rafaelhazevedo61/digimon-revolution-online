package com.dro.modules.digitama.infra;


import com.dro.modules.digitama.domain.DigitamaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Digitama.
 */
public interface DigitamaHistoryRepository extends JpaRepository<DigitamaHistory, UUID> {

    List<DigitamaHistory> findByPlayerIdOrderByHatchedAtDesc(UUID playerId);
}