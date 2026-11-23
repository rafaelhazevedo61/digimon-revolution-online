package com.dro.modules.achievement.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AchievementQueueRepository {
    private final JdbcTemplate jdbc;
    public AchievementQueueRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Precisa ser chamado dentro da transação que processa e remove a pendência. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<UUID> lockNext() {
        List<UUID> ready = jdbc.query("""
                SELECT player_id FROM achievement_pending_players
                WHERE available_at <= CURRENT_TIMESTAMP
                ORDER BY requested_at, player_id LIMIT 1 FOR UPDATE SKIP LOCKED
                """, (rs, row) -> rs.getObject("player_id", UUID.class));
        return ready.stream().findFirst();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(UUID playerId) {
        jdbc.update("""
                INSERT INTO achievement_processing_state(player_id, processed_at) VALUES (?, CURRENT_TIMESTAMP)
                ON CONFLICT(player_id) DO UPDATE SET processed_at = EXCLUDED.processed_at
                """, playerId);
        jdbc.update("DELETE FROM achievement_pending_players WHERE player_id = ?", playerId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void defer(UUID playerId, long retryDelayMillis) {
        jdbc.update("""
                UPDATE achievement_pending_players SET failures = failures + 1,
                    available_at = CURRENT_TIMESTAMP + (CAST(? AS BIGINT) * INTERVAL '1 millisecond')
                WHERE player_id = ?
                """, retryDelayMillis, playerId);
    }

    public record State(Instant processedAt, boolean pending) {}

    public State state(UUID playerId) {
        List<State> states = jdbc.query("""
                SELECT s.processed_at, (q.player_id IS NOT NULL) AS pending FROM players p
                LEFT JOIN achievement_processing_state s ON s.player_id = p.id
                LEFT JOIN achievement_pending_players q ON q.player_id = p.id
                WHERE p.id = ?
                """, (rs, row) -> {
                    Timestamp timestamp = rs.getTimestamp("processed_at");
                    return new State(timestamp == null ? null : timestamp.toInstant(), rs.getBoolean("pending"));
                }, playerId);
        return states.stream().findFirst().orElse(new State(null, false));
    }
}
