package com.dro.modules.player.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        String username,
        String email,
        LocalDateTime createdAt
) {}