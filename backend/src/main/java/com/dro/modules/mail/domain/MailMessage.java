package com.dro.modules.mail.domain;

import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mensagem persistente do Correio, enviada por um jogador ou gerada pelo sistema.
 *
 * <p>A exclusão é mantida separadamente para remetente e destinatário. Assim,
 * apagar uma cópia não remove a cópia pertencente à outra parte. Mensagens do
 * sistema podem carregar uma origem, uma ação e uma {@code deliveryKey} para
 * permitir processamento controlado e idempotente.</p>
 */
@Entity
@Table(name = "mail_messages", indexes = {@Index(name = "idx_mail_recipient_inbox", columnList = "recipient_player_id, recipient_deleted, created_at"), @Index(name = "idx_mail_sender_sent", columnList = "sender_player_id, sender_deleted, created_at"), @Index(name = "idx_mail_unread", columnList = "recipient_player_id, read_at, recipient_deleted")})
public class MailMessage {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_player_id")
    private Player sender;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_player_id", nullable = false)
    private Player recipient;
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 32)
    private MailMessageType messageType;
    @Column(nullable = false, length = 80)
    private String subject;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "read_at")
    private LocalDateTime readAt;
    @Column(name = "sender_deleted", nullable = false)
    private boolean senderDeleted;
    @Column(name = "recipient_deleted", nullable = false)
    private boolean recipientDeleted;
    @Column(name = "source_type", length = 64)
    private String sourceType;
    @Column(name = "source_id")
    private UUID sourceId;
    @Column(name = "action_type", length = 64)
    private String actionType;
    @Column(name = "action_payload", columnDefinition = "TEXT")
    private String actionPayload;
    @Column(name = "delivery_key", length = 128, unique = true)
    private String deliveryKey;

    /**
     * Verifica se a mensagem ainda pode ser consultada pelo jogador informado.
     *
     * <p>O remetente e o destinatário possuem cópias independentes. A mensagem
     * só fica invisível para uma parte quando essa parte exclui sua própria
     * cópia.</p>
     *
     * @param playerId jogador que deseja consultar a mensagem
     * @return {@code true} quando o jogador é uma das partes e sua cópia não foi excluída
     */
    public boolean isVisibleTo(UUID playerId) {
        if (playerId == null) return false;
        if (recipient != null && recipient.getId().equals(playerId)) return !recipientDeleted;
        return sender != null && sender.getId().equals(playerId) && !senderDeleted;
    }

    /**
     * Verifica se o jogador é o destinatário da mensagem.
     *
     * <p>Essa verificação não considera a exclusão da cópia e é usada para
     * autorizar ações que somente o destinatário pode executar.</p>
     *
     * @param playerId jogador a ser verificado
     * @return {@code true} quando o jogador é o destinatário
     */
    public boolean belongsToRecipient(UUID playerId) {
        return recipient != null && recipient.getId().equals(playerId);
    }

    /**
     * Verifica se o jogador é o remetente da mensagem.
     *
     * @param playerId jogador a ser verificado
     * @return {@code true} quando o jogador é o remetente
     */
    public boolean belongsToSender(UUID playerId) {
        return sender != null && sender.getId().equals(playerId);
    }

    private static MailMessageType $default$messageType() {
        return MailMessageType.PLAYER;
    }

    private static boolean $default$senderDeleted() {
        return false;
    }

    private static boolean $default$recipientDeleted() {
        return false;
    }


    public static class MailMessageBuilder {
        private UUID id;
        private Player sender;
        private Player recipient;
        private boolean messageType$set;
        private MailMessageType messageType$value;
        private String subject;
        private String body;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;
        private boolean senderDeleted$set;
        private boolean senderDeleted$value;
        private boolean recipientDeleted$set;
        private boolean recipientDeleted$value;
        private String sourceType;
        private UUID sourceId;
        private String actionType;
        private String actionPayload;
        private String deliveryKey;

        MailMessageBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder sender(final Player sender) {
            this.sender = sender;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder recipient(final Player recipient) {
            this.recipient = recipient;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder messageType(final MailMessageType messageType) {
            this.messageType$value = messageType;
            messageType$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder body(final String body) {
            this.body = body;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder readAt(final LocalDateTime readAt) {
            this.readAt = readAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder senderDeleted(final boolean senderDeleted) {
            this.senderDeleted$value = senderDeleted;
            senderDeleted$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder recipientDeleted(final boolean recipientDeleted) {
            this.recipientDeleted$value = recipientDeleted;
            recipientDeleted$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder sourceType(final String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder sourceId(final UUID sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder actionType(final String actionType) {
            this.actionType = actionType;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder actionPayload(final String actionPayload) {
            this.actionPayload = actionPayload;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MailMessage.MailMessageBuilder deliveryKey(final String deliveryKey) {
            this.deliveryKey = deliveryKey;
            return this;
        }

        public MailMessage build() {
            MailMessageType messageType$value = this.messageType$value;
            if (!this.messageType$set) messageType$value = MailMessage.$default$messageType();
            boolean senderDeleted$value = this.senderDeleted$value;
            if (!this.senderDeleted$set) senderDeleted$value = MailMessage.$default$senderDeleted();
            boolean recipientDeleted$value = this.recipientDeleted$value;
            if (!this.recipientDeleted$set) recipientDeleted$value = MailMessage.$default$recipientDeleted();
            return new MailMessage(this.id, this.sender, this.recipient, messageType$value, this.subject, this.body, this.createdAt, this.readAt, senderDeleted$value, recipientDeleted$value, this.sourceType, this.sourceId, this.actionType, this.actionPayload, this.deliveryKey);
        }

        @Override
        public String toString() {
            return "MailMessage.MailMessageBuilder(id=" + this.id + ", sender=" + this.sender + ", recipient=" + this.recipient + ", messageType$value=" + this.messageType$value + ", subject=" + this.subject + ", body=" + this.body + ", createdAt=" + this.createdAt + ", readAt=" + this.readAt + ", senderDeleted$value=" + this.senderDeleted$value + ", recipientDeleted$value=" + this.recipientDeleted$value + ", sourceType=" + this.sourceType + ", sourceId=" + this.sourceId + ", actionType=" + this.actionType + ", actionPayload=" + this.actionPayload + ", deliveryKey=" + this.deliveryKey + ")";
        }
    }

    public static MailMessage.MailMessageBuilder builder() {
        return new MailMessage.MailMessageBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public Player getSender() {
        return this.sender;
    }

    public Player getRecipient() {
        return this.recipient;
    }

    public MailMessageType getMessageType() {
        return this.messageType;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getBody() {
        return this.body;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getReadAt() {
        return this.readAt;
    }

    public boolean isSenderDeleted() {
        return this.senderDeleted;
    }

    public boolean isRecipientDeleted() {
        return this.recipientDeleted;
    }

    public String getSourceType() {
        return this.sourceType;
    }

    public UUID getSourceId() {
        return this.sourceId;
    }

    public String getActionType() {
        return this.actionType;
    }

    public String getActionPayload() {
        return this.actionPayload;
    }

    public String getDeliveryKey() {
        return this.deliveryKey;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setSender(final Player sender) {
        this.sender = sender;
    }

    public void setRecipient(final Player recipient) {
        this.recipient = recipient;
    }

    public void setMessageType(final MailMessageType messageType) {
        this.messageType = messageType;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReadAt(final LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public void setSenderDeleted(final boolean senderDeleted) {
        this.senderDeleted = senderDeleted;
    }

    public void setRecipientDeleted(final boolean recipientDeleted) {
        this.recipientDeleted = recipientDeleted;
    }

    public void setSourceType(final String sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceId(final UUID sourceId) {
        this.sourceId = sourceId;
    }

    public void setActionType(final String actionType) {
        this.actionType = actionType;
    }

    public void setActionPayload(final String actionPayload) {
        this.actionPayload = actionPayload;
    }

    public void setDeliveryKey(final String deliveryKey) {
        this.deliveryKey = deliveryKey;
    }

    public MailMessage() {
        this.messageType = MailMessage.$default$messageType();
        this.senderDeleted = MailMessage.$default$senderDeleted();
        this.recipientDeleted = MailMessage.$default$recipientDeleted();
    }

    public MailMessage(final UUID id, final Player sender, final Player recipient, final MailMessageType messageType, final String subject, final String body, final LocalDateTime createdAt, final LocalDateTime readAt, final boolean senderDeleted, final boolean recipientDeleted, final String sourceType, final UUID sourceId, final String actionType, final String actionPayload, final String deliveryKey) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.messageType = messageType;
        this.subject = subject;
        this.body = body;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.senderDeleted = senderDeleted;
        this.recipientDeleted = recipientDeleted;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.actionType = actionType;
        this.actionPayload = actionPayload;
        this.deliveryKey = deliveryKey;
    }
}
