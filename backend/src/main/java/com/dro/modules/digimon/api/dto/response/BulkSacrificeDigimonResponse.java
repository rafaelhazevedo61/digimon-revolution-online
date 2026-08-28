package com.dro.modules.digimon.api.dto.response;

import java.util.List;
import java.util.UUID;

public record BulkSacrificeDigimonResponse(
        int sacrificedCount,
        int digitalDataReceived,
        List<SacrificedDigimonResponse> digimons
) {
    public record SacrificedDigimonResponse(
            UUID digimonId,
            String name,
            int digitalDataReceived
    ) {}
}
