-- Padroniza o limite de pilha dos itens acumuláveis para suportar a abertura
-- múltipla de baús e o recebimento de recompensas em maior quantidade.
-- Itens não acumuláveis não possuem limite de pilha aplicável.
UPDATE item_definitions
SET max_stack = CASE WHEN stackable THEN 999 ELSE NULL END;
