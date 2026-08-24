package com.dro.modules.auth.application;

import com.dro.modules.auth.api.dto.request.RegisterRequest;
import com.dro.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.dro.modules.auth.domain.exception.UsernameAlreadyTakenException;
import com.dro.modules.invite.application.ValidateAlphaInviteUseCase;
import com.dro.modules.invite.domain.AlphaInvite;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Autenticação.
 */
@Service
public class RegisterUseCase {

    private final PlayerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ValidateAlphaInviteUseCase validateAlphaInviteUseCase;

    private boolean inviteRequired;

    public RegisterUseCase (PlayerRepository repository, PasswordEncoder passwordEncoder, ValidateAlphaInviteUseCase validateAlphaInviteUseCase) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.validateAlphaInviteUseCase = validateAlphaInviteUseCase;
    }

    @Transactional
    public void execute (RegisterRequest request) {

        AlphaInvite invite = null;
        if (inviteRequired || (request.inviteCode() != null && !request.inviteCode().isBlank())) {
            invite = validateAlphaInviteUseCase.lockValidInvite(request.inviteCode(), request.email());
        }

        if (repository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        if (repository.existsByUsername(request.username())) {
            throw new UsernameAlreadyTakenException();
        }

        Player player = Player.createPlayer(
                UUID.randomUUID(),
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                LocalDateTime.now()
        );

        repository.save(player);

        if (invite != null) {
            invite.markUsed(player.getId(), LocalDateTime.now());
        }
    }


}