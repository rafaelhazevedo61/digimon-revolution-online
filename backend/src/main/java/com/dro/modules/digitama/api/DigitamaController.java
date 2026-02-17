package com.dro.modules.digitama.api;

import com.dro.modules.digitama.application.SelectDigitamaUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/digitama")
@RequiredArgsConstructor
public class DigitamaController {

    private final SelectDigitamaUseCase useCase;

    @PostMapping("/select")
    public ResponseEntity<Void> select(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SelectDigitamaRequest request
    ) {
        useCase.execute(authorization, request.type());
        return ResponseEntity.ok().build();
    }
}
