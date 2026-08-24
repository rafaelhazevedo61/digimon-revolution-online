package com.dro.modules.digimon.api;

import com.dro.modules.digimon.api.dto.request.UpdateDigimonInfoImageRequest;
import com.dro.modules.digimon.api.dto.response.DigimonInfoResponse;
import com.dro.modules.digimon.application.UpdateDigimonInfoImageUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints administrativos do catálogo-base de Digimons.
 */
@RestController
@RequestMapping("/admin/digimon-infos")
public class AdminDigimonInfoController {
    private final UpdateDigimonInfoImageUseCase updateDigimonInfoImageUseCase;

    @PutMapping("/{id}/image")
    public ResponseEntity<DigimonInfoResponse> updateImage(@PathVariable Long id, @RequestBody @Valid UpdateDigimonInfoImageRequest request) {
        return ResponseEntity.ok(updateDigimonInfoImageUseCase.execute(id, request));
    }

    public AdminDigimonInfoController(final UpdateDigimonInfoImageUseCase updateDigimonInfoImageUseCase) {
        this.updateDigimonInfoImageUseCase = updateDigimonInfoImageUseCase;
    }
}
