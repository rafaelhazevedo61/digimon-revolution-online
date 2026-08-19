package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.domain.MailMessageMapper;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Recupera uma mensagem visível para o jogador autenticado.
 *
 * <p>A consulta aceita a cópia do remetente ou do destinatário enquanto ela não
 * tiver sido excluída pela respectiva parte.</p>
 */
@Service
@RequiredArgsConstructor
public class GetMailMessageUseCase {

    private final MailMessageRepository mailMessageRepository;

    /**
     * Busca a mensagem detalhada pelo identificador.
     *
     * @param token token JWT do jogador
     * @param messageId identificador da mensagem
     * @return mensagem visível convertida para resposta da API
     * @throws ConflictException quando a mensagem não está visível para o jogador
     */
    @Transactional(readOnly = true)
    public MailMessageResponse execute(String token, UUID messageId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        return mailMessageRepository.findVisibleById(messageId, playerId)
                .map(MailMessageMapper::toResponse)
                .orElseThrow(() -> new ConflictException("Mail message not found"));
    }
}
