package com.dro.modules.tower.application;

import com.dro.modules.tower.api.TowerDtos.*;
import com.dro.modules.tower.domain.TowerRules.*;
import com.dro.modules.tower.infra.TowerRepository;
import com.dro.modules.tower.infra.TowerRepository.State;
import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.arena.application.DigimonPowerService.CombatStats;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.mission.domain.MissionTeam;
import com.dro.modules.mission.infra.*;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TowerServiceTest {
    @Mock TowerRepository repository;
    @Mock PlayerRepository players;
    @Mock MissionTeamRepository teams;
    @Mock DigimonRepository digimons;
    @Mock MissionInstanceRepository missions;
    @Mock DigimonPowerService power;
    @Mock ItemDefinitionRepository items;
    @Mock AddItemUseCase grant;
    @Mock TransactionAuditPublisher audit;
    TowerService service;
    UUID playerId = UUID.randomUUID(), teamId = UUID.randomUUID(), digimonId = UUID.randomUUID();
    Player player;
    Challenge request;
    @BeforeEach void setup() {
        service = new TowerService(repository, players, teams, digimons, missions, power, items, grant, audit);
        player = Player.builder().id(playerId).bits(20).build();
        request = new Challenge(teamId, UUID.randomUUID(), 0);
        lenient().when(players.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        lenient().when(repository.state(playerId)).thenReturn(new State(0, 0, null, null));
    }
    void team() {
        when(teams.findByIdAndPlayerIdForUpdate(teamId, playerId)).thenReturn(Optional.of(new MissionTeam(playerId, "Meu time", List.of(digimonId), digimonId)));
        Digimon d = mock(Digimon.class);
        lenient().when(d.getId()).thenReturn(digimonId);
        lenient().when(d.getName()).thenReturn("Agumon");
        when(d.getStatus()).thenReturn(DigimonStatus.ACTIVE);
        when(digimons.findAllByIdForUpdate(playerId, List.of(digimonId))).thenReturn(List.of(d));
        lenient().when(power.effectiveStats(d, null)).thenReturn(new CombatStats(1000, 1000, 100));
    }
    @Test void initialReadDoesNotCreateProgress() {
        when(players.existsById(playerId)).thenReturn(true);
        Page page = service.page(playerId);
        assertEquals(0, page.highestFloor()); assertEquals(1, page.nextFloor().number());
        verify(repository, never()).save(any(), any());
    }
    @Test void winGrantsExactlyOneRewardAndAdvances() {
        team(); var item = mock(ItemDefinition.class);
        when(items.findByCode("ENERGY_CELL")).thenReturn(Optional.of(item));
        Page result = service.challenge(playerId, request);
        assertTrue(result.lastResult().won()); assertEquals(1, result.highestFloor());
        assertEquals(List.of(new MemberHealth(digimonId, "Agumon", 1000, 1000)), result.lastResult().members());
        assertEquals(2, result.nextFloor().number()); assertEquals(1, result.attempts()); assertEquals(120, player.getBits());
        assertEquals(30, java.time.Duration.between(result.serverTime(), result.availableAt()).getSeconds());
        verify(grant).addMaterialToPlayer(playerId, item, 1);
        verify(repository).save(eq(playerId), any()); verify(audit).success(any(), eq("TOWER_CHALLENGED"), any(), any(), any());
    }
    @Test void defeatKeepsFloorWithoutRewards() {
        team(); when(power.effectiveStats(any(), any())).thenReturn(new CombatStats(1, 1, 0));
        Page result = service.challenge(playerId, request);
        assertFalse(result.lastResult().won());
        assertEquals(List.of(new MemberHealth(digimonId, "Agumon", 0, 1)), result.lastResult().members());
        assertEquals(0, result.highestFloor()); assertEquals(1, result.attempts());
        assertEquals(20, player.getBits()); verifyNoInteractions(grant, items);
    }
    @Test void retryReturnsSavedResultEvenDuringCooldown() {
        Result saved = new Result(request.requestId(), 1, true, 1, "Time", "Koromon", new Reward(100, "ENERGY_CELL", "Célula", 1), List.of(new MemberHealth(digimonId, "Agumon", 1000, 1000)));
        when(repository.state(playerId)).thenReturn(new State(1, 1, Instant.now().plusSeconds(30), saved));
        assertEquals(saved, service.challenge(playerId, request).lastResult());
        verifyNoInteractions(teams, digimons, grant, audit); verify(repository, never()).save(any(), any());
    }
    @Test void staleAttemptDoesNotStartAnotherCombat() {
        when(repository.state(playerId)).thenReturn(new State(0, 1, null, null));
        assertThrows(ConflictException.class, () -> service.challenge(playerId, request)); verifyNoInteractions(teams, grant);
    }
    @Test void cooldownAlsoAppliesAfterDefeat() {
        when(repository.state(playerId)).thenReturn(new State(0, 0, Instant.now().plusSeconds(20), null));
        assertThrows(ConflictException.class, () -> service.challenge(playerId, request)); verifyNoInteractions(teams, grant);
    }
    @Test void foreignTeamRejected() {
        assertThrows(NotFoundException.class, () -> service.challenge(playerId, request)); verifyNoInteractions(digimons, grant);
    }
    @Test void missingTeamMemberRejected() {
        when(teams.findByIdAndPlayerIdForUpdate(teamId, playerId)).thenReturn(Optional.of(new MissionTeam(playerId, "Time", List.of(digimonId), digimonId)));
        assertThrows(ConflictException.class, () -> service.challenge(playerId, request)); verifyNoInteractions(grant);
    }
    @Test void busyTeamRejected() {
        team(); when(missions.existsByPlayerIdAndAnyDigimonIdAndStatusIn(eq(playerId), anyList(), anyList())).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.challenge(playerId, request)); verifyNoInteractions(grant);
    }
    @Test void inventoryFailureDoesNotPersistProgressOrBits() {
        team(); var item = mock(ItemDefinition.class); when(items.findByCode("ENERGY_CELL")).thenReturn(Optional.of(item));
        doThrow(new ConflictException("Inventário cheio")).when(grant).addMaterialToPlayer(playerId, item, 1);
        assertThrows(ConflictException.class, () -> service.challenge(playerId, request));
        assertEquals(20, player.getBits()); verify(repository, never()).save(any(), any()); verifyNoInteractions(audit);
    }
    @Test void walletOverflowDoesNotDiscardReward() {
        team(); player.setBits(Integer.MAX_VALUE);
        assertThrows(ConflictException.class, () -> service.challenge(playerId, request)); verifyNoInteractions(items, grant);
        verify(repository, never()).save(any(), any());
    }
}
