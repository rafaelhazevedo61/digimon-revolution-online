package com.dro.modules.digimon.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BulkSacrificeDigimonRequest(
        @NotEmpty(message = "Selecione pelo menos um Digimon")
        @Size(max = 500, message = "É possível sacrificar no máximo 500 Digimons por operação")
        List<@NotNull UUID> digimonIds
) {}
