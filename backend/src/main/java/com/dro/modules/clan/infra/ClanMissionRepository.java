package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClanMissionRepository extends JpaRepository<ClanMission, UUID> {

    Optional<ClanMission> findByCode(String code);
}
