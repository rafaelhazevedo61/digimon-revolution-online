package com.dro.modules.player.application;

import com.dro.modules.auth.domain.exception.InvalidCredentialsException;
import com.dro.modules.player.api.dto.request.ChangePasswordRequest;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.security.JwtService;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePlayerPasswordUseCaseTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ChangePlayerPasswordUseCase useCase;

    private String makeToken(UUID playerId) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", playerId.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }

    private Player makePlayer(UUID id) {
        return Player.builder()
                .id(id)
                .username("tamer")
                .password("encoded-old")
                .build();
    }

    @Test
    void execute_changesPassword_whenCurrentPasswordMatches() {
        UUID playerId = UUID.randomUUID();
        Player player = makePlayer(playerId);
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("old-pass", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new");
        when(jwtService.generateToken(player)).thenReturn("new-token");

        var response = useCase.execute(token, new ChangePasswordRequest("old-pass", "new-pass"));

        assertEquals("encoded-new", player.getPassword());
        assertEquals(1, player.getTokenVersion());
        assertEquals("new-token", response.token());
        verify(playerRepository).save(player);
        verify(jwtService).generateToken(player);
    }

    @Test
    void execute_throwsBadRequest_whenNewPasswordEqualsCurrent() {
        UUID playerId = UUID.randomUUID();
        String token = makeToken(playerId);

        assertThrows(BadRequestException.class, () -> useCase.execute(
                token,
                new ChangePasswordRequest("same-pass", "same-pass")
        ));
    }

    @Test
    void execute_throwsInvalidCredentials_whenCurrentPasswordWrong() {
        UUID playerId = UUID.randomUUID();
        Player player = makePlayer(playerId);
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        when(passwordEncoder.matches("wrong-pass", "encoded-old")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> useCase.execute(
                token,
                new ChangePasswordRequest("wrong-pass", "new-pass")
        ));
    }

    @Test
    void execute_throwsNotFound_whenPlayerMissing() {
        UUID playerId = UUID.randomUUID();
        String token = makeToken(playerId);

        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> useCase.execute(
                token,
                new ChangePasswordRequest("old-pass", "new-pass")
        ));
    }
}
