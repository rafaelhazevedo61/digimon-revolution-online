package com.dro.modules.digitama.application;

import com.dro.modules.digitama.api.dto.response.DigitamaHistoryResponse;
import com.dro.modules.digitama.domain.DigitamaHistory;
import com.dro.modules.digitama.infra.DigitamaHistoryRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digitama.
 */
@Service
@RequiredArgsConstructor
public class GetDigitamaHistoryUseCase {

    private final DigitamaHistoryRepository historyRepository;

    public List<DigitamaHistoryResponse> execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        return historyRepository.findByPlayerIdOrderByHatchedAtDesc(playerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DigitamaHistoryResponse toResponse(DigitamaHistory history) {
        return new DigitamaHistoryResponse(
                history.getId(),
                history.getDigitamaType(),
                history.getDigimonName(),
                history.getDigimonId(),
                history.getHatchedAt(),
                history.getSource()
        );
    }
}