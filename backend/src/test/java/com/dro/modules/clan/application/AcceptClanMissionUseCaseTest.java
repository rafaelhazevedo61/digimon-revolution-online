package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.PlayerClanMissionResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.ClanMissionObjectiveType;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.ClanMissionRepository;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcceptClanMissionUseCaseTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private ClanRepository clanRepository;
    @Mock private ClanMissionRepository clanMissionRepository;
    @Mock private PlayerClanMissionRepository playerClanMissionRepository;
    @Spy private ClanMissionResponseMapper mapper = new ClanMissionResponseMapper();

    @InjectMocks
    private AcceptClanMissionUseCase useCase;

    private String makeToken(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    @Test
    void execute_acceptsMissionSuccessfully() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = Player.builder()
                .id(playerId)
                .username("player")
                .clanId(clanId)
                .build();

        Clan clan = Clan.builder()
                .id(clanId)
                .name("Clan")
                .tag("TAG")
                .leaderId(UUID.randomUUID())
                .level(3)
                .maxMembers(5)
                .honorMarks(0)
                .build();

        ClanMission mission = ClanMission.builder()
                .id(missionId)
                .code("CLAN_DAILY_ARENA")
                .title("Vitórias na Arena")
                .objectiveType(ClanMissionObjectiveType.ARENA_WINS)
                .targetValue(3)
                .minHonorMarksReward(20)
                .maxHonorMarksReward(40)
                .clanXpReward(70)
                .minClanLevel(2)
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findByPlayerIdAndStatusIn(eq(playerId), anyCollection()))
                .thenReturn(Optional.empty());
        when(playerClanMissionRepository.existsByPlayerIdAndClanMissionIdAndAcceptedAtGreaterThanEqual(any(), any(), any()))
                .thenReturn(false);
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));
        when(clanMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        PlayerClanMissionResponse response = useCase.execute(token, missionId);

        assertEquals(missionId, response.missionId());
        assertEquals("Vitórias na Arena", response.title());
        assertEquals(PlayerClanMissionStatus.IN_PROGRESS, response.status());
        assertTrue(response.honorMarksReward() >= 20 && response.honorMarksReward() <= 40);
        verify(playerClanMissionRepository).save(any(PlayerClanMission.class));
    }

    @Test
    void execute_throwsWhenAlreadyHasActiveMission() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = Player.builder()
                .id(playerId)
                .username("player")
                .clanId(clanId)
                .build();

        PlayerClanMission active = PlayerClanMission.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .clanId(clanId)
                .clanMissionId(missionId)
                .status(PlayerClanMissionStatus.IN_PROGRESS)
                .progress(0)
                .acceptedAt(LocalDateTime.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findByPlayerIdAndStatusIn(eq(playerId), anyCollection()))
                .thenReturn(Optional.of(active));

        assertThrows(ConflictException.class, () -> useCase.execute(token, missionId));
    }

    @Test
    void execute_throwsWhenAlreadyAcceptedToday() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = Player.builder()
                .id(playerId)
                .username("player")
                .clanId(clanId)
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findByPlayerIdAndStatusIn(eq(playerId), anyCollection()))
                .thenReturn(Optional.empty());
        when(playerClanMissionRepository.existsByPlayerIdAndClanMissionIdAndAcceptedAtGreaterThanEqual(any(), any(), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> useCase.execute(token, missionId));
    }

    @Test
    void execute_throwsWhenNotInClan() {
        UUID playerId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = Player.builder()
                .id(playerId)
                .username("player")
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        assertThrows(BadRequestException.class, () -> useCase.execute(token, missionId));
    }
}
