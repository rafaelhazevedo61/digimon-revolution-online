package com.dro.modules.incubation.api;

import com.dro.modules.incubation.application.ClaimIncubationUseCase;
import com.dro.modules.incubation.application.StartIncubationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incubation")
@RequiredArgsConstructor
public class IncubationController {

    private final StartIncubationUseCase startUseCase;
    private final ClaimIncubationUseCase claimUseCase;

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
    public ResponseEntity<Void> claim (
            @RequestHeader("Authorization") String authorization
    ) {
        claimUseCase.execute(authorization);
        return ResponseEntity.ok().build();
    }
}
