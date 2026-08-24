package com.dro.modules.admin.api;

import com.dro.modules.arena.application.AdminResetArenaDailyUseCase;
import com.dro.modules.boss.world.application.AdminForceNewWorldBossCycleUseCase;
import com.dro.modules.boss.world.application.AdminResetWorldBossDailyUseCase;
import com.dro.modules.clan.application.AdminCompleteClanMissionsUseCase;
import com.dro.modules.clan.raid.application.AdminResetClanRaidDailyUseCase;
import com.dro.shared.audit.AdminAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Componente da camada de controller da API do módulo de Administração.
 */
@RestController
@RequestMapping("/admin/tools")
public class AdminToolsController {
    private final AdminResetArenaDailyUseCase resetArenaDailyUseCase;
    private final AdminResetClanRaidDailyUseCase resetClanRaidDailyUseCase;
    private final AdminResetWorldBossDailyUseCase resetWorldBossDailyUseCase;
    private final AdminForceNewWorldBossCycleUseCase forceNewWorldBossCycleUseCase;
    private final AdminCompleteClanMissionsUseCase completeClanMissionsUseCase;
    private final AdminAuditService adminAuditService;

    @PostMapping("/reset-daily-arena-attacks")
    public ResponseEntity<Map<String, Object>> resetDailyArenaAttacks(@RequestHeader("Authorization") String authorization) {
        int playersReset = resetArenaDailyUseCase.execute();
        audit(authorization, "ADMIN_RESET_DAILY_ARENA_ATTACKS", "reset-daily-arena-attacks", Map.of("playersReset", playersReset));
        return ResponseEntity.ok(Map.of("message", "Daily arena attacks reset successfully", "playersReset", playersReset));
    }

    @PostMapping("/reset-clan-raid-daily")
    public ResponseEntity<Map<String, Object>> resetClanRaidDaily(@RequestHeader("Authorization") String authorization) {
        int raidsReset = resetClanRaidDailyUseCase.execute();
        audit(authorization, "ADMIN_RESET_CLAN_RAID_DAILY", "reset-clan-raid-daily", Map.of("raidsReset", raidsReset));
        return ResponseEntity.ok(Map.of("message", "Clan raid daily reset successfully", "raidsReset", raidsReset));
    }

    @PostMapping("/reset-world-boss-daily")
    public ResponseEntity<Map<String, Object>> resetWorldBossDaily(@RequestHeader("Authorization") String authorization) {
        int instancesReset = resetWorldBossDailyUseCase.execute();
        audit(authorization, "ADMIN_RESET_WORLD_BOSS_DAILY", "reset-world-boss-daily", Map.of("instancesReset", instancesReset));
        return ResponseEntity.ok(Map.of("message", "World boss daily reset successfully", "instancesReset", instancesReset));
    }

    @PostMapping("/force-new-world-boss-cycle")
    public ResponseEntity<Map<String, Object>> forceNewWorldBossCycle(@RequestHeader("Authorization") String authorization) {
        var instance = forceNewWorldBossCycleUseCase.execute();
        audit(authorization, "ADMIN_FORCE_NEW_WORLD_BOSS_CYCLE", "force-new-world-boss-cycle", Map.of("instanceId", instance.getId(), "cycleNumber", instance.getCycleNumber()));
        return ResponseEntity.ok(Map.of("message", "New world boss cycle opened successfully", "instanceId", instance.getId(), "cycleNumber", instance.getCycleNumber(), "bossDate", instance.getBossDate()));
    }

    @PostMapping("/complete-clan-missions")
    public ResponseEntity<Map<String, Object>> completeClanMissions(@RequestHeader("Authorization") String authorization) {
        long completed = completeClanMissionsUseCase.execute();
        audit(authorization, "ADMIN_COMPLETE_CLAN_MISSIONS", "complete-clan-missions", Map.of("completedCount", completed));
        return ResponseEntity.ok(Map.of("message", "All in-progress clan missions completed", "completedCount", completed));
    }

    private void audit(String authorization, String eventType, String operation, Map<String, Object> metadata) {
        adminAuditService.success(authorization, eventType, "AdminTool", operation, operation, "Ferramenta administrativa executada", metadata);
    }

    public AdminToolsController(final AdminResetArenaDailyUseCase resetArenaDailyUseCase, final AdminResetClanRaidDailyUseCase resetClanRaidDailyUseCase, final AdminResetWorldBossDailyUseCase resetWorldBossDailyUseCase, final AdminForceNewWorldBossCycleUseCase forceNewWorldBossCycleUseCase, final AdminCompleteClanMissionsUseCase completeClanMissionsUseCase, final AdminAuditService adminAuditService) {
        this.resetArenaDailyUseCase = resetArenaDailyUseCase;
        this.resetClanRaidDailyUseCase = resetClanRaidDailyUseCase;
        this.resetWorldBossDailyUseCase = resetWorldBossDailyUseCase;
        this.forceNewWorldBossCycleUseCase = forceNewWorldBossCycleUseCase;
        this.completeClanMissionsUseCase = completeClanMissionsUseCase;
        this.adminAuditService = adminAuditService;
    }
}
