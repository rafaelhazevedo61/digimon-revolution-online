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
    @Mock private com.dro.modules.inventory.application.AddItemUseCase addItemUseCase;
    @Mock private com.dro.modules.inventory.infra.ItemDefinitionRepository itemDefinitionRepository;
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

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findOwnedByIdForUpdate(playerMissionId, playerId))
                .thenReturn(Optional.of(active));
        when(clanMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(clanRepository.findByIdForUpdate(clanId)).thenReturn(Optional.of(clan));
        when(clanBonusService.getHonorMarksBonusPercent(clanId)).thenReturn(0.0);

        var chest = com.dro.modules.inventory.domain.ItemDefinition.builder().code("CHEST_CLAN_CONTRIBUTION").build();
        when(itemDefinitionRepository.findByCode("CHEST_CLAN_CONTRIBUTION")).thenReturn(Optional.of(chest));
        PlayerClanMissionResponse response = useCase.execute(token, playerMissionId);

        assertEquals(PlayerClanMissionStatus.CLAIMED, response.status());
        assertEquals(1, response.personalRewardQuantity());
        assertEquals("CHEST_CLAN_CONTRIBUTION", response.personalRewardItemCode());
        verify(addItemUseCase).addMaterialToPlayer(playerId, chest, 1);
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

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findOwnedByIdForUpdate(playerMissionId, playerId))
                .thenReturn(Optional.of(active));
        when(clanMissionRepository.findById(missionId)).thenReturn(Optional.of(mission));

        assertThrows(BadRequestException.class, () -> useCase.execute(token, playerMissionId));
    }
    private record Fixture(UUID playerId, UUID clanId, UUID missionId, UUID playerMissionId,
                           Player player, Clan clan, ClanMission mission, PlayerClanMission active) {}

    private Fixture fixture(PlayerClanMissionStatus status) {
        UUID playerId=UUID.randomUUID(), clanId=UUID.randomUUID(), missionId=UUID.randomUUID(), playerMissionId=UUID.randomUUID();
        var player=Player.builder().id(playerId).clanId(clanId).build();
        var clan=Clan.builder().id(clanId).level(1).honorMarks(10).experience(0).build();
        var mission=ClanMission.builder().id(missionId).targetValue(5).clanXpReward(60).build();
        var active=PlayerClanMission.builder().id(playerMissionId).playerId(playerId).clanId(clanId)
            .clanMissionId(missionId).progress(5).honorMarksReward(20).status(status).build();
        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(playerClanMissionRepository.findOwnedByIdForUpdate(playerMissionId,playerId)).thenReturn(Optional.of(active));
        return new Fixture(playerId,clanId,missionId,playerMissionId,player,clan,mission,active);
    }

    @Test void rejectsClaimedAndExpiredMissionsWithoutRewards() {
        var f=fixture(PlayerClanMissionStatus.CLAIMED);
        assertThrows(BadRequestException.class,()->useCase.execute(makeToken(f.playerId()),f.playerMissionId()));
        f.active().setStatus(PlayerClanMissionStatus.EXPIRED);
        assertThrows(BadRequestException.class,()->useCase.execute(makeToken(f.playerId()),f.playerMissionId()));
        verifyNoInteractions(addItemUseCase,clanRepository,itemDefinitionRepository);
    }

    @Test void rejectsMissionFromFormerClan() {
        var f=fixture(PlayerClanMissionStatus.COMPLETED);
        f.player().setClanId(UUID.randomUUID());
        assertThrows(BadRequestException.class,()->useCase.execute(makeToken(f.playerId()),f.playerMissionId()));
        verifyNoInteractions(addItemUseCase,clanRepository,itemDefinitionRepository);
    }

    @Test void fullInventoryKeepsClanAndMissionUnchanged() {
        var f=fixture(PlayerClanMissionStatus.COMPLETED);
        when(clanMissionRepository.findById(f.missionId())).thenReturn(Optional.of(f.mission()));
        when(clanRepository.findByIdForUpdate(f.clanId())).thenReturn(Optional.of(f.clan()));
        var chest=com.dro.modules.inventory.domain.ItemDefinition.builder().code("CHEST_CLAN_CONTRIBUTION").build();
        when(itemDefinitionRepository.findByCode("CHEST_CLAN_CONTRIBUTION")).thenReturn(Optional.of(chest));
        doThrow(new com.dro.shared.automation.AutomationFailureException("Estoque cheio",org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY,com.dro.shared.automation.AutomationFailureCode.INVENTORY_STACK_FULL))
            .when(addItemUseCase).addMaterialToPlayer(f.playerId(),chest,1);
        assertThrows(com.dro.shared.automation.AutomationFailureException.class,()->useCase.execute(makeToken(f.playerId()),f.playerMissionId()));
        assertEquals(PlayerClanMissionStatus.COMPLETED,f.active().getStatus());
        assertEquals(10,f.clan().getHonorMarks());
        assertEquals(0,f.clan().getExperience());
        verify(playerClanMissionRepository,never()).save(any());
        verify(clanRepository,never()).save(any());
    }

    @Test void retryCannotGrantSecondChestAndHonorBonusRemains() {
        var f=fixture(PlayerClanMissionStatus.COMPLETED);
        when(clanMissionRepository.findById(f.missionId())).thenReturn(Optional.of(f.mission()));
        when(clanRepository.findByIdForUpdate(f.clanId())).thenReturn(Optional.of(f.clan()));
        when(clanBonusService.getHonorMarksBonusPercent(f.clanId())).thenReturn(0.5);
        var chest=com.dro.modules.inventory.domain.ItemDefinition.builder().code("CHEST_CLAN_CONTRIBUTION").build();
        when(itemDefinitionRepository.findByCode("CHEST_CLAN_CONTRIBUTION")).thenReturn(Optional.of(chest));
        useCase.execute(makeToken(f.playerId()),f.playerMissionId());
        assertThrows(BadRequestException.class,()->useCase.execute(makeToken(f.playerId()),f.playerMissionId()));
        verify(addItemUseCase,times(1)).addMaterialToPlayer(f.playerId(),chest,1);
        assertEquals(40,f.clan().getHonorMarks());
        assertEquals(60,f.clan().getExperience());
    }

    @Test void cannotClaimAnotherPlayersMission() {
        var id=UUID.randomUUID();
        when(playerRepository.findByIdForUpdate(id)).thenReturn(Optional.of(Player.builder().id(id).clanId(UUID.randomUUID()).build()));
        assertThrows(com.dro.shared.exception.NotFoundException.class,()->useCase.execute(makeToken(id),UUID.randomUUID()));
        verifyNoInteractions(addItemUseCase,clanRepository,itemDefinitionRepository);
    }

}
