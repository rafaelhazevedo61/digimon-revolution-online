package com.dro.modules.digitama.application;

import com.dro.modules.digitama.api.dto.response.AvailableDigitamaEntryResponse;
import com.dro.modules.digitama.api.dto.response.AvailableDigitamaPoolResponse;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.digimon.domain.DigimonInfos;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digitama.
 */
@Service
public class GetAvailableDigitamaPoolsUseCase {
    private final DigitamaPoolRepository digitamaPoolRepository;
    private final DigitamaPoolEligibilityService digitamaPoolEligibilityService;

    public List<AvailableDigitamaPoolResponse> execute() {
        return digitamaPoolRepository.findByActiveTrueAndContentActiveTrue().stream().map(this::toResponse).toList();
    }

    private AvailableDigitamaPoolResponse toResponse(DigitamaPool pool) {
        List<AvailableDigitamaEntryResponse> entries = digitamaPoolEligibilityService.getEligibleEntries(pool).stream()
                .sorted(Comparator.comparingInt(DigitamaPoolEntry::getWeight).reversed())
                .map(this::toEntryResponse)
                .toList();
        DigitamaType digitamaType = DigitamaType.fromPoolCode(pool.getCode());
        String type = digitamaType.name();
        String element = digitamaType.getElement() == null ? null : digitamaType.getElement().name();
        boolean selectable = !entries.isEmpty();
        String lockedReason = selectable ? null : "Nenhum Digimon BABY elegível está habilitado.";
        return new AvailableDigitamaPoolResponse(
                pool.getCode(),
                type,
                element,
                pool.getName(),
                pool.getDescription(),
                selectable,
                lockedReason,
                entries
        );
    }

    private AvailableDigitamaEntryResponse toEntryResponse(DigitamaPoolEntry entry) {
        DigimonInfos digimonInfo = entry.getDigimonInfo();
        return new AvailableDigitamaEntryResponse(digimonInfo.getId(), digimonInfo.getName(), digimonInfo.getStage().name(), digimonInfo.getAttribute().name(), digimonInfo.getElement().name(), digimonInfo.getSpecie().name(), entry.getWeight());
    }

    public GetAvailableDigitamaPoolsUseCase(
            final DigitamaPoolRepository digitamaPoolRepository,
            final DigitamaPoolEligibilityService digitamaPoolEligibilityService
    ) {
        this.digitamaPoolRepository = digitamaPoolRepository;
        this.digitamaPoolEligibilityService = digitamaPoolEligibilityService;
    }
}
