package com.dro.modules.digimon.api;

import com.dro.modules.digimon.api.dto.request.RebirthDigimonRequest;
import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.api.dto.request.SelectDigimonRequest;
import com.dro.modules.digimon.application.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/digimon")
@RequiredArgsConstructor
public class DigimonController {

    private final GetDigimonUseCase useCase;
    private final AddExperienceUseCase addExperienceUseCase;
    private final SelectActiveDigimonUseCase selectUseCase;
    private final EvolveDigimonUseCase evolveDigimonUseCase;
    private final RebirthUseCase rebirthUseCase;

    @GetMapping("/me")
    public ResponseEntity<List<DigimonResponse>> me(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(useCase.execute(authorization));
    }

    @PostMapping("/add-xp")
    public ResponseEntity<Void> addXp(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int amount
    ) {
        addExperienceUseCase.execute(authorization, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/select")
    public ResponseEntity<Void> select(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SelectDigimonRequest request
    ) {
        selectUseCase.execute(authorization, request.digimonId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/evolve")
    public ResponseEntity<String> evolve(
            @RequestHeader("Authorization") String authorization
    ) {
        evolveDigimonUseCase.execute(authorization);
        return ResponseEntity.ok("Digimon evolved successfully");
    }

    @PostMapping("/rebirth")
    public ResponseEntity<String> rebirth(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid RebirthDigimonRequest request
    ) {
        rebirthUseCase.execute(authorization, request.digimonId());
        return ResponseEntity.ok("Digimon reborn successfully");
    }
}
