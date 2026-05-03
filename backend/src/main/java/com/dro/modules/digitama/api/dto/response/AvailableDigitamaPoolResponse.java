package com.dro.modules.digitama.api.dto.response;

import java.util.List;

public record AvailableDigitamaPoolResponse(
        String code,
        String name,
        String description,
        List<AvailableDigitamaEntryResponse> entries
) {
}