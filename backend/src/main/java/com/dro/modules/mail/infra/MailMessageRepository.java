package com.dro.modules.mail.infra;

import com.dro.modules.mail.domain.MailMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface MailMessageRepository extends JpaRepository<MailMessage, UUID> {

    Page<MailMessage> findByRecipientIdAndRecipientDeletedFalseOrderByCreatedAtDesc(
            UUID recipientId,
            Pageable pageable
    );

    Page<MailMessage> findBySenderIdAndSenderDeletedFalseOrderByCreatedAtDesc(
            UUID senderId,
            Pageable pageable
    );

    long countByRecipientIdAndRecipientDeletedFalseAndReadAtIsNull(UUID recipientId);

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

    long countBySenderIdAndCreatedAtAfter(UUID senderId, LocalDateTime createdAt);
}
