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
class UnequipUseCaseTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private UnequipUseCase unequipUseCase;

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

    @Test
    void execute_unequipsSuccessfully() {
        Equipment equipment = Equipment.builder()
                .id(UUID.randomUUID())
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

        digimon.setWeaponId(equipment.getId());

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        unequipUseCase.execute(token, equipment.getId());

        assertFalse(equipment.isEquipped());
        assertNull(digimon.getWeaponId());
        verify(digimonRepository).save(digimon);
        verify(equipmentRepository).save(equipment);
    }

    @Test
    void execute_throwsWhenNotEquipped() {
        Equipment equipment = Equipment.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .name("Iron Claw")
                .slot(EquipmentSlot.WEAPON)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(0)
                .bonusAttack(5)
                .bonusDefense(0)
                .equipped(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        assertThrows(RuntimeException.class,
                () -> unequipUseCase.execute(token, equipment.getId()));
    }

    @Test
    void execute_throwsWhenNoActiveDigimon() {
        player.setActiveDigimonId(null);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(RuntimeException.class,
                () -> unequipUseCase.execute(token, UUID.randomUUID()));
    }

    @Test
    void execute_throwsWhenEquipmentNotBelongsToDigimon() {
        Equipment equipment = Equipment.builder()
                .id(UUID.randomUUID())
                .digimonId(UUID.randomUUID())
                .name("Other")
                .slot(EquipmentSlot.WEAPON)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(0)
                .bonusAttack(0)
                .bonusDefense(0)
                .equipped(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(equipment.getId())).thenReturn(Optional.of(equipment));

        assertThrows(RuntimeException.class,
                () -> unequipUseCase.execute(token, equipment.getId()));
    }
}
