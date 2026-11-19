package com.dro.shared.security;

import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthInterceptorTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUpResponseWriter() throws Exception {
        lenient().when(request.getRequestURI()).thenReturn("/admin/test");
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    @Test
    void allowsAdminWhenDatabaseStillMarksPlayerAsAdmin() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(token(playerId));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId, UserType.ADMIN)));

        assertThat(new AdminAuthInterceptor(playerRepository).preHandle(request, response, null)).isTrue();
    }

    @Test
    void rejectsPlayerAfterDatabaseDemotionEvenWithAdminClaim() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(tokenWithAdminClaim(playerId));
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player(playerId, UserType.PLAYER)));

        assertThat(new AdminAuthInterceptor(playerRepository).preHandle(request, response, null)).isFalse();
        verify(response).setStatus(403);
    }

    @Test
    void rejectsMissingPlayerAsForbidden() throws Exception {
        UUID playerId = UUID.randomUUID();
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(tokenWithAdminClaim(playerId));
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        assertThat(new AdminAuthInterceptor(playerRepository).preHandle(request, response, null)).isFalse();
        verify(response).setStatus(403);
    }

    @Test
    void rejectsMissingAuthorizationAsUnauthorized() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThat(new AdminAuthInterceptor(playerRepository).preHandle(request, response, null)).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void rejectsInvalidTokenAsUnauthorized() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");

        assertThat(new AdminAuthInterceptor(playerRepository).preHandle(request, response, null)).isFalse();
        verify(response).setStatus(401);
    }

    @Test
    void keepsOptionsBypass() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");

        assertThat(new AdminAuthInterceptor(playerRepository).preHandle(request, response, null)).isTrue();
    }

    private Player player(UUID id, UserType type) {
        return Player.builder().id(id).username("admin").userType(type).build();
    }

    private String token(UUID playerId) {
        return tokenWithAdminClaim(playerId);
    }

    private String tokenWithAdminClaim(UUID playerId) {
        return JwtTokenCodec.create(
                Map.of(
                        "sub", playerId.toString(),
                        "userType", "ADMIN",
                        "iss", JwtSettings.getIssuer(),
                        "exp", Instant.now().getEpochSecond() + 3600
                ),
                JwtSettings.getSecret()
        );
    }
}
