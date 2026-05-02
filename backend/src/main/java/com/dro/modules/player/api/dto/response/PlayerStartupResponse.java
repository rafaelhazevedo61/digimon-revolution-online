package com.dro.modules.player.api.dto.response;

import com.dro.modules.player.domain.enums.StartupDestination;

public record PlayerStartupResponse(
        boolean hasSelectedStarter,
        StartupDestination redirectTo
) {
}