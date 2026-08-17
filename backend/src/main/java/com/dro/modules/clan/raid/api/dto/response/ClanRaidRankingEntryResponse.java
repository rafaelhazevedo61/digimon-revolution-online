package com.dro.modules.clan.raid.api.dto.response;

import java.util.UUID;

public record ClanRaidRankingEntryResponse(
        int position,
        UUID playerId,
        String username,
        long totalDamage
) {
}
