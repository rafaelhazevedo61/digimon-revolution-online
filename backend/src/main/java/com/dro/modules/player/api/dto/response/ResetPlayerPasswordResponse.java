package com.dro.modules.player.api.dto.response;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record ResetPlayerPasswordResponse(
        UUID playerId,
        String username,
        String newPassword,
        boolean generated
) {}
