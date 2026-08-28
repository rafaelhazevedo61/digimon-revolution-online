package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonInfoPageResponse;
import com.dro.modules.digimon.api.dto.response.DigimonInfoResponse;
import com.dro.modules.digimon.api.dto.response.DigitamaOriginResponse;
import com.dro.modules.digitama.application.DigitamaPoolEligibilityService;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.spec.DigimonInfosSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
public class GetDigimonInfosUseCase {
    private final DigimonInfosRepository digimonInfosRepository;
    private final DigitamaPoolRepository digitamaPoolRepository;
    private final DigitamaPoolEligibilityService digitamaPoolEligibilityService;

    public DigimonInfoPageResponse execute(String name, String stage, String attribute, String element, String specie, Pageable pageable) {
        Map<Long, List<DigitamaOriginResponse>> digitamaOrigins = loadDigitamaOrigins();
        Page<DigimonInfoResponse> digimonInfos = digimonInfosRepository.findAll(DigimonInfosSpecifications.withFilters(name, stage, attribute, element, specie), pageable)
                .map(digimonInfo -> DigimonInfoResponse.from(digimonInfo, digitamaOrigins.getOrDefault(digimonInfo.getId(), List.of())));
        return DigimonInfoPageResponse.from(digimonInfos);
    }

    private Map<Long, List<DigitamaOriginResponse>> loadDigitamaOrigins() {
        Map<Long, List<DigitamaOriginResponse>> originsByDigimon = new HashMap<>();
        for (DigitamaPool pool : digitamaPoolRepository.findByActiveTrueAndContentActiveTrue()) {
            for (var entry : digitamaPoolEligibilityService.getEligibleEntries(pool)) {
                Long digimonInfoId = entry.getDigimonInfo().getId();
                originsByDigimon.computeIfAbsent(digimonInfoId, ignored -> new java.util.ArrayList<>())
                        .add(new DigitamaOriginResponse(pool.getCode(), pool.getName()));
            }
        }
        return originsByDigimon;
    }

    public GetDigimonInfosUseCase(final DigimonInfosRepository digimonInfosRepository, final DigitamaPoolRepository digitamaPoolRepository, final DigitamaPoolEligibilityService digitamaPoolEligibilityService) {
        this.digimonInfosRepository = digimonInfosRepository;
        this.digitamaPoolRepository = digitamaPoolRepository;
        this.digitamaPoolEligibilityService = digitamaPoolEligibilityService;
    }
}
