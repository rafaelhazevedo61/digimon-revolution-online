package com.dro.modules.player.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.api.dto.request.ChangeUsernameRequest;
import com.dro.modules.player.api.dto.response.ChangeUsernameResponse;
import com.dro.modules.player.api.dto.response.UsernameChangeInfoResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.security.JwtService;
import com.dro.shared.util.TokenExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/** Troca o username mediante cobrança de Bits do Digimon ativo. */
@Service
public class ChangeUsernameUseCase {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final JwtService jwtService;
    private final int[] configuredCosts;

    public ChangeUsernameUseCase(
            PlayerRepository playerRepository,
            DigimonRepository digimonRepository,
            JwtService jwtService,
            @Value("${dro.gameplay.username-change.costs:1000,5000,10000}") String configuredCosts) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.jwtService = jwtService;
        this.configuredCosts = parseCosts(configuredCosts);
    }

    @Transactional(readOnly = true)
    public UsernameChangeInfoResponse preview(String token) {
        Player player = playerRepository.findById(TokenExtractor.extractPlayerId(token))
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon activeDigimon = digimonRepository.findByPlayerIdAndStatus(player.getId(), DigimonStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new BadRequestException("É necessário ter um Digimon ativo para consultar o custo."));
        return new UsernameChangeInfoResponse(
                player.getUsername(),
                calculateCost(player.getUsernameChangeCount()),
                activeDigimon.getBits(),
                player.getUsernameChangeCount());
    }

    @Transactional
    public ChangeUsernameResponse execute(String token, ChangeUsernameRequest request) {
        Player player = playerRepository.findByIdForUpdate(TokenExtractor.extractPlayerId(token))
                .orElseThrow(() -> new NotFoundException("Player not found"));
        String newUsername = request.newUsername().trim();
        if (newUsername.equalsIgnoreCase(player.getUsername().trim())) {
            throw new BadRequestException("O novo username deve ser diferente do atual.");
        }
        if (playerRepository.existsByUsernameIgnoreCase(newUsername)) {
            throw new BadRequestException("Este username já está em uso.");
        }
        Digimon activeDigimon = digimonRepository.findByPlayerIdAndStatusForUpdate(player.getId(), DigimonStatus.ACTIVE)
                .stream().findFirst()
                .orElseThrow(() -> new BadRequestException("É necessário ter um Digimon ativo para pagar o custo."));
        int cost = calculateCost(player.getUsernameChangeCount());
        if (activeDigimon.getBits() < cost) {
            throw new BadRequestException("Você não possui Bits suficientes para trocar o username. Custo: " + cost + ".");
        }
        activeDigimon.setBits(activeDigimon.getBits() - cost);
        player.setUsername(newUsername);
        player.incrementUsernameChangeCount();
        player.incrementTokenVersion();
        playerRepository.save(player);
        digimonRepository.save(activeDigimon);
        return new ChangeUsernameResponse(
                jwtService.generateToken(player),
                newUsername,
                cost,
                activeDigimon.getBits(),
                player.getUsernameChangeCount());
    }

    public int calculateCost(int changeCount) {
        if (changeCount < configuredCosts.length) {
            return configuredCosts[changeCount];
        }
        int last = configuredCosts[configuredCosts.length - 1];
        return Math.addExact(last, Math.multiplyExact(changeCount - configuredCosts.length + 1, last));
    }

    private static int[] parseCosts(String value) {
        int[] costs = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .toArray();
        if (costs.length == 0 || Arrays.stream(costs).anyMatch(cost -> cost <= 0)) {
            throw new IllegalArgumentException("Username change costs must contain positive values");
        }
        for (int i = 1; i < costs.length; i++) {
            if (costs[i] <= costs[i - 1]) {
                throw new IllegalArgumentException("Username change costs must be strictly increasing");
            }
        }
        return costs;
    }
}
