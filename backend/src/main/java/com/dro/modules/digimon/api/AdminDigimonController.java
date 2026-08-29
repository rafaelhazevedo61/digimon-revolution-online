package com.dro.modules.digimon.api;

import com.dro.modules.digimon.application.AddExperienceUseCase;
import com.dro.modules.digimon.application.SimulateTraitHatchUseCase;
import com.dro.modules.digimon.api.dto.response.TraitHatchSimulationResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.AdminAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Digimon.
 */
@RestController
@RequestMapping("/admin/digimon")
public class AdminDigimonController {
    private final AddExperienceUseCase addExperienceUseCase;
    private final SimulateTraitHatchUseCase simulateTraitHatchUseCase;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final AdminAuditService adminAuditService;

    @PostMapping("/add-xp")
    public ResponseEntity<Void> addXp(@RequestHeader("Authorization") String authorization, @RequestParam UUID digimonId, @RequestParam int amount) {
        addExperienceUseCase.executeForDigimon(digimonId, amount);
        adminAuditService.success(authorization, "ADMIN_DIGIMON_ADD_XP", "Digimon", digimonId.toString(), "add-xp", "Experiência concedida ao Digimon", Map.of("targetDigimonId", digimonId.toString(), "amount", amount));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/simulator/trait-hatch")
    public ResponseEntity<TraitHatchSimulationResponse> simulateTraitHatch(@RequestParam(defaultValue = "1000") int attempts) {
        return ResponseEntity.ok(simulateTraitHatchUseCase.execute(attempts));
    }

    @GetMapping("/by-player/{playerId}")
    public ResponseEntity<?> getByPlayer(@PathVariable UUID playerId) {
        UUID activeDigimonId = playerRepository.findById(playerId)
                .map(Player::getActiveDigimonId)
                .orElse(null);
        List<Digimon> digimons = activeDigimonId == null
                ? List.of()
                : digimonRepository.findById(activeDigimonId)
                    .filter(digimon -> playerId.equals(digimon.getPlayerId()))
                    .map(List::of)
                    .orElseGet(List::of);
        var result = digimons.stream().map(d -> Map.of("id", d.getId().toString(), "name", d.getName(), "type", d.getType(), "level", String.valueOf(d.getLevel()), "stage", d.getStage().name(), "status", d.getStatus().name())).toList();
        return ResponseEntity.ok(result);
    }

    public AdminDigimonController(final AddExperienceUseCase addExperienceUseCase, final SimulateTraitHatchUseCase simulateTraitHatchUseCase, final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final AdminAuditService adminAuditService) {
        this.addExperienceUseCase = addExperienceUseCase;
        this.simulateTraitHatchUseCase = simulateTraitHatchUseCase;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.adminAuditService = adminAuditService;
    }
}
