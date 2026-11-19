package com.dro.modules.equipment.application;
import com.dro.shared.security.JwtTestToken;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDigimonInventoryUseCaseTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @InjectMocks
    private GetDigimonInventoryUseCase useCase;

    private UUID playerId;
    private UUID digimonId;
    private String token;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonId = UUID.randomUUID();
        token = JwtTestToken.create(playerId);
    }

    @Test
    void execute_returnsInventory() {
        Digimon digimon = Digimon.builder()
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

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(equipmentRepository.findByDigimonId(digimonId)).thenReturn(List.of(equipment));

        List<EquipmentResponse> result = useCase.execute(token, digimonId);

        assertEquals(1, result.size());
        assertEquals("Iron Claw", result.get(0).name());
    }

    @Test
    void execute_throwsWhenDigimonNotBelongsToPlayer() {
        Digimon digimon = Digimon.builder()
                .id(digimonId)
                .playerId(UUID.randomUUID())
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

        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));

        assertThrows(RuntimeException.class, () -> useCase.execute(token, digimonId));
    }

    @Test
    void execute_throwsWhenDigimonNotFound() {
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> useCase.execute(token, digimonId));
    }
}
