package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanUpgradeType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Componente da camada de repositório de persistência do módulo de Clãs.
 */
public interface ClanUpgradeTypeRepository extends JpaRepository<ClanUpgradeType, String> {
}
