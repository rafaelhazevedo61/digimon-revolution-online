package com.dro.modules.clan.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClanSummaryResponse(
        UUID id,
        String name,
        String tag,
        String description,
        int memberCount,
        int maxMembers,
        int level,
        LocalDateTime createdAt
) {
}
