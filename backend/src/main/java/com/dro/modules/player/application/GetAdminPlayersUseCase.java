package com.dro.modules.player.application;

import com.dro.modules.player.api.dto.response.AdminPlayerPageResponse;
import com.dro.modules.player.api.dto.response.AdminPlayerResponse;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.player.infra.spec.PlayerSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
@Service
public class GetAdminPlayersUseCase {
    private final PlayerRepository playerRepository;

    public AdminPlayerPageResponse execute(String username, String email, String selectedDigitama, Boolean starterSelected, Pageable pageable) {
        Page<AdminPlayerResponse> players = playerRepository.findAll(PlayerSpecifications.withFilters(username, email, selectedDigitama, starterSelected), pageable).map(AdminPlayerResponse::from);
        return AdminPlayerPageResponse.from(players);
    }

    public GetAdminPlayersUseCase(final PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }
}
