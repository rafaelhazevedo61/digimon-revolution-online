package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanMissionResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.infra.ClanMissionRepository;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class ListClanMissionsUseCase {
    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final ClanMissionRepository clanMissionRepository;
    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanMissionResponseMapper mapper;

    public List<ClanMissionResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getClanId() == null) {
            return List.of();
        }
        Clan clan = clanRepository.findById(player.getClanId()).orElseThrow(() -> new NotFoundException("Clan not found"));
        List<ClanMission> missions = clanMissionRepository.findAll();
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        Set<UUID> acceptedTodayIds = playerClanMissionRepository.findByPlayerIdAndAcceptedAtGreaterThanEqual(playerId, startOfDay).stream().map(PlayerClanMission::getClanMissionId).collect(Collectors.toSet());
        return missions.stream().filter(m -> m.getMinClanLevel() <= clan.getLevel()).sorted(Comparator.comparingInt(ClanMission::getMinClanLevel).thenComparing(ClanMission::getCode)).map(m -> mapper.toCatalog(m, acceptedTodayIds.contains(m.getId()))).toList();
    }

    public ListClanMissionsUseCase(final PlayerRepository playerRepository, final ClanRepository clanRepository, final ClanMissionRepository clanMissionRepository, final PlayerClanMissionRepository playerClanMissionRepository, final ClanMissionResponseMapper mapper) {
        this.playerRepository = playerRepository;
        this.clanRepository = clanRepository;
        this.clanMissionRepository = clanMissionRepository;
        this.playerClanMissionRepository = playerClanMissionRepository;
        this.mapper = mapper;
    }
}
