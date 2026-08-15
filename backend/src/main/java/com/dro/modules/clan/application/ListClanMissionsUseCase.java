package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanMissionResponse;
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

import java.util.*;

@Service
@RequiredArgsConstructor
public class ListClanMissionsUseCase {

    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final ClanMissionRepository clanMissionRepository;
    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanMissionResponseMapper mapper;

    public List<ClanMissionResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getClanId() == null) {
            return List.of();
        }

        Clan clan = clanRepository.findById(player.getClanId())
                .orElseThrow(() -> new NotFoundException("Clan not found"));

        List<ClanMission> missions = clanMissionRepository.findAll();
        Optional<PlayerClanMission> active = playerClanMissionRepository
                .findByPlayerIdAndStatus(playerId, PlayerClanMissionStatus.IN_PROGRESS);
        Set<UUID> acceptedIds = active.map(a -> Set.of(a.getClanMissionId())).orElse(Set.of());

        return missions.stream()
                .filter(m -> m.getMinClanLevel() <= clan.getLevel())
                .sorted(Comparator.comparingInt(ClanMission::getMinClanLevel)
                        .thenComparing(ClanMission::getCode))
                .map(m -> mapper.toCatalog(m, acceptedIds.contains(m.getId())))
                .toList();
    }
}
