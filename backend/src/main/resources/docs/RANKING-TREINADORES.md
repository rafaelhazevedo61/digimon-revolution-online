# Ranking de Treinadores — protótipo

## Objetivo e acesso

A aba **Jogadores**, na página **Classificação**, ordena contas pelo progresso em coleção, conquistas e Torre Infinita. É uma classificação permanente, sem temporada, reset ou recompensa própria. As classificações existentes de Poder, Rebirth, Arena e Clãs continuam disponíveis.

Link interno: `#ranking?tab=players`. A classificação é da conta, independentemente do Digimon ativo.

## Pontuação

| Componente | Regra |
| --- | --- |
| Registros da coleção | 10 pontos por combinação distinta de espécie + raridade registrada. |
| Maestria da coleção | 50 pontos adicionais por espécie com as quatro raridades registradas. |
| Conquistas | 25, 50 ou 100 pontos por conquista ativa concluída, conforme o catálogo ao final. Não exige resgatar a recompensa. |
| Torre Infinita | 20 pontos por andar do maior progresso vencido da conta. Tentativas e derrotas não pontuam. |

`Coleção = registros × 10 + espécies com maestria × 50`

`Total = Coleção + Conquistas + maior andar vencido × 20`

Exemplo: 19 registros, 2 maestrias, duas conquistas de 25, uma de 50, uma de 100 e andar 15 vencido: coleção = 290; conquistas = 200; torre = 300; **total = 790 pontos**.

Os valores são iniciais para balanceamento. Não há parcela direta de poder, renascimentos ou rating de arena. As conquistas relacionadas a esses sistemas contribuem normalmente quando concluídas. Os pontos de treinador são uma medida de classificação, não moeda, Bits ou XP.

A pontuação é recalculada a partir de registros persistidos; não é incrementada a cada consulta. Repetir resgate, combate ou atualização não duplica pontos. As fontes existentes contam desde a primeira atualização do ranking: não é necessário repetir ações já registradas.

O ranking usa conquistas cujo `completed_at` está preenchido e cuja definição está ativa. A rotina própria das conquistas precisa processar uma meta nova antes de ela contribuir. Alterar pesos ou desativar conquistas pode alterar a pontuação no próximo processamento; não há garantia de pontuação monotônica após mudanças administrativas de regras.

## Participação e desempate

Participam contas com `user_type = PLAYER`, inclusive contas com zero pontos.

Ficam fora:
- Administradores.
- Contas associadas a algum Digimon com `is_bot = true`.
- Contas marcadas explicitamente como teste/excluídas por `players.trainer_ranking_excluded = true`.

Não se infere conta de teste pelo nome ou e-mail. A marcação é administrativa no banco neste protótipo, sem tela nova:

```sql
UPDATE players
SET trainer_ranking_excluded = TRUE
WHERE id = '<UUID_DA_CONTA>'::uuid;
```

Para permitir novamente, use `FALSE`. Mudanças de elegibilidade e nome aparecem na lista após a próxima atualização. A situação pessoal verifica a elegibilidade atual e pode informar exclusão antes de a lista periódica ser renovada.

Ordem de classificação:
1. Maior pontuação total.
2. Maior pontuação de torre.
3. Maior pontuação de conquistas.
4. Maior pontuação de coleção.
5. Nome do jogador em ordem crescente, sem distinguir maiúsculas/minúsculas.
6. UUID em ordem crescente, para um resultado estável.

As posições são únicas (1, 2, 3…), sem empate compartilhado. Idade da conta e horário do último login não influenciam.

## Atualização agendada

O scheduler está ligado por padrão, com intervalo de **5 minutos** entre execuções e primeira tentativa **10 segundos após a inicialização do agendamento**. Não há cálculo disparado por GET.

| Propriedade | Padrão | Finalidade |
| --- | --- | --- |
| `dro.ranking.trainers.enabled` | `true` | Habilitar o processamento. |
| `dro.ranking.trainers.delay-ms` | `300000` | Intervalo entre execuções, em milissegundos. Configure no mínimo 1000. |
| `dro.ranking.trainers.initial-delay-ms` | `10000` | Espera inicial, em milissegundos. |

Exemplo opcional em YAML:

```yaml
dro:
  ranking:
    trainers:
      enabled: true
      delay-ms: 300000
      initial-delay-ms: 10000
```

A leitura mostra a última atualização concluída. Contas novas podem aguardar o próximo ciclo. A pontuação de uma conquista recém-realizada pode demorar o tempo de processamento da conquista mais o intervalo do ranking.

O processamento usa a linha única de `trainer_ranking_state` como lock compartilhado entre instâncias, com `FOR UPDATE SKIP LOCKED` e verificação de intervalo. Somente uma instância renova a classificação por vez. Não depende exclusivamente de um lock em memória.

A substituição das linhas e a atualização dos metadados são uma única transação, com timeout de 120 segundos. Em falha, a versão anterior permanece disponível e o job registra o erro; uma nova tentativa ocorre no próximo ciclo. Leituras em andamento continuam vendo sua versão consistente. Não é usado `TRUNCATE`.

## Interface e API

A aba mostra:
- Nome, posição e pontuação total dos jogadores da página.
- Separação dos pontos de coleção, conquistas e torre.
- Detalhes expansíveis dos registros, maestrias, conquistas e andar alcançado.
- Sua posição em destaque, mesmo quando está fora da página exibida.
- Horário da última atualização, quantidade de participantes e paginação.

`GET /ranking/players?page=0&size=10`, autenticado pelo token.

- Página começa em zero; tamanho permitido de 1 a 50.
- Resposta: `entries`, `me`, `myStatus`, `updatedAt`, `totalPlayers`, `page`, `size`, `hasNext`.
- `myStatus`: `RANKED` (posição disponível), `PENDING` (conta elegível aguardando atualização), `EXCLUDED` (não participa).
- Antes do primeiro ciclo: lista vazia, `updatedAt = null` e aviso de processamento pendente.
- Não retorna e-mail, IDs dos Digimons, tokens ou outros dados privados; o identificador público da conta permite destacar o próprio jogador.
- Não há busca por nome nesta aba no protótipo; a busca existente de Poder permanece.
- A leitura da página, do próprio jogador e dos metadados usa transação `REPEATABLE_READ` para não misturar versões durante uma renovação.

## Persistência e operação

Migration **V255__trainer_ranking.sql**:
- `achievement_definitions.trainer_points`: peso independente das recompensas, com valores aceitos 25/50/100 e padrão 25 para novas definições.
- `players.trainer_ranking_excluded`: exclusão explícita de contas de teste.
- `trainer_ranking_entries`: uma linha por participante, posição e parcelas da pontuação.
- `trainer_ranking_state`: data da atualização e total de participantes.
- Índice parcial para identificar jogadores associados a bots.

O cálculo agrupa coleção e conquistas em SQL, evitando consultar fontes uma vez por jogador. A lista usa posição indexada e a posição pessoal usa a chave da conta. Scores usam `NUMERIC(30,0)`, evitando overflow de BIGINT ao multiplicar o andar da torre. Contagens e posições usam BIGINT; números extremos podem ultrapassar a precisão de exibição do JavaScript, fora do alcance prático do protótipo.

Não consulta históricos de missão: o expurgo de missões não apaga a classificação de coleção/torre ou conquistas já persistidas. Exclusão de conta remove sua linha por FK; totais e posições são normalizados no próximo processamento.

## Validação e evolução

Testes adicionados:
- `TrainerRankingServiceTest`: posição pessoal, elegibilidade, pendência inicial, paginação e execução do job.
- `scripts/trainer-ranking.test.cjs`: migration e SQL real em PGlite, composição dos pontos, exclusão de bots/admin/teste, desempate, idempotência, resgate sem pontos extras, rollback e DOM da aba.

O ambiente de execução estava indisponível na elaboração desta entrega: os testes foram escritos, mas sua execução e a compilação Java precisam ser realizadas na homologação. Verificações de sintaxe JavaScript e renderização direta foram feitas separadamente. PGlite não substitui testes de concorrência com PostgreSQL e múltiplas instâncias reais.

Próximos ajustes possíveis: homologar pesos com jogadores reais, medir duração do recálculo com muitas contas, avaliar atualização incremental por contas alteradas, incluir gestão de exclusão no admin e ranking sazonal separado. Esta entrega não concede prêmios por posição.

## Catálogo inicial de pontuação das conquistas

Classificação inicial: **básica = 25**, **intermediária = 50**, **avançada = 100**. A dificuldade é uma decisão de balanceamento, não derivada automaticamente dos Bits da recompensa ou do valor numérico da meta.

| Código | Conquista | Pontos |
| --- | --- | ---: |
| `FIRST_HATCH` | Primeiro companheiro | 25 |
| `FIRST_MISSION` | Primeira expedição | 25 |
| `FIRST_EQUIP` | Pronto para a aventura | 25 |
| `HATCH_10` | Berçário digital I | 25 |
| `HATCH_50` | Berçário digital II | 50 |
| `HATCH_100` | Guardião do berçário | 100 |
| `STAGE_BABY_II` | Os primeiros passos | 25 |
| `STAGE_ROOKIE` | Um parceiro de verdade | 25 |
| `STAGE_CHAMPION` | Força de Champion | 50 |
| `STAGE_ULTIMATE` | Além dos limites | 50 |
| `STAGE_MEGA` | O despertar Mega | 100 |
| `REBIRTH_1` | Um novo começo | 25 |
| `REBIRTH_10` | Dez novos destinos | 50 |
| `REBIRTH_25` | O ciclo continua | 100 |
| `SPECIES_10` | Espaço para novos amigos | 25 |
| `SPECIES_25` | Olhar de colecionador | 50 |
| `SPECIES_50` | Uma família digital | 50 |
| `SPECIES_100` | Enciclopédia viva | 100 |
| `MISSION_10` | Explorador I | 25 |
| `MISSION_100` | Explorador II | 50 |
| `MISSION_500` | Explorador III | 100 |
| `MISSION_1000` | Lenda das expedições | 100 |
| `ARENA_1` | Primeira vitória | 25 |
| `ARENA_25` | Competidor | 50 |
| `ARENA_100` | Nome de respeito | 50 |
| `WORLD_1` | Na linha de frente | 25 |
| `WORLD_50` | Presença constante | 50 |
| `WORLD_200` | Defensor do Mundo Digital | 100 |
| `RAID_10` | Força em conjunto | 25 |
| `RAID_50` | Companheiro de batalha | 50 |
| `REFINE_5` | Arsenal aprimorado | 50 |
| `REFINE_10` | Obra de mestre | 100 |
| `ASCEND_1` | Um novo patamar | 50 |
| `HATCH_5` | Berçário em expansão — 5 | 25 |
| `HATCH_25` | Berçário em expansão — 25 | 50 |
| `HATCH_250` | Berçário em expansão — 250 | 100 |
| `HATCH_500` | Berçário em expansão — 500 | 100 |
| `MISSION_25` | Expedições sem fronteiras — 25 | 25 |
| `MISSION_50` | Expedições sem fronteiras — 50 | 25 |
| `MISSION_250` | Expedições sem fronteiras — 250 | 50 |
| `MISSION_2500` | Expedições sem fronteiras — 2500 | 100 |
| `MISSION_5000` | Expedições sem fronteiras — 5000 | 100 |
| `REBIRTH_5` | Novos ciclos — 5 | 50 |
| `REBIRTH_50` | Novos ciclos — 50 | 100 |
| `REBIRTH_100` | Novos ciclos — 100 | 100 |
| `SPECIES_5` | Descobertas digitais — 5 | 25 |
| `SPECIES_75` | Descobertas digitais — 75 | 50 |
| `SPECIES_150` | Descobertas digitais — 150 | 100 |
| `ARENA_10` | Vitórias na arena — 10 | 25 |
| `ARENA_50` | Vitórias na arena — 50 | 50 |
| `ARENA_250` | Vitórias na arena — 250 | 100 |
| `ARENA_500` | Vitórias na arena — 500 | 100 |
| `WORLD_10` | Vanguarda mundial — 10 | 25 |
| `WORLD_100` | Vanguarda mundial — 100 | 50 |
| `WORLD_500` | Vanguarda mundial — 500 | 100 |
| `RAID_1` | Pelo clã — 1 | 25 |
| `RAID_25` | Pelo clã — 25 | 50 |
| `RAID_100` | Pelo clã — 100 | 100 |
| `RAID_250` | Pelo clã — 250 | 100 |
| `BIRTH_RARE` | Um brilho diferente | 25 |
| `BIRTH_EPIC` | Nascimento extraordinário | 50 |
| `BIRTH_LEGENDARY` | Uma lenda nasceu | 100 |
| `BIRTH_ELITE_10` | Berçário de elite | 100 |
| `BIRTH_PERFECT_IV` | Potencial perfeito | 50 |
| `BIRTH_ALL_PERFECT_IV` | Perfeição digital | 100 |
| `BIRTH_TRAIT_1` | Personalidade marcante | 25 |
| `BIRTH_TRAIT_10` | Novos talentos | 50 |
| `MASTERY_1` | Muitas faces | 50 |
| `MASTERY_5` | Conhecimento aprofundado | 100 |
| `MASTERY_10` | Especialista em espécies | 100 |
| `LINES_1` | Do início ao fim | 50 |
| `LINES_5` | Caminhos conhecidos | 100 |
| `LINES_10` | Arquivista digital | 100 |
| `COLLECTION_BABY_10` | Pequenos companheiros | 25 |
| `COLLECTION_ROOKIE_10` | Novos aventureiros | 50 |
| `COLLECTION_CHAMPION_10` | Grandes guerreiros | 50 |
| `COLLECTION_ULTIMATE_10` | Poder extraordinário | 100 |
| `COLLECTION_MEGA_10` | Encontro de lendas | 100 |
| `EXPLORE_NATIVE_FOREST` | Conhecedor de Floresta Nativa | 25 |
| `EXPLORE_GEAR_SAVANNA` | Conhecedor de Savana Gear | 50 |
| `EXPLORE_FACTORIAL_TOWN` | Conhecedor de Cidade Fatorial | 50 |
| `EXPLORE_FREEZELAND` | Conhecedor de Freezeland | 100 |
| `EXPLORE_SERVER_DESERT` | Conhecedor de Deserto do Server | 100 |
| `EXPLORE_INFINITY_MOUNTAIN` | Conhecedor de Montanha Infinita | 100 |
| `TEAM_1` | Melhor acompanhado | 25 |
| `TEAM_100` | Trabalho em equipe | 50 |
| `TEAM_500` | Esquadrão veterano | 100 |
| `FULL_EQUIPPED` | Pronto para o combate | 25 |
| `SET_1` | Sinergia de conjunto | 50 |
| `SET_3` | Arsenal diversificado | 100 |
| `REFINE_SUCCESS_1` | Primeiro aprimoramento | 25 |
| `REFINE_SUCCESS_50` | Ferreiro dedicado | 100 |
| `ASCENSION_DONE_1` | Transformação de equipamento | 50 |
| `ASCENSION_DONE_10` | Artesão experiente | 100 |
| `BOSS_WIN_1` | Primeiro grande adversário | 25 |
| `BOSS_WIN_10` | Caçador de chefes | 50 |
| `BOSS_WIN_100` | Caçador veterano | 100 |
| `BOSS_DISTINCT_5` | Adversários conhecidos | 50 |
| `WORLD_PARTICIPATION` | Unidos contra o impossível | 50 |
| `RAID_PARTICIPATION` | Vitória do clã | 50 |

## Referências

- [Conquistas](CONQUISTAS.md).
- [Torre Infinita](TORRE-INFINITA.md).
- [Processamento agendado de conquistas](PROCESSAMENTO-CONQUISTAS.md).
