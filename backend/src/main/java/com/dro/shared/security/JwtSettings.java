package com.dro.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtSettings {

    private static final String DEFAULT_SECRET = "dro-local-development-secret-change-this-before-production-2026";
    private static final String DEFAULT_ISSUER = "digimon-revolution-online";
    private static final long DEFAULT_EXPIRATION_MINUTES = 1440L;

    private static String secret = readEnv("DRO_JWT_SECRET", DEFAULT_SECRET);
    private static String issuer = readEnv("DRO_JWT_ISSUER", DEFAULT_ISSUER);
    private static long expirationMinutes = readLongEnv("DRO_JWT_EXPIRATION_MINUTES", DEFAULT_EXPIRATION_MINUTES);

    public JwtSettings(
            @Value("${dro.security.jwt.secret:${DRO_JWT_SECRET:dro-local-development-secret-change-this-before-production-2026}}") String configuredSecret,
            @Value("${dro.security.jwt.issuer:${DRO_JWT_ISSUER:digimon-revolution-online}}") String configuredIssuer,
            @Value("${dro.security.jwt.expiration-minutes:${DRO_JWT_EXPIRATION_MINUTES:1440}}") long configuredExpirationMinutes
    ) {
        secret = configuredSecret;
        issuer = configuredIssuer;
        expirationMinutes = configuredExpirationMinutes;
    }

    public static String getSecret() {
        return secret;
    }

    public static String getIssuer() {
        return issuer;
    }

    public static long getExpirationMinutes() {
        return expirationMinutes;
    }

    private static String readEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static long readLongEnv(String name, long defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}