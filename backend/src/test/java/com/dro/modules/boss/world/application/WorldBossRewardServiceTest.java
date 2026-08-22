package com.dro.modules.boss.world.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.world.api.dto.response.WorldBossRewardResponse;
import com.dro.modules.boss.world.domain.WorldBossAttack;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossReward;
import com.dro.modules.boss.world.domain.WorldBossRewardType;
import com.dro.modules.boss.world.infra.WorldBossAttackRepository;
import com.dro.modules.boss.world.infra.WorldBossRewardRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorldBossRewardServiceTest {

    @Mock
    private WorldBossAttackRepository worldBossAttackRepository;

    @Mock
    private WorldBossRewardRepository worldBossRewardRepository;

    @Mock
    private AddItemUseCase addItemUseCase;

    @Mock
    private LootTableEntity lootTable;

    @Mock
    private ItemDefinition itemDefinition;

    @InjectMocks
    private WorldBossRewardService service;

    private BossDefinitionEntity boss;
    private WorldBossInstance instance;
    private ChestDefinitionEntity attemptChest;
    private ChestDefinitionEntity topDamageChest;
    private ChestDefinitionEntity finalBlowChest;
    private UUID playerOne;
    private UUID playerTwo;
    private UUID digimonOne;
    private UUID digimonTwo;
    private UUID currentAttackId;

    @BeforeEach
    void setUp() {
        playerOne = UUID.randomUUID();
        playerTwo = UUID.randomUUID();
        digimonOne = UUID.randomUUID();
        digimonTwo = UUID.randomUUID();
        currentAttackId = UUID.randomUUID();

        boss = BossDefinitionEntity.builder()
                .code("WORLD_BOSS_APOCALYMON")
                .name("Apocalymon")
                .build();
        instance = WorldBossInstance.builder()
                .id(UUID.randomUUID())
                .bossId(1L)
                .bossDate(LocalDate.now())
                .maxHp(1_000_000)
                .remainingHp(0)
                .build();

        attemptChest = chest("CHEST_WORLD_CUSTOM_ATTEMPT", "Baú por tentativa");
        topDamageChest = chest("CHEST_WORLD_CUSTOM_TOP_DAMAGE", "Baú de maior dano");
        finalBlowChest = chest("CHEST_WORLD_CUSTOM_FINAL_BLOW", "Baú do golpe final");
        boss.setWorldAttemptChestDefinition(attemptChest);
        boss.setWorldTopDamageChestDefinition(topDamageChest);
        boss.setWorldFinalBlowChestDefinition(finalBlowChest);
        lenient().when(worldBossRewardRepository.findByEventKey(anyString())).thenReturn(Optional.empty());
        lenient().when(worldBossRewardRepository.save(any(WorldBossReward.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void grantOnDefeatGivesAttemptTopDamageAndFinalBlowChests() {
        WorldBossAttack previousPlayerOne = attack(playerOne, digimonOne, 60, Instant.parse("2026-08-21T10:00:00Z"), UUID.randomUUID());
        WorldBossAttack topDamagePlayerTwo = attack(playerTwo, digimonTwo, 120, Instant.parse("2026-08-21T10:01:00Z"), UUID.randomUUID());
        WorldBossAttack currentFinalBlow = attack(playerOne, digimonOne, 50, Instant.parse("2026-08-21T10:02:00Z"), currentAttackId);
        when(worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(instance.getId()))
                .thenReturn(List.of(currentFinalBlow, topDamagePlayerTwo, previousPlayerOne));

        List<WorldBossRewardResponse> rewards = service.grant(boss, instance, currentFinalBlow, true);

        assertEquals(List.of("ATTEMPT", "TOP_DAMAGE", "FINAL_BLOW"),
                rewards.stream().map(WorldBossRewardResponse::rewardType).toList());
        assertEquals(List.of(
                        "CHEST_WORLD_CUSTOM_ATTEMPT",
                        "CHEST_WORLD_CUSTOM_TOP_DAMAGE",
                        "CHEST_WORLD_CUSTOM_FINAL_BLOW"
                ), rewards.stream().map(WorldBossRewardResponse::chestCode).toList());

        verify(addItemUseCase, times(2)).addMaterial(digimonOne, itemDefinition, 1);
        verify(addItemUseCase).addMaterial(digimonTwo, itemDefinition, 1);
        verify(addItemUseCase, times(3)).addMaterial(any(), any(), eq(1));
        verify(worldBossRewardRepository, times(3)).save(any(WorldBossReward.class));

        ArgumentCaptor<WorldBossReward> captor = ArgumentCaptor.forClass(WorldBossReward.class);
        verify(worldBossRewardRepository, times(3)).save(captor.capture());
        List<WorldBossReward> saved = captor.getAllValues();
        assertEquals(playerTwo, saved.get(1).getRecipientPlayerId());
        assertEquals(digimonTwo, saved.get(1).getRecipientDigimonId());
        assertEquals(playerOne, saved.get(2).getRecipientPlayerId());
        assertEquals(WorldBossRewardType.FINAL_BLOW, saved.get(2).getRewardType());
    }

    @Test
    void grantUsesFirstParticipantAsDeterministicTieBreaker() {
        WorldBossAttack firstPlayer = attack(playerOne, digimonOne, 100, Instant.parse("2026-08-21T10:00:00Z"), UUID.randomUUID());
        WorldBossAttack secondPlayer = attack(playerTwo, digimonTwo, 99, Instant.parse("2026-08-21T10:01:00Z"), UUID.randomUUID());
        WorldBossAttack currentFinalBlow = attack(playerTwo, digimonTwo, 1, Instant.parse("2026-08-21T10:02:00Z"), currentAttackId);
        when(worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(instance.getId()))
                .thenReturn(List.of(currentFinalBlow, secondPlayer, firstPlayer));

        service.grant(boss, instance, currentFinalBlow, true);

        ArgumentCaptor<WorldBossReward> captor = ArgumentCaptor.forClass(WorldBossReward.class);
        verify(worldBossRewardRepository, times(3)).save(captor.capture());
        assertEquals(playerOne, captor.getAllValues().get(1).getRecipientPlayerId());
    }

    @Test
    void grantIsIdempotentWhenAttemptEventAlreadyExists() {
        WorldBossAttack attack = attack(playerOne, digimonOne, 20, Instant.parse("2026-08-21T10:00:00Z"), currentAttackId);
        WorldBossReward existing = WorldBossReward.builder()
                .id(UUID.randomUUID())
                .rewardType(WorldBossRewardType.ATTEMPT)
                .chestDefinition(attemptChest)
                .build();
        when(worldBossRewardRepository.findByEventKey(anyString())).thenReturn(Optional.of(existing));

        List<WorldBossRewardResponse> rewards = service.grant(boss, instance, attack, false);

        assertEquals(1, rewards.size());
        assertSame(attemptChest, existing.getChestDefinition());
        verifyNoInteractions(addItemUseCase);
        verify(worldBossRewardRepository, never()).save(any());
    }

    private ChestDefinitionEntity chest(String code, String name) {
        ChestDefinitionEntity chest = mock(ChestDefinitionEntity.class);
        lenient().when(chest.getCode()).thenReturn(code);
        lenient().when(chest.getName()).thenReturn(name);
        lenient().when(chest.isActive()).thenReturn(true);
        lenient().when(chest.getLootTable()).thenReturn(lootTable);
        lenient().when(chest.getItemDefinition()).thenReturn(itemDefinition);
        lenient().when(lootTable.isActive()).thenReturn(true);
        return chest;
    }

    private WorldBossAttack attack(UUID playerId, UUID digimonId, int damage, Instant createdAt, UUID id) {
        return WorldBossAttack.builder()
                .id(id)
                .worldBossId(instance.getId())
                .playerId(playerId)
                .digimonId(digimonId)
                .damage(damage)
                .createdAt(createdAt)
                .build();
    }
}
