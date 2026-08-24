package com.dro.modules.equipment.application;
import com.dro.shared.security.JwtTestToken;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipUseCaseTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TutorialService tutorialService;

    @InjectMocks
    private EquipUseCase equipUseCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;
    private Player player;
    private Digimon digimon;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = JwtTestToken.create(playerId);

        player = Player.builder()
                .id(playerId)
                .username("test")
                .email("test@test.com")
                .password("encoded")
                .createdAt(LocalDateTime.now())
                .activeDigimonId(digimonId)
                .build();

        digimon = Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .name("Agumon")
                .type("FIRE")
                .stage(Stage.BABY)
                .level(1)
                .experience(0)
                .hp(10)
                .attack(5)
                .defense(5)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(10)
                .maxEnergy(10)
                .lastEnergyUpdate(Instant.now())
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Equipment createEquipment(EquipmentSlot slot) {
        return Equipment.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .name("Test Equipment")
                .slot(slot)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(10)
                .bonusAttack(5)
                .bonusDefense(3)
                .equipped(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void execute_equipsSuccessfully() {
        Equipment equipment = createEquipment(EquipmentSlot.WEAPON);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        equipUseCase.execute(token, equipment.getId());

        assertTrue(equipment.isEquipped());
        assertEquals(equipment.getId(), digimon.getWeaponId());
        verify(equipmentRepository).save(equipment);
        verify(digimonRepository).save(digimon);
    }

    @Test
    void execute_autoSwaps_whenSlotOccupied() {
        Equipment oldWeapon = createEquipment(EquipmentSlot.WEAPON);
        oldWeapon.equip();
        digimon.setWeaponId(oldWeapon.getId());

        Equipment newWeapon = createEquipment(EquipmentSlot.WEAPON);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(newWeapon.getId())).thenReturn(Optional.of(newWeapon));
        when(equipmentRepository.findById(oldWeapon.getId())).thenReturn(Optional.of(oldWeapon));

        equipUseCase.execute(token, newWeapon.getId());

        assertFalse(oldWeapon.isEquipped());
        assertTrue(newWeapon.isEquipped());
        assertEquals(newWeapon.getId(), digimon.getWeaponId());
    }

    @Test
    void execute_throwsWhenNoActiveDigimon() {
        player.setActiveDigimonId(null);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(RuntimeException.class,
                () -> equipUseCase.execute(token, UUID.randomUUID()));
    }

    @Test
    void execute_throwsWhenEquipmentNotBelongsToDigimon() {
        Equipment equipment = createEquipment(EquipmentSlot.WEAPON);
        equipment = Equipment.builder()
                .id(equipment.getId())
                .digimonId(UUID.randomUUID())
                .name("Other")
                .slot(EquipmentSlot.WEAPON)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(0)
                .bonusAttack(0)
                .bonusDefense(0)
                .equipped(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        Equipment finalEquipment = equipment;
        assertThrows(RuntimeException.class,
                () -> equipUseCase.execute(token, finalEquipment.getId()));
    }

    @Test
    void execute_throwsWhenEquipmentAlreadyEquipped() {
        Equipment equipment = createEquipment(EquipmentSlot.WEAPON);
        equipment.equip();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        assertThrows(RuntimeException.class,
                () -> equipUseCase.execute(token, equipment.getId()));
    }
}
