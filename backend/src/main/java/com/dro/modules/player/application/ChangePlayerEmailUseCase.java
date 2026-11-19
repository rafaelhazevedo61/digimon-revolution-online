package com.dro.modules.player.application;

import com.dro.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.dro.modules.auth.domain.exception.InvalidCredentialsException;
import com.dro.modules.player.api.dto.request.ChangeEmailRequest;
import com.dro.modules.player.api.dto.response.ChangeEmailResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.security.JwtService;
import com.dro.shared.util.TokenExtractor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Altera o e-mail da própria conta mediante confirmação da senha atual.
 */
@Service
public class ChangePlayerEmailUseCase {
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public ChangeEmailResponse execute(String token, ChangeEmailRequest request) {
        Player player = playerRepository.findById(TokenExtractor.extractPlayerId(token))
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (!passwordEncoder.matches(request.currentPassword(), player.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String normalizedEmail = request.newEmail().trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.equals(player.getEmail().trim().toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("New email must be different from current email");
        }

        if (playerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        player.setEmail(normalizedEmail);
        player.incrementTokenVersion();
        playerRepository.save(player);

        return new ChangeEmailResponse(jwtService.generateToken(player), normalizedEmail);
    }

    public ChangePlayerEmailUseCase(final PlayerRepository playerRepository,
                                    final PasswordEncoder passwordEncoder,
                                    final JwtService jwtService) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
}
