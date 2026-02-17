package com.dro.modules.auth.application;

import com.dro.modules.auth.api.LoginRequest;
import com.dro.modules.auth.api.LoginResponse;
import com.dro.modules.auth.domain.Player;
import com.dro.modules.auth.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final PlayerRepository repository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse execute(LoginRequest request) {

        Player player = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), player.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Token fake por enquanto
        String token = UUID.randomUUID() + ":" + player.getId();

        return new LoginResponse(player.getId(), token);
    }
}
