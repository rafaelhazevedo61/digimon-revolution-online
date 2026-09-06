BEGIN;

-- V217: atualiza a lista da loja da Arena conforme o catálogo aprovado.
-- Equipamentos Data Breaker são mantidos fora do escopo nesta etapa.

UPDATE arena_shop_products
SET active = FALSE
WHERE active = TRUE;

INSERT INTO arena_shop_products (code, name, item_type, quantity, price_coins, active)
VALUES
    ('ARENA_REFINEMENT_STONE',
     'Pedra de Refinamento',
     'REFINEMENT_STONE', 1, 100, TRUE),
    ('ARENA_DATA_CORE',
     'Núcleo de Dados',
     'DATA_CORE', 1, 150, TRUE),
    ('ARENA_REFINEMENT_SUCCESS_BOOST',
     'Pergaminho de Refinamento',
     'REFINEMENT_SUCCESS_BOOST', 1, 500, TRUE),
    ('ARENA_REFINEMENT_PROTECTION',
     'Cristal de Proteção',
     'REFINEMENT_PROTECTION', 1, 1200, TRUE),
    ('ARENA_INCUBATION_SLOT_UNLOCK',
     '1º Expansor de Slot de Incubação',
     'INCUBATION_SLOT_UNLOCK', 1, 5000, TRUE),
    ('ARENA_COLLECTION_DIGIVICE',
     'Digivice de Registro',
     'COLLECTION_DIGIVICE', 1, 400, TRUE),
    ('ARENA_RARITY_PRESERVATION',
     'Cristal de Preservação de Raridade',
     'RARITY_PRESERVATION', 1, 2500, TRUE)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    item_type = EXCLUDED.item_type,
    quantity = EXCLUDED.quantity,
    price_coins = EXCLUDED.price_coins,
    active = TRUE;

DO $$
DECLARE
    active_products INT;
    wrong_prices INT;
    unsupported_products INT;
BEGIN
    SELECT COUNT(*)
    INTO active_products
    FROM arena_shop_products
    WHERE active = TRUE;

    SELECT COUNT(*)
    INTO wrong_prices
    FROM arena_shop_products product
    JOIN (VALUES
        ('ARENA_REFINEMENT_STONE', 100),
        ('ARENA_DATA_CORE', 150),
        ('ARENA_REFINEMENT_SUCCESS_BOOST', 500),
        ('ARENA_REFINEMENT_PROTECTION', 1200),
        ('ARENA_INCUBATION_SLOT_UNLOCK', 5000),
        ('ARENA_COLLECTION_DIGIVICE', 400),
        ('ARENA_RARITY_PRESERVATION', 2500)
    ) AS expected(code, price_coins) ON expected.code = product.code
    WHERE product.active = TRUE
      AND product.price_coins <> expected.price_coins;

    SELECT COUNT(*)
    INTO unsupported_products
    FROM arena_shop_products
    WHERE active = TRUE
      AND item_type NOT IN (
          'REFINEMENT_STONE', 'DATA_CORE',
          'REFINEMENT_SUCCESS_BOOST', 'REFINEMENT_PROTECTION',
          'INCUBATION_SLOT_UNLOCK', 'COLLECTION_DIGIVICE',
          'RARITY_PRESERVATION'
      );

    IF active_products <> 7 OR wrong_prices > 0 OR unsupported_products > 0 THEN
        RAISE EXCEPTION
            'Arena Shop: esperados 7 produtos ativos, % encontrados; % preço(s) incorreto(s); % produto(s) não suportado(s).',
            active_products, wrong_prices, unsupported_products;
    END IF;
END $$;

COMMIT;
