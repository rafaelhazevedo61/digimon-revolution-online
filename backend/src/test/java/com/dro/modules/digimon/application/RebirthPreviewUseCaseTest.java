package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.RebirthPreviewResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RebirthPreviewUseCaseTest {

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private MissionInstanceRepository missionInstanceRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private RebirthPreviewUseCase useCase;

    @Test
    void execute_withoutPreservation_keepsCurrentIvMinimumFormula() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = createDigimon(playerId, digimonId, Rarity.EPIC);
        preparePreviewDependencies(playerId, digimonId, digimon);

        RebirthPreviewResponse response = useCase.execute(
                JwtTestToken.create(playerId),
                digimonId,
                false
        );

        // IV atual 40, rebirth 1: max(3 + 3, 40 / 2) = 20.
        assertEquals(20, response.hpIvRange().min());
        assertEquals(20, response.attackIvRange().min());
        assertEquals(20, response.defenseIvRange().min());
    }

    @Test
    void execute_withPreservation_usesPreservedRarityIvMinimum() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = createDigimon(playerId, digimonId, Rarity.EPIC);
        preparePreviewDependencies(playerId, digimonId, digimon);

        RebirthPreviewResponse response = useCase.execute(
                JwtTestToken.create(playerId),
                digimonId,
                true
        );

        // EPIC = 50, rebirth 1 = +3: max(50 + 3, 40 / 2) = 53.
        assertEquals(53, response.hpIvRange().min());
        assertEquals(53, response.attackIvRange().min());
        assertEquals(53, response.defenseIvRange().min());
    }

    private void preparePreviewDependencies(UUID playerId, UUID digimonId, Digimon digimon) {
        Player player = org.mockito.Mockito.mock(Player.class);
        when(player.getDigitalData()).thenReturn(0);
        when(digimonRepository.findById(digimonId)).thenReturn(Optional.of(digimon));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(equipmentRepository.findByDigimonIdAndEquippedTrue(digimonId)).thenReturn(List.of());
        when(inventoryRepository.findByDigimonIdAndItemType(any(UUID.class), any())).thenReturn(Optional.empty());
        when(missionInstanceRepository.countByDigimonIdAndStatus(digimonId, MissionStatus.COMPLETED)).thenReturn(0L);
        when(missionInstanceRepository.existsByDigimonIdAndStatus(any(UUID.class), any())).thenReturn(false);
    }

    private Digimon createDigimon(UUID playerId, UUID digimonId, Rarity rarity) {
        return Digimon.builder()
                .id(digimonId)
                .playerId(playerId)
                .name("Greymon")
                .type("DINOSAUR")
                .stage(Stage.CHAMPION)
                .level(100)
                .ivHp(40)
                .ivAttack(40)
                .ivDefense(40)
                .rarity(rarity)
                .status(DigimonStatus.ACTIVE)
                .rebirthCount(0)
                .bits(100_000)
                .build();
    }
}
