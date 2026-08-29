package com.dro.modules.player.api.dto.response;

import com.dro.modules.player.domain.Player;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record AdminPlayerResponse(
        UUID id,
        UUID clanId,
        String username,
        String email,
        LocalDateTime createdAt,
        String selectedDigitama,
        UUID activeDigimonId,
        LocalDateTime lastMissionAt,
        boolean starterSelected
) {
    public static AdminPlayerResponse from(Player player) {
        return new AdminPlayerResponse(
                player.getId(),
                player.getClanId(),
                player.getUsername(),
                player.getEmail(),
                player.getCreatedAt(),
                player.getSelectedDigitama() != null ? player.getSelectedDigitama().name() : null,
                player.getActiveDigimonId(),
                player.getLastMissionAt(),
                player.hasSelectedStarter()
        );
    }
}