package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
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
class GetDigimonEquipmentUseCaseTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @InjectMocks
    private GetDigimonEquipmentUseCase useCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;
    private Digimon digimon;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = UUID.randomUUID() + ":" + playerId;

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
    void execute_returnsEquippedItems_withBonuses() {
        Equipment weapon = Equipment.builder()
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

        Equipment armor = Equipment.builder()
                .id(UUID.randomUUID())
                .digimonId(digimonId)
                .name("Leather Armor")
                .slot(EquipmentSlot.ARMOR)
                .rarity(EquipmentRarity.COMMON)
                .bonusHp(5)
                .bonusAttack(0)
                .bonusDefense(5)
                .equipped(true)
                .createdAt(LocalDateTime.now())
                .build();

        digimon.setWeaponId(weapon.getId());
        digimon.setArmorId(armor.getId());

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(weapon.getId())).thenReturn(Optional.of(weapon));
        when(equipmentRepository.findById(armor.getId())).thenReturn(Optional.of(armor));

        DigimonEquipmentResponse response = useCase.execute(token, digimonId);

        assertEquals(digimonId, response.digimonId());
        assertEquals(2, response.equippedItems().size());
        assertEquals(5, response.totalBonusHp());
        assertEquals(5, response.totalBonusAttack());
        assertEquals(5, response.totalBonusDefense());
    }

    @Test
    void execute_returnsEmpty_whenNoEquipment() {
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        DigimonEquipmentResponse response = useCase.execute(token, digimonId);

        assertEquals(0, response.equippedItems().size());
        assertEquals(0, response.totalBonusHp());
        assertEquals(0, response.totalBonusAttack());
        assertEquals(0, response.totalBonusDefense());
    }

    @Test
    void execute_throwsWhenDigimonNotBelongsToPlayer() {
        digimon.setPlayerId(UUID.randomUUID());
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        assertThrows(RuntimeException.class, () -> useCase.execute(token, digimonId));
    }
}
