package com.dro.modules.arena.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Arena.
 */
public record ChallengeArenaRequest(
        @NotNull UUID opponentDigimonId
) {}
