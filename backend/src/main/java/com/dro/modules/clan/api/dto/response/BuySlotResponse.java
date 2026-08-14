package com.dro.modules.clan.api.dto.response;

public record BuySlotResponse(
        int boughtSlots,
        int maxMembers,
        int remainingBits,
        int nextSlotCost
) {
}
