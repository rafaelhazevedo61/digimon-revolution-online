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
        // Contagens novas não dependem de estado mutável posterior ao nascimento.
        jdbc.query("""
                SELECT
                    COUNT(*) FILTER (WHERE rarity = 'RARE') AS rare,
                    COUNT(*) FILTER (WHERE rarity = 'EPIC') AS epic,
                    COUNT(*) FILTER (WHERE rarity = 'LEGENDARY') AS legendary,
                    COUNT(*) FILTER (WHERE rarity IN ('EPIC', 'LEGENDARY')) AS elite,
                    COUNT(*) FILTER (WHERE iv_hp = 100 OR iv_attack = 100 OR iv_defense = 100) AS perfect,
                    COUNT(*) FILTER (WHERE iv_hp = 100 AND iv_attack = 100 AND iv_defense = 100) AS all_perfect,
                    COUNT(*) FILTER (WHERE trait IS NOT NULL) AS traits
                FROM achievement_birth_snapshots WHERE player_id = ?
                """, rs -> {
                    result.put("BIRTH_RARE", rs.getLong("rare"));
                    result.put("BIRTH_EPIC", rs.getLong("epic"));
                    result.put("BIRTH_LEGENDARY", rs.getLong("legendary"));
                    result.put("BIRTH_ELITE", rs.getLong("elite"));
                    result.put("BIRTH_PERFECT_IV", rs.getLong("perfect"));
                    result.put("BIRTH_ALL_PERFECT_IV", rs.getLong("all_perfect"));
                    result.put("BIRTH_TRAIT", rs.getLong("traits"));
                }, playerId);
        jdbc.query("SELECT metric, value FROM player_achievement_counters WHERE player_id = ?",
                rs -> { result.put(rs.getString("metric"), rs.getLong("value")); }, playerId);
        jdbc.query("SELECT metric, COUNT(*) AS value FROM achievement_gameplay_events WHERE player_id = ? GROUP BY metric",
                rs -> { result.put(rs.getString("metric"), rs.getLong("value")); }, playerId);
        result.put("MASTERED_SPECIES", number("""
                SELECT COUNT(*) FROM (
                    SELECT digimon_info_id FROM digimon_collection_entries WHERE player_id = ?
                    AND rarity IN ('COMMON', 'RARE', 'EPIC', 'LEGENDARY')
                    GROUP BY digimon_info_id HAVING COUNT(DISTINCT rarity) = 4
                ) mastered
                """, playerId));
        result.put("COMPLETE_LINES", number("""
                SELECT COUNT(*) FROM (
                    SELECT s.line_id FROM achievement_line_species s
                    LEFT JOIN digimon_collection_entries c ON c.digimon_info_id = s.digimon_info_id AND c.player_id = ?
                    GROUP BY s.line_id
                    HAVING COUNT(DISTINCT c.digimon_info_id) = COUNT(DISTINCT s.digimon_info_id)
                ) completed
                """, playerId));
        jdbc.query("""
                SELECT i.stage, COUNT(DISTINCT c.digimon_info_id) AS value
                FROM digimon_collection_entries c JOIN digimon_infos i ON i.id = c.digimon_info_id
                WHERE c.player_id = ? GROUP BY i.stage
                """, rs -> { result.put("SPECIES_" + rs.getString("stage"), rs.getLong("value")); }, playerId);
        jdbc.query("""
                SELECT a.area, COUNT(c.mission_id) AS value FROM achievement_area_missions a
                LEFT JOIN achievement_completed_missions c ON c.mission_id = a.mission_id AND c.player_id = ?
                GROUP BY a.area
                """, rs -> { result.put("AREA_" + rs.getString("area"), rs.getLong("value")); }, playerId);
        result.put("BOSS_WINS", number("SELECT COUNT(*) FROM boss_attempts WHERE player_id = ? AND status = 'VICTORY'", playerId));
        result.put("DISTINCT_BOSSES", number("SELECT COUNT(DISTINCT boss_id) FROM boss_attempts WHERE player_id = ? AND status = 'VICTORY'", playerId));
        result.put("WORLD_DEFEATS", number("""
                SELECT COUNT(DISTINCT a.world_boss_id) FROM world_boss_attacks a
                JOIN world_boss_instances b ON b.id = a.world_boss_id
                WHERE a.player_id = ? AND a.damage > 0 AND b.status = 'DEFEATED'
                """, playerId));
        result.put("RAID_DEFEATS", number("""
                SELECT COUNT(DISTINCT a.clan_raid_id) FROM clan_raid_attacks a
                JOIN clan_raid_instances b ON b.id = a.clan_raid_id
                WHERE a.player_id = ? AND a.damage > 0 AND b.status = 'DEFEATED'
                """, playerId));
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
                WHERE player_achievements.progress < EXCLUDED.progress
                   OR (player_achievements.completed_at IS NULL AND EXCLUDED.completed_at IS NOT NULL)
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
