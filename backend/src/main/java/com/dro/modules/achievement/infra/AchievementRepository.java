package com.dro.modules.achievement.infra;

import com.dro.modules.achievement.api.AchievementDtos.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Repository
public class AchievementRepository {
    private final JdbcTemplate jdbc;

    public AchievementRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record Definition(String code, String category, String name, String description,
                             String metric, long target, Reward reward) {}

    public List<Definition> definitions() {
        return jdbc.query("""
                SELECT a.*, i.name AS item_name FROM achievement_definitions a
                JOIN item_definitions i ON i.code = a.reward_item_code
                WHERE a.active = TRUE ORDER BY a.sort_order, a.code
                """, (rs, row) -> new Definition(rs.getString("code"), rs.getString("category"),
                rs.getString("name"), rs.getString("description"), rs.getString("metric"),
                rs.getLong("target"), new Reward(rs.getInt("reward_bits"), rs.getString("reward_item_code"),
                rs.getString("item_name"), rs.getInt("reward_quantity"))));
    }

    /** Históricos duráveis, sem inferir nascimentos a partir de compras ou renascimentos. */
    public Map<String, Long> metrics(UUID playerId) {
        Map<String, Long> result = new HashMap<>();
        result.put("HATCHES", number("""
                SELECT (SELECT COUNT(*) FROM digitama_history WHERE player_id = ? AND source = 'DIRECT_HATCH')
                     + (SELECT COUNT(*) FROM incubations WHERE player_id = ? AND status = 'CLAIMED')
                """, playerId, playerId));
        result.put("MISSIONS", number("SELECT COALESCE(MAX(value), 0) FROM player_achievement_counters WHERE player_id = ? AND metric = 'MISSIONS'", playerId));
        result.put("SPECIES", number("SELECT COUNT(DISTINCT digimon_info_id) FROM digimon_collection_entries WHERE player_id = ?", playerId));
        result.put("STAGE", number("""
                SELECT COALESCE(MAX(CASE stage WHEN 'BABY_II' THEN 1 WHEN 'ROOKIE' THEN 2
                    WHEN 'CHAMPION' THEN 3 WHEN 'ULTIMATE' THEN 4 WHEN 'MEGA' THEN 5 ELSE 0 END), 0)
                FROM (
                    SELECT i.stage FROM digimon_collection_entries c JOIN digimon_infos i ON i.id = c.digimon_info_id
                    WHERE c.player_id = ?
                    UNION ALL SELECT stage FROM digimons WHERE player_id = ?
                ) reached
                """, playerId, playerId));
        result.put("REBIRTHS", number("SELECT COUNT(*) FROM digimons WHERE player_id = ? AND status = 'REBORN'", playerId));
        result.put("ARENA_WINS", number("SELECT COUNT(*) FROM arena_matches WHERE attacker_player_id = ? AND attacker_won = TRUE", playerId));
        result.put("WORLD_ATTACKS", number("SELECT COUNT(*) FROM world_boss_attacks WHERE player_id = ?", playerId));
        result.put("RAID_ATTACKS", number("SELECT COUNT(*) FROM clan_raid_attacks WHERE player_id = ?", playerId));
        result.put("EQUIPPED", number("SELECT COUNT(*) FROM tutorial_progress WHERE player_id = ? AND step = 'EQUIP_ITEM'", playerId));
        result.put("REFINEMENT", number("SELECT COALESCE(MAX(refinement_level), 0) FROM inventory_equipments WHERE player_id = ?", playerId));
        result.put("ASCENSION", number("SELECT COALESCE(MAX(ascension_level), 0) FROM inventory_equipments WHERE player_id = ?", playerId));
        return result;
    }

    private long number(String sql, Object... parameters) {
        Long value = jdbc.queryForObject(sql, Long.class, parameters);
        return value == null ? 0 : value;
    }

    /** Progresso monotônico: nunca perde uma conclusão, mesmo após renascer ou vender uma peça. */
    public void observe(UUID playerId, Definition definition, long observed) {
        long progress = Math.max(0, Math.min(observed, definition.target()));
        jdbc.update("""
                INSERT INTO player_achievements (player_id, achievement_code, progress, completed_at)
                VALUES (?, ?, ?, CASE WHEN CAST(? AS BIGINT) >= CAST(? AS BIGINT) THEN CURRENT_TIMESTAMP ELSE NULL END)
                ON CONFLICT (player_id, achievement_code) DO UPDATE SET
                    progress = GREATEST(player_achievements.progress, EXCLUDED.progress),
                    completed_at = COALESCE(player_achievements.completed_at, EXCLUDED.completed_at)
                """, playerId, definition.code(), progress, progress, definition.target());
    }

    public record Progress(long progress, Instant completedAt, Instant claimedAt) {}

    public Map<String, Progress> progress(UUID playerId) {
        Map<String, Progress> result = new HashMap<>();
        jdbc.query("SELECT * FROM player_achievements WHERE player_id = ?", rs -> {
            result.put(rs.getString("achievement_code"), new Progress(rs.getLong("progress"),
                    instant(rs.getTimestamp("completed_at")), instant(rs.getTimestamp("claimed_at"))));
        }, playerId);
        return result;
    }

    public boolean claim(UUID playerId, String code) {
        return jdbc.update("""
                UPDATE player_achievements SET claimed_at = CURRENT_TIMESTAMP
                WHERE player_id = ? AND achievement_code = ? AND completed_at IS NOT NULL AND claimed_at IS NULL
                """, playerId, code) == 1;
    }

    private static Instant instant(Timestamp timestamp) { return timestamp == null ? null : timestamp.toInstant(); }
}
