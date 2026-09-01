package com.dro.modules.mission.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SaveMissionTeamRequest(
        @NotBlank
        @Size(max = 40)
        String name,
        @NotNull
        @Size(min = 3, max = 3)
        List<UUID> digimonIds,
        @NotNull
        UUID captainDigimonId
) {
}
