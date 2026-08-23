package com.dro.modules.player.application;

import com.dro.modules.player.api.dto.request.WipePlayersRequest;
import com.dro.shared.audit.AdminAuditService;
import com.dro.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
@Service
@RequiredArgsConstructor
public class WipePlayerDataUseCase {

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuditService adminAuditService;

    /**
     * Remove os dados operacionais de todos os jogadores após confirmação explícita.
     *
     * <p>A auditoria registra apenas o ator e a confirmação, nunca dados
     * sensíveis.</p>
     *
     * @param authorization token do administrador autenticado
     * @param request confirmação que deve ser exatamente {@code WIPE}
     * @throws BadRequestException quando a confirmação não é exatamente {@code WIPE}
     */
    @Transactional
    public void execute(String authorization, WipePlayersRequest request) {
        if (request == null || !"WIPE".equals(request.confirmation())) {
            throw new BadRequestException("A confirmação deve ser exatamente WIPE.");
        }

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

        adminAuditService.success(
                authorization,
                "ADMIN_PLAYER_WIPE",
                "PlayerData",
                "all",
                "wipe",
                "Dados de jogadores removidos",
                Map.of("confirmation", "WIPE")
        );
    }
}
