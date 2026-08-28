# Especificação técnica — Calendário de Atividades Diário

**Projeto:** Digimon Revolution Online  
**Autor:** Manus AI  
**Data do estudo:** 27 de agosto de 2026  
**Branch analisada:** `develop`  
**Status:** estudo e desenho; nenhuma implementação proposta neste documento foi aplicada.

## 1. Resumo executivo

O Calendário de Atividades Diário será um ciclo mensal em que cada jogador precisa alcançar uma meta fixa de pontos em cada dia do mês para desbloquear uma recompensa. O calendário não será um check-in de login: o jogador deverá realizar ações válidas dentro do jogo. Ao atingir a meta do dia, poderá resgatar um **Baú do Calendário de Atividades**, cuja Loot Table será administrada pelo painel Admin. Se o jogador resgatar a recompensa de **todos os dias do mês**, também desbloqueará uma única recompensa adicional: o **Baú de Conclusão Mensal**, exclusivo por completar o calendário inteiro.

A proposta recomendada é separar o sistema em três responsabilidades. A primeira registra eventos de gameplay concluídos com segurança. A segunda mantém o agregado diário e o estado de resgate do jogador. A terceira expõe o calendário para o frontend e associa a recompensa a um Chest configurado no catálogo existente. Essa separação evita duplicar regras em cada módulo e preserva a arquitetura atual de Chest, Loot Table, auditoria e transações.

> **Decisão principal:** pontuar a conclusão de uma ação persistida, e não o simples clique ou a abertura da tela. Assim, uma missão só pontua quando é resgatada, um ovo só pontua quando é efetivamente chocado, e cada ataque válido de Arena, Clan Raid, World Boss ou Boss pontua quando o ataque é salvo.

## 2. Escopo e premissas

O calendário tem vigência no mês-calendário corrente. Um mês com 28, 29, 30 ou 31 dias terá exatamente essa quantidade de dias disponíveis; portanto, o requisito de fevereiro deve considerar também ano bissexto. A meta diária é a mesma para todos os dias do mês e deve ser carregada da configuração do backend. Os pontos acumulados não atravessam a virada do mês.

As recompensas serão itens de Chest, não um processamento especial de Loot. O Chest diário terá uma Loot Table normal, e o Chest de Conclusão Mensal terá outra Loot Table, obrigatoriamente diferente e configurada de forma independente. Ambos poderão conter itens, moedas e outros conteúdos que o sistema atual já saiba sortear. A criação ou alteração da composição das Loot Tables continuará sendo feita no Admin, conforme solicitado, sem seed de drops obrigatória nesta primeira entrega.

Não fazem parte desta especificação a distribuição de itens nas Loot Tables, a alteração dos drops de Montanha Infinita, a criação de uma migration de conteúdo de loot ou a implementação do frontend. O documento define os contratos e pontos de integração necessários para uma implementação posterior.

## 3. Inventário dos modos de jogo

O estudo encontrou os seguintes fluxos relevantes no estado atual da `develop`.

| Fonte de atividade | Evento recomendado | Ponto de integração observado | Registra tentativa ou conclusão? | Observação |
|---|---|---|---|---|
| Missões | `MISSION_COMPLETED` | `ClaimMissionUseCase.execute` | Conclusão/resgate | A instância é validada, recompensas são aplicadas e depois marcada como reivindicada. [1] |
| Arena | `ARENA_MATCH_COMPLETED` | `ChallengeArenaUseCase.execute` | Partida válida | Vitória e derrota persistem uma `ArenaMatch`; a recomendação é pontuar ambas, pois a ação do jogador ocorreu. [2] |
| Clan Raid | `CLAN_RAID_ATTACK` | `AttackClanRaidUseCase.execute` | Ataque válido | Cada ataque gera `ClanRaidAttack`, mesmo quando não derrota o raid. [3] |
| World Boss | `WORLD_BOSS_ATTACK` | `AttackWorldBossUseCase.execute` | Ataque válido | O fluxo possui chave de idempotência e persiste `WorldBossAttack`; o evento deve ser emitido somente na primeira execução real. [4] |
| Boss normal/diário/semanal/mensal | `BOSS_CHALLENGE_COMPLETED` | `ChallengeBossUseCase.execute` | Tentativa concluída | O enum atual suporta `NORMAL`, `DAILY`, `WEEKLY`, `MONTHLY`, `CLAN` e `WORLD`; Clan e World são bloqueados nesse caso de uso por possuírem fluxos dedicados. [5] |
| Incubação | `DIGITAMA_HATCHED` | `ClaimIncubationUseCase.execute` | Eclosão efetiva | `StartIncubationUseCase` apenas consome o Digitama e o incubador; a criação do Digimon ocorre no claim. [6] |

A ação de Arena deve ser configurável quanto a vitória e derrota. Por padrão, recomenda-se pontuar qualquer partida válida, porque o requisito descreve “lutar na arena” e não “vencer na arena”. A mesma regra deve ser aplicada a Bosses: a configuração pode definir se derrota concede pontos, mas o comportamento inicial mais simples é pontuar toda tentativa que chegou ao resultado e foi persistida.

Não foi identificado outro modo de jogo atual que precise obrigatoriamente ser incluído no primeiro escopo. Sistemas futuros, como eventos temporários, PvP adicional ou atividades administrativas, devem ser integrados por um novo `ActivitySource`, sem alterar as tabelas existentes.

## 4. Regras de negócio

### 4.1. Calendário mensal

O calendário é identificado por `year_month`, no formato `YYYY-MM`, e pela data local do servidor. O mês deve ser calculado no backend com `YearMonth.lengthOfMonth()`, nunca no frontend. O primeiro acesso do jogador ao mês não precisa criar 28–31 linhas antecipadamente: é preferível criar o registro diário sob demanda ou materializar os dias quando o calendário for consultado, desde que o resultado apresentado seja determinístico.

Cada jogador terá no máximo um agregado por mês e uma linha por dia. O dia usa `LocalDate`, não apenas o número do dia, para evitar ambiguidades em consultas e auditoria. A virada do mês naturalmente direciona novos eventos para outro `year_month`.

### 4.2. Pontuação

A pontuação de cada evento é obtida de uma configuração por fonte. O agregado diário soma os pontos recebidos e pode limitar a contribuição de uma fonte com um teto diário opcional. A meta diária é única e igual em todos os dias do mês.

A fórmula funcional é:

```text
pontos_do_dia = soma(pontos_de_eventos_aceitos_no_dia)
meta_atingida = pontos_do_dia >= meta_diaria
pode_resgatar = meta_atingida AND reward_claimed_at IS NULL
```

O total não deve ser reduzido quando o jogador resgata a recompensa. Depois do resgate, a interface deve mostrar o dia como concluído e impedir novo resgate.

### 4.3. Evento válido e idempotência

Cada evento deve carregar um `source`, uma `source_reference_id` e uma chave de idempotência. A chave recomendada é derivada da entidade persistida, por exemplo `MISSION:<missionInstanceId>`, `ARENA:<arenaMatchId>`, `CLAN_RAID:<attackId>`, `WORLD_BOSS:<attackId>`, `BOSS:<attemptId>` e `HATCH:<incubationId>`.

A tabela de eventos deve ter uma restrição única sobre `(player_id, source, source_reference_id)`. Dessa forma, retries HTTP, reprocessamento de outbox e chamadas concorrentes não concedem pontos duas vezes. O registro do evento e a atualização do agregado diário devem ocorrer na mesma transação da ação de gameplay sempre que o módulo já estiver transacional.

### 4.4. Reward Chests

Recomenda-se separar claramente dois códigos estáveis:

| Finalidade | Código sugerido | Elegibilidade |
|---|---|---|
| Recompensa diária | `CHEST_ACTIVITY_CALENDAR` | Meta do dia atingida e recompensa diária ainda não resgatada. |
| Bônus de conclusão mensal | `CHEST_ACTIVITY_CALENDAR_MONTHLY` | Todos os dias do mês tiveram a recompensa diária resgatada e o bônus mensal ainda não foi resgatado. |

O calendário não deve sortear loot diretamente nem guardar cópia da composição dos baús. No resgate diário ou mensal, o backend valida que o Chest correspondente está ativo, que sua Loot Table está ativa e então adiciona uma unidade do item Chest ao inventário, seguindo o padrão utilizado por Arena, Boss e World Boss. [2] [4] [5]

O baú mensal deve ser concedido **uma única vez por jogador e por mês**. A elegibilidade depende de resgates, não apenas de metas atingidas: atingir a meta de todos os dias sem clicar em um dos resgates diários não libera o bônus. Quando o último dia pendente for resgatado, o backend deve marcar `monthly_completion_eligible_at` e informar na resposta que o bônus mensal ficou disponível. Recomenda-se que o baú mensal seja resgatado por um endpoint separado, com um clique explícito do jogador, permitindo retentativas seguras caso a concessão falhe e deixando clara a diferença entre a recompensa diária e o bônus de conclusão.

A adoção de dois Chests permite que a recompensa de conclusão seja realmente exclusiva, com Loot Table independente. A composição de ambos deve permanecer editável no Admin e não deve exigir migration de conteúdo.

## 5. Modelo de persistência proposto

A nomenclatura abaixo é conceitual e deve ser ajustada às convenções de migrations da branch no momento da implementação. Não criar as tabelas nesta fase.

### 5.1. `activity_calendar_daily`

| Coluna | Tipo sugerido | Regras |
|---|---|---|
| `id` | `uuid` | PK |
| `player_id` | `uuid` | FK para player; obrigatório |
| `activity_date` | `date` | obrigatório |
| `year_month` | `varchar(7)` | obrigatório; derivado de `activity_date` |
| `points` | `integer` | não nulo; default 0; nunca negativo |
| `goal_reached_at` | `timestamptz` | nulo até atingir a meta |
| `reward_claimed_at` | `timestamptz` | nulo até o resgate |
| `created_at` | `timestamptz` | obrigatório |
| `updated_at` | `timestamptz` | obrigatório |
| `version` | `bigint` | opcional, se a entidade seguir optimistic locking |

Restrições: `unique(player_id, activity_date)`, `check(points >= 0)`, e `check(reward_claimed_at is null or goal_reached_at is not null)`.

### 5.2. `activity_point_events`

| Coluna | Tipo sugerido | Regras |
|---|---|---|
| `id` | `uuid` | PK |
| `player_id` | `uuid` | FK para player |
| `activity_date` | `date` | data de negócio do evento |
| `source` | `varchar(40)` | enum persistido como texto |
| `source_reference_id` | `varchar(120)` | id da entidade de origem |
| `points` | `integer` | pontos efetivamente concedidos |
| `metadata` | `jsonb` | contexto opcional para auditoria |
| `created_at` | `timestamptz` | instante de registro |

Restrições: `unique(player_id, source, source_reference_id)`, `check(points > 0)`, e índice por `(player_id, activity_date)`. O `metadata` não deve ser usado para recalcular pontuação; serve somente para diagnóstico e auditoria.

### 5.3. `activity_calendar_monthly`

Para o bônus de conclusão, recomenda-se uma linha mensal explícita em vez de inferir o estado apenas por consultas repetidas às linhas diárias.

| Coluna | Tipo sugerido | Regras |
|---|---|---|
| `id` | `uuid` | PK |
| `player_id` | `uuid` | FK para player |
| `year_month` | `varchar(7)` | obrigatório; `YYYY-MM` |
| `total_days` | `smallint` | quantidade de dias do mês no momento da criação |
| `claimed_days` | `smallint` | quantidade de recompensas diárias resgatadas |
| `monthly_completion_eligible_at` | `timestamptz` | preenchido quando `claimed_days = total_days` |
| `monthly_reward_claimed_at` | `timestamptz` | preenchido após conceder o baú mensal |
| `created_at` | `timestamptz` | obrigatório |
| `updated_at` | `timestamptz` | obrigatório |

Restrições: `unique(player_id, year_month)`, `check(total_days between 28 and 31)`, `check(claimed_days between 0 and total_days)` e `check(monthly_reward_claimed_at is null or monthly_completion_eligible_at is not null)`. A atualização de `claimed_days` deve ser idempotente e ocorrer na mesma transação do resgate diário.

### 5.4. Configuração de ciclo

Não é necessário criar uma tabela de configuração de ciclo para o primeiro escopo. A meta, os valores dos eventos e os dois códigos de Chest ficam no `application.yaml`, como solicitado. O banco armazena somente o resultado efetivamente concedido. Se no futuro a meta precisar mudar sem deploy, poderá ser criada uma entidade de configuração versionada, mas isso deve ser uma decisão explícita para não misturar configuração operacional com estado de jogador.

## 6. Configuração proposta no `application.yaml`

A configuração deve seguir o agrupamento `gameplay` já existente, que atualmente contém limites de Arena, cooldowns de Clan Raid e World Boss e consumo de energia. [7]

```yaml
gameplay:
  activity-calendar:
    enabled: ${DRO_ACTIVITY_CALENDAR_ENABLED:true}
    daily-goal: ${DRO_ACTIVITY_CALENDAR_DAILY_GOAL:10}
    reward-chest-code: ${DRO_ACTIVITY_CALENDAR_REWARD_CHEST_CODE:CHEST_ACTIVITY_CALENDAR}
    monthly-completion-chest-code: ${DRO_ACTIVITY_CALENDAR_MONTHLY_CHEST_CODE:CHEST_ACTIVITY_CALENDAR_MONTHLY}
    points:
      mission-completed: ${DRO_ACTIVITY_CALENDAR_POINTS_MISSION:1}
      arena-match: ${DRO_ACTIVITY_CALENDAR_POINTS_ARENA:1}
      clan-raid-attack: ${DRO_ACTIVITY_CALENDAR_POINTS_CLAN_RAID:1}
      world-boss-attack: ${DRO_ACTIVITY_CALENDAR_POINTS_WORLD_BOSS:1}
      boss-challenge: ${DRO_ACTIVITY_CALENDAR_POINTS_BOSS:1}
      digitama-hatched: ${DRO_ACTIVITY_CALENDAR_POINTS_HATCH:1}
    limits:
      mission-completed: ${DRO_ACTIVITY_CALENDAR_LIMIT_MISSION:0}
      arena-match: ${DRO_ACTIVITY_CALENDAR_LIMIT_ARENA:0}
      clan-raid-attack: ${DRO_ACTIVITY_CALENDAR_LIMIT_CLAN_RAID:0}
      world-boss-attack: ${DRO_ACTIVITY_CALENDAR_LIMIT_WORLD_BOSS:0}
      boss-challenge: ${DRO_ACTIVITY_CALENDAR_LIMIT_BOSS:0}
      digitama-hatched: ${DRO_ACTIVITY_CALENDAR_LIMIT_HATCH:0}
```

O valor `0` em `limits` deve significar “sem teto”, e não “não pontua”. Valores negativos devem ser rejeitados na inicialização. `daily-goal` deve ser maior que zero quando o recurso estiver habilitado. A configuração também deve validar que os dois códigos de Chest não estão vazios e não são iguais. O código do bônus mensal não deve ser opcional quando o calendário estiver habilitado, pois a conclusão integral do mês é uma recompensa prevista pelo sistema.

Recomenda-se encapsular o bloco em `ActivityCalendarConfig`, com métodos como `pointsFor(ActivitySource source)` e `limitFor(ActivitySource source)`. Os casos de uso não devem acessar propriedades YAML diretamente.

## 7. Arquitetura e pontos de integração

A implementação futura deve criar um serviço transversal, por exemplo `ActivityPointService`, no módulo `activitycalendar`. Os casos de uso de origem chamam esse serviço depois que a ação foi validada e a entidade de origem foi construída. O serviço registra o evento idempotente, atualiza o agregado da data local e marca `goal_reached_at` quando o total cruza a meta.

| Caso de uso | Momento sugerido da chamada |
|---|---|
| `ClaimMissionUseCase` | Após `instance.markClaimed()` e antes do commit; usar `missionInstanceId` como referência. |
| `ChallengeArenaUseCase` | Depois de construir e salvar a partida, tanto para vitória quanto para derrota se a configuração aceitar ambas. |
| `AttackClanRaidUseCase` | Depois de criar `ClanRaidAttack`; não depender de `defeated`, pois cada ataque é uma ação válida. |
| `AttackWorldBossUseCase` | Depois de salvar o novo `WorldBossAttack`; não chamar no caminho de retorno idempotente. |
| `ChallengeBossUseCase` | Depois de salvar `BossAttemptEntity`; usar `attempt.id`, independentemente do resultado conforme configuração. |
| `ClaimIncubationUseCase` | Depois de finalizar a incubação e criar o Digimon; usar `incubationId`. Não pontuar `StartIncubationUseCase`. |

Como todos esses fluxos já usam `@Transactional`, o ponto deve ser integrado na mesma unidade transacional. [1] [2] [3] [4] [5] [6] Se o serviço de atividade falhar, a ação de origem também deve falhar e ser revertida, evitando conceder a recompensa principal sem registrar o ponto. Uma alternativa assíncrona via outbox é possível, mas não é recomendada para a primeira versão porque atrasaria a atualização visual e exigiria uma política adicional para eventos pendentes.

## 8. API do calendário

Propõe-se um controller autenticado com os seguintes contratos.

| Método | Endpoint | Finalidade |
|---|---|---|
| `GET` | `/activity-calendar/current` | Retorna o mês corrente, meta, total de dias, dia atual e estado de cada dia. |
| `POST` | `/activity-calendar/days/{date}/claim` | Resgata a recompensa do dia informado, validando que a data pertence ao mês corrente e que a meta foi alcançada. |
| `POST` | `/activity-calendar/months/{yearMonth}/claim-completion` | Resgata o Chest exclusivo de conclusão mensal, uma única vez, após todos os dias terem sido resgatados. |

O `GET` deve retornar `yearMonth`, `dailyGoal`, `rewardChestCode`, `monthlyCompletionChestCode`, `daysInMonth`, `currentDate`, `currentDayPoints`, `currentDayGoalReached`, `claimedDays`, `monthlyCompletionEligible`, `monthlyRewardClaimed` e uma lista de dias. Cada dia deve conter `date`, `dayOfMonth`, `points`, `goalReached`, `rewardClaimed` e `rewardClaimedAt`. O campo `monthlyCompletionEligible` deve ser verdadeiro somente quando todos os dias do mês estiverem com `rewardClaimed = true`.

O resgate diário deve ser transacional, bloquear ou atualizar de forma otimista a linha diária e retornar conflito quando outro request já tiver resgatado o prêmio. Depois de um resgate bem-sucedido, o serviço incrementa `claimed_days` uma única vez. Se esse incremento fizer `claimed_days = total_days`, o serviço preenche `monthly_completion_eligible_at` e a resposta informa que o bônus mensal está disponível. O endpoint não deve aceitar resgate de datas futuras, datas de meses anteriores ou de um dia sem meta atingida. O resgate deve conceder apenas o item Chest; a abertura do Chest continua no fluxo já existente de inventário.

O endpoint mensal deve aceitar somente o `yearMonth` corrente, exigir `monthly_completion_eligible_at IS NOT NULL` e executar uma atualização condicional em `monthly_reward_claimed_at IS NULL`. Em caso de retry após sucesso, deve retornar o estado já resgatado sem conceder um segundo Chest. O bônus mensal não deve ser automaticamente transferido para o mês seguinte.

## 9. Integração do painel Admin

O painel Admin já possui controllers separados para Chest e Loot Table. [8] [9] Portanto, não é necessário criar uma tela de loot exclusiva para o calendário no primeiro escopo. O trabalho de Admin consiste em cadastrar o item Chest, associá-lo a uma Loot Table ativa e editar essa Loot Table pelo CRUD existente.

A configuração do calendário deve expor, no painel de configuração do servidor se esse recurso já existir, pelo menos a meta diária, os pontos por fonte, os limites opcionais e o código do Chest. Caso a administração de `application.yaml` não seja dinâmica no produto atual, esses valores devem permanecer somente em configuração de deploy e o Admin deve administrar apenas a Loot Table.

O checklist operacional para o Admin é:

1. Criar ou confirmar os dois itens no catálogo: `CHEST_ACTIVITY_CALENDAR` para a recompensa diária e `CHEST_ACTIVITY_CALENDAR_MONTHLY` para o bônus exclusivo de conclusão.
2. Criar uma Loot Table, por exemplo `LOOT_ACTIVITY_CALENDAR`, e associá-la ao Chest diário.
3. Criar uma segunda Loot Table, por exemplo `LOOT_ACTIVITY_CALENDAR_MONTHLY`, e associá-la ao Chest mensal; ela deve conter uma composição exclusiva e não ser a mesma Loot Table do prêmio diário.
4. Manter os dois Chests e as duas Loot Tables ativos antes de liberar o calendário.
5. Configurar os itens e quantidades manualmente, sem migration de conteúdo nesta fase.
6. Configurar `reward-chest-code` e `monthly-completion-chest-code` com os códigos cadastrados.
7. Validar em ambiente de teste o resgate diário, o desbloqueio após o último dia, o resgate do baú mensal e a impossibilidade de recebê-lo duas vezes.

## 10. Frontend e experiência do jogador

A tela deve apresentar o mês completo em uma grade adaptável, contendo 28, 29, 30 ou 31 células. Cada célula mostra o número do dia, os pontos acumulados, a meta e um estado visual claro: bloqueado, em progresso, meta atingida ou recompensa resgatada. A tela também deve exibir uma seção de **Conclusão Mensal**, com a contagem `claimedDays / daysInMonth`, o estado “disponível” quando todos os dias forem resgatados e um botão separado para o Chest exclusivo mensal. O frontend deve consumir `daysInMonth` e a lista devolvida pelo backend, sem assumir 30 ou 31 dias.

O botão de resgate deve aparecer somente para o dia atual elegível, salvo se o produto decidir permitir resgates retroativos durante o mesmo mês. A recomendação é permitir o resgate de qualquer dia já concluído e ainda não resgatado, mas restringir o uso ao mês corrente. Isso evita punir o jogador por não ter clicado imediatamente após atingir a meta, sem abrir um calendário histórico infinito.

O total de pontos deve atualizar após cada ação de gameplay por uma nova consulta ou por atualização do estado retornado pela ação. Não é necessário que cada tela de missão, Arena ou Boss implemente regras visuais próprias; o calendário é a fonte de verdade para a progressão.

## 11. Concorrência, segurança e auditoria

O serviço deve extrair o jogador do token no backend e nunca aceitar `playerId` vindo do corpo da requisição. A data usada para pontuar deve ser calculada no servidor, com o fuso horário oficial da aplicação. A recomendação é definir explicitamente uma propriedade `gameplay.time-zone`, em vez de depender silenciosamente do timezone da máquina.

O registro único de evento protege contra duplicidade, enquanto a atualização do agregado deve usar `INSERT ... ON CONFLICT`, lock pessimista ou retry de optimistic locking. O resgate exige uma segunda proteção: `unique(player_id, activity_date)` combinado com uma atualização condicional que somente prossegue quando `reward_claimed_at IS NULL`.

Cada pontuação e resgate deve publicar auditoria com jogador, fonte, referência, pontos, data, total após o evento e código do Chest. O sistema já utiliza um publicador de auditoria transacional nos fluxos de missão, Arena, Boss e World Boss, o que deve ser reaproveitado. [1] [2] [4] [5]

## 12. Testes de aceitação

| Cenário | Resultado esperado |
|---|---|
| Mês de 28 dias | API devolve 28 dias; não existe dia 29, 30 ou 31. |
| Fevereiro bissexto | API devolve 29 dias. |
| Mês de 30 dias | API devolve 30 dias. |
| Mês de 31 dias | API devolve 31 dias. |
| Evento repetido com a mesma origem | Apenas o primeiro concede pontos. |
| Dois requests simultâneos do mesmo evento | Um evento é persistido; o outro é ignorado ou tratado como idempotente. |
| Meta atingida | `goal_reached_at` é preenchido e o resgate fica disponível. |
| Resgate repetido | O segundo request não concede outro Chest. |
| Resgate sem meta | Retorna erro de negócio e não altera inventário. |
| Ataque de World Boss repetido com a mesma idempotency key | Retorna a resposta existente sem pontuar novamente. |
| `StartIncubationUseCase` | Não concede ponto. |
| `ClaimIncubationUseCase` bem-sucedido | Concede um ponto de eclosão, uma única vez. |
| Chest diário inativo ou Loot Table diária inativa | Resgate diário falha sem marcar o dia como resgatado. |
| Chest mensal inativo ou Loot Table mensal inativa | Resgate mensal falha sem marcar o bônus como resgatado. |
| Todos os dias resgatados | Bônus mensal fica elegível exatamente uma vez. |
| Meta atingida em todos os dias, mas um resgate diário pendente | Bônus mensal permanece inelegível. |
| Resgate mensal repetido | O segundo request não concede outro Chest. |
| Configuração inválida | Aplicação não inicia ou rejeita configuração com mensagem clara. |

## 13. Sequência de implementação recomendada

A primeira etapa deve criar o módulo de domínio e as migrations das tabelas de evento e agregado diário, além de testes unitários para mês, meta, teto e idempotência. A segunda deve implementar `ActivityCalendarConfig` e `ActivityPointService`, sem alterar ainda todas as fontes.

A terceira etapa deve integrar missão, Arena, Clan Raid, World Boss, Boss e Incubação na ordem indicada na seção 7. Em seguida, devem ser adicionados o controller autenticado, DTOs, resgate transacional, auditoria e testes de integração. A quinta etapa deve adicionar a tela do calendário e o tratamento dos estados visuais. Por fim, deve ser feito o cadastro manual do Chest e da Loot Table no Admin, sem seed de loot.

A migration deverá usar o próximo número disponível no branch no momento do desenvolvimento. No estado analisado, as migrations chegam a `V148`, mas esse número deve ser confirmado novamente antes de criar qualquer arquivo para evitar colisão com trabalho paralelo. [10]

## 14. Decisões ainda abertas

A principal decisão de produto é se derrotas de Arena e Boss pontuam. A recomendação técnica deste estudo é que sim, porque o requisito menciona executar a ação; contudo, deve ser possível desligar essa contribuição por configuração sem alterar o schema.

Também é necessário confirmar se o jogador pode resgatar dias anteriores ainda não resgatados. A regra do bônus mensal deve permanecer baseada em **todos os resgates diários**, e não apenas em todas as metas atingidas. O baú mensal deve ser resgatado por um clique separado após o último resgate diário; essa abordagem oferece melhor clareza para o jogador e menor risco de falha silenciosa ao conceder dois prêmios na mesma operação. A recomendação é permitir dentro do mês corrente. Deve ser definido ainda o timezone oficial do jogo, o comportamento quando o Chest estiver temporariamente mal configurado e se haverá teto por fonte. O desenho suporta todas essas opções sem alterar o conceito central.

## Referências

[1]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/mission/application/ClaimMissionUseCase.java "ClaimMissionUseCase — conclusão e resgate de missão"

[2]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/arena/application/ChallengeArenaUseCase.java "ChallengeArenaUseCase — partida de Arena"

[3]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/clan/raid/application/AttackClanRaidUseCase.java "AttackClanRaidUseCase — ataque de Clan Raid"

[4]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/boss/world/application/AttackWorldBossUseCase.java "AttackWorldBossUseCase — ataque de World Boss"

[5]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/boss/application/ChallengeBossUseCase.java "ChallengeBossUseCase — boss normal, diário, semanal e mensal"

[6]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/incubation/application/ClaimIncubationUseCase.java "ClaimIncubationUseCase — conclusão da incubação"

[7]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/resources/application.yml "application.yml — configuração de gameplay"

[8]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/loot/api/AdminChestController.java "AdminChestController — administração de Chests"

[9]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/loot/api/AdminLootTableController.java "AdminLootTableController — administração de Loot Tables"

[10]: https://github.com/rafaelhazevedo61/digimon-revolution-online/tree/develop/backend/src/main/resources/db/migration "Migrations Flyway — estado observado na develop"
