package com.dro.modules.digimon.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SelectDigimonRequest(
        @NotNull
        UUID digimonId
) {}
