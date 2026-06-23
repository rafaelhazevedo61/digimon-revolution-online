package com.dro.modules.boss.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChallengeBossRequest(
        @NotNull UUID digimonId
) {}
