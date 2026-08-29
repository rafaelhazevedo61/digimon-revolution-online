package com.dro.modules.clan.storage.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClanStorageHistoryResponse(
        UUID id,
        String action,
        UUID actorPlayerId,
        String actorUsername,
        Long itemDefinitionId,
        String itemCode,
        String itemName,
        int quantity,
        LocalDateTime createdAt
) {
}
