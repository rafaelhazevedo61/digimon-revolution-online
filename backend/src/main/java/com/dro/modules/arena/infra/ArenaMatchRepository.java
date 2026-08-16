package com.dro.modules.arena.infra;

import com.dro.modules.arena.domain.ArenaMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArenaMatchRepository extends JpaRepository<ArenaMatch, UUID> {

    Page<ArenaMatch> findByAttackerPlayerIdOrDefenderPlayerIdOrderByCreatedAtDesc(
            UUID attackerPlayerId, UUID defenderPlayerId, Pageable pageable);

    long countByAttackerPlayerIdAndCreatedAtGreaterThanEqual(UUID attackerPlayerId, Instant since);

    List<ArenaMatch> findByAttackerPlayerIdAndCreatedAtGreaterThanEqual(UUID attackerPlayerId, Instant since);

    Optional<ArenaMatch> findFirstByAttackerPlayerIdAndDefenderDigimonIdOrderByCreatedAtDesc(
            UUID attackerPlayerId, UUID defenderDigimonId);

    long deleteByCreatedAtGreaterThanEqual(Instant since);

    List<ArenaMatch> findByCreatedAtGreaterThanEqual(Instant since);
}
