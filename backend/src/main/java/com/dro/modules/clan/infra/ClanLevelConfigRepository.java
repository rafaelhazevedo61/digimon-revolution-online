package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanLevelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Componente da camada de repositório de persistência do módulo de Clãs.
 */
public interface ClanLevelConfigRepository extends JpaRepository<ClanLevelConfig, Integer> {
}
