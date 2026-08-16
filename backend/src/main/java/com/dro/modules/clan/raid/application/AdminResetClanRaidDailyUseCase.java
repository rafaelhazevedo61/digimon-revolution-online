package com.dro.modules.clan.raid.application;

import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
import com.dro.modules.clan.raid.infra.ClanRaidRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminResetClanRaidDailyUseCase {

    private final ClanRaidRepository clanRaidRepository;
    private final ClanRaidAttackRepository clanRaidAttackRepository;

    @Transactional
    public Result execute() {
        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant now = Instant.now();

        List<ClanRaid> todayRaids = clanRaidRepository.findByCreatedAtGreaterThanEqual(startOfDay);
        for (ClanRaid raid : todayRaids) {
            raid.setStatus(ClanRaidStatus.ACTIVE);
            raid.setRemainingHp(raid.getMaxHp());
            raid.setDefeatedAt(null);
            raid.setUpdatedAt(now);
        }
        clanRaidRepository.saveAll(todayRaids);

        long attacksDeleted = clanRaidAttackRepository.deleteByCreatedAtGreaterThanEqual(startOfDay);
        return new Result(todayRaids.size(), attacksDeleted);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Result {
        private final int raidsReset;
        private final long attacksDeleted;
    }
}
