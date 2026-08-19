package com.dro.modules.digitama.api.dto.request;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Digitama.
 */
public record SelectDigitamaRequest(
        @NotNull
        DigitamaType type
) {}
