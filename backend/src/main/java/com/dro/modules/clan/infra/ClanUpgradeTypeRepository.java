package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanUpgradeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClanUpgradeTypeRepository extends JpaRepository<ClanUpgradeType, String> {
}
