package com.dro.modules.clan.domain;

import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clan_invitations", indexes = {
        @Index(name = "idx_clan_invitation_invitee", columnList = "invitee_player_id, status, expires_at"),
        @Index(name = "idx_clan_invitation_clan", columnList = "clan_id, status, created_at")
})
/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanInvitation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clan_id", nullable = false)
    private Clan clan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_player_id", nullable = false)
    private Player inviter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitee_player_id", nullable = false)
    private Player invitee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ClanInvitationStatus status = ClanInvitationStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "acted_at")
    private LocalDateTime actedAt;

    public boolean isPendingAt(LocalDateTime now) {
        return status == ClanInvitationStatus.PENDING && expiresAt.isAfter(now);
    }
}
