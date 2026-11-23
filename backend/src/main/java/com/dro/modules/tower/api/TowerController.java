package com.dro.modules.tower.api;

import com.dro.modules.tower.application.TowerService;
import com.dro.modules.tower.api.TowerDtos.*;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players/me/tower")
public class TowerController {
    private final TowerService service;
    public TowerController(TowerService service) { this.service = service; }
    @GetMapping
    public Page page(@RequestHeader("Authorization") String token) {
        return service.page(TokenExtractor.extractPlayerId(token));
    }
    @PostMapping("/challenge")
    public Page challenge(@RequestHeader("Authorization") String token, @RequestBody @Valid Challenge request) {
        return service.challenge(TokenExtractor.extractPlayerId(token), request);
    }
}
