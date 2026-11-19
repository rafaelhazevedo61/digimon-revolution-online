BEGIN;

CREATE TABLE equipment_rarity_profiles (
    id              BIGSERIAL PRIMARY KEY,
    profile_key     VARCHAR(40)  NOT NULL UNIQUE,
    display_name    VARCHAR(120) NOT NULL,
    common_percent  INT          NOT NULL,
    rare_percent    INT          NOT NULL,
    epic_percent    INT          NOT NULL,
    legendary_percent INT        NOT NULL,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(80)  NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT ck_equipment_rarity_profile_non_negative CHECK (
        common_percent >= 0
        AND rare_percent >= 0
        AND epic_percent >= 0
        AND legendary_percent >= 0
    ),
    CONSTRAINT ck_equipment_rarity_profile_total CHECK (
        common_percent + rare_percent + epic_percent + legendary_percent = 100
    )
);

INSERT INTO equipment_rarity_profiles (
    profile_key,
    display_name,
    common_percent,
    rare_percent,
    epic_percent,
    legendary_percent,
    updated_by
)
VALUES
    ('BOSS_NORMAL', 'Boss Normal', 65, 22, 10, 3, 'SYSTEM'),
    ('BOSS_DAILY', 'Boss Diário', 55, 28, 13, 4, 'SYSTEM'),
    ('BOSS_WEEKLY', 'Boss Semanal', 40, 30, 20, 10, 'SYSTEM'),
    ('BOSS_MONTHLY', 'Boss Mensal', 20, 30, 30, 20, 'SYSTEM')
ON CONFLICT (profile_key) DO NOTHING;

COMMIT;
