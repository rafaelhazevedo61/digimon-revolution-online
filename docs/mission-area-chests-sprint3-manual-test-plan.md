# Roteiro manual — Sprint 3: Baús da Área nas Missões

## Objetivo

Validar que a conclusão de uma Missão migrada entrega exatamente um Baú da Área, mantendo XP, Bits, energia, progresso, cooldown e demais regras existentes. O loot aleatório legado não deve ser aplicado às missões vinculadas a `chest_definition_id`.

A collection correspondente é `backend/src/main/resources/collection/DRO - MISSIONS SPRINT 3.postman_collection.json`. O JWT não está incluído no arquivo; preencha localmente a variável `playerToken`.

## Pré-condições

A aplicação deve estar executando com PostgreSQL e MongoDB disponíveis. As migrations até a V104 devem estar aplicadas com sucesso. O jogador deve possuir um Digimon ativo que atenda aos requisitos da missão escolhida. Para um teste rápido, use uma missão inicial compatível, como `MISSION_1`, desde que esteja disponível para o Digimon.

A migration V104 cria loot tables e baús específicos por missão. O código técnico do baú fica oculto na interface, mas pode ser consultado no banco para validação.

## Ordem recomendada

| Ordem | Requisição | Resultado esperado |
|---:|---|---|
| 1 | `00 - Configuração / Healthcheck` | HTTP 200 e `status = UP`. |
| 2 | `00 - Configuração / Listar inventário antes da missão` | HTTP 200; captura opcional do saldo inicial de baús. |
| 3 | `01 - Missão e Baú da Área / Listar missões disponíveis` | A missão escolhida aparece para o Digimon. |
| 4 | `01 - Missão e Baú da Área / Iniciar missão` | HTTP 200 e `missionInstanceId`. |
| 5 | `01 - Missão e Baú da Área / Listar missões ativas` | A instância aparece durante a execução. |
| 6 | Aguardar `endsAt` | Aguardar o horário indicado na resposta de início. |
| 7 | `01 - Missão e Baú da Área / Resgatar missão e receber Baú da Área` | HTTP 200, XP/Bits no response e recompensa `LOOT_CHEST` com quantidade 1. |
| 8 | `02 - Verificação pós-claim / Listar inventário após o claim` | O inventário contém o baú recebido. |
| 9 | `03 - Consultas SQL` | Confirmar vínculo, pool, abertura e auditoria. |

## Validações importantes

A resposta do claim deve conter os valores de `xpGained` e `bitsGained` normalmente. A lista `rewards` deve conter uma recompensa com `item = LOOT_CHEST` e `quantity = 1`. Essa recompensa também deve retornar `itemCode` começando por `CHEST_MISSION_` e um `itemName` preenchido. A mesma resposta não deve conter `FRAGMENT_ROOKIE`, `FRAGMENT_CHAMPION`, `FRAGMENT_ULTIMATE` ou `FRAGMENT_MEGA` como drop aleatório.

O claim deve marcar a instância como resgatada. Repetir o claim da mesma instância deve continuar retornando conflito de missão já resgatada, sem conceder outro baú.

## Consultas SQL

### Migrations

```sql
SELECT version, description, success
FROM flyway_schema_history
WHERE version IN ('101', '102', '103', '104')
ORDER BY version;
```

### Missões vinculadas aos baús

```sql
SELECT
    md.id AS mission_id,
    md.name AS mission_name,
    md.area,
    md.required_stage,
    md.required_level,
    md.chest_definition_id,
    cd.code AS chest_code,
    cd.name AS chest_name,
    lt.code AS loot_table_code,
    lt.min_items,
    lt.max_items
FROM mission_definitions md
JOIN chest_definitions cd
    ON cd.id = md.chest_definition_id
JOIN loot_tables lt
    ON lt.id = cd.loot_table_id
ORDER BY md.area, md.required_level, md.id;
```

Todas as missões de conteúdo devem possuir um `chest_definition_id`. Se uma missão administrativa ou de debug permanecer sem vínculo por decisão operacional, ela deve ser tratada conscientemente; não deve ficar sem recompensa por acidente.

### Pool da missão

Substitua `MISSION_1` pelo código testado:

```sql
SELECT
    md.id AS mission_id,
    lt.code AS loot_table_code,
    lte.rarity,
    lte.item_type,
    lte.material_code,
    lte.weight,
    lte.min_quantity,
    lte.max_quantity,
    lte.active
FROM mission_definitions md
JOIN chest_definitions cd
    ON cd.id = md.chest_definition_id
JOIN loot_tables lt
    ON lt.id = cd.loot_table_id
JOIN loot_table_entries lte
    ON lte.loot_table_id = lt.id
WHERE md.id = 'MISSION_1'
ORDER BY lte.rarity, lte.id;
```

A pool deve usar `EVOLUTION_MATERIAL` com `material_code` nomeado quando houver material de evolução. Os fragmentos genéricos por estágio não devem aparecer nas novas entradas de Missões.

### Recompensa persistida no inventário

```sql
SELECT
    ii.id AS inventory_item_id,
    ii.digimon_id,
    ii.item_type,
    ii.item_definition_id,
    idf.code AS item_code,
    idf.name AS item_name,
    ii.quantity
FROM inventory_items ii
LEFT JOIN item_definitions idf
    ON idf.id = ii.item_definition_id
WHERE ii.digimon_id = 'UUID_DO_DIGIMON'
  AND ii.item_type = 'LOOT_CHEST'
ORDER BY ii.id;
```

### Auditoria positiva do claim

```sql
SELECT
    event_id,
    event_type,
    aggregate_type,
    aggregate_id,
    status,
    attempts,
    payload_json,
    created_at,
    published_at
FROM audit_outbox_events
WHERE event_type = 'MISSION_CLAIMED'
ORDER BY created_at DESC;
```

O payload deve conter o jogador, missão, área, XP, Bits e, quando a missão estiver migrada, o `chestCode` e `chestQuantity = 1`. Após o processamento do Outbox, o status esperado é `PUBLISHED` e o evento deve aparecer na coleção de auditoria do MongoDB.

## Compatibilidade do legado

As tabelas `mission_loot_chances` e `mission_loot_items` permanecem no banco nesta sprint para compatibilidade e histórico. O claim prioriza o Baú da Área quando `chest_definition_id` está preenchido. O loot legado só é usado para uma missão que ainda não tenha vínculo, evitando uma quebra abrupta de dados antigos.
