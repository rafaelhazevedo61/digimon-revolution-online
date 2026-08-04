-- Coluna de versão para lock otimista (@Version) em Digimon.
-- Evita last-write-wins quando dois desafios simultâneos atualizam o mesmo Digimon.
ALTER TABLE digimons ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
