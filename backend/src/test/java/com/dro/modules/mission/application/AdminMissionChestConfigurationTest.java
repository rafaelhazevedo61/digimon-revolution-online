package com.dro.modules.mission.application;

import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.mission.api.dto.request.CreateMissionRequest;
import com.dro.modules.mission.api.dto.request.RewardRequest;
import com.dro.modules.mission.api.dto.response.AdminMissionResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminMissionChestConfigurationTest {

    @Mock
    private MissionDefinitionRepository missionDefinitionRepository;

    @Mock
    private ChestDefinitionRepository chestDefinitionRepository;

    @InjectMocks
    private CreateMissionUseCase createMissionUseCase;

    @Test
    void createsMissionLinkedToActiveAreaChest() {
        LootTableEntity lootTable = LootTableEntity.builder()
                .code("LOOT_TABLE_NATIVE_FOREST")
                .name("Loot Table Floresta Nativa")
                .build();
        ChestDefinitionEntity chest = ChestDefinitionEntity.builder()
                .code("CHEST_AREA_NATIVE_FOREST")
                .name("Baú Floresta Nativa")
                .lootTable(lootTable)
                .active(true)
                .build();

        when(missionDefinitionRepository.existsById("MISSION_ADMIN_TEST")).thenReturn(false);
        when(chestDefinitionRepository.findByCodeAndActiveTrue("CHEST_AREA_NATIVE_FOREST"))
                .thenReturn(Optional.of(chest));
        when(missionDefinitionRepository.saveAndFlush(any(MissionDefinitionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdminMissionResponse response = createMissionUseCase.execute(request("CHEST_AREA_NATIVE_FOREST", List.of()));

        assertThat(response.chestCode()).isEqualTo("CHEST_AREA_NATIVE_FOREST");
        assertThat(response.chestName()).isEqualTo("Baú Floresta Nativa");
        assertThat(response.chestLootTableCode()).isEqualTo("LOOT_TABLE_NATIVE_FOREST");
        assertThat(response.rewards()).isEmpty();
        verify(chestDefinitionRepository).findByCodeAndActiveTrue("CHEST_AREA_NATIVE_FOREST");
    }

    @Test
    void rejectsLegacyRewardsWhenMissionUsesAreaChest() {
        CreateMissionRequest request = request(
                "CHEST_AREA_NATIVE_FOREST",
                List.of(new RewardRequest(ItemType.TRAINING_STONE, 1))
        );

        assertThatThrownBy(() -> createMissionUseCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("não aceitam recompensas fixas");
        verify(chestDefinitionRepository, never()).findByCodeAndActiveTrue(any());
    }

    private CreateMissionRequest request(String chestCode, List<RewardRequest> rewards) {
        return new CreateMissionRequest(
                "MISSION_ADMIN_TEST",
                "Missão Administrativa de Teste",
                "Missão vinculada a um baú.",
                Area.NATIVE_FOREST,
                Stage.ROOKIE,
                1,
                100,
                50,
                5,
                60,
                chestCode,
                rewards,
                List.of(),
                List.of()
        );
    }
}
