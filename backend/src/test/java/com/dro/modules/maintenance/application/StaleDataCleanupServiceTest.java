package com.dro.modules.maintenance.application;

import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.shared.audit.AuditOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaleDataCleanupServiceTest {
    @Mock
    private MissionInstanceRepository missionInstanceRepository;
    @Mock
    private AuditOutboxRepository auditOutboxRepository;

    private StaleDataCleanupService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new StaleDataCleanupService(missionInstanceRepository, auditOutboxRepository);
        setField("retentionHours", 24L);
        setField("batchSize", 500);
    }

    @Test
    void dryRunReportsEligibleRowsWithoutDeleting() {
        when(missionInstanceRepository.countClaimedBefore(org.mockito.ArgumentMatchers.any())).thenReturn(4L);
        when(auditOutboxRepository.countCompletedBefore(org.mockito.ArgumentMatchers.any())).thenReturn(7L);

        StaleDataCleanupService.CleanupReport report = service.execute(true);

        assertTrue(report.dryRun());
        assertEquals(4L, report.eligibleMissionInstances());
        assertEquals(7L, report.eligibleOutboxEvents());
        assertEquals(0, report.totalDeleted());
        verify(missionInstanceRepository, never()).deleteClaimedBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt());
        verify(auditOutboxRepository, never()).deleteCompletedBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void executeDeletesEligibleRowsFromBothTables() {
        when(missionInstanceRepository.countClaimedBefore(org.mockito.ArgumentMatchers.any())).thenReturn(2L);
        when(auditOutboxRepository.countCompletedBefore(org.mockito.ArgumentMatchers.any())).thenReturn(3L);
        when(missionInstanceRepository.deleteClaimedBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(500))).thenReturn(2);
        when(auditOutboxRepository.deleteCompletedBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(500))).thenReturn(3);

        StaleDataCleanupService.CleanupReport report = service.execute(false);

        assertEquals(2, report.missionInstancesDeleted());
        assertEquals(3, report.outboxEventsDeleted());
        assertEquals(5, report.totalDeleted());
        verify(missionInstanceRepository).deleteClaimedBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(500));
        verify(auditOutboxRepository).deleteCompletedBefore(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(500));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = StaleDataCleanupService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }
}
