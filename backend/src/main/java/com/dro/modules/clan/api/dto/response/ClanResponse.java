package com.dro.modules.clan.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Contrato de dados do módulo de Clãs.
 */
public record ClanResponse(
        UUID id,
        String name,
        String tag,
        String description,
        UUID leaderId,
        String leaderUsername,
        String emblem,
        int baseMaxMembers,
        int maxMembers,
        int memberCapacityUpgradeLevel,
        int memberCount,
        int level,
        int experience,
        int xpToNextLevel,
        int honorMarks,
        LocalDateTime createdAt,
        List<ClanMemberResponse> members,
        boolean isMember,
        ClanRoleResponse myRole,
        List<ClanUpgradeResponse> activeUpgrades
) {
}
