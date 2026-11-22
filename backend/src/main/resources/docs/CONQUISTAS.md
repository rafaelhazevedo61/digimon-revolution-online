# Conquistas — catálogo completo

Este documento lista as **33 conquistas** cadastradas no catálogo versionado do sistema, com suas condições e recompensas. Acesso no jogo: **Mais → Conquistas**.

Fonte: [V250 — catálogo de conquistas e Passe VIP](../db/migration/V250__achievement_prototype_and_vip_pass.sql). Os valores abaixo refletem essa migration; alterações posteriores diretamente no banco precisam ser refletidas neste documento.

## Regras gerais

- Cada conquista concede os Bits e a quantidade de itens indicados na tabela, uma única vez por conta.
- O resgate é manual. Os itens vão para o inventário, sem ativação ou uso automático.
- As etapas acumulativas compartilham a mesma contagem: as primeiras 10 missões fazem parte da meta de 100.
- O progresso é consultado e atualizado ao abrir/atualizar a página ou tentar resgatar uma conquista.
- Uma conclusão já reconhecida permanece registrada após renascimento ou troca de Digimon.
- Feitos anteriores são aproveitados quando existem registros confiáveis no banco.

## Primeiros passos

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `FIRST_HATCH` | Primeiro companheiro | Choque seu primeiro Digitama. | 100 | 1 × Célula de Energia |
| `FIRST_MISSION` | Primeira expedição | Resgate sua primeira missão. | 100 | 1 × Célula de Energia |
| `FIRST_EQUIP` | Pronto para a aventura | Equipe uma peça em seu Digimon. | 150 | 2 × Disco de XP +5% |

## Criação

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `HATCH_10` | Berçário digital I | Choque 10 Digitamas. | 500 | 1 × Incubadora rara |
| `HATCH_50` | Berçário digital II | Choque 50 Digitamas. | 2.000 | 1 × Incubadora épica |
| `HATCH_100` | Guardião do berçário | Choque 100 Digitamas. | 5.000 | 1 × Dado de Raridade |

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

## Coleção

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `SPECIES_10` | Espaço para novos amigos | Registre 10 espécies diferentes na coleção. | 500 | 1 × Expansor de Storage +5 |
| `SPECIES_25` | Olhar de colecionador | Registre 25 espécies diferentes na coleção. | 1.500 | 1 × Dado de Raridade |
| `SPECIES_50` | Uma família digital | Registre 50 espécies diferentes na coleção. | 3.000 | 1 × Expansor de Storage +5 |
| `SPECIES_100` | Enciclopédia viva | Registre 100 espécies diferentes na coleção. | 6.000 | 2 × Expansor de Storage +5 |

## Missões

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `MISSION_10` | Explorador I | Resgate 10 missões, individuais ou em equipe. | 300 | 2 × Célula de Energia |
| `MISSION_100` | Explorador II | Resgate 100 missões, incluindo resgates automáticos. | 1.500 | 2 × Carga de Energia Total |
| `MISSION_500` | Explorador III | Resgate 500 missões. | 5.000 | 1 × Passe VIP de 1 Dia |
| `MISSION_1000` | Lenda das expedições | Resgate 1.000 missões. | 10.000 | 1 × Incubadora lendária |

## Combate

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `ARENA_1` | Primeira vitória | Vença um desafio iniciado por você na arena. | 250 | 1 × Célula de Energia |
| `ARENA_25` | Competidor | Vença 25 desafios iniciados por você na arena. | 1.500 | 3 × Disco de XP +20% |
| `ARENA_100` | Nome de respeito | Vença 100 desafios iniciados por você na arena. | 5.000 | 1 × Dado de Raridade |
| `WORLD_1` | Na linha de frente | Realize um ataque válido ao Chefe Mundial. | 250 | 1 × Célula de Energia |
| `WORLD_50` | Presença constante | Realize 50 ataques ao Chefe Mundial. | 2.500 | 1 × Núcleo de Ascensão |
| `WORLD_200` | Defensor do Mundo Digital | Realize 200 ataques ao Chefe Mundial. | 7.500 | 5 × Carga de Energia Total |

## Clã

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `RAID_10` | Força em conjunto | Realize 10 ataques em incursões de clã. | 1.000 | 1 × Carga de Energia Total |
| `RAID_50` | Companheiro de batalha | Realize 50 ataques em incursões de clã. | 3.000 | 1 × Núcleo de Ascensão |

## Equipamentos

| Código | Conquista | Condição | Bits | Item recebido |
|---|---|---|---:|---|
| `REFINE_5` | Arsenal aprimorado | Tenha um equipamento com refinamento +5 ou superior. | 1.000 | 2 × Cristal de Proteção |
| `REFINE_10` | Obra de mestre | Tenha um equipamento com refinamento +10 ou superior. | 3.000 | 1 × Núcleo de Ascensão |
| `ASCEND_1` | Um novo patamar | Tenha um equipamento com uma ascensão ou mais. | 1.500 | 3 × Cristal de Proteção |

## Resumo das recompensas

Ao concluir e resgatar todas as conquistas deste catálogo, a conta recebe **79.600 Bits**, além dos seguintes itens:

| Item | Código do item | Quantidade total |
|---|---|---:|
| Célula de Energia | `ENERGY_CELL` | 8 |
| Disco de XP +5% | `XP_DISC_5` | 4 |
| Incubadora rara | `INCUBATOR_RARE` | 1 |
| Incubadora épica | `INCUBATOR_EPIC` | 1 |
| Dado de Raridade | `RARITY_REROLL` | 4 |
| Disco de XP +10% | `XP_DISC_10` | 3 |
| Carga de Energia Total | `VIP_ENERGY_CHARGE` | 10 |
| Passe VIP de 1 Dia | `VIP_DAY_PASS` | 2 |
| Cristal de Preservação de Raridade | `RARITY_PRESERVATION` | 3 |
| Expansor de Storage +5 | `STORAGE_SLOT_5` | 4 |
| Incubadora lendária | `INCUBATOR_LEGENDARY` | 1 |
| Disco de XP +20% | `XP_DISC_20` | 3 |
| Núcleo de Ascensão | `ASCENSION_CORE` | 3 |
| Cristal de Proteção | `REFINEMENT_PROTECTION` | 5 |

Os quatro expansores de Storage +5 somam **+20 espaços**, depois de usados. Os dois Passes VIP somam **48 horas de VIP**, se ambos forem usados.

## Como cada condição é contada

| Condição | Origem da contagem |
|---|---|
| Nascimentos | Nascimentos diretos registrados e incubações resgatadas. Renascimentos e compras não contam como nascimento. |
| Missões | Resgates manuais e automáticos. Iniciar ou apenas terminar o tempo de uma missão não basta: ela precisa chegar a `CLAIMED`. |
| Espécies | Espécies distintas registradas na coleção. Raridades diferentes da mesma espécie não aumentam essa contagem. |
| Estágio | Maior estágio comprovado pelo álbum ou pelos registros de Digimons da conta. Um estágio superior também satisfaz os anteriores. |
| Renascimentos | Quantidade de registros de Digimons com status `REBORN`, sem somar o contador de todas as gerações da mesma linhagem. |
| Arena | Vitórias de desafios iniciados pela própria conta. Defesas passivas não entram. |
| Chefe Mundial | Ataques válidos registrados para a conta. |
| Incursões de clã | Ataques registrados em incursões de clã. |
| Primeiro equipamento | Etapa `EQUIP_ITEM` registrada no tutorial. |
| Refinamento e ascensão | Maior patamar de uma peça que a conta possui ao consultar a página. Não soma refinamentos de várias peças. |

### Missões e expurgo

O contador de conquistas de missões é separado das instâncias de missão. A transição para `CLAIMED` incrementa esse contador de forma permanente; regravar o mesmo status não incrementa novamente. Portanto, **expurgar missões antigas não apaga as contagens posteriores à instalação do sistema**.

Na instalação, o contador é inicializado com as missões resgatadas que ainda constam no histórico. Missões já expurgadas antes disso não podem ser recuperadas. O contador antigo de bônus de missão, limitado a 100 conclusões por missão, não é usado para inventar esse total.

### Equipamentos

A peça precisa estar na conta quando a condição for reconhecida. Uma peça vendida antes da primeira consulta pode não ser reconhecida. Depois de registrada a conclusão, vender a peça ou renascer não remove a conquista.

### Passe VIP de 1 Dia

O item `VIP_DAY_PASS` permanece no inventário até ser usado. Concede **24 horas** a partir do uso se o VIP estiver inativo/vencido; com VIP ativo, soma 24 horas ao vencimento existente. Não pode ser vendido ou negociado e não resgata automaticamente a Carga de Energia Total diária.

### Itens diferentes com nomes semelhantes

- **Cristal de Preservação de Raridade** (`RARITY_PRESERVATION`): preserva a raridade no renascimento.
- **Cristal de Proteção** (`REFINEMENT_PROTECTION`): item de proteção do refinamento de equipamentos.

## Manutenção do catálogo

As definições ficam em `achievement_definitions`, e o progresso/resgate em `player_achievements`. Mudanças de nome, meta, Bits, item ou quantidade devem ser acompanhadas da atualização desta lista. Após aplicar uma migration, crie outra para alterar o catálogo; não reescreva migrations já executadas.
