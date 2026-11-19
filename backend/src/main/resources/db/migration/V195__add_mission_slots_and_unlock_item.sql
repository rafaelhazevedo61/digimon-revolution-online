ALTER TABLE players
    ADD COLUMN unlocked_mission_slots INTEGER NOT NULL DEFAULT 1;

ALTER TABLE mission_instances
    ADD COLUMN slot_number INTEGER NOT NULL DEFAULT 1;

ALTER TABLE players
    ADD CONSTRAINT chk_players_unlocked_mission_slots
        CHECK (unlocked_mission_slots BETWEEN 1 AND 3);

ALTER TABLE mission_instances
    ADD CONSTRAINT chk_mission_instances_slot_number
        CHECK (slot_number BETWEEN 1 AND 3);

WITH ranked_active_missions AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY player_id ORDER BY started_at, id) AS assigned_slot
    FROM mission_instances
    WHERE status <> 'CLAIMED'
)
UPDATE mission_instances AS instance
SET slot_number = ranked_active_missions.assigned_slot
FROM ranked_active_missions
WHERE instance.id = ranked_active_missions.id;

CREATE UNIQUE INDEX ux_mission_instances_player_slot_active
    ON mission_instances (player_id, slot_number)
    WHERE status <> 'CLAIMED';

INSERT INTO item_definitions (
    code, name, description, category, stackable, buy_price, sell_price,
    tradable, sellable, usable, max_stack, rarity, icon
)
VALUES (
    'MISSION_SLOT_UNLOCK',
    'Expansor de Slot de Missão',
    'Desbloqueia mais um slot de missão para o jogador, até o limite de três.',
    'CONSUMABLE', TRUE, NULL, 500,
    TRUE, TRUE, TRUE, 3, 'EPIC', 'mission_slot_unlock'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    stackable = EXCLUDED.stackable,
    buy_price = EXCLUDED.buy_price,
    sell_price = EXCLUDED.sell_price,
    tradable = EXCLUDED.tradable,
    sellable = EXCLUDED.sellable,
    usable = EXCLUDED.usable,
    max_stack = EXCLUDED.max_stack,
    rarity = EXCLUDED.rarity,
    icon = EXCLUDED.icon;

INSERT INTO shop_products (
    code, name, description, product_type, category, item_type,
    item_definition_code, equipment_template_name, price, sell_price, active
)
VALUES (
    'MISSION_SLOT_UNLOCK',
    'Expansor de Slot de Missão',
    'Consumível que desbloqueia mais um slot de missão para o jogador.',
    'ITEM', 'CONSUMABLE', 'MISSION_SLOT_UNLOCK',
    'MISSION_SLOT_UNLOCK', NULL, 2000, 500, TRUE
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    product_type = EXCLUDED.product_type,
    category = EXCLUDED.category,
    item_type = EXCLUDED.item_type,
    item_definition_code = EXCLUDED.item_definition_code,
    price = EXCLUDED.price,
    sell_price = EXCLUDED.sell_price,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM';
