-- Adiciona limite de slots ativos e storage ao jogador
ALTER TABLE players ADD COLUMN max_digimon_slots INT NOT NULL DEFAULT 3;
ALTER TABLE players ADD COLUMN max_storage_slots INT NOT NULL DEFAULT 50;

-- Adiciona flag stored ao digimon (digimons no storage nao contam como ativos)
-- status ACTIVE = ativo, STORED = no storage, REBORN = renascido
-- Nenhum digimon existente precisa mudar - todos continuam ACTIVE
