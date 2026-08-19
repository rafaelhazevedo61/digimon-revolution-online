package com.dro.modules.clan.raid.application;

import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.infra.ClanRaidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
@RequiredArgsConstructor
public class AdminResetClanRaidDailyUseCase {

    private final ClanRaidRepository clanRaidRepository;

    @Transactional
    public int execute() {
        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant now = Instant.now();

        List<ClanRaid> todayRaids = clanRaidRepository.findByCreatedAtGreaterThanEqual(startOfDay);
        for (ClanRaid raid : todayRaids) {
            raid.setDailyResetAt(now);
            raid.setUpdatedAt(now);
        }
        clanRaidRepository.saveAll(todayRaids);

        return todayRaids.size();
    }
}
