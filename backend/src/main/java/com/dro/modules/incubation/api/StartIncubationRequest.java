package com.dro.modules.incubation.api;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.NotNull;

/**
 * Contrato de dados do módulo de Incubação.
 */
public record StartIncubationRequest(
        @NotNull
        ItemType digitamaType,

        @NotNull
        ItemType incubatorType
) {}
