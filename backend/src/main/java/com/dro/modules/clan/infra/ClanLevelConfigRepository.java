package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanLevelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClanLevelConfigRepository extends JpaRepository<ClanLevelConfig, Integer> {
}
