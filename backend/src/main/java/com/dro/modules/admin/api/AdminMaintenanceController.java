package com.dro.modules.admin.api;

import com.dro.modules.maintenance.application.StaleDataCleanupService;
import com.dro.shared.audit.AdminAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Ferramentas administrativas de manutenção e limpeza do banco. */
@RestController
@RequestMapping("/admin/maintenance")
public class AdminMaintenanceController {
    private final StaleDataCleanupService staleDataCleanupService;
    private final AdminAuditService adminAuditService;

    @PostMapping("/cleanup-stale-data")
    public ResponseEntity<Map<String, Object>> cleanupStaleData(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "true") boolean dryRun
    ) {
        StaleDataCleanupService.CleanupReport report = staleDataCleanupService.execute(dryRun);
        if (!dryRun) {
            adminAuditService.success(
                    authorization,
                    "ADMIN_CLEANUP_STALE_DATA",
                    "Maintenance",
                    "cleanup-stale-data",
                    "cleanup-stale-data",
                    "Limpeza de dados antigos executada",
                    Map.of(
                            "missionInstancesDeleted", report.missionInstancesDeleted(),
                            "outboxEventsDeleted", report.outboxEventsDeleted(),
                            "cutoff", report.cutoff().toString()
                    )
            );
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("dryRun", report.dryRun());
        response.put("cutoff", report.cutoff());
        response.put("eligibleMissionInstances", report.eligibleMissionInstances());
        response.put("eligibleOutboxEvents", report.eligibleOutboxEvents());
        response.put("missionInstancesDeleted", report.missionInstancesDeleted());
        response.put("outboxEventsDeleted", report.outboxEventsDeleted());
        response.put("totalDeleted", report.totalDeleted());
        return ResponseEntity.ok(response);
    }

    public AdminMaintenanceController(
            StaleDataCleanupService staleDataCleanupService,
            AdminAuditService adminAuditService
    ) {
        this.staleDataCleanupService = staleDataCleanupService;
        this.adminAuditService = adminAuditService;
    }
}
