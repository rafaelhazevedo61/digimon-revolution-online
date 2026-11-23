package com.dro.modules.ranking.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Publishes temporal comparisons inside the ranking refresh transaction and database lock. */
@Repository
public class TrainerRankingProgressRepository {
    private final JdbcTemplate jdbc;
    public TrainerRankingProgressRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void publish() {
        jdbc.update("""
                WITH context AS (
                    SELECT trainer_ranking_rules_version() AS version,
                        (updated_at AT TIME ZONE 'America/Sao_Paulo')::date AS today,
                        DATE_TRUNC('week', updated_at AT TIME ZONE 'America/Sao_Paulo')::date AS monday
                    FROM trainer_ranking_state WHERE id = 1
                )
                UPDATE trainer_ranking_state s SET rules_version = c.version, week_start = c.monday,
                    comparison_date = c.today - 1,
                    comparison_available = EXISTS (SELECT 1 FROM trainer_ranking_recorded_days d
                        WHERE d.snapshot_date = c.today - 1 AND d.rules_version = c.version)
                FROM context c WHERE s.id = 1
                """);
        jdbc.update("""
                INSERT INTO trainer_ranking_recorded_days (snapshot_date, rules_version, observed_at)
                SELECT (updated_at AT TIME ZONE 'America/Sao_Paulo')::date, rules_version, updated_at
                FROM trainer_ranking_state WHERE id = 1
                ON CONFLICT (snapshot_date, rules_version) DO NOTHING
                """);
        // Existing accounts start from observed progress, including those currently excluded.
        // New accounts created after this period opens will have no baseline row and start at zero.
        jdbc.update("""
                WITH opened AS (
                    INSERT INTO trainer_ranking_weeks (week_start, rules_version, started_at)
                    SELECT s.week_start, s.rules_version, COALESCE((SELECT MIN(h.observed_at)
                        FROM trainer_ranking_daily_history h WHERE h.snapshot_date >= s.week_start
                        AND h.rules_version = s.rules_version AND h.observed_at <= s.updated_at), s.updated_at)
                    FROM trainer_ranking_state s WHERE s.id = 1
                    ON CONFLICT (week_start, rules_version) DO NOTHING
                    RETURNING week_start, rules_version
                )
                INSERT INTO trainer_ranking_week_baselines
                    (week_start, rules_version, player_id, baseline_score, baseline_at, baseline_kind)
                SELECT o.week_start, o.rules_version, p.id, COALESCE(h.total_score, e.total_score),
                    COALESCE(h.observed_at, s.updated_at), 'EXISTING'
                FROM opened o CROSS JOIN players p CROSS JOIN trainer_ranking_state s
                LEFT JOIN trainer_ranking_entries e ON e.player_id = p.id
                LEFT JOIN LATERAL (SELECT history.total_score, history.observed_at
                    FROM trainer_ranking_daily_history history WHERE history.player_id = p.id
                    AND history.snapshot_date >= o.week_start AND history.rules_version = o.rules_version
                    AND history.observed_at <= s.updated_at ORDER BY history.observed_at LIMIT 1) h ON TRUE
                WHERE s.id = 1
                """);
        jdbc.update("""
                INSERT INTO trainer_ranking_week_baselines AS b
                    (week_start, rules_version, player_id, baseline_score, baseline_at, baseline_kind)
                SELECT s.week_start, s.rules_version, e.player_id, 0, s.updated_at, 'NEW_ENTRY'
                FROM trainer_ranking_entries e CROSS JOIN trainer_ranking_state s WHERE s.id = 1
                ON CONFLICT (week_start, rules_version, player_id) DO UPDATE
                    SET baseline_score = COALESCE(b.baseline_score,
                            (SELECT e.total_score FROM trainer_ranking_entries e WHERE e.player_id = EXCLUDED.player_id)),
                        baseline_at = CASE WHEN b.baseline_score IS NULL THEN EXCLUDED.baseline_at ELSE b.baseline_at END,
                        baseline_kind = CASE WHEN b.baseline_score IS NULL THEN 'RETURNING' ELSE b.baseline_kind END
                """);
        jdbc.update("""
                WITH comparisons AS (
                    SELECT e.player_id, GREATEST(0, e.total_score - b.baseline_score) AS gain,
                        b.baseline_at, b.baseline_kind, s.comparison_available, h.player_id AS previous_player,
                        h.position - e.position AS delta, e.position AS global_position
                    FROM trainer_ranking_entries e CROSS JOIN trainer_ranking_state s
                    JOIN trainer_ranking_week_baselines b ON b.week_start = s.week_start
                        AND b.rules_version = s.rules_version AND b.player_id = e.player_id
                    LEFT JOIN trainer_ranking_daily_history h ON h.player_id = e.player_id
                        AND h.snapshot_date = s.comparison_date AND h.rules_version = s.rules_version
                    WHERE s.id = 1
                ), ordered AS (
                    SELECT *, ROW_NUMBER() OVER (ORDER BY gain DESC, global_position) AS weekly_position
                    FROM comparisons
                )
                UPDATE trainer_ranking_entries e SET weekly_gain = c.gain, weekly_position = c.weekly_position,
                    weekly_baseline_at = c.baseline_at, weekly_baseline_kind = c.baseline_kind,
                    position_change = CASE WHEN c.comparison_available AND c.previous_player IS NOT NULL THEN c.delta END,
                    movement = CASE WHEN NOT c.comparison_available THEN 'UNAVAILABLE'
                        WHEN c.previous_player IS NULL THEN 'NEW' WHEN c.delta > 0 THEN 'UP'
                        WHEN c.delta < 0 THEN 'DOWN' ELSE 'SAME' END
                FROM ordered c WHERE e.player_id = c.player_id
                """);
        jdbc.update("""
                UPDATE trainer_ranking_state s SET week_baseline_at = w.started_at
                FROM trainer_ranking_weeks w WHERE s.id = 1
                    AND w.week_start = s.week_start AND w.rules_version = s.rules_version
                """);
    }
}
