package com.dro.shared.security;

import com.dro.shared.exception.ApiErrorCode;
import com.dro.shared.exception.dto.ErrorResponse;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final PlayerRepository playerRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            sendError(response, request, HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, "Missing Authorization header");
            return false;
        }

        Map<String, Object> claims;
        UUID playerId;
        try {
            claims = JwtTokenCodec.validateAndReadClaims(
                    authHeader,
                    JwtSettings.getSecret(),
                    JwtSettings.getIssuer()
            );
            playerId = parsePlayerId(claims.get("sub"));
        } catch (Exception e) {
            sendError(response, request, HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, "Invalid or expired token");
            return false;
        }

        var player = playerRepository.findById(playerId);
        if (player.isEmpty() || player.get().getUserType() != UserType.ADMIN) {
            sendError(response, request, HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Access denied: admin only");
            return false;
        }

        return true;
    }

    private UUID parsePlayerId(Object subject) {
        if (!(subject instanceof String value) || value.isBlank()) {
            throw new IllegalArgumentException("Invalid JWT subject");
        }
        return UUID.fromString(value);
    }

    private void sendError(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpStatus status,
            ApiErrorCode code,
            String message
    ) throws Exception {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI()
        );

        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(errorResponse));
    }
}