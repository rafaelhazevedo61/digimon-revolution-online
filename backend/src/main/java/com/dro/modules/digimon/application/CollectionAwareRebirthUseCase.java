package com.dro.modules.digimon.application;

import com.dro.modules.clan.application.ClanMissionProgressTracker;
import com.dro.modules.collection.application.CollectionRegistrationService;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Especialização do Rebirth que mantém o fluxo atual e adiciona o registro
 * automático da nova forma Baby na coleção do jogador.
 */
@Service
@Primary
public class CollectionAwareRebirthUseCase extends RebirthUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final CollectionRegistrationService collectionRegistrationService;

    public CollectionAwareRebirthUseCase(
            DigimonRepository digimonRepository,
            PlayerRepository playerRepository,
            InventoryRepository inventoryRepository,
            MissionInstanceRepository missionInstanceRepository,
            DigimonInfosRepository digimonInfosRepository,
            EvolutionLineRepository evolutionLineRepository,
            EquipmentRepository equipmentRepository,
            ClanMissionProgressTracker clanMissionProgressTracker,
            MissionTeamRepository missionTeamRepository,
            CollectionRegistrationService collectionRegistrationService
    ) {
        super(
                digimonRepository,
                playerRepository,
                inventoryRepository,
                missionInstanceRepository,
                digimonInfosRepository,
                evolutionLineRepository,
                equipmentRepository,
                clanMissionProgressTracker,
                missionTeamRepository,
                collectionRegistrationService
        );
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.collectionRegistrationService = collectionRegistrationService;
    }

    @Override
    @Transactional
    public void execute(
            String token,
            UUID digimonId,
            int codeInfiniteHp,
            int codeInfiniteAttack,
            int codeInfiniteDefense,
            boolean preserveRarity
    ) {
        super.execute(
                token,
                digimonId,
                codeInfiniteHp,
                codeInfiniteAttack,
                codeInfiniteDefense,
                preserveRarity
        );

        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            return;
        }

        Digimon rebornDigimon = digimonRepository.findById(player.getActiveDigimonId()).orElse(null);
        collectionRegistrationService.registerIfMissing(rebornDigimon, "REBIRTH");
    }
}
