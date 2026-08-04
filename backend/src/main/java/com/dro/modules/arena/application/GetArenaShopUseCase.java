package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaShopProductResponse;
import com.dro.modules.arena.api.dto.response.ArenaShopResponse;
import com.dro.modules.arena.infra.ArenaShopProductRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetArenaShopUseCase {

    private final PlayerRepository playerRepository;
    private final ArenaShopProductRepository arenaShopProductRepository;

    public ArenaShopResponse execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        List<ArenaShopProductResponse> products = arenaShopProductRepository
                .findByActiveTrueOrderByPriceCoinsAsc()
                .stream()
                .map(p -> new ArenaShopProductResponse(
                        p.getCode(),
                        p.getName(),
                        p.getItemType(),
                        p.getQuantity(),
                        p.getPriceCoins()))
                .toList();

        return new ArenaShopResponse(player.getArenaCoins(), products);
    }
}
