package com.dro.modules.incubation.api;

import com.dro.modules.digitama.api.dto.response.HatchDigitamaResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.incubation.api.dto.request.StartIncubationRequest;
import com.dro.modules.incubation.application.ClaimIncubationUseCase;
import com.dro.modules.incubation.application.GetIncubationUseCase;
import com.dro.modules.incubation.application.StartIncubationUseCase;
import com.dro.modules.incubation.application.SetIncubationAutomationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Incubação.
 */
@RestController
@RequestMapping("/incubation")
public class IncubationController {

    private final StartIncubationUseCase startUseCase;
    private final ClaimIncubationUseCase claimUseCase;
    private final GetIncubationUseCase getUseCase;
    private final DigimonInfosRepository digimonInfosRepository;
    private final SetIncubationAutomationUseCase automationUseCase;

    public IncubationController(
            StartIncubationUseCase startUseCase,
            ClaimIncubationUseCase claimUseCase,
            GetIncubationUseCase getUseCase,
            DigimonInfosRepository digimonInfosRepository,
            SetIncubationAutomationUseCase automationUseCase
    ) {
        this.startUseCase = startUseCase;
        this.claimUseCase = claimUseCase;
        this.getUseCase = getUseCase;
        this.digimonInfosRepository = digimonInfosRepository;
        this.automationUseCase = automationUseCase;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid StartIncubationRequest request
    ) {
        startUseCase.execute(
                authorization,
                request.slotNumber(),
                request.digitamaType(),
                request.incubatorType()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{incubationId}/claim")
    public ResponseEntity<HatchDigitamaResponse> claim(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID incubationId
    ) {
        Digimon digimon = claimUseCase.execute(authorization, incubationId);
        String imageUrl = digimon.getDigimonInfoId() == null
                ? null
                : digimonInfosRepository.findById(digimon.getDigimonInfoId())
                        .map(info -> info.getImageUrl())
                        .orElse(null);
        return ResponseEntity.ok(HatchDigitamaResponse.from(digimon, imageUrl));
    }

    @PatchMapping("/{incubationId}/automation")
    public ResponseEntity<Void> automation(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID incubationId,
            @RequestParam boolean autoClaim,
            @RequestParam(required = false) Boolean autoRepeat
    ) {
        automationUseCase.execute(authorization, incubationId, autoClaim, autoRepeat);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(getUseCase.execute(authorization));
    }
}
