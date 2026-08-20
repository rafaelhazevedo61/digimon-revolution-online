# Planejamento de baús temáticos e revisão de drops

**Estado:** Sprint 1 em implementação — modelo de loot tables e baús
**Data:** 20 de agosto de 2026

## 1. Objetivo

Padronizar as recompensas dos modos Missões, Bosses, Arena e Boss Mundial por meio de **baús temáticos**. O modo de jogo entrega um baú; o jogador abre o baú e recebe um item sorteado a partir de uma raridade e de uma pool compatível.

A proposta mantém a progressão compreensível, permite revisar percentuais sem espalhar regras pelo código e possibilita que cada modo tenha identidade própria sem criar uma lógica de drop completamente diferente para cada sistema.

> **Modelo central:** primeiro sorteamos a raridade do baú; depois sorteamos um item dentro da pool daquela raridade.

## 2. Inventário confirmado do projeto

O enum `ItemType` atualmente contém os seguintes tipos relevantes:

| Grupo | Tipos existentes |
|---|---|
| Poção | `POTION_SMALL` |
| Recursos | `TRAINING_STONE`, `DATA_CORE` |
| Digitamas | `DIGITAMA_STARTER`, `DIGITAMA_FIRE`, `DIGITAMA_WATER`, `DIGITAMA_NATURE` |
| Incubadoras | `INCUBATOR_COMMON`, `INCUBATOR_RARE`, `INCUBATOR_EPIC` |
| Fragmentos por estágio legados | `FRAGMENT_ROOKIE`, `FRAGMENT_CHAMPION`, `FRAGMENT_ULTIMATE`, `FRAGMENT_MEGA` |
| Materiais nomeados de evolução | `EVOLUTION_MATERIAL`, diferenciado por `material_code`, como Fragmento Agumon e Fragmento Gabumon |
| Baús temáticos | `LOOT_CHEST`, diferenciado pelo código da definição do baú |
| Refinamento | `REFINEMENT_STONE` |

Há duas estruturas diferentes, mas a primeira é considerada legado de projeto:

- `FRAGMENT_ROOKIE`, `FRAGMENT_CHAMPION`, `FRAGMENT_ULTIMATE` e `FRAGMENT_MEGA` foram conceitos iniciais para representar progressão por estágio. Podem permanecer temporariamente por compatibilidade, mas não devem orientar as novas pools.
- A progressão definitiva usa `EVOLUTION_MATERIAL` com `material_code` nomeado por Digimon ou por linha evolutiva, como Fragmento Agumon e Fragmento Gabumon.

O inventário técnico existente já foi confirmado nas migrations V31, V36, V37, V40, V42 e V45. Há 30 materiais nomeados de evolução, distribuídos em seis linhas completas: Agumon, Gabumon, Patamon, Biyomon, Tentomon e Gomamon. Cada linha possui um material para `BABY_II`, `ROOKIE`, `CHAMPION`, `ULTIMATE` e `MEGA`.

| Estágio de destino | Quantidade exigida na evolução | Nível mínimo | Raridade cadastrada | Ícone cadastrado |
|---|---:|---:|---|---|
| `BABY_II` | 5 | 10 | `COMMON` | `fragment_baby2` |
| `ROOKIE` | 10 | 15 | `COMMON` | `fragment_rookie_specific` |
| `CHAMPION` | 20 | 25 | `RARE` | `fragment_champion_specific` |
| `ULTIMATE` | 30 | 50 | `EPIC` | `fragment_ultimate_specific` |
| `MEGA` | 50 | 75 | `LEGENDARY` | `fragment_mega_specific` |

A quantidade acima é o requisito por etapa definido em `evolution_step_materials` na V40. Ela não deve ser confundida com o `sell_price` presente nas definições de item da V45. Os nomes, descrições, categoria `EVOLUTION_MATERIAL`, regras de negociação, limite de pilha e ícones também já estão cadastrados. O que ainda será criado pelo sistema de baús é apenas a origem de obtenção configurável nas loot tables. As novas loot tables devem priorizar esses materiais nomeados; fragmentos genéricos só devem aparecer em uma tabela de compatibilidade ou migração explicitamente aprovada.

## 3. Percentuais atuais de Missões

As migrations atuais usam quatro raridades — `COMMON`, `RARE`, `EPIC` e `LEGENDARY` — e todas as distribuições somam 100% por missão. Os valores abaixo são a referência existente antes da mudança para baús:

| Área | Missão | Common | Rare | Epic | Legendary |
|---|---|---:|---:|---:|---:|
| Floresta Nativa | `MISSION_NF_2` | 70% | 20% | 8% | 2% |
| Floresta Nativa | `MISSION_NF_3` | 65% | 22% | 10% | 3% |
| Gear Savanna | `MISSION_GS_2` | 60% | 25% | 12% | 3% |
| Gear Savanna | `MISSION_GS_3` | 58% | 25% | 13% | 4% |
| Factorial Town | `MISSION_FT_2` | 58% | 24% | 13% | 5% |
| Factorial Town | `MISSION_FT_3` | 55% | 25% | 14% | 6% |
| Freezeland | `MISSION_FL_2` | 52% | 26% | 15% | 7% |
| Freezeland | `MISSION_FL_3` | 50% | 27% | 15% | 8% |
| Server Desert | `MISSION_SD_2` | 48% | 27% | 17% | 8% |
| Server Desert | `MISSION_SD_3` | 45% | 28% | 18% | 9% |
| Infinity Mountain | `MISSION_IM_2` | 42% | 28% | 19% | 11% |
| Infinity Mountain | `MISSION_IM_3` | 38% | 28% | 20% | 14% |

Essa progressão já segue a intenção de tornar as missões mais difíceis melhores: a chance de `COMMON` cai e as chances de `EPIC`/`LEGENDARY` sobem. O ponto a decidir é se esses percentuais passarão a definir a raridade do **Baú da Área** ou se a missão também terá um modificador próprio.

## 4. Situação atual dos modos

| Modo | Estado atual identificado | Mudança proposta |
|---|---|---|
| Missões | Recompensas base e `mission_loot_items` por raridade. Há Training Stone, Data Core, alguns Digitamas, incubadoras e fragmentos genéricos legados. | Remover a entrega direta de itens variáveis e entregar um Baú da Área; priorizar materiais nomeados de evolução nas novas pools. |
| Bosses comuns | O sistema possui recompensas de XP/Bits e tabelas de loot de equipamentos/raridade. | Entregar Baú Boss com pool de consumíveis, materiais e, quando apropriado, equipamentos. |
| Arena | Recompensa atual em Bits e Arena Coins: vitória dá Bits de 25 a 200 e moedas de Arena de 15 a 40; derrota concede 5 moedas de Arena. Há cinco desafios diários. | Entregar Baú Arena por tier principalmente em vitórias, mantendo Bits/Moedas como recompensa direta. |
| Boss Mundial | Cada ataque grava dano, XP e Bits. O modelo atual não possui drop de item por ataque. | Entregar Baú Boss Mundial com regra de limite por jogador/ciclo, sem permitir farm ilimitado por ataques repetidos. |

A transformação para baús não deve remover XP, Bits, Arena Coins ou recompensas fixas que tenham função de progressão. O objetivo inicial é substituir o sorteio direto de itens, não eliminar todas as recompensas atuais.

## 5. Nomenclatura proposta

Os nomes exibidos ao jogador podem ser:

| Origem | Nome exibido | Código recomendado |
|---|---|---|
| Arena Bronze | Baú Arena Bronze | `CHEST_ARENA_BRONZE` |
| Arena Prata | Baú Arena Prata | `CHEST_ARENA_PRATA` |
| Arena Ouro | Baú Arena Ouro | `CHEST_ARENA_OURO` |
| Arena Platina | Baú Arena Platina | `CHEST_ARENA_PLATINA` |
| Arena Diamante | Baú Arena Diamante | `CHEST_ARENA_DIAMANTE` |
| Missão Floresta Nativa | Baú Floresta Nativa | `CHEST_MISSION_NATIVE_FOREST` |
| Missão Gear Savanna | Baú Gear Savanna | `CHEST_MISSION_GEAR_SAVANNA` |
| Missão Factorial Town | Baú Factorial Town | `CHEST_MISSION_FACTORIAL_TOWN` |
| Missão Freezeland | Baú Freezeland | `CHEST_MISSION_FREEZELAND` |
| Missão Server Desert | Baú Server Desert | `CHEST_MISSION_SERVER_DESERT` |
| Missão Infinity Mountain | Baú Infinity Mountain | `CHEST_MISSION_INFINITY_MOUNTAIN` |
| Boss Mundial | Baú Boss Mundial — nome do boss | `CHEST_WORLD_BOSS_<BOSS_CODE>` |
| Boss normal | Baú Boss — nome do boss | `CHEST_BOSS_<BOSS_CODE>` |
| Boss diário | Baú Boss Diário — nome do boss | `CHEST_BOSS_DAILY_<BOSS_CODE>` |
| Boss semanal | Baú Boss Semanal — nome do boss | `CHEST_BOSS_WEEKLY_<BOSS_CODE>` |
| Boss mensal | Baú Boss Mensal — nome do boss | `CHEST_BOSS_MONTHLY_<BOSS_CODE>` |

Os tiers oficiais da Arena já existentes são **Bronze, Prata, Ouro, Platina e Diamante**. Não devemos adicionar outros nomes sem revisar a regra de rating.

Tecnicamente, é preferível ter um tipo genérico de item para baú, como `LOOT_CHEST`, acompanhado de `chest_code`, do que criar um enum novo para cada baú. Assim, novos bosses e áreas podem ser cadastrados no banco sem alterar Java.

## 6. Modelo de sorteio

Cada abertura deve seguir este fluxo:

```text
Jogador possui um baú
        |
        v
Validação de propriedade e quantidade
        |
        v
Sorteio da raridade do baú
        |
        v
Seleção ponderada de uma entrada da pool daquela raridade
        |
        v
Entrega atômica do item e remoção de um baú
        |
        v
Auditoria positiva da abertura
```

Cada baú deve ter uma tabela de pesos por raridade. Cada raridade deve ter uma pool de itens com peso, quantidade mínima, quantidade máxima e condições opcionais. A soma dos pesos de raridade deve ser validada como 100 ou normalizada de maneira explícita; a soma dos pesos da pool deve ser maior que zero.

A abertura deve ser uma operação PostgreSQL transacional. Não é aceitável remover o baú e falhar antes de entregar o item, nem entregar o item duas vezes após uma repetição da requisição. A abertura deve gerar uma auditoria positiva com o código do baú, raridade sorteada, item entregue e quantidade, sem token ou senha.

## 7. Pools conceituais iniciais

Os percentuais abaixo são uma proposta de organização, não valores finais. Primeiro precisamos decidir a economia e simular a velocidade de aquisição.

| Raridade | Pool conceitual |
|---|---|
| Common | Poção Pequena, Pedra de Treino, Núcleo de Dados e materiais nomeados de evolução de entrada, quando compatíveis com a área. |
| Rare | Núcleo de Dados, Digitama elemental compatível, Incubadora Comum/Rara e materiais nomeados de evolução inicial. |
| Epic | Incubadora Rara/Épica, Digitamas, materiais nomeados de evolução intermediários e recompensas específicas da área. |
| Legendary | Incubadora Épica, materiais nomeados de evolução avançados, `EVOLUTION_MATERIAL` raro e possível recompensa exclusiva do boss/origem. |

A pool não precisa conter todos os itens em todos os baús. O Baú Floresta Nativa pode priorizar materiais nomeados de linhas iniciais, Training Stone e Digitama de Natureza; o Baú Infinity Mountain pode priorizar materiais nomeados de linhas avançadas, Incubadora Épica e recompensas específicas. Os fragmentos genéricos só devem ser usados como fallback legado ou em uma tabela de migração. A identidade da origem deve continuar perceptível.

A `POTION_SMALL`, `DIGITAMA_STARTER` e `REFINEMENT_STONE` precisam de decisão explícita. A recomendação inicial é não colocar `DIGITAMA_STARTER` em baús normais, pois ele pertence ao onboarding; e usar `REFINEMENT_STONE` em Bosses/equipamentos ou eventos, não como drop comum de qualquer missão.

## 8. Regras específicas por modo

### 8.1 Missões

Todas as missões de uma área entregam o mesmo Baú da Área. A missão mais difícil mantém a mesma identidade de pool, mas usa uma tabela de pesos de raridade mais favorável. Os percentuais atuais da tabela da seção 3 podem ser usados como ponto de partida.

Recomendação inicial: manter XP, custo de energia e duração da missão; substituir o item sorteado em `mission_loot_items` por uma unidade do Baú da Área. A abertura do baú faz o segundo sorteio. Isso evita que uma missão dê simultaneamente uma recompensa fixa e um item aleatório sem controle.

### 8.2 Arena

O jogador deve receber o Baú Arena correspondente ao tier do rating no momento da vitória. A derrota pode continuar entregando apenas a participação em Arena Coins na primeira versão. O limite de cinco desafios diários permanece como proteção contra inflação.

A abertura de um Baú Arena Bronze não deve acessar a pool de Diamante. Cada tier deve ter uma progressão própria. O tier pode ser recalculado no momento da recompensa ou fixado no resultado da partida; a recomendação é fixá-lo no resultado para evitar ambiguidades quando o rating mudar depois.

### 8.3 Bosses comuns e periódicos

O código do boss e a periodicidade devem identificar o baú. Um Boss Diário, Semanal ou Mensal pode ter a mesma família de pool, mas pesos e recompensas diferentes. A periodicidade não deve ser apenas parte do nome visual; precisa ser um campo/código persistido para auditoria e balanceamento.

A recompensa deve ter limite claro: uma vitória elegível pode gerar um baú, enquanto repetição bloqueada, cooldown ou derrota não deve gerar o mesmo prêmio. XP, Bits e equipamentos existentes devem ser revisados para evitar duplicação involuntária.

### 8.4 Boss Mundial

O Baú Boss Mundial deve estar ligado ao código do boss e ao ciclo diário. Não é recomendável entregar um baú a cada ataque sem limite, pois isso permitiria farm por múltiplos hits. A primeira proposta é uma recompensa de participação por jogador elegível no ciclo, além de recompensas de ranking/derrota conforme a regra definida.

A regra precisa responder explicitamente: o jogador recebe um baú ao primeiro ataque válido, ao atingir um dano mínimo, ao boss ser derrotado ou por posição no ranking? Essa decisão deve vir antes da implementação. O atual XP/Bits por ataque pode continuar separado, mas o baú deve ter idempotência por `player_id + boss_cycle_id + reward_type`.

## 9. Modelo de dados recomendado

| Entidade | Função |
|---|---|
| `chest_definitions` | Código, nome, descrição, ícone, origem, tier/periodicidade, ativo e negociável. |
| `chest_rarity_weights` | Pesos de Common, Rare, Epic e Legendary por baú ou regra de fonte. |
| `chest_drop_entries` | Pool por raridade, item/material, peso, quantidade mínima/máxima e condições. |
| `chest_openings` | Registro idempotente da abertura, raridade sorteada, item entregue e quantidade. |
| `reward_sources` | Opcional; liga o baú a missão, vitória, boss, ciclo ou tier. |

A criação do baú pode usar `ItemDefinition` se os catálogos atuais suportarem `item_type`, `material_code` e metadados. Se isso deixar o modelo ambíguo, uma tabela própria de baús será mais clara. A decisão deve ser tomada após revisar o CRUD administrativo de itens.

## 10. Sequência de implementação sugerida

| PR/Sprint | Escopo | Critério de aceite |
|---:|---|---|
| 1 | Inventário e limpeza das tabelas atuais | Todas as recompensas atuais por modo, área, boss e tier estão documentadas; nenhuma chance deixa de somar 100%. |
| 2 | Modelo de baús e migrations | Baús, pesos e pools são cadastráveis; chaves e validações impedem pools inválidas. |
| 3 | Abertura transacional | Consumir um baú entrega exatamente um item; retry não duplica; erro faz rollback. |
| 4 | Missões | Cada área entrega seu baú; dificuldade altera pesos de raridade; XP/energia permanecem corretos. |
| 5 | Bosses | Baús normal/diário/semanal/mensal são entregues sob as regras corretas e sem farm indevido. |
| 6 | Arena | Vitória entrega baú do tier correto; derrota e limite diário permanecem coerentes. |
| 7 | Boss Mundial | Baú do boss/ciclo é idempotente e limitado por jogador; ranking e participação ficam claros. |
| 8 | Interface e administração | Inventário mostra baús, abertura revela raridade/item e admin consegue revisar pools sem editar código. |
| 9 | Balanceamento | Simulações e testes manuais confirmam progressão, inflação e utilidade dos itens. |

## 11. Perguntas que precisam de decisão antes do código

1. O baú será negociável entre jogadores ou ficará vinculado ao jogador que o recebeu?
2. Cada abertura terá exatamente um item ou poderá ter quantidade variável/múltiplos itens?
3. O jogador verá a pool possível antes de abrir o baú?
4. O Baú Boss Mundial será obtido por ataque, dano mínimo, derrota do boss ou ranking?
5. Arena dará baú somente em vitória ou também um baú menor por participação?
6. As recompensas fixas atuais de missões, Bosses e World Boss permanecerão junto do baú?
7. Materiais específicos de evolução entrarão em todas as áreas ou somente em áreas/bosses compatíveis?
8. Um item raro poderá repetir ou haverá proteção contra duplicação?
9. Haverá limite diário/semanal de baús por modo?
10. A abertura será livre ou exigirá algum item adicional, como chave?

## 12. Recomendação

A ideia deve ser aprovada como direção, mas ainda não deve ser implementada antes de responder às perguntas da seção 11 e concluir a matriz de pools. O próximo trabalho recomendado é a **Sprint de Inventário e Balanceamento de Drops**, não uma sprint de robustez de produção.

Essa sprint deve produzir uma tabela final com cada baú, raridades, pesos, itens possíveis, quantidades, origem, limites e justificativa de progressão. Depois que essa matriz for aprovada, a implementação poderá ser dividida em PRs pequenos sem precisar redesenhar a economia no meio do desenvolvimento.


## 13. Decisões aprovadas

As decisões abaixo passam a ser a referência de negócio para a implementação.

| Tema | Decisão aprovada |
|---|---|
| Negociação | Baús poderão ser negociados entre jogadores. |
| Expiração | Baús não expiram. |
| Chave | A abertura não exigirá chave inicialmente. |
| Pool visível | A pool não será exibida dentro do jogo. A Wiki poderá documentar as possibilidades e regras. |
| Quantidade de itens | Cada abertura poderá entregar de 1 a 4 tipos diferentes de item. |
| Quantidade por item | Cada entrada terá quantidade mínima e máxima configuráveis. |
| Raridades | `COMMON`, `RARE`, `EPIC` e `LEGENDARY`. |
| Sorteio | Primeiro sorteia-se a raridade; depois uma entrada ponderada dentro da pool daquela raridade. |
| Duplicatas | Itens repetidos serão entregues normalmente. Não haverá proteção contra duplicatas inicialmente. |
| Limites | Não haverá limites diários, semanais ou mensais de obtenção de baús inicialmente. |
| Administração | Drops serão configurados por loot tables nomeadas e editáveis pelo painel admin, se a estrutura permitir. |
| Auditoria | A abertura será rastreável com jogador, baú, origem, raridade, itens, quantidades e identificadores técnicos. |

### 13.1 Missões

As recompensas de XP e Bits serão mantidas. O item aleatório atual será substituído por um Baú da Área. Cada missão continuará podendo ter sua própria configuração de loot, mas missões diferentes poderão apontar para a mesma `loot_table` quando compartilharem uma pool.

A dificuldade da missão continuará influenciando os pesos de raridade. Os percentuais atuais serão preservados inicialmente, incluindo a progressão já existente entre as seis áreas. Uma missão individual poderá receber uma loot table exclusiva quando for necessário inserir itens raros ou recompensas específicas.

Cada conclusão válida de missão entregará um baú. Cancelamento, falha ou expiração da missão não entregará o baú.

### 13.2 Arena

A Arena entregará um baú somente em caso de vitória. A derrota continuará podendo conceder Arena Coins como participação, mas não entregará baú inicialmente. O tier será capturado no momento da luta e seguirá os cinco tiers existentes: Bronze, Prata, Ouro, Platina e Diamante.

A vitória poderá entregar um baú por partida, respeitando as regras atuais de desafios. Não haverá temporada nesta primeira implementação.

### 13.3 Bosses

Os Bosses manterão XP, Bits e as recompensas estruturais existentes, enquanto a camada de itens será migrada para baús temáticos e loot tables. Os nomes conceituais serão:

```text
Baú Boss — nome do boss
Baú Boss Diário — nome do boss
Baú Boss Semanal — nome do boss
Baú Boss Mensal — nome do boss
```

A periodicidade e o boss serão dados da origem do baú, não apenas texto visual. Itens exclusivos poderão ser configurados em loot tables específicas, seguindo a mesma ideia de reutilização e especialização das Missões.

Os fragmentos/materiais específicos de evolução não serão relacionados aos Bosses existentes nesta primeira versão. Essa decisão poderá ser revisada quando novos Bosses ou linhas evolutivas forem adicionados.

### 13.4 Boss Mundial

O Boss Mundial terá três recompensas distintas:

| Recompensa | Critério |
|---|---|
| Baú de Tentativa | Uma unidade por tentativa válida realizada pelo jogador. |
| Baú de Maior Dano | Uma unidade para o jogador que causar o maior dano no ciclo do boss. |
| Baú de Derrota | Uma unidade para os jogadores elegíveis quando o boss for derrotado. |

Os três baús serão vinculados ao nome/código do boss. O ciclo será usado para impedir duplicidade técnica, mas não mudará o nome comercial do baú. A implementação deverá usar chaves idempotentes por jogador, boss e tipo de recompensa para que refresh, retry ou reprocessamento não duplique baús.

A regra de “uma unidade por tentativa” deve respeitar o número de tentativas válidas que o sistema permitir. Assim, se o modo continuar permitindo três tentativas por ciclo, o jogador poderá receber até três baús de tentativa, além dos bônus de maior dano e derrota quando for elegível.

## 14. Modelo final de loot tables

A unidade principal de configuração será uma loot table nomeada. Uma missão, boss, tier de Arena ou recompensa do Boss Mundial apontará para uma tabela por código.

```text
loot_table
    ├── nome/código
    ├── origem e descrição
    ├── ativo
    ├── pesos por raridade
    └── entradas por raridade
            ├── item ou material
            ├── peso da entrada
            ├── quantidade mínima
            ├── quantidade máxima
            ├── chance/condição opcional
            └── ativo
```

Uma abertura deve escolher de 1 a 4 entradas **diferentes**. O algoritmo deverá remover temporariamente uma entrada já escolhida ou recalcular a pool restante para impedir que o mesmo tipo apareça duas vezes na mesma abertura. A quantidade de cada entrada será sorteada dentro do intervalo configurado.

Uma tabela deve poder ser usada por várias fontes. Por exemplo:

```text
LOOT_MISSION_NATIVE_FOREST_DEFAULT
LOOT_MISSION_NATIVE_FOREST_ELITE
LOOT_BOSS_APOCALYMON_DAILY
LOOT_ARENA_BRONZE
LOOT_WORLD_BOSS_APOCALYMON_ATTEMPT
LOOT_WORLD_BOSS_APOCALYMON_TOP_DAMAGE
LOOT_WORLD_BOSS_APOCALYMON_DEFEAT
```

O baú deve apontar para a loot table no momento da criação ou ter a tabela resolvida pela origem. Para manter a rastreabilidade, a abertura deve registrar a versão ou identificação da tabela usada; alterações futuras não podem tornar uma abertura antiga impossível de explicar.

## 15. Primeira sprint de implementação: Missões

A primeira sprint de código será limitada às Missões. Bosses, Arena e Boss Mundial só serão migrados depois que o modelo de abertura for validado.

### PR 1 — Modelo de loot tables e baús

Criar migrations e entidades para baús, loot tables, pesos de raridade e entradas de pool. Adicionar validações para raridades inválidas, pesos negativos, quantidade mínima maior que a máxima, loot table sem entradas e pesos que não formem uma distribuição válida.

### PR 2 — Abertura transacional de baús

Implementar o uso de um baú com entrega atômica de 1 a 4 itens diferentes. A operação deve remover a unidade correta do inventário, sortear raridade e itens, inserir as recompensas e registrar auditoria. O retry da mesma requisição não pode consumir ou conceder duas vezes.

### PR 3 — Integração com Missões

Alterar a conclusão de missão para manter XP/Bits e entregar o Baú da Área. Cada missão deve apontar para sua loot table; missões que compartilham pool poderão apontar para o mesmo código. Os percentuais atuais devem ser migrados para a tabela de raridade correspondente.

### PR 4 — Painel administrativo e visualização do inventário

Permitir cadastrar/editar loot tables, pesos, entradas e quantidades. No jogo, exibir apenas o baú no inventário e uma ação de abrir; não exibir a pool antes da abertura. A tela de resultado deverá revelar raridade, item e quantidade após a abertura.

### PR 5 — Testes e Wiki

Cobrir abertura com 1, 2, 3 e 4 itens diferentes, ranges de quantidade, duplicidade entre aberturas, rollback, retry, baú negociado, ausência de expiração, missão concluída e missão falha. Atualizar a Wiki com as regras gerais, sem revelar a pool dentro do jogo.

## 16. Critérios de aceite da primeira sprint

A sprint de Missões estará aprovada quando:

1. Uma missão concluída entregar o baú correto da área e manter XP/Bits.
2. Missões com a mesma loot table produzirem o mesmo conjunto de possibilidades.
3. Missões com loot tables diferentes puderem possuir recompensas raras exclusivas.
4. Uma abertura entregar entre 1 e 4 tipos diferentes, com quantidades dentro dos ranges configurados.
5. Nenhuma abertura mostrar a pool antes do resultado.
6. Baús puderem ser negociados e não expirarem.
7. Repetições de itens forem permitidas sem duplicar a própria operação.
8. Falha ou retry não consumir o baú duas vezes nem conceder itens duplicados.
9. O resultado for registrado na auditoria positiva com origem, raridade, itens e quantidades.
10. O painel admin conseguir editar uma loot table sem alteração de código, se a primeira versão administrativa for incluída no escopo.

## 17. Decisões ainda implícitas para a implementação

A decisão de “1 a 4 itens diferentes” exige escolher como o número de itens será definido. A recomendação inicial é que cada loot table tenha pesos configuráveis para `item_count = 1`, `2`, `3` e `4`, em vez de sempre usar uma distribuição uniforme.

Também será necessário definir se a raridade sorteada se aplica ao baú inteiro ou a cada item. A interpretação adotada neste planejamento é que a raridade é sorteada uma vez por abertura e todos os 1 a 4 itens vêm da pool daquela raridade. Se você preferir que cada item tenha sua própria raridade, o modelo será mais generoso e mais complexo.

Por fim, para o Boss Mundial, a regra “um baú por tentativa” será implementada como uma concessão idempotente por `player_id + boss_id + cycle_id + attempt_number`; o baú de maior dano e o baú de derrota terão chaves separadas. Isso permite que as recompensas sejam repetíveis por tentativa, mas não duplicadas pela mesma tentativa.

## 18. Próximo passo

O próximo passo recomendado é aprovar este modelo e confirmar a interpretação da raridade por abertura. Depois disso, a implementação deve começar pelo PR do modelo de loot tables, sem alterar ainda as recompensas dos quatro modos em produção de desenvolvimento até que a abertura seja validada isoladamente.
