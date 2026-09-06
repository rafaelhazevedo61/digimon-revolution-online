package com.dro.modules.clan.raid.api.dto.response;

/**
 * Representa um Baú concedido por uma incursão de clã.
 */
public record ClanRaidRewardResponse(
        String rewardType,
        String chestCode,
        String chestName
) {
}
