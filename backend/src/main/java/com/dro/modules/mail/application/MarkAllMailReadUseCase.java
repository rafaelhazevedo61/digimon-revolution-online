package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.response.MarkAllMailReadResponse;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Marca em lote as mensagens comuns recebidas como lidas.
 *
 * <p>Mensagens com ação pendente permanecem não lidas para que convites e
 * recompensas disponíveis não sejam ocultados pela ação em lote.</p>
 */
@Service
public class MarkAllMailReadUseCase {
    private final MailMessageRepository mailMessageRepository;

    /**
     * Marca as mensagens elegíveis do destinatário autenticado.
     *
     * @param token token JWT do jogador
     * @return quantidade de mensagens atualizadas
     */
    @Transactional
    public MarkAllMailReadResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        int markedCount = mailMessageRepository.markAllEligibleAsRead(playerId, LocalDateTime.now());
        return new MarkAllMailReadResponse(markedCount);
    }

    public MarkAllMailReadUseCase(final MailMessageRepository mailMessageRepository) {
        this.mailMessageRepository = mailMessageRepository;
    }
}
