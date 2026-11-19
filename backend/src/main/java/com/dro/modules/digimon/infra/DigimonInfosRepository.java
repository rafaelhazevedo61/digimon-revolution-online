package com.dro.modules.digimon.infra;

import com.dro.modules.digimon.domain.DigimonInfos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Componente da camada de repositório de persistência do módulo de Digimon.
 */
public interface DigimonInfosRepository extends JpaRepository<DigimonInfos, Long>, JpaSpecificationExecutor<DigimonInfos> {

    Optional<DigimonInfos> findByName(String name);
}