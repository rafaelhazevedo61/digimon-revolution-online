package com.dro.modules.admin.application;

import com.dro.modules.admin.api.dto.AdminAnnouncementRequest;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.player.infra.PlayerRepository;
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

    /**
     * Envia uma mensagem administrativa a todos os jogadores persistidos.
     *
     * <p>A autorização {@code ADMIN} é aplicada pelo
     * {@code AdminAuthInterceptor} antes da entrada no controller.</p>
     *
     * @param request assunto e conteúdo do comunicado
     * @return quantidade de jogadores que receberam a mensagem
     */
    @Transactional
    public int execute(AdminAnnouncementRequest request) {
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
