package com.dro.modules.tower.infra;

import com.dro.modules.tower.api.TowerDtos.Result;
import com.dro.modules.tower.domain.TowerRules.Reward;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class TowerRepository {
    private final JdbcTemplate jdbc;
    public TowerRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record State(long highestFloor, long attempts, Instant availableAt, Result lastResult) {}
    public State state(UUID playerId) {
        return jdbc.query("SELECT * FROM player_tower_progress WHERE player_id = ?", (rs, row) -> {
            UUID requestId = rs.getObject("request_id", UUID.class);
            Timestamp availableAt = rs.getTimestamp("available_at");
            Result result = requestId == null ? null : new Result(requestId, rs.getLong("last_floor"),
                    rs.getBoolean("last_won"), rs.getInt("last_rounds"), rs.getString("last_team_name"),
                    rs.getString("last_enemy_name"), new Reward(rs.getInt("last_bits"),
                    rs.getString("last_item_code"), rs.getString("last_item_name"), rs.getInt("last_quantity")));
            return new State(rs.getLong("highest_floor"), rs.getLong("attempts"), availableAt == null ? null : availableAt.toInstant(), result);
        }, playerId).stream().findFirst().orElse(new State(0, 0, null, null));
    }
    /** Caller holds the player row lock, including for the first attempt. */
    public void save(UUID playerId, State state) {
        Result r = state.lastResult();
        jdbc.update("""
                INSERT INTO player_tower_progress (player_id, highest_floor, attempts, available_at,
                    request_id, last_floor, last_won, last_rounds, last_bits, last_item_code,
                    last_item_name, last_quantity, last_team_name, last_enemy_name)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (player_id) DO UPDATE SET highest_floor = EXCLUDED.highest_floor,
                    attempts = EXCLUDED.attempts, available_at = EXCLUDED.available_at,
                    request_id = EXCLUDED.request_id, last_floor = EXCLUDED.last_floor,
                    last_won = EXCLUDED.last_won, last_rounds = EXCLUDED.last_rounds,
                    last_bits = EXCLUDED.last_bits, last_item_code = EXCLUDED.last_item_code,
                    last_item_name = EXCLUDED.last_item_name, last_quantity = EXCLUDED.last_quantity,
                    last_team_name = EXCLUDED.last_team_name, last_enemy_name = EXCLUDED.last_enemy_name
                """, playerId, state.highestFloor(), state.attempts(), Timestamp.from(state.availableAt()),
                r.requestId(), r.floor(), r.won(), r.rounds(), r.reward().bits(), r.reward().itemCode(),
                r.reward().itemName(), r.reward().quantity(), r.teamName(), r.enemyName());
    }
}
