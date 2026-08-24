package com.dro.modules.auth.application;

import com.dro.modules.auth.api.dto.request.RegisterRequest;
import com.dro.modules.auth.domain.exception.EmailAlreadyRegisteredException;
import com.dro.modules.auth.domain.exception.UsernameAlreadyTakenException;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private PlayerRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUseCase registerUseCase;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest("testuser", "test@email.com", "password123", null);
    }

    @Test
    void execute_savesPlayer_whenValid() {
        when(repository.existsByEmail("test@email.com")).thenReturn(false);
        when(repository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");

        registerUseCase.execute(validRequest);

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(repository).save(captor.capture());

        Player saved = captor.getValue();
        assertEquals("testuser", saved.getUsername());
        assertEquals("test@email.com", saved.getEmail());
        assertEquals("encoded_password", saved.getPassword());
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void execute_throwsEmailAlreadyRegistered_whenDuplicateEmail() {
        when(repository.existsByEmail("test@email.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> registerUseCase.execute(validRequest));

        verify(repository, never()).save(any());
    }

    @Test
    void execute_throwsUsernameAlreadyTaken_whenDuplicateUsername() {
        when(repository.existsByEmail("test@email.com")).thenReturn(false);
        when(repository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UsernameAlreadyTakenException.class,
                () -> registerUseCase.execute(validRequest));

        verify(repository, never()).save(any());
    }
}
