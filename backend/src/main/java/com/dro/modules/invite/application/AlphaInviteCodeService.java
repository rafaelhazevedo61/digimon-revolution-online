package com.dro.modules.invite.application;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;

@Component
public class AlphaInviteCodeService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        return "DRO-ALPHA-" + block() + "-" + block() + "-" + block() + "-" + block();
    }

    public String hash(String rawCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(normalize(rawCode).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    public String hint(String rawCode) {
        String normalized = normalize(rawCode);
        return normalized.substring(Math.max(0, normalized.length() - 9));
    }

    private String block() {
        StringBuilder value = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            value.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return value.toString();
    }

    private String normalize(String rawCode) {
        if (rawCode == null) {
            return "";
        }
        return rawCode.trim().toUpperCase(Locale.ROOT);
    }
}
