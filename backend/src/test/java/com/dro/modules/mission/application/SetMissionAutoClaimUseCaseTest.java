package com.dro.modules.mission.application;

import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetMissionAutoClaimUseCaseTest {

    @Mock
    private MissionInstanceRepository missionInstanceRepository;

    @Test
    void enablesFullAutomaticModeAndKeepsRepeatEnabled() {
        UUID playerId = UUID.randomUUID();
        UUID missionInstanceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        MissionInstance instance = org.mockito.Mockito.mock(MissionInstance.class);
        when(instance.getId()).thenReturn(missionInstanceId);
        when(instance.getTeamId()).thenReturn(teamId);
        when(instance.getSlotNumber()).thenReturn(2);
        when(instance.isAlreadyClaimed()).thenReturn(false);
        when(missionInstanceRepository.findByIdAndPlayerId(missionInstanceId, playerId))
                .thenReturn(Optional.of(instance));

        var response = new SetMissionAutoClaimUseCase(missionInstanceRepository)
                .execute(tokenFor(playerId), missionInstanceId, true);

        assertThat(response.enabled()).isTrue();
        verify(instance).setAutoClaimEnabled(true);
        verify(instance).setAutoRepeatEnabled(true);
        verify(missionInstanceRepository).save(instance);
    }

    @Test
    void rejectsFullAutomaticModeForLegacyMissionWithoutTeam() {
        UUID playerId = UUID.randomUUID();
        UUID missionInstanceId = UUID.randomUUID();
        MissionInstance instance = org.mockito.Mockito.mock(MissionInstance.class);
        when(instance.getTeamId()).thenReturn(null);
        when(instance.isAlreadyClaimed()).thenReturn(false);
        when(missionInstanceRepository.findByIdAndPlayerId(missionInstanceId, playerId))
                .thenReturn(Optional.of(instance));

        assertThatThrownBy(() -> new SetMissionAutoClaimUseCase(missionInstanceRepository)
                .execute(tokenFor(playerId), missionInstanceId, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("time de missão");
    }

    private String tokenFor(UUID playerId) {
        return JwtTokenCodec.create(
                Map.of(
                        "sub", playerId.toString(),
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().plusSeconds(300).getEpochSecond()
                ),
                JwtSettings.getSecret()
        );
    }
}
