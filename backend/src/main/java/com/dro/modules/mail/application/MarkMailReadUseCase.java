package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.domain.MailMessageMapper;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Marca mensagens recebidas como lidas.
 *
 * <p>O remetente pode visualizar sua cópia, mas somente o destinatário pode
 * alterar o estado de leitura.</p>
 */
@Service
@RequiredArgsConstructor
public class MarkMailReadUseCase {

    private final MailMessageRepository mailMessageRepository;

    /**
     * Marca a mensagem como lida, sem alterar novamente um horário já definido.
     *
     * @param token token JWT do jogador
     * @param messageId identificador da mensagem recebida
     * @return mensagem após a atualização
     * @throws ConflictException quando a mensagem não existe ou o jogador não é o destinatário
     */
    @Transactional
    public MailMessageResponse execute(String token, UUID messageId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        MailMessage message = mailMessageRepository.findVisibleById(messageId, playerId)
                .orElseThrow(() -> new ConflictException("Mail message not found"));

        if (!message.belongsToRecipient(playerId)) {
            throw new ConflictException("Only the recipient can mark a message as read");
        }

        if (message.getReadAt() == null) {
            message.setReadAt(LocalDateTime.now());
            mailMessageRepository.save(message);
        }

        return MailMessageMapper.toResponse(message);
    }
}
