package com.dro.modules.player.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdatePlayerPaginationPreferenceRequest(@NotNull Boolean paginationEnabled) {
}
