package com.dro.modules.player.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Jogadores.
 */
public record PlayerResponse(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt
) {}