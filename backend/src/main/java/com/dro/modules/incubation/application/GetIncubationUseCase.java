package com.dro.modules.incubation.application;

import com.dro.modules.incubation.api.IncubationResponse;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Incubação.
 */
@Service
@RequiredArgsConstructor
public class GetIncubationUseCase {

    private final IncubationRepository incubationRepository;

    public IncubationResponse execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Incubation incubation = incubationRepository
                .findByPlayerIdAndStatus(playerId, IncubationStatus.IN_PROGRESS)
                .orElse(null);

        if (incubation == null) {
            return null;
        }

        long remaining = Duration.between(
                LocalDateTime.now(),
                incubation.getFinishAt()
        ).getSeconds();

        if (remaining < 0) {
            remaining = 0;
        }

        return new IncubationResponse(
                incubation.getDigitamaType(),
                incubation.getIncubatorType(),
                incubation.getStatus(),
                incubation.getStartedAt(),
                incubation.getFinishAt(),
                remaining
        );
    }
}
