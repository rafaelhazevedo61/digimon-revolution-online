package com.dro.modules.player.infra;

import com.dro.modules.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
