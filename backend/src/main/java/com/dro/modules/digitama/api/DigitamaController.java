package com.dro.modules.digitama.api;

import com.dro.modules.digitama.api.dto.HatchDigitamaResponse;
import com.dro.modules.digitama.api.dto.SelectDigitamaRequest;
import com.dro.modules.digitama.application.HatchDigitamaUseCase;
import com.dro.modules.digitama.application.SelectDigitamaUseCase;
import com.dro.modules.digimon.domain.Digimon;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/digitama")
@RequiredArgsConstructor
public class DigitamaController {

    private final SelectDigitamaUseCase selectDigitamaUseCase;
    private final HatchDigitamaUseCase hatchDigitamaUseCase;

    @PostMapping("/select")
    public ResponseEntity<Void> select(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SelectDigitamaRequest request
    ) {
        selectDigitamaUseCase.execute(authorization, request.type());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/hatch")
    public ResponseEntity<HatchDigitamaResponse> hatch(
            @RequestHeader("Authorization") String authorization
    ) {
        Digimon digimon = hatchDigitamaUseCase.execute(authorization);
        return ResponseEntity.ok(HatchDigitamaResponse.from(digimon));
    }

}
