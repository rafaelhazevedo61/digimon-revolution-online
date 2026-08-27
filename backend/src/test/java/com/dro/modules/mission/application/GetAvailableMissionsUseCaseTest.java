package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.MissionResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAvailableMissionsUseCaseTest {

    @Test
    void returnsMissionsByAreaAndMissionProgressionDescending() {
        UUID playerId = UUID.randomUUID();
        UUID activeDigimonId = UUID.randomUUID();
        Player player = mock(Player.class);
        Digimon digimon = mock(Digimon.class);
        PlayerRepository playerRepository = mock(PlayerRepository.class);
        DigimonRepository digimonRepository = mock(DigimonRepository.class);
        MissionDefinitionRepository missionDefinitionRepository = mock(MissionDefinitionRepository.class);

        when(player.getActiveDigimonId()).thenReturn(activeDigimonId);
        when(digimon.getLevel()).thenReturn(100);
        when(digimon.getStage()).thenReturn(Stage.MEGA);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(digimonRepository.findById(activeDigimonId)).thenReturn(Optional.of(digimon));
        when(missionDefinitionRepository.findByActiveTrue()).thenReturn(List.of(
                mission("MISSION_NF_1", Area.NATIVE_FOREST, Stage.BABY, 1),
                mission("MISSION_IM_1", Area.INFINITY_MOUNTAIN, Stage.MEGA, 60),
                mission("MISSION_NF_3", Area.NATIVE_FOREST, Stage.BABY, 5),
                mission("MISSION_SD_1", Area.SERVER_DESERT, Stage.ULTIMATE, 50),
                mission("MISSION_IM_2", Area.INFINITY_MOUNTAIN, Stage.MEGA, 65),
                mission("MISSION_NF_2", Area.NATIVE_FOREST, Stage.BABY, 3)
        ));

        GetAvailableMissionsUseCase useCase = new GetAvailableMissionsUseCase(
                playerRepository,
                digimonRepository,
                missionDefinitionRepository
        );

        List<MissionResponse> result = useCase.execute(makeToken(playerId));

        assertEquals(
                List.of(
                        "MISSION_IM_2",
                        "MISSION_IM_1",
                        "MISSION_SD_1",
                        "MISSION_NF_3",
                        "MISSION_NF_2",
                        "MISSION_NF_1"
                ),
                result.stream().map(MissionResponse::id).toList()
        );
    }

    private MissionDefinitionEntity mission(String id, Area area, Stage requiredStage, int requiredLevel) {
        return MissionDefinitionEntity.builder()
                .id(id)
                .name(id)
                .description("Test mission")
                .area(area)
                .requiredStage(requiredStage)
                .requiredLevel(requiredLevel)
                .baseXp(10)
                .baseBits(5)
                .energyCost(1)
                .durationSeconds(60)
                .active(true)
                .build();
    }

    private String makeToken(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
