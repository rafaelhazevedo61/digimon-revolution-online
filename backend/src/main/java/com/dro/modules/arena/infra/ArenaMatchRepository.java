package com.dro.modules.arena.infra;

import com.dro.modules.arena.domain.ArenaMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArenaMatchRepository extends JpaRepository<ArenaMatch, UUID> {

    Page<ArenaMatch> findByAttackerPlayerIdOrDefenderPlayerIdOrderByCreatedAtDesc(
            UUID attackerPlayerId, UUID defenderPlayerId, Pageable pageable);
}
