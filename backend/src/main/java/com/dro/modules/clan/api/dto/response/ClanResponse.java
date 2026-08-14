package com.dro.modules.clan.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ClanResponse(
        UUID id,
        String name,
        String tag,
        String description,
        UUID leaderId,
        String leaderUsername,
        String emblem,
        int maxMembers,
        int memberCount,
        int level,
        int experience,
        int xpToNextLevel,
        LocalDateTime createdAt,
        List<ClanMemberResponse> members,
        boolean isMember,
        ClanRoleResponse myRole
) {
}
