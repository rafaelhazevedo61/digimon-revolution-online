# Plano de teste manual — Sprint 7: Baús de Boss Mundial

## Objetivo

Validar que cada ataque válido ao Boss Mundial concede um Baú por tentativa e que, no ataque que reduz o HP do Boss a zero, são concedidos também um Baú de maior dano acumulado e um Baú de golpe final.

O maior dano é calculado pela soma de todos os ataques de cada jogador na instância diária, considerando somente jogadores que participaram do ciclo que terminou na derrota. Em caso de empate, vence o jogador que atingiu o total primeiro; o UUID é o último critério determinístico de desempate. O golpe final pertence ao jogador cujo ataque reduziu o HP restante a zero.

## Regras de recompensa

| Tipo | Quando é concedido | Destinatário |
| --- | --- | --- |
| `ATTEMPT` | Cada ataque válido | Jogador do ataque |
| `TOP_DAMAGE` | Uma vez, no ataque que derrota o Boss | Jogador com maior dano acumulado na instância |
| `FINAL_BLOW` | Uma vez, no ataque que derrota o Boss | Jogador do golpe final |

Se o mesmo jogador possuir o maior dano acumulado e aplicar o golpe final, receberá dois Baús especiais, além do Baú da tentativa. Ataques posteriores à derrota são rejeitados e não concedem novas recompensas.

## Pré-requisitos

A branch deve estar baseada na `develop` após o merge do PR #70. O backend deve iniciar para que o Flyway aplique as migrations V110, V111, V112 e V113. O Boss Mundial atualmente catalogado é `WORLD_BOSS_APOCALYMON`, com `cooldown_minutes` inicial de 5 minutos e `cooldown_enabled` ativo. O valor permanece configurável no cadastro do Boss, sem ser apagado quando o cooldown é desligado. Os três Baús seguem o padrão inicial:

| Tipo | Baú | Loot Table |
| --- | --- | --- |
| Tentativa | `CHEST_BOSS_WORLD_APOCALYMON_ATTEMPT` | `LOOT_TABLE_BOSS_WORLD_APOCALYMON_ATTEMPT` |
| Maior dano | `CHEST_BOSS_WORLD_APOCALYMON_TOP_DAMAGE` | `LOOT_TABLE_BOSS_WORLD_APOCALYMON_TOP_DAMAGE` |
| Golpe final | `CHEST_BOSS_WORLD_APOCALYMON_FINAL_BLOW` | `LOOT_TABLE_BOSS_WORLD_APOCALYMON_FINAL_BLOW` |

As pools são conservadoras e editáveis pelo painel **Baús Temáticos → Loot Tables**. A tela pública não revela as entradas antes da abertura. No cadastro do Boss Mundial, cada um dos três papéis pode apontar para outro Baú ativo, e cada Baú mantém sua própria Loot Table.

## 1. Baú por tentativa

Acesse **Boss Mundial** e execute um ataque válido. O modal deve exibir o Baú por tentativa. O inventário do Digimon usado deve receber exatamente uma unidade de `CHEST_BOSS_WORLD_APOCALYMON_ATTEMPT`.

Aguarde o cooldown configurado expirar e execute outro ataque válido com uma nova chave `Idempotency-Key`. Um segundo Baú por tentativa deve ser recebido, pois são ataques diferentes. O limite diário continua sendo três ataques por jogador, conforme a regra existente.

Tente atacar novamente antes de o cooldown expirar. A API deve rejeitar a operação, informando o tempo aproximado restante, sem consumir energia, alterar o HP do Boss, conceder XP/Bits ou criar Baú.

Abra um dos Baús pelo fluxo normal de `/inventory/chests/open` e confirme que o consumo, o sorteio e os registros `chest_openings` e `chest_opening_items` funcionam sem alteração.

## 2. Idempotência

Repita exatamente a mesma requisição usando o mesmo valor de `Idempotency-Key`. A API deve retornar o mesmo snapshot do ataque: dano, HP restante, chance, recompensas e ataques restantes. Não deve haver novo consumo de energia, novo ataque, novo ganho de XP/Bits ou novo Baú.

O script cURL e a collection Postman usam `world-boss-test-request-001` como exemplo. Para realizar um ataque novo, altere o valor. Para validar reprocessamento, repita o valor sem alteração.

## 2.1. Cooldown configurável

Altere temporariamente o `cooldown_minutes` do Boss Mundial no painel administrativo ou diretamente no PostgreSQL para um valor curto durante o teste. Após um ataque válido, uma nova tentativa do mesmo jogador antes do intervalo deve retornar erro de cooldown. O bloqueio deve acontecer antes do consumo de energia, do cálculo de dano e da concessão de recompensas.

Confirme que um segundo jogador pode atacar normalmente durante o cooldown do primeiro jogador. O cooldown é individual por jogador e por instância diária, não global para todos os participantes.

A resposta de `GET /world-boss/me` fornece `attackCooldownMinutes` e `nextAttackAvailableAt`. Enquanto esse horário estiver no futuro, o botão deve permanecer desabilitado e mostrar a contagem regressiva em formato `MM:SS` ou `HH:MM:SS`. Quando chegar a zero, a tela deve recarregar o snapshot e habilitar o botão novamente.

O card **“Suas recompensas no ciclo atual”** não deve mais aparecer na tela pública. O Baú deve ser informado no modal do ataque que o concedeu e, quando necessário, no inventário do jogador.

## 3. Derrota, maior dano e golpe final

Para testar as recompensas especiais, use uma instância com HP baixo ou o mecanismo administrativo já existente para preparar o encerramento. Faça ataques de pelo menos dois jogadores e registre o dano de cada um.

No último ataque, confirme que a resposta contém três recompensas quando o jogador do golpe final também for o maior causador de dano, ou que contém a recompensa da tentativa e as recompensas especiais destinadas aos respectivos jogadores conforme o caso.

Depois da derrota, consulte o Boss Mundial novamente. A instância deve aparecer como derrotada e os jogadores devem visualizar somente as recompensas que receberam. O ranking global deve refletir o dano acumulado usado para escolher o Baú de maior dano.

Tente atacar novamente após a derrota. A API deve rejeitar a operação, sem alterar inventário, XP, Bits, ataques ou recompensas.

## 4. Painel administrativo

No painel, acesse **Baús Temáticos** e selecione a origem **Boss**. Os três Baús de Apocalymon devem aparecer. Confirme os códigos, Loot Tables, status ativo e negociabilidade.

Na tela **Bosses**, abra **Editar** no Boss Mundial. O modal deve exibir a seção **Baús do Boss Mundial** com três seletores: **Baú por tentativa**, **Baú de maior dano** e **Baú do golpe final**. Selecione três Baús ativos diferentes e salve. A tabela deve exibir os três vínculos, e cada Baú deve continuar apontando para sua própria Loot Table. Tente repetir um mesmo Baú em duas situações; o painel deve rejeitar o envio antes de salvar e a API também deve impedir a configuração duplicada.

Ainda no mesmo modal, confirme que o checkbox **Ativar cooldown** está marcado e o campo `Cooldown (min)` habilitado com o valor atual. Desmarque o checkbox, salve e confirme que a tabela mostra o cooldown como **Desligado**. O valor em minutos deve permanecer armazenado. Reabra o modal, marque o checkbox novamente e confirme que o valor anterior foi preservado.

Ao desligar o checkbox, o campo de minutos deve ficar visualmente desabilitado, mas seu valor não deve ser apagado. O PUT administrativo deve enviar `cooldownEnabled: false`; ao religar, deve enviar `cooldownEnabled: true` e reutilizar os minutos preservados. Essa operação deve funcionar sem exigir o Baú legado de **Recompensa (Vitória)**, pois esse campo não é usado por Boss Mundial.

Com o cooldown desligado, uma nova tentativa do mesmo jogador não deve ser bloqueada pelo intervalo. Depois de religá-lo, o intervalo configurado deve voltar a ser aplicado e a tela pública deve mostrar a contagem regressiva.

Em **Loot Tables**, verifique as três tabelas `LOOT_TABLE_BOSS_WORLD_APOCALYMON_*`. Os pesos de raridade e as entradas devem ser editáveis pelo mecanismo genérico existente. A desativação de uma Loot Table deve impedir a concessão do Baú correspondente e registrar a falha da operação, sem entregar item parcialmente.

## 5. Queries PostgreSQL

Consultar ataques e snapshot idempotente:

```sql
SELECT
    id,
    world_boss_id,
    player_id,
    digimon_id,
    damage,
    request_id,
    remaining_hp_after,
    win_chance,
    defeated,
    defeated_reward_xp,
    defeated_reward_bits,
    daily_attacks_remaining,
    created_at
FROM world_boss_attacks
WHERE world_boss_id = 'WORLD_BOSS_INSTANCE_ID'
ORDER BY created_at ASC;
```

Calcular o maior dano acumulado por participante:

```sql
SELECT
    player_id,
    SUM(damage) AS total_damage,
    MIN(created_at) AS first_attack_at,
    COUNT(*) AS attacks
FROM world_boss_attacks
WHERE world_boss_id = 'WORLD_BOSS_INSTANCE_ID'
GROUP BY player_id
ORDER BY total_damage DESC, first_attack_at ASC, player_id ASC;
```

Consultar as recompensas oficialmente concedidas:

```sql
SELECT
    r.id,
    r.reward_type,
    r.event_key,
    r.recipient_player_id,
    r.recipient_digimon_id,
    cd.code AS chest_code,
    cd.name AS chest_name,
    r.source_attack_id,
    r.created_at
FROM world_boss_rewards r
JOIN chest_definitions cd ON cd.id = r.chest_definition_id
WHERE r.world_boss_id = 'WORLD_BOSS_INSTANCE_ID'
ORDER BY r.created_at ASC, r.reward_type ASC;
```

Confirmar que não existem dois registros para o mesmo evento:

```sql
SELECT event_key, COUNT(*) AS rows_found
FROM world_boss_rewards
GROUP BY event_key
HAVING COUNT(*) > 1;
```

Essa consulta deve retornar zero linhas. Para conferir a idempotência por ataque:

```sql
SELECT world_boss_id, player_id, request_id, COUNT(*) AS rows_found
FROM world_boss_attacks
WHERE request_id IS NOT NULL
GROUP BY world_boss_id, player_id, request_id
HAVING COUNT(*) > 1;
```

Essa consulta também deve retornar zero linhas.

Consultar a configuração dos Baús no Boss Mundial:

```sql
SELECT
    b.code AS boss_code,
    b.cooldown_minutes,
    b.cooldown_enabled,
    attempt_chest.code AS attempt_chest_code,
    attempt_chest.loot_table_id AS attempt_loot_table_id,
    top_damage_chest.code AS top_damage_chest_code,
    top_damage_chest.loot_table_id AS top_damage_loot_table_id,
    final_blow_chest.code AS final_blow_chest_code,
    final_blow_chest.loot_table_id AS final_blow_loot_table_id
FROM boss_definitions b
LEFT JOIN chest_definitions attempt_chest
    ON attempt_chest.id = b.world_attempt_chest_definition_id
LEFT JOIN chest_definitions top_damage_chest
    ON top_damage_chest.id = b.world_top_damage_chest_definition_id
LEFT JOIN chest_definitions final_blow_chest
    ON final_blow_chest.id = b.world_final_blow_chest_definition_id
WHERE b.boss_type = 'WORLD'
  AND b.code = 'WORLD_BOSS_APOCALYMON';
```

As três colunas de Baú devem estar preenchidas e apontar para códigos diferentes. Para consultar os códigos das Loot Tables associadas de forma mais legível:

```sql
SELECT
    b.code AS boss_code,
    role.reward_role,
    c.code AS chest_code,
    c.name AS chest_name,
    lt.code AS loot_table_code,
    lt.active AS loot_table_active
FROM boss_definitions b
CROSS JOIN LATERAL (
    VALUES
        ('ATTEMPT', b.world_attempt_chest_definition_id),
        ('TOP_DAMAGE', b.world_top_damage_chest_definition_id),
        ('FINAL_BLOW', b.world_final_blow_chest_definition_id)
) AS role(reward_role, chest_id)
JOIN chest_definitions c ON c.id = role.chest_id
LEFT JOIN loot_tables lt ON lt.id = c.loot_table_id
WHERE b.code = 'WORLD_BOSS_APOCALYMON'
ORDER BY role.reward_role;
```

Consultar os Baús de um jogador:

```sql
SELECT
    ii.id,
    ii.digimon_id,
    idf.code AS chest_code,
    idf.name AS chest_name,
    ii.quantity
FROM inventory_items ii
JOIN item_definitions idf ON idf.id = ii.item_definition_id
WHERE ii.digimon_id = 'SEU_DIGIMON_ID'
  AND idf.code LIKE 'CHEST_BOSS_WORLD_%'
ORDER BY idf.code;
```

## Critérios de aceite

- [x] Cada ataque válido entrega um Baú `ATTEMPT` ao jogador atacante.

- [x] O mesmo `Idempotency-Key` não cria ataque, consumo ou Baú adicional.

- [ ] O maior dano usa a soma acumulada de todos os ataques da instância.

- [ ] O maior dano considera somente participantes do ciclo derrotado.

- [ ] O Baú `TOP_DAMAGE` é concedido uma única vez no encerramento.

- [ ] O Baú `FINAL_BLOW` é concedido uma única vez ao jogador do ataque final.

- [ ] Um jogador pode receber simultaneamente `TOP_DAMAGE` e `FINAL_BLOW`.

- [ ] Ataques posteriores à derrota são rejeitados.

- [ ] O cooldown configurável bloqueia ataques repetidos do mesmo jogador antes do prazo.

- [ ] O valor padrão do cooldown é cinco minutos quando o cadastro não possui valor positivo.

- [ ] O cooldown de um jogador não bloqueia os demais participantes.

- [ ] O card redundante de recompensas do ciclo não aparece na tela pública.

- [ ] A abertura dos Baús usa o fluxo transacional já existente.

- [x] O painel lista e edita os três Baús do Boss Mundial.

- [ ] Cada situação do Boss Mundial usa o Baú configurado para seu próprio papel e sua própria Loot Table.

- [ ] A edição somente do cooldown não exige o Baú legado de Boss normal/periódico.

- [ ] Baús vinculados a qualquer um dos três papéis não podem ser desativados pelo painel.

- [ ] As consultas de duplicidade retornam zero linhas.

- [ ] A auditoria positiva contém instância, jogador, dano, estado e Baús concedidos.
