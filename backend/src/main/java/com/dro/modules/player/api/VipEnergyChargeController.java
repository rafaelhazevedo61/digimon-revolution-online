package com.dro.modules.player.api;

import com.dro.modules.player.application.VipEnergyChargeService;
import com.dro.shared.util.TokenExtractor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players/me/vip/energy-charge")
public class VipEnergyChargeController {
    private final VipEnergyChargeService service;

    public VipEnergyChargeController(VipEnergyChargeService service) {
        this.service = service;
    }

    @GetMapping
    public VipEnergyChargeService.Status status(@RequestHeader("Authorization") String authorization) {
        return service.getStatus(TokenExtractor.extractPlayerId(authorization));
    }

    @PostMapping
    public VipEnergyChargeService.Status claim(@RequestHeader("Authorization") String authorization) {
        return service.claim(TokenExtractor.extractPlayerId(authorization));
    }
}
