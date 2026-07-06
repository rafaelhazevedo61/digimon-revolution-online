package com.dro.modules.digimon.api;

import com.dro.modules.digimon.application.AddExperienceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/digimon")
@RequiredArgsConstructor
public class AdminDigimonController {

    private final AddExperienceUseCase addExperienceUseCase;

    @PostMapping("/add-xp")
    public ResponseEntity<Void> addXp(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int amount
    ) {
        addExperienceUseCase.execute(authorization, amount);
        return ResponseEntity.ok().build();
    }
}
