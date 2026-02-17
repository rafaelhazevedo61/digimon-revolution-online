package com.dro.modules.incubation.api;

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

    @PostMapping("/start")
    public ResponseEntity<Void> start(
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
}
