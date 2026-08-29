package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidRepository;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.shared.config.GameplayConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClanRaidServiceTest {

    @Mock
    private ClanRaidRepository clanRaidRepository;

    @Mock
    private BossDefinitionRepository bossDefinitionRepository;

    @Mock
    private GameplayConfig gameplayConfig;

    @InjectMocks
    private ClanRaidService service;

    @Test
    void getOrCreateToday_keepsDefeatedRaidWhenAutomaticRespawnIsDisabled() {
        UUID clanId = UUID.randomUUID();
        ClanRaid defeated = raid(clanId, ClanRaidStatus.DEFEATED);
        when(clanRaidRepository.findFirstByClanIdOrderByCreatedAtDesc(clanId)).thenReturn(Optional.of(defeated));
        when(gameplayConfig.isAutoBossRespawnAfterDefeatEnabled()).thenReturn(false);

        ClanRaid result = service.getOrCreateToday(clanId);

        assertSame(defeated, result);
        verify(bossDefinitionRepository, never()).findAllActive();
    }

    @Test
    void getOrCreateToday_createsNewRaidWhenAutomaticRespawnIsEnabled() {
        UUID clanId = UUID.randomUUID();
        ClanRaid defeated = raid(clanId, ClanRaidStatus.DEFEATED);
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .id(10L)
                .code("CLAN_RAID_OMEGAMON")
                .name("Omegamon")
                .bossType(BossType.CLAN)
                .requiredStage(Stage.BABY)
                .requiredLevel(1)
                .requiredRebirths(0)
                .hp(50_000)
                .atk(100)
                .def(100)
                .energyCost(15)
                .cooldownMinutes(5)
                .baseXpReward(100)
                .baseBitsReward(10)
                .defeatXpPercent(5)
                .active(true)
                .build();
        when(clanRaidRepository.findFirstByClanIdOrderByCreatedAtDesc(clanId)).thenReturn(Optional.of(defeated));
        when(gameplayConfig.isAutoBossRespawnAfterDefeatEnabled()).thenReturn(true);
        when(bossDefinitionRepository.findAllActive()).thenReturn(List.of(boss));
        when(clanRaidRepository.save(any(ClanRaid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClanRaid result = service.getOrCreateToday(clanId);

        assertNotSame(defeated, result);
        assertEquals(clanId, result.getClanId());
        assertEquals(10L, result.getBossId());
        assertEquals(50_000, result.getRemainingHp());
        assertEquals(ClanRaidStatus.ACTIVE, result.getStatus());
        verify(clanRaidRepository).save(any(ClanRaid.class));
    }

    private ClanRaid raid(UUID clanId, ClanRaidStatus status) {
        Instant now = Instant.now();
        return ClanRaid.builder()
                .id(UUID.randomUUID())
                .clanId(clanId)
                .bossId(10L)
                .maxHp(50_000)
                .remainingHp(status == ClanRaidStatus.DEFEATED ? 0 : 50_000)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .defeatedAt(status == ClanRaidStatus.DEFEATED ? now.minusSeconds(3601) : null)
                .build();
    }
}

