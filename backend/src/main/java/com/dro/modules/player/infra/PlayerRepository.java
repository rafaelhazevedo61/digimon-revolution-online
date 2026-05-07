package com.dro.modules.player.infra;

import com.dro.modules.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PlayerRepository extends JpaRepository<Player, UUID>, JpaSpecificationExecutor<Player> {

    Optional<Player> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}