package com.dro.modules.clan.raid.infra;

import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClanRaidRepository extends JpaRepository<ClanRaid, UUID> {

    Optional<ClanRaid> findFirstByClanIdOrderByCreatedAtDesc(UUID clanId);

    Optional<ClanRaid> findFirstByClanIdAndStatusOrderByCreatedAtDesc(UUID clanId, ClanRaidStatus status);

    List<ClanRaid> findByCreatedAtGreaterThanEqual(Instant since);
}
