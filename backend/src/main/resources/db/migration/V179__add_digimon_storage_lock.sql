-- Permite proteger Digimons armazenados contra sacrifício acidental.
-- Registros existentes começam desbloqueados para preservar o comportamento atual.
ALTER TABLE digimons
    ADD COLUMN is_locked BOOLEAN NOT NULL DEFAULT FALSE;
