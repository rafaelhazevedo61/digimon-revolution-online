package com.dro.modules.achievement.api;

import com.dro.modules.achievement.application.AchievementService;
import com.dro.modules.achievement.api.AchievementDtos.*;
import com.dro.shared.util.TokenExtractor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players/me/achievements")
public class AchievementController {
    private final AchievementService service;
    public AchievementController(AchievementService service) { this.service = service; }

    @GetMapping
    public Page list(@RequestHeader("Authorization") String token) {
        return service.list(TokenExtractor.extractPlayerId(token));
    }

    @PostMapping("/{code}/claim")
    public Claim claim(@RequestHeader("Authorization") String token, @PathVariable String code) {
        return service.claim(TokenExtractor.extractPlayerId(token), code);
    }
}
