package com.dro.modules.auth.application;

import com.dro.modules.auth.api.RegisterRequest;
import com.dro.modules.auth.domain.Player;
import com.dro.modules.auth.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final PlayerRepository repository;

    public void execute(RegisterRequest request) {

        if (repository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        if (repository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already taken");
        }

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(request.password()) // depois vamos criptografar
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(player);
    }
}
