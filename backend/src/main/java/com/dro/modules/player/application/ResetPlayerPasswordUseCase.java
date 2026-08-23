package com.dro.modules.player.application;

import com.dro.modules.player.api.dto.request.ResetPlayerPasswordRequest;
import com.dro.modules.player.api.dto.response.ResetPlayerPasswordResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.AdminAuditService;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
@Service
public class ResetPlayerPasswordUseCase {

    private static final String RANDOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int RANDOM_LENGTH = 12;

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService adminAuditService;

    public ResetPlayerPasswordUseCase(
            PlayerRepository playerRepository,
            PasswordEncoder passwordEncoder,
            AdminAuditService adminAuditService
    ) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminAuditService = adminAuditService;
    }

    @Transactional
    public ResetPlayerPasswordResponse execute(
            String authorization,
            UUID playerId,
            ResetPlayerPasswordRequest request
    ) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        String plainPassword = resolvePassword(request);
        String encodedPassword = passwordEncoder.encode(plainPassword);

        player.setPassword(encodedPassword);
        player.incrementTokenVersion();
        playerRepository.save(player);
        adminAuditService.success(
                authorization,
                "ADMIN_PLAYER_PASSWORD_RESET",
                "Player",
                player.getId().toString(),
                "reset-password",
                "Senha de jogador redefinida",
                Map.of(
                        "targetPlayerId", player.getId().toString(),
                        "targetUsername", player.getUsername(),
                        "tokenVersion", player.getTokenVersion(),
                        "generated", request.generateRandom()
                )
        );

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
            throw new BadRequestException(
                    "A nova senha é obrigatória quando a geração automática não estiver habilitada."
            );
        }

        if (request.newPassword().length() < 8 || request.newPassword().length() > 60) {
            throw new BadRequestException(
                    "A senha deve ter entre 8 e 60 caracteres."
            );
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
