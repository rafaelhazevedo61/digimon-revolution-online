# Digimon Revolution Online - Documento Funcional

Documento completo das funcionalidades do projeto, incluindo regras de negocio, formulas, endpoints e fluxos.

---

## Indice

1. [Autenticacao](#1-autenticacao)
2. [Jogador (Player)](#2-jogador-player)
3. [Digimon](#3-digimon)
4. [Digitama e Incubacao](#4-digitama-e-incubacao)
5. [Evolucao](#5-evolucao)
6. [Rebirth](#6-rebirth)
7. [Missoes](#7-missoes)
8. [Loja (Shop)](#8-loja-shop)
9. [Inventario](#9-inventario)
10. [Equipamentos](#10-equipamentos)
11. [Bosses](#11-bosses)
12. [Ranking](#12-ranking)
13. [Slots e Storage](#13-slots-e-storage)
14. [Painel Administrativo](#14-painel-administrativo)
15. [Frontends](#15-frontends)

---

## 1. Autenticacao

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/auth/register` | Registrar novo jogador |
| POST | `/auth/login` | Login (retorna JWT token) |

### Fluxo
- Registro: cria Player com username, email e senha (hash).
- Login: valida credenciais, retorna JWT contendo `playerId`.
- Todas as rotas protegidas recebem o token via header `Authorization`.

---

## 2. Jogador (Player)

### Entidade `Player`

| Campo | Tipo | Descricao |
|-------|------|-----------|
| id | UUID | Identificador unico |
| username | String | Nome do jogador |
| email | String | Email do jogador |
| activeDigimonId | UUID | Digimon ativo selecionado |
| selectedDigitama | DigitamaType | Digitama selecionada para hatch |
| starterSelected | boolean | Se ja escolheu o starter |
| maxDigimonSlots | int | Limite de Digimons ativos (default: 3) |
| maxStorageSlots | int | Limite de Digimons no storage (default: 50) |

### Dashboard

**Endpoint:** `GET /players/me/dashboard`

Retorna o estado completo do jogador:
- Digimon ativo (stats, IVs, grade, equipamentos)
- Equipamentos equipados + set bonus
- Inventario resumido
- Missoes ativas (em andamento/completadas)
- Incubacao em andamento
- Slot info (ativos X/3, storage X/50)

---

## 3. Digimon

### Entidade `Digimon`

| Campo | Tipo | Descricao |
|-------|------|-----------|
| id | UUID | Identificador unico |
| playerId | UUID | Dono do Digimon |
| name | String | Nome (customizavel) |
| type | String | Tipo de origem (digitama) |
| stage | Stage | Estagio evolutivo |
| level | int | Nivel (1-100) |
| experience | int | XP atual |
| hp / attack / defense | int | Stats calculados |
| ivHp / ivAttack / ivDefense | int | IVs (0-100) |
| grade | DigimonGrade | Grade baseada nos IVs |
| rarity | Rarity | Raridade do Digimon |
| personality | Personality | Personalidade |
| trait | Trait | Habilidade especial (pode ser null) |
| energy / maxEnergy | int | Energia para missoes/bosses |
| bits | int | Moeda do Digimon |
| rebirthCount | int | Quantidade de rebirths |
| status | DigimonStatus | ACTIVE, STORED ou REBORN |
| digimonInfoId | Long | Referencia ao DigimonInfos (especie) |
| weaponId / armorId / accessoryId | UUID | Equipamentos equipados |

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/digimon` | Listar Digimons ativos do jogador |
| GET | `/digimon/{id}` | Detalhes de um Digimon |
| GET | `/digimon/{id}/lineage` | Linhagem de rebirths |
| PUT | `/digimon/rename` | Renomear Digimon (max 20 chars) |
| POST | `/digimon/{id}/select` | Definir como Digimon ativo |
| POST | `/digimon/{id}/store` | Guardar no storage |
| POST | `/digimon/{id}/retrieve` | Retirar do storage |
| GET | `/digimon/storage` | Listar Digimons no storage |

### Estagios Evolutivos (Stage)

```
BABY -> BABY_II -> ROOKIE -> CHAMPION -> ULTIMATE -> MEGA
```

### Raridades do Digimon

| Raridade | Multiplicador de Stats | Multiplicador de XP | IV Minimo |
|----------|----------------------|---------------------|-----------|
| COMMON | x1.00 | x1.00 | 0 |
| RARE | x1.10 | x1.05 | 25 |
| EPIC | x1.25 | x1.10 | 50 |
| LEGENDARY | x1.50 | x1.20 | 75 |

### Personalidades

| Personalidade | Efeito |
|--------------|--------|
| DURABLE | +10% HP |
| FIGHTER | +10% ATK |
| DEFENDER | +10% DEF |
| BRAINY | +5% ATK, +5% XP |
| NIMBLE | +5% ATK, +5% DEF |
| LIVELY | +10% XP |

### Traits (Habilidades Especiais)

| Trait | Efeito |
|-------|--------|
| FAST_LEARNER | +10% XP |
| ENERGETIC | +5 energia maxima |
| VITALITY | +10% HP |
| BERSERKER | +10% ATK |
| IRON_BODY | +10% DEF |

Chance de ter Trait ao nascer: 5% (hatch normal) / 10%+2%/rebirth ate 40% (rebirth).

### Grade do Digimon

Baseada nos IVs (0-100 cada):

| Grade | Criterio |
|-------|----------|
| SSS | 3 IVs perfeitos (100) |
| SS | 2 IVs perfeitos |
| S | 1 IV perfeito |
| A | Media dos IVs >= 85 |
| B | Media >= 70 |
| C | Media >= 55 |
| D | Media >= 40 |
| E | Media < 40 |

### Formula de Stats

```
stat = floor((baseStat + IV * peso) * rarityMult * stageMult * personalityMult * traitMult * rebirthMult)
```

Pesos dos IVs: HP = 0.30, ATK = 0.20, DEF = 0.20

Multiplicadores por Stage:

| Stage | Multiplicador |
|-------|--------------|
| BABY | x1.0 |
| BABY_II | x1.1 |
| ROOKIE | x1.2 |
| CHAMPION | x1.5 |
| ULTIMATE | x2.0 |
| MEGA | x2.8 |

### Level Up

- Level maximo: 100
- XP para proximo nivel: `nivel * 100` (level 1 = 100 XP, level 50 = 5000 XP)
- Ao subir de nivel: +2 HP, +1 ATK, +1 DEF
- XP ganho sofre multiplicadores de raridade, personalidade e trait

### Energia

- Energia maxima padrao: 20 (trait ENERGETIC: 25)
- Regeneracao: 1 energia a cada 5 minutos
- Consumida por missoes e bosses

### Bits (Moeda)

- Bits ficam vinculados ao Digimon (nao ao jogador)
- Usados para: compras na loja, refinamento, rebirth
- Ganhos via: missoes, bosses

---

## 4. Digitama e Incubacao

### Digitamas Disponiveis

| Tipo | Pool Code | Possiveis Babies |
|------|-----------|-----------------|
| STARTER | DIGITAMA_STARTER | Botamon, Pichimon, Pabumon, Punimon, Poyomon, Yuramon |
| FIRE | DIGITAMA_FIRE | Botamon, Punimon |
| WATER | DIGITAMA_WATER | Pichimon, Poyomon |
| NATURE | DIGITAMA_NATURE | Pabumon, Yuramon |

### Fluxo: Escolha Inicial (Starter)

1. Jogador novo escolhe uma Digitama via `POST /digitama/select`
2. Choca via `POST /digitama/hatch`
3. Sistema sorteia: especie (DigimonInfo) da pool, raridade, personalidade, trait, IVs
4. Digimon nasce como BABY nivel 1

### Fluxo: Incubacao

1. Jogador usa Digitama + Incubadora do inventario
2. `POST /incubation/start` - inicia incubacao (consome os itens)
3. Aguarda tempo da incubadora:
   - INCUBATOR_COMMON: 5 minutos
   - INCUBATOR_RARE: 2 minutos
   - INCUBATOR_EPIC: 30 segundos
4. `POST /incubation/claim` - resgata o Digimon nascido
5. Validacao: slots ativos nao podem estar cheios (max 3)

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/digitama/select` | Selecionar digitama (starter) |
| POST | `/digitama/hatch` | Chocar digitama |
| GET | `/digitama/history` | Historico de chocas |
| GET | `/digitama/pools` | Pools disponiveis |
| POST | `/incubation/start` | Iniciar incubacao |
| POST | `/incubation/claim` | Resgatar Digimon da incubacao |

---

## 5. Evolucao

### Requisitos por Estagio

| De | Para | Level Minimo | Fragmento | Quantidade |
|----|------|-------------|-----------|------------|
| BABY | BABY_II | 10 | - | - |
| BABY_II | ROOKIE | 15 | FRAGMENT_ROOKIE | 5 |
| ROOKIE | CHAMPION | 25 | FRAGMENT_CHAMPION | 10 |
| CHAMPION | ULTIMATE | 50 | FRAGMENT_ULTIMATE | 20 |
| ULTIMATE | MEGA | 75 | FRAGMENT_MEGA | 50 |

### Linhas de Evolucao

- Cada especie pertence a uma ou mais linhas de evolucao (tabela `evolution_lines`)
- Cada linha define os steps ordenados (BABY -> BABY_II -> ROOKIE -> ... -> MEGA)
- Cada step pode exigir materiais especificos (tabela `evolution_step_materials`)
- Se o Digimon tem multiplas linhas disponiveis, o jogador deve escolher qual seguir

### Fluxo

1. `POST /digimon/evolve` com `evolutionLineId` (opcional se so ha uma linha)
2. Valida level minimo do proximo step
3. Consome materiais necessarios (fragmentos + materiais de evolucao)
4. Recalcula stats com novo `stageMultiplier`
5. Se o Digimon nao tem nome customizado, atualiza o nome para a nova especie
6. Atualiza `digimonInfoId` para a nova especie

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/digimon/evolve` | Evoluir Digimon |
| GET | `/digimon-infos` | Lista todas as especies |
| GET | `/digimon-infos/{id}` | Detalhes de uma especie |

---

## 6. Rebirth

### Pre-requisitos
- Digimon deve estar ACTIVE
- Level 100
- Stage Champion ou superior
- Nao pode estar em missao ativa
- Bits e Data Core suficientes

### Custos

| Rebirth # | Bits | Data Core |
|-----------|------|-----------|
| 1o | 10.000 | 1 |
| 2o | 20.000 | 2 |
| 3o | 30.000 | 3 |
| N-esimo | N * 10.000 | N |

### Bonus de Rebirth

**IV Minimo:** +3 por rebirth (cap: +30 no rebirth 10+)

**Stats:** +2% por rebirth (cap: +20% no rebirth 10+)

### Heranca de IVs

O novo Digimon nao herda IVs diretamente. Em vez disso:

- IV anterior = 100: minimo garantido = 90
- IV anterior >= 90: minimo garantido = 75
- Demais: `max(rarityMinIv + rebirthBonus, previousIv / 2)`
- Novo IV sorteado entre o minimo calculado e 100

### Heranca de Raridade

- Chance de herdar: 40% + 2% por rebirth (cap: 70%)
- Se nao herdar: sorteio com pesos dinamicos (COMMON diminui, melhores aumentam com rebirths)

### Fluxo

1. `POST /digimon/{id}/rebirth` (preview: `GET /digimon/{id}/rebirth/preview`)
2. Valida pre-requisitos e custos
3. Digimon antigo recebe status REBORN
4. Novo Digimon criado como BABY nivel 1 com:
   - Novo sorteio de raridade (com heranca), personalidade e trait
   - IVs com piso herdado
   - Bits do Digimon antigo transferidos
   - `rebirthCount` incrementado
   - `rebornedFrom` aponta para o antigo
5. Se o antigo era o Digimon ativo, o novo assume

---

## 7. Missoes

### Estrutura

Cada missao (tabela `mission_definitions`) possui:
- Area, tipo (EASY/NORMAL/HARD), requisitos de stage/level
- Duracao, custo de energia
- XP base, recompensas fixas e loot table aleatoria

### Areas do Mundo

| Area | Stage Necessario |
|------|-----------------|
| NATIVE_FOREST | BABY |
| GEAR_SAVANNA | ROOKIE |
| FACTORIAL_TOWN | ROOKIE |
| FREEZELAND | CHAMPION |
| SERVER_DESERT | ULTIMATE |
| INFINITY_MOUNTAIN | MEGA |

Uma area e desbloqueada quando qualquer Digimon do jogador atingiu o stage minimo.

### Fluxo

1. `GET /missions/available` - listar missoes desbloqueadas
2. `POST /missions/start` - iniciar missao (consome energia, cria instancia com timer)
3. Aguardar duracao
4. `POST /missions/{instanceId}/claim` - resgatar recompensas

### Recompensas

- **XP:** `baseXp * progressMultiplier` (progressMultiplier = 1 + completions * 0.01)
- **Items fixos:** quantidade escala com progress multiplier
- **Loot aleatorio:** roll na loot table da missao

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/missions/available` | Missoes disponiveis |
| GET | `/missions/active` | Missoes em andamento |
| POST | `/missions/start` | Iniciar missao |
| POST | `/missions/{id}/claim` | Resgatar recompensa |
| GET | `/areas` | Listar areas do mundo |

---

## 8. Loja (Shop)

### Tipos de Produto

- **ITEM:** Pocoes, digitamas, incubadoras, fragmentos, materiais
- **EQUIPMENT:** Equipamentos (templates)

### Comprar

1. `POST /shop/buy` com `productCode` e `quantity`
2. Debita Bits do Digimon ativo
3. Equipamentos: 1 por vez, raridade sempre COMMON (perfil SHOP: 100% Common)
4. Items: pode comprar multiplos

### Vender

1. `POST /shop/sell` com `productCode`/`equipmentId`/`itemType` e `quantity`
2. Credita Bits no Digimon ativo
3. Equipamentos equipados nao podem ser vendidos

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/shop/products` | Catalogo da loja |
| POST | `/shop/buy` | Comprar produto |
| POST | `/shop/sell` | Vender item/equipamento |

---

## 9. Inventario

### Tipos de Item

| Categoria | Itens |
|-----------|-------|
| Pocoes | POTION_SMALL |
| Recursos | TRAINING_STONE, DATA_CORE |
| Digitamas | DIGITAMA_STARTER, DIGITAMA_FIRE, DIGITAMA_WATER, DIGITAMA_NATURE |
| Incubadoras | INCUBATOR_COMMON, INCUBATOR_RARE, INCUBATOR_EPIC |
| Fragmentos | FRAGMENT_ROOKIE, FRAGMENT_CHAMPION, FRAGMENT_ULTIMATE, FRAGMENT_MEGA |
| Materiais | EVOLUTION_MATERIAL, REFINEMENT_STONE |

### Notas
- Inventario e vinculado ao Digimon (nao ao jogador)
- Itens possuem definicoes com nome, descricao, preco de venda, se vendavel
- Materiais de evolucao sao diferenciados por `material_code`

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/inventory` | Inventario do Digimon ativo |
| GET | `/inventory/equipments` | Equipamentos do Digimon ativo |

---

## 10. Equipamentos

### Entidade `Equipment`

| Campo | Tipo | Descricao |
|-------|------|-----------|
| name | String | Nome do equipamento |
| slot | EquipmentSlot | WEAPON, ARMOR ou ACCESSORY |
| rarity | EquipmentRarity | Raridade da instancia |
| bonusHp / bonusAttack / bonusDefense | int | Stats base (do template) |
| setCode | String | Codigo do set |
| tier | int | Tier (1-10) |
| refinementLevel | int | Nivel de refinamento (0-10) |
| equipped | boolean | Se esta equipado |

### Slots

| Slot | Descricao |
|------|-----------|
| WEAPON | Arma |
| ARMOR | Armadura |
| ACCESSORY | Acessorio |

Cada Digimon pode equipar 1 item por slot (3 slots total).

### Templates de Equipamento

Templates definem os stats base por set/slot/tier. A raridade NAO faz parte do template - e determinada no momento do drop/concessao.

120 templates ativos: 4 sets x 3 slots x 10 tiers.

### Raridade de Equipamento

| Raridade | Multiplicador de Stats |
|----------|----------------------|
| COMMON | x1.00 |
| RARE | x1.15 |
| EPIC | x1.30 |
| LEGENDARY | x1.50 |

### Stats Efetivos

```
effectiveBonus = round(bonusBase * rarityMultiplier) + (refinementLevel * 2)
```

### Sets de Equipamento

| Set | 2 pecas | 3 pecas |
|-----|---------|---------|
| BERSERKER | +10% ATK | +20% ATK |
| GUARDIAN | +5% HP, +10% DEF | +10% HP, +20% DEF |
| VITALITY | +10% HP | +20% HP |
| BALANCED | +5% HP/ATK/DEF | +10% HP/ATK/DEF |

Set bonus e calculado sobre o total de stats efetivos dos equipamentos.

### Refinamento

| De -> Para | Custo (Bits) | Taxa de Sucesso |
|-----------|-------------|----------------|
| +0 -> +1 | 1.000 | 100% |
| +1 -> +2 | 1.500 | 95% |
| +2 -> +3 | 2.000 | 90% |
| +3 -> +4 | 2.500 | 80% |
| +4 -> +5 | 3.000 | 70% |
| +5 -> +6 | 3.500 | 60% |
| +6 -> +7 | 4.000 | 50% |
| +7 -> +8 | 4.500 | 40% |
| +8 -> +9 | 5.000 | 30% |
| +9 -> +10 | 5.500 | 20% |

- Formula do custo: `1000 + (currentLevel * 500)`
- Consome 1 REFINEMENT_STONE por tentativa
- Se falhar: nivel permanece (sem downgrade)
- Cada nivel de refinamento adiciona +2 a cada stat efetivo

### Perfis de Raridade (Roll de Equipamento)

| Perfil | Common | Rare | Epic | Legendary |
|--------|--------|------|------|-----------|
| DEFAULT | 60% | 25% | 12% | 3% |
| BOSS_NORMAL | 65% | 22% | 10% | 3% |
| BOSS_DAILY | 55% | 28% | 13% | 4% |
| BOSS_WEEKLY | 40% | 30% | 20% | 10% |
| BOSS_MONTHLY | 20% | 30% | 30% | 20% |
| SHOP | 100% | 0% | 0% | 0% |

Validacao: a soma dos 4 valores deve ser exatamente 100.

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| POST | `/equipment/{id}/equip` | Equipar item |
| POST | `/equipment/{id}/unequip` | Desequipar item |
| POST | `/equipment/{id}/refine` | Refinar equipamento |

---

## 11. Bosses

### Tipos de Boss

| Tipo | Cooldown | Rotacao |
|------|----------|---------|
| NORMAL | Variavel (config) | Sempre disponivel |
| DAILY | Variavel (config) | Rotacao diaria por stage |
| WEEKLY | Variavel (config) | Sempre disponivel |
| MONTHLY | Variavel (config) | Sempre disponivel |

### Entidade `BossDefinition`

Cada boss possui:
- `code`, `name`, `bossType`, `requiredStage`, `requiredLevel`, `requiredRebirths`
- Stats: `hp`, `atk`, `def`
- `energyCost`, `cooldownMinutes`
- Recompensas: `baseXpReward`, `baseBitsReward`, `defeatXpPercent`
- Lista de drops (tabela `boss_drops`)
- `imageUrl`, `active`

### Formula de Combate

```
Power = HP * 0.30 + ATK * 1.50 + DEF * 1.00
WinChance = clamp(round(digimonPower / bossPower * 100), 5, 95)
```

- Digimon Power inclui equipamentos + set bonus
- Threshold minimo: 30% (abaixo disso = derrota automatica, sem rolar dado)
- Acima do threshold: rola dado 1-100, vitoria se roll <= winChance
- Consome energia do Digimon (mesmo se perder)

### Rotacao Diaria

Bosses do tipo DAILY do mesmo stage rotacionam automaticamente:
- Sistema usa `dayIndex = epoch_day % total_bosses_daily_do_mesmo_stage`
- Apenas 1 boss diario ativo por stage por dia

### Drops (Loot)

**Equipamentos (agrupados):**
- Cada boss tem 120 drops EQUIPMENT na loot table (todos os templates)
- Chance fixa (25%) de dropar equipamento - 1 unico roll
- Se passou: sorteia 1 template aleatorio dos 120
- Rola raridade via perfil do tipo de boss (BOSS_NORMAL, BOSS_DAILY, etc.)

**Items:**
- Cada drop tem chance individual
- Quantidade entre `minQuantity` e `maxQuantity` (roll aleatorio)

### Recompensas

**Vitoria:**
- XP base do boss
- Bits base do boss
- Roll de drops

**Derrota:**
- XP parcial: `baseXp * defeatXpPercent / 100`
- 0 Bits
- Sem drops

### Cooldown

Cada boss tem cooldown individual por jogador. Valida ultimo attempt.

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/bosses/available` | Bosses disponiveis (inclui winChance calculada) |
| POST | `/bosses/{code}/challenge` | Desafiar boss |
| GET | `/bosses/history` | Historico de tentativas |
| GET | `/bosses/cooldowns` | Cooldowns ativos |

---

## 12. Ranking

### Categorias

| Ranking | Ordenacao |
|---------|----------|
| Por Nivel | Level desc, XP desc |
| Por Grade | Grade ordinal, IVs |
| Por Rebirth | rebirthCount desc, level desc |

### Regras
- Apenas Digimons com status ACTIVE aparecem no ranking
- Digimons STORED e REBORN sao excluidos
- Paginacao: `page` e `size`
- Frontend destaca cards do jogador logado com borda cyan

### Endpoints

| Metodo | Rota | Descricao |
|--------|------|-----------|
| GET | `/ranking/level` | Ranking por nivel |
| GET | `/ranking/grade` | Ranking por grade |
| GET | `/ranking/rebirth` | Ranking por rebirth |

---

## 13. Slots e Storage

### Limites

| Tipo | Default | Descricao |
|------|---------|-----------|
| Slots Ativos | 3 | Digimons que participam de combate/missoes/ranking |
| Slots Storage | 50 | Digimons inativos guardados |

### Guardar Digimon (Store)

1. Digimon deve estar ACTIVE
2. Nao pode ser o Digimon ativo (`activeDigimonId`)
3. Storage nao pode estar cheio
4. **Auto-desequipa** todos os equipamentos antes de mover
5. Status muda para STORED

### Retirar Digimon (Retrieve)

1. Digimon deve estar STORED
2. Slots ativos nao podem estar cheios
3. Status muda para ACTIVE

### Impacto em Outras Features

- **Incubacao:** Claim bloqueado se slots ativos cheios
- **Ranking:** Apenas ACTIVE aparecem
- **Missoes/Bosses:** Apenas Digimon ativo pode participar
- **Equipamentos:** Auto-desequipados ao guardar

---

## 14. Painel Administrativo

### Modulos Admin

| Modulo | Funcionalidade |
|--------|---------------|
| Equipment Templates | CRUD completo + busca + ordenacao |
| Shop Products | CRUD completo (items e equipamentos) |
| Missions | CRUD completo com rewards e loot table |
| Bosses | CRUD completo com drops |
| Digimon Infos | Visualizar/gerenciar especies |
| Evolution Lines | Gerenciar linhas de evolucao |
| Players | Visualizar jogadores |
| Items | Gerenciar definicoes de itens |

### Simuladores

**Simulador de Equipamento:**
- Seleciona template, tier, raridade, refinamento
- Calcula stats efetivos com multiplicadores
- Mostra comparativo por raridade e por tier

**Simulador de Digimon:**
- Seleciona especie, raridade, stage, personalidade, trait
- Ajusta IVs individuais (sliders + botoes Min/Mid/Max/Aleatorio)
- Calcula stats em tempo real com a formula do `DigimonFactory`
- Grade automatica, combat power, comparativos por raridade e stage

### Endpoints Admin

Todos os endpoints admin ficam em `/admin/*`:
- `/admin/equipment-templates` - CRUD templates
- `/admin/shop-products` - CRUD produtos
- `/admin/missions` - CRUD missoes
- `/admin/bosses` - CRUD bosses + drops

---

## 15. Frontends

### Game Frontend (PWA)

Aplicacao web progressiva (SPA) para jogadores.

| Tela | Arquivo | Descricao |
|------|---------|-----------|
| Login/Registro | auth.js | Autenticacao |
| Starter | starter.js | Escolha da digitama + selecao de Digimon + slot info |
| Dashboard | dashboard.js | Tela principal com stats, equips, missoes ativas |
| Missoes | missions.js | Listar areas, missoes disponiveis, iniciar/resgatar |
| Loja | shop.js | Comprar/vender items e equipamentos |
| Inventario | inventory.js | Listar items e equipamentos, equipar/desequipar, refinar |
| Evolucao | evolution.js | Evoluir Digimon |
| Rebirth | rebirth.js | Preview e execucao de rebirth |
| Ranking | ranking.js | Rankings (nivel, grade, rebirth) com destaque pessoal |
| Incubacao | incubation.js | Selecionar digitama/incubadora, incubar, resgatar |
| Pokedex | pokedex.js | Catalogo de especies (DigimonInfo) |
| Bosses | bosses.js | Filtros por stage/tipo, modal de detalhes, desafiar |
| Storage | storage.js | Listar/retirar Digimons guardados |
| Mais | more.js | Menu com links para storage e funcoes extras |

### Admin Frontend

Painel administrativo para gestao do jogo.

| Tela | Arquivo | Descricao |
|------|---------|-----------|
| Dashboard | dashboard.js | Visao geral |
| Equipment Templates | equipment-templates.js | CRUD + busca/ordenacao |
| Shop Products | shop-products.js | CRUD produtos |
| Missions | missions.js | CRUD missoes |
| Bosses | bosses.js | CRUD bosses + drops |
| Digimon Infos | digimon-infos.js | Especies |
| Evolution Lines | evolution-lines.js | Linhas de evolucao |
| Players | players.js | Jogadores |
| Items | items.js | Definicoes de itens |
| Simulador Equip | equipment-simulator.js | Simulador de equipamento |
| Simulador Digimon | digimon-simulator.js | Simulador de Digimon |

---

## Apendice: Migrations

O banco utiliza Flyway para versionamento. Migrations V1 a V70 cobrem:

- V1-V9: Schema inicial (players, digimons, inventario, incubacao)
- V10-V15: Personalidade, energia, rebirth, trait
- V16-V19: Equipamentos, slots, inventario por Digimon
- V20-V27: Starter, DigimonInfos, content tables
- V28-V45: Evolution lines, digitama pools, materials, item definitions
- V46-V56: Mission definitions, shop products, equipment templates (migrado de hardcoded para DB)
- V57-V62: Digitama pools extras, reestruturacao de equipamentos (sets/tiers/refinamento)
- V63-V66: Boss definitions, attempts, seeds
- V67-V69: Separacao de raridade do template, perfis de raridade, loot table cross join
- V70: Digimon slots e storage
