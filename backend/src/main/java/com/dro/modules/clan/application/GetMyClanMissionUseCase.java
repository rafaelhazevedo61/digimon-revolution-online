package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.PlayerClanMissionResponse;
import com.dro.modules.clan.domain.ClanMission;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.ClanMissionRepository;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class GetMyClanMissionUseCase {
    private final PlayerRepository playerRepository;
    private final PlayerClanMissionRepository playerClanMissionRepository;
    private final ClanMissionRepository clanMissionRepository;
    private final ClanMissionResponseMapper mapper;

    public PlayerClanMissionResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        if (!playerRepository.existsById(playerId)) {
            throw new NotFoundException("Player not found");
        }
        PlayerClanMission active = playerClanMissionRepository.findByPlayerIdAndStatusIn(playerId, List.of(PlayerClanMissionStatus.IN_PROGRESS, PlayerClanMissionStatus.COMPLETED)).orElse(null);
        if (active == null) {
            return null;
        }
        ClanMission mission = clanMissionRepository.findById(active.getClanMissionId()).orElseThrow(() -> new NotFoundException("Clan mission not found"));
        return mapper.toPlayerMission(active, mission);
    }

    public GetMyClanMissionUseCase(final PlayerRepository playerRepository, final PlayerClanMissionRepository playerClanMissionRepository, final ClanMissionRepository clanMissionRepository, final ClanMissionResponseMapper mapper) {
        this.playerRepository = playerRepository;
        this.playerClanMissionRepository = playerClanMissionRepository;
        this.clanMissionRepository = clanMissionRepository;
        this.mapper = mapper;
    }
}
