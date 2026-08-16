package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidRepository;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClanRaidService {

    private final ClanRaidRepository clanRaidRepository;
    private final BossDefinitionRepository bossDefinitionRepository;

    @Transactional
    public ClanRaid getOrCreateToday(UUID clanId) {
        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        return clanRaidRepository.findFirstByClanIdOrderByCreatedAtDesc(clanId)
                .filter(raid -> !raid.getCreatedAt().isBefore(startOfDay))
                .orElseGet(() -> createNewRaid(clanId));
    }

    private ClanRaid createNewRaid(UUID clanId) {
        BossDefinitionEntity boss = bossDefinitionRepository.findAllActive().stream()
                .filter(b -> b.getBossType() == BossType.CLAN)
                .min((a, b) -> {
                    int stageCompare = Integer.compare(a.getRequiredStage().ordinal(), b.getRequiredStage().ordinal());
                    if (stageCompare != 0) return stageCompare;
                    return Integer.compare(a.getRequiredLevel(), b.getRequiredLevel());
                })
                .orElseThrow(() -> new NotFoundException("No clan raid boss is available"));

        Instant now = Instant.now();
        ClanRaid raid = ClanRaid.builder()
                .id(UUID.randomUUID())
                .clanId(clanId)
                .bossId(boss.getId())
                .maxHp(boss.getHp())
                .remainingHp(boss.getHp())
                .status(ClanRaidStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return clanRaidRepository.save(raid);
    }
}
