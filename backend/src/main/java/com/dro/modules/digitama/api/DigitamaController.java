package com.dro.modules.digitama.api;

import com.dro.modules.digitama.api.dto.response.DigitamaHistoryResponse;
import com.dro.modules.digitama.api.dto.response.HatchDigitamaResponse;
import com.dro.modules.digitama.api.dto.request.SelectDigitamaRequest;
import com.dro.modules.digitama.application.GetDigitamaHistoryUseCase;
import com.dro.modules.digitama.application.HatchDigitamaUseCase;
import com.dro.modules.digitama.application.SelectDigitamaUseCase;
import com.dro.modules.digimon.domain.Digimon;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/digitama")
@RequiredArgsConstructor
public class DigitamaController {

    private final SelectDigitamaUseCase selectDigitamaUseCase;
    private final HatchDigitamaUseCase hatchDigitamaUseCase;
    private final GetDigitamaHistoryUseCase getDigitamaHistoryUseCase;

    @PostMapping("/select")
    public ResponseEntity<Void> select(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid SelectDigitamaRequest request
    ) {
        selectDigitamaUseCase.execute(authorization, request.type());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hatch")
    public ResponseEntity<HatchDigitamaResponse> hatch(
            @RequestHeader("Authorization") String authorization
    ) {
        Digimon digimon = hatchDigitamaUseCase.execute(authorization);
        return ResponseEntity.ok(HatchDigitamaResponse.from(digimon));
    }


    @GetMapping("/history")
    public ResponseEntity<List<DigitamaHistoryResponse>> history(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(getDigitamaHistoryUseCase.execute(authorization));
    }

}
