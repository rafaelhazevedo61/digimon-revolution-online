# Torre Infinita — protótipo

Disponível em **Mais → Combate → Torre Infinita**. A torre reutiliza as formações de **Meus Times** (1 a 3 Digimons), sem exigir um time exclusivo.

## Progressão

- Progresso permanente por conta, começando no andar 1; sem temporada ou reset.
- Vitória avança exatamente um andar. Derrota não remove andares e não concede recompensa.
- Sem gasto de energia ou limite diário; intervalo padrão de **30 segundos** após cada combate, inclusive derrotas, para todas as contas. No perfil local, o cooldown está desligado por padrão pela flag descrita ao final.
- É possível trocar de formação entre tentativas. Cada combate começa com HP completo; não altera HP ou energia fora da torre.
- Membros devem pertencer à conta e estar ACTIVE, HATCHED ou STORED. Bots e Digimons renascidos/sacrificados/consumidos não participam.
- Membros vinculados a missões RUNNING ou COMPLETED precisam concluir e resgatar essas missões antes do desafio.
- A caixa do último resultado possui detalhes expansíveis com nome, HP restante/máximo, barra de vida e estado de cada membro na ordem da formação. É um snapshot persistente do fim do combate; não modifica o HP real dos Digimons. Derrota por limite de rodadas pode deixar sobreviventes. Resultados anteriores à V254 continuam disponíveis sem detalhes retroativos.
- O combate é automático e determinístico. O time ataca primeiro; os membros vivos atacam o mesmo guardião, que alterna seus ataques entre os membros vivos em ordem de formação. Limite de 60 rodadas; se não vencer nesse prazo, conta como derrota.
- Equipamentos, refinamento, conjuntos e bônus de clã usam os mesmos atributos efetivos calculados pela arena. Não há vantagens elementais, atributos de personalidade ou habilidades adicionais nesta versão.

## Guardiões e dificuldade

O andar define o encontro, independentemente do jogador. São quatro perfis alternados: equilibrado, ofensivo (+40% ATK, -15% HP), defensivo (+80% DEF, -15% ATK) e resistente (+60% HP, -15% ATK).

Para o andar `n`, o fator é `(1 + (n - 1) / 10)²`, aplicado a HP 40, ATK 8 e DEF 4. Chefes (múltiplos de 5) multiplicam os atributos por 1,4; marcos (múltiplos de 10) por 1,8, substituindo o multiplicador de chefe. Arredondamento para cima. Chefes podem formar picos de dificuldade.

### Digimons usados nos desafios

| Andares | Estágio representado | Equilibrado | Ofensivo | Defensivo | Resistente |
| --- | --- | --- | --- | --- | --- |
| 1–20 | Baby II | Koromon | Tsunomon | Tokomon | Tanemon |
| 21–40 | Rookie | Agumon | Gabumon | Tentomon | Gomamon |
| 41–60 | Champion | Greymon | Garurumon | Kabuterimon | Ikkakumon |
| 61–80 | Ultimate | MetalGreymon | WereGarurumon | MegaKabuterimon | Zudomon |
| 81 em diante | Mega | WarGreymon | MetalGarurumon | HerculesKabuterimon | Vikemon |

A sequência de perfis é **Equilibrado → Ofensivo → Defensivo → Resistente**, repetida a cada quatro andares. O índice é `(andar - 1) % 4`. Cada faixa de 20 andares usa os quatro Digimons da linha correspondente; a partir do 81, os mesmos quatro Megas continuam alternando sem uma última faixa.

Por exemplo: 1 = Koromon, 2 = Tsunomon, 3 = Tokomon, 4 = Tanemon, 5 = Koromon (chefe). No andar 21, a sequência passa a Agumon, Gabumon, Tentomon e Gomamon.

**Chefes e marcos usam essa mesma seleção; ainda não existe um catálogo exclusivo de chefes.** O tipo do andar aumenta os atributos e altera a recompensa. Por exemplo, o andar 10 é um Tsunomon ofensivo com multiplicador de marco; o 20 é um Tanemon resistente com multiplicador de marco.

São encontros próprios da torre: os nomes representam os guardiões, mas os atributos vêm das fórmulas do andar, sem usar os atributos-base das espécies no catálogo ou alterar seus registros.

### Perfis e tipos de andar

| Perfil | HP | ATK | DEF |
| --- | --- | --- | --- |
| Equilibrado | ×1,00 | ×1,00 | ×1,00 |
| Ofensivo | ×0,85 | ×1,40 | ×1,00 |
| Defensivo | ×1,00 | ×0,85 | ×1,80 |
| Resistente | ×1,60 | ×0,85 | ×1,00 |

| Tipo | Quando aparece | Multiplicador adicional de HP, ATK e DEF |
| --- | --- | --- |
| Comum | Andar não múltiplo de 5 | ×1,00 |
| Chefe | Múltiplo de 5, exceto múltiplos de 10 | ×1,40 |
| Marco | Múltiplo de 10 | ×1,80 |

O cálculo aplica crescimento, perfil e tipo antes de arredondar cada atributo para cima. O marco substitui o multiplicador de chefe; não soma nem multiplica os dois.

### Exemplos dos primeiros desafios

| Andar | Guardião | Perfil | Tipo | HP | ATK | DEF |
| --- | --- | --- | --- | ---: | ---: | ---: |
| 1 | Koromon | Equilibrado | Comum | 40 | 8 | 4 |
| 2 | Tsunomon | Ofensivo | Comum | 42 | 14 | 5 |
| 3 | Tokomon | Defensivo | Comum | 58 | 10 | 11 |
| 4 | Tanemon | Resistente | Comum | 109 | 12 | 7 |
| 5 | Koromon | Equilibrado | Chefe | 110 | 22 | 11 |
| 10 | Tsunomon | Ofensivo | Marco | 221 | 73 | 26 |

Esses exemplos são iguais para todos os jogadores. Uma composição mais forte não faz o guardião aumentar seus atributos.

### Resolução do combate

Dano por ataque: `ceil(max(ATK - DEF_alvo × 0,5; ATK × 0,1; 1))`. Sem sorteio de vitória. A ordem dos membros pode influenciar a sobrevivência.

Atributos usam BigDecimal para não transbordar em andares altos. Não há último andar de conteúdo; IDs de andar/tentativa usam BIGINT (limite técnico próximo de 9,22 × 10¹⁸, tratado com erro de negócio).

## Recompensas automáticas

| Tipo | Bits | Item |
| --- | --- | --- |
| Comum | `100 + 25 × min(196, floor(andar / 10))`, teto 5.000 | 1 Célula de Energia |
| Chefe (5, 15, 25…) | Duas vezes os Bits comuns, teto 10.000 | 1 Carga de Energia Total |
| Marco (10, 20, 30…) | Duas vezes os Bits comuns, teto 10.000 | 1 Núcleo de Ascensão |

### Códigos de item e exemplos de recompensa

| Tipo | Código no inventário | Quantidade por vitória |
| --- | --- | ---: |
| Comum | `ENERGY_CELL` | 1 |
| Chefe | `VIP_ENERGY_CHARGE` | 1 |
| Marco | `ASCENSION_CORE` | 1 |

O código `VIP_ENERGY_CHARGE` corresponde ao item exibido na torre como **Carga de Energia Total**.

| Andar | Tipo | Bits | Item |
| --- | --- | ---: | --- |
| 1–4 | Comum | 100 por andar | 1 Célula de Energia por andar |
| 5 | Chefe | 200 | 1 Carga de Energia Total |
| 6–9 | Comum | 100 por andar | 1 Célula de Energia por andar |
| 10 | Marco | 250 | 1 Núcleo de Ascensão |
| 11 | Comum | 125 | 1 Célula de Energia |
| 15 | Chefe | 250 | 1 Carga de Energia Total |
| 20 | Marco | 300 | 1 Núcleo de Ascensão |
| 50 | Marco | 450 | 1 Núcleo de Ascensão |
| 100 | Marco | 700 | 1 Núcleo de Ascensão |
| 1.960 | Marco | 10.000 | 1 Núcleo de Ascensão |
| 1.961 | Comum | 5.000 | 1 Célula de Energia |

A base de Bits aumenta em 25 a cada dez andares, até atingir 5.000 no andar 1.960. Chefes e marcos dobram a base, até 10.000. A quantidade e a seleção dos itens seguem o tipo do andar, sem escalada de quantidade. Não há XP, equipamentos, fragmentos, pontos de coleção ou sorteio de loot na recompensa desta versão.

Recebidas uma única vez por andar, na primeira vitória, diretamente na carteira e no inventário. Não é possível selecionar ou repetir andares concluídos. Itens não aumentam em quantidade com a altura da torre. Valores são iniciais para avaliação de balanceamento.

Se a pilha do item estiver cheia ou a carteira não comportar os Bits, toda a tentativa é revertida: sem avanço, recompensa ou cooldown consumido. Libere espaço/saldo e tente novamente.

## API e consistência

- `GET /players/me/tower`: progresso, tentativas, hora do servidor, cooldown, próximo encontro e último resultado; não cria progresso ou recalcula conquistas.
- `POST /players/me/tower/challenge`: `{ "teamId": "UUID", "requestId": "UUID", "expectedAttempts": 0 }`.
- A identidade vem do token; nunca do corpo. Times e membros são validados no servidor.
- O bloqueio pessimista da linha do jogador serializa tentativas da mesma conta, inclusive na primeira participação e entre instâncias. Time e Digimons também são bloqueados.
- Bits, item, progresso, cooldown e auditoria são gravados na mesma transação. A migration V253 cria uma linha por conta em `player_tower_progress`; a V254 acrescenta `player_tower_last_members`, com até três snapshots por conta, substituídos a cada tentativa aceita. Nomes e HP continuam disponíveis mesmo se o time mudar ou o Digimon for removido. Progresso e detalhes são lidos em uma única consulta, sem misturar tentativas concorrentes.
- Repetir o último `requestId` devolve o estado salvo, sem novo combate ou recompensa. Pedidos antigos são recusados pela versão `expectedAttempts`, mesmo após o cooldown. O cliente reutiliza o pedido após falha de rede e reconcilia com GET.
- Apenas o último resultado é mantido na tabela de progresso (crescimento proporcional às contas). Auditoria dos combates usa o outbox existente.
- Expurgo de missões não altera o progresso da torre. Esta entrega não adiciona novas conquistas.

## Revisão e testes

Prévia visual sem conta: `docs/prototypes/tower.html`, servida por HTTP a partir da raiz do repositório. Os combates dessa página são demonstrativos, sem acesso à API.

Testes Java: `TowerRulesTest`, `TowerServiceTest`, `DigimonPowerServiceTest`; regressão de arena: `ChallengeArenaUseCaseTest` e `ArenaRulesTest`.

Testes de DOM e SQL real da migration/upsert: `scripts/tower.test.cjs`. Instale `jsdom` e `@electric-sql/pglite` fora do repositório e execute conforme o cabeçalho do script.

Os testes de SQL usam PostgreSQL embarcado (PGlite). Não substituem homologação com PostgreSQL do servidor, várias instâncias e contas reais. Rever curva de dificuldade e economia após jogar o protótipo.

## Testes locais sem cooldown

A flag `dro.gameplay.tower-cooldown-enabled` tem padrão **true** em `GameplayConfig`. No perfil `local`, `application-local.yml` define `tower-cooldown-enabled: ${DRO_TOWER_COOLDOWN_ENABLED:false}`, desativando o intervalo por padrão. Reinicie o backend com o perfil local ativo para aplicar.

Para reativar os 30 segundos localmente, defina `DRO_TOWER_COOLDOWN_ENABLED=true`. Quando desativado, timestamps futuros de combates anteriores são ignorados e a API informa disponibilidade imediata à interface. A flag é independente do cooldown dos outros chefes. Pedidos repetidos e versões antigas continuam protegidos.
