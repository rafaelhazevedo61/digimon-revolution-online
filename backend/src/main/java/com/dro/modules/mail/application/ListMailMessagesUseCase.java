package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.response.MailMessagePageResponse;
import com.dro.modules.mail.domain.MailMessageMapper;
import com.dro.modules.mail.domain.MailRules;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Lista as cópias de mensagens visíveis para o jogador autenticado.
 *
 * <p>A Entrada e a caixa Enviadas usam consultas distintas e respeitam a
 * exclusão independente. O tamanho solicitado é limitado a
 * {@link MailRules#MAX_PAGE_SIZE}.</p>
 */
@Service
@RequiredArgsConstructor
public class ListMailMessagesUseCase {

    private final MailMessageRepository mailMessageRepository;

    /**
     * Lista mensagens recebidas, da mais recente para a mais antiga.
     *
     * @param token token JWT do jogador
     * @param page índice da página, normalizado para não ser negativo
     * @param size tamanho solicitado, limitado pelas regras do Correio
     * @return página com resumos das mensagens recebidas
     */
    @Transactional(readOnly = true)
    public MailMessagePageResponse inbox(String token, int page, int size) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        PageRequest pageable = pageRequest(page, size);
        return toPage(mailMessageRepository
                .findByRecipientIdAndRecipientDeletedFalseOrderByCreatedAtDesc(playerId, pageable));
    }

    /**
     * Lista mensagens enviadas pelo jogador, da mais recente para a mais antiga.
     *
     * @param token token JWT do jogador
     * @param page índice da página
     * @param size tamanho solicitado
     * @return página com resumos das mensagens enviadas
     */
    @Transactional(readOnly = true)
    public MailMessagePageResponse sent(String token, int page, int size) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        PageRequest pageable = pageRequest(page, size);
        return toPage(mailMessageRepository
                .findBySenderIdAndSenderDeletedFalseOrderByCreatedAtDesc(playerId, pageable));
    }

    /** Normaliza página e tamanho para os limites aceitos pelo Correio. */
    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size <= 0 ? 20 : size, MailRules.MAX_PAGE_SIZE));
        return PageRequest.of(safePage, safeSize);
    }

    private MailMessagePageResponse toPage(Page<com.dro.modules.mail.domain.MailMessage> page) {
        return new MailMessagePageResponse(
                page.getContent().stream().map(MailMessageMapper::toSummary).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
