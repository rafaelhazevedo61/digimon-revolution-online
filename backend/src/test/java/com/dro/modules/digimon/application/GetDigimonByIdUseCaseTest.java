package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
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
class GetDigimonByIdUseCaseTest {

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private GetDigimonByIdUseCase useCase;

    private Digimon createDigimon(UUID digimonId) {
        return Digimon.builder()
                .id(digimonId)
                .playerId(UUID.randomUUID())
                .name("Botamon")
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
    void execute_returnsDigimonResponse() {
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = createDigimon(digimonId);

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        DigimonResponse response = useCase.execute(digimonId);

        assertEquals(digimonId, response.id());
        assertEquals("Botamon", response.name());
        assertEquals("FIRE", response.type());
        assertEquals(Stage.BABY, response.stage());
        assertEquals(1, response.level());
    }

    @Test
    void execute_includesEquipmentBonuses() {
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = createDigimon(digimonId);

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

        digimon.setWeaponId(weapon.getId());

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findById(weapon.getId())).thenReturn(Optional.of(weapon));

        DigimonResponse response = useCase.execute(digimonId);

        assertEquals(0, response.equipBonusHp());
        assertEquals(5, response.equipBonusAttack());
        assertEquals(0, response.equipBonusDefense());
    }

    @Test
    void execute_throwsWhenNotFound() {
        UUID digimonId = UUID.randomUUID();
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> useCase.execute(digimonId));
    }
}
