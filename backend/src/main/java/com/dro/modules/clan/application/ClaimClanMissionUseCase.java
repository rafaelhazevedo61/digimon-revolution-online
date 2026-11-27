package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.PlayerClanMissionResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.ClanMissionRepository;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class ClaimClanMissionUseCase {
    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final ClanMissionRepository clanMissionRepository;
    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanBonusService clanBonusService;
    private final ClanMissionResponseMapper mapper;
    private final AddItemUseCase addItemUseCase;
    private final ItemDefinitionRepository itemDefinitionRepository;

    @Transactional
    public PlayerClanMissionResponse execute(String token, UUID playerMissionId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getClanId() == null) {
            throw new BadRequestException("You must be in a clan to claim a clan mission");
        }
        // Serializa o resgate por conta e por missão, inclusive em múltiplas instâncias.
        PlayerClanMission active = playerClanMissionRepository.findOwnedByIdForUpdate(playerMissionId, playerId)
                .orElseThrow(() -> new NotFoundException("Clan mission not found"));
        if (!player.getClanId().equals(active.getClanId())) {
            throw new BadRequestException("This mission belongs to another clan");
        }
        if (active.getStatus() != PlayerClanMissionStatus.IN_PROGRESS && active.getStatus() != PlayerClanMissionStatus.COMPLETED) {
            throw new BadRequestException("This clan mission cannot be claimed");
        }
        ClanMission mission = clanMissionRepository.findById(active.getClanMissionId()).orElseThrow(() -> new NotFoundException("Clan mission not found"));
        if (active.getProgress() < mission.getTargetValue()) {
            throw new BadRequestException("Mission not completed yet");
        }
        Clan clan = clanRepository.findByIdForUpdate(player.getClanId()).orElseThrow(() -> new NotFoundException("Clan not found"));
        var chest = itemDefinitionRepository.findByCode(ClanMissionResponseMapper.CONTRIBUTION_CHEST_CODE)
                .orElseThrow(() -> new NotFoundException("Contribution chest is not configured"));
        // Se o estoque estiver cheio, toda a transação falha e o resgate permanece pendente.
        addItemUseCase.addMaterialToPlayer(playerId, chest, 1);
        double honorMarksMultiplier = 1.0 + clanBonusService.getHonorMarksBonusPercent(clan.getId());
        int finalHonorMarks = (int) Math.floor(active.getHonorMarksReward() * honorMarksMultiplier);
        clan.setHonorMarks(clan.getHonorMarks() + finalHonorMarks);
        com.dro.modules.clan.domain.ClanRules.addExperience(clan, mission.getClanXpReward());
        active.setStatus(PlayerClanMissionStatus.CLAIMED);
        active.setCompletedAt(LocalDateTime.now());
        clanRepository.save(clan);
        playerClanMissionRepository.save(active);
        return mapper.toPlayerMission(active, mission);
    }

    public ClaimClanMissionUseCase(final PlayerRepository playerRepository, final ClanRepository clanRepository, final ClanMissionRepository clanMissionRepository, final PlayerClanMissionRepository playerClanMissionRepository, final ClanBonusService clanBonusService, final ClanMissionResponseMapper mapper, final AddItemUseCase addItemUseCase, final ItemDefinitionRepository itemDefinitionRepository) {
        this.playerRepository = playerRepository;
        this.clanRepository = clanRepository;
        this.clanMissionRepository = clanMissionRepository;
        this.playerClanMissionRepository = playerClanMissionRepository;
        this.clanBonusService = clanBonusService;
        this.mapper = mapper;
        this.addItemUseCase = addItemUseCase;
        this.itemDefinitionRepository = itemDefinitionRepository;
    }
}
