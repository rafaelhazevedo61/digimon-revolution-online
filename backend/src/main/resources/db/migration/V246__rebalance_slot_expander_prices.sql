UPDATE shop_products
SET price = 1000000,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'SYSTEM'
WHERE code IN ('INCUBATION_SLOT_UNLOCK', 'MISSION_SLOT_UNLOCK');
