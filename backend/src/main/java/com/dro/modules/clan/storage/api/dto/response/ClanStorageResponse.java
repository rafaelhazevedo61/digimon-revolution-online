package com.dro.modules.clan.storage.api.dto.response;

import java.util.List;
import java.util.UUID;

public record ClanStorageResponse(
        UUID clanId,
        int capacity,
        int usedSlots,
        int availableSlots,
        int honorMarks,
        int capacityUpgradeLevel,
        int maxCapacityUpgradeLevel,
        int nextUpgradeCostHonorMarks,
        List<ClanStorageItemResponse> items,
        List<ClanStorageHistoryResponse> history
) {
}
