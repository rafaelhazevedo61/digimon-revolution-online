package com.dro.modules.clan.api.dto.request;

import jakarta.validation.constraints.Size;

public record ClanUpdateRequest(
        @Size(max = 280) String description,
        @Size(max = 50) String emblem
) {
}
