ALTER TABLE alpha_invites
    ADD COLUMN deleted_at TIMESTAMP NULL,
    ADD COLUMN deleted_by_admin_id UUID NULL;