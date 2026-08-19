CREATE TABLE clan_invitations (
    id UUID PRIMARY KEY,
    clan_id UUID NOT NULL REFERENCES clans(id),
    inviter_player_id UUID NOT NULL REFERENCES players(id),
    invitee_player_id UUID NOT NULL REFERENCES players(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    acted_at TIMESTAMP NULL,
    CONSTRAINT chk_clan_invitation_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT chk_clan_invitation_dates
        CHECK (expires_at > created_at),
    CONSTRAINT chk_clan_invitation_not_self
        CHECK (inviter_player_id <> invitee_player_id)
);

CREATE INDEX idx_clan_invitation_invitee
    ON clan_invitations(invitee_player_id, status, expires_at);
CREATE INDEX idx_clan_invitation_clan
    ON clan_invitations(clan_id, status, created_at DESC);
CREATE UNIQUE INDEX uk_clan_invitation_pending
    ON clan_invitations(clan_id, invitee_player_id)
    WHERE status = 'PENDING';
