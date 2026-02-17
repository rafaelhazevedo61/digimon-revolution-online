package com.dro.modules.incubation.api;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.NotNull;

public record StartIncubationRequest(
        @NotNull
        ItemType digitamaType,

        @NotNull
        ItemType incubatorType
) {}
