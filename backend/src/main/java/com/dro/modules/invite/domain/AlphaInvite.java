package com.dro.modules.invite.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alpha_invites")
public class AlphaInvite {

    @Id
    private UUID id;

    @Column(
            name = "code_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String codeHash;

    @Column(name = "code_hint", nullable = false, length = 16)
    private String codeHint;

    @Column(name = "tester_name", nullable = false, length = 100)
    private String testerName;

    @Column(name = "tester_email", nullable = false, length = 100)
    private String testerEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_by_player_id", unique = true)
    private UUID usedByPlayerId;

    @Column(name = "created_by_admin_id", nullable = false)
    private UUID createdByAdminId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by_admin_id")
    private UUID deletedByAdminId;

    protected AlphaInvite() {
    }

    public AlphaInvite(UUID id, String codeHash, String codeHint, String testerName, String testerEmail,
                       LocalDateTime createdAt, LocalDateTime expiresAt, UUID createdByAdminId) {
        this.id = id;
        this.codeHash = codeHash;
        this.codeHint = codeHint;
        this.testerName = testerName;
        this.testerEmail = testerEmail;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.createdByAdminId = createdByAdminId;
    }

    public boolean isUsed() {
        return usedAt != null || usedByPlayerId != null;
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public void markUsed(UUID playerId, LocalDateTime usedAt) {
        if (isUsed()) {
            throw new IllegalStateException("Alpha invite already used");
        }
        this.usedByPlayerId = playerId;
        this.usedAt = usedAt;
    }

    public UUID getId() { return id; }
    public String getCodeHash() { return codeHash; }
    public String getCodeHint() { return codeHint; }
    public String getTesterName() { return testerName; }
    public String getTesterEmail() { return testerEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public UUID getUsedByPlayerId() { return usedByPlayerId; }
    public UUID getCreatedByAdminId() { return createdByAdminId; }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete(UUID adminId, LocalDateTime deletedAt) {
        if (isDeleted()) {
            return;
        }

        this.deletedAt = deletedAt;
        this.deletedByAdminId = adminId;
    }
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public UUID getDeletedByAdminId() {
        return deletedByAdminId;
    }

}
