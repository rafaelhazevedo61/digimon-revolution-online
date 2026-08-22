# Plano de teste manual — Sprint 6: Baús de Arena

## Objetivo

Validar que uma vitória na Arena concede exatamente um Baú correspondente ao tier do atacante **após** a atualização do rating, sem alterar a fórmula de combate, Bits, Moedas de Arena, energia, limites ou cooldowns existentes.

A derrota não concede Baú. A abertura do Baú continua sendo feita pelo fluxo transacional existente em `/inventory/chests/open`; a pool de recompensas não é exibida antes da abertura.

## Pré-requisitos

A branch deve estar baseada na `develop` após o merge do PR #69. O backend deve iniciar para que o Flyway aplique a migration V109. O jogador de teste precisa possuir um Digimon ativo, energia suficiente e um oponente válido no lobby da Arena.

Os códigos seedados são:

| Tier | Baú | Loot Table | Peso de raridade inicial |
|---|---|---|---|
| Bronze | `CHEST_ARENA_BRONZE` | `LOOT_TABLE_ARENA_BRONZE` | Common 80, Rare 15, Epic 4, Legendary 1 |
| Prata | `CHEST_ARENA_PRATA` | `LOOT_TABLE_ARENA_PRATA` | Common 65, Rare 25, Epic 8, Legendary 2 |
| Ouro | `CHEST_ARENA_OURO` | `LOOT_TABLE_ARENA_OURO` | Common 50, Rare 30, Epic 15, Legendary 5 |
| Platina | `CHEST_ARENA_PLATINA` | `LOOT_TABLE_ARENA_PLATINA` | Common 40, Rare 30, Epic 20, Legendary 10 |
| Diamante | `CHEST_ARENA_DIAMANTE` | `LOOT_TABLE_ARENA_DIAMANTE` | Common 30, Rare 30, Epic 25, Legendary 15 |

Os pesos são relativos entre raridades e permanecem editáveis pelo painel administrativo de Loot Tables. As pools usam itens catalogados existentes e materiais de evolução nomeados por `material_code`; os fragmentos genéricos legados não fazem parte das novas pools.

## 1. Validação pela tela pública

Acesse a tela de Arena e confirme que o lobby, os oponentes, a chance de vitória, o rating, a energia, o limite diário, o cooldown e as Moedas de Arena continuam funcionando como antes.

Realize uma partida com vitória. O resultado deve continuar mostrando vitória, rating, Bits e Moedas de Arena, e deve acrescentar o campo **Baú recebido** com um dos cinco nomes oficiais. O nome do Baú deve corresponder ao tier exibido no resultado final.

Realize uma partida com derrota. O resultado não deve exibir Baú recebido. A derrota deve continuar concedendo somente as recompensas já existentes para participação, como Moedas de Arena, conforme a regra atual.

Depois da vitória, abra o inventário. Deve existir exatamente uma unidade do Baú correspondente. A abertura deve ser realizada pelo fluxo de Baús já existente. O retorno da abertura deve informar o conteúdo sorteado, e o inventário deve reduzir o Baú em uma unidade.

## 2. Validação do tier pós-rating

Para validar a regra de fronteira, prepare um jogador próximo de uma mudança de tier e execute uma vitória que ultrapasse o limite. O Baú concedido deve corresponder ao tier **depois** da alteração do rating, e esse mesmo tier deve aparecer no resultado da partida.

Os limites atuais são: Bronze abaixo de 1000, Prata de 1000 até 1199, Ouro de 1200 até 1499, Platina de 1500 até 1899 e Diamante a partir de 1900.

## 3. Validação do painel administrativo

No painel, acesse **Baús Temáticos**. O seletor **Origem** deve permitir filtrar `Área / Missão`, `Arena` e `Boss`. Ao selecionar `Arena`, devem aparecer os cinco Baús de Arena seedados.

Abra um Baú de Arena e confirme que o código, o item de inventário, a Loot Table vinculada, o status e a negociabilidade estão corretos. Em seguida, acesse **Loot Tables**, localize uma das tabelas `LOOT_TABLE_ARENA_*` e confirme que os pesos de raridade e as entradas podem ser editados pelo mecanismo genérico existente.

A pool não deve ser exibida na tela pública da Arena ou no lobby. Ela deve permanecer disponível somente no painel administrativo e na documentação de configuração.

## 4. Queries PostgreSQL

Listar os cinco Baús e suas Loot Tables:

```sql
SELECT
    cd.code AS chest_code,
    cd.name AS chest_name,
    cd.active AS chest_active,
    cd.tradable,
    lt.code AS loot_table_code,
    lt.active AS loot_table_active,
    lt.min_items,
    lt.max_items
FROM chest_definitions cd
JOIN loot_tables lt ON lt.id = cd.loot_table_id
WHERE cd.code LIKE 'CHEST_ARENA_%'
ORDER BY cd.code;
```

Conferir os pesos de raridade:

```sql
SELECT
    lt.code AS loot_table_code,
    rw.rarity,
    rw.weight
FROM loot_table_rarity_weights rw
JOIN loot_tables lt ON lt.id = rw.loot_table_id
WHERE lt.code LIKE 'LOOT_TABLE_ARENA_%'
ORDER BY lt.code, rw.rarity;
```

Conferir as entradas ativas e a existência dos itens catalogados:

```sql
SELECT
    lt.code AS loot_table_code,
    e.rarity,
    e.item_type,
    e.material_code,
    e.weight,
    e.min_quantity,
    e.max_quantity,
    e.active
FROM loot_table_entries e
JOIN loot_tables lt ON lt.id = e.loot_table_id
WHERE lt.code LIKE 'LOOT_TABLE_ARENA_%'
ORDER BY lt.code, e.rarity, e.id;
```

Conferir partidas que concederam Baú:

```sql
SELECT
    am.id AS arena_match_id,
    am.attacker_player_id,
    am.attacker_digimon_id,
    am.attacker_won,
    am.attacker_rating_after,
    cd.code AS reward_chest_code,
    cd.name AS reward_chest_name,
    am.created_at
FROM arena_matches am
LEFT JOIN chest_definitions cd ON cd.id = am.reward_chest_definition_id
WHERE am.attacker_player_id = 'SEU_PLAYER_ID'
ORDER BY am.created_at DESC;
```

Conferir o Baú no inventário do Digimon ativo:

```sql
SELECT
    ii.id,
    ii.digimon_id,
    ii.item_type,
    idf.code AS item_code,
    idf.name AS item_name,
    ii.quantity
FROM inventory_items ii
JOIN item_definitions idf ON idf.id = ii.item_definition_id
WHERE ii.digimon_id = 'SEU_DIGIMON_ID'
  AND idf.code LIKE 'CHEST_ARENA_%'
ORDER BY idf.code;
```

Conferir as aberturas e itens sorteados:

```sql
SELECT
    co.request_id,
    cd.code AS chest_code,
    co.rarity,
    co.opened_at,
    coi.item_type,
    coi.material_code,
    coi.quantity
FROM chest_openings co
JOIN chest_definitions cd ON cd.id = co.chest_definition_id
JOIN chest_opening_items coi ON coi.chest_opening_id = co.id
WHERE co.player_id = 'SEU_PLAYER_ID'
  AND cd.code LIKE 'CHEST_ARENA_%'
ORDER BY co.opened_at DESC, coi.id;
```

## 5. Regressão e critérios de aceite

- [ ] O lobby continua carregando sem alteração visual ou funcional indevida.
- [ ] Uma vitória concede um único Baú correspondente ao tier pós-rating.
- [ ] Uma derrota não concede Baú.
- [ ] Bits, Moedas de Arena, energia, rating, limite diário e cooldown continuam corretos.
- [ ] O Baú aparece no inventário como `LOOT_CHEST` e pode ser aberto por `/inventory/chests/open`.
- [ ] A abertura registra `chest_openings` e `chest_opening_items`.
- [ ] O campo `reward_chest_definition_id` fica preenchido somente em vitórias.
- [ ] O painel filtra os cinco Baús de Arena e permite editar suas Loot Tables.
- [ ] A pool não é exposta ao jogador antes da abertura.
- [ ] Auditoria positiva contém partida, jogador, tier e código do Baú.
