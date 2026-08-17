package com.dro.modules.arena.application;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminResetArenaDailyUseCase {

    private final PlayerRepository playerRepository;

    @Transactional
    public int execute() {
        LocalDateTime now = LocalDateTime.now();
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            player.setArenaDailyResetAt(now);
        }
        playerRepository.saveAll(players);
        return players.size();
    }
}
