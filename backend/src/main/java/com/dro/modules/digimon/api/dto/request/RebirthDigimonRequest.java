package com.dro.modules.digimon.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record RebirthDigimonRequest(

        @NotNull
        UUID digimonId

) {
}