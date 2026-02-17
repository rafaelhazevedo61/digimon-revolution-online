package com.dro.modules.digimon.api;

import com.dro.modules.digimon.application.GetDigimonUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/digimon")
@RequiredArgsConstructor
public class DigimonController {

    private final GetDigimonUseCase useCase;

    @GetMapping("/me")
    public ResponseEntity<DigimonResponse> me(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(useCase.execute(authorization));
    }
}
