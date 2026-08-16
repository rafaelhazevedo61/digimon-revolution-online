package com.dro.modules.clan.infra;

import com.dro.modules.clan.domain.ClanUpgradePurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClanUpgradePurchaseRepository extends JpaRepository<ClanUpgradePurchase, UUID> {

    List<ClanUpgradePurchase> findByClanId(UUID clanId);

    Optional<ClanUpgradePurchase> findByClanIdAndUpgradeCode(UUID clanId, String upgradeCode);
}
