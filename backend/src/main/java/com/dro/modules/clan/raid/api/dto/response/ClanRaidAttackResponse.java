package com.dro.modules.clan.raid.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ClanRaidAttackResponse(
        UUID id,
        UUID playerId,
        String username,
        int damage,
        Instant createdAt
) {
}
