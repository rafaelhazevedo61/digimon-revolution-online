# Torre Infinita — protótipo

Disponível em **Mais → Combate → Torre Infinita**. A torre reutiliza as formações de **Meus Times** (1 a 3 Digimons), sem exigir um time exclusivo.

## Progressão

- Progresso permanente por conta, começando no andar 1; sem temporada ou reset.
- Vitória avança exatamente um andar. Derrota não remove andares e não concede recompensa.
- Sem gasto de energia ou limite diário; intervalo obrigatório de **30 segundos** após cada combate, inclusive derrotas, para todas as contas.
- É possível trocar de formação entre tentativas. Cada combate começa com HP completo; não altera HP ou energia fora da torre.
- Membros devem pertencer à conta e estar ACTIVE, HATCHED ou STORED. Bots e Digimons renascidos/sacrificados/consumidos não participam.
- Membros vinculados a missões RUNNING ou COMPLETED precisam concluir e resgatar essas missões antes do desafio.
- A caixa do último resultado possui detalhes expansíveis com nome, HP restante/máximo, barra de vida e estado de cada membro na ordem da formação. É um snapshot persistente do fim do combate; não modifica o HP real dos Digimons. Derrota por limite de rodadas pode deixar sobreviventes. Resultados anteriores à V254 continuam disponíveis sem detalhes retroativos.
- O combate é automático e determinístico. O time ataca primeiro; os membros vivos atacam o mesmo guardião, que alterna seus ataques entre os membros vivos em ordem de formação. Limite de 60 rodadas; se não vencer nesse prazo, conta como derrota.
- Equipamentos, refinamento, conjuntos e bônus de clã usam os mesmos atributos efetivos calculados pela arena. Não há vantagens elementais, atributos de personalidade ou habilidades adicionais nesta versão.

## Guardiões e dificuldade

O andar define o encontro, independentemente do jogador. São quatro perfis alternados: equilibrado, ofensivo (+40% ATK, -15% HP), defensivo (+80% DEF, -15% ATK) e resistente (+60% HP, -15% ATK).

Para o andar `n`, o fator é `(1 + (n - 1) / 10)²`, aplicado a HP 40, ATK 8 e DEF 4. Chefes (múltiplos de 5) multiplicam os atributos por 1,4; marcos (múltiplos de 10) por 1,8, substituindo o multiplicador de chefe. Arredondamento para cima. Chefes podem formar picos de dificuldade.

Os nomes dos guardiões rotacionam a cada quatro andares: Baby II nos andares 1–20, Rookie 21–40, Champion 41–60, Ultimate 61–80 e Mega a partir de 81. São encontros próprios da torre; seus atributos não alteram o catálogo de Digimons.

Dano por ataque: `ceil(max(ATK - DEF_alvo × 0,5; ATK × 0,1; 1))`. Sem sorteio de vitória. A ordem dos membros pode influenciar a sobrevivência.

Atributos usam BigDecimal para não transbordar em andares altos. Não há último andar de conteúdo; IDs de andar/tentativa usam BIGINT (limite técnico próximo de 9,22 × 10¹⁸, tratado com erro de negócio).

## Recompensas automáticas

| Tipo | Bits | Item |
| --- | --- | --- |
| Comum | `100 + 25 × min(196, floor(andar / 10))`, teto 5.000 | 1 Célula de Energia |
| Chefe (5, 15, 25…) | Duas vezes os Bits comuns, teto 10.000 | 1 Carga de Energia Total |
| Marco (10, 20, 30…) | Duas vezes os Bits comuns, teto 10.000 | 1 Núcleo de Ascensão |

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
