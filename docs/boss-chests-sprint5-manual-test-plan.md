# Sprint 5 — Baús de Bosses

## Objetivo

A Sprint 5 introduz Baús de recompensa para Bosses normais e periódicos. A implementação usa a **Opção B**: os drops diretos de itens catalogados deixam de ser concedidos no combate e passam para uma Loot Table vinculada ao Baú; os drops de equipamentos permanecem no fluxo legado nesta sprint.

O fluxo resultante é:

```text
Boss elegível → vitória → XP + Bits + Baú → abertura transacional do Baú → itens da Loot Table
```

Uma derrota não concede Baú. O Boss precisa possuir um Baú ativo com Loot Table ativa; caso contrário, a vitória é rejeitada em transação para não conceder XP/Bits sem a recompensa configurada.

## Tipos contemplados

A V107 cria um Baú e uma Loot Table individual para cada Boss com os tipos `NORMAL`, `DAILY`, `WEEKLY` e `MONTHLY`.

| Tipo | Prefixo do Baú | Prefixo da Loot Table |
|---|---|---|
| Normal | `CHEST_BOSS_` | `LOOT_TABLE_BOSS_NORMAL_` |
| Diário | `CHEST_BOSS_DAILY_` | `LOOT_TABLE_BOSS_DAILY_` |
| Semanal | `CHEST_BOSS_WEEKLY_` | `LOOT_TABLE_BOSS_WEEKLY_` |
| Mensal | `CHEST_BOSS_MONTHLY_` | `LOOT_TABLE_BOSS_MONTHLY_` |

As Loot Tables seedadas começam com uma entrada conservadora em `COMMON`, com quantidade de itens igual a 1. Elas devem ser balanceadas pelo painel administrativo antes do uso definitivo. Raridades sem entradas ativas continuam sendo ignoradas pelo sorteador.

## Preparação

Atualize a branch da Sprint 5 e reinicie o backend para executar a migration V107. Confirme a migration:

```sql
SELECT version, description, success
FROM flyway_schema_history
WHERE version = '107';
```

Confirme os vínculos criados:

```sql
SELECT
    b.code AS boss_code,
    b.boss_type,
    b.active AS boss_active,
    cd.code AS chest_code,
    cd.active AS chest_active,
    lt.code AS loot_table_code,
    lt.active AS loot_table_active,
    COUNT(lte.id) AS loot_entries
FROM boss_definitions b
LEFT JOIN chest_definitions cd ON cd.id = b.chest_definition_id
LEFT JOIN loot_tables lt ON lt.id = cd.loot_table_id
LEFT JOIN loot_table_entries lte ON lte.loot_table_id = lt.id
WHERE b.boss_type IN ('NORMAL', 'DAILY', 'WEEKLY', 'MONTHLY')
GROUP BY b.code, b.boss_type, b.active, cd.code, cd.active, lt.code, lt.active
ORDER BY b.boss_type, b.code;
```

Cada Boss normal/periódico deve possuir Baú, Loot Table ativa e pelo menos uma entrada.

## Validação administrativa

Abra **Administração → Bosses**.

1. A tabela deve exibir o Baú de Recompensa de cada Boss.
2. O endpoint de opções deve retornar somente Baús ativos com Loot Table ativa:

```text
GET /admin/bosses/chest-options
```

3. Ao criar um Boss `NORMAL`, `DAILY`, `WEEKLY` ou `MONTHLY`, o formulário deve exigir a seleção de um Baú.
4. Ao editar um Boss, deve ser possível trocar o Baú por outro Baú ativo.
5. Um Baú inexistente, inativo ou com Loot Table inativa deve retornar HTTP 409.
6. Bosses `CLAN` e `WORLD` permanecem sem exigência de Baú nesta sprint, pois serão tratados nas sprints específicas.

## Validação do combate

Na tela pública de Bosses, o detalhe deve mostrar o nome do Baú de vitória, mas não deve mostrar a pool da Loot Table.

Ao vencer um Boss, a resposta deve conter:

```json
{
  "result": "VICTORY",
  "chestCode": "CHEST_BOSS_EXEMPLO",
  "chestName": "Baú Boss — Exemplo",
  "drops": [
    {
      "type": "CHEST",
      "code": "CHEST_BOSS_EXEMPLO",
      "quantity": 1
    }
  ]
}
```

O Baú deve aparecer no inventário do jogador. Equipamentos legados, quando sorteados, continuam sendo entregues e aparecem junto ao Baú. Itens consumíveis que estavam em `boss_drops` não devem ser duplicados como drops diretos; eles devem estar na Loot Table e só devem aparecer após a abertura do Baú.

Uma derrota deve retornar `DEFEAT`, conceder apenas o XP de derrota já previsto e não conceder Baú.

## Validação de abertura

Use o fluxo já existente de abertura de Baús:

```text
Inventário → Baú de Boss → Abrir
```

Confirme que a abertura usa `/inventory/chests/open`, consome uma unidade do Baú, cria `chest_openings` e `chest_opening_items`, e entrega a recompensa da Loot Table.

A mesma requisição repetida com o mesmo `requestId` deve ser idempotente. Uma nova abertura deve exigir outro `requestId` e outra unidade disponível no inventário.

## Validação de proteção operacional

Tente desativar um Baú que esteja vinculado a um Boss:

```text
Resultado esperado: HTTP 409
Mensagem: não é possível desativar o Baú porque ele está vinculado a um ou mais Bosses.
```

A regra deve funcionar tanto pelo botão de ativação quanto pelo `PUT` administrativo. Também não deve ser possível desativar a Loot Table do Baú, conforme a proteção implementada na Sprint anterior.

## Consultas de conferência

Inventário do jogador:

```sql
SELECT
    ii.id,
    ii.digimon_id,
    ii.item_type,
    ii.item_definition_id,
    idf.code AS item_code,
    idf.name AS item_name,
    ii.quantity
FROM inventory_items ii
LEFT JOIN item_definitions idf ON idf.id = ii.item_definition_id
WHERE ii.digimon_id = 'DIGIMON_ID'
  AND (ii.item_type = 'LOOT_CHEST' OR idf.category = 'CHEST')
ORDER BY ii.id;
```

Resultado da abertura:

```sql
SELECT
    co.id AS opening_id,
    co.request_id,
    cd.code AS chest_code,
    coi.rarity,
    coi.item_type,
    coi.material_code,
    coi.quantity
FROM chest_openings co
JOIN chest_definitions cd ON cd.id = co.chest_definition_id
JOIN chest_opening_items coi ON coi.chest_opening_id = co.id
WHERE co.player_id = 'PLAYER_ID'
ORDER BY co.opened_at DESC, coi.id;
```

Tentativas do Boss:

```sql
SELECT
    ba.id,
    b.code AS boss_code,
    b.boss_type,
    ba.status,
    ba.xp_gained,
    ba.bits_gained,
    ba.damage_dealt,
    ba.created_at
FROM boss_attempts ba
JOIN boss_definitions b ON b.id = ba.boss_id
WHERE ba.player_id = 'PLAYER_ID'
ORDER BY ba.created_at DESC;
```

A tentativa de vitória deve possuir XP/Bits e o inventário deve possuir o Baú correspondente. A tentativa de derrota não deve alterar o inventário com Baú.

## Limites da Sprint 5

Equipamentos continuam sendo sorteados e concedidos pelo fluxo legado. Boss Mundial (`WORLD`) e Boss de Clã (`CLAN`) não recebem Baú nesta sprint; serão tratados nas sprints específicas de Boss Mundial e demais raids.
