package com.dro.modules.admin.application;

import com.dro.modules.admin.api.dto.AdminAnnouncementRequest;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Administração.
 */
@Service
@RequiredArgsConstructor
public class CreateAdminAnnouncementUseCase {

    private final PlayerRepository playerRepository;
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    @Transactional
    public int execute(String token, AdminAnnouncementRequest request) {
        UUID adminId = TokenExtractor.extractPlayerId(token);
        var admin = playerRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Administrador não encontrado."));
        if (admin.getUserType() != UserType.ADMIN) {
            throw new ForbiddenException("Somente administradores podem enviar comunicados.");
        }

        UUID announcementId = UUID.randomUUID();
        int delivered = 0;
        for (var player : playerRepository.findAll()) {
            createSystemMailMessageUseCase.create(
                    MailMessageType.ADMIN,
                    "ADMIN_ANNOUNCEMENT",
                    player.getId(),
                    announcementId,
                    null,
                    request.subject(),
                    request.body(),
                    "admin:announcement:" + announcementId + ":" + player.getId()
            );
            delivered++;
        }
        return delivered;
    }
}
