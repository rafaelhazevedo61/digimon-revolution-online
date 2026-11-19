package com.dro.modules.invite.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAlphaInviteResponse(
        UUID id,
        String inviteCode,
        String testerName,
        String testerEmail,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
}
