package com.dro.modules.clan.raid.application;

import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.raid.api.dto.response.AttackClanRaidResponse;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import com.dro.modules.clan.raid.domain.ClanRaidStatus;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
import com.dro.modules.clan.raid.infra.ClanRaidRepository;
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
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttackClanRaidUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private ClanRepository clanRepository;

    @Mock
    private BossDefinitionRepository bossDefinitionRepository;

    @Mock
    private ClanRaidRepository clanRaidRepository;

    @Mock
    private ClanRaidAttackRepository clanRaidAttackRepository;

    @Mock
    private ClanRaidService clanRaidService;

    @Mock
    private DigimonPowerService digimonPowerService;

    @Mock
    private ClanBonusService clanBonusService;

    @Mock
    private GlobalDamageBuffService globalDamageBuffService;

    @Mock
    private GameplayConfig gameplayConfig;

    @InjectMocks
    private AttackClanRaidUseCase useCase;

    private UUID playerId;
    private UUID clanId;
    private UUID digimonId;
    private String token;
    private Player player;
    private Digimon digimon;
    private BossDefinitionEntity boss;
    private ClanRaid raid;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        clanId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = createToken(playerId);

        player = Player.builder()
                .id(playerId)
                .username("clan-raid-player")
                .email("clan-raid@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(digimonId)
                .clanId(clanId)
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
                .code("CLAN_RAID_OMEGAMON")
                .name("Omegamon")
                .bossType(BossType.CLAN)
                .requiredStage(Stage.BABY)
                .requiredLevel(1)
                .requiredRebirths(0)
                .hp(100_000)
                .atk(100)
                .def(100)
                .energyCost(1)
                .cooldownMinutes(5)
                .cooldownEnabled(true)
                .baseXpReward(100)
                .baseBitsReward(10)
                .defeatXpPercent(5)
                .active(true)
                .build();
        raid = ClanRaid.builder()
                .id(UUID.randomUUID())
                .clanId(clanId)
                .bossId(1L)
                .maxHp(100_000)
                .remainingHp(100_000)
                .status(ClanRaidStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(clanRaidService.getOrCreateToday(clanId)).thenReturn(raid);
        when(bossDefinitionRepository.findById(1L)).thenReturn(Optional.of(boss));
        lenient().when(clanRaidAttackRepository.findFirstByClanRaidIdAndPlayerIdOrderByCreatedAtDesc(
                eq(raid.getId()), eq(playerId))).thenReturn(Optional.empty());
        lenient().when(digimonPowerService.calculatePower(digimon, clanId)).thenReturn(100_000.0);
        lenient().when(globalDamageBuffService.getMultiplier()).thenReturn(1.0);
        lenient().when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(false);
        lenient().when(gameplayConfig.isClanRaidCooldownEnabled()).thenReturn(true);
    }

    @Test
    void attackIsBlockedWhenFiveMinuteCooldownHasNotElapsed() {
        ClanRaidAttack lastAttack = ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raid.getId())
                .playerId(playerId)
                .digimonId(digimonId)
                .damage(100)
                .createdAt(Instant.now().minusSeconds(60))
                .build();
        when(clanRaidAttackRepository.findFirstByClanRaidIdAndPlayerIdOrderByCreatedAtDesc(raid.getId(), playerId))
                .thenReturn(Optional.of(lastAttack));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> useCase.execute(token)
        );

        assertTrue(exception.getMessage().contains("cooldown"));
        verify(clanRaidAttackRepository, never()).save(any());
        verify(digimonRepository, never()).save(any());
        verify(clanRaidRepository, never()).save(any());
    }

    @Test
    void attackIgnoresRecentAttackWhenCooldownIsDisabled() {
        boss.setCooldownEnabled(false);
        ClanRaidAttack lastAttack = ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raid.getId())
                .playerId(playerId)
                .digimonId(digimonId)
                .damage(100)
                .createdAt(Instant.now().minusSeconds(1))
                .build();
        when(clanRaidAttackRepository.findFirstByClanRaidIdAndPlayerIdOrderByCreatedAtDesc(raid.getId(), playerId))
                .thenReturn(Optional.of(lastAttack));

        AttackClanRaidResponse response = useCase.execute(token);

        assertFalse(response.defeated());
        verify(clanRaidAttackRepository).save(any(ClanRaidAttack.class));
        verify(digimonRepository).save(any(Digimon.class));
        verify(clanRaidRepository).save(any(ClanRaid.class));
    }

    @Test
    void attackIgnoresRecentAttackWhenGlobalCooldownIsDisabled() {
        when(gameplayConfig.isClanRaidCooldownEnabled()).thenReturn(false);
        ClanRaidAttack lastAttack = ClanRaidAttack.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raid.getId())
                .playerId(playerId)
                .digimonId(digimonId)
                .damage(100)
                .createdAt(Instant.now().minusSeconds(1))
                .build();
        when(clanRaidAttackRepository.findFirstByClanRaidIdAndPlayerIdOrderByCreatedAtDesc(raid.getId(), playerId))
                .thenReturn(Optional.of(lastAttack));

        AttackClanRaidResponse response = useCase.execute(token);

        assertFalse(response.defeated());
        verify(clanRaidAttackRepository).save(any(ClanRaidAttack.class));
        verify(digimonRepository).save(any(Digimon.class));
        verify(clanRaidRepository).save(any(ClanRaid.class));
    }

    private static String createToken(UUID subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}

