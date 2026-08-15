package com.dro.modules.player.application;

import com.dro.modules.player.api.dto.request.ResetPlayerPasswordRequest;
import com.dro.modules.player.api.dto.response.ResetPlayerPasswordResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPlayerPasswordUseCase {

    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_LENGTH = 12;

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResetPlayerPasswordResponse execute(UUID playerId, ResetPlayerPasswordRequest request) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        String plainPassword = resolvePassword(request);
        String encodedPassword = passwordEncoder.encode(plainPassword);

        player.setPassword(encodedPassword);
        playerRepository.save(player);

        return new ResetPlayerPasswordResponse(
                player.getId(),
                player.getUsername(),
                plainPassword,
                request.generateRandom()
        );
    }

    private String resolvePassword(ResetPlayerPasswordRequest request) {
        if (request.generateRandom()) {
            return generateRandomPassword();
        }

        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new BadRequestException("New password is required when not generating a random password");
        }

        if (request.newPassword().length() < 4 || request.newPassword().length() > 60) {
            throw new BadRequestException("Password must be between 4 and 60 characters");
        }

        return request.newPassword();
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(RANDOM_LENGTH);

        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = random.nextInt(RANDOM_CHARS.length());
            sb.append(RANDOM_CHARS.charAt(index));
        }

        return sb.toString();
    }
}
