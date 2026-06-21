package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.api.dto.response.RefineEquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefineEquipmentUseCase {

    private static final int STONES_PER_REFINEMENT = 1;

    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public RefineEquipmentResponse execute(String token, UUID equipmentId) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new NotFoundException("Equipment not found"));

        if (!equipment.getDigimonId().equals(digimon.getId())) {
            throw new ForbiddenException("Equipment does not belong to this Digimon");
        }

        if (equipment.getRefinementLevel() >= EquipmentRules.MAX_REFINEMENT_LEVEL) {
            throw new UnprocessableException("Equipment is already at maximum refinement level (+10)");
        }

        int costBits = EquipmentRules.refinementCostBits(equipment.getRefinementLevel());

        if (digimon.getBits() < costBits) {
            throw new UnprocessableException(
                    "Not enough Bits. Required: " + costBits + ", available: " + digimon.getBits());
        }

        InventoryItem stoneItem = inventoryRepository
                .findByDigimonIdAndItemType(digimon.getId(), ItemType.REFINEMENT_STONE)
                .orElseThrow(() -> new NotFoundException("No Refinement Stones in inventory"));

        if (stoneItem.getQuantity() < STONES_PER_REFINEMENT) {
            throw new UnprocessableException(
                    "Not enough Refinement Stones. Required: " + STONES_PER_REFINEMENT
                            + ", available: " + stoneItem.getQuantity());
        }

        digimon.setBits(digimon.getBits() - costBits);
        stoneItem.setQuantity(stoneItem.getQuantity() - STONES_PER_REFINEMENT);
        equipment.setRefinementLevel(equipment.getRefinementLevel() + 1);

        digimonRepository.save(digimon);
        inventoryRepository.save(stoneItem);
        equipmentRepository.save(equipment);

        return new RefineEquipmentResponse(
                "Equipment refined to +" + equipment.getRefinementLevel(),
                equipment.getRefinementLevel(),
                costBits,
                STONES_PER_REFINEMENT,
                EquipmentResponse.from(equipment)
        );
    }
}
