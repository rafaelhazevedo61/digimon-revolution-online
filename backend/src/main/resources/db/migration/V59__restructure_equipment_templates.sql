-- Reestruturar equipment_templates para suportar sets e tiers
-- 1. Limpar templates antigos
DELETE FROM equipment_templates;

-- 2. Adicionar novas colunas
ALTER TABLE equipment_templates DROP CONSTRAINT equipment_templates_pkey;
ALTER TABLE equipment_templates ADD COLUMN id BIGSERIAL;
ALTER TABLE equipment_templates ADD PRIMARY KEY (id);
ALTER TABLE equipment_templates ADD COLUMN set_code VARCHAR(30) NOT NULL DEFAULT 'BERSERKER';
ALTER TABLE equipment_templates ADD COLUMN tier INT NOT NULL DEFAULT 1;
ALTER TABLE equipment_templates ALTER COLUMN name DROP NOT NULL;

-- 3. Unique constraint: um template por set+slot+tier
ALTER TABLE equipment_templates ADD CONSTRAINT uq_equipment_set_slot_tier UNIQUE (set_code, slot, tier);
