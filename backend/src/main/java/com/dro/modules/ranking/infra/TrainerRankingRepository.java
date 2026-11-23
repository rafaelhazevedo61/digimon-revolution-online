package com.dro.modules.ranking.infra;

import com.dro.modules.ranking.api.dto.response.TrainerRankingDtos.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public class TrainerRankingRepository {
    private final JdbcTemplate jdbc;
    public TrainerRankingRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private static final RowMapper<Entry> ENTRY = (rs, row) -> new Entry(
            rs.getObject("player_id", UUID.class), rs.getString("player_name"), rs.getLong("position"),
            rs.getBigDecimal("total_score"), rs.getBigDecimal("collection_score"),
            rs.getBigDecimal("achievement_score"), rs.getBigDecimal("tower_score"),
            rs.getLong("collection_entries"), rs.getLong("mastered_species"),
            rs.getLong("completed_achievements"), rs.getLong("highest_floor"), rs.getBigDecimal("weekly_gain"),
            rs.getObject("weekly_position", Long.class), instant(rs.getTimestamp("weekly_baseline_at")),
            rs.getString("weekly_baseline_kind"), rs.getString("movement"), rs.getObject("position_change", Long.class));
    private static java.time.Instant instant(Timestamp value) { return value == null ? null : value.toInstant(); }

    /** Metadata row is the cross-instance lock; re-check freshness after acquiring it. */
    public boolean lockIfDue(long intervalMillis) {
        return !jdbc.query("""
                SELECT id FROM trainer_ranking_state
                WHERE updated_at IS NULL OR updated_at <= CURRENT_TIMESTAMP - (? * INTERVAL '1 millisecond')
                FOR UPDATE SKIP LOCKED
                """, (rs, row) -> rs.getInt("id"), intervalMillis).isEmpty();
    }

    public void rebuild() {
        // DELETE is transactional/MVCC-safe; TRUNCATE would break older reader snapshots.
        jdbc.update("DELETE FROM trainer_ranking_entries");
        jdbc.update("""
                INSERT INTO trainer_ranking_entries (player_id, position, player_name, collection_entries,
                    mastered_species, completed_achievements, highest_floor, collection_score,
                    achievement_score, tower_score, total_score)
                WITH species AS (
                    SELECT player_id, digimon_info_id, COUNT(DISTINCT rarity) AS rarities
                    FROM digimon_collection_entries GROUP BY player_id, digimon_info_id
                ), collection AS (
                    SELECT player_id, SUM(rarities)::bigint AS entries,
                           COUNT(*) FILTER (WHERE rarities = 4) AS mastered
                    FROM species GROUP BY player_id
                ), achievements AS (
                    SELECT a.player_id, COUNT(*) AS completed, SUM(d.trainer_points) AS score
                    FROM player_achievements a JOIN achievement_definitions d ON d.code = a.achievement_code
                    WHERE a.completed_at IS NOT NULL AND d.active = TRUE GROUP BY a.player_id
                ), bots AS (
                    SELECT DISTINCT player_id FROM digimons WHERE is_bot = TRUE
                ), scores AS (
                    SELECT p.id, p.username, COALESCE(c.entries, 0) AS entries,
                           COALESCE(c.mastered, 0) AS mastered, COALESCE(a.completed, 0) AS completed,
                           COALESCE(t.highest_floor, 0) AS floor,
                           COALESCE(c.entries, 0)::numeric * 10 + COALESCE(c.mastered, 0)::numeric * 50 AS collection_score,
                           COALESCE(a.score, 0)::numeric AS achievement_score,
                           COALESCE(t.highest_floor, 0)::numeric * 20 AS tower_score
                    FROM players p LEFT JOIN collection c ON c.player_id = p.id
                    LEFT JOIN achievements a ON a.player_id = p.id
                    LEFT JOIN player_tower_progress t ON t.player_id = p.id
                    LEFT JOIN bots b ON b.player_id = p.id
                    WHERE p.user_type = 'PLAYER' AND p.trainer_ranking_excluded = FALSE AND b.player_id IS NULL
                ), totals AS (
                    SELECT *, collection_score + achievement_score + tower_score AS total_score FROM scores
                )
                SELECT id, ROW_NUMBER() OVER (ORDER BY total_score DESC, tower_score DESC,
                    achievement_score DESC, collection_score DESC, LOWER(username), id),
                    username, entries, mastered, completed, floor, collection_score, achievement_score,
                    tower_score, total_score FROM totals
                """);
        jdbc.update("""
                UPDATE trainer_ranking_state SET updated_at = CURRENT_TIMESTAMP,
                    total_players = (SELECT COUNT(*) FROM trainer_ranking_entries) WHERE id = 1
                """);
    }

    public State state() {
        return jdbc.queryForObject("SELECT * FROM trainer_ranking_state WHERE id = 1", (rs, row) -> {
            Timestamp updated = rs.getTimestamp("updated_at");
            return new State(instant(updated), rs.getLong("total_players"), rs.getObject("week_start", java.time.LocalDate.class),
                    instant(rs.getTimestamp("week_baseline_at")), rs.getObject("comparison_date", java.time.LocalDate.class),
                    rs.getBoolean("comparison_available"), rs.getString("rules_version"));
        });
    }
    public List<Entry> page(int page, int size) { return page(page, size, ""); }
    public List<Entry> page(int page, int size, String search) {
        return jdbc.query("""
                SELECT * FROM trainer_ranking_entries
                WHERE STRPOS(LOWER(player_name), LOWER(?)) > 0
                ORDER BY position LIMIT ? OFFSET ?
                """, ENTRY, search, size, (long) page * size);
    }
    public List<Entry> weeklyPage(int page, int size, String search) {
        return jdbc.query("""
                SELECT * FROM trainer_ranking_entries
                WHERE STRPOS(LOWER(player_name), LOWER(?)) > 0 AND weekly_position IS NOT NULL
                ORDER BY weekly_position LIMIT ? OFFSET ?
                """, ENTRY, search, size, (long) page * size);
    }
    public long countMatching(String search) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM trainer_ranking_entries
                WHERE STRPOS(LOWER(player_name), LOWER(?)) > 0
                """, Long.class, search);
        return count == null ? 0 : count;
    }
    public Profile profile(UUID playerId) {
        return jdbc.query("""
                SELECT e.*, c.id AS clan_id, c.name AS clan_name, c.tag AS clan_tag, s.updated_at
                FROM trainer_ranking_entries e JOIN players p ON p.id = e.player_id
                CROSS JOIN trainer_ranking_state s
                LEFT JOIN clans c ON c.id = p.clan_id AND c.active = TRUE
                WHERE e.player_id = ? AND s.id = 1 AND p.user_type = 'PLAYER'
                    AND p.trainer_ranking_excluded = FALSE
                    AND NOT EXISTS (SELECT 1 FROM digimons d WHERE d.player_id = p.id AND d.is_bot = TRUE)
                """, (rs, row) -> new Profile(ENTRY.mapRow(rs, row), rs.getObject("clan_id", UUID.class),
                rs.getString("clan_name"), rs.getString("clan_tag"), rs.getTimestamp("updated_at").toInstant()), playerId)
                .stream().findFirst().orElse(null);
    }

    /** Called inside the snapshot transaction and its database lock. First daily observation is immutable. */
    public void captureDailyHistory() {
        jdbc.update("""
                INSERT INTO trainer_ranking_daily_history (snapshot_date, rules_version, player_id,
                    observed_at, position, player_name, collection_entries, mastered_species,
                    completed_achievements, highest_floor, collection_score, achievement_score, tower_score, total_score)
                SELECT (s.updated_at AT TIME ZONE 'America/Sao_Paulo')::date, trainer_ranking_rules_version(),
                    e.player_id, s.updated_at, e.position, e.player_name, e.collection_entries,
                    e.mastered_species, e.completed_achievements, e.highest_floor,
                    e.collection_score, e.achievement_score, e.tower_score, e.total_score
                FROM trainer_ranking_entries e CROSS JOIN trainer_ranking_state s WHERE s.id = 1
                ON CONFLICT (snapshot_date, rules_version, player_id) DO NOTHING
                """);
    }
    public Entry me(UUID playerId) {
        return jdbc.query("SELECT * FROM trainer_ranking_entries WHERE player_id = ?", ENTRY, playerId)
                .stream().findFirst().orElse(null);
    }
    /** Empty list means a deleted/nonexistent player; false means explicitly ineligible. */
    public List<Boolean> eligibility(UUID playerId) {
        return jdbc.query("""
                SELECT user_type = 'PLAYER' AND trainer_ranking_excluded = FALSE
                    AND NOT EXISTS (SELECT 1 FROM digimons d WHERE d.player_id = p.id AND d.is_bot = TRUE) AS eligible
                FROM players p WHERE p.id = ?
                """, (rs, row) -> rs.getBoolean("eligible"), playerId);
    }
}
