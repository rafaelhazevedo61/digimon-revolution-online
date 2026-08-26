package com.dro.modules.player.application;
import com.dro.shared.security.JwtTestToken;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.api.dto.response.PlayerDashboardResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPlayerDashboardUseCaseTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private DigimonRepository digimonRepository;
    @Mock private EquipmentRepository equipmentRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private MissionInstanceRepository missionInstanceRepository;
    @Mock private IncubationRepository incubationRepository;

    @InjectMocks
    private GetPlayerDashboardUseCase useCase;

    private String makeToken(UUID playerId) {
        return JwtTestToken.create(playerId);
    }

    private Player makePlayer(UUID playerId, UUID activeDigimonId) {
        return Player.builder()
                .id(playerId)
                .username("testuser")
                .email("test@email.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(activeDigimonId)
                .build();
    }

    private Digimon makeDigimon(UUID id, UUID playerId) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.ROOKIE)
                .level(10)
                .experience(0)
                .hp(100)
                .attack(50)
                .defense(40)
                .ivHp(5)
                .ivAttack(3)
                .ivDefense(4)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(100)
                .maxEnergy(100)
                .lastEnergyUpdate(Instant.now())
                .bits(500)
                .rebirthCount(0)
                .status(DigimonStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_returnsFullDashboard_withActiveDigimon() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = makePlayer(playerId, digimonId);
        Digimon digimon = makeDigimon(digimonId, playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(inventoryRepository.findByDigimonId(digimonId)).thenReturn(List.of(
                InventoryItem.builder()
                        .id(UUID.randomUUID())
                        .digimonId(digimonId)
                        .itemType(ItemType.TRAINING_STONE)
                        .quantity(5)
                        .build()
        ));
        when(missionInstanceRepository.findByPlayerIdAndStatusIn(eq(playerId), any()))
                .thenReturn(List.of());
        when(incubationRepository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED))
                .thenReturn(List.of());

        PlayerDashboardResponse response = useCase.execute(token);

        assertNotNull(response);
        assertEquals(playerId, response.id());
        assertEquals("testuser", response.username());
        assertNotNull(response.activeDigimon());
        assertEquals("Agumon", response.activeDigimon().name());
        assertEquals(1, response.inventory().size());
        assertEquals(ItemType.TRAINING_STONE, response.inventory().get(0).itemType());
        assertEquals(5, response.inventory().get(0).quantity());
        assertTrue(response.activeMissions().isEmpty());
        assertNotNull(response.incubation());
        assertEquals(3, response.incubation().totalSlots());
        assertEquals(1, response.incubation().unlockedSlots());
        assertEquals(3, response.incubation().slots().size());
        assertNull(response.incubation().slots().get(0).incubation());
        assertFalse(response.incubation().slots().get(1).unlocked());
        assertFalse(response.incubation().slots().get(2).unlocked());
    }

    @Test
    void execute_returnsNullActiveDigimon_whenNoDigimonSelected() {
        UUID playerId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = makePlayer(playerId, null);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(missionInstanceRepository.findByPlayerIdAndStatusIn(eq(playerId), any()))
                .thenReturn(List.of());
        when(incubationRepository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED))
                .thenReturn(List.of());

        PlayerDashboardResponse response = useCase.execute(token);

        assertNotNull(response);
        assertNull(response.activeDigimon());
        assertTrue(response.equippedItems().isEmpty());
        assertTrue(response.inventory().isEmpty());
    }

    @Test
    void execute_includesIncubation_whenInProgress() {
        UUID playerId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = makePlayer(playerId, null);

        Incubation incubation = Incubation.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .digitamaType(ItemType.DIGITAMA_FIRE)
                .incubatorType(ItemType.INCUBATOR_COMMON)
                .status(IncubationStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .finishAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(missionInstanceRepository.findByPlayerIdAndStatusIn(eq(playerId), any()))
                .thenReturn(List.of());
        when(incubationRepository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED))
                .thenReturn(List.of(incubation));

        PlayerDashboardResponse response = useCase.execute(token);

        assertNotNull(response.incubation());
        assertEquals(3, response.incubation().totalSlots());
        assertNotNull(response.incubation().slots().get(0).incubation());
        assertEquals(ItemType.DIGITAMA_FIRE, response.incubation().slots().get(0).incubation().digitamaType());
        assertTrue(response.incubation().slots().get(0).incubation().remainingSeconds() > 0);
    }

    @Test
    void execute_includesMultipleIncubationsInTheirSlots() {
        UUID playerId = UUID.randomUUID();
        String token = makeToken(playerId);
        Player player = Player.builder()
                .id(playerId)
                .username("testuser")
                .email("test@email.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .unlockedIncubationSlots(2)
                .build();

        Incubation first = Incubation.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .slotNumber(1)
                .digitamaType(ItemType.DIGITAMA_FIRE)
                .incubatorType(ItemType.INCUBATOR_COMMON)
                .status(IncubationStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusMinutes(1))
                .finishAt(LocalDateTime.now().plusMinutes(4))
                .build();
        Incubation second = Incubation.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .slotNumber(2)
                .digitamaType(ItemType.DIGITAMA_WATER)
                .incubatorType(ItemType.INCUBATOR_RARE)
                .status(IncubationStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now().minusMinutes(1))
                .finishAt(LocalDateTime.now().plusMinutes(1))
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(missionInstanceRepository.findByPlayerIdAndStatusIn(eq(playerId), any()))
                .thenReturn(List.of());
        when(incubationRepository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED))
                .thenReturn(List.of(first, second));

        PlayerDashboardResponse response = useCase.execute(token);

        assertEquals(3, response.incubation().slots().size());
        assertEquals(ItemType.DIGITAMA_FIRE, response.incubation().slots().get(0).incubation().digitamaType());
        assertEquals(ItemType.DIGITAMA_WATER, response.incubation().slots().get(1).incubation().digitamaType());
        assertTrue(response.incubation().slots().get(0).unlocked());
        assertTrue(response.incubation().slots().get(1).unlocked());
        assertFalse(response.incubation().slots().get(2).unlocked());
    }

    @Test
    void execute_includesEquippedItems() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        UUID weaponId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = makePlayer(playerId, digimonId);
        Digimon digimon = makeDigimon(digimonId, playerId);
        digimon.setWeaponId(weaponId);

        Equipment weapon = Equipment.builder()
                .id(weaponId)
                .digimonId(digimonId)
                .name("Iron Claw")
                .slot(EquipmentSlot.WEAPON)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(0)
                .bonusAttack(5)
                .bonusDefense(0)
                .equipped(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(weaponId)).thenReturn(Optional.of(weapon));
        when(inventoryRepository.findByDigimonId(digimonId)).thenReturn(List.of());
        when(missionInstanceRepository.findByPlayerIdAndStatusIn(eq(playerId), any()))
                .thenReturn(List.of());
        when(incubationRepository.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED))
                .thenReturn(List.of());

        PlayerDashboardResponse response = useCase.execute(token);

        assertEquals(1, response.equippedItems().size());
        assertEquals("Iron Claw", response.equippedItems().get(0).name());
        assertEquals(5, response.activeDigimon().equipBonusAttack());
    }

    @Test
    void execute_throwsException_whenTokenNull() {
        assertThrows(UnauthorizedException.class, () -> useCase.execute(null));
    }

    @Test
    void execute_throwsException_whenPlayerNotFound() {
        UUID playerId = UUID.randomUUID();
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.execute(token));
    }
}
