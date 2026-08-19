package com.dro.modules.mail.application;

import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Obtém a quantidade de mensagens recebidas ainda não lidas.
 *
 * <p>A contagem considera somente a cópia do destinatário e ignora mensagens
 * que ele já excluiu.</p>
 */
@Service
@RequiredArgsConstructor
public class GetUnreadMailCountUseCase {

    private final MailMessageRepository mailMessageRepository;

    /**
     * Conta mensagens não lidas do jogador autenticado.
     *
     * @param token token JWT do jogador
     * @return quantidade de mensagens ainda não abertas
     */
    public long execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        return mailMessageRepository.countByRecipientIdAndRecipientDeletedFalseAndReadAtIsNull(playerId);
    }
}
