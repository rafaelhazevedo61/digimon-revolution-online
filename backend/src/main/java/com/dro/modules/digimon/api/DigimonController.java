package com.dro.modules.digimon.api;

import com.dro.modules.digimon.application.AddExperienceUseCase;
import com.dro.modules.digimon.application.GetDigimonUseCase;
import com.dro.modules.digimon.application.SelectActiveDigimonUseCase;
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
}
