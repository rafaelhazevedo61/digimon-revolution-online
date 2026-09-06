package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.config.GameplayConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClanRaidResponseMapperTest {

    @Mock
    private BossDefinitionRepository bossDefinitionRepository;

    @Mock
    private ClanRaidAttackRepository clanRaidAttackRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameplayConfig gameplayConfig;

    @InjectMocks
    private ClanRaidResponseMapper mapper;

    @Test
    void exposesNextAttackAvailableAtWhileCooldownIsActive() {
        UUID raidId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Instant previousAttackAt = Instant.now().minusSeconds(60);
        ClanRaid raid = raid(raidId, ClanRaidStatus.ACTIVE);
        BossDefinitionEntity boss = boss();
        ClanRaidAttack previousAttack = attack(raidId, playerId, previousAttackAt);
        stubCommon(raid, playerId, boss, previousAttack);

        var response = mapper.toResponse(raid, playerId);

        assertThat(response.cooldownEnabled()).isTrue();
        assertThat(response.attackCooldownMinutes()).isEqualTo(5);
        assertThat(response.nextAttackAvailableAt()).isAfter(previousAttackAt);
    }

    @Test
    void doesNotExposeNextAttackWhenGlobalCooldownIsDisabled() {
        UUID raidId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        ClanRaid raid = raid(raidId, ClanRaidStatus.ACTIVE);
        BossDefinitionEntity boss = boss();
        ClanRaidAttack previousAttack = attack(raidId, playerId, Instant.now().minusSeconds(60));
        stubCommon(raid, playerId, boss, previousAttack);
        when(gameplayConfig.isClanRaidCooldownEnabled()).thenReturn(false);

        var response = mapper.toResponse(raid, playerId);

        assertThat(response.cooldownEnabled()).isFalse();
        assertThat(response.attackCooldownMinutes()).isEqualTo(5);
        assertThat(response.nextAttackAvailableAt()).isNull();
    }

    @Test
    void exposesNextAttackWhenLegacyDatabaseCooldownIsDisabled() {
        UUID raidId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        ClanRaid raid = raid(raidId, ClanRaidStatus.ACTIVE);
        BossDefinitionEntity boss = boss();
        ClanRaidAttack previousAttack = attack(raidId, playerId, Instant.now().minusSeconds(60));
        stubCommon(raid, playerId, boss, previousAttack);

        var response = mapper.toResponse(raid, playerId);

        assertThat(response.cooldownEnabled()).isTrue();
        assertThat(response.attackCooldownMinutes()).isEqualTo(5);
        assertThat(response.nextAttackAvailableAt()).isNotNull().isAfter(Instant.now());
    }

    @Test
    void accumulatesAllPlayerDamageInPersonalTotalAndRanking() {
        UUID raidId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        ClanRaid raid = raid(raidId, ClanRaidStatus.ACTIVE);
        BossDefinitionEntity boss = boss();
        ClanRaidAttack firstAttack = attack(raidId, playerId, Instant.now().minusSeconds(120));
        firstAttack.setDamage(125);
        ClanRaidAttack secondAttack = attack(raidId, playerId, Instant.now().minusSeconds(60));
        secondAttack.setDamage(275);
        stubCommon(raid, playerId, boss, secondAttack);
        when(clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raidId))
                .thenReturn(List.of(secondAttack, firstAttack));

        var response = mapper.toResponse(raid, playerId);

        assertThat(response.myTotalDamage()).isEqualTo(400);
        assertThat(response.ranking()).singleElement().extracting(entry -> entry.totalDamage())
                .isEqualTo(400L);
    }

    private void stubCommon(ClanRaid raid, UUID playerId, BossDefinitionEntity boss, ClanRaidAttack previousAttack) {
        when(bossDefinitionRepository.findById(raid.getBossId())).thenReturn(Optional.of(boss));
        when(clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raid.getId()))
                .thenReturn(List.of(previousAttack));
        when(playerRepository.findAllById(any())).thenReturn(List.of());
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());
        when(gameplayConfig.isClanRaidCooldownEnabled()).thenReturn(true);
    }

    private ClanRaid raid(UUID raidId, ClanRaidStatus status) {
        return ClanRaid.builder()
                .id(raidId)
                .clanId(UUID.randomUUID())
                .bossId(1L)
                .maxHp(50_000)
                .remainingHp(status == ClanRaidStatus.DEFEATED ? 0 : 49_000)
                .status(status)
                .createdAt(Instant.now().minusSeconds(3600))
                .build();
    }

    private BossDefinitionEntity boss() {
        return BossDefinitionEntity.builder()
                .id(1L)
                .code("CLAN_RAID_OMEGAMON")
                .name("Omegamon")
                .cooldownMinutes(5)
                .build();
    }

    private ClanRaidAttack attack(UUID raidId, UUID playerId, Instant createdAt) {
        return ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raidId)
                .playerId(playerId)
                .digimonId(UUID.randomUUID())
                .damage(100)
                .createdAt(createdAt)
                .build();
    }
}
