package com.dro.modules.digimon.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record BulkSacrificeDigimonRequest(
        @NotEmpty(message = "Selecione pelo menos um Digimon")
        @Size(max = 100, message = "É possível sacrificar no máximo 100 Digimons por operação")
        List<@NotNull UUID> digimonIds
) {}
