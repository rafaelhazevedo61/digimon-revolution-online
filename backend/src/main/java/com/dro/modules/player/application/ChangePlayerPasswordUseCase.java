package com.dro.modules.player.application;

import com.dro.modules.auth.domain.exception.InvalidCredentialsException;
import com.dro.modules.player.api.dto.request.ChangePasswordRequest;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
@Service
@RequiredArgsConstructor
public class ChangePlayerPasswordUseCase {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(String token, ChangePasswordRequest request) {
        if (request.currentPassword().equals(request.newPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        Player player = playerRepository.findById(TokenExtractor.extractPlayerId(token))
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (!passwordEncoder.matches(request.currentPassword(), player.getPassword())) {
            throw new InvalidCredentialsException();
        }

        player.setPassword(passwordEncoder.encode(request.newPassword()));
        playerRepository.save(player);
    }
}
