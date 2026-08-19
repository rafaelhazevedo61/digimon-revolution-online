package com.dro.modules.player.api.dto.request;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record ResetPlayerPasswordRequest(
        String newPassword,
        boolean generateRandom
) {}
