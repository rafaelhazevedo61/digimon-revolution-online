package com.dro.modules.player.application;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class VipAccess {
    public static final String PAUSE_REASON = "VIP_REQUIRED";
    private final PlayerRepository players;

    public VipAccess(PlayerRepository players) {
        this.players = players;
    }

    // Call inside the automation transaction, before locking an activity.
    public Player lockPlayer(UUID playerId) {
        return players.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
    }

    public static void requireActive(Player player) {
        if (!player.hasActiveVip(Instant.now())) {
            throw new ForbiddenException("A automação é exclusiva para jogadores com VIP ativo.");
        }
    }
}
