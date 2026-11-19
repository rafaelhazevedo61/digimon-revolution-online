-- Fragmentos de Mega são itens empilháveis e devem poder ser negociados na Casa de Leilões.
-- O filtro por ícone identifica exclusivamente os fragmentos de estágio Mega.
UPDATE item_definitions
SET tradable = TRUE
WHERE category = 'EVOLUTION_MATERIAL'
  AND icon = 'fragment_mega_specific'
  AND stackable = TRUE;
