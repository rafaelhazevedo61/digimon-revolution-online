package com.dro.modules.mission.application;

import com.dro.modules.player.application.VipAccess;
import com.dro.modules.player.domain.Player;
import com.dro.shared.exception.ForbiddenException;
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

    @Mock
    private VipAccess vipAccess;

    @Test
    void enablesFullAutomaticModeAndKeepsRepeatEnabled() {
        UUID playerId = UUID.randomUUID();
        UUID missionInstanceId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).build();
        player.setVipExpiresAt(Instant.now().plusSeconds(300));
        when(vipAccess.lockPlayer(playerId)).thenReturn(player);
        MissionInstance instance = org.mockito.Mockito.mock(MissionInstance.class);
        when(instance.getId()).thenReturn(missionInstanceId);
        when(instance.getTeamId()).thenReturn(teamId);
        when(instance.getSlotNumber()).thenReturn(2);
        when(instance.getPlayerId()).thenReturn(playerId);
        when(instance.isAlreadyClaimed()).thenReturn(false);
        when(missionInstanceRepository.findByIdForUpdate(missionInstanceId))
                .thenReturn(Optional.of(instance));

        var response = new SetMissionAutoClaimUseCase(missionInstanceRepository, vipAccess)
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
        Player player = Player.builder().id(playerId).build();
        player.setVipExpiresAt(Instant.now().plusSeconds(300));
        when(vipAccess.lockPlayer(playerId)).thenReturn(player);
        MissionInstance instance = org.mockito.Mockito.mock(MissionInstance.class);
        when(instance.getTeamId()).thenReturn(null);
        when(instance.getPlayerId()).thenReturn(playerId);
        when(instance.isAlreadyClaimed()).thenReturn(false);
        when(missionInstanceRepository.findByIdForUpdate(missionInstanceId))
                .thenReturn(Optional.of(instance));

        assertThatThrownBy(() -> new SetMissionAutoClaimUseCase(missionInstanceRepository, vipAccess)
                .execute(tokenFor(playerId), missionInstanceId, true))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("time de missão");
    }

    @Test
    void rejectsExpiredVipBeforeEnablingAutomation() {
        UUID playerId = UUID.randomUUID();
        Player player = Player.builder().id(playerId).build();
        player.setVipExpiresAt(Instant.now().minusSeconds(1));
        when(vipAccess.lockPlayer(playerId)).thenReturn(player);

        assertThatThrownBy(() -> new SetMissionAutoClaimUseCase(missionInstanceRepository, vipAccess)
                .execute(tokenFor(playerId), UUID.randomUUID(), true))
                .isInstanceOf(ForbiddenException.class);
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
