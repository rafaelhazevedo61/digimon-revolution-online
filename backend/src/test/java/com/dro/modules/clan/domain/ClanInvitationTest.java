package com.dro.modules.clan.domain;

import com.dro.modules.player.domain.Player;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClanInvitationTest {

    @Test
    void isPendingAt_returnsTrueOnlyBeforeExpirationWhilePending() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 19, 10, 0);
        ClanInvitation invitation = ClanInvitation.builder()
                .id(UUID.randomUUID())
                .clan(Clan.builder().id(UUID.randomUUID()).name("DRO").tag("DRO").leaderId(UUID.randomUUID()).build())
                .inviter(Player.builder().id(UUID.randomUUID()).username("lider").build())
                .invitee(Player.builder().id(UUID.randomUUID()).username("jogador").build())
                .status(ClanInvitationStatus.PENDING)
                .createdAt(createdAt)
                .expiresAt(createdAt.plusDays(7))
                .build();

        assertTrue(invitation.isPendingAt(createdAt.plusDays(1)));
        assertFalse(invitation.isPendingAt(createdAt.plusDays(7)));

        invitation.setStatus(ClanInvitationStatus.ACCEPTED);
        assertFalse(invitation.isPendingAt(createdAt.plusDays(1)));
    }
}
