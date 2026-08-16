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
class ClaimClanMissionUseCaseTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private ClanRepository clanRepository;
    @Mock private ClanMissionRepository clanMissionRepository;
    @Mock private PlayerClanMissionRepository playerClanMissionRepository;
    @Mock private ClanBonusService clanBonusService;
    @Spy private ClanMissionResponseMapper mapper = new ClanMissionResponseMapper();

    @InjectMocks
    private ClaimClanMissionUseCase useCase;

    private String makeToken(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    @Test
    void execute_claimsCompletedMissionAndCreditsClan() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        UUID playerMissionId = UUID.randomUUID();
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
                .level(1)
                .maxMembers(5)
                .honorMarks(0)
                .experience(0)
                .build();

        ClanMission mission = ClanMission.builder()
                .id(missionId)
                .code("CLAN_DAILY_BOSSES")
                .title("Caça a Bosses")
                .objectiveType(ClanMissionObjectiveType.BOSSES_DEFEATED)
                .targetValue(3)
                .minHonorMarksReward(15)
                .maxHonorMarksReward(30)
                .clanXpReward(60)
                .minClanLevel(1)
                .build();

        PlayerClanMission active = PlayerClanMission.builder()
                .id(playerMissionId)
                .playerId(playerId)
                .clanId(clanId)
                .clanMissionId(missionId)
                .status(PlayerClanMissionStatus.COMPLETED)
                .progress(3)
                .honorMarksReward(20)
                .acceptedAt(LocalDateTime.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findByPlayerIdAndStatusIn(eq(playerId), anyCollection()))
                .thenReturn(Optional.of(active));
        when(clanMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(clanRepository.findById(clanId)).thenReturn(Optional.of(clan));
        when(clanBonusService.getHonorMarksBonusPercent(clanId)).thenReturn(0.0);

        PlayerClanMissionResponse response = useCase.execute(token, playerMissionId);

        assertEquals(PlayerClanMissionStatus.CLAIMED, response.status());
        assertEquals(20, clan.getHonorMarks());
        assertEquals(60, clan.getExperience());
        verify(clanRepository).save(clan);
        verify(playerClanMissionRepository).save(active);
    }

    @Test
    void execute_throwsWhenMissionNotCompleted() {
        UUID playerId = UUID.randomUUID();
        UUID clanId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        UUID playerMissionId = UUID.randomUUID();
        String token = makeToken(playerId);

        Player player = Player.builder()
                .id(playerId)
                .username("player")
                .clanId(clanId)
                .build();

        ClanMission mission = ClanMission.builder()
                .id(missionId)
                .code("CLAN_DAILY_BOSSES")
                .title("Caça a Bosses")
                .objectiveType(ClanMissionObjectiveType.BOSSES_DEFEATED)
                .targetValue(3)
                .minHonorMarksReward(15)
                .maxHonorMarksReward(30)
                .clanXpReward(60)
                .minClanLevel(1)
                .build();

        PlayerClanMission active = PlayerClanMission.builder()
                .id(playerMissionId)
                .playerId(playerId)
                .clanId(clanId)
                .clanMissionId(missionId)
                .status(PlayerClanMissionStatus.IN_PROGRESS)
                .progress(1)
                .honorMarksReward(20)
                .acceptedAt(LocalDateTime.now())
                .build();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findByPlayerIdAndStatusIn(eq(playerId), anyCollection()))
                .thenReturn(Optional.of(active));
        when(clanMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        assertThrows(BadRequestException.class, () -> useCase.execute(token, playerMissionId));
    }
}
