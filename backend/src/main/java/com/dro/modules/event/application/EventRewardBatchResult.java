package com.dro.modules.event.application;

import java.util.List;
import java.util.UUID;

public record EventRewardBatchResult(
        int createdCount,
        int skippedCount,
        int requestedCount,
        List<UUID> rewardIds
) {
}
