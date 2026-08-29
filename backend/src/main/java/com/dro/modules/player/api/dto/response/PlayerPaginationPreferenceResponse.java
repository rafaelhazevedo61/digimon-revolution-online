package com.dro.modules.player.api.dto.response;

import com.dro.modules.player.domain.PlayerDisplayPreference;

public record PlayerPaginationPreferenceResponse(boolean paginationEnabled) {
    public static PlayerPaginationPreferenceResponse from(PlayerDisplayPreference preference) {
        return new PlayerPaginationPreferenceResponse(preference.isPaginationEnabled());
    }
}
