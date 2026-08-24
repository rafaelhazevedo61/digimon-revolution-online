package com.dro.modules.clan.domain;

import com.dro.modules.player.domain.Player;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clan_invitations", indexes = {@Index(name = "idx_clan_invitation_invitee", columnList = "invitee_player_id, status, expires_at"), @Index(name = "idx_clan_invitation_clan", columnList = "clan_id, status, created_at")})
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
    private ClanInvitationStatus status;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "acted_at")
    private LocalDateTime actedAt;

    public boolean isPendingAt(LocalDateTime now) {
        return status == ClanInvitationStatus.PENDING && expiresAt.isAfter(now);
    }

    private static ClanInvitationStatus $default$status() {
        return ClanInvitationStatus.PENDING;
    }


    public static class ClanInvitationBuilder {
        private UUID id;
        private Clan clan;
        private Player inviter;
        private Player invitee;
        private boolean status$set;
        private ClanInvitationStatus status$value;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private LocalDateTime actedAt;

        ClanInvitationBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder clan(final Clan clan) {
            this.clan = clan;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder inviter(final Player inviter) {
            this.inviter = inviter;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder invitee(final Player invitee) {
            this.invitee = invitee;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder status(final ClanInvitationStatus status) {
            this.status$value = status;
            status$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder expiresAt(final LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ClanInvitation.ClanInvitationBuilder actedAt(final LocalDateTime actedAt) {
            this.actedAt = actedAt;
            return this;
        }

        public ClanInvitation build() {
            ClanInvitationStatus status$value = this.status$value;
            if (!this.status$set) status$value = ClanInvitation.$default$status();
            return new ClanInvitation(this.id, this.clan, this.inviter, this.invitee, status$value, this.createdAt, this.expiresAt, this.actedAt);
        }

        @Override
        public String toString() {
            return "ClanInvitation.ClanInvitationBuilder(id=" + this.id + ", clan=" + this.clan + ", inviter=" + this.inviter + ", invitee=" + this.invitee + ", status$value=" + this.status$value + ", createdAt=" + this.createdAt + ", expiresAt=" + this.expiresAt + ", actedAt=" + this.actedAt + ")";
        }
    }

    public static ClanInvitation.ClanInvitationBuilder builder() {
        return new ClanInvitation.ClanInvitationBuilder();
    }

    public UUID getId() {
        return this.id;
    }

    public Clan getClan() {
        return this.clan;
    }

    public Player getInviter() {
        return this.inviter;
    }

    public Player getInvitee() {
        return this.invitee;
    }

    public ClanInvitationStatus getStatus() {
        return this.status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public LocalDateTime getActedAt() {
        return this.actedAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setClan(final Clan clan) {
        this.clan = clan;
    }

    public void setInviter(final Player inviter) {
        this.inviter = inviter;
    }

    public void setInvitee(final Player invitee) {
        this.invitee = invitee;
    }

    public void setStatus(final ClanInvitationStatus status) {
        this.status = status;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setExpiresAt(final LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setActedAt(final LocalDateTime actedAt) {
        this.actedAt = actedAt;
    }

    public ClanInvitation() {
        this.status = ClanInvitation.$default$status();
    }

    public ClanInvitation(final UUID id, final Clan clan, final Player inviter, final Player invitee, final ClanInvitationStatus status, final LocalDateTime createdAt, final LocalDateTime expiresAt, final LocalDateTime actedAt) {
        this.id = id;
        this.clan = clan;
        this.inviter = inviter;
        this.invitee = invitee;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.actedAt = actedAt;
    }
}
