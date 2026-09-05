package com.dro.modules.maintenance.application;

import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.shared.audit.AuditOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/** Remove registros finalizados que já ultrapassaram a janela de retenção. */
@Service
public class StaleDataCleanupService {
    private final MissionInstanceRepository missionInstanceRepository;
    private final AuditOutboxRepository auditOutboxRepository;

    @Value("${dro.maintenance.cleanup.retention-hours:24}")
    private long retentionHours;

    @Value("${dro.maintenance.cleanup.batch-size:500}")
    private int batchSize;

    /** Executa a limpeza automática diariamente por padrão às 03:00 UTC. */
    @Scheduled(
            cron = "${dro.maintenance.cleanup.cron:0 0 3 * * *}",
            zone = "${dro.maintenance.cleanup.zone:UTC}"
    )
    public void scheduledCleanup() {
        CleanupReport report = execute(false);
        if (report.totalDeleted() > 0) {
            org.slf4j.LoggerFactory.getLogger(StaleDataCleanupService.class)
                    .info("Stale data cleanup completed: missionInstancesDeleted={}, outboxEventsDeleted={}, cutoff={}",
                            report.missionInstancesDeleted(), report.outboxEventsDeleted(), report.cutoff());
        }
    }

    @Transactional
    public CleanupReport execute(boolean dryRun) {
        Instant cutoff = Instant.now().minus(Duration.ofHours(Math.max(1, retentionHours)));
        long eligibleMissionInstances = missionInstanceRepository.countClaimedBefore(cutoff);
        long eligibleOutboxEvents = auditOutboxRepository.countCompletedBefore(cutoff);

        if (dryRun) {
            return new CleanupReport(cutoff, eligibleMissionInstances, eligibleOutboxEvents, 0, 0, true);
        }

        int deletedMissionInstances = deleteMissionInstances(cutoff);
        int deletedOutboxEvents = deleteOutboxEvents(cutoff);
        return new CleanupReport(
                cutoff,
                eligibleMissionInstances,
                eligibleOutboxEvents,
                deletedMissionInstances,
                deletedOutboxEvents,
                false
        );
    }

    private int deleteMissionInstances(Instant cutoff) {
        int total = 0;
        int deleted;
        do {
            deleted = missionInstanceRepository.deleteClaimedBefore(cutoff, Math.max(1, batchSize));
            total += deleted;
        } while (deleted == Math.max(1, batchSize));
        return total;
    }

    private int deleteOutboxEvents(Instant cutoff) {
        int total = 0;
        int deleted;
        do {
            deleted = auditOutboxRepository.deleteCompletedBefore(cutoff, Math.max(1, batchSize));
            total += deleted;
        } while (deleted == Math.max(1, batchSize));
        return total;
    }

    public record CleanupReport(
            Instant cutoff,
            long eligibleMissionInstances,
            long eligibleOutboxEvents,
            int missionInstancesDeleted,
            int outboxEventsDeleted,
            boolean dryRun
    ) {
        public int totalDeleted() {
            return missionInstancesDeleted + outboxEventsDeleted;
        }
    }

    public StaleDataCleanupService(
            MissionInstanceRepository missionInstanceRepository,
            AuditOutboxRepository auditOutboxRepository
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.auditOutboxRepository = auditOutboxRepository;
    }
}
