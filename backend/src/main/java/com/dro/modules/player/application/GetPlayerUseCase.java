package com.dro.modules.player.application;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.player.api.dto.response.PlayerResponse;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlayerUseCase {

    private final PlayerRepository repository;

    public PlayerResponse execute(String token) {

        if (token == null || !token.contains(":")) {
            throw new BadRequestException("Invalid token");
        }

        String playerIdPart = token.split(":")[1];

        UUID playerId = UUID.fromString(playerIdPart);

        Player player = repository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        return new PlayerResponse(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getCreatedAt()
        );
    }
}
