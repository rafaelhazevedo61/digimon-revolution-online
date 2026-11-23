package com.dro.modules.tutorial.application;

import com.dro.modules.tutorial.api.dto.response.*;
import com.dro.modules.tutorial.infra.TutorialGuideRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.security.JwtTokenCodec;
import com.dro.shared.security.JwtSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TutorialGuideServiceTest {
    final TutorialService tutorial = mock(TutorialService.class);
    final TutorialGuideRepository repository = mock(TutorialGuideRepository.class);
    final PlayerRepository players = mock(PlayerRepository.class);
    final TutorialGuideService service = new TutorialGuideService(tutorial, repository, players);
    final UUID playerId = UUID.randomUUID();
    String token;
    @BeforeEach void setup() {
        token = JwtTokenCodec.create(Map.of("sub", playerId.toString(), "iss", JwtSettings.getIssuer(),
                "exp", Instant.now().plusSeconds(3600).getEpochSecond()), JwtSettings.getSecret());
        lenient().when(players.existsById(playerId)).thenReturn(true);
        lenient().when(repository.readTopics(playerId)).thenReturn(Set.of());
    }
    TutorialProgressResponse initial(boolean finished, String... steps) {
        var entries = Arrays.stream(steps).map(step -> new TutorialStepResponse(step, 1, "Etapa", "Descrição", 0, null, null, 0, true, true)).toList();
        return new TutorialProgressResponse(entries.size(), 6, finished, 0, 0, false, finished, entries);
    }
    @Test void hatchUnlocksEarlyTopicsButNotLaterActivities() {
        when(tutorial.getProgress(token)).thenReturn(initial(false, "HATCH_DIGIMON"));
        var result = service.page(token);
        assertEquals(12, result.totalTopics());
        assertTrue(result.topics().stream().filter(t -> t.code().equals("COLLECTION")).findFirst().orElseThrow().available());
        assertFalse(result.topics().stream().filter(t -> t.code().equals("TOWER")).findFirst().orElseThrow().available());
        verify(repository).readTopics(playerId);
    }
    @Test void firstMissionUnlocksTeamsTowerAndRanking() {
        when(tutorial.getProgress(token)).thenReturn(initial(false, "HATCH_DIGIMON", "COMPLETE_MISSION"));
        var topics = service.page(token).topics();
        for (String code : List.of("TEAMS", "TOWER", "RANKING")) assertTrue(topics.stream().filter(t -> t.code().equals(code)).findFirst().orElseThrow().available());
        assertFalse(topics.stream().filter(t -> t.code().equals("REBIRTH")).findFirst().orElseThrow().available());
    }
    @Test void evolutionUnlocksForgeRebirthAndOptionalVip() {
        when(tutorial.getProgress(token)).thenReturn(initial(false, "HATCH_DIGIMON", "COMPLETE_MISSION", "EVOLVE_DIGIMON"));
        var topics = service.page(token).topics();
        for (String code : List.of("FORGE", "REBIRTH", "VIP")) assertTrue(topics.stream().filter(t -> t.code().equals(code)).findFirst().orElseThrow().available());
    }
    @Test void finishedTutorialIsNotReopenedAndUnlocksOptionalGuide() {
        var initial = initial(true);
        when(tutorial.getProgress(token)).thenReturn(initial);
        var result = service.page(token);
        assertSame(initial, result.initial()); assertTrue(result.topics().stream().allMatch(t -> t.available()));
        verify(repository, never()).acknowledge(any(), any());
    }
    @Test void blockedTopicCannotBeMarkedRead() {
        when(tutorial.getProgress(token)).thenReturn(initial(false));
        assertThrows(BadRequestException.class, () -> service.acknowledge(token, "TOWER"));
        verify(repository, never()).acknowledge(any(), any());
    }
    @Test void unknownTopicIsRejectedBeforeDatabaseAccess() {
        assertThrows(BadRequestException.class, () -> service.acknowledge(token, "FAKE_TOPIC"));
        verifyNoInteractions(repository, tutorial);
    }
    @Test void acknowledgementOnlyWritesReadStatusAndReturnsInitialProgressUnchanged() {
        var initial = initial(false, "HATCH_DIGIMON");
        when(tutorial.getProgress(token)).thenReturn(initial);
        when(repository.readTopics(playerId)).thenReturn(Set.of(), Set.of("COLLECTION"));
        var result = service.acknowledge(token, "collection");
        assertSame(initial, result.initial()); assertEquals(1, result.readTopics());
        verify(repository).acknowledge(playerId, "COLLECTION");
        verify(tutorial, never()).completeStep(any(), any());
        verify(tutorial, never()).claimReward(any(), any());
    }
    @Test void unknownStoredTopicsDoNotInflateReadCount() {
        when(tutorial.getProgress(token)).thenReturn(initial(true));
        when(repository.readTopics(playerId)).thenReturn(Set.of("COLLECTION", "REMOVED_TOPIC"));
        assertEquals(1, service.page(token).readTopics());
    }
    @Test void nonexistentPlayerCannotReadGuide() {
        when(players.existsById(playerId)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.page(token));
        verifyNoInteractions(tutorial, repository);
    }
}
