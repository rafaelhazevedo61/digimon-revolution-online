# Roteiro manual — Collection Postman da Sprint 2 de Baús

## Objetivo

Este roteiro valida o fluxo implementado no PR #60: abertura transacional de baú, sorteio de raridade e itens, consumo de uma unidade, persistência do resultado, retry idempotente, rejeição de conflito e publicação de `CHEST_OPENED` no Transactional Outbox.

A collection não contém JWT. O token deve ser preenchido localmente na variável `playerToken` do Postman.

## Pré-condições

A API deve estar em execução em `http://localhost:8080`, com PostgreSQL e MongoDB disponíveis. As migrations V101, V102 e V103 devem ter sido aplicadas com sucesso. O jogador usado no token precisa ter um Digimon ativo e pelo menos uma unidade de um baú no inventário, preferencialmente `CHEST_MISSION_NATIVE_FOREST`.

A definição do baú e o item de inventário devem estar vinculados ao mesmo `item_definition_id`. O endpoint usa o Digimon ativo do jogador; um baú pertencente a outro Digimon não será localizado pela abertura.

## Configuração do Postman

Importe `backend/src/main/resources/collection/DRO - CHEST SPRINT 2.postman_collection.json`. Abra as variáveis da collection e preencha somente `playerToken` com um JWT válido. Se necessário, altere `baseUrl` e `chestCode`. A variável `openingRequestId` deve permanecer vazia no início para que o pre-request gere uma chave nova.

Não é necessário preencher manualmente `openingRequestId`, `openingResponse`, `openingId` ou `chestQuantityBefore`. A collection tenta capturar esses valores durante a execução.

## Ordem recomendada

| Ordem | Pasta/requisição | Resultado esperado |
|---:|---|---|
| 1 | `00 - Configuração e pré-condições / Healthcheck da API` | HTTP 200 e `status = UP`. |
| 2 | `00 - Configuração e pré-condições / Listar inventário e capturar baú` | HTTP 200 e captura automática de um baú. |
| 3 | `01 - Abertura válida / Abrir baú` | HTTP 200, raridade oficial e 1–4 itens diferentes. |
| 4 | `03 - Verificação do inventário / Conferir consumo do baú` | Quantidade atual igual à quantidade inicial menos uma unidade. |
| 5 | `02 - Idempotência / Repetir exatamente a mesma abertura` | HTTP 200 com o mesmo resultado lógico. |
| 6 | `03 - Verificação do inventário / Conferir consumo do baú` | A quantidade não diminui novamente. |
| 7 | `02 - Idempotência / Reutilizar requestId para outro jogador ou baú` | HTTP 409 ou resposta controlada equivalente. |
| 8 | `04 - Validações e erros esperados` | HTTP 400 para body inválido, 404/422 para baú inexistente e 401/403 sem JWT. |
| 9 | `05 - Consultas SQL de verificação` | Executar as queries no DBeaver ou `psql`. |

## Consultas SQL

### Migration

```sql
SELECT version, description, success
FROM flyway_schema_history
WHERE version IN ('101', '102', '103')
ORDER BY version;
```

O esperado é encontrar as três versões com `success = true`.

### Definição do baú e loot table

```sql
SELECT
    cd.code AS chest_code,
    cd.name,
    cd.tradable,
    cd.active,
    lt.code AS loot_table_code,
    lt.min_items,
    lt.max_items
FROM chest_definitions cd
JOIN loot_tables lt ON lt.id = cd.loot_table_id
WHERE cd.code = 'CHEST_MISSION_NATIVE_FOREST';

SELECT
    lt.code AS loot_table_code,
    rw.rarity,
    rw.weight
FROM loot_tables lt
JOIN loot_table_rarity_weights rw ON rw.loot_table_id = lt.id
WHERE lt.code = 'LOOT_TABLE_MISSION_AREA_DEFAULT'
ORDER BY rw.rarity;

SELECT
    lt.code AS loot_table_code,
    lte.rarity,
    lte.item_type,
    lte.material_code,
    lte.weight,
    lte.min_quantity,
    lte.max_quantity,
    lte.active
FROM loot_tables lt
JOIN loot_table_entries lte ON lte.loot_table_id = lt.id
WHERE lt.code = 'LOOT_TABLE_MISSION_AREA_DEFAULT'
ORDER BY lte.rarity, lte.id;
```

### Resultado persistido da abertura

Substitua o valor pelo conteúdo atual da variável `openingRequestId` no Postman.

```sql
SELECT
    co.id,
    co.request_id,
    co.player_id,
    cd.code AS chest_code,
    co.rarity,
    co.source,
    co.opened_at
FROM chest_openings co
JOIN chest_definitions cd ON cd.id = co.chest_definition_id
WHERE co.request_id = 'postman-chest-COLOQUE_O_VALOR_AQUI';

SELECT
    coi.chest_opening_id,
    coi.item_type,
    coi.material_code,
    coi.quantity
FROM chest_opening_items coi
JOIN chest_openings co ON co.id = coi.chest_opening_id
WHERE co.request_id = 'postman-chest-COLOQUE_O_VALOR_AQUI'
ORDER BY coi.id;
```

Deve existir exatamente uma linha em `chest_openings`, mesmo depois do retry, e entre uma e quatro linhas em `chest_opening_items`.

### Inventário antes e depois

```sql
SELECT
    ii.digimon_id,
    ii.item_type,
    ii.item_definition_id,
    idf.code,
    idf.name,
    ii.quantity
FROM inventory_items ii
LEFT JOIN item_definitions idf ON idf.id = ii.item_definition_id
WHERE idf.category = 'CHEST'
ORDER BY ii.digimon_id, idf.code;
```

A primeira abertura deve reduzir o baú em exatamente uma unidade. O retry com o mesmo `requestId` não deve modificar a quantidade novamente.

### Auditoria positiva

```sql
SELECT
    event_id,
    event_type,
    aggregate_type,
    aggregate_id,
    correlation_id,
    status,
    attempts,
    created_at,
    available_at,
    published_at,
    payload_json
FROM audit_outbox_events
WHERE event_type = 'CHEST_OPENED'
ORDER BY created_at DESC;
```

O evento esperado possui `event_type = 'CHEST_OPENED'`, `aggregate_type = 'ChestOpening'` e payload contendo `playerId`, `digimonId`, `requestId`, `chestCode`, `rarity`, itens e quantidades. Após o processamento do Outbox, o status esperado é `PUBLISHED`.

## Cenários de rollback

A collection valida automaticamente o consumo e a idempotência. Para validar rollback de entrega de forma determinística, configure temporariamente uma entrada de loot com quantidade maior que o `max_stack` do item no ambiente local, ou utilize uma tabela de teste administrativamente controlada quando o painel existir. A abertura deverá retornar erro, não consumir o baú, não gravar `chest_openings` e não criar `CHEST_OPENED`.

Depois do teste, restaure a configuração da loot table antes de executar novos cenários. Não altere a pool de produção apenas para realizar este teste.
