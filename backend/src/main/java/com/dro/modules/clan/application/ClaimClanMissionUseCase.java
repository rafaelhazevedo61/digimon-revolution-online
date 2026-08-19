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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
@RequiredArgsConstructor
public class ClaimClanMissionUseCase {

    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final ClanMissionRepository clanMissionRepository;
    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanBonusService clanBonusService;
    private final ClanMissionResponseMapper mapper;

    @Transactional
    public PlayerClanMissionResponse execute(String token, UUID playerMissionId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getClanId() == null) {
            throw new BadRequestException("You must be in a clan to claim a clan mission");
        }

        PlayerClanMission active = playerClanMissionRepository
                .findByPlayerIdAndStatusIn(playerId, List.of(PlayerClanMissionStatus.IN_PROGRESS, PlayerClanMissionStatus.COMPLETED))
                .orElseThrow(() -> new BadRequestException("You have no active clan mission"));

        if (!active.getId().equals(playerMissionId)) {
            throw new BadRequestException("Mission ID does not match your active mission");
        }

        ClanMission mission = clanMissionRepository.findById(active.getClanMissionId())
                .orElseThrow(() -> new NotFoundException("Clan mission not found"));

        if (active.getProgress() < mission.getTargetValue()) {
            throw new BadRequestException("Mission not completed yet");
        }

        Clan clan = clanRepository.findById(player.getClanId())
                .orElseThrow(() -> new NotFoundException("Clan not found"));

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
}
