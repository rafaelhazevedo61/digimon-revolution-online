package com.dro.modules.mail.application;

import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.mail.domain.MailRules;
import com.dro.modules.mail.infra.MailMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cria mensagens geradas por sistemas do jogo ou por operações administrativas.
 *
 * <p>Mensagens do sistema não possuem remetente jogador e são inseridas com uma
 * {@code deliveryKey} única. Repetir a mesma operação não cria uma segunda
 * mensagem para o destinatário.</p>
 */
@Service
public class CreateSystemMailMessageUseCase {
    private final MailMessageRepository mailMessageRepository;

    /**
     * Cria uma notificação de Casa de Leilões usando a origem informada.
     *
     * @param recipientId jogador que receberá a notificação
     * @param sourceId identificador do anúncio ou operação relacionada
     * @param actionType ação opcional disponível na mensagem
     * @param subject assunto da notificação
     * @param body corpo da notificação
     * @param deliveryKey chave única da entrega
     * @return mensagem persistida
     */
    @Transactional
    public MailMessage createAuctionNotification(UUID recipientId, UUID sourceId, String actionType, String subject, String body, String deliveryKey) {
        return create(MailMessageType.AUCTION, "AUCTION", recipientId, sourceId, actionType, subject, body, deliveryKey);
    }

    /**
     * Cria uma mensagem especial e a recupera pela chave de entrega.
     *
     * @param messageType tipo especial da mensagem, nunca {@code PLAYER}
     * @param sourceType tipo da origem do sistema
     * @param recipientId jogador destinatário
     * @param sourceId identificador opcional da origem
     * @param actionType ação opcional disponível no Correio
     * @param subject assunto da mensagem
     * @param body corpo da mensagem
     * @param deliveryKey chave idempotente com até 128 caracteres
     * @return mensagem criada ou já existente para a chave informada
     * @throws IllegalArgumentException quando os campos não respeitam as regras do Correio
     */
    @Transactional
    public MailMessage create(MailMessageType messageType, String sourceType, UUID recipientId, UUID sourceId, String actionType, String subject, String body, String deliveryKey) {
        validate(messageType, sourceType, recipientId, actionType, subject, body, deliveryKey);
        UUID messageId = UUID.randomUUID();
        mailMessageRepository.insertSystemMessage(messageId, recipientId, messageType.name(), subject.trim(), body.trim(), LocalDateTime.now(), sourceType, sourceId, actionType, null, deliveryKey);
        return mailMessageRepository.findByDeliveryKey(deliveryKey).orElseThrow(() -> new IllegalStateException("System mail message could not be persisted"));
    }

    /**
     * Valida os campos compartilhados por todas as mensagens do sistema.
     */
    private void validate(MailMessageType messageType, String sourceType, UUID recipientId, String actionType, String subject, String body, String deliveryKey) {
        if (messageType == null || messageType == MailMessageType.PLAYER) {
            throw new IllegalArgumentException("System mail requires a non-player message type");
        }
        if (sourceType == null || sourceType.isBlank() || sourceType.length() > MailRules.SOURCE_TYPE_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail source type");
        }
        if (recipientId == null) {
            throw new IllegalArgumentException("System mail recipient is required");
        }
        if (actionType != null && actionType.length() > MailRules.ACTION_TYPE_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail action type");
        }
        if (subject == null || subject.trim().isEmpty() || subject.trim().length() > MailRules.SUBJECT_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail subject");
        }
        if (body == null || body.trim().isEmpty() || body.trim().length() > MailRules.BODY_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail body");
        }
        if (deliveryKey == null || deliveryKey.isBlank() || deliveryKey.length() > MailRules.DELIVERY_KEY_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail delivery key");
        }
    }

    public CreateSystemMailMessageUseCase(final MailMessageRepository mailMessageRepository) {
        this.mailMessageRepository = mailMessageRepository;
    }
}
