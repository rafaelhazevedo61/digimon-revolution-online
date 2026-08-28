package com.dro.modules.admin.api;

import com.dro.modules.server.application.GlobalDamageBuffService;
import com.dro.shared.audit.AdminAuditService;
import com.dro.shared.gameplay.WeekendDoubleRewardRules;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

/**
 * Componente da camada de controller da API do módulo de Administração.
 */
@RestController
@RequestMapping("/admin/server")
public class AdminServerController {
    private final GlobalDamageBuffService globalDamageBuffService;
    private final AdminAuditService adminAuditService;

    @GetMapping("/weekend-double-reward")
    public ResponseEntity<WeekendDoubleRewardRules.State> getWeekendDoubleReward() {
        return ResponseEntity.ok(WeekendDoubleRewardRules.getState(Instant.now()));
    }

    @PostMapping("/weekend-double-reward/toggle")
    public ResponseEntity<WeekendDoubleRewardRules.State> toggleWeekendDoubleReward(@RequestHeader("Authorization") String authorization) {
        WeekendDoubleRewardRules.State state = WeekendDoubleRewardRules.toggle(Instant.now());
        auditWeekendDoubleReward(authorization, "ADMIN_WEEKEND_DOUBLE_REWARD_TOGGLE", "toggle", state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/weekend-double-reward/automatic")
    public ResponseEntity<WeekendDoubleRewardRules.State> useAutomaticWeekendDoubleReward(@RequestHeader("Authorization") String authorization) {
        WeekendDoubleRewardRules.State state = WeekendDoubleRewardRules.setManualOverride(null, Instant.now());
        auditWeekendDoubleReward(authorization, "ADMIN_WEEKEND_DOUBLE_REWARD_AUTOMATIC", "automatic", state);
        return ResponseEntity.ok(state);
    }

    @GetMapping("/damage-buff")
    public ResponseEntity<GlobalDamageBuffService.State> getDamageBuff() {
        return ResponseEntity.ok(globalDamageBuffService.getState());
    }

    @PostMapping("/damage-buff/toggle")
    public ResponseEntity<GlobalDamageBuffService.State> toggleDamageBuff(@RequestHeader("Authorization") String authorization) {
        GlobalDamageBuffService.State state = globalDamageBuffService.toggle();
        audit(authorization, "ADMIN_DAMAGE_BUFF_TOGGLE", "toggle", state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/damage-buff")
    public ResponseEntity<GlobalDamageBuffService.State> setDamageBuff(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, Object> body) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        double multiplier = 100.0;
        if (body.containsKey("multiplier")) {
            try {
                multiplier = Double.parseDouble(body.get("multiplier").toString());
            } catch (NumberFormatException ignored) {
            }
        }
        GlobalDamageBuffService.State state = globalDamageBuffService.set(enabled, multiplier);
        audit(authorization, "ADMIN_DAMAGE_BUFF_SET", "set", state);
        return ResponseEntity.ok(state);
    }

    private void auditWeekendDoubleReward(String authorization, String eventType, String operation, WeekendDoubleRewardRules.State state) {
        adminAuditService.success(authorization, eventType, "Server", "weekend-double-reward", operation, "Configuração do evento de Double XP e Double Bits alterada", Map.of("active", state.active(), "manualOverride", state.manualOverride(), "scheduledActive", state.scheduledActive()));
    }

    private void audit(String authorization, String eventType, String operation, GlobalDamageBuffService.State state) {
        adminAuditService.success(authorization, eventType, "Server", "damage-buff", operation, "Configuração global de dano alterada", Map.of("enabled", state.enabled(), "multiplier", state.multiplier()));
    }

    public AdminServerController(final GlobalDamageBuffService globalDamageBuffService, final AdminAuditService adminAuditService) {
        this.globalDamageBuffService = globalDamageBuffService;
        this.adminAuditService = adminAuditService;
    }
}
