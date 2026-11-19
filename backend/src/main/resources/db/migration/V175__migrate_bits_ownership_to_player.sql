-- Transfere a posse da moeda Bits dos Digimons para o jogador.
ALTER TABLE players
    ADD COLUMN bits INTEGER NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM (
            SELECT player_id, SUM(bits::bigint) AS total_bits
            FROM digimons
            GROUP BY player_id
        ) totals
        WHERE totals.total_bits > 2147483647
    ) THEN
        RAISE EXCEPTION 'A soma de bits de algum jogador excede o limite de INTEGER';
    END IF;
END $$;

UPDATE players player
SET bits = totals.total_bits::integer
FROM (
    SELECT player_id, COALESCE(SUM(bits), 0)::bigint AS total_bits
    FROM digimons
    GROUP BY player_id
) totals
WHERE player.id = totals.player_id;

-- A coluna legada permanece nesta etapa para compatibilidade com entidades e fixtures.
-- Nenhum fluxo novo deve usá-la como fonte de saldo; a remoção ocorrerá após a
-- migração completa dos casos de uso para Player.bits.
