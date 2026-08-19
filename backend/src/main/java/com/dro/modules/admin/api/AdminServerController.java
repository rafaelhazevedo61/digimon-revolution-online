package com.dro.modules.admin.api;

import com.dro.modules.server.application.GlobalDamageBuffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Componente da camada de controller da API do módulo de Administração.
 */
@RestController
@RequestMapping("/admin/server")
@RequiredArgsConstructor
public class AdminServerController {

    private final GlobalDamageBuffService globalDamageBuffService;

    @GetMapping("/damage-buff")
    public ResponseEntity<GlobalDamageBuffService.State> getDamageBuff() {
        return ResponseEntity.ok(globalDamageBuffService.getState());
    }

    @PostMapping("/damage-buff/toggle")
    public ResponseEntity<GlobalDamageBuffService.State> toggleDamageBuff() {
        return ResponseEntity.ok(globalDamageBuffService.toggle());
    }

    @PostMapping("/damage-buff")
    public ResponseEntity<GlobalDamageBuffService.State> setDamageBuff(
            @RequestBody Map<String, Object> body
    ) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        double multiplier = 100.0;
        if (body.containsKey("multiplier")) {
            try {
                multiplier = Double.parseDouble(body.get("multiplier").toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return ResponseEntity.ok(globalDamageBuffService.set(enabled, multiplier));
    }
}
