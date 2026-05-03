package com.dro.modules.digitama.api.dto.response;


import com.dro.modules.digitama.domain.DigitamaType;
import com.dro.modules.digitama.domain.enums.HatchSource;

import java.time.LocalDateTime;
import java.util.UUID;

public record DigitamaHistoryResponse(
        UUID id,
        DigitamaType digitamaType,
        String speciesName,
        UUID digimonId,
        LocalDateTime hatchedAt,
        HatchSource source
) {}
