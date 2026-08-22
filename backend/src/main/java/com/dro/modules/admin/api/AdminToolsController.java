package com.dro.modules.admin.api;

import com.dro.modules.arena.application.AdminResetArenaDailyUseCase;
import com.dro.modules.boss.world.application.AdminForceNewWorldBossCycleUseCase;
import com.dro.modules.boss.world.application.AdminResetWorldBossDailyUseCase;
import com.dro.modules.clan.application.AdminCompleteClanMissionsUseCase;
import com.dro.modules.clan.raid.application.AdminResetClanRaidDailyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Componente da camada de controller da API do módulo de Administração.
 */
@RestController
@RequestMapping("/admin/tools")
@RequiredArgsConstructor
public class AdminToolsController {

    private final AdminResetArenaDailyUseCase resetArenaDailyUseCase;
    private final AdminResetClanRaidDailyUseCase resetClanRaidDailyUseCase;
    private final AdminResetWorldBossDailyUseCase resetWorldBossDailyUseCase;
    private final AdminForceNewWorldBossCycleUseCase forceNewWorldBossCycleUseCase;
    private final AdminCompleteClanMissionsUseCase completeClanMissionsUseCase;

    @PostMapping("/reset-daily-arena-attacks")
    public ResponseEntity<Map<String, Object>> resetDailyArenaAttacks() {
        int playersReset = resetArenaDailyUseCase.execute();
        return ResponseEntity.ok(Map.of(
                "message", "Daily arena attacks reset successfully",
                "playersReset", playersReset
        ));
    }

    @PostMapping("/reset-clan-raid-daily")
    public ResponseEntity<Map<String, Object>> resetClanRaidDaily() {
        int raidsReset = resetClanRaidDailyUseCase.execute();
        return ResponseEntity.ok(Map.of(
                "message", "Clan raid daily reset successfully",
                "raidsReset", raidsReset
        ));
    }

    @PostMapping("/reset-world-boss-daily")
    public ResponseEntity<Map<String, Object>> resetWorldBossDaily() {
        int instancesReset = resetWorldBossDailyUseCase.execute();
        return ResponseEntity.ok(Map.of(
                "message", "World boss daily reset successfully",
                "instancesReset", instancesReset
        ));
    }

    @PostMapping("/force-new-world-boss-cycle")
    public ResponseEntity<Map<String, Object>> forceNewWorldBossCycle() {
        var instance = forceNewWorldBossCycleUseCase.execute();
        return ResponseEntity.ok(Map.of(
                "message", "New world boss cycle opened successfully",
                "instanceId", instance.getId(),
                "cycleNumber", instance.getCycleNumber(),
                "bossDate", instance.getBossDate()
        ));
    }

    @PostMapping("/complete-clan-missions")
    public ResponseEntity<Map<String, Object>> completeClanMissions() {
        long completed = completeClanMissionsUseCase.execute();
        return ResponseEntity.ok(Map.of(
                "message", "All in-progress clan missions completed",
                "completedCount", completed
        ));
    }
}
