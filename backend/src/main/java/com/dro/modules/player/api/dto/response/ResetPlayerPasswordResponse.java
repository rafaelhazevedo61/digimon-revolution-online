package com.dro.modules.player.api.dto.response;

import java.util.UUID;

public record ResetPlayerPasswordResponse(
        UUID playerId,
        String username,
        String newPassword,
        boolean generated
) {}
