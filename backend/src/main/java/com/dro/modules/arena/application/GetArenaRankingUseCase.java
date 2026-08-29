package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaRankingEntryResponse;
import com.dro.modules.arena.domain.ArenaRules;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Arena.
 */
@Service
public class GetArenaRankingUseCase {
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final List<DigimonStatus> RANKING_STATUSES = List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED);
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    public List<ArenaRankingEntryResponse> execute(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        Page<Digimon> result = digimonRepository.findByStatusInOrderByArenaRatingDescLevelDesc(RANKING_STATUSES, PageRequest.of(safePage, safeSize));
        List<Digimon> digimons = result.getContent();
        List<UUID> playerIds = digimons.stream().map(Digimon::getPlayerId).distinct().toList();
        Map<UUID, String> playerNames = playerRepository.findAllById(playerIds).stream().collect(Collectors.toMap(Player::getId, Player::getUsername));
        List<ArenaRankingEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < digimons.size(); i++) {
            Digimon d = digimons.get(i);
            int position = safePage * safeSize + i + 1;
            entries.add(new ArenaRankingEntryResponse(position, d.getName(), playerNames.getOrDefault(d.getPlayerId(), "Unknown"), d.getStage(), d.getLevel(), d.getArenaRating(), d.getArenaWins(), d.getArenaLosses(), d.getId(), d.getPlayerId(), ArenaRules.tierFor(d.getArenaRating()).getLabel()));
        }
        return entries;
    }

    public GetArenaRankingUseCase(final DigimonRepository digimonRepository, final PlayerRepository playerRepository) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
    }
}
