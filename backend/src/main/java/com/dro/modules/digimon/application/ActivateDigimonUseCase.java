package com.dro.modules.digimon.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Torna um Digimon o único parceiro ativo do jogador.
 */
@Service
public class ActivateDigimonUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public Digimon execute(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon digimon = digimonRepository.findByIdForUpdate(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));
        activate(player, digimon, playerId, false);
        return digimon;
    }

    @Transactional
    public Digimon executeStored(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon digimon = digimonRepository.findByIdForUpdate(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));
        activate(player, digimon, playerId, true);
        return digimon;
    }

    private void activate(Player player, Digimon digimon, UUID playerId, boolean requireStored) {
        if (!playerId.equals(digimon.getPlayerId())) {
            throw new ForbiddenException("Digimon does not belong to player");
        }
        if (requireStored && digimon.getStatus() != DigimonStatus.STORED) {
            throw new BadRequestException("Digimon is not in storage");
        }
        if (!requireStored
                && digimon.getStatus() != DigimonStatus.ACTIVE
                && digimon.getStatus() != DigimonStatus.STORED
                && digimon.getStatus() != DigimonStatus.HATCHED) {
            throw new BadRequestException("Only stored or newly hatched Digimons can become active");
        }
        List<Digimon> activeDigimons = digimonRepository
                .findByPlayerIdAndStatusForUpdate(playerId, DigimonStatus.ACTIVE);
        if (digimon.getStatus() == DigimonStatus.ACTIVE
                && digimon.getId().equals(player.getActiveDigimonId())
                && activeDigimons.size() == 1) {
            return;
        }

        boolean movedPreviousActive = false;
        for (Digimon activeDigimon : activeDigimons) {
            if (!activeDigimon.getId().equals(digimon.getId())) {
                moveToStorage(player, activeDigimon, digimon);
                movedPreviousActive = true;
            }
        }

        // A constraint parcial permite apenas um ACTIVE por jogador. Persistimos
        // o parceiro anterior como STORED antes de promover o selecionado.
        if (movedPreviousActive) {
            digimonRepository.flush();
        }

        digimon.setStatus(DigimonStatus.ACTIVE);
        digimonRepository.save(digimon);
        player.setActiveDigimonId(digimon.getId());
        playerRepository.save(player);
    }

    private void moveToStorage(Player player, Digimon currentActive, Digimon selectedDigimon) {
        long storedCount = digimonRepository.countByPlayerIdAndStatus(player.getId(), DigimonStatus.STORED);
        long selectedStorageSlot = selectedDigimon.getStatus() == DigimonStatus.STORED ? 1 : 0;
        if (storedCount - selectedStorageSlot >= player.getMaxStorageSlots()) {
            throw new BadRequestException("Storage cheio (" + storedCount + "/" + player.getMaxStorageSlots() + ").");
        }
        unequipAll(currentActive);
        currentActive.setStatus(DigimonStatus.STORED);
        digimonRepository.save(currentActive);
    }

    private void unequipAll(Digimon digimon) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            UUID equipmentId = digimon.getEquipmentIdBySlot(slot);
            if (equipmentId == null) {
                continue;
            }
            Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
            if (equipment != null) {
                equipment.unequip();
                equipmentRepository.save(equipment);
            }
            digimon.clearSlot(slot);
        }
    }

    public ActivateDigimonUseCase(
            final PlayerRepository playerRepository,
            final DigimonRepository digimonRepository,
            final EquipmentRepository equipmentRepository
    ) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.equipmentRepository = equipmentRepository;
    }
}
