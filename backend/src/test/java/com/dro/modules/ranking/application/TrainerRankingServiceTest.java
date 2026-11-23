package com.dro.modules.ranking.application;

import com.dro.modules.ranking.infra.TrainerRankingRepository;
import com.dro.modules.ranking.api.dto.response.TrainerRankingDtos.*;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerRankingServiceTest {
    final TrainerRankingRepository repository = mock(TrainerRankingRepository.class);
    final TrainerRankingService service = new TrainerRankingService(repository);
    final UUID playerId = UUID.randomUUID();
    final Entry entry = new Entry(playerId, "Treinador", 42, BigDecimal.valueOf(415),
            BigDecimal.valueOf(90), BigDecimal.valueOf(125), BigDecimal.valueOf(200), 4, 1, 2, 10);

    @Test void includesOwnPositionOutsideCurrentPage() {
        when(repository.eligibility(playerId)).thenReturn(List.of(true));
        when(repository.state()).thenReturn(new State(Instant.EPOCH, 100));
        when(repository.page(0, 10)).thenReturn(List.of());
        when(repository.me(playerId)).thenReturn(entry);
        var page = service.page(playerId, 0, 10);
        assertEquals(42, page.me().position()); assertEquals("RANKED", page.myStatus());
        assertTrue(page.hasNext()); assertEquals(Instant.EPOCH, page.updatedAt());
    }
    @Test void excludedAccountStillCanReadTheCompetition() {
        when(repository.eligibility(playerId)).thenReturn(List.of(false));
        when(repository.state()).thenReturn(new State(null, 0));
        var page = service.page(playerId, 0, 10);
        assertEquals("EXCLUDED", page.myStatus()); assertNull(page.me()); assertFalse(page.hasNext());
        verify(repository, never()).me(any());
    }
    @Test void newAccountWaitsForSnapshotWithoutTriggeringRebuild() {
        when(repository.eligibility(playerId)).thenReturn(List.of(true));
        when(repository.state()).thenReturn(new State(null, 0));
        var page = service.page(playerId, 0, 10);
        assertEquals("PENDING", page.myStatus()); assertNull(page.updatedAt());
        verify(repository, never()).rebuild(); verify(repository, never()).lockIfDue(anyLong());
    }
    @Test void rejectsInvalidPagination() {
        assertThrows(BadRequestException.class, () -> service.page(playerId, -1, 10));
        assertThrows(BadRequestException.class, () -> service.page(playerId, 0, 0));
        assertThrows(BadRequestException.class, () -> service.page(playerId, 0, 51));
        verifyNoInteractions(repository);
    }
    @Test void searchPreservesGlobalPositionAndOwnEntryWithFilteredPagination() {
        when(repository.eligibility(playerId)).thenReturn(List.of(true));
        when(repository.state()).thenReturn(new State(Instant.EPOCH, 100));
        when(repository.me(playerId)).thenReturn(entry);
        when(repository.countMatching("Trein")).thenReturn(1L);
        when(repository.page(0, 10, "Trein")).thenReturn(List.of(entry));
        var result = service.page(playerId, 0, 10, "  Trein  ");
        assertEquals(42, result.entries().get(0).position());
        assertEquals(100, result.totalPlayers()); assertEquals(1, result.matchingPlayers());
        assertEquals(entry, result.me()); assertFalse(result.hasNext());
    }
    @Test void oversizedSearchIsRejectedBeforeQuerying() {
        assertThrows(BadRequestException.class, () -> service.page(playerId, 0, 10, "x".repeat(51)));
        verifyNoInteractions(repository);
    }
    @Test void publicProfileReturnsOnlyThePublicDto() {
        var profile = new Profile(entry, null, null, null, Instant.EPOCH);
        when(repository.profile(playerId)).thenReturn(profile);
        assertEquals(profile, service.profile(playerId));
    }
    @Test void missingOrExcludedPublicProfileIsRejected() {
        assertThrows(NotFoundException.class, () -> service.profile(playerId));
    }
    @Test void missingPlayerIsRejected() {
        when(repository.eligibility(playerId)).thenReturn(List.of());
        assertThrows(NotFoundException.class, () -> service.page(playerId, 0, 10));
    }
    @Test void refreshRequiresSharedDatabaseLockAndFreshnessCheck() {
        var refresher = new TrainerRankingRefresher(repository);
        when(repository.lockIfDue(300000)).thenReturn(false);
        assertFalse(refresher.refresh(300000)); verify(repository, never()).rebuild(); verify(repository, never()).captureDailyHistory();
    }
    @Test void refreshRebuildsAfterLock() {
        var refresher = new TrainerRankingRefresher(repository);
        when(repository.lockIfDue(300000)).thenReturn(true);
        assertTrue(refresher.refresh(300000)); verify(repository).rebuild(); verify(repository).captureDailyHistory();
    }
    @Test void schedulerCanBeDisabled() {
        var refresher = mock(TrainerRankingRefresher.class);
        new TrainerRankingJob(refresher, false, 300000).run();
        verifyNoInteractions(refresher);
    }
}
