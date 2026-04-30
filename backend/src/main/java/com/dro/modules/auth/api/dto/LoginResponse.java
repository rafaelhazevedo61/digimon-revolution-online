package com.dro.modules.auth.api.dto;

import java.util.UUID;

public record LoginResponse(
        UUID playerId,
        String token
) {}
