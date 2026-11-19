# Análise do sistema de incubação

**Repositório analisado:** `rafaelhazevedo61/digimon-revolution-online`  
**Branch analisada:** `develop`  
**Data da análise:** 25 de agosto de 2026  
**Escopo:** fluxo de início da incubação, contadores, expiração, claim, criação do Digimon, seleção do ativo, listagem da coleção, slots e cache do PWA.

## Resumo executivo

A análise encontrou **duas causas estruturais confirmadas** e uma hipótese operacional que precisa ser verificada no ambiente publicado.

O primeiro bug é favorecido pela existência de **duas implementações independentes de contador**: uma no dashboard e outra na tela dedicada de incubação. Elas mantêm intervalos separados, usam IDs diferentes e são iniciadas em ciclos de navegação distintos. Além disso, a tela inicializa o texto com `remainingSeconds`, mas recalcula o restante a cada segundo usando `finishAt`. Isso cria duas fontes de verdade no cliente. A implementação atual não contém nenhuma constante ou texto `180` no frontend; portanto, a contagem fixa de 180 segundos não é reproduzível a partir do código atual e deve ser investigada como **versão antiga em cache, artefato publicado diferente do branch analisado ou resposta de API divergente**. A causa arquitetural do comportamento sobreposto, entretanto, está confirmada.

O segundo bug não indica, no caminho normal, perda do Digimon no banco. O claim cria o Digimon com `playerId` correto e status `ACTIVE`, salva-o e devolve seu `id`. O problema é que o frontend ignora esse `id`, apenas mostra um toast e renderiza novamente a tela de incubação. Se o jogador já possui um Digimon ativo, o backend preserva `activeDigimonId`; como o dashboard exibe somente o Digimon apontado por esse campo, o recém-chocado não aparece na Home. Ele deveria aparecer na lista `/digimon/me`, mas o usuário não é levado para essa tela nem recebe uma confirmação visual da coleção atualizada.

| Problema | Diagnóstico | Confiança | Prioridade |
|---|---|---:|---:|
| Contador real substituído ou sobreposto por 180 segundos | Timers duplicados e fontes de tempo divergentes; literal `180` ausente no frontend atual | Alta para a causa estrutural; média para a origem exata do número 180 | P0 |
| Digimon chocado não aparece | Claim salva como `ACTIVE`, mas não seleciona o novo Digimon nem atualiza a lista; dashboard mostra apenas um ativo | Alta | P0 |
| Incubação pronta pode desaparecer da tela | `/incubation/me` consulta apenas `IN_PROGRESS`, enquanto o dashboard também trabalha com `READY` | Alta | P1 |
| Claim/start concorrente pode duplicar ou liberar estados inconsistentes | Busca sem lock específico e ausência de restrição de unicidade para incubação ativa | Média/alta | P1 |

## Fluxo ponta a ponta encontrado

A tela dedicada consulta simultaneamente `/incubation/me` e `/players/me/dashboard`. Quando encontra uma incubação, renderiza o card e inicia `incubStartTimer`; quando não encontra, consulta `/inventory`, agrega os itens e permite selecionar uma digitama e uma incubadora. O início envia `digitamaType` e `incubatorType` para `/incubation/start` [1] [2] [3].

| Etapa | Implementação | Comportamento observado |
|---|---|---|
| Seleção de itens | `game-frontend/assets/js/incubation.js:152-292` | Filtra o inventário por categoria ou por enum legado e envia os tipos selecionados. |
| Inventário usado | `backend/.../InventoryController.java:33-42` | Lista itens vinculados ao Digimon apontado por `player.activeDigimonId`. |
| Início | `backend/.../StartIncubationUseCase.java:28-65` | Valida tipos, consome digitama e incubadora, calcula `finishAt` e salva a incubação. |
| Consulta dedicada | `backend/.../GetIncubationUseCase.java:20-30` | Busca apenas status `IN_PROGRESS`, calcula `remainingSeconds` no servidor e devolve timestamps. |
| Card do dashboard | `game-frontend/assets/js/dashboard.js:281-300` | Renderiza uma segunda representação da mesma incubação. |
| Timer do dashboard | `game-frontend/assets/js/dashboard.js:313-341` | Recalcula a cada segundo a partir de `finishAt`. |
| Timer da tela dedicada | `game-frontend/assets/js/incubation.js:86-110` | Também recalcula a cada segundo a partir de `finishAt`, mas em outro intervalo. |
| Claim | `game-frontend/assets/js/incubation.js:112-123` | Chama `/incubation/claim`, exibe toast e re-renderiza a tela de incubação; não atualiza Digimons nem seleciona o recém-criado. |
| Criação do Digimon | `backend/.../ClaimIncubationUseCase.java:41-59` e `DigimonFactory.java:67-91` | Salva novo Digimon como `ACTIVE`, associa-o ao jogador e só altera `activeDigimonId` se ainda não houver ativo. |
| Listagem da coleção | `backend/.../GetDigimonUseCase.java:28-35` | `/digimon/me` retorna todos os Digimons do jogador com status `ACTIVE`. |
| Dashboard | `backend/.../GetPlayerDashboardUseCase.java:119-194` | Renderiza somente o Digimon apontado por `activeDigimonId`. |

## Bug 1 — contador sobreposto ou iniciado em 180 segundos

### Evidências no código

A tela dedicada possui o estado global `incubTimerInterval`, inicia o contador em `incubStartTimer` e atualiza `#incub-timer`. O dashboard possui outro estado global, `incubTimerDashInterval`, inicia `startIncubationTimer` e atualiza `#incub-dash-timer` [2] [3]. Embora os IDs sejam diferentes e o timer do dashboard normalmente se autodetenha ao deixar de encontrar seu elemento, ambos representam a mesma incubação e vivem fora de um controlador de ciclo de vida comum.

A tela dedicada exibe inicialmente `inc.remainingSeconds` e depois passa a calcular `(finishAt - Date.now()) / 1000`. O dashboard faz cálculo semelhante, mas usa `formatTime`, enquanto a tela dedicada usa `incubFormatTime`. O contrato ainda devolve `startedAt`, `finishAt` e `remainingSeconds` simultaneamente [4]. O resultado é uma arquitetura com múltiplas fontes e múltiplos escritores para uma informação que deveria ter um único dono.

A busca exata no branch analisado não encontrou `180` em `game-frontend`. A duração configurada no backend é de 5 minutos para `INCUBATOR_COMMON`, 2 minutos para `INCUBATOR_RARE` e 30 segundos para `INCUBATOR_EPIC` [5]. Assim, o número 180 não pode ser atribuído a uma constante presente no frontend atual nem às regras atuais de incubadora.

| Evidência | Interpretação |
|---|---|
| Dois intervalos: `incubTimerInterval` e `incubTimerDashInterval` | O sistema pode iniciar e encerrar timers de forma assimétrica durante navegações e re-renderizações. |
| `remainingSeconds` usado na renderização e `finishAt` usado no tick | O valor mostrado inicialmente e o valor calculado depois não possuem uma única fonte formal. |
| `LocalDateTime` sem offset no contrato | Se servidor e navegador estiverem em fusos diferentes, a conversão de `finishAt` pode divergir do cálculo feito no servidor. |
| Nenhum `180` no frontend atual | O valor 180 deve ser rastreado no Network/DOM do ambiente publicado; hipóteses principais são cache/artefato antigo ou endpoint diferente. |
| Service worker usa cache com nome `dro-game-v37` e fallback para assets | Uma versão anterior pode reaparecer quando a rede falha; a invalidação de cache deve ser tratada durante o deploy [6]. |

### Plano de correção do contador

A correção deve começar pela criação de um **único controlador de timer de incubação**. O dashboard e a tela dedicada devem apenas renderizar o valor e registrar o elemento atual; nenhum deles deve possuir um intervalo próprio. Esse controlador deve ter `start`, `tick` e `stop`, cancelar o intervalo anterior antes de iniciar outro, executar um `tick` imediato e verificar se o elemento ainda pertence à rota atual.

A API deve adotar uma fonte temporal inequívoca. A opção recomendada é trocar `LocalDateTime` por `Instant` ou `OffsetDateTime` serializado em UTC e fazer o cliente calcular o restante apenas a partir de `finishAt`. Como proteção adicional, o backend pode devolver `serverNow` ou manter `remainingSeconds` como fallback, mas o frontend não deve alternar entre as duas fontes durante o mesmo ciclo.

No deploy, deve-se incrementar a versão do cache, garantir que `index.html`, `dashboard.js`, `incubation.js` e `service-worker.js` pertencem ao mesmo release e realizar uma atualização controlada do service worker. A investigação do 180 deve capturar a resposta de `/incubation/me`, o valor de `finishAt`, o valor de `remainingSeconds`, a URL efetiva de cada script e a quantidade de elementos `#incub-timer` e `#incub-dash-timer` no DOM. Isso distinguirá rapidamente um defeito do código atual de um cliente servindo JavaScript antigo.

## Bug 2 — Digimon chocado não aparece para o jogador

### Evidências no código

O claim passa pelo caso de uso transacional, cria um Digimon com o `playerId` extraído do token, salva o registro e finaliza a incubação. A factory define `status(DigimonStatus.ACTIVE)` [7] [8]. Portanto, no caminho normal, o Digimon não é criado em storage e não é descartado.

A seleção do Digimon exibido na Home é uma regra diferente da existência na coleção. O dashboard busca apenas `player.activeDigimonId` e devolve um único `activeDigimon` [9] [10]. Quando o claim encontra um jogador que já possui parceiro ativo, `setActiveIfFirstDigimon` não substitui esse ponteiro [7]. O novo registro continua `ACTIVE`, mas não é o Digimon mostrado no card principal.

O frontend recebe o `id` do Digimon recém-criado na resposta de `/incubation/claim`, mas não usa esse valor. Em vez disso, exibe somente um toast e chama novamente `renderIncubationPage` [2]. O fluxo inicial do starter já demonstra o comportamento esperado: recebe o Digimon, chama `/digimon/select` com o identificador e navega para o dashboard [11]. A tela de seleção de Digimons também consulta `/digimon/me` e lista todos os Digimons ativos [12].

| Situação | Estado no backend | O que o jogador vê hoje | Resultado esperado |
|---|---|---|---|
| Jogador sem Digimon ativo | Novo Digimon vira `ACTIVE` e pode preencher `activeDigimonId` | Deve aparecer na Home | Manter, com atualização explícita da UI |
| Jogador já possui Digimon ativo | Novo Digimon vira `ACTIVE`, mas `activeDigimonId` continua apontando para o anterior | A Home continua mostrando o parceiro antigo | Mostrar confirmação e levar à coleção; opcionalmente oferecer “Selecionar como ativo” |
| Coleção ativa consultada | `/digimon/me` retorna os registros `ACTIVE` | A tela de incubação não consulta essa rota após claim | Atualizar a lista e destacar o novo `id` |
| Slots cheios | Claim é bloqueado pelo backend e a UI tenta antecipar com `slotInfo` | Pode haver mensagem de slot cheio | Manter validação server-side e atualizar `slotInfo` após cada operação |

### Plano de correção do pós-claim

O frontend deve guardar o objeto retornado pelo claim, validar a presença de `digimon.id`, exibir um modal ou tela curta de nascimento e oferecer duas ações: **“Selecionar como ativo”** e **“Ver minha coleção”**. A primeira chama `/digimon/select` usando o `id` retornado e então navega para `dashboard`; a segunda navega para `digimon-select`, que já lista `/digimon/me`. Em ambos os casos, a interface deve atualizar dados de slots, inventário e dashboard após a operação.

A regra de produto precisa ser decidida explicitamente: o Digimon recém-chocado deve substituir automaticamente o ativo atual ou deve entrar como novo Digimon ativo sem alterar o parceiro atual? O código existente indica que a intenção atual é a segunda opção, pois só seleciona automaticamente quando o jogador ainda não tem ativo. O plano recomendado preserva essa regra e torna a consequência visível: “Digimon adicionado à sua coleção; seu parceiro atual continua ativo”. Se a decisão for trocar automaticamente, a mudança deve ser feita no backend, não apenas na interface.

Também é recomendável ordenar `/digimon/me` por `createdAt DESC` ou adicionar um campo de criação/identificador à ordenação para que o recém-nascido apareça no topo de forma determinística. Isso não corrige a persistência, mas elimina a sensação de desaparecimento causada por uma ordem não garantida do repositório [13].

## Riscos relacionados encontrados durante a análise

A consulta dedicada `/incubation/me` procura apenas `IN_PROGRESS`, enquanto o claim procura primeiro `READY` e depois `IN_PROGRESS`. O dashboard, por sua vez, consulta todos os estados diferentes de `CLAIMED` e pode transformar uma incubação expirada de `IN_PROGRESS` em `READY` [14]. Essa assimetria permite que uma incubação pronta deixe de aparecer na tela dedicada, além de permitir que `/incubation/start` não reconheça uma incubação `READY` como bloqueio ativo [15]. A correção deve padronizar a noção de incubação ativa como `IN_PROGRESS` ou `READY` em todos os fluxos.

O banco não possui, na migração original, uma restrição de unicidade parcial para impedir mais de uma incubação não reivindicada por jogador [16]. O caso de uso de início verifica a existência antes de salvar, mas essa verificação isolada não protege contra duas requisições concorrentes. O claim também deve usar lock pessimista ou uma atualização condicional para impedir dois claims do mesmo registro. O botão desabilitado no frontend é apenas uma conveniência de UX e não substitui a proteção transacional.

A seleção de itens do frontend usa o inventário do Digimon atualmente ativo. Isso é coerente com o endpoint `/inventory`, mas significa que trocar o Digimon ativo pode mudar os itens disponíveis para incubação [2] [17]. Deve ser confirmado como regra de produto; caso o inventário de incubação deva ser do jogador, o modelo atual está acoplando indevidamente a operação ao Digimon ativo.

## Plano de execução priorizado

| Fase | Prioridade | Entregas | Critério de conclusão |
|---|---:|---|---|
| 1. Instrumentação e reprodução | P0 | Capturar resposta de `/incubation/me`, DOM, scripts efetivos, versão do service worker e sequência de navegação. | Identificação comprovada da origem do valor 180 no ambiente publicado. |
| 2. Unificação do contador | P0 | Criar controlador único; remover duplicidade de intervalos; tick imediato; cancelamento ao trocar de rota; estado “pronta” sem reconsulta desnecessária. | Um único elemento é atualizado por ciclo e o contador coincide com o restante server-side. |
| 3. Contrato temporal | P0/P1 | Serializar `finishAt` com offset/UTC e padronizar o cálculo; manter tolerância a relógio do cliente e aba suspensa. | Servidor e navegador exibem o mesmo segundo restante em fusos diferentes e após retomar a aba. |
| 4. Pós-claim visível | P0 | Usar `digimon.id`; mostrar resultado; selecionar ou encaminhar para coleção; atualizar dashboard e slots. | Após claim, o jogador consegue encontrar o Digimon sem recarregar manualmente e a coleção mostra o novo registro. |
| 5. Estados READY/IN_PROGRESS | P1 | Unificar queries de incubação ativa, impedir novo start com incubação pronta e fazer `/incubation/me` devolver READY. | Ovo pronto nunca desaparece entre dashboard e tela dedicada. |
| 6. Concorrência e idempotência | P1 | Lock/atualização condicional no claim e proteção de uma incubação ativa por jogador. | Cliques/requisições concorrentes não criam dois Digimons nem duas incubações. |
| 7. Cache e release | P1 | Incrementar cache, publicar assets coerentes e validar atualização do service worker. | Cliente novo e cliente já instalado recebem o mesmo código corrigido. |
| 8. Testes de regressão | P0/P1 | Testes de timer, contrato temporal, claim, seleção, slots, estados e navegação. | Todos os cenários da matriz abaixo passam em ambiente limpo e em PWA já instalado. |

## Matriz mínima de testes de aceite

| Cenário | Verificação |
|---|---|
| Incubadora comum | Inicia com aproximadamente 300 segundos, sem segundo contador e sem saltar para 180. |
| Incubadora rara | Inicia com aproximadamente 120 segundos e mantém contagem regressiva monotônica. |
| Incubadora épica | Inicia com aproximadamente 30 segundos, inclusive após recarregar a página. |
| Navegação dashboard → incubação → dashboard | Nunca permanecem dois timers ativos; somente o elemento da rota atual é atualizado. |
| Aba suspensa ou recarga após expiração | Incubação aparece como pronta e o claim continua disponível. |
| Fuso do servidor diferente do navegador | `finishAt` e restante exibido permanecem corretos. |
| Claim com Digimon já ativo | Novo Digimon é salvo, aparece em `/digimon/me` e o usuário recebe ação clara para selecioná-lo. |
| Primeiro Digimon do jogador | Claim define o ativo quando não existe ativo e o dashboard mostra o novo parceiro. |
| Slots ativos cheios | Claim falha com mensagem clara, não consome parcialmente o estado e não cria Digimon. |
| Duplo clique ou duas requisições de claim | Apenas um Digimon é criado e a segunda tentativa recebe estado já reivindicado. |
| Incubação READY | Não é possível iniciar outra incubação enquanto a anterior não for reivindicada. |
| PWA instalado em versão antiga | O service worker atualiza os assets e não serve a implementação antiga do contador. |

## Validação realizada e limitação do ambiente

O branch analisado estava limpo e a busca exata não encontrou `180` no frontend atual. Os testes relacionados foram localizados, mas a execução ficou bloqueada pela infraestrutura do sandbox: `mvn` não está instalado, o Maven Wrapper encontrou `release version 17 not supported` e o ambiente não possui `javac` disponível, embora possua o runtime Java 21. Isso não foi tratado como falha dos testes do projeto; a execução deve ser repetida em um ambiente com JDK completo compatível.

A recomendação imediata é implementar primeiro as fases 1, 2 e 4. Elas atacam diretamente os dois sintomas relatados. Em seguida, as fases 3, 5 e 6 eliminam as inconsistências de contrato e concorrência que podem fazer o problema reaparecer sob recarga, expiração, troca de rota ou requisições simultâneas.

## Referências

[1]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/game-frontend/assets/js/incubation.js#L152-L292 "Tela de seleção e início da incubação"
[2]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/game-frontend/assets/js/incubation.js#L3-L147 "Renderização, timer e claim da tela dedicada"
[3]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/game-frontend/assets/js/dashboard.js#L281-L341 "Card e timer de incubação no dashboard"
[4]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/incubation/api/dto/response/IncubationResponse.java#L11-L18 "Contrato de resposta da incubação"
[5]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/incubation/domain/IncubatorRules.java#L12-L19 "Durações das incubadoras"
[6]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/game-frontend/service-worker.js#L1-L53 "Cache e atualização do service worker"
[7]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/incubation/application/ClaimIncubationUseCase.java#L41-L113 "Claim, criação, slots e finalização"
[8]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/digimon/domain/DigimonFactory.java#L67-L91 "Criação do Digimon como ACTIVE"
[9]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/player/application/GetPlayerDashboardUseCase.java#L119-L194 "Construção do único activeDigimon do dashboard"
[10]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/player/api/dto/response/PlayerDashboardResponse.java#L15-L35 "Contrato do dashboard com activeDigimon singular"
[11]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/game-frontend/assets/js/starter.js#L177-L218 "Pós-hatch do starter com seleção do Digimon"
[12]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/game-frontend/assets/js/starter.js#L220-L306 "Listagem da coleção ativa"
[13]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/digimon/infra/DigimonRepository.java#L25-L29 "Consultas de Digimon sem ordenação explícita"
[14]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/player/application/GetPlayerDashboardUseCase.java#L260-L300 "Estados e expiração no dashboard"
[15]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/incubation/application/StartIncubationUseCase.java#L28-L65 "Validação de incubação ativa no start"
[16]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/resources/db/migration/V5__create_incubation_table.sql#L2-L9 "Tabela de incubação sem unicidade por jogador"
[17]: https://github.com/rafaelhazevedo61/digimon-revolution-online/blob/develop/backend/src/main/java/com/dro/modules/inventory/api/InventoryController.java#L33-L42 "Inventário vinculado ao Digimon ativo"
