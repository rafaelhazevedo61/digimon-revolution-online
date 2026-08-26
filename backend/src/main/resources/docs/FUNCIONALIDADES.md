# Digimon Revolution Online - Documento Funcional

> Atualizado com base na implementação atual da branch de referência em 23/08/2026.
>
> Este documento descreve **funcionalidades implementadas**. Valores de balanceamento que vivem em banco/configuração devem ser consultados na fonte de dados correspondente, evitando duplicação desnecessária e divergências futuras.

---

## Índice

1. [Princípios deste documento](#1-princípios-deste-documento)
2. [Autenticação e sessões](#2-autenticação-e-sessões)
3. [Jogador (Player)](#3-jogador-player)
4. [Digimon](#4-digimon)
5. [Digitama e incubação](#5-digitama-e-incubação)
6. [Evolução](#6-evolução)
7. [Rebirth](#7-rebirth)
8. [Missões e áreas](#8-missões-e-áreas)
9. [Loja](#9-loja)
10. [Inventário](#10-inventário)
11. [Equipamentos](#11-equipamentos)
12. [Bosses](#12-bosses)
13. [Boss Mundial](#13-boss-mundial)
14. [Ranking](#14-ranking)
15. [Slots e Storage](#15-slots-e-storage)
16. [Arena PvP](#16-arena-pvp)
17. [Clãs](#17-clãs)
18. [Missões de Clã](#18-missões-de-clã)
19. [Raid de Clã](#19-raid-de-clã)
20. [Casa de Leilões](#20-casa-de-leilões)
21. [Correio](#21-correio)
22. [Eventos e premiações](#22-eventos-e-premiações)
23. [Loot Tables e Baús](#23-loot-tables-e-baús)
24. [Tutorial](#24-tutorial)
25. [Painel Administrativo](#25-painel-administrativo)
26. [Frontends](#26-frontends)
27. [Status de funcionalidades futuras](#27-status-de-funcionalidades-futuras)
28. [Migrations](#28-migrations)

---

## 1. Princípios deste documento

Este arquivo é uma referência funcional do comportamento atualmente implementado.

### Fonte de verdade

A prioridade para validação de uma regra é:

1. código de domínio/use case;
2. configuração persistida em banco e migrations;
3. testes automatizados;
4. este documento.

Quando existir divergência, o comportamento implementado deve ser verificado antes de alterar código com base nesta documentação.

### Valores de balanceamento

Percentuais de drop, custos, cooldowns, requisitos e recompensas que são administráveis ou persistidos em catálogo **não são repetidos aqui**, salvo quando o valor faz parte de uma regra estrutural estável.

Exemplos de dados que devem ser consultados diretamente no catálogo/banco:

- recompensas de missões;
- custos e produtos da loja;
- drops e recompensas de bosses;
- requisitos e materiais de linhas evolutivas;
- pesos de Loot Tables;
- raridade de equipamentos por perfil;
- cooldowns configuráveis;
- recompensas e conteúdo de baús.

### Funcionalidades planejadas

Regras apenas planejadas não devem ser descritas como comportamento atual. Quando necessário, devem aparecer na seção [Status de funcionalidades futuras](#27-status-de-funcionalidades-futuras), claramente identificadas como não implementadas.

---

## 2. Autenticação e sessões

A autenticação pública utiliza JWT.

### Fluxos implementados

- cadastro de jogador;
- login com username/e-mail e senha conforme implementação do módulo;
- armazenamento de senha com hash;
- autenticação de rotas protegidas via header `Authorization`;
- alteração de senha pelo próprio jogador;
- invalidação global de sessões através de `tokenVersion`;
- endpoints administrativos separados do namespace público.

### Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/register` | Registrar novo jogador |
| POST | `/auth/login` | Autenticar e emitir JWT |
| POST | `/players/me/change-password` | Alterar a própria senha |
| POST | `/players/me/logout-all` | Invalidar os tokens previamente emitidos para a conta |

O logout somente do dispositivo atual é realizado no frontend removendo o token local.

---

## 3. Jogador (Player)

O jogador representa a conta e mantém referências para o estado principal da jornada, incluindo Digimon ativo, starter, capacidade de slots e controle de sessão.

### Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| GET | `/players/me` | Dados da conta autenticada |
| GET | `/players/me/dashboard` | Resumo da jornada e do Digimon ativo |
| GET | `/players/me/startup` | Determina o fluxo inicial do jogador |
| POST | `/players/me/change-password` | Alteração de senha |
| POST | `/players/me/logout-all` | Revogação global de sessões |

### Startup

O endpoint de startup orienta o frontend entre os estados principais da jornada inicial, como escolha de starter, hatch e entrada na experiência normal do jogo.

---

## 4. Digimon

Cada Digimon pertence a um jogador e possui espécie, estágio, nível, experiência, IVs, raridade, personalidade, trait, energia, Bits, dados de rebirth e estado de armazenamento.

### Estágios implementados

```text
BABY -> BABY_II -> ROOKIE -> CHAMPION -> ULTIMATE -> MEGA
```

Todos esses estágios fazem parte da implementação atual.

### Progressão

- nível máximo atual: 100;
- stats são calculados a partir dos dados da espécie e modificadores do Digimon;
- IVs afetam os atributos individuais;
- raridade, personalidade, trait, estágio, rebirth e equipamentos podem influenciar os resultados finais;
- fórmulas e multiplicadores de balanceamento devem ser consultados nas classes de domínio/configurações correspondentes, evitando manter cópias numéricas neste documento.

### Estados principais

- `ACTIVE`: Digimon disponível na coleção ativa;
- `STORED`: Digimon guardado no Storage;
- `REBORN`: versão anterior preservada após Rebirth.

### Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| GET | `/digimon/me` | Listar Digimons ativos do jogador |
| GET | `/digimon/{digimonId}` | Consultar um Digimon |
| POST | `/digimon/select` | Selecionar o Digimon ativo |
| GET | `/digimon/{digimonId}/evolution-options` | Consultar opções de evolução |
| POST | `/digimon/evolve` | Evoluir Digimon |
| POST | `/digimon/rebirth` | Executar Rebirth |
| GET | `/digimon/{digimonId}/rebirth-preview` | Visualizar condições do Rebirth |
| GET | `/digimon/{digimonId}/lineage` | Consultar linhagem de Rebirth |
| PUT | `/digimon/rename` | Renomear Digimon |
| POST | `/digimon/{digimonId}/store` | Enviar ao Storage |
| POST | `/digimon/{digimonId}/retrieve` | Retirar do Storage |
| GET | `/digimon/storage` | Listar Digimons armazenados |
| GET | `/digimon/level-table` | Consultar tabela de progressão de nível |
| GET | `/digimon-infos` | Catálogo de espécies |

---

## 5. Digitama e incubação

O jogo possui dois fluxos relacionados a Digitamas:

1. fluxo inicial de starter;
2. incubação de Digitamas obtidas durante o jogo.

### Starter

O jogador escolhe uma opção disponível e realiza o hatch. A espécie nasce a partir da pool configurada e recebe os atributos aleatórios definidos pela lógica de criação de Digimon.

### Incubação

A incubadora possui três slots fixos, exibidos sempre para o jogador. Cada jogador começa com o slot 1 desbloqueado; os slots 2 e 3 permanecem bloqueados até serem liberados. Cada slot desbloqueado pode manter uma incubação independente, permitindo ovos em paralelo.

A capacidade de incubação é persistida em `players.unlocked_incubation_slots` e pertence ao jogador, não ao Digimon ativo. O item consumível **Expansor de Slot de Incubação** desbloqueia exatamente um slot por uso, consome uma unidade do inventário e não permite ultrapassar o limite total de três slots. O inventário atual é entregue ao Digimon ativo, mas o efeito de capacidade é aplicado exclusivamente ao jogador.

O fluxo utiliza Digitama e incubadora do inventário, associa a nova incubação ao slot escolhido e permite resgatar o Digimon daquele slot quando estiver pronta. Os estados `IN_PROGRESS` e `READY` mantêm o slot ocupado; o claim altera somente a incubação selecionada para `CLAIMED` e libera sua posição.

As pools e tempos de incubação são dados de catálogo/configuração e não devem ser duplicados neste documento.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/digitama/select` | Selecionar starter |
| POST | `/digitama/hatch` | Realizar hatch do starter |
| GET | `/digitama/history` | Histórico de hatch |
| GET | `/digitama-pools/available` | Pools disponíveis |
| POST | `/incubation/start` | Iniciar incubação em um slot desbloqueado e vazio |
| POST | `/incubation/{incubationId}/claim` | Resgatar a incubação concluída indicada |
| GET | `/incubation/me` | Consultar os três slots da incubadora |

---

## 6. Evolução

A evolução é orientada pelas tabelas de linhas evolutivas e seus steps.

### Regras gerais

- cada linha define uma sequência ordenada de espécies/estágios;
- cada step pode possuir requisito de nível e materiais específicos;
- os materiais são definidos em catálogo, não por um fragmento genérico fixo no código;
- quando existem múltiplas linhas válidas, o jogador escolhe a opção desejada;
- a evolução atual suporta progressão até `MEGA`;
- ao evoluir, a espécie e o estágio são atualizados e os stats são recalculados.

### Fonte dos requisitos

Requisitos e materiais devem ser consultados nas tabelas de evolução (`evolution_lines`, steps e materiais associados) e nas migrations/seeds correspondentes.

### Endpoints relacionados

| Método | Rota | Descrição |
|---|---|---|
| GET | `/digimon/{digimonId}/evolution-options` | Opções válidas para o Digimon |
| POST | `/digimon/evolve` | Executar evolução |
| GET | `/evolution-lines` | Consultar linhas evolutivas |
| GET | `/evolution-lines/available` | Consultar linhas disponíveis |

---

## 7. Rebirth

O Rebirth permite reiniciar a progressão de um Digimon elegível, preservando a linhagem e aplicando os benefícios definidos pela mecânica atual.

### Regras gerais

- exige condições de estágio, nível e recursos verificadas pelo use case;
- o Digimon anterior é preservado como `REBORN`;
- um novo Digimon é criado no início da progressão;
- a linhagem entre versões é preservada;
- existem regras de melhoria/herança de IV, raridade e outros atributos;
- custos e percentuais devem ser consultados na implementação atual para evitar documentação de balanceamento obsoleta.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/digimon/{digimonId}/rebirth-preview` | Prévia de requisitos/resultado |
| POST | `/digimon/rebirth` | Executar Rebirth |
| GET | `/digimon/{digimonId}/lineage` | Consultar linhagem |

---

## 8. Missões e áreas

As missões são definidas por catálogo no banco e executadas através de instâncias pertencentes ao jogador/Digimon.

### Estrutura

Uma definição de missão pode determinar, entre outros dados:

- área;
- requisito de estágio/nível;
- duração;
- custo de energia;
- experiência/recompensas;
- baú de recompensa associado.

### Áreas

O conteúdo atual possui progressão por áreas até estágios avançados, incluindo conteúdo para `ULTIMATE` e `MEGA`.

A relação exata entre área, estágio e missões deve ser consultada no catálogo persistido.

### Fluxo

1. jogador consulta missões disponíveis;
2. inicia uma missão válida;
3. o sistema registra a instância e seu tempo;
4. após conclusão, o jogador realiza o claim;
5. as recompensas configuradas são processadas.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/missions` | Consultar missões disponíveis |
| GET | `/missions/active` | Missões atuais |
| POST | `/missions/start` | Iniciar missão |
| POST | `/missions/{missionInstanceId}/claim` | Resgatar missão |
| GET | `/areas` | Consultar áreas |

---

## 9. Loja

A loja utiliza um catálogo persistido de produtos ativos.

### Funcionalidades

- consulta de produtos;
- compra utilizando a moeda prevista pela implementação;
- venda de itens/equipamentos elegíveis;
- produtos de item e equipamento;
- preços e disponibilidade administráveis via painel.

Preços, itens e regras específicas de catálogo devem ser consultados em `shop_products` e na implementação dos use cases.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/shop` | Consultar catálogo da loja |
| POST | `/shop/buy` | Comprar produto |
| POST | `/shop/sell` | Vender item/equipamento |

---

## 10. Inventário

O inventário utilizado pela jornada está associado ao Digimon ativo e integra itens de catálogo, materiais, Digitamas, incubadoras e baús.

### Características

- itens catalogados são enriquecidos por `item_definitions`;
- itens podem possuir categorias diferentes;
- alguns itens são consumíveis diretamente;
- baús são abertos através de operação transacional específica;
- concessões administrativas ficam sob `/admin`, não no namespace público do jogador.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/inventory` | Inventário do Digimon ativo |
| POST | `/inventory/use` | Utilizar item suportado, incluindo `INCUBATION_SLOT_UNLOCK` para aumentar em um o limite de slots de incubação do jogador |
| POST | `/inventory/chests/open` | Abrir um baú do inventário |
| GET | `/items` | Catálogo público de definições de item |

---

## 11. Equipamentos

O sistema possui equipamentos por template e instâncias pertencentes a Digimons.

### Estrutura

- slots de equipamento incluem arma, armadura e acessório;
- templates definem os dados base do equipamento;
- instâncias possuem raridade e refinamento próprios;
- existem sets com bônus de conjunto;
- equipamentos participam do cálculo efetivo de stats/combate;
- regras de refinamento e perfis de raridade são configuráveis e não são duplicados numericamente aqui.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/equipment/digimon/{digimonId}/inventory` | Inventário de equipamentos |
| GET | `/equipment/digimon/{digimonId}` | Equipamentos equipados |
| POST | `/equipment/equip` | Equipar |
| POST | `/equipment/unequip` | Desequipar |
| POST | `/equipment/unequip-all` | Desequipar todos |
| POST | `/equipment/refine` | Refinar equipamento |
| GET | `/equipment/{equipmentId}/refine-preview` | Prévia de refinamento |

---

## 12. Bosses

Bosses convencionais são definidos em catálogo e possuem requisitos, stats, custo, cooldown e recompensa configuráveis.

### Tipos atuais

O catálogo suporta categorias como bosses normais e rotações periódicas. A disponibilidade final é calculada a partir da definição persistida e das regras do módulo.

### Combate

O desafio compara o poder efetivo do Digimon com o boss e aplica as regras de combate implementadas. Equipamentos e bônus relevantes entram no cálculo.

A fórmula numérica, threshold, cooldown e recompensas devem ser consultados no código/configuração atual, pois fazem parte do balanceamento.

### Recompensas

Bosses podem conceder experiência, Bits e/ou baús/recompensas associados ao catálogo configurado.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/bosses/available` | Bosses disponíveis |
| POST | `/bosses/{bossCode}/challenge` | Desafiar boss |
| GET | `/bosses/history` | Histórico de tentativas |
| GET | `/bosses/cooldowns` | Cooldowns atuais |

---

## 13. Boss Mundial

O Boss Mundial é uma atividade separada dos bosses convencionais.

### Funcionamento

- existe um ciclo ativo de Boss Mundial;
- o jogador consulta o estado do ciclo e sua participação;
- ataques respeitam as regras e cooldown definidos pelo módulo;
- o dano contribui para o progresso compartilhado do ciclo;
- o sistema possui recompensas vinculadas ao conteúdo atual, incluindo integração com baús;
- não existe limite diário de ataques; cada ataque respeita cooldown de cinco minutos somente quando `dro.gameplay.boss-cooldown-enabled` e `dro.gameplay.world-boss.cooldown-enabled` estão habilitados;
- no perfil alpha, a flag `dro.gameplay.auto-boss-respawn-after-defeat-enabled` abre automaticamente um novo ciclo após a derrota, preservando o histórico do ciclo anterior;
- ferramentas administrativas permitem controlar/resetar ciclos para operação e contingência.

### Endpoints do jogador

| Método | Rota | Descrição |
|---|---|---|
| GET | `/world-boss/me` | Estado do Boss Mundial para o jogador |
| POST | `/world-boss/attack` | Realizar ataque |

---

## 14. Ranking

O jogo possui rankings gerais de Digimons por critérios de progressão.

### Categorias atuais

- nível;
- grade;
- Rebirth.

O módulo possui paginação e considera apenas os Digimons elegíveis segundo o estado atual da implementação.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/ranking/level` | Ranking por nível |
| GET | `/ranking/grade` | Ranking por grade |
| GET | `/ranking/rebirth` | Ranking por Rebirth |

Arena e Clãs possuem rankings próprios em seus respectivos módulos.

---

## 15. Slots e Storage

O jogador possui capacidade para Digimons ativos e Digimons armazenados.

### Regras gerais

- Digimons `ACTIVE` ocupam slots ativos;
- Digimons `STORED` ocupam o Storage;
- o Digimon atualmente selecionado não pode ser guardado quando a regra de domínio impedir a operação;
- operações de incubação e recuperação respeitam disponibilidade de slots;
- equipamentos são tratados de forma segura ao mover um Digimon para o Storage.

Os limites padrão são atributos/configurações do jogador e devem ser consultados na entidade/configuração atual se forem necessários para regra de negócio.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/digimon/{digimonId}/store` | Guardar Digimon |
| POST | `/digimon/{digimonId}/retrieve` | Recuperar Digimon |
| GET | `/digimon/storage` | Consultar Storage |

---

## 16. Arena PvP

A Arena é o sistema competitivo PvP atual.

### Funcionalidades implementadas

- lobby com adversários elegíveis;
- desafio entre Digimons;
- registro de resultado;
- pontuação/ranking competitivo;
- histórico de partidas;
- loja própria da Arena;
- recompensas integradas ao sistema atual de loot/baús.

O balanceamento de ataques diários, pontuação, moeda e recompensas deve ser consultado no código/catálogos correspondentes.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/arena/lobby` | Consultar lobby |
| POST | `/arena/challenge` | Desafiar adversário |
| GET | `/arena/ranking` | Ranking da Arena |
| GET | `/arena/history` | Histórico do jogador |
| GET | `/arena/shop` | Loja da Arena |
| POST | `/arena/shop/buy` | Comprar produto da Arena |

---

## 17. Clãs

O sistema de Clãs permite organização social entre jogadores.

### Funcionalidades implementadas

- criação e listagem de Clãs;
- consulta do próprio Clã e de Clãs específicos;
- atualização de informações permitidas;
- convites;
- entrada e saída;
- expulsão de membro;
- alteração de funções;
- transferência de liderança;
- dissolução com soft delete;
- upgrades de Clã;
- Honor Marks;
- ranking de Clãs.

Permissões dependem do papel do membro e das regras de cada use case.

### Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/clans` | Criar Clã |
| GET | `/clans` | Listar/pesquisar Clãs |
| GET | `/clans/me` | Consultar o próprio Clã |
| GET | `/clans/{id}` | Consultar Clã |
| PATCH | `/clans/{id}` | Atualizar dados permitidos |
| DELETE | `/clans/{id}` | Dissolver Clã |
| POST | `/clans/{id}/invite` | Convidar jogador |
| POST | `/clans/{id}/join` | Entrar no Clã |
| POST | `/clans/{id}/leave` | Sair do Clã |
| POST | `/clans/{id}/members/{username}/kick` | Expulsar membro |
| POST | `/clans/{id}/members/{username}/role` | Alterar função |
| POST | `/clans/{id}/members/{username}/transfer` | Transferir liderança |
| GET | `/clans/{id}/upgrades` | Consultar upgrades |
| POST | `/clans/{id}/upgrades/{code}/buy` | Comprar upgrade |
| GET | `/clans/ranking` | Ranking de Clãs |

---

## 18. Missões de Clã

Clãs possuem missões próprias, separadas das missões individuais do mapa.

### Fluxo

- jogador pertencente a Clã consulta as opções disponíveis;
- aceita uma missão válida;
- progresso é atualizado pelas ações previstas pela definição da missão;
- missão concluída pode ser resgatada;
- o módulo possui ranking relacionado a Honor Marks.

A disponibilidade diária, requisitos, recompensas e critérios de progresso devem ser tratados como regras do catálogo/use cases atuais, evitando duplicação de valores aqui.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/clan-missions` | Missões de Clã disponíveis |
| GET | `/clan-missions/me` | Missão atual do jogador |
| POST | `/clan-missions/{id}/accept` | Aceitar missão |
| POST | `/clan-missions/{id}/claim` | Resgatar missão concluída |
| GET | `/clan-missions/ranking` | Ranking de Honor Marks |

---

## 19. Raid de Clã

Raid de Clã é uma atividade coletiva do Clã contra um alvo compartilhado.

### Funcionamento

- o jogador consulta a Raid associada ao seu Clã;
- ataques contribuem para o progresso coletivo;
- não existe limite diário de ataques; cada jogador respeita cooldown de cinco minutos entre ataques somente quando `dro.gameplay.boss-cooldown-enabled` e `dro.gameplay.clan-raid.cooldown-enabled` estão habilitados;
- no perfil alpha, a flag `dro.gameplay.auto-boss-respawn-after-defeat-enabled` abre automaticamente uma nova Raid após a derrota, preservando o histórico da instância anterior;
- o sistema aplica as regras de disponibilidade e participação configuradas;
- ferramentas administrativas existem para contingência/reset operacional.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/clan-raids/me` | Consultar Raid do próprio Clã |
| POST | `/clan-raids/attack` | Atacar na Raid |

---

## 20. Casa de Leilões

A Casa de Leilões permite negociação assíncrona entre jogadores por listings.

### Funcionalidades implementadas

- listar anúncios ativos;
- pesquisa e filtros;
- consultar os próprios anúncios;
- publicar item elegível;
- comprar anúncio;
- cancelar anúncio próprio dentro das regras permitidas;
- histórico de transações;
- expiração automática de anúncios;
- integração com Correio para notificações relacionadas ao fluxo de leilão.

Taxas, duração, categorias elegíveis e demais parâmetros de mercado devem ser consultados no domínio/configurações atuais.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/auction/listings` | Anúncios disponíveis |
| GET | `/auction/my-listings` | Anúncios do jogador |
| GET | `/auction/history` | Histórico de transações |
| POST | `/auction/listings` | Criar anúncio |
| POST | `/auction/listings/{listingId}/buy` | Comprar anúncio |
| POST | `/auction/listings/{listingId}/cancel` | Cancelar anúncio |

---

## 21. Correio

O Correio é um sistema de mensagens entre jogadores e também serve como canal para comunicações/ações sistêmicas.

### Funcionalidades

- caixa de entrada;
- mensagens enviadas;
- contagem de não lidas;
- leitura individual;
- envio entre jogadores com validações;
- marcação como lida;
- exclusão da cópia do usuário;
- mensagens com ações, como convites e resgate de recompensas;
- anúncios administrativos;
- notificações relacionadas a outros sistemas, como Clãs e Casa de Leilões.

### Endpoints do jogador

| Método | Rota | Descrição |
|---|---|---|
| GET | `/mail/inbox` | Caixa de entrada |
| GET | `/mail/sent` | Mensagens enviadas |
| GET | `/mail/unread-count` | Quantidade de não lidas |
| GET | `/mail/{messageId}` | Consultar mensagem |
| POST | `/mail` | Enviar mensagem |
| POST | `/mail/{messageId}/read` | Marcar como lida |
| POST | `/mail/{messageId}/action` | Executar ação da mensagem |
| DELETE | `/mail/{messageId}` | Excluir a cópia da mensagem |

---

## 22. Eventos e premiações

O módulo `event` atual implementa **premiações administrativas de evento**, e não um calendário público completo de eventos automatizados.

### Funcionamento atual

- administrador cria uma premiação contendo Bits e/ou item;
- a premiação pode conter até 10 itens distintos, cada um com código de definição e quantidade própria;
- os itens podem ser selecionados pelo catálogo paginado de `item_definitions`; `itemType` e `itemQuantity` continuam aceitos para premiações legadas de item único;
- destinatários podem ser um jogador, uma lista manual de até 100 jogadores, todos os membros de um Clã ou todos os jogadores do servidor;
- o modo global considera as contas do tipo `PLAYER` existentes no momento do envio;
- cada prêmio é persistido individualmente por jogador;
- uma mensagem de Correio é criada para permitir o resgate;
- premiações possuem estado e validade;
- o jogador resgata a premiação através da ação da mensagem no Correio.

### Endpoint administrativo principal

| Método | Rota | Descrição |
|---|---|---|
| POST | `/admin/mail/event-rewards` | Criar/distribuir premiação de evento (`PLAYER`, `CLAN`, `PLAYERS` ou `ALL_PLAYERS`), com Bits e até 10 itens |
| GET | `/admin/mail/recipients/players/count` | Consultar a quantidade de jogadores elegíveis para o modo global |

A expansão para calendário, programação automática ou catálogo público de eventos deve ser tratada como evolução futura, não como funcionalidade já entregue.

---

## 23. Loot Tables e Baús

O módulo de Loot centraliza tabelas reutilizáveis para geração de recompensas e baús.

### Loot Tables

Uma Loot Table persistida possui, conceitualmente:

- código e nome;
- estado ativo/inativo;
- faixa de quantidade de itens por abertura;
- pesos por raridade;
- entradas elegíveis e seus pesos/regras.

O algoritmo valida a configuração antes do roll e seleciona recompensas de acordo com raridades e entradas elegíveis.

### Baús

Baús são definições de catálogo vinculadas a uma Loot Table. Eles podem ser concedidos por diferentes sistemas, incluindo conteúdo de área/missão, bosses, Arena e Boss Mundial conforme configuração.

A abertura:

1. valida jogador, inventário e definição do baú;
2. processa o roll da Loot Table;
3. concede as recompensas;
4. registra a abertura e seus itens;
5. usa mecanismo transacional/idempotente previsto pelo fluxo para evitar concessões duplicadas em retries válidos.

### Endpoint do jogador

| Método | Rota | Descrição |
|---|---|---|
| POST | `/inventory/chests/open` | Abrir baú possuído pelo jogador |

### Administração

| Rota | Uso |
|---|---|
| `/admin/loot-tables` | CRUD/configuração de Loot Tables |
| `/admin/chests` | Configuração de definições de baús |

Pesos, raridades e conteúdo dos baús são balanceamento persistido e não devem ser copiados numericamente para este documento.

---

## 24. Tutorial

O tutorial acompanha a introdução do jogador aos principais sistemas da jornada.

### Funcionalidades implementadas

- consulta do progresso;
- etapas identificáveis pelo sistema;
- resgate individual de recompensas de etapas elegíveis;
- conclusão explícita do tutorial;
- proteção para impedir resgates inválidos/repetidos conforme as regras persistidas.

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/tutorial` | Consultar progresso |
| POST | `/tutorial/steps/{step}/claim` | Resgatar recompensa de etapa |
| POST | `/tutorial/finish` | Finalizar tutorial |

---

## 25. Painel Administrativo

O painel administrativo concentra operações de manutenção de catálogo, moderação e ferramentas internas. Endpoints administrativos devem permanecer no namespace `/admin`.

### Áreas atuais

- jogadores;
- Digimon Infos;
- linhas evolutivas;
- itens;
- templates de equipamento;
- produtos da loja;
- missões;
- bosses e perfis de raridade;
- Loot Tables;
- baús;
- configuração de baús de áreas/missões;
- comunicados de Correio;
- premiações de evento;
- ferramentas operacionais;
- simuladores internos;
- configurações administrativas de servidor disponíveis no projeto.

### Ferramentas internas

Ferramentas de grant, debug, reset e simulação são administrativas e não devem permanecer expostas como endpoints públicos do jogador.

Exemplos de namespaces atuais:

```text
/admin/players
/admin/digimon
/admin/inventory
/admin/items
/admin/equipment-templates
/admin/shop-products
/admin/missions
/admin/bosses
/admin/loot-tables
/admin/chests
/admin/mail
/admin/tools
/admin/server
```

---

## 26. Frontends

### Game Frontend

A aplicação do jogador é uma SPA/PWA em HTML, Tailwind e JavaScript.

Telas/módulos atualmente presentes incluem:

- autenticação;
- starter;
- dashboard;
- missões;
- loja;
- inventário;
- evolução;
- Rebirth;
- ranking;
- incubação;
- Pokédex interna;
- bosses;
- Boss Mundial;
- Storage;
- Arena;
- Clãs;
- Casa de Leilões;
- Correio;
- tutorial;
- configurações e menu Mais.

### Admin Frontend

O painel administrativo possui telas para os principais catálogos e operações internas, incluindo:

- dashboard;
- jogadores;
- Digimon Infos;
- linhas evolutivas;
- itens;
- templates de equipamentos;
- produtos da loja;
- missões;
- bosses;
- Loot Tables;
- baús de área;
- anúncios de Correio;
- premiações de evento;
- simuladores;
- ferramentas administrativas.

---

## 27. Status de funcionalidades futuras

Este documento não define roadmap.

Funcionalidades não implementadas não devem receber regras funcionais aqui. Exemplos de evoluções discutidas publicamente, mas que precisam permanecer separadas do estado atual, incluem sistemas como troca direta entre jogadores e expansões futuras de conteúdo/social/economia.

Quando uma feature futura for implementada e integrada à branch de referência, ela deve ser adicionada a este documento somente depois da validação do comportamento real.

---

## 28. Migrations

O banco utiliza Flyway para versionamento de schema, catálogos e seeds.

A versão de referência analisada contém migrations até **V119**.

Como novas migrations são adicionadas continuamente, este documento não mantém uma lista rígida de intervalos por feature. Para histórico exato, consulte:

```text
backend/src/main/resources/db/migration/
```

Entre os sistemas cobertos pelas migrations atuais estão:

- autenticação e jogadores;
- Digimons, progressão, Rebirth e Storage;
- itens, inventário e incubação;
- evolução e linhas evolutivas;
- missões, áreas e loja;
- equipamentos, sets, refinamento e perfis de raridade;
- bosses e Boss Mundial;
- Arena;
- Clãs, missões de Clã e Raid;
- Casa de Leilões;
- Correio;
- premiações de evento;
- auditoria/outbox;
- Loot Tables e baús;
- recompensas de conteúdo;
- tutorial;
- versionamento de sessão;
- soft delete de Clãs.

---

## Regra de manutenção deste documento

Ao implementar uma nova funcionalidade ou alterar uma regra existente:

1. atualizar o código e testes;
2. atualizar migrations/configurações quando aplicável;
3. atualizar este documento no mesmo PR quando a mudança afetar comportamento funcional;
4. evitar copiar valores de balanceamento que já possuem fonte configurável no banco;
5. deixar explícito quando algo for apenas planejamento.
