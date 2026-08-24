CREATE TABLE alpha_invites (
    id UUID PRIMARY KEY,
    code_hash CHAR(64) NOT NULL UNIQUE,
    code_hint VARCHAR(16) NOT NULL,
    tester_name VARCHAR(100) NOT NULL,
    tester_email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    used_by_player_id UUID NULL UNIQUE,
    created_by_admin_id UUID NOT NULL,
    CONSTRAINT fk_alpha_invite_used_by_player
        FOREIGN KEY (used_by_player_id) REFERENCES players(id),
    CONSTRAINT fk_alpha_invite_created_by_admin
        FOREIGN KEY (created_by_admin_id) REFERENCES players(id),
    CONSTRAINT ck_alpha_invite_expiration
        CHECK (expires_at > created_at)
);

CREATE INDEX idx_alpha_invites_tester_email ON alpha_invites (LOWER(tester_email));
CREATE INDEX idx_alpha_invites_expires_at ON alpha_invites (expires_at);
CREATE INDEX idx_alpha_invites_created_at ON alpha_invites (created_at DESC);
