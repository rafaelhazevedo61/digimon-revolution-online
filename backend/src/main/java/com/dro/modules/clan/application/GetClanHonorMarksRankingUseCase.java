package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanHonorMarksRankingEntryResponse;
import com.dro.modules.clan.domain.PlayerClanMission;
import com.dro.modules.clan.domain.enums.PlayerClanMissionStatus;
import com.dro.modules.clan.infra.PlayerClanMissionRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
@RequiredArgsConstructor
public class GetClanHonorMarksRankingUseCase {

    private final PlayerRepository playerRepository;
    private final PlayerClanMissionRepository playerClanMissionRepository;

    public List<ClanHonorMarksRankingEntryResponse> execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null || player.getClanId() == null) {
            return List.of();
        }

        List<PlayerClanMission> claimed = playerClanMissionRepository
                .findByClanIdAndStatus(player.getClanId(), PlayerClanMissionStatus.CLAIMED);

        Map<UUID, Long> contributionByPlayer = claimed.stream()
                .collect(Collectors.groupingBy(
                        PlayerClanMission::getPlayerId,
                        Collectors.summingLong(PlayerClanMission::getHonorMarksReward)
                ));

        if (contributionByPlayer.isEmpty()) {
            return List.of();
        }

        Map<UUID, String> usernameById = playerRepository.findAllById(contributionByPlayer.keySet()).stream()
                .collect(Collectors.toMap(Player::getId, Player::getUsername));

        return contributionByPlayer.entrySet().stream()
                .map(e -> new ClanHonorMarksRankingEntryResponse(
                        e.getKey(),
                        usernameById.getOrDefault(e.getKey(), "Unknown"),
                        e.getValue()
                ))
                .sorted(Comparator.comparingLong(ClanHonorMarksRankingEntryResponse::contribution).reversed()
                        .thenComparing(ClanHonorMarksRankingEntryResponse::username))
                .toList();
    }
}
