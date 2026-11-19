package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.enums.Rarity;
import java.util.UUID;

public record RarityRerollResponse(UUID rerollId, Rarity currentRarity, Rarity newRarity, int keepCostBits, int availableBits, String message) {}
