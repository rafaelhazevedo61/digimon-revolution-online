-- Loja da Arena: itens genéricos comprados com Moedas de Arena.
CREATE TABLE arena_shop_products (
    code        VARCHAR(64) PRIMARY KEY,
    name        VARCHAR(128) NOT NULL,
    item_type   VARCHAR(64) NOT NULL,
    quantity    INTEGER NOT NULL,
    price_coins INTEGER NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO arena_shop_products (code, name, item_type, quantity, price_coins, active) VALUES
    ('ARENA_POTION_SMALL',    'Poção Pequena',            'POTION_SMALL',    1, 20,  TRUE),
    ('ARENA_POTION_SMALL_5',  'Pacote de Poções (5x)',    'POTION_SMALL',    5, 90,  TRUE),
    ('ARENA_REFINEMENT_STONE','Pedra de Refinamento',     'REFINEMENT_STONE',1, 60,  TRUE),
    ('ARENA_TRAINING_STONE',  'Pedra de Treinamento',     'TRAINING_STONE',  1, 50,  TRUE),
    ('ARENA_DATA_CORE',       'Núcleo de Dados',          'DATA_CORE',       1, 120, TRUE);
