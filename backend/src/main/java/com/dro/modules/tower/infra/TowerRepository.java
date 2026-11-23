package com.dro.modules.tower.infra;

import com.dro.modules.tower.api.TowerDtos.Result;
import com.dro.modules.tower.api.TowerDtos.MemberHealth;
import com.dro.modules.tower.domain.TowerRules.Reward;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TowerRepository {
    private final JdbcTemplate jdbc;
    public TowerRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public record State(long highestFloor, long attempts, Instant availableAt, Result lastResult) {}
    public State state(UUID playerId) {
        // One statement keeps the result and its members in the same MVCC snapshot.
        return jdbc.query("""
                SELECT p.*, m.digimon_id, m.digimon_name, m.remaining_hp, m.max_hp
                FROM player_tower_progress p
                LEFT JOIN player_tower_last_members m ON m.player_id = p.player_id
                WHERE p.player_id = ? ORDER BY m.position
                """, rs -> {
            if (!rs.next()) return new State(0, 0, null, null);
            long highestFloor = rs.getLong("highest_floor");
            long attempts = rs.getLong("attempts");
            UUID requestId = rs.getObject("request_id", UUID.class);
            Timestamp availableAt = rs.getTimestamp("available_at");
            long lastFloor = rs.getLong("last_floor");
            boolean won = rs.getBoolean("last_won");
            int rounds = rs.getInt("last_rounds");
            String teamName = rs.getString("last_team_name");
            String enemyName = rs.getString("last_enemy_name");
            Reward reward = new Reward(rs.getInt("last_bits"), rs.getString("last_item_code"),
                    rs.getString("last_item_name"), rs.getInt("last_quantity"));
            List<MemberHealth> members = new ArrayList<>();
            do {
                UUID digimonId = rs.getObject("digimon_id", UUID.class);
                if (digimonId != null) members.add(new MemberHealth(digimonId, rs.getString("digimon_name"),
                        rs.getInt("remaining_hp"), rs.getInt("max_hp")));
            } while (rs.next());
            Result result = requestId == null ? null : new Result(requestId, lastFloor, won, rounds,
                    teamName, enemyName, reward, members);
            return new State(highestFloor, attempts, availableAt == null ? null : availableAt.toInstant(), result);
        }, playerId);
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
        jdbc.update("DELETE FROM player_tower_last_members WHERE player_id = ?", playerId);
        for (int i = 0; i < r.members().size(); i++) {
            MemberHealth member = r.members().get(i);
            jdbc.update("""
                    INSERT INTO player_tower_last_members
                        (player_id, position, digimon_id, digimon_name, remaining_hp, max_hp)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, playerId, i, member.digimonId(), member.name(), member.remainingHp(), member.maxHp());
        }
    }
}
