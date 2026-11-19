package com.dro.modules.invite.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlphaInviteResponse(
        UUID id,
        String codeHint,
        String testerName,
        String testerEmail,
        String status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime usedAt,
        UUID usedByPlayerId,
        UUID createdByAdminId,
        LocalDateTime deletedAt,
        UUID deletedByAdminId
) {
}
