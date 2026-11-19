package com.dro.modules.digitama.api;

import com.dro.modules.digitama.api.dto.response.DigitamaHistoryResponse;
import com.dro.modules.digitama.api.dto.response.HatchDigitamaResponse;
import com.dro.modules.digitama.api.dto.request.SelectDigitamaRequest;
import com.dro.modules.digitama.application.GetDigitamaHistoryUseCase;
import com.dro.modules.digitama.application.HatchDigitamaUseCase;
import com.dro.modules.digitama.application.SelectDigitamaUseCase;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Digitama.
 */
@RestController
@RequestMapping("/digitama")
public class DigitamaController {

    private final SelectDigitamaUseCase selectDigitamaUseCase;
    private final HatchDigitamaUseCase hatchDigitamaUseCase;
    private final GetDigitamaHistoryUseCase getDigitamaHistoryUseCase;
    private final DigimonInfosRepository digimonInfosRepository;

    public DigitamaController (SelectDigitamaUseCase selectDigitamaUseCase, HatchDigitamaUseCase hatchDigitamaUseCase, GetDigitamaHistoryUseCase getDigitamaHistoryUseCase, DigimonInfosRepository digimonInfosRepository) {
        this.selectDigitamaUseCase = selectDigitamaUseCase;
        this.hatchDigitamaUseCase = hatchDigitamaUseCase;
        this.getDigitamaHistoryUseCase = getDigitamaHistoryUseCase;
        this.digimonInfosRepository = digimonInfosRepository;
    }

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
        String imageUrl = digimon.getDigimonInfoId() == null ? null : digimonInfosRepository.findById(digimon.getDigimonInfoId())
                .map(info -> info.getImageUrl())
                .orElse(null);
        return ResponseEntity.ok(HatchDigitamaResponse.from(digimon, imageUrl));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DigitamaHistoryResponse>> history(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(getDigitamaHistoryUseCase.execute(authorization));
    }
}