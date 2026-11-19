package com.dro.modules.boss.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record ChallengeBossRequest(
        @NotNull UUID digimonId
) {}
