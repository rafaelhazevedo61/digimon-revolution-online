-- A regra atual mantém um único Digimon ativo por jogador.
-- Primeiro, preserva o ativo apontado pelo jogador quando ele ainda é válido;
-- em caso contrário, mantém o Digimon ativo mais antigo.
UPDATE players p
SET active_digimon_id = NULL
WHERE p.active_digimon_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM digimons d
      WHERE d.id = p.active_digimon_id
        AND d.player_id = p.id
        AND d.status = 'ACTIVE'
  );

WITH ranked_active AS (
    SELECT d.id,
           ROW_NUMBER() OVER (
               PARTITION BY d.player_id
               ORDER BY CASE WHEN d.id = p.active_digimon_id THEN 0 ELSE 1 END,
                        d.created_at ASC NULLS LAST,
                        d.id
           ) AS position
    FROM digimons d
    LEFT JOIN players p ON p.id = d.player_id
    WHERE d.status = 'ACTIVE'
)
UPDATE digimons d
SET status = 'STORED'
FROM ranked_active r
WHERE d.id = r.id
  AND r.position > 1;

UPDATE players p
SET active_digimon_id = selected.id
FROM (
    SELECT DISTINCT ON (d.player_id) d.player_id, d.id
    FROM digimons d
    WHERE d.status = 'ACTIVE'
    ORDER BY d.player_id, d.created_at ASC NULLS LAST, d.id
) selected
WHERE p.id = selected.player_id
  AND p.active_digimon_id IS NULL;

-- O campo é mantido por compatibilidade de schema, mas não representa mais
-- uma quantidade configurável de ativos.
UPDATE players
SET max_digimon_slots = 1
WHERE max_digimon_slots <> 1;

ALTER TABLE players
    ALTER COLUMN max_digimon_slots SET DEFAULT 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_digimons_one_active_per_player
    ON digimons (player_id)
    WHERE status = 'ACTIVE';
