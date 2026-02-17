package com.dro.modules.auth.api;

import java.util.UUID;

public record LoginResponse(
        UUID playerId,
        String token
) {}
