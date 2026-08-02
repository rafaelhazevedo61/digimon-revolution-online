package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaLobbyResponse;
import com.dro.modules.arena.api.dto.response.ArenaOpponentResponse;
import com.dro.modules.arena.domain.ArenaRules;
import com.dro.modules.boss.domain.BossCombatRules;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetArenaLobbyUseCase {

    private static final int MAX_OPPONENTS = 15;

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonPowerService digimonPowerService;

    public ArenaLobbyResponse execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("You have no active Digimon to fight in the arena");
        }

        Digimon me = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));

        me.regenerateEnergy();
        double myPower = digimonPowerService.calculatePower(me);

        List<Digimon> candidates = digimonRepository
                .findByStatusAndPlayerIdNot(DigimonStatus.ACTIVE, playerId);

        List<Digimon> nearest = candidates.stream()
                .sorted(Comparator.comparingInt(d -> Math.abs(d.getArenaRating() - me.getArenaRating())))
                .limit(MAX_OPPONENTS)
                .toList();

        List<UUID> opponentPlayerIds = nearest.stream()
                .map(Digimon::getPlayerId)
                .distinct()
                .toList();

        Map<UUID, String> playerNames = playerRepository.findAllById(opponentPlayerIds)
                .stream()
                .collect(Collectors.toMap(Player::getId, Player::getUsername));

        List<ArenaOpponentResponse> opponents = nearest.stream().map(d -> {
            double power = digimonPowerService.calculatePower(d);
            int winChance = BossCombatRules.calculateWinChance(myPower, power);
            return new ArenaOpponentResponse(
                    d.getId(),
                    d.getName(),
                    playerNames.getOrDefault(d.getPlayerId(), "Unknown"),
                    d.getStage(),
                    d.getLevel(),
                    d.getArenaRating(),
                    (int) Math.round(power),
                    winChance
            );
        }).toList();

        return new ArenaLobbyResponse(
                me.getName(),
                me.getArenaRating(),
                me.getArenaWins(),
                me.getArenaLosses(),
                (int) Math.round(myPower),
                me.getEnergy(),
                ArenaRules.ENERGY_COST,
                opponents
        );
    }
}
