BEGIN;

-- V211: remove recompensas diretas de fragmentos genéricos legados.
-- V208, V209 e V210 permanecem imutáveis. A partir do vínculo de uma missão
-- com um Baú da Área, a Loot Table do baú é a única fonte de recompensa.
--
-- O filtro por chest_definition_id evita alterar missões antigas que ainda não
-- foram migradas para o fluxo de Baús. Fragmentos individuais continuam
-- válidos dentro das Loot Tables internas dos próprios baús.

DO $$
DECLARE
    deleted_fixed_rewards INT;
    deleted_loot_items INT;
    remaining_fixed_rewards INT;
    remaining_loot_items INT;
BEGIN
    DELETE FROM mission_rewards reward
    USING mission_definitions mission
    WHERE reward.mission_id = mission.id
      AND mission.chest_definition_id IS NOT NULL
      AND reward.item_type IN (
          'FRAGMENT_BABY_II',
          'FRAGMENT_ROOKIE',
          'FRAGMENT_CHAMPION',
          'FRAGMENT_ULTIMATE',
          'FRAGMENT_MEGA'
      );

    GET DIAGNOSTICS deleted_fixed_rewards = ROW_COUNT;

    DELETE FROM mission_loot_items loot_item
    USING mission_definitions mission
    WHERE loot_item.mission_id = mission.id
      AND mission.chest_definition_id IS NOT NULL
      AND loot_item.item_type IN (
          'FRAGMENT_BABY_II',
          'FRAGMENT_ROOKIE',
          'FRAGMENT_CHAMPION',
          'FRAGMENT_ULTIMATE',
          'FRAGMENT_MEGA'
      );

    GET DIAGNOSTICS deleted_loot_items = ROW_COUNT;

    SELECT COUNT(*)
    INTO remaining_fixed_rewards
    FROM mission_rewards reward
    JOIN mission_definitions mission ON mission.id = reward.mission_id
    WHERE mission.chest_definition_id IS NOT NULL
      AND reward.item_type IN (
          'FRAGMENT_BABY_II',
          'FRAGMENT_ROOKIE',
          'FRAGMENT_CHAMPION',
          'FRAGMENT_ULTIMATE',
          'FRAGMENT_MEGA'
      );

    SELECT COUNT(*)
    INTO remaining_loot_items
    FROM mission_loot_items loot_item
    JOIN mission_definitions mission ON mission.id = loot_item.mission_id
    WHERE mission.chest_definition_id IS NOT NULL
      AND loot_item.item_type IN (
          'FRAGMENT_BABY_II',
          'FRAGMENT_ROOKIE',
          'FRAGMENT_CHAMPION',
          'FRAGMENT_ULTIMATE',
          'FRAGMENT_MEGA'
      );

    IF remaining_fixed_rewards > 0 OR remaining_loot_items > 0 THEN
        RAISE EXCEPTION
            'Missões: fragmentos legados permaneceram (% recompensa(s) fixa(s), % item(ns) de loot).',
            remaining_fixed_rewards,
            remaining_loot_items;
    END IF;

    RAISE NOTICE
        'V211: removidas % recompensa(s) fixa(s) e % item(ns) de loot legado(s).',
        deleted_fixed_rewards,
        deleted_loot_items;
END $$;

COMMIT;
