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
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Equipamentos.
 */
@Service
public class RefineEquipmentUseCase {
    private static final int STONES_PER_REFINEMENT = 1;
    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionAuditPublisher transactionAuditPublisher;

    @Transactional
    public RefineEquipmentResponse execute(String token, UUID equipmentId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        var player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        Equipment equipment = equipmentRepository.findById(equipmentId).orElseThrow(() -> new NotFoundException("Equipment not found"));
        if (!equipment.getDigimonId().equals(digimon.getId())) {
            throw new ForbiddenException("Equipment does not belong to this Digimon");
        }
        if (equipment.getRefinementLevel() >= EquipmentRules.MAX_REFINEMENT_LEVEL) {
            throw new UnprocessableException("Equipment is already at maximum refinement level (+10)");
        }
        int currentLevel = equipment.getRefinementLevel();
        int costBits = EquipmentRules.refinementCostBits(currentLevel);
        int successRate = EquipmentRules.refinementSuccessRate(currentLevel);
        if (digimon.getBits() < costBits) {
            throw new UnprocessableException("Not enough Bits. Required: " + costBits + ", available: " + digimon.getBits());
        }
        InventoryItem stoneItem = inventoryRepository.findByDigimonIdAndItemType(digimon.getId(), ItemType.REFINEMENT_STONE).orElseThrow(() -> new NotFoundException("No Refinement Stones in inventory"));
        if (stoneItem.getQuantity() < STONES_PER_REFINEMENT) {
            throw new UnprocessableException("Not enough Refinement Stones. Required: " + STONES_PER_REFINEMENT + ", available: " + stoneItem.getQuantity());
        }
        digimon.setBits(digimon.getBits() - costBits);
        stoneItem.setQuantity(stoneItem.getQuantity() - STONES_PER_REFINEMENT);
        int roll = ThreadLocalRandom.current().nextInt(1, 101);
        boolean success = roll <= successRate;
        if (success) {
            equipment.setRefinementLevel(currentLevel + 1);
        }
        digimonRepository.save(digimon);
        inventoryRepository.save(stoneItem);
        equipmentRepository.save(equipment);
        transactionAuditPublisher.success("equipment-refine:" + UUID.randomUUID(), "EQUIPMENT_REFINED", "Equipment", equipmentId.toString(), Map.ofEntries(Map.entry("module", "equipment"), Map.entry("operation", "refine"), Map.entry("actorId", playerId.toString()), Map.entry("digimonId", digimon.getId().toString()), Map.entry("equipmentId", equipmentId.toString()), Map.entry("previousLevel", currentLevel), Map.entry("newLevel", equipment.getRefinementLevel()), Map.entry("success", success), Map.entry("successRate", successRate), Map.entry("costBits", costBits), Map.entry("refinementStones", STONES_PER_REFINEMENT), Map.entry("summary", "Equipment refinement processed")));
        String message = success ? "Refinamento bem-sucedido! +" + equipment.getRefinementLevel() : "Refinamento falhou! O equipamento permanece em +" + currentLevel;
        return new RefineEquipmentResponse(message, success, equipment.getRefinementLevel(), successRate, costBits, STONES_PER_REFINEMENT, EquipmentResponse.from(equipment));
    }

    public RefineEquipmentUseCase(final EquipmentRepository equipmentRepository, final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final InventoryRepository inventoryRepository, final TransactionAuditPublisher transactionAuditPublisher) {
        this.equipmentRepository = equipmentRepository;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionAuditPublisher = transactionAuditPublisher;
    }
}
