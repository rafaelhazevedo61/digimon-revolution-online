package com.dro.modules.mail.domain;

import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mail_messages", indexes = {
        @Index(name = "idx_mail_recipient_inbox", columnList = "recipient_player_id, recipient_deleted, created_at"),
        @Index(name = "idx_mail_sender_sent", columnList = "sender_player_id, sender_deleted, created_at"),
        @Index(name = "idx_mail_unread", columnList = "recipient_player_id, read_at, recipient_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private MailMessageType messageType = MailMessageType.PLAYER;

    @Column(nullable = false, length = 80)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sender_deleted", nullable = false)
    @Builder.Default
    private boolean senderDeleted = false;

    @Column(name = "recipient_deleted", nullable = false)
    @Builder.Default
    private boolean recipientDeleted = false;

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

    public boolean isVisibleTo(UUID playerId) {
        if (playerId == null) return false;
        if (recipient != null && recipient.getId().equals(playerId)) return !recipientDeleted;
        return sender != null && sender.getId().equals(playerId) && !senderDeleted;
    }

    public boolean belongsToRecipient(UUID playerId) {
        return recipient != null && recipient.getId().equals(playerId);
    }

    public boolean belongsToSender(UUID playerId) {
        return sender != null && sender.getId().equals(playerId);
    }
}
