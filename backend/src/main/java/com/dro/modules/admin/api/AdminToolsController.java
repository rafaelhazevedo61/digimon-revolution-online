package com.dro.modules.admin.api;

import com.dro.modules.arena.application.AdminResetArenaDailyUseCase;
import com.dro.modules.clan.application.AdminCompleteClanMissionsUseCase;
import com.dro.modules.clan.raid.application.AdminResetClanRaidDailyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/tools")
@RequiredArgsConstructor
public class AdminToolsController {

    private final AdminResetArenaDailyUseCase resetArenaDailyUseCase;
    private final AdminResetClanRaidDailyUseCase resetClanRaidDailyUseCase;
    private final AdminCompleteClanMissionsUseCase completeClanMissionsUseCase;

    @PostMapping("/reset-daily-arena-attacks")
    public ResponseEntity<Map<String, Object>> resetDailyArenaAttacks() {
        long deleted = resetArenaDailyUseCase.execute();
        return ResponseEntity.ok(Map.of(
                "message", "Daily arena attacks reset successfully",
                "deletedMatches", deleted
        ));
    }

    @PostMapping("/reset-clan-raid-daily")
    public ResponseEntity<Map<String, Object>> resetClanRaidDaily() {
        AdminResetClanRaidDailyUseCase.Result result = resetClanRaidDailyUseCase.execute();
        return ResponseEntity.ok(Map.of(
                "message", "Clan raid daily reset successfully",
                "raidsReset", result.getRaidsReset(),
                "attacksDeleted", result.getAttacksDeleted()
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
