# Conquistas — catálogo completo

O catálogo contém **101 conquistas permanentes por conta**, em **13 categorias**. Acesso: **Mais → Conquistas**. Cada recompensa pode ser resgatada uma única vez por conta, independentemente do Digimon ativo.

Fontes: [V250 — catálogo inicial](../db/migration/V250__achievement_prototype_and_vip_pass.sql) e [V251 — expansão para 100](../db/migration/V251__expand_achievements_to_100.sql). A expansão preserva as 33 definições originais e seus resgates. A [V252](../db/migration/V252__scheduled_achievement_processing.sql) acrescenta o processamento agendado, sem mudar as 100 metas ou recompensas originais. A V259 acrescenta a conquista de leitura do Guia do Treinador.

As metas acumulam o progresso entre etapas. Os Bits são creditados à conta e os itens ficam no inventário até o uso. O progresso é processado em segundo plano, com intervalo padrão de 30 segundos entre lotes de até 100 contas pendentes. Abrir/atualizar a página consulta o resultado já processado; resgatar não força um recálculo.

## Primeiros passos

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `FIRST_HATCH` | Primeiro companheiro | Choque seu primeiro Digitama. | 100 | 1 × Célula de Energia |
| `FIRST_MISSION` | Primeira expedição | Resgate sua primeira missão. | 100 | 1 × Célula de Energia |
| `FIRST_EQUIP` | Pronto para a aventura | Equipe uma peça em seu Digimon. | 150 | 2 × Disco de XP +5% |
| `TRAINER_GUIDE_COMPLETE` | Treinador bem informado | Marque Entendi nos 12 conteúdos do Guia do Treinador. | 500 | 1 × Carga de Energia Total |

## Criação

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `HATCH_10` | Berçário digital I | Choque 10 Digitamas. | 500 | 1 × Incubadora rara |
| `HATCH_50` | Berçário digital II | Choque 50 Digitamas. | 2.000 | 1 × Incubadora épica |
| `HATCH_100` | Guardião do berçário | Choque 100 Digitamas. | 5.000 | 1 × Dado de Raridade |
| `HATCH_5` | Berçário em expansão — 5 | Choque 5 Digitamas. | 75 | 2 × Disco de XP +10% |
| `HATCH_25` | Berçário em expansão — 25 | Choque 25 Digitamas. | 375 | 2 × Disco de XP +10% |
| `HATCH_250` | Berçário em expansão — 250 | Choque 250 Digitamas. | 3.750 | 2 × Disco de XP +10% |
| `HATCH_500` | Berçário em expansão — 500 | Choque 500 Digitamas. | 7.500 | 1 × Dado de Raridade |

## Raridade e talentos ao nascer

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `BIRTH_RARE` | Um brilho diferente | Choque seu primeiro Digimon Raro. | 300 | 2 × Célula de Energia |
| `BIRTH_EPIC` | Nascimento extraordinário | Choque seu primeiro Digimon Épico. | 750 | 2 × Disco de XP +10% |
| `BIRTH_LEGENDARY` | Uma lenda nasceu | Choque seu primeiro Digimon Lendário. | 2.500 | 1 × Cristal de Preservação de Raridade |
| `BIRTH_ELITE_10` | Berçário de elite | Choque 10 Digimons Épicos ou Lendários. | 3.000 | 3 × Carga de Energia Total |
| `BIRTH_PERFECT_IV` | Potencial perfeito | Choque um Digimon com pelo menos um IV 100. | 1.000 | 2 × Disco de XP +20% |
| `BIRTH_ALL_PERFECT_IV` | Perfeição digital | Choque um Digimon com os três IVs 100. | 5.000 | 1 × Dado de Raridade |
| `BIRTH_TRAIT_1` | Personalidade marcante | Choque seu primeiro Digimon com trait. | 500 | 2 × Célula de Energia |
| `BIRTH_TRAIT_10` | Novos talentos | Choque 10 Digimons com trait. | 2.000 | 2 × Carga de Energia Total |

## Evolução

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `STAGE_BABY_II` | Os primeiros passos | Alcance Baby II ou um estágio superior. | 150 | 2 × Disco de XP +5% |
| `STAGE_ROOKIE` | Um parceiro de verdade | Alcance Rookie ou um estágio superior. | 300 | 2 × Célula de Energia |
| `STAGE_CHAMPION` | Força de Champion | Alcance Champion ou um estágio superior. | 750 | 3 × Disco de XP +10% |
| `STAGE_ULTIMATE` | Além dos limites | Alcance Ultimate ou um estágio superior. | 1.500 | 2 × Carga de Energia Total |
| `STAGE_MEGA` | O despertar Mega | Alcance o estágio Mega. | 3.000 | 1 × Passe VIP de 1 Dia |

## Renascimento

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `REBIRTH_1` | Um novo começo | Realize seu primeiro renascimento. | 750 | 1 × Cristal de Preservação de Raridade |
| `REBIRTH_10` | Dez novos destinos | Realize 10 renascimentos na conta. | 3.500 | 1 × Dado de Raridade |
| `REBIRTH_25` | O ciclo continua | Realize 25 renascimentos na conta. | 7.500 | 2 × Cristal de Preservação de Raridade |
| `REBIRTH_5` | Novos ciclos — 5 | Realize 5 renascimentos na conta. | 500 | 2 × Disco de XP +20% |
| `REBIRTH_50` | Novos ciclos — 50 | Realize 50 renascimentos na conta. | 5.000 | 2 × Disco de XP +20% |
| `REBIRTH_100` | Novos ciclos — 100 | Realize 100 renascimentos na conta. | 10.000 | 1 × Cristal de Preservação de Raridade |

## Coleção

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `SPECIES_10` | Espaço para novos amigos | Registre 10 espécies diferentes na coleção. | 500 | 1 × Expansor de Storage +5 |
| `SPECIES_25` | Olhar de colecionador | Registre 25 espécies diferentes na coleção. | 1.500 | 1 × Dado de Raridade |
| `SPECIES_50` | Uma família digital | Registre 50 espécies diferentes na coleção. | 3.000 | 1 × Expansor de Storage +5 |
| `SPECIES_100` | Enciclopédia viva | Registre 100 espécies diferentes na coleção. | 6.000 | 2 × Expansor de Storage +5 |
| `SPECIES_5` | Descobertas digitais — 5 | Registre 5 espécies diferentes. | 100 | 2 × Disco de XP +10% |
| `SPECIES_75` | Descobertas digitais — 75 | Registre 75 espécies diferentes. | 1.500 | 2 × Disco de XP +10% |
| `SPECIES_150` | Descobertas digitais — 150 | Registre 150 espécies diferentes. | 3.000 | 1 × Dado de Raridade |
| `MASTERY_1` | Muitas faces | Registre as quatro raridades de 1 espécie(s). | 500 | 2 × Disco de XP +20% |
| `MASTERY_5` | Conhecimento aprofundado | Registre as quatro raridades de 5 espécie(s). | 2.500 | 2 × Disco de XP +20% |
| `MASTERY_10` | Especialista em espécies | Registre as quatro raridades de 10 espécie(s). | 5.000 | 2 × Disco de XP +20% |
| `LINES_1` | Do início ao fim | Registre todas as espécies de 1 linha(s) evolutiva(s). | 750 | 1 × Carga de Energia Total |
| `LINES_5` | Caminhos conhecidos | Registre todas as espécies de 5 linha(s) evolutiva(s). | 3.750 | 3 × Carga de Energia Total |
| `LINES_10` | Arquivista digital | Registre todas as espécies de 10 linha(s) evolutiva(s). | 7.500 | 3 × Carga de Energia Total |
| `COLLECTION_BABY_10` | Pequenos companheiros | Registre 10 espécies diferentes do estágio BABY. | 1.500 | 2 × Disco de XP +20% |
| `COLLECTION_ROOKIE_10` | Novos aventureiros | Registre 10 espécies diferentes do estágio ROOKIE. | 1.500 | 2 × Disco de XP +20% |
| `COLLECTION_CHAMPION_10` | Grandes guerreiros | Registre 10 espécies diferentes do estágio CHAMPION. | 1.500 | 2 × Disco de XP +20% |
| `COLLECTION_ULTIMATE_10` | Poder extraordinário | Registre 10 espécies diferentes do estágio ULTIMATE. | 1.500 | 2 × Disco de XP +20% |
| `COLLECTION_MEGA_10` | Encontro de lendas | Registre 10 espécies diferentes do estágio MEGA. | 1.500 | 2 × Disco de XP +20% |

## Missões

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `MISSION_10` | Explorador I | Resgate 10 missões, individuais ou em equipe. | 300 | 2 × Célula de Energia |
| `MISSION_100` | Explorador II | Resgate 100 missões, incluindo resgates automáticos. | 1.500 | 2 × Carga de Energia Total |
| `MISSION_500` | Explorador III | Resgate 500 missões. | 5.000 | 1 × Passe VIP de 1 Dia |
| `MISSION_1000` | Lenda das expedições | Resgate 1.000 missões. | 10.000 | 1 × Incubadora lendária |
| `MISSION_25` | Expedições sem fronteiras — 25 | Resgate 25 missões. | 125 | 2 × Célula de Energia |
| `MISSION_50` | Expedições sem fronteiras — 50 | Resgate 50 missões. | 250 | 2 × Célula de Energia |
| `MISSION_250` | Expedições sem fronteiras — 250 | Resgate 250 missões. | 1.250 | 2 × Carga de Energia Total |
| `MISSION_2500` | Expedições sem fronteiras — 2500 | Resgate 2500 missões. | 12.500 | 4 × Carga de Energia Total |
| `MISSION_5000` | Expedições sem fronteiras — 5000 | Resgate 5000 missões. | 25.000 | 4 × Carga de Energia Total |

## Exploração

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `EXPLORE_NATIVE_FOREST` | Conhecedor de Floresta Nativa | Resgate ao menos uma vez todas as missões de Floresta Nativa incluídas nesta conquista. | 2.000 | 2 × Carga de Energia Total |
| `EXPLORE_GEAR_SAVANNA` | Conhecedor de Savana Gear | Resgate ao menos uma vez todas as missões de Savana Gear incluídas nesta conquista. | 2.000 | 2 × Carga de Energia Total |
| `EXPLORE_FACTORIAL_TOWN` | Conhecedor de Cidade Fatorial | Resgate ao menos uma vez todas as missões de Cidade Fatorial incluídas nesta conquista. | 2.000 | 2 × Carga de Energia Total |
| `EXPLORE_FREEZELAND` | Conhecedor de Freezeland | Resgate ao menos uma vez todas as missões de Freezeland incluídas nesta conquista. | 2.000 | 2 × Carga de Energia Total |
| `EXPLORE_SERVER_DESERT` | Conhecedor de Deserto do Server | Resgate ao menos uma vez todas as missões de Deserto do Server incluídas nesta conquista. | 2.000 | 2 × Carga de Energia Total |
| `EXPLORE_INFINITY_MOUNTAIN` | Conhecedor de Montanha Infinita | Resgate ao menos uma vez todas as missões de Montanha Infinita incluídas nesta conquista. | 2.000 | 2 × Carga de Energia Total |

## Equipes

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `TEAM_1` | Melhor acompanhado | Resgate 1 missão(ões) com time completo de três Digimons. | 200 | 1 × Célula de Energia |
| `TEAM_100` | Trabalho em equipe | Resgate 100 missão(ões) com time completo de três Digimons. | 800 | 2 × Carga de Energia Total |
| `TEAM_500` | Esquadrão veterano | Resgate 500 missão(ões) com time completo de três Digimons. | 4.000 | 2 × Carga de Energia Total |

## Combate

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `ARENA_1` | Primeira vitória | Vença um desafio iniciado por você na arena. | 250 | 1 × Célula de Energia |
| `ARENA_25` | Competidor | Vença 25 desafios iniciados por você na arena. | 1.500 | 3 × Disco de XP +20% |
| `ARENA_100` | Nome de respeito | Vença 100 desafios iniciados por você na arena. | 5.000 | 1 × Dado de Raridade |
| `WORLD_1` | Na linha de frente | Realize um ataque válido ao Chefe Mundial. | 250 | 1 × Célula de Energia |
| `WORLD_50` | Presença constante | Realize 50 ataques ao Chefe Mundial. | 2.500 | 1 × Núcleo de Ascensão |
| `WORLD_200` | Defensor do Mundo Digital | Realize 200 ataques ao Chefe Mundial. | 7.500 | 5 × Carga de Energia Total |
| `ARENA_10` | Vitórias na arena — 10 | Vença 10 desafios iniciados por você na arena. | 150 | 2 × Célula de Energia |
| `ARENA_50` | Vitórias na arena — 50 | Vença 50 desafios iniciados por você na arena. | 750 | 2 × Célula de Energia |
| `ARENA_250` | Vitórias na arena — 250 | Vença 250 desafios iniciados por você na arena. | 3.750 | 2 × Carga de Energia Total |
| `ARENA_500` | Vitórias na arena — 500 | Vença 500 desafios iniciados por você na arena. | 7.500 | 2 × Carga de Energia Total |
| `WORLD_10` | Vanguarda mundial — 10 | Realize 10 ataques válidos ao Chefe Mundial. | 100 | 2 × Célula de Energia |
| `WORLD_100` | Vanguarda mundial — 100 | Realize 100 ataques válidos ao Chefe Mundial. | 1.000 | 2 × Carga de Energia Total |
| `WORLD_500` | Vanguarda mundial — 500 | Realize 500 ataques válidos ao Chefe Mundial. | 5.000 | 2 × Carga de Energia Total |

## Chefes

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `BOSS_WIN_1` | Primeiro grande adversário | Vença 1 desafio(s) de chefes. | 250 | 1 × Célula de Energia |
| `BOSS_WIN_10` | Caçador de chefes | Vença 10 desafio(s) de chefes. | 500 | 2 × Carga de Energia Total |
| `BOSS_WIN_100` | Caçador veterano | Vença 100 desafio(s) de chefes. | 5.000 | 2 × Carga de Energia Total |
| `BOSS_DISTINCT_5` | Adversários conhecidos | Derrote cinco chefes diferentes. | 2.000 | 3 × Disco de XP +20% |
| `WORLD_PARTICIPATION` | Unidos contra o impossível | Ataque um Chefe Mundial em um ciclo que termine em derrota do chefe. | 1.500 | 2 × Carga de Energia Total |
| `RAID_PARTICIPATION` | Vitória do clã | Ataque um chefe de incursão em um ciclo que termine em derrota do chefe. | 1.500 | 2 × Carga de Energia Total |

## Clã

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `RAID_10` | Força em conjunto | Realize 10 ataques em incursões de clã. | 1.000 | 1 × Carga de Energia Total |
| `RAID_50` | Companheiro de batalha | Realize 50 ataques em incursões de clã. | 3.000 | 1 × Núcleo de Ascensão |
| `RAID_1` | Pelo clã — 1 | Realize 1 ataques em incursões de clã. | 100 | 1 × Célula de Energia |
| `RAID_25` | Pelo clã — 25 | Realize 25 ataques em incursões de clã. | 375 | 2 × Célula de Energia |
| `RAID_100` | Pelo clã — 100 | Realize 100 ataques em incursões de clã. | 1.500 | 2 × Carga de Energia Total |
| `RAID_250` | Pelo clã — 250 | Realize 250 ataques em incursões de clã. | 3.750 | 2 × Carga de Energia Total |

## Equipamentos

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `REFINE_5` | Arsenal aprimorado | Tenha um equipamento com refinamento +5 ou superior. | 1.000 | 2 × Cristal de Proteção |
| `REFINE_10` | Obra de mestre | Tenha um equipamento com refinamento +10 ou superior. | 3.000 | 1 × Núcleo de Ascensão |
| `ASCEND_1` | Um novo patamar | Tenha um equipamento com uma ascensão ou mais. | 1.500 | 3 × Cristal de Proteção |
| `FULL_EQUIPPED` | Pronto para o combate | Equipe arma, armadura e acessório no mesmo Digimon. | 500 | 1 × Cristal de Proteção |
| `SET_1` | Sinergia de conjunto | Ative um bônus de conjunto com três peças. | 1.000 | 2 × Cristal de Proteção |
| `SET_3` | Arsenal diversificado | Ative três conjuntos diferentes com três peças cada. | 3.000 | 1 × Núcleo de Ascensão |
| `REFINE_SUCCESS_1` | Primeiro aprimoramento | Realize 1 refinamento(s) bem-sucedido(s). | 250 | 1 × Célula de Energia |
| `REFINE_SUCCESS_50` | Ferreiro dedicado | Realize 50 refinamento(s) bem-sucedido(s). | 3.000 | 3 × Cristal de Proteção |
| `ASCENSION_DONE_1` | Transformação de equipamento | Realize 1 ascensão(ões) de equipamento. | 1.000 | 2 × Cristal de Proteção |
| `ASCENSION_DONE_10` | Artesão experiente | Realize 10 ascensão(ões) de equipamento. | 5.000 | 2 × Núcleo de Ascensão |

## Total de recompensas

O resgate das 101 conquistas concede **259.050 Bits** e:

| Item | Código | Quantidade total |
|---|---|---:|
| Célula de Energia | `ENERGY_CELL` | 28 |
| Disco de XP +5% | `XP_DISC_5` | 4 |
| Incubadora rara | `INCUBATOR_RARE` | 1 |
| Incubadora épica | `INCUBATOR_EPIC` | 1 |
| Dado de Raridade | `RARITY_REROLL` | 7 |
| Disco de XP +10% | `XP_DISC_10` | 15 |
| Carga de Energia Total | `VIP_ENERGY_CHARGE` | 69 |
| Passe VIP de 1 Dia | `VIP_DAY_PASS` | 2 |
| Cristal de Preservação de Raridade | `RARITY_PRESERVATION` | 5 |
| Expansor de Storage +5 | `STORAGE_SLOT_5` | 4 |
| Incubadora lendária | `INCUBATOR_LEGENDARY` | 1 |
| Disco de XP +20% | `XP_DISC_20` | 28 |
| Núcleo de Ascensão | `ASCENSION_CORE` | 6 |
| Cristal de Proteção | `REFINEMENT_PROTECTION` | 13 |

Os expansores mantêm o total de **+20 espaços de storage**, depois do uso. Os dois Passes VIP mantêm o total de **48 horas de VIP**, depois do uso. A expansão não acrescenta outros expansores ou Passes VIP.

## Contagem, histórico e expurgo

- **Nascimentos totais:** histórico de nascimentos diretos mais incubações resgatadas. Compras e renascimentos não contam como nascimento.
- **Raridade, IVs e traits ao nascer:** usam uma fotografia imutável capturada pelos fluxos de nascimento direto/incubação após a implantação da V251. Não são inferidos a partir de atributos atuais. Reroll posterior, evolução, compra e renascimento não criam essa fotografia.
- **Missões:** contam quando passam para `CLAIMED`, manual ou automaticamente. O contador permanente sobrevive ao expurgo.
- **Equipes:** contam somente missões resgatadas com três integrantes e time associado. Um time de dois não é completo. O contador permanece após excluir o time ou expurgar a missão. A instalação aproveita apenas as instâncias antigas ainda disponíveis.
- **Exploração:** mantém o conjunto de missões ativas de cada área na implantação da V251, excluindo a missão administrativa. Cada missão precisa ter sido resgatada ao menos uma vez. A instalação aproveita o progresso por missão e as instâncias resgatadas restantes. O registro de conclusões por missão não é expurgado. Missões adicionadas ao jogo no futuro não aumentam automaticamente essas metas.
- **Coleção:** conta espécies diferentes, sem multiplicar uma espécie pelas raridades. As quatro raridades de uma espécie precisam estar registradas no álbum para contar como domínio completo.
- **Linhas:** cada linha conta uma vez; todas as suas espécies precisam constar no álbum em alguma raridade. A V251 congela as espécies das linhas ativas com conteúdo disponível. Adicionar etapas futuras não retira conquistas anteriores. Novas linhas podem ser incluídas por uma migration posterior.
- **Estágio:** considera registros de Digimons e do álbum; um estágio superior também satisfaz metas anteriores.
- **Renascimentos:** conta registros com status `REBORN`, sem somar os contadores de todas as gerações da linhagem.
- **Arena:** conta vitórias de desafios iniciados pela conta; defesas passivas não contam.
- **Chefes individuais:** conta tentativas com `VICTORY`. Chefes distintos são identificados pelo ID da definição do chefe, não pelo nome nem pelo número de tentativas.
- **Participação coletiva:** exige pelo menos um ataque com dano positivo naquele mesmo ciclo e que a instância termine em `DEFEATED`. Não exige maior dano, vitória individual ou golpe final. Múltiplos ataques ao mesmo ciclo não multiplicam sua participação.
- **Possuir refinamento/ascensão:** a V252 preserva o maior patamar observado na gravação da peça. A venda ou perda posterior não apaga esse marco enquanto a conta aguarda processamento. Peças já vendidas antes da instalação e sem evidência não são reconstruídas.
- **Realizar refinamentos/ascensões:** novas ações passam a ser registradas pela V251. Refinamentos falhos não contam. Comprar uma peça pronta não conta. Não se tenta reconstruir essas ações a partir do nível atual do equipamento.
- **Equipar e ativar conjuntos:** o primeiro equipamento usa o registro existente do tutorial. As novas metas registram equipagens após a V251. O conjunto precisa realmente ativar o bônus de três peças; repetir o mesmo código de conjunto não conta como outro conjunto. A ativação permanece registrada após desequipar ou vender peças.

Uma conclusão reconhecida não perde o estado após troca de parceiro, renascimento ou redução de um histórico. Feitos anteriores sem evidências confiáveis não são inventados. Dados já apagados antes da implantação não podem ser reconstruídos integralmente.

## Recompensas especiais

- `VIP_DAY_PASS`: começa 24 horas de VIP no uso se estiver inativo, ou soma 24 horas ao vencimento ativo. Não é vendido/negociado e não resgata automaticamente a Carga Total diária.
- `STORAGE_SLOT_5`: aumenta o storage em +5 por unidade ao ser usado.
- `RARITY_PRESERVATION`: Cristal de Preservação de Raridade, usado no renascimento.
- `REFINEMENT_PROTECTION`: Cristal de Proteção, usado no refinamento. Não é o mesmo item de preservação de raridade.

## Manutenção

As definições ficam em `achievement_definitions`; progresso e resgates ficam em `player_achievements`. Alterações em metas ou recompensas devem atualizar esta lista. Use migrations novas para modificar catálogos já implantados, sem reescrever V250/V251.

## Atualização em segundo plano

A captura de ações e fotografias de nascimento continua imediata e transacional. Apenas contas com pendências são avaliadas pela rotina. Uma conta ocupa no máximo uma linha na fila, mesmo com várias atividades; a ordem considera a primeira atividade pendente. A instalação faz uma carga inicial única para reconhecer históricos anteriores.

A página exibe `lastUpdatedAt` (último processamento) e `updatePending` (existem atividades aguardando processamento). Antes do primeiro lote, o progresso aparece em zero com indicação de espera. Atualizar a página não antecipa a fila. Uma conquista só pode ser resgatada após sua conclusão ter sido persistida pelo processador.

Os 30 segundos são um intervalo de execução, não um prazo garantido: backlog, duração do lote e falhas podem aumentar a espera. Metas já concluídas não são reavaliadas. Veja [PROCESSAMENTO-CONQUISTAS.md](PROCESSAMENTO-CONQUISTAS.md) para configuração, concorrência e recuperação de falhas.


## Conquista do Guia do Treinador

`TRAINER_GUIDE_COMPLETE`, categoria Primeiros passos, métrica `GUIDE_READ_TOPICS`, meta 12. Conta os reconhecimentos Entendi dos tópicos COLLECTION, ACHIEVEMENTS, ENERGY, INCUBATION, TEAMS, BOSSES, ARENA, TOWER, RANKING, FORGE, REBIRTH e VIP. Abrir a página ou navegar para uma atividade não basta. Não é necessário usar/comprar VIP, renascer, vencer a torre ou executar as atividades descritas: trata-se da leitura das orientações liberadas pelos marcos iniciais.

As leituras já salvas antes da V259 contam. Cada leitura nova apenas enfileira a conta na rotina existente; não recalcula as conquistas no clique. A definição nova também enfileira as contas para avaliação retroativa. A conclusão e o resgate são os mesmos das outras conquistas: recompensa única, transacional, de **500 Bits + 1 Carga de Energia Total**, e **25 pontos de treinador** por concluir, sem exigir resgate.

A lista de 12 tópicos está congelada para esta meta. Tópicos futuros/desconhecidos não substituem uma leitura obrigatória nem retiram uma conquista concluída; alterações futuras precisam de decisão explícita no catálogo. Não muda as seis etapas nem os resgates do tutorial inicial.
