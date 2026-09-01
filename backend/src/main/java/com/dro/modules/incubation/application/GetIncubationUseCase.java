package com.dro.modules.incubation.application;

import com.dro.modules.incubation.api.dto.response.IncubationResponse;
import com.dro.modules.incubation.api.dto.response.IncubationSlotResponse;
import com.dro.modules.incubation.api.dto.response.IncubationSlotsResponse;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.domain.IncubatorRules;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Incubação.
 */
@Service
public class GetIncubationUseCase {
    private final IncubationRepository incubationRepository;
    private final PlayerRepository playerRepository;

    public IncubationSlotsResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        int unlockedSlots = Math.max(
                1,
                Math.min(IncubatorRules.TOTAL_SLOTS, player.getUnlockedIncubationSlots())
        );
        LocalDateTime now = LocalDateTime.now();
        Map<Integer, Incubation> activeBySlot = new HashMap<>();

        for (Incubation incubation : incubationRepository
                .findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED)) {
            if (incubation.getStatus() == IncubationStatus.IN_PROGRESS
                    && !incubation.getFinishAt().isAfter(now)) {
                incubation.markReadyIfFinished();
                incubationRepository.save(incubation);
            }
            activeBySlot.put(incubation.getSlotNumber(), incubation);
        }

        List<IncubationSlotResponse> slots = java.util.stream.IntStream
                .rangeClosed(1, IncubatorRules.TOTAL_SLOTS)
                .mapToObj(slotNumber -> new IncubationSlotResponse(
                        slotNumber,
                        slotNumber <= unlockedSlots,
                        toResponse(activeBySlot.get(slotNumber), now)
                ))
                .toList();

        return new IncubationSlotsResponse(
                IncubatorRules.TOTAL_SLOTS,
                unlockedSlots,
                slots
        );
    }

    private IncubationResponse toResponse(Incubation incubation, LocalDateTime now) {
        if (incubation == null) {
            return null;
        }
        long remaining = Math.max(0, Duration.between(now, incubation.getFinishAt()).getSeconds());
        return new IncubationResponse(
                incubation.getId(),
                incubation.getSlotNumber(),
                incubation.getDigitamaType(),
                incubation.getIncubatorType(),
                incubation.getStatus(),
                incubation.getStartedAt(),
                incubation.getFinishAt(),
                remaining,
                incubation.isAutoRepeatEnabled(),
                incubation.isAutoClaimEnabled(),
                incubation.getAutomationPauseReason(),
                incubation.getAutomationPausedAt(),
                incubation.getAutomationLastErrorCode()
        );
    }

    public GetIncubationUseCase(
            final IncubationRepository incubationRepository,
            final PlayerRepository playerRepository
    ) {
        this.incubationRepository = incubationRepository;
        this.playerRepository = playerRepository;
    }
}
