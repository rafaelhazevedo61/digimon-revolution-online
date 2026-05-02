package com.dro.modules.digimon.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RenameDigimonRequest(
        @NotNull UUID digimonId,
        @NotBlank @Size(min = 1, max = 20) String newName
) {}
