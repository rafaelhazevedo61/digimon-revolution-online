package com.dro.modules.digitama.api.dto.request;

import com.dro.modules.digitama.domain.DigitamaType;
import jakarta.validation.constraints.NotNull;

public record SelectDigitamaRequest(
        @NotNull
        DigitamaType type
) {}
