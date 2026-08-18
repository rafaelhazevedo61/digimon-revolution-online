package com.dro.modules.mail.domain;

import com.dro.modules.player.domain.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailMessageTest {

    @Test
    void systemMessage_isVisibleOnlyToRecipient() {
        UUID recipientId = UUID.randomUUID();
        MailMessage message = MailMessage.builder()
                .id(UUID.randomUUID())
                .sender(null)
                .recipient(Player.builder().id(recipientId).username("destinatario").build())
                .messageType(MailMessageType.AUCTION)
                .subject("Venda concluída")
                .body("Sua venda foi concluída.")
                .build();

        assertTrue(message.isVisibleTo(recipientId));
        assertFalse(message.isVisibleTo(UUID.randomUUID()));
    }

    @Test
    void visibility_isIndependentForSenderAndRecipient() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        MailMessage message = MailMessage.builder()
                .id(UUID.randomUUID())
                .sender(Player.builder().id(senderId).username("remetente").build())
                .recipient(Player.builder().id(recipientId).username("destinatario").build())
                .subject("Assunto")
                .body("Mensagem")
                .build();

        assertTrue(message.isVisibleTo(senderId));
        assertTrue(message.isVisibleTo(recipientId));
        assertFalse(message.isVisibleTo(UUID.randomUUID()));

        message.setSenderDeleted(true);
        assertFalse(message.isVisibleTo(senderId));
        assertTrue(message.isVisibleTo(recipientId));

        message.setRecipientDeleted(true);
        assertFalse(message.isVisibleTo(recipientId));
    }
}
