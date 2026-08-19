package com.dro.modules.mail.application;

import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Exclui somente a cópia da mensagem pertencente ao jogador autenticado.
 *
 * <p>A exclusão é lógica e independente: a cópia do remetente e a cópia do
 * destinatário podem desaparecer em momentos diferentes.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteMailMessageUseCase {

    private final MailMessageRepository mailMessageRepository;

    /**
     * Marca como excluída a cópia visível ao jogador.
     *
     * @param token token JWT do jogador
     * @param messageId identificador da mensagem
     * @throws ConflictException quando a mensagem não está visível para o jogador
     */
    @Transactional
    public void execute(String token, UUID messageId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        MailMessage message = mailMessageRepository.findVisibleById(messageId, playerId)
                .orElseThrow(() -> new ConflictException("Mail message not found"));

        if (message.belongsToRecipient(playerId)) {
            message.setRecipientDeleted(true);
        } else if (message.belongsToSender(playerId)) {
            message.setSenderDeleted(true);
        } else {
            throw new ConflictException("Mail message not found");
        }

        mailMessageRepository.save(message);
    }
}
