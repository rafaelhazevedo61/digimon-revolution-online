# Plano de teste manual — Abertura forçada de ciclo do Boss Mundial

## Objetivo

Validar que um administrador consegue abrir um novo ciclo do Boss Mundial depois da derrota do ciclo atual, sem apagar ou alterar ataques, recompensas, inventário ou auditoria do histórico anterior.

## Pré-requisitos

O backend deve iniciar com a migration V114 aplicada. A migration adiciona `cycle_number` em `world_boss_instances` e substitui a unicidade somente por data por uma unicidade composta de data e ciclo. O administrador deve estar autenticado no painel.

## Fluxo principal

1. Acesse **Ferramentas** no painel administrativo.
2. Localize o card **Forçar novo ciclo do Boss Mundial**.
3. Confirme a operação no diálogo apresentado.
4. A mensagem deve informar que um novo ciclo foi aberto e exibir o número do ciclo.
5. Acesse novamente a tela pública **Boss Mundial**. O Boss deve aparecer como **Em batalha**, com HP cheio e contagem de ataques do jogador zerada para a nova instância.
6. Execute um ataque. O ataque deve ser aceito normalmente, mesmo que o mesmo jogador tenha atacado no ciclo anterior, pois cooldown, limite diário, dano acumulado e recompensas são vinculados à instância do Boss.

## Proteção contra operação indevida

Se a instância mais recente do dia estiver `ACTIVE`, o comando deve retornar HTTP 409 e não criar outra instância. O mesmo deve ocorrer ao executar o comando duas vezes: o primeiro comando cria o novo ciclo ativo e o segundo deve ser rejeitado até que esse novo ciclo seja derrotado.

Se não existir instância do Boss Mundial no dia, o comando também deve retornar HTTP 409. Cancelar a confirmação no navegador não deve fazer nenhuma requisição.

## Queries PostgreSQL

Consultar todos os ciclos do dia:

```sql
SELECT
    id,
    boss_id,
    boss_date,
    cycle_number,
    status,
    created_at,
    defeated_at
FROM world_boss_instances
WHERE boss_date = CURRENT_DATE
ORDER BY cycle_number ASC;
```

Após o fluxo principal, devem existir pelo menos duas linhas para a data atual: a anterior com `status = 'DEFEATED'` e o novo ciclo com `status = 'ACTIVE'`. O `cycle_number` do novo ciclo deve ser maior.

Confirmar que os ataques do ciclo anterior continuam vinculados à instância antiga:

```sql
SELECT
    a.world_boss_id,
    i.cycle_number,
    i.status,
    COUNT(*) AS attacks
FROM world_boss_attacks a
JOIN world_boss_instances i ON i.id = a.world_boss_id
WHERE i.boss_date = CURRENT_DATE
GROUP BY a.world_boss_id, i.cycle_number, i.status
ORDER BY i.cycle_number;
```

Confirmar que as recompensas não foram migradas ou duplicadas:

```sql
SELECT
    r.world_boss_id,
    i.cycle_number,
    r.reward_type,
    r.recipient_player_id,
    COUNT(*) AS rows_found
FROM world_boss_rewards r
JOIN world_boss_instances i ON i.id = r.world_boss_id
WHERE i.boss_date = CURRENT_DATE
GROUP BY r.world_boss_id, i.cycle_number, r.reward_type, r.recipient_player_id
ORDER BY i.cycle_number, r.reward_type;
```

A consulta deve mostrar as recompensas do ciclo anterior na instância antiga. Novas recompensas devem aparecer na nova instância somente após ataques válidos.

## Critérios de aceite

- [ ] O botão exige confirmação antes de abrir um novo ciclo.
- [ ] O novo ciclo é criado somente após a derrota do ciclo atual.
- [ ] O novo ciclo fica `ACTIVE`, com HP inicial do Boss.
- [ ] O novo ciclo recebe `cycle_number` maior que o ciclo anterior.
- [ ] Ataques e recompensas do ciclo anterior permanecem intactos.
- [ ] O mesmo jogador pode iniciar o novo ciclo sem herdar cooldown ou limite de ataques da instância anterior.
- [ ] Uma segunda instância ativa não pode ser criada.
- [ ] A execução em estado inválido retorna HTTP 409, não HTTP 500.
