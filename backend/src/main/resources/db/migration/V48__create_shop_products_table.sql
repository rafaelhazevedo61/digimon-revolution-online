-- product_type: ITEM, EQUIPMENT
-- category:     POTION, MATERIAL, FRAGMENT, CONSUMABLE, EQUIPMENT
-- item_type:    POTION_SMALL, TRAINING_STONE, DATA_CORE,
--               DIGITAMA_STARTER, DIGITAMA_FIRE, DIGITAMA_WATER, DIGITAMA_NATURE,
--               INCUBATOR_COMMON, INCUBATOR_RARE, INCUBATOR_EPIC,
--               FRAGMENT_ROOKIE, FRAGMENT_CHAMPION, FRAGMENT_ULTIMATE, FRAGMENT_MEGA,
--               EVOLUTION_MATERIAL

CREATE TABLE shop_products (
    code        VARCHAR(80) PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    product_type VARCHAR(20) NOT NULL,      -- ITEM | EQUIPMENT
    category    VARCHAR(20) NOT NULL,       -- POTION | MATERIAL | FRAGMENT | CONSUMABLE | EQUIPMENT
    item_type   VARCHAR(60),                -- obrigatorio quando product_type = ITEM (ver lista acima)
    equipment_template_name VARCHAR(120),   -- obrigatorio quando product_type = EQUIPMENT
    price       INT NOT NULL,
    sell_price  INT NOT NULL DEFAULT 0,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);
