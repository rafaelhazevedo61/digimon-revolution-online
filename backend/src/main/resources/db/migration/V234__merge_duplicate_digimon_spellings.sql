-- Unifica Digimons duplicados por grafia (JP/EN) no registro com a grafia inglesa:
--   Grappu Leomon -> GrapLeomon, Tunomon -> Tsunomon, Mochimon -> Motimon,
--   Pichimon -> Pitchmon, Pabumon -> Bubbmon, Pukamon -> Bukamon.
-- Os fragmentos duplicados são convertidos para o código canônico:
--   FRAGMENT_GRAPPULEOMON -> FRAGMENT_GRAPLEOMON, FRAGMENT_TUNOMON -> FRAGMENT_TSUNOMON,
--   FRAGMENT_MOCHIMON -> FRAGMENT_MOTIMON, FRAGMENT_PUKAMON -> FRAGMENT_BUKAMON.
BEGIN;

CREATE TEMP TABLE tmp_digimon_merge (
    duplicate_name VARCHAR(50) NOT NULL,
    canonical_name VARCHAR(50) NOT NULL,
    duplicate_id   BIGINT,
    canonical_id   BIGINT
) ON COMMIT DROP;

INSERT INTO tmp_digimon_merge (duplicate_name, canonical_name) VALUES
    ('Grappu Leomon', 'GrapLeomon'),
    ('Tunomon',       'Tsunomon'),
    ('Mochimon',      'Motimon'),
    ('Pichimon',      'Pitchmon'),
    ('Pabumon',       'Bubbmon'),
    ('Pukamon',       'Bukamon');

UPDATE tmp_digimon_merge m
   SET duplicate_id = dup.id,
       canonical_id = canon.id
  FROM digimon_infos dup, digimon_infos canon
 WHERE dup.name = m.duplicate_name
   AND canon.name = m.canonical_name;

-- Só processa pares em que ambos os registros existem.
DELETE FROM tmp_digimon_merge WHERE duplicate_id IS NULL OR canonical_id IS NULL;

-- 1. Digimons de jogadores e bots.
UPDATE digimons d
   SET digimon_info_id = m.canonical_id,
       name = m.canonical_name
  FROM tmp_digimon_merge m
 WHERE d.digimon_info_id = m.duplicate_id
    OR (d.digimon_info_id IS NULL AND d.name = m.duplicate_name);

UPDATE digitama_history h
   SET digimon_name = m.canonical_name
  FROM tmp_digimon_merge m
 WHERE h.digimon_name = m.duplicate_name;

-- 2. Linhas de evolução (as duas grafias nunca aparecem na mesma linha).
UPDATE evolution_line_steps s
   SET digimon_info_id = m.canonical_id
  FROM tmp_digimon_merge m
 WHERE s.digimon_info_id = m.duplicate_id
   AND NOT EXISTS (
       SELECT 1 FROM evolution_line_steps other
        WHERE other.evolution_line_id = s.evolution_line_id
          AND other.digimon_info_id = m.canonical_id
   );

DELETE FROM evolution_step_materials esm
 USING evolution_line_steps s, tmp_digimon_merge m
 WHERE esm.evolution_line_step_id = s.id
   AND s.digimon_info_id = m.duplicate_id;

DELETE FROM evolution_line_steps s
 USING tmp_digimon_merge m
 WHERE s.digimon_info_id = m.duplicate_id;

-- 3. Pools de digitama.
UPDATE digitama_pool_entries e
   SET digimon_info_id = m.canonical_id
  FROM tmp_digimon_merge m
 WHERE e.digimon_info_id = m.duplicate_id
   AND NOT EXISTS (
       SELECT 1 FROM digitama_pool_entries other
        WHERE other.digitama_pool_id = e.digitama_pool_id
          AND other.digimon_info_id = m.canonical_id
   );

DELETE FROM digitama_pool_entries e
 USING tmp_digimon_merge m
 WHERE e.digimon_info_id = m.duplicate_id;

-- 4. Coleção (Digidex).
UPDATE digimon_collection_entries c
   SET digimon_info_id = m.canonical_id
  FROM tmp_digimon_merge m
 WHERE c.digimon_info_id = m.duplicate_id
   AND NOT EXISTS (
       SELECT 1 FROM digimon_collection_entries other
        WHERE other.player_id = c.player_id
          AND other.digimon_info_id = m.canonical_id
          AND other.rarity = c.rarity
   );

DELETE FROM digimon_collection_entries c
 USING tmp_digimon_merge m
 WHERE c.digimon_info_id = m.duplicate_id;

-- 5. Remove o registro duplicado.
DELETE FROM digimon_infos di
 USING tmp_digimon_merge m
 WHERE di.id = m.duplicate_id;

-- ---------------------------------------------------------------------------
-- Fragmentos duplicados.
-- ---------------------------------------------------------------------------
CREATE TEMP TABLE tmp_fragment_merge (
    duplicate_code VARCHAR(80) NOT NULL,
    canonical_code VARCHAR(80) NOT NULL,
    duplicate_id   BIGINT,
    canonical_id   BIGINT
) ON COMMIT DROP;

INSERT INTO tmp_fragment_merge (duplicate_code, canonical_code) VALUES
    ('FRAGMENT_GRAPPULEOMON', 'FRAGMENT_GRAPLEOMON'),
    ('FRAGMENT_TUNOMON',      'FRAGMENT_TSUNOMON'),
    ('FRAGMENT_MOCHIMON',     'FRAGMENT_MOTIMON'),
    ('FRAGMENT_PUKAMON',      'FRAGMENT_BUKAMON');

UPDATE tmp_fragment_merge m
   SET duplicate_id = dup.id,
       canonical_id = canon.id
  FROM item_definitions dup, item_definitions canon
 WHERE dup.code = m.duplicate_code
   AND canon.code = m.canonical_code;

DELETE FROM tmp_fragment_merge WHERE duplicate_id IS NULL OR canonical_id IS NULL;

-- 6. Inventário dos jogadores: soma quantidades quando já possuem o canônico.
UPDATE inventory_items canon
   SET quantity = canon.quantity + dup.quantity
  FROM inventory_items dup, tmp_fragment_merge m
 WHERE dup.item_definition_id = m.duplicate_id
   AND canon.item_definition_id = m.canonical_id
   AND canon.player_id = dup.player_id;

DELETE FROM inventory_items dup
 USING tmp_fragment_merge m
 WHERE dup.item_definition_id = m.duplicate_id
   AND EXISTS (
       SELECT 1 FROM inventory_items canon
        WHERE canon.player_id = dup.player_id
          AND canon.item_definition_id = m.canonical_id
   );

UPDATE inventory_items i
   SET item_definition_id = m.canonical_id
  FROM tmp_fragment_merge m
 WHERE i.item_definition_id = m.duplicate_id;

-- 7. Armazém e histórico de clã, leilão.
UPDATE clan_storage_items c
   SET item_definition_id = m.canonical_id
  FROM tmp_fragment_merge m
 WHERE c.item_definition_id = m.duplicate_id;

UPDATE clan_storage_history h
   SET item_definition_id = m.canonical_id,
       item_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE h.item_definition_id = m.duplicate_id;

UPDATE auction_listings a
   SET item_definition_id = m.canonical_id
  FROM tmp_fragment_merge m
 WHERE a.item_definition_id = m.duplicate_id;

UPDATE auction_transactions t
   SET item_definition_id = m.canonical_id
  FROM tmp_fragment_merge m
 WHERE t.item_definition_id = m.duplicate_id;

-- 8. Referências por código (texto livre).
UPDATE evolution_step_materials e
   SET material_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE e.material_code = m.duplicate_code;

DELETE FROM loot_table_entries dup
 USING tmp_fragment_merge m
 WHERE dup.material_code = m.duplicate_code
   AND EXISTS (
       SELECT 1 FROM loot_table_entries canon
        WHERE canon.loot_table_id = dup.loot_table_id
          AND canon.rarity = dup.rarity
          AND canon.item_type = dup.item_type
          AND canon.material_code = m.canonical_code
   );

UPDATE loot_table_entries l
   SET material_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE l.material_code = m.duplicate_code;

UPDATE chest_opening_items c
   SET material_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE c.material_code = m.duplicate_code;

UPDATE mission_rewards r
   SET item_type = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE r.item_type = m.duplicate_code;

UPDATE mission_loot_items l
   SET item_type = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE l.item_type = m.duplicate_code;

UPDATE boss_drops b
   SET item_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE b.item_code = m.duplicate_code;

UPDATE event_rewards e
   SET item_definition_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE e.item_definition_code = m.duplicate_code;

DELETE FROM event_reward_items dup
 USING tmp_fragment_merge m
 WHERE dup.item_definition_code = m.duplicate_code
   AND EXISTS (
       SELECT 1 FROM event_reward_items canon
        WHERE canon.event_reward_id = dup.event_reward_id
          AND canon.item_definition_code = m.canonical_code
   );

UPDATE event_reward_items e
   SET item_definition_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE e.item_definition_code = m.duplicate_code;

UPDATE shop_products s
   SET item_definition_code = m.canonical_code
  FROM tmp_fragment_merge m
 WHERE s.item_definition_code = m.duplicate_code;

-- 9. Remove o item duplicado.
DELETE FROM item_definitions i
 USING tmp_fragment_merge m
 WHERE i.id = m.duplicate_id;

COMMIT;
