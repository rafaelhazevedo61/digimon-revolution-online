# Tutorial inicial e Guia do Treinador

## Objetivo

Ensinar o ciclo inicial e apresentar os demais conteúdos gradualmente. O tutorial obrigatório de Primeiros Passos mantém suas seis etapas; o Guia do Treinador acrescenta orientações opcionais, consultáveis mesmo após finalizar o tutorial.

## Primeiros Passos

| Ordem | Código preservado | Objetivo |
| --- | --- | --- |
| 1 | SELECT_DIGITAMA | Escolher a Digitama inicial. |
| 2 | HATCH_DIGIMON | Conhecer o primeiro parceiro, nascido pela tela inicial. |
| 3 | COMPLETE_MISSION | Iniciar uma missão liberada, aguardar e resgatar o resultado. |
| 4 | BUY_SHOP | Fazer a primeira compra, conferindo preços e limites. |
| 5 | EQUIP_ITEM | Equipar arma, armadura ou acessório no Digimon. |
| 6 | EVOLVE_DIGIMON | Consultar nível e fragmentos e realizar a primeira evolução. |

As ações reais continuam registrando essas etapas automaticamente pelos fluxos existentes. A ordem orienta o jogador, mas não introduz novo bloqueio de gameplay. As recompensas, os códigos persistidos e os resgates existentes foram preservados. A finalização continua exigindo as seis etapas e o resgate das recompensas pendentes.

Os textos agora explicam o resgate das missões, o cuidado com os Bits, os bônus de equipamentos e a consulta aos requisitos de evolução. O destino da etapa de nascimento é `starter`, porque o primeiro nascimento não usa a incubação posterior. Uma etapa sem recompensa só aparece riscada depois de efetivamente concluída.

## Descobertas opcionais

Acesso: **Mais → Conta → Guia do Treinador**, ou botão **Abrir Guia do Treinador** no card de Primeiros Passos. A rota é `#trainer-guide`.

| Conteúdo | Apresentado após | Destino |
| --- | --- | --- |
| Coleção | Primeiro nascimento | Coleção |
| Conquistas | Primeiro nascimento | Conquistas |
| Energia e recuperação | Primeiro nascimento | Inventário |
| Novas incubações | Primeiro nascimento | Incubação |
| Times de um a três Digimons | Primeira missão concluída/resgatada | Meus Times |
| Chefes, Chefe Mundial e incursões | Primeira missão concluída/resgatada | Chefes |
| Arena | Primeira missão concluída/resgatada | Arena |
| Torre Infinita | Primeira missão concluída/resgatada | Torre |
| Classificação de jogadores | Primeira missão concluída/resgatada | Classificação, aba Jogadores |
| Ferreiro | Primeira evolução | Ferreiro |
| Renascimento | Primeira evolução | Renascimento |
| Benefícios opcionais do VIP | Primeira evolução | VIP |

A disponibilidade acima libera a orientação, não a atividade. Níveis, estágio, energia, time, materiais, limites e outros requisitos continuam sendo validados pela atividade correspondente. O guia não exige que o jogador complete arena, torre, renascimento, VIP ou todo o catálogo de sistemas para finalizar os primeiros passos.

A página mostra uma próxima descoberta sugerida, seguida de grupos recolhíveis: Seu progresso, Seus parceiros, Novos desafios e Próximos objetivos. Os textos explicam:

- Coleção por espécie/raridade, duplicatas e maestria.
- Atualização periódica e resgate das conquistas.
- Energia condicionada à configuração de consumo e itens de recuperação.
- Diferença entre nascimento inicial e incubação de novos parceiros.
- Formações de um a três Digimons e disponibilidade de membros.
- Requisitos de chefes e arena.
- Torre permanente, recompensa por vitória, chefes/marcos, ausência de custo de energia e detalhes de HP.
- Classificação geral/semanal, busca, perfis e necessidade de histórico para variação diária.
- Custos, chances e riscos do ferreiro e planejamento do renascimento.
- VIP como opção, sem obrigatoriedade para concluir tutorial ou guia.

## Leitura e progresso

- **Conhecer** abre a tela correspondente. Isso não declara uma ação realizada.
- **Entendi** registra somente a leitura educativa e não declara combates, missões ou outras ações realizadas. Ler os 12 conteúdos conta para a conquista Treinador bem informado, processada pela rotina agendada.
- Cada tópico pode ser reconhecido uma única vez por conta; repetir o pedido é idempotente.
- O reconhecimento é salvo no servidor e aparece em outros dispositivos após carregar a página.
- Os tópicos lidos continuam consultáveis. Não há reset de leitura nesta versão. Ao ler os 12 conteúdos, a conquista oferece 500 Bits + 1 Carga de Energia Total, resgatados em Conquistas após o processamento.
- Contas que já finalizaram o tutorial mantêm seu estado e têm as orientações disponíveis no guia; o card encerrado não reaparece no dashboard.

## Backend e persistência

Migration **V258__trainer_guide_read_topics.sql** cria `trainer_guide_read_topics`, com chave composta por jogador/tópico e horário de leitura. Exclusão da conta remove suas leituras por FK. Não altera `tutorial_progress` nem `tutorial_completions`.

Catálogo: `TutorialGuideTopic`. Pré-requisitos usam eventos existentes de `TutorialStep`; não consultam ou recalculam conquistas, ranking ou históricos de combate.

| Endpoint autenticado | Comportamento |
| --- | --- |
| GET /tutorial/guide | Retorna o tutorial inicial, os tópicos disponíveis/lidos e a contagem de leituras. Não grava progresso. |
| POST /tutorial/guide/{topic}/acknowledge | Valida o código e seu pré-requisito, registra a leitura para a identidade do token e retorna a página atualizada. |

O corpo não recebe identidade de outro jogador. Códigos desconhecidos e orientações ainda bloqueadas são rejeitados. Um registro desconhecido no banco não aumenta a contagem do catálogo atual. A V259 adiciona um trigger leve de enfileiramento para conquistas em cada leitura nova. A tabela não concede recompensas diretamente nem registra ações de gameplay fictícias.

## Validação

- `TutorialServiceTest`: regressão das seis etapas, recompensas e finalização existentes.
- `TutorialGuideServiceTest`: disponibilidade por marco, preservação de tutorial finalizado, rejeição de tópicos, identidade da conta e separação entre leitura e gameplay.
- `scripts/tutorial-guide.test.cjs`: SQL real da migration/upsert em PGlite, idempotência, isolamento por conta, remoção por FK, estados da interface, duplo clique, textos seguros, rota do primeiro nascimento, aba Jogadores e respostas tardias.
- O plano manual em `docs/tutorial-manual-test-plan.md` inclui os cenários de integração do guia.

O cache da PWA inclui os scripts/CSS do guia. Homologar também no celular e com o backend real antes de publicar para todos os jogadores.


A conquista `TRAINER_GUIDE_COMPLETE` considera os 12 tópicos atuais, inclusive leituras anteriores à V259. A conclusão vale 25 pontos de treinador, independentemente do resgate; consulte [CONQUISTAS.md](CONQUISTAS.md).
