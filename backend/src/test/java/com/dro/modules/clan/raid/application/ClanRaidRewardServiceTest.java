package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidRewardResponse;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import com.dro.modules.clan.raid.domain.ClanRaidRewardType;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
import com.dro.modules.clan.raid.infra.ClanRaidRewardRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClanRaidRewardServiceTest {
    @Mock
    private ClanRaidAttackRepository clanRaidAttackRepository;
    @Mock
    private ClanRaidRewardRepository clanRaidRewardRepository;
    @Mock
    private AddItemUseCase addItemUseCase;
    @Mock
    private ChestDefinitionEntity attemptChest;
    @Mock
    private ChestDefinitionEntity topDamageChest;
    @Mock
    private ChestDefinitionEntity finalBlowChest;
    @Mock
    private LootTableEntity lootTable;
    @Mock
    private ItemDefinition attemptItem;
    @Mock
    private ItemDefinition topDamageItem;
    @Mock
    private ItemDefinition finalBlowItem;

    private ClanRaidRewardService service;
    private UUID raidId;
    private UUID currentPlayerId;
    private UUID currentDigimonId;
    private UUID topPlayerId;
    private UUID topDigimonId;
    private ClanRaid raid;
    private ClanRaidAttack currentAttack;
    private ClanRaidAttack topDamageAttack;
    private BossDefinitionEntity boss;

    @BeforeEach
    void setUp() {
        service = new ClanRaidRewardService(clanRaidAttackRepository, clanRaidRewardRepository, addItemUseCase);
        raidId = UUID.randomUUID();
        currentPlayerId = UUID.randomUUID();
        currentDigimonId = UUID.randomUUID();
        topPlayerId = UUID.randomUUID();
        topDigimonId = UUID.randomUUID();
        Instant now = Instant.now();
        raid = ClanRaid.builder()
                .id(raidId)
                .clanId(UUID.randomUUID())
                .bossId(1L)
                .maxHp(50_000)
                .remainingHp(0)
                .status(ClanRaidStatus.DEFEATED)
                .createdAt(now.minusSeconds(120))
                .updatedAt(now)
                .defeatedAt(now)
                .build();
        currentAttack = ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raidId)
                .playerId(currentPlayerId)
                .digimonId(currentDigimonId)
                .damage(100)
                .createdAt(now)
                .build();
        topDamageAttack = ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raidId)
                .playerId(topPlayerId)
                .digimonId(topDigimonId)
                .damage(300)
                .createdAt(now.minusSeconds(60))
                .build();

        stubChest(attemptChest, "CHEST_CLAN_RAID_OMEGAMON_ATTEMPT", attemptItem);
        stubChest(topDamageChest, "CHEST_CLAN_RAID_OMEGAMON_TOP_DAMAGE", topDamageItem);
        stubChest(finalBlowChest, "CHEST_CLAN_RAID_OMEGAMON_FINAL_BLOW", finalBlowItem);
        boss = BossDefinitionEntity.builder()
                .id(1L)
                .code("CLAN_RAID_OMEGAMON")
                .clanRaidAttemptChestDefinition(attemptChest)
                .clanRaidTopDamageChestDefinition(topDamageChest)
                .clanRaidFinalBlowChestDefinition(finalBlowChest)
                .build();
        lenient().when(clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raidId))
                .thenReturn(List.of(topDamageAttack, previousTopDamageAttack(), currentAttack));
        lenient().when(clanRaidRewardRepository.findByEventKey(anyString())).thenReturn(Optional.empty());
        lenient().when(clanRaidRewardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void grantUsesAccumulatedDamageToSelectTopParticipant() {
        List<ClanRaidRewardResponse> rewards = service.grant(boss, raid, currentAttack, true);

        assertThat(rewards).extracting(ClanRaidRewardResponse::rewardType)
                .containsExactly("ATTEMPT", "TOP_DAMAGE", "FINAL_BLOW");
        verify(addItemUseCase).addMaterial(currentDigimonId, attemptItem, 1);
        verify(addItemUseCase).addMaterial(topDigimonId, topDamageItem, 1);
        verify(addItemUseCase).addMaterial(currentDigimonId, finalBlowItem, 1);
    }

    @Test
    void grantDoesNotDuplicateAnExistingAttemptEvent() {
        var existing = mock(com.dro.modules.clan.raid.domain.ClanRaidReward.class);
        when(existing.getRewardType()).thenReturn(ClanRaidRewardType.ATTEMPT);
        when(existing.getChestDefinition()).thenReturn(attemptChest);
        when(attemptChest.getCode()).thenReturn("CHEST_CLAN_RAID_OMEGAMON_ATTEMPT");
        when(attemptChest.getName()).thenReturn("Baú da Incursão de Clã — Tentativa");
        when(clanRaidRewardRepository.findByEventKey(anyString())).thenReturn(Optional.of(existing));

        List<ClanRaidRewardResponse> rewards = service.grant(boss, raid, currentAttack, false);

        assertThat(rewards).singleElement().extracting(ClanRaidRewardResponse::rewardType)
                .isEqualTo("ATTEMPT");
        verify(addItemUseCase, never()).addMaterial(any(), any(), anyInt());
        verify(clanRaidRewardRepository, never()).save(any());
    }

    private void stubChest(ChestDefinitionEntity chest, String code, ItemDefinition item) {
        lenient().when(chest.isActive()).thenReturn(true);
        lenient().when(chest.getCode()).thenReturn(code);
        lenient().when(chest.getName()).thenReturn(code);
        lenient().when(chest.getLootTable()).thenReturn(lootTable);
        lenient().when(chest.getItemDefinition()).thenReturn(item);
        lenient().when(lootTable.isActive()).thenReturn(true);
    }

    private ClanRaidAttack previousTopDamageAttack() {
        return ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raidId)
                .playerId(topPlayerId)
                .digimonId(topDigimonId)
                .damage(100)
                .createdAt(topDamageAttack.getCreatedAt().minusSeconds(60))
                .build();
    }
}
