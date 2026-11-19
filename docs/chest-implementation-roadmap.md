# Roadmap de implementação — Baús temáticos e Loot Tables

**Projeto:** Digimon Revolution Online
**Estado:** Sprint 3 em implementação — Baús da Área nas Missões
**Data:** 20 de agosto de 2026
**Branch-base de todos os PRs:** `develop`

## 1. Objetivo do roadmap

Este roadmap organiza a implementação do sistema de baús temáticos e loot tables para os quatro modos de jogo atualmente previstos: **Missões, Bosses, Arena e Boss Mundial**. Cada etapa deverá ser desenvolvida em um PR separado contra `develop`, compilada com Maven, validada com testes focados e revisada antes do merge.

A estratégia é começar pelo núcleo reutilizável do sistema e, em seguida, ativar primeiro as Missões. Bosses, Arena e Boss Mundial serão integrados somente depois que a abertura transacional e a primeira economia de Missões estiverem validadas.

> **Regra central:** o modo de jogo entrega um baú; a abertura do baú sorteia uma raridade e, depois, uma pool de itens compatível com aquela raridade.

O PostgreSQL continuará sendo a fonte oficial dos dados do jogo. Inventário, baús, abertura, itens recebidos, Bits, equipamentos e recompensas deverão permanecer em transações relacionais. O MongoDB continuará restrito à auditoria, e a auditoria positiva será enviada pelo Transactional Outbox somente depois do commit da operação oficial.

## 2. Decisões de produto já consolidadas

| Tema | Regra aprovada |
|---|---|
| Negociação | Baús negociáveis entre jogadores. |
| Abertura | Sem chave, sem custo adicional e sem expiração. |
| Pool | Oculta dentro do jogo; a Wiki poderá explicar as regras gerais. |
| Raridades | `COMMON`, `RARE`, `EPIC` e `LEGENDARY`. |
| Sorteio | Primeiro raridade; depois entrada da pool daquela raridade. |
| Quantidade | De 1 a 4 tipos diferentes de item por abertura, com mínimo e máximo configuráveis para cada entrada. |
| Loot tables | Nomeadas, reutilizáveis e editáveis pelo painel administrativo. |
| Missões | Mantêm XP e Bits; substituem o item aleatório por Baú da Área. Cada missão pode usar uma loot table própria ou compartilhada. |
| Arena | Baú somente em vitória. O tier é fixado no momento da luta. Tiers: Bronze, Prata, Ouro, Platina e Diamante. |
| Bosses | Baús separados por boss e periodicidade: normal, diário, semanal e mensal. |
| Boss Mundial | Três baús distintos: por tentativa elegível, por maior dano e por derrota do boss. |
| Idempotência | A recompensa do Boss Mundial será protegida por jogador, boss/ciclo e tipo de recompensa. |
| Auditoria | Cada abertura de baú deverá registrar a operação completa sem tokens, senhas ou segredos. |
| Ordem de implementação | Missões serão o primeiro modo de jogo integrado. |

## 3. Inventário de evolução confirmado

As migrations já confirmam 30 materiais nomeados de evolução distribuídos em seis linhas completas: Agumon, Gabumon, Patamon, Biyomon, Tentomon e Gomamon. Cada linha possui um material para `BABY_II`, `ROOKIE`, `CHAMPION`, `ULTIMATE` e `MEGA`.

| Estágio de destino | Quantidade exigida | Nível mínimo | Raridade cadastrada | Uso nas novas pools |
|---|---:|---:|---|---|
| `BABY_II` | 5 | 10 | `COMMON` | Material nomeado de progressão inicial. |
| `ROOKIE` | 10 | 15 | `COMMON` | Material nomeado compatível com áreas iniciais. |
| `CHAMPION` | 20 | 25 | `RARE` | Material de progressão intermediária. |
| `ULTIMATE` | 30 | 50 | `EPIC` | Material de progressão avançada. |
| `MEGA` | 50 | 75 | `LEGENDARY` | Material avançado e de menor disponibilidade. |

Os códigos atualmente existentes são `FRAGMENT_KOROMON`, `FRAGMENT_TSUNOMON`, `FRAGMENT_TOKOMON`, `FRAGMENT_YOKOMON`, `FRAGMENT_MOTIMON`, `FRAGMENT_BUKAMON`, `FRAGMENT_AGUMON`, `FRAGMENT_GABUMON`, `FRAGMENT_PATAMON`, `FRAGMENT_BIYOMON`, `FRAGMENT_TENTOMON`, `FRAGMENT_GOMAMON`, os seis materiais Champion, os seis Ultimate e os seis Mega correspondentes às linhas cadastradas.

Apesar do prefixo `FRAGMENT_` nos códigos, esses itens específicos estão classificados como `EVOLUTION_MATERIAL` e possuem `material_code` individual. Portanto, eles são os materiais nomeados oficiais para a futura progressão. Os tipos genéricos `FRAGMENT_ROOKIE`, `FRAGMENT_CHAMPION`, `FRAGMENT_ULTIMATE` e `FRAGMENT_MEGA` são legados conceituais e não devem ser usados como base das novas loot tables, salvo em uma tabela de compatibilidade ou migração aprovada.

## 4. Visão geral das sprints e PRs

| Sprint | PR | Escopo principal | Dependência |
|---:|---|---|---|
| 0 | Preparação já confirmada | Inventário de itens, materiais e regras de negócio. | Nenhuma. |
| 1 | Modelo de loot tables e baús | Migrations, entidades, validações e catálogo persistido. | Sprint 0. |
| 2 | Abertura transacional | Sorteio, entrega de 1–4 itens, idempotência e rollback. | Sprint 1. |
| 3 | Integração com Missões | Baús por área e loot tables por missão. | Sprints 1 e 2. |
| 4 | Administração e inventário | CRUD administrativo e abertura pelo inventário do jogador. | Sprints 1–3. |
| 5 | Testes, balanceamento e Wiki inicial | Testes automatizados, simulações e documentação de Missões. | Sprints 2–4. |
| 6 | Integração com Bosses | Baús normal, diário, semanal e mensal por boss. | Sprint 5. |
| 7 | Integração com Arena | Baús de vitória por tier. | Sprint 5. |
| 8 | Integração com Boss Mundial | Três recompensas idempotentes por boss/ciclo. | Sprint 5 e regras atuais do World Boss. |
| 9 | Balanceamento final e expansão | Ajustes econômicos, novas pools, observabilidade e documentação completa. | Sprints 6–8. |

## 5. Sprint 0 — Preparação e inventário técnico

Esta etapa não exige um novo PR funcional porque o levantamento já foi confirmado nas migrations. Ela deve ser tratada como pré-condição documentada para iniciar o código.

Foram confirmados os materiais nomeados, as seis linhas evolutivas, as quantidades exigidas por etapa, os níveis mínimos, as raridades, os ícones, os limites de pilha e as regras de negociação. Também foi confirmada a separação entre os materiais específicos classificados como `EVOLUTION_MATERIAL` e os quatro fragmentos genéricos legados.

O resultado dessa etapa é uma matriz de referência para as loot tables. A origem de obtenção ainda não está implementada; será criada nas sprints de loot tables e integração com Missões.

**Critério de conclusão:** nenhuma decisão de produto permanece bloqueada e os itens válidos para as novas pools estão identificados.

## 6. Sprint 1 — Modelo de loot tables e baús

### Objetivo

Criar o modelo persistido e reutilizável que permitirá cadastrar baús, tabelas, pesos e entradas sem espalhar regras de drop pelo código Java.

### Escopo do PR

Criar as migrations para:

- `loot_tables`: código, nome, descrição e status ativo;
- `loot_table_rarity_weights`: pesos de `COMMON`, `RARE`, `EPIC` e `LEGENDARY` por tabela;
- `loot_table_entries`: item ou material, raridade, peso, quantidade mínima, quantidade máxima e status ativo;
- `chest_definitions`: código do baú, nome, descrição, loot table vinculada, negociável e ativo;
- `chest_openings`: estrutura inicial para registrar a abertura e sua idempotência;
- `LOOT_CHEST` no `ItemType`, usando `chest_code` como identificador do baú no inventário.

As migrations deverão incluir constraints para impedir peso negativo, quantidade mínima maior que a máxima, entrada sem item válido e referências quebradas. A regra de soma dos pesos poderá ser validada no serviço, com normalização explícita quando necessário.

Os seeds iniciais deverão cadastrar as loot tables e baús de Missões, mas sem ativar ainda a entrega em produção da missão. As primeiras pools devem usar os 30 materiais nomeados e os demais itens aprovados, não os fragmentos genéricos legados.

### Critérios de aceite

O banco sobe do zero com todas as migrations. É possível cadastrar uma loot table reutilizável, associá-la a dois baús e inserir entradas de raridades diferentes. Uma tabela inválida é rejeitada antes de ser utilizada. O PR não altera ainda a recompensa efetiva das Missões.

## 7. Sprint 2 — Abertura transacional de baús

### Objetivo

Implementar o caso de uso responsável por abrir um baú com segurança, entregando de 1 a 4 tipos diferentes de item em uma única transação PostgreSQL.

### Escopo do PR

Criar o `OpenChestUseCase` e os serviços de domínio necessários para:

1. validar a posse e a quantidade de baús;
2. resolver o `chest_code` e a loot table ativa;
3. sortear a raridade conforme os pesos da tabela;
4. selecionar de 1 a 4 entradas distintas dentro da pool da raridade;
5. sortear a quantidade de cada entrada entre `qty_min` e `qty_max`;
6. remover exatamente um baú;
7. entregar todos os itens sorteados;
8. persistir a abertura com os itens, quantidades, raridade, origem e requisição idempotente;
9. publicar `CHEST_OPENED` no Transactional Outbox após o commit oficial.

O algoritmo não poderá selecionar a mesma entrada duas vezes na mesma abertura. Se houver menos de quatro entradas elegíveis, deverá selecionar no máximo a quantidade disponível, sem inventar duplicatas.

A Sprint 2 também adiciona o endpoint autenticado `POST /inventory/chests/open`, que recebe `chestCode` e `requestId`. A migration V102 persiste o intervalo de 1 a 4 tipos por loot table, e a V103 cria unicidade para itens catalogados por Digimon e definição de item, protegendo créditos concorrentes.

### Critérios de aceite

Uma abertura válida consome exatamente um baú e entrega de 1 a 4 tipos diferentes. As quantidades respeitam os ranges configurados. Repetir a mesma requisição idempotente retorna o resultado persistido sem entregar novamente os itens. Reutilizar o mesmo `requestId` para outro jogador ou baú é rejeitado. Falha na entrega provoca rollback completo. Baú negociável continua negociável porque a definição do item não o vincula automaticamente ao jogador. A auditoria positiva `CHEST_OPENED` só é criada após a persistência da operação oficial.

## 8. Sprint 3 — Integração com Missões

### Objetivo

Substituir o item aleatório atual por um Baú da Área, mantendo as recompensas fixas de missão.

### Escopo do PR

Alterar o `ClaimMissionUseCase` para manter XP, Bits, energia, cooldown e demais regras atuais, mas entregar o baú correspondente à área em vez de entregar diretamente um item aleatório.

Cada missão migrada terá um `chest_definition_id` apontando para um Baú da Área de código técnico próprio e uma loot table nomeada. Missões que precisarem compartilhar uma pool poderão apontar para a mesma definição de baú/loot table em configuração posterior. A migration V104 cria as pools a partir das chances legadas de `V56__seed_new_missions.sql`, remove fragmentos genéricos das novas entradas e adiciona materiais `EVOLUTION_MATERIAL` nomeados por Digimon conforme a área e o estágio da missão.

A pool de cada área deverá priorizar materiais nomeados compatíveis com a progressão disponível. Por exemplo, áreas iniciais podem priorizar materiais `BABY_II` e `ROOKIE`, enquanto áreas avançadas podem incluir materiais `CHAMPION`, `ULTIMATE` e `MEGA` com pesos menores e raridades correspondentes. Essa regra deve ser configurável, não codificada com `if/else` por área.

### Critérios de aceite

A conclusão de uma missão entrega XP e Bits normalmente e entrega exatamente um Baú da Área. O response informa `LOOT_CHEST`, `itemCode` e `itemName`, sem revelar a pool. Missões distintas podem compartilhar uma loot table. Missões difíceis preservam os pesos de raridade migrados. O item aleatório antigo não é entregue em duplicidade para missões vinculadas. A operação continua transacional e publica `MISSION_CLAIMED` no Transactional Outbox. Missões sem vínculo ainda usam o loot legado apenas como fallback de compatibilidade.

## 9. Sprint 4 — Painel administrativo e inventário do jogador

### Objetivo

Permitir que administradores editem loot tables e que jogadores visualizem e abram baús pela interface do jogo.

### Escopo do PR

No painel administrativo, criar telas para listar, criar, editar, ativar e desativar loot tables, pesos de raridade e entradas. O painel deverá permitir definir o item, `material_code`, raridade, peso e quantidade mínima/máxima.

A interface deverá validar pools vazias, pesos inválidos, ranges invertidos, materiais inexistentes e alterações em tabelas ativas. A pool completa não será exibida ao jogador.

No inventário do jogador, exibir o baú com nome, ícone, quantidade e ação **Abrir**. Após a abertura, mostrar a raridade obtida e todos os itens recebidos, incluindo nome, quantidade e ícone. O código interno do baú poderá permanecer oculto na interface.

### Critérios de aceite

Administrador consegue editar uma tabela sem alterar código. O jogador consegue abrir um baú pelo inventário. A tela não revela a pool antes da abertura. O resultado da abertura corresponde ao registro persistido no backend. Mensagens de sucesso e erro aparecem em Português Brasileiro.

## 10. Sprint 5 — Testes, balanceamento e Wiki inicial

### Objetivo

Validar o núcleo e a primeira integração antes de expandir para Bosses, Arena e Boss Mundial.

### Escopo do PR

Criar testes unitários e de integração focados em:

- sorteio de cada raridade;
- abertura com 1, 2, 3 e 4 entradas distintas;
- quantidades mínima e máxima;
- pool com menos de quatro entradas;
- ausência de entradas ativas;
- retry idempotente;
- rollback quando a entrega falhar;
- baú negociável;
- auditoria positiva após commit;
- falha do MongoDB sem rollback da operação oficial;
- preservação de XP, Bits e energia nas Missões.

Executar simulações locais para avaliar a velocidade de aquisição dos materiais nomeados e identificar inflação de Poções, Pedras de Treino, Núcleos de Dados, Digitamas e Incubadoras.

Atualizar a Wiki com as regras que o jogador precisa conhecer, sem expor a pool completa dentro do jogo. A documentação deverá explicar raridades, abertura, quantidade variável, negociação e relação entre áreas e dificuldade.

### Critérios de aceite

Os testes focados passam com Maven. `git diff --check` não apresenta erros. As simulações não revelam uma fonte claramente desbalanceada. A Wiki descreve o funcionamento sem mostrar detalhes técnicos do código ao jogador.

## 11. Sprint 6 — Integração com Bosses

### Objetivo

Adicionar baús para bosses normais e bosses com periodicidade diária, semanal e mensal.

### Escopo do PR

Criar ou vincular baús pelo código do boss e periodicidade. A loot table deverá ser reutilizável quando dois bosses tiverem economia semelhante, mas também deverá permitir uma tabela exclusiva para recompensas específicas.

Definir claramente se cada baú é concedido por vitória, participação elegível, primeira conclusão do ciclo ou outra regra já existente no boss. Repetições bloqueadas, cooldowns e derrotas não poderão gerar baús indevidos.

### Critérios de aceite

Cada tipo de boss entrega somente o baú correspondente à sua regra. Um boss diário não entrega o baú semanal. A recompensa não duplica em retry ou repetição bloqueada. O código do boss, periodicidade, raridade e itens entregues aparecem na auditoria.

## 12. Sprint 7 — Integração com Arena

### Objetivo

Entregar baús de Arena somente em vitórias, respeitando o tier definido no momento da luta.

### Escopo do PR

Integrar os cinco tiers oficiais: Bronze, Prata, Ouro, Platina e Diamante. O tier deverá ser capturado no resultado da luta e persistido junto à recompensa, evitando que uma alteração posterior de rating modifique retroativamente o baú devido.

A vitória continuará entregando Bits e Arena Coins conforme as regras existentes. A derrota continuará sem baú nesta primeira versão, mantendo apenas a recompensa de participação já prevista.

### Critérios de aceite

Vitória em cada tier entrega o baú correto. Derrota não entrega baú. Alterar o rating depois da luta não muda o tier já definido. Os cinco desafios diários continuam funcionando. A recompensa não duplica em repetição da requisição.

## 13. Sprint 8 — Integração com Boss Mundial

### Objetivo

Adicionar os três baús do Boss Mundial com proteção de idempotência por jogador, boss/ciclo e tipo de recompensa.

### Escopo do PR

Implementar os seguintes tipos:

| Tipo | Regra |
|---|---|
| Baú por tentativa | Concedido uma vez por jogador elegível no ciclo, conforme a definição de tentativa válida. |
| Baú por maior dano | Concedido ao jogador elegível que ficar em primeiro lugar no dano do ciclo. |
| Baú por derrota | Concedido após a derrota do boss aos jogadores elegíveis conforme a regra definida. |

A operação deverá usar a identidade composta equivalente a `player_id + boss_id/cycle_id + reward_type`. O fechamento do boss deverá ser protegido contra concorrência e reprocessamento. O cálculo atual de dano, XP e Bits não será substituído nesta sprint; o PR adicionará os baús sobre o fluxo existente.

### Critérios de aceite

Cada jogador elegível recebe no máximo um baú de tentativa por ciclo. Somente o maior dano recebe o baú de ranking. A derrota não gera o mesmo prêmio duas vezes. Reiniciar ou repetir a chamada não duplica recompensas. Ataques simultâneos não produzem duas recompensas para o mesmo tipo. Todas as recompensas possuem auditoria positiva.

## 14. Sprint 9 — Balanceamento final, expansão e documentação completa

### Objetivo

Consolidar a economia depois de observar Missões, Bosses, Arena e Boss Mundial funcionando juntos.

### Escopo do PR

Revisar pesos e quantidades com base nos resultados dos testes e do uso real. Ajustar pools por área, boss, tier e periodicidade. Adicionar novos materiais nomeados quando novas linhas evolutivas forem implementadas. Evitar adicionar os fragmentos genéricos legados às novas pools sem justificativa de compatibilidade.

Completar a Wiki, o README operacional, a coleção de requisições e a documentação administrativa. Incluir procedimentos para investigar uma abertura, uma recompensa duplicada, uma entrega ausente e uma auditoria em `DEAD_LETTER`.

### Critérios de aceite

As quatro modalidades têm regras documentadas. Os pesos e ranges estão versionados por migration ou configuração administrativa auditável. Os testes de economia e idempotência passam. A documentação permite reproduzir as principais verificações sem depender de conhecimento interno do código.

## 15. Ordem recomendada de execução

A ordem recomendada é **Sprint 1 → Sprint 2 → Sprint 3 → Sprint 4 → Sprint 5**. Esse primeiro bloco entrega o produto jogável mínimo: o jogador conclui uma missão, recebe um baú, abre o baú e recebe materiais ou itens com auditoria e rollback seguros.

Somente depois da validação desse fluxo devem ser executadas as Sprints 6, 7 e 8. A Sprint 9 deve fechar o ciclo após os quatro modos estarem funcionando, porque balancear todos os modos antes de testar o núcleo aumentaria o risco de retrabalho.

Cada PR deverá seguir o mesmo checklist operacional: branch criada a partir de `develop`, escopo isolado, migrations reversíveis ou compatíveis com a política do projeto, testes focados, `./mvnw -q -DskipTests compile`, testes relevantes, `git diff --check`, documentação atualizada, push para o GitHub e abertura de PR separado contra `develop`.

## Referências

[1]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/resources/db/migration/V40__seed_evolution_step_materials.sql "Migration V40 — materiais e quantidades de evolução"
[2]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/resources/db/migration/V42__seed_item_definitions.sql "Migration V42 — definições dos itens"
[3]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/resources/db/migration/V45__update_item_definitions_attributes.sql "Migration V45 — atributos dos itens"
[4]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/resources/db/migration/V56__seed_new_missions.sql "Migration V56 — missões e percentuais atuais"
[5]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/shared/audit/AuditOutboxProcessor.java "Processador do Transactional Outbox"
[6]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/arena/domain/ArenaRules.java "Regras atuais da Arena"
[7]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/resources/db/migration/V92__world_boss.sql "Migration do Boss Mundial"
