package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.IvRangeResponse;
import com.dro.modules.digimon.api.dto.response.RebirthPreviewResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.RebirthRules;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digimon.
 */
@Service
@RequiredArgsConstructor
public class RebirthPreviewUseCase {

    private static final int MAX_IV = 100;
    private static final int REQUIRED_LEVEL = 100;

    private final DigimonRepository digimonRepository;
    private final InventoryRepository inventoryRepository;
    private final MissionInstanceRepository missionInstanceRepository;

    public RebirthPreviewResponse execute(String token, UUID digimonId) {

        UUID playerId = extractPlayerId(token);

        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        validateOwner(digimon, playerId);

        int currentRebirthCount = digimon.getRebirthCount();
        int newRebirthCount = currentRebirthCount + 1;

        int costBits = RebirthRules.calculateBitsCost(currentRebirthCount);
        int costDataCore = RebirthRules.calculateDataCoreCost(currentRebirthCount);

        int currentBits = digimon.getBits();
        int remainingBitsAfterRebirth = Math.max(0, currentBits - costBits);

        int rarityMinimumIv = RebirthRules.calculateIvBonus(newRebirthCount);

        IvRangeResponse hpRange = calculateIvRange(
                digimon.getIvHp(),
                rarityMinimumIv,
                newRebirthCount
        );

        IvRangeResponse attackRange = calculateIvRange(
                digimon.getIvAttack(),
                rarityMinimumIv,
                newRebirthCount
        );

        IvRangeResponse defenseRange = calculateIvRange(
                digimon.getIvDefense(),
                rarityMinimumIv,
                newRebirthCount
        );

        double statMultiplier = RebirthRules.calculateStatMultiplier(newRebirthCount);

        String ineligibilityReason = getIneligibilityReason(
                digimon,
                costBits,
                costDataCore
        );

        boolean eligible = ineligibilityReason == null;

        return new RebirthPreviewResponse(
                eligible,
                ineligibilityReason,
                currentRebirthCount,
                newRebirthCount,
                costBits,
                costDataCore,
                currentBits,
                remainingBitsAfterRebirth,
                hpRange,
                attackRange,
                defenseRange,
                statMultiplier
        );
    }

    private UUID extractPlayerId(String token) {
        return TokenExtractor.extractPlayerId(token);
    }

    private void validateOwner(Digimon digimon, UUID playerId) {
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new ForbiddenException("This Digimon does not belong to the player");
        }
    }

    private IvRangeResponse calculateIvRange(
            int previousIv,
            int rarityMinimumIv,
            int rebirthCount
    ) {
        int min = RebirthRules.calculateInheritedIvMinimum(
                previousIv,
                rarityMinimumIv,
                rebirthCount
        );

        return new IvRangeResponse(min, MAX_IV);
    }

    private String getIneligibilityReason(
            Digimon digimon,
            int costBits,
            int costDataCore
    ) {

        if (digimon.getStatus().name().equals("REBORN")) {
            return "Only active Digimons can perform Rebirth";
        }

        if (digimon.getLevel() < REQUIRED_LEVEL) {
            return "Digimon must be level 100 to perform Rebirth";
        }

        if (!RebirthRules.isEligibleStage(digimon.getStage())) {
            return "Digimon must be Champion or higher to perform Rebirth";
        }

        boolean hasRunningMission = missionInstanceRepository.existsByDigimonIdAndStatus(
                digimon.getId(),
                MissionStatus.RUNNING
        );

        if (hasRunningMission) {
            return "Digimon cannot perform Rebirth while in a running mission";
        }

        if (digimon.getBits() < costBits) {
            return "Not enough Bits to perform Rebirth";
        }

        int dataCoreQuantity = inventoryRepository
                .findByDigimonIdAndItemType(digimon.getId(), ItemType.DATA_CORE)
                .map(InventoryItem::getQuantity)
                .orElse(0);

        if (dataCoreQuantity < costDataCore) {
            return "Not enough Data Core to perform Rebirth";
        }

        return null;
    }
}