package com.dro.modules.auth.application;

import com.dro.modules.auth.api.dto.request.LoginRequest;
import com.dro.modules.auth.api.dto.response.LoginResponse;
import com.dro.modules.auth.domain.exception.InvalidCredentialsException;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private PlayerRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private Player createPlayer() {
        return Player.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@email.com")
                .password("encoded_password")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void execute_returnsToken_whenCredentialsValid() {
        Player player = createPlayer();
        when(repository.findByEmail("test@email.com")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        LoginResponse response = loginUseCase.execute(new LoginRequest("test@email.com", "password123"));

        assertNotNull(response.token());
        assertEquals(player.getId(), response.playerId());
        assertTrue(response.token().contains(player.getId().toString()));
    }

    @Test
    void execute_throwsInvalidCredentials_whenEmailNotFound() {
        when(repository.findByEmail("wrong@email.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> loginUseCase.execute(new LoginRequest("wrong@email.com", "password123")));
    }

    @Test
    void execute_throwsInvalidCredentials_whenPasswordWrong() {
        Player player = createPlayer();
        when(repository.findByEmail("test@email.com")).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> loginUseCase.execute(new LoginRequest("test@email.com", "wrongpassword")));
    }
}
