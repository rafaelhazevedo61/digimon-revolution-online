package com.dro.modules.digimon.api;

import com.dro.modules.digimon.api.dto.response.DigimonInfoPageResponse;
import com.dro.modules.digimon.application.GetDigimonInfosUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/digimon-infos")
@RequiredArgsConstructor
public class DigimonInfoController {

    private final GetDigimonInfosUseCase getDigimonInfosUseCase;

    @GetMapping
    public ResponseEntity<DigimonInfoPageResponse> getDigimonInfos(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String attribute,
            @RequestParam(required = false) String element,
            @RequestParam(required = false) String specie,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                normalizePageSize(size),
                Sort.by(
                        Sort.Order.asc("stage"),
                        Sort.Order.asc("name")
                )
        );

        return ResponseEntity.ok(
                getDigimonInfosUseCase.execute(
                        name,
                        stage,
                        attribute,
                        element,
                        specie,
                        pageRequest
                )
        );
    }

    private int normalizePageSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }
}