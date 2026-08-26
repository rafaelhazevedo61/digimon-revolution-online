package com.dro.modules.boss.world.application;

import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.world.api.dto.response.AttackWorldBossResponse;
import com.dro.modules.boss.world.api.dto.response.WorldBossRewardResponse;
import com.dro.modules.boss.world.domain.WorldBossAttack;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossRewardType;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.world.infra.WorldBossAttackRepository;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import com.dro.modules.boss.world.infra.WorldBossRewardRepository;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.server.application.GlobalDamageBuffService;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttackWorldBossUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private com.dro.modules.boss.infra.BossDefinitionRepository bossDefinitionRepository;

    @Mock
    private WorldBossInstanceRepository worldBossInstanceRepository;

    @Mock
    private WorldBossAttackRepository worldBossAttackRepository;

    @Mock
    private WorldBossRewardRepository worldBossRewardRepository;

    @Mock
    private WorldBossService worldBossService;

    @Mock
    private WorldBossRewardService worldBossRewardService;

    @Mock
    private DigimonPowerService digimonPowerService;

    @Mock
    private ClanBonusService clanBonusService;

    @Mock
    private GlobalDamageBuffService globalDamageBuffService;

    @Mock
    private TransactionAuditPublisher transactionAuditPublisher;

    @Mock
    private GameplayConfig gameplayConfig;

    @InjectMocks
    private AttackWorldBossUseCase useCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;
    private Player player;
    private Digimon digimon;
    private BossDefinitionEntity boss;
    private WorldBossInstance instance;
    private WorldBossRewardResponse attemptReward;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = createToken(playerId);

        player = Player.builder()
                .id(playerId)
                .username("world-boss-player")
                .email("world-boss@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(digimonId)
                .userType(UserType.PLAYER)
                .build();
        digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.ROOKIE)
                .level(10)
                .experience(0)
                .hp(100)
                .attack(50)
                .defense(50)
                .grade(DigimonGrade.C)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(100)
                .maxEnergy(100)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        boss = BossDefinitionEntity.builder()
                .id(1L)
                .code("WORLD_BOSS_APOCALYMON")
                .name("Apocalymon")
                .bossType(BossType.WORLD)
                .requiredStage(Stage.BABY)
                .requiredLevel(1)
                .requiredRebirths(0)
                .hp(100)
                .atk(10)
                .def(10)
                .energyCost(1)
                .baseXpReward(100)
                .baseBitsReward(10)
                .defeatXpPercent(5)
                .active(true)
                .build();
        instance = WorldBossInstance.builder()
                .id(UUID.randomUUID())
                .bossId(1L)
                .bossDate(LocalDate.now())
                .maxHp(1)
                .remainingHp(1)
                .status(WorldBossStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        attemptReward = new WorldBossRewardResponse(
                WorldBossRewardType.ATTEMPT.getCode(),
                "CHEST_BOSS_WORLD_APOCALYMON_ATTEMPT",
                "Baú por tentativa"
        );

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(worldBossService.getOrCreateToday()).thenReturn(instance);
        when(bossDefinitionRepository.findById(1L)).thenReturn(Optional.of(boss));
        lenient().when(worldBossAttackRepository.findByWorldBossIdAndPlayerIdAndRequestId(
                eq(instance.getId()), eq(playerId), anyString())).thenReturn(Optional.empty());
        lenient().when(digimonPowerService.calculatePower(digimon, null)).thenReturn(100_000.0);
        lenient().when(globalDamageBuffService.getMultiplier()).thenReturn(1.0);
        lenient().when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(true);
        lenient().when(gameplayConfig.isWorldBossCooldownEnabled()).thenReturn(true);
        lenient().when(worldBossRewardService.grant(any(), any(), any(), anyBoolean()))
                .thenReturn(List.of(attemptReward));
    }

    @Test
    void attackVictoryGrantsAttemptChestAndPersistsSnapshot() {
        AttackWorldBossResponse response = useCase.execute(token, "request-1");

        assertTrue(response.defeated());
        assertEquals(List.of(attemptReward), response.rewards());
        assertEquals(0, response.remainingHp());

        ArgumentCaptor<WorldBossAttack> captor = ArgumentCaptor.forClass(WorldBossAttack.class);
        verify(worldBossAttackRepository).save(captor.capture());
        WorldBossAttack saved = captor.getValue();
        assertEquals("request-1", saved.getRequestId());
        assertEquals(0, saved.getRemainingHpAfter());
        assertTrue(saved.isDefeated());
        verify(worldBossRewardService).grant(boss, instance, saved, true);
        verify(transactionAuditPublisher).success(
                anyString(), eq("WORLD_BOSS_ATTACKED"), eq("WorldBossAttack"), anyString(), anyMap());
    }

    @Test
    void repeatedRequestReturnsOriginalSnapshotWithoutNewAttackOrReward() {
        WorldBossAttack existing = WorldBossAttack.builder()
                .id(UUID.randomUUID())
                .worldBossId(instance.getId())
                .playerId(playerId)
                .digimonId(digimonId)
                .damage(10)
                .bitsGained(1)
                .xpGained(5)
                .requestId("same-request")
                .remainingHpAfter(90)
                .winChance(60)
                .defeated(false)
                .defeatedRewardXp(0)
                .defeatedRewardBits(0)
                .createdAt(Instant.now())
                .build();
        when(worldBossAttackRepository.findByWorldBossIdAndPlayerIdAndRequestId(
                instance.getId(), playerId, "same-request")).thenReturn(Optional.of(existing));
        when(worldBossRewardService.findBySourceAttackId(existing.getId())).thenReturn(List.of(attemptReward));

        AttackWorldBossResponse response = useCase.execute(token, "same-request");

        assertFalse(response.defeated());
        assertEquals(90, response.remainingHp());
        assertEquals(List.of(attemptReward), response.rewards());
        verify(worldBossAttackRepository, never()).save(any());
        verify(worldBossRewardService, never()).grant(any(), any(), any(), anyBoolean());
        verify(digimonRepository, never()).save(any());
        verify(transactionAuditPublisher, never()).success(anyString(), anyString(), anyString(), anyString(), anyMap());
    }



    @Test
    void energyDisabledAllowsAttackWithZeroEnergyWithoutConsumption() {
        digimon.setEnergy(0);
        when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(false);

        AttackWorldBossResponse response = useCase.execute(token, "request-energy-disabled");

        assertTrue(response.defeated());
        assertEquals(0, digimon.getEnergy());

        ArgumentCaptor<WorldBossAttack> captor = ArgumentCaptor.forClass(WorldBossAttack.class);
        verify(worldBossAttackRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getEnergyCost());
    }

    @Test
    void attackIsBlockedWhenConfiguredCooldownHasNotElapsed() {
        WorldBossAttack lastAttack = WorldBossAttack.builder()
                .id(UUID.randomUUID())
                .worldBossId(instance.getId())
                .playerId(playerId)
                .digimonId(digimonId)
                .damage(10)
                .createdAt(Instant.now().minusSeconds(60))
                .build();
        when(worldBossAttackRepository.findFirstByWorldBossIdAndPlayerIdOrderByCreatedAtDesc(
                instance.getId(), playerId)).thenReturn(Optional.of(lastAttack));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> useCase.execute(token, "request-cooldown")
        );

        assertTrue(exception.getMessage().contains("cooldown"));
        verify(worldBossAttackRepository, never()).save(any());
        verify(worldBossRewardService, never()).grant(any(), any(), any(), anyBoolean());
        verify(digimonRepository, never()).save(any());
    }

    @Test
    void attackIgnoresRecentAttackWhenGlobalCooldownIsDisabled() {
        when(gameplayConfig.isWorldBossCooldownEnabled()).thenReturn(false);
        WorldBossAttack lastAttack = WorldBossAttack.builder()
                .id(UUID.randomUUID())
                .worldBossId(instance.getId())
                .playerId(playerId)
                .digimonId(digimonId)
                .damage(10)
                .createdAt(Instant.now().minusSeconds(1))
                .build();
        when(worldBossAttackRepository.findFirstByWorldBossIdAndPlayerIdOrderByCreatedAtDesc(
                instance.getId(), playerId)).thenReturn(Optional.of(lastAttack));

        AttackWorldBossResponse response = useCase.execute(token, "request-global-disabled-cooldown");

        assertTrue(response.defeated());
        verify(worldBossAttackRepository).save(any(WorldBossAttack.class));
        verify(worldBossRewardService).grant(any(), any(), any(), eq(true));
    }

    @Test
    void attackIsBlockedAfterBossWasDefeated() {
        instance.setStatus(WorldBossStatus.DEFEATED);
        instance.setRemainingHp(0);

        assertThrows(BadRequestException.class, () -> useCase.execute(token, "request-2"));

        verify(worldBossAttackRepository, never()).save(any());
        verify(worldBossRewardService, never()).grant(any(), any(), any(), anyBoolean());
    }

    private static String createToken(UUID subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
