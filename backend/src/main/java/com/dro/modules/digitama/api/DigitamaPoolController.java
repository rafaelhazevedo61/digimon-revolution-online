package com.dro.modules.digitama.api;

import com.dro.modules.digitama.api.dto.response.AvailableDigitamaPoolResponse;
import com.dro.modules.digitama.application.GetAvailableDigitamaPoolsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/digitama-pools")
@RequiredArgsConstructor
public class DigitamaPoolController {

    private final GetAvailableDigitamaPoolsUseCase getAvailableDigitamaPoolsUseCase;

    @GetMapping("/available")
    public ResponseEntity<List<AvailableDigitamaPoolResponse>> getAvailablePools() {
        return ResponseEntity.ok(getAvailableDigitamaPoolsUseCase.execute());
    }
}