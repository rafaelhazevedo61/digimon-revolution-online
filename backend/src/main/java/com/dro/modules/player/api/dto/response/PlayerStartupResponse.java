package com.dro.modules.player.api.dto.response;

import com.dro.modules.player.domain.enums.StartupDestination;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record PlayerStartupResponse(
        boolean hasSelectedStarter,
        StartupDestination redirectTo
) {
}