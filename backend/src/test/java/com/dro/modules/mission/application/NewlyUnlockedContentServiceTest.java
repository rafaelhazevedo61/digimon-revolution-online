package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.mission.api.dto.response.NewlyUnlockedContentResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NewlyUnlockedContentServiceTest {
    @Test
    void usesTheCurrentMissionRequiredLevelFromRepository() {
        MissionDefinitionRepository repository = mock(MissionDefinitionRepository.class);
        Digimon digimon = mock(Digimon.class);
        MissionDefinitionEntity mission = mission("MISSION_DYNAMIC", 3);
        when(repository.findByActiveTrue()).thenReturn(List.of(mission));
        when(digimon.getLevel()).thenReturn(3);
        when(digimon.getStage()).thenReturn(Stage.BABY);

        NewlyUnlockedContentResponse result = new NewlyUnlockedContentService(repository)
                .detect(digimon, 2, Stage.BABY);

        assertEquals(1, result.missions().size());
        assertEquals(3, result.missions().get(0).requiredLevel());
    }

    @Test
    void doesNotUseAStaleHardCodedLevelWhenTheMissionRequirementChanges() {
        MissionDefinitionRepository repository = mock(MissionDefinitionRepository.class);
        Digimon digimon = mock(Digimon.class);
        MissionDefinitionEntity mission = mission("MISSION_DYNAMIC", 4);
        when(repository.findByActiveTrue()).thenReturn(List.of(mission));
        when(digimon.getLevel()).thenReturn(3);
        when(digimon.getStage()).thenReturn(Stage.BABY);

        NewlyUnlockedContentResponse result = new NewlyUnlockedContentService(repository)
                .detect(digimon, 2, Stage.BABY);

        assertTrue(result.missions().isEmpty());
    }

    private MissionDefinitionEntity mission(String id, int requiredLevel) {
        return MissionDefinitionEntity.builder()
                .id(id)
                .name("Missão dinâmica")
                .description("Test mission")
                .area(Area.NATIVE_FOREST)
                .requiredStage(Stage.BABY)
                .requiredLevel(requiredLevel)
                .baseXp(10)
                .baseBits(5)
                .energyCost(1)
                .durationSeconds(60)
                .active(true)
                .build();
    }
}
