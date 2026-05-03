package com.dro.modules.digitama.application;

import com.dro.modules.digitama.api.dto.response.AvailableDigitamaEntryResponse;
import com.dro.modules.digitama.api.dto.response.AvailableDigitamaPoolResponse;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.digimon.domain.DigimonInfos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAvailableDigitamaPoolsUseCase {

    private final DigitamaPoolRepository digitamaPoolRepository;

    public List<AvailableDigitamaPoolResponse> execute() {
        return digitamaPoolRepository.findByActiveTrueAndContentActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AvailableDigitamaPoolResponse toResponse(DigitamaPool pool) {
        List<AvailableDigitamaEntryResponse> entries = pool.getEntries()
                .stream()
                .filter(DigitamaPoolEntry::isActive)
                .sorted(Comparator.comparingInt(DigitamaPoolEntry::getWeight).reversed())
                .map(this::toEntryResponse)
                .toList();

        return new AvailableDigitamaPoolResponse(
                pool.getCode(),
                pool.getName(),
                pool.getDescription(),
                entries
        );
    }

    private AvailableDigitamaEntryResponse toEntryResponse(DigitamaPoolEntry entry) {
        DigimonInfos digimonInfo = entry.getDigimonInfo();

        return new AvailableDigitamaEntryResponse(
                digimonInfo.getId(),
                digimonInfo.getName(),
                digimonInfo.getStage().name(),
                digimonInfo.getAttribute().name(),
                digimonInfo.getElement().name(),
                digimonInfo.getSpecie().name(),
                entry.getWeight()
        );
    }
}