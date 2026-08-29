package com.dro.modules.digimon.application;

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

import java.util.UUID;

/** Converte um Digimon armazenado em Dados Digitais de forma atômica. */
@Service
public class SacrificeDigimonUseCase {
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public int execute(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon digimon = digimonRepository.findByIdForUpdate(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));

        if (!playerId.equals(digimon.getPlayerId())) {
            throw new BadRequestException("Digimon does not belong to player");
        }
        if (digimon.getStatus() != DigimonStatus.STORED && digimon.getStatus() != DigimonStatus.HATCHED) {
            throw new BadRequestException("Only stored or newly hatched Digimons can be sacrificed");
        }
        if (digimon.getId().equals(player.getActiveDigimonId())) {
            throw new BadRequestException("The active Digimon cannot be sacrificed");
        }
        if (missionInstanceRepository.existsByDigimonIdAndStatus(digimonId, MissionStatus.RUNNING)) {
            throw new BadRequestException("Digimon cannot be sacrificed while in a running mission");
        }

        int reward = DigitalDataRules.calculate(
                digimon.getStage(), digimon.getLevel(),
                digimon.getIvHp(), digimon.getIvAttack(), digimon.getIvDefense());
        player.addDigitalData(reward);
        playerRepository.save(player);
        digimon.setStatus(DigimonStatus.SACRIFICED);
        digimonRepository.save(digimon);
        return reward;
    }

    public SacrificeDigimonUseCase(DigimonRepository digimonRepository, PlayerRepository playerRepository, MissionInstanceRepository missionInstanceRepository, InventoryRepository inventoryRepository) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.missionInstanceRepository = missionInstanceRepository;
        this.inventoryRepository = inventoryRepository;
    }
}
