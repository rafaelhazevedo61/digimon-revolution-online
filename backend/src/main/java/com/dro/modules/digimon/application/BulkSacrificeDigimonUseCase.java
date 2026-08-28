package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.BulkSacrificeDigimonResponse;
import com.dro.modules.digimon.domain.DigitalDataRules;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Sacrifica vários Digimons armazenados de forma atômica. */
@Service
public class BulkSacrificeDigimonUseCase {
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public BulkSacrificeDigimonResponse execute(String token, List<UUID> digimonIds) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        validateIds(digimonIds);

        List<Digimon> digimons = digimonRepository.findAllByIdForUpdate(playerId, digimonIds);
        if (digimons.size() != digimonIds.size()) {
            throw new NotFoundException("One or more Digimons not found");
        }

        Map<UUID, Digimon> byId = new HashMap<>();
        digimons.forEach(digimon -> byId.put(digimon.getId(), digimon));
        int totalReward = 0;
        List<BulkSacrificeDigimonResponse.SacrificedDigimonResponse> sacrificed = new ArrayList<>();

        for (UUID digimonId : digimonIds) {
            Digimon digimon = byId.get(digimonId);
            validateSacrifice(playerId, player, digimon);
            int reward = DigitalDataRules.calculate(
                    digimon.getStage(), digimon.getLevel(),
                    digimon.getIvHp(), digimon.getIvAttack(), digimon.getIvDefense());
            totalReward += reward;
            sacrificed.add(new BulkSacrificeDigimonResponse.SacrificedDigimonResponse(
                    digimon.getId(), digimon.getName(), reward));
        }

        for (Digimon digimon : digimons) {
            inventoryRepository.deleteByDigimonId(digimon.getId());
            digimon.setStatus(DigimonStatus.SACRIFICED);
            digimonRepository.save(digimon);
        }
        player.addDigitalData(totalReward);
        playerRepository.save(player);

        return new BulkSacrificeDigimonResponse(sacrificed.size(), totalReward, sacrificed);
    }

    private void validateIds(List<UUID> digimonIds) {
        if (digimonIds == null || digimonIds.isEmpty()) {
            throw new BadRequestException("Selecione pelo menos um Digimon");
        }
        if (digimonIds.size() > 100) {
            throw new BadRequestException("É possível sacrificar no máximo 100 Digimons por operação");
        }
        if (new HashSet<>(digimonIds).size() != digimonIds.size()) {
            throw new BadRequestException("Não é possível selecionar o mesmo Digimon mais de uma vez");
        }
    }

    private void validateSacrifice(UUID playerId, Player player, Digimon digimon) {
        if (digimon == null) {
            throw new NotFoundException("Digimon not found");
        }
        if (!playerId.equals(digimon.getPlayerId())) {
            throw new BadRequestException("Digimon does not belong to player");
        }
        if (digimon.getStatus() != DigimonStatus.STORED) {
            throw new BadRequestException("Only stored Digimons can be sacrificed");
        }
        if (digimon.getId().equals(player.getActiveDigimonId())) {
            throw new BadRequestException("The active Digimon cannot be sacrificed");
        }
        if (missionInstanceRepository.existsByDigimonIdAndStatus(digimon.getId(), MissionStatus.RUNNING)) {
            throw new BadRequestException("Digimon cannot be sacrificed while in a running mission");
        }
    }

    public BulkSacrificeDigimonUseCase(
            DigimonRepository digimonRepository,
            PlayerRepository playerRepository,
            MissionInstanceRepository missionInstanceRepository,
            InventoryRepository inventoryRepository
    ) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.missionInstanceRepository = missionInstanceRepository;
        this.inventoryRepository = inventoryRepository;
    }
}
