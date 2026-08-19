package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de repositório de persistência do módulo de Clãs.
 */
public interface ClanMissionRepository extends JpaRepository<ClanMission, UUID> {

    Optional<ClanMission> findByCode(String code);
}
