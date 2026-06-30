package com.dro.modules.player.application;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WipePlayerDataUseCase {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void execute() {
        jdbcTemplate.execute("DELETE FROM boss_attempts");
        jdbcTemplate.execute("DELETE FROM mission_instances");
        jdbcTemplate.execute("DELETE FROM player_mission_progress");
        jdbcTemplate.execute("DELETE FROM incubations");
        jdbcTemplate.execute("DELETE FROM inventory_items");
        jdbcTemplate.execute("DELETE FROM equipments");
        jdbcTemplate.execute("DELETE FROM digitama_history");
        jdbcTemplate.execute("DELETE FROM digimons");
        jdbcTemplate.execute("UPDATE players SET active_digimon_id = NULL");
        jdbcTemplate.execute("DELETE FROM players");
    }
}
