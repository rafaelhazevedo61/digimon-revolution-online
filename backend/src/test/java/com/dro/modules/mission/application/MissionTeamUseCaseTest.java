package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.request.SaveMissionTeamRequest;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.domain.MissionTeam;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionTeamUseCaseTest {
    @Mock private MissionTeamRepository missionTeamRepository;
    @Mock private MissionInstanceRepository missionInstanceRepository;
    @Mock private DigimonRepository digimonRepository;

    private UUID playerId;
    private List<UUID> digimonIds;
    private String token;
    private MissionTeamUseCase useCase;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        digimonIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        token = JwtTokenCodec.create(
                Map.of(
                        "sub", playerId.toString(),
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().getEpochSecond() + 3600
                ),
                JwtSettings.getSecret()
        );
        useCase = new MissionTeamUseCase(missionTeamRepository, missionInstanceRepository, digimonRepository);
    }

    @Test
    void createsTeamWithThreeOwnedDigimons() {
        when(digimonRepository.findAllByIdForUpdate(playerId, digimonIds))
                .thenReturn(List.of(digimon(digimonIds.get(0)), digimon(digimonIds.get(1)), digimon(digimonIds.get(2))));
        MissionTeam saved = new MissionTeam(playerId, "Exploradores", digimonIds, digimonIds.get(0));
        when(missionTeamRepository.save(any(MissionTeam.class))).thenReturn(saved);

        var response = useCase.create(token, new SaveMissionTeamRequest(" Exploradores ", digimonIds, digimonIds.get(0)));

        assertEquals("Exploradores", response.name());
        assertEquals(digimonIds, response.digimonIds());
        assertEquals(digimonIds.get(0), response.captainDigimonId());
        ArgumentCaptor<MissionTeam> captor = ArgumentCaptor.forClass(MissionTeam.class);
        verify(missionTeamRepository).save(captor.capture());
        assertEquals("Exploradores", captor.getValue().getName());
    }

    @Test
    void rejectsRepeatedDigimonInTeam() {
        List<UUID> repeated = List.of(digimonIds.get(0), digimonIds.get(0), digimonIds.get(2));

        assertThrows(
                BadRequestException.class,
                () -> useCase.create(token, new SaveMissionTeamRequest("Inválido", repeated, digimonIds.get(0)))
        );
    }

    @Test
    void rejectsEditingTeamWhileItIsInMission() {
        UUID teamId = UUID.randomUUID();
        MissionTeam team = new MissionTeam(playerId, "Exploradores", digimonIds, digimonIds.get(0));
        when(missionTeamRepository.findByIdAndPlayerIdForUpdate(teamId, playerId)).thenReturn(Optional.of(team));
        when(missionInstanceRepository.existsByTeamIdAndStatusIn(
                eq(teamId),
                eq(List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED))
        )).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> useCase.update(token, teamId, new SaveMissionTeamRequest("Novo nome", digimonIds, digimonIds.get(1)))
        );
    }

    private Digimon digimon(UUID id) {
        return Digimon.builder()
                .id(id)
                .playerId(playerId)
                .status(DigimonStatus.HATCHED)
                .build();
    }
}
