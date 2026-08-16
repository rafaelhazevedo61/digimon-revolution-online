package com.dro.modules.player.api.dto.request;

public record ResetPlayerPasswordRequest(
        String newPassword,
        boolean generateRandom
) {}
