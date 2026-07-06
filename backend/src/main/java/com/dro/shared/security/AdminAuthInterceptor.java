package com.dro.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization header");
            return false;
        }

        try {
            Map<String, Object> claims = JwtTokenCodec.validateAndReadClaims(
                    authHeader,
                    JwtSettings.getSecret(),
                    JwtSettings.getIssuer()
            );

            Object userType = claims.get("userType");
            if (!"ADMIN".equals(userType)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied: admin only");
                return false;
            }

            return true;
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return false;
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Map.of("message", message)));
    }
}
