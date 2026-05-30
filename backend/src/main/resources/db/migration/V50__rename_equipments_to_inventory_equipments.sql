-- Renomeia tabela de instancias de equipamento do jogador para evitar
-- confusao com a futura tabela de definicoes (equipment_templates)
ALTER TABLE equipments RENAME TO inventory_equipments;

ALTER INDEX idx_equipments_digimon_id RENAME TO idx_inventory_equipments_digimon_id;
