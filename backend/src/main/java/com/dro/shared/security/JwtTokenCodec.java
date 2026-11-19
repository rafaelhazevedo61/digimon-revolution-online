package com.dro.shared.security;

import com.dro.shared.exception.UnauthorizedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JwtTokenCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private JwtTokenCodec() {
    }

    public static String create(Map<String, Object> claims, String secret) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(claims);
            String content = encodedHeader + "." + encodedPayload;
            String signature = sign(content, secret);

            return content + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create JWT token", e);
        }
    }

    public static Map<String, Object> validateAndReadClaims(String rawToken, String secret, String expectedIssuer) {
        try {
            String token = normalize(rawToken);
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                throw new UnauthorizedException("Invalid JWT token");
            }

            String content = parts[0] + "." + parts[1];
            String expectedSignature = sign(content, secret);

            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new UnauthorizedException("Invalid JWT signature");
            }

            Map<String, Object> header = decodeJson(parts[0]);
            if (!"HS256".equals(header.get("alg"))) {
                throw new UnauthorizedException("Invalid JWT algorithm");
            }

            Map<String, Object> claims = decodeJson(parts[1]);
            validateIssuer(claims, expectedIssuer);
            validateExpiration(claims);

            return claims;
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid JWT token");
        }
    }

    public static String normalize(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Missing JWT token");
        }

        String token = rawToken.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        if (token.isBlank()) {
            throw new UnauthorizedException("Missing JWT token");
        }

        return token;
    }

    private static String encodeJson(Map<String, Object> data) throws Exception {
        return BASE64_URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(data));
    }

    private static Map<String, Object> decodeJson(String encodedJson) throws Exception {
        byte[] decoded = BASE64_URL_DECODER.decode(encodedJson);
        return OBJECT_MAPPER.readValue(decoded, MAP_TYPE);
    }

    private static String sign(String content, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private static void validateIssuer(Map<String, Object> claims, String expectedIssuer) {
        Object issuer = claims.get("iss");
        if (expectedIssuer != null && !expectedIssuer.isBlank() && !expectedIssuer.equals(issuer)) {
            throw new UnauthorizedException("Invalid JWT issuer");
        }
    }

    private static void validateExpiration(Map<String, Object> claims) {
        Object exp = claims.get("exp");
        if (!(exp instanceof Number number)) {
            throw new UnauthorizedException("Invalid JWT expiration");
        }

        long expirationEpochSeconds = number.longValue();
        if (Instant.now().getEpochSecond() >= expirationEpochSeconds) {
            throw new UnauthorizedException("Expired JWT token");
        }
    }
}