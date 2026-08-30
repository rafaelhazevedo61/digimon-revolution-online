package com.dro.modules.equipment.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.request.AscendEquipmentRequest;
import com.dro.modules.equipment.api.dto.response.AscendEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.AscendEquipmentPreviewResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AscendEquipmentUseCase {
    private final EquipmentRepository equipmentRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final InventoryRepository inventoryRepository;

    public AscendEquipmentUseCase(EquipmentRepository equipmentRepository, DigimonRepository digimonRepository,
                                  PlayerRepository playerRepository, InventoryRepository inventoryRepository) {
        this.equipmentRepository = equipmentRepository;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public AscendEquipmentResponse execute(String token, AscendEquipmentRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));
        Equipment equipment = equipmentRepository.findByIdForUpdate(request.equipmentId())
                .orElseThrow(() -> new NotFoundException("Equipment not found"));
        if (!playerId.equals(equipment.getPlayerId())) {
            throw new ForbiddenException("Equipment does not belong to this player");
        }
        if (equipment.isEquipped()) {
            throw new BadRequestException("Desequipe o equipamento antes de ascendê-lo");
        }
        if (equipment.getRefinementLevel() < EquipmentRules.ASCENSION_REFINEMENT_REQUIREMENT) {
            throw new UnprocessableException("O equipamento precisa estar no refinamento máximo +" + EquipmentRules.ASCENSION_REFINEMENT_REQUIREMENT);
        }
        if (equipment.getAscensionLevel() >= EquipmentRules.MAX_ASCENSION_LEVEL) {
            throw new BadRequestException("O equipamento já atingiu o limite de " + EquipmentRules.MAX_ASCENSION_LEVEL + " Ascensões");
        }
        int targetLevel = equipment.getAscensionLevel() + 1;
        EquipmentRules.validateAscensionTarget(targetLevel);
        int requiredRebirths = EquipmentRules.ascensionRebirthRequirement(targetLevel);
        if (digimon.getRebirthCount() < requiredRebirths) {
            throw new UnprocessableException("O Digimon precisa ter pelo menos " + requiredRebirths + " Renascimento(s) para esta Ascensão");
        }
        int bitsCost = EquipmentRules.ascensionBitsCost(targetLevel);
        if (digimon.getBits() < bitsCost) {
            throw new UnprocessableException("Bits insuficientes para realizar a Ascensão");
        }
        InventoryItem core = inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.ASCENSION_CORE)
                .orElseThrow(() -> new UnprocessableException("Núcleos de Ascensão insuficientes"));
        int coreCost = EquipmentRules.ascensionCoreCost(targetLevel);
        if (core.getQuantity() < coreCost) {
            throw new UnprocessableException("Núcleos de Ascensão insuficientes");
        }
        digimon.setBits(digimon.getBits() - bitsCost);
        core.setQuantity(core.getQuantity() - coreCost);
        equipment.setAscensionLevel(targetLevel);
        digimonRepository.save(digimon);
        equipmentRepository.save(equipment);
        if (core.getQuantity() == 0) inventoryRepository.delete(core);
        else inventoryRepository.save(core);
        return new AscendEquipmentResponse(
                "Ascensão " + targetLevel + " realizada com sucesso!",
                targetLevel,
                requiredRebirths,
                EquipmentResponse.from(equipment));
    }

    @Transactional(readOnly = true)
    public AscendEquipmentPreviewResponse preview(String token, UUID equipmentId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) throw new BadRequestException("No active digimon selected");
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new NotFoundException("Equipment not found"));
        if (!playerId.equals(equipment.getPlayerId())) throw new ForbiddenException("Equipment does not belong to this player");

        int currentLevel = equipment.getAscensionLevel();
        int nextLevel = currentLevel + 1;
        int requiredRebirths = nextLevel <= EquipmentRules.MAX_ASCENSION_LEVEL ? EquipmentRules.ascensionRebirthRequirement(nextLevel) : 0;
        int coreCost = nextLevel <= EquipmentRules.MAX_ASCENSION_LEVEL ? EquipmentRules.ascensionCoreCost(nextLevel) : 0;
        int bitsCost = nextLevel <= EquipmentRules.MAX_ASCENSION_LEVEL ? EquipmentRules.ascensionBitsCost(nextLevel) : 0;
        int currentCores = inventoryRepository.findByPlayerIdAndItemType(playerId, ItemType.ASCENSION_CORE)
                .map(InventoryItem::getQuantity).orElse(0);
        int beforeHp = equipment.getEffectiveBonusHp();
        int beforeAttack = equipment.getEffectiveBonusAttack();
        int beforeDefense = equipment.getEffectiveBonusDefense();
        int afterHp = ascendedBonus(equipment.getBonusHp(), equipment, nextLevel);
        int afterAttack = ascendedBonus(equipment.getBonusAttack(), equipment, nextLevel);
        int afterDefense = ascendedBonus(equipment.getBonusDefense(), equipment, nextLevel);
        String restriction = null;
        if (currentLevel >= EquipmentRules.MAX_ASCENSION_LEVEL) restriction = "Este equipamento já atingiu o limite de Ascensão";
        else if (equipment.isEquipped()) restriction = "Desequipe o equipamento antes de ascender";
        else if (equipment.getRefinementLevel() < EquipmentRules.ASCENSION_REFINEMENT_REQUIREMENT) restriction = "Refinamento insuficiente: exige +" + EquipmentRules.ASCENSION_REFINEMENT_REQUIREMENT;
        else if (digimon.getRebirthCount() < requiredRebirths) restriction = "O Digimon ativo precisa de pelo menos " + requiredRebirths + " Rebirth(s)";
        else if (digimon.getBits() < bitsCost) restriction = "Bits insuficientes";
        else if (currentCores < coreCost) restriction = "Núcleos de Ascensão insuficientes";
        return new AscendEquipmentPreviewResponse(EquipmentResponse.from(equipment), nextLevel,
                EquipmentRules.ASCENSION_REFINEMENT_REQUIREMENT, requiredRebirths, digimon.getRebirthCount(),
                coreCost, currentCores, bitsCost, digimon.getBits(), beforeHp, afterHp,
                beforeAttack, afterAttack, beforeDefense, afterDefense, restriction == null, restriction);
    }

    private int ascendedBonus(int base, Equipment equipment, int level) {
        if (base <= 0 || level > EquipmentRules.MAX_ASCENSION_LEVEL) return 0;
        int refined = (int) Math.round(base * equipment.getRarity().getStatMultiplier()) + equipment.getRefinementLevel() * 2;
        return (int) Math.round(refined * EquipmentRules.ascensionMultiplier(level));
    }
}
