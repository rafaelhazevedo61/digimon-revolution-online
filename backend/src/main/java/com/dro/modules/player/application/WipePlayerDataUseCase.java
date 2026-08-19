package com.dro.modules.player.application;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
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
        jdbcTemplate.execute("UPDATE players SET active_digimon_id = NULL");
        jdbcTemplate.execute("UPDATE digimons SET weapon_id = NULL, armor_id = NULL, accessory_id = NULL");
        jdbcTemplate.execute("DELETE FROM inventory_equipments");
        jdbcTemplate.execute("DELETE FROM digitama_history");
        jdbcTemplate.execute("DELETE FROM digimons");
        jdbcTemplate.execute("DELETE FROM players");
    }
}
