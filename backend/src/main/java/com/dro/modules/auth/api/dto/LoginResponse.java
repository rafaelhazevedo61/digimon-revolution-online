package com.dro.modules.auth.api.dto;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Autenticação.
 */
public record LoginResponse(
        UUID playerId,
        String token
) {}
