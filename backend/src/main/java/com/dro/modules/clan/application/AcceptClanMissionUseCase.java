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
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AcceptClanMissionUseCase {

    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final ClanMissionRepository clanMissionRepository;
    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanMissionResponseMapper mapper;

    @Transactional
    public PlayerClanMissionResponse execute(String token, UUID missionId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getClanId() == null) {
            throw new BadRequestException("You must be in a clan to accept a clan mission");
        }

        boolean hasActive = playerClanMissionRepository
                .findByPlayerIdAndStatusIn(playerId, List.of(PlayerClanMissionStatus.IN_PROGRESS, PlayerClanMissionStatus.COMPLETED))
                .isPresent();

        if (hasActive) {
            throw new ConflictException("You already have an active clan mission");
        }

        Clan clan = clanRepository.findById(player.getClanId())
                .orElseThrow(() -> new NotFoundException("Clan not found"));

        ClanMission mission = clanMissionRepository.findById(missionId)
                .orElseThrow(() -> new NotFoundException("Clan mission not found"));

        if (mission.getMinClanLevel() > clan.getLevel()) {
            throw new BadRequestException("Clan level too low for this mission");
        }

        int reward = ThreadLocalRandom.current().nextInt(
                mission.getMinHonorMarksReward(),
                mission.getMaxHonorMarksReward() + 1
        );

        PlayerClanMission playerMission = PlayerClanMission.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .clanMissionId(mission.getId())
                .clanId(clan.getId())
                .progress(0)
                .honorMarksReward(reward)
                .status(PlayerClanMissionStatus.IN_PROGRESS)
                .acceptedAt(LocalDateTime.now())
                .build();

        playerClanMissionRepository.save(playerMission);
        return mapper.toPlayerMission(playerMission, mission);
    }
}
