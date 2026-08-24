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
class UnequipAllUseCaseTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private UnequipAllUseCase unequipAllUseCase;

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
                .name("Test " + slot.name())
                .slot(slot)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(10)
                .bonusAttack(5)
                .bonusDefense(3)
                .equipped(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void execute_unequipsAll_returnsCount() {
        Equipment weapon = createEquipment(EquipmentSlot.WEAPON);
        Equipment armor = createEquipment(EquipmentSlot.ARMOR);
        Equipment accessory = createEquipment(EquipmentSlot.ACCESSORY);

        digimon.setWeaponId(weapon.getId());
        digimon.setArmorId(armor.getId());
        digimon.setAccessoryId(accessory.getId());

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(weapon.getId())).thenReturn(Optional.of(weapon));
        when(equipmentRepository.findById(armor.getId())).thenReturn(Optional.of(armor));
        when(equipmentRepository.findById(accessory.getId())).thenReturn(Optional.of(accessory));

        int count = unequipAllUseCase.execute(token);

        assertEquals(3, count);
        assertFalse(weapon.isEquipped());
        assertFalse(armor.isEquipped());
        assertFalse(accessory.isEquipped());
        assertNull(digimon.getWeaponId());
        assertNull(digimon.getArmorId());
        assertNull(digimon.getAccessoryId());
        verify(digimonRepository).save(digimon);
    }

    @Test
    void execute_noEquipments_returnsZero() {
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        int count = unequipAllUseCase.execute(token);

        assertEquals(0, count);
    }

    @Test
    void execute_partialEquipments_returnsPartialCount() {
        Equipment weapon = createEquipment(EquipmentSlot.WEAPON);
        digimon.setWeaponId(weapon.getId());

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(weapon.getId())).thenReturn(Optional.of(weapon));

        int count = unequipAllUseCase.execute(token);

        assertEquals(1, count);
        assertFalse(weapon.isEquipped());
        assertNull(digimon.getWeaponId());
    }

    @Test
    void execute_throwsWhenNoActiveDigimon() {
        player.setActiveDigimonId(null);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(RuntimeException.class, () -> unequipAllUseCase.execute(token));
    }
}
