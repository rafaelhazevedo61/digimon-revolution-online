-- Carteira de Moedas de Arena (moeda de PvP) por jogador.
ALTER TABLE players ADD COLUMN arena_coins INTEGER NOT NULL DEFAULT 0;
