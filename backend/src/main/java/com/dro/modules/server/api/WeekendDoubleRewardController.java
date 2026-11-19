package com.dro.modules.server.api;

import com.dro.shared.gameplay.WeekendDoubleRewardRules;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Consulta pública do estado do evento recorrente de Double XP e Double Bits.
 */
@RestController
@RequestMapping("/events")
public class WeekendDoubleRewardController {

    @GetMapping("/weekend-double-reward")
    public ResponseEntity<WeekendDoubleRewardRules.State> getStatus() {
        return ResponseEntity.ok(WeekendDoubleRewardRules.getState(Instant.now()));
    }
}

