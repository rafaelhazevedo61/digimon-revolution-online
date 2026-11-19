package com.dro.modules.evolution.infra;

import com.dro.modules.digimon.domain.enums.Stage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.UUID;

@Repository
public class FragmentExchangeDailyRepository {
    private final JdbcTemplate jdbc;
    public FragmentExchangeDailyRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public int received(UUID playerId, Stage stage, LocalDate date) {
        return jdbc.queryForObject("""
                SELECT COALESCE(SUM(received), 0) FROM fragment_exchange_daily
                WHERE player_id = ? AND target_stage = ? AND exchange_date = ?
                """, Integer.class, playerId, stage.name(), date);
    }

    // O caso de uso mantém o lock do jogador até o commit, inclusive no primeiro uso do dia.
    public void add(UUID playerId, Stage stage, LocalDate date, int amount) {
        jdbc.update("""
                INSERT INTO fragment_exchange_daily (player_id, target_stage, exchange_date, received)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (player_id, target_stage, exchange_date)
                DO UPDATE SET received = fragment_exchange_daily.received + EXCLUDED.received
                """, playerId, stage.name(), date, amount);
    }
}
