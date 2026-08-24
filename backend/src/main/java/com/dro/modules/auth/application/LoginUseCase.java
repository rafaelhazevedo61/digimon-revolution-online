package com.dro.modules.auth.application;

import com.dro.modules.auth.api.dto.request.LoginRequest;
import com.dro.modules.auth.api.dto.response.LoginResponse;
import com.dro.modules.auth.domain.exception.InvalidCredentialsException;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Autenticação.
 */
@Service
public class LoginUseCase {
    private final PlayerRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse execute(LoginRequest request) {
        Player player = repository.findByEmail(request.email()).orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), player.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(player);
        return new LoginResponse(player.getId(), token);
    }

    public LoginUseCase(final PlayerRepository repository, final PasswordEncoder passwordEncoder, final JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
}
