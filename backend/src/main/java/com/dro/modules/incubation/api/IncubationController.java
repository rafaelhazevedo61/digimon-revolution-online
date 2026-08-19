package com.dro.modules.incubation.api;

import com.dro.modules.digitama.api.dto.response.HatchDigitamaResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.incubation.application.ClaimIncubationUseCase;
import com.dro.modules.incubation.application.GetIncubationUseCase;
import com.dro.modules.incubation.application.StartIncubationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Componente da camada de controller da API do módulo de Incubação.
 */
@RestController
@RequestMapping("/incubation")
@RequiredArgsConstructor
public class IncubationController {

    private final StartIncubationUseCase startUseCase;
    private final ClaimIncubationUseCase claimUseCase;
    private final GetIncubationUseCase getUseCase;

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
        return ResponseEntity.ok(HatchDigitamaResponse.from(digimon));
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
