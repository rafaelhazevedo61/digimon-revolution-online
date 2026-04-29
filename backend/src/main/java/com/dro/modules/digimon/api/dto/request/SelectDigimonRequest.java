package com.dro.modules.digimon.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SelectDigimonRequest(
        @NotNull
        UUID digimonId
) {}
