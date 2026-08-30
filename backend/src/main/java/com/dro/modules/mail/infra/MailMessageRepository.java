package com.dro.modules.mail.infra;

import com.dro.modules.mail.domain.MailMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Consultas e inserções persistentes das mensagens do Correio.
 *
 * <p>As consultas de Entrada e Enviadas respeitam a exclusão independente de
 * cada parte. Mensagens geradas pelo sistema usam {@code deliveryKey} única
 * para que o mesmo comunicado ou evento não seja entregue duas vezes.</p>
 */
public interface MailMessageRepository extends JpaRepository<MailMessage, UUID> {

    /** Lista a Entrada do jogador em ordem decrescente de criação. */
    Page<MailMessage> findByRecipientIdAndRecipientDeletedFalseOrderByCreatedAtDesc(
            UUID recipientId,
            Pageable pageable
    );

    List<MailMessage> findByRecipientIdAndRecipientDeletedFalse(UUID recipientId);

    List<MailMessage> findBySenderIdAndSenderDeletedFalse(UUID senderId);

    /** Lista as mensagens enviadas pelo jogador em ordem decrescente de criação. */
    Page<MailMessage> findBySenderIdAndSenderDeletedFalseOrderByCreatedAtDesc(
            UUID senderId,
            Pageable pageable
    );

    /** Conta mensagens recebidas ainda não abertas pelo jogador. */
    long countByRecipientIdAndRecipientDeletedFalseAndReadAtIsNull(UUID recipientId);

    /**
     * Busca uma mensagem visível para uma das partes sem expor cópias excluídas.
     */
    @Query("""
            SELECT m FROM MailMessage m
            WHERE m.id = :id
              AND ((m.recipient.id = :playerId AND m.recipientDeleted = false)
                OR (m.sender.id = :playerId AND m.senderDeleted = false))
            """)
    Optional<MailMessage> findVisibleById(
            @Param("id") UUID id,
            @Param("playerId") UUID playerId
    );

    /**
     * Marca como lidas todas as mensagens recebidas, exceto premiações de
     * evento que ainda possuem loot pendente para resgate.
     *
     * @return quantidade de mensagens atualizadas
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE MailMessage m
            SET m.readAt = :readAt
            WHERE m.recipient.id = :recipientId
              AND m.recipientDeleted = false
              AND m.readAt IS NULL
              AND (
                    m.actionType IS NULL
                    OR m.sourceType IS NULL
                    OR m.sourceType <> 'EVENT_REWARD'
                    OR m.actionType <> 'EVENT_REWARD_CLAIM'
                  )
            """)
    int markAllEligibleAsRead(
            @Param("recipientId") UUID recipientId,
            @Param("readAt") LocalDateTime readAt
    );

    /** Conta mensagens comuns enviadas depois do instante usado pelo rate limit. */
    long countBySenderIdAndCreatedAtAfter(UUID senderId, LocalDateTime createdAt);

    /** Localiza uma mensagem do sistema pela chave idempotente de entrega. */
    Optional<MailMessage> findByDeliveryKey(String deliveryKey);

    /**
     * Insere uma mensagem do sistema sem duplicá-la para a mesma {@code deliveryKey}.
     *
     * @return {@code 1} quando criada; {@code 0} quando a chave já existe
     */
    @Modifying
    @Query(value = """
            INSERT INTO mail_messages (
                id, sender_player_id, recipient_player_id, message_type,
                subject, body, created_at, source_type, source_id,
                action_type, action_payload, delivery_key
            ) VALUES (
                :id, NULL, :recipientId, :messageType,
                :subject, :body, :createdAt, :sourceType, :sourceId,
                :actionType, :actionPayload, :deliveryKey
            )
            ON CONFLICT (delivery_key) DO NOTHING
            """, nativeQuery = true)
    int insertSystemMessage(
            @Param("id") UUID id,
            @Param("recipientId") UUID recipientId,
            @Param("messageType") String messageType,
            @Param("subject") String subject,
            @Param("body") String body,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("sourceType") String sourceType,
            @Param("sourceId") UUID sourceId,
            @Param("actionType") String actionType,
            @Param("actionPayload") String actionPayload,
            @Param("deliveryKey") String deliveryKey
    );
}
