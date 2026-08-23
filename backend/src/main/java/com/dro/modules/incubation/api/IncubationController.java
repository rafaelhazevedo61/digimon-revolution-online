package com.dro.modules.incubation.api;

import com.dro.modules.digitama.api.dto.response.HatchDigitamaResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.incubation.api.dto.request.StartIncubationRequest;
import com.dro.modules.incubation.application.ClaimIncubationUseCase;
import com.dro.modules.incubation.application.GetIncubationUseCase;
import com.dro.modules.incubation.application.StartIncubationUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public IncubationController(
            StartIncubationUseCase startUseCase,
            ClaimIncubationUseCase claimUseCase,
            GetIncubationUseCase getUseCase,
            DigimonInfosRepository digimonInfosRepository
    ) {
        this.startUseCase = startUseCase;
        this.claimUseCase = claimUseCase;
        this.getUseCase = getUseCase;
        this.digimonInfosRepository = digimonInfosRepository;
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start (
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid StartIncubationRequest request
    ) {
        startUseCase.execute(
                authorization,
                request.digitamaType(),
                request.incubatorType()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/claim")
    public ResponseEntity<HatchDigitamaResponse> claim (
            @RequestHeader("Authorization") String authorization
    ) {
        Digimon digimon = claimUseCase.execute(authorization);
        String imageUrl = digimon.getDigimonInfoId() == null ? null : digimonInfosRepository.findById(digimon.getDigimonInfoId())
                .map(info -> info.getImageUrl())
                .orElse(null);
        return ResponseEntity.ok(HatchDigitamaResponse.from(digimon, imageUrl));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me (
            @RequestHeader("Authorization") String authorization
    ) {
        var response = getUseCase.execute(authorization);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }
}
