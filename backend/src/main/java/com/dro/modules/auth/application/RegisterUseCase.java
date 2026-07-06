package com.dro.modules.auth.application;

import com.dro.modules.auth.api.dto.RegisterRequest;
import com.dro.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.dro.modules.auth.domain.exception.UsernameAlreadyTakenException;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final PlayerRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(RegisterRequest request) {

        if (repository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        if (repository.existsByUsername(request.username())) {
            throw new UsernameAlreadyTakenException();
        }

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .maxDigimonSlots(3)
                .maxStorageSlots(50)
                .userType(UserType.PLAYER)
                .build();

        repository.save(player);
    }
}