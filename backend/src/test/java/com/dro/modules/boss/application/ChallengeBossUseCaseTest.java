package com.dro.modules.boss.application;

import com.dro.modules.boss.api.dto.response.BossChallengeResponse;
import com.dro.modules.boss.domain.*;
import com.dro.modules.boss.infra.BossAttemptRepository;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.application.EquipmentRarityProfileService;
import com.dro.modules.equipment.application.GrantEquipmentUseCase;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.server.application.GlobalDamageBuffService;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.config.GameplayConfig;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeBossUseCaseTest {

    @Mock private BossDefinitionRepository bossDefinitionRepository;
    @Mock private BossAttemptRepository bossAttemptRepository;
    @Mock private DigimonRepository digimonRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private AddItemUseCase addItemUseCase;
    @Mock private ChestDefinitionRepository chestDefinitionRepository;
    @Mock private GrantEquipmentUseCase grantEquipmentUseCase;
    @Mock private EquipmentRarityProfileService equipmentRarityProfileService;
    @Mock private ClanBonusService clanBonusService;
    @Mock private ClanMissionProgressTracker clanMissionProgressTracker;
    @Mock private GlobalDamageBuffService globalDamageBuffService;
    @Mock private TransactionAuditPublisher transactionAuditPublisher;
    @Mock private GameplayConfig gameplayConfig;

    @InjectMocks
    private ChallengeBossUseCase challengeBossUseCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        lenient().when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(true);
        token = JwtTokenCodec.create(
                Map.of("sub", playerId.toString(), "iss", JwtSettings.getIssuer(), "exp", Instant.now().getEpochSecond() + 3600),
                JwtSettings.getSecret()
        );
    }

    @Test
    void grantsChestOnVictoryAndAuditsOperation() {
        BossDefinitionEntity boss = bossWithChest("BOSS_1", "CHEST_1");
        Digimon digimon = activeDigimon(playerId, digimonId);
        Player player = player(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(bossDefinitionRepository.findByCode("BOSS_1")).thenReturn(Optional.of(boss));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(globalDamageBuffService.isEnabled()).thenReturn(true); // Força vitória determinística
        when(globalDamageBuffService.getMultiplier()).thenReturn(1.0);
        when(chestDefinitionRepository.findWithCatalogByCode("CHEST_1"))
                .thenReturn(Optional.of(boss.getChestDefinition()));

        BossChallengeResponse response = challengeBossUseCase.execute(token, "BOSS_1", digimonId);

        assertThat(response.result()).isEqualTo("VICTORY");
        assertThat(response.chestCode()).isEqualTo("CHEST_1");

        verify(addItemUseCase).addMaterial(eq(digimonId), any(ItemDefinition.class), eq(1));
        verify(transactionAuditPublisher).success(
                argThat(id -> id.startsWith("boss-challenge:")),
                eq("BOSS_CHALLENGED"),
                eq("BossAttempt"),
                anyString(),
                anyMap()
        );
    }

    @Test
    void rejectsChallengeIfBossHasNoChestConfigured() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .code("BOSS_NO_CHEST")
                .active(true)
                .bossType(BossType.NORMAL)
                .requiredStage(Stage.ROOKIE)
                .build();
        Digimon digimon = activeDigimon(playerId, digimonId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId)));
        when(bossDefinitionRepository.findByCode("BOSS_NO_CHEST")).thenReturn(Optional.of(boss));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(globalDamageBuffService.isEnabled()).thenReturn(true);

        assertThatThrownBy(() -> challengeBossUseCase.execute(token, "BOSS_NO_CHEST", digimonId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Boss não possui Baú de recompensa configurado");
    }

    @Test
    void rollsLegacyEquipmentDropsAlongsideChest() {
        BossDefinitionEntity boss = bossWithChest("BOSS_EQUIP", "CHEST_EQUIP");
        boss.setDrops(List.of(BossDropEntity.builder()
                .boss(boss)
                .dropType("EQUIPMENT")
                .templateName("Espada de Teste")
                .chance(100)
                .minQuantity(1)
                .maxQuantity(1)
                .build()));

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId)));
        when(bossDefinitionRepository.findByCode("BOSS_EQUIP")).thenReturn(Optional.of(boss));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(activeDigimon(playerId, digimonId)));
        when(globalDamageBuffService.isEnabled()).thenReturn(true);
        when(globalDamageBuffService.getMultiplier()).thenReturn(1.0);
        when(equipmentRarityProfileService.roll("BOSS_NORMAL", 0.0)).thenReturn(EquipmentRarity.COMMON);
        when(chestDefinitionRepository.findWithCatalogByCode("CHEST_EQUIP"))
                .thenReturn(Optional.of(boss.getChestDefinition()));

        BossChallengeResponse response = challengeBossUseCase.execute(token, "BOSS_EQUIP", digimonId);

        assertThat(response.drops()).anyMatch(d -> d.type().equals("EQUIPMENT") && d.code().equals("Espada de Teste"));
        assertThat(response.chestCode()).isEqualTo("CHEST_EQUIP");
    }

    @Test
    void energyDisabledAllowsChallengeWithZeroEnergyWithoutConsumption() {
        BossDefinitionEntity boss = bossWithChest("BOSS_NO_ENERGY", "CHEST_NO_ENERGY");
        Digimon digimon = activeDigimon(playerId, digimonId);
        digimon.setEnergy(0);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId)));
        when(bossDefinitionRepository.findByCode("BOSS_NO_ENERGY")).thenReturn(Optional.of(boss));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(gameplayConfig.isEnergyConsumptionEnabled()).thenReturn(false);
        when(globalDamageBuffService.isEnabled()).thenReturn(true);
        when(globalDamageBuffService.getMultiplier()).thenReturn(1.0);
        when(chestDefinitionRepository.findWithCatalogByCode("CHEST_NO_ENERGY"))
                .thenReturn(Optional.of(boss.getChestDefinition()));

        BossChallengeResponse response = challengeBossUseCase.execute(token, "BOSS_NO_ENERGY", digimonId);

        assertThat(response.result()).isEqualTo("VICTORY");
        assertThat(digimon.getEnergy()).isZero();
        verify(digimonRepository).save(digimon);
        verify(bossAttemptRepository).save(any(BossAttemptEntity.class));
    }

    private BossDefinitionEntity bossWithChest(String code, String chestCode) {
        LootTableEntity table = LootTableEntity.builder().code("LT_" + code).active(true).build();
        ItemDefinition item = ItemDefinition.builder().id(100L).code(chestCode).name("Baú de Teste").category("CHEST").build();
        ChestDefinitionEntity chest = ChestDefinitionEntity.builder()
                .code(chestCode)
                .name("Baú de Teste")
                .active(true)
                .lootTable(table)
                .itemDefinition(item)
                .build();

        return BossDefinitionEntity.builder()
                .id(1L)
                .code(code)
                .name("Boss Teste")
                .bossType(BossType.NORMAL)
                .requiredStage(Stage.ROOKIE)
                .requiredLevel(1)
                .hp(100).atk(10).def(10)
                .energyCost(5)
                .cooldownMinutes(60)
                .baseXpReward(100)
                .baseBitsReward(50)
                .active(true)
                .chestDefinition(chest)
                .build();
    }

    private Digimon activeDigimon(UUID playerId, UUID id) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .stage(Stage.ROOKIE)
                .level(10)
                .hp(200).attack(50).defense(50)
                .energy(100)
                .maxEnergy(100)
                .lastEnergyUpdate(Instant.now())
                .grade(DigimonGrade.B)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .build();
    }

    private Player player(UUID id) {
        return Player.builder().id(id).userType(UserType.PLAYER).build();
    }
}
