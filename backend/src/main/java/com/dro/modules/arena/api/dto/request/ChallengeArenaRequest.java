package com.dro.modules.arena.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChallengeArenaRequest(
        @NotNull UUID opponentDigimonId
) {}
