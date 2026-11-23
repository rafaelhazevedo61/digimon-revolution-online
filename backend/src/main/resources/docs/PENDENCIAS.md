# Pendências técnicas e evoluções planejadas

Este documento acompanha débitos técnicos, validações e melhorias ainda não concluídas do Digimon Revolution Online. As propostas não representam funcionalidades já disponíveis nem defeitos confirmados.

## Como acompanhar

- **☐**: pendente. **☑**: concluído.
- Ao iniciar um item, registre o andamento em sua referência/observação, mantendo a caixa desmarcada.
- Marque como concluído somente após atender ao critério de conclusão e registrar o PR, teste ou outra evidência na coluna de referência.
- Preserve os IDs e as linhas concluídas para manter o histórico. Novos itens recebem novos IDs.
- As prioridades são sugestões: **Alta** prepara a próxima evolução; **Média** amplia a experiência; **Baixa** pode esperar as etapas anteriores.
- O acompanhamento é manual, atualizado junto às entregas; não há sincronização automática com os PRs.

## Equipamentos

| Check | ID | Prioridade | Pendência | Critério de conclusão | Referência / andamento |
| --- | --- | --- | --- | --- | --- |
| ☐ | EQP-001 | Alta | Implementar no backend os bônus de HP, ATK e DEF para 2 e 3 peças dos sets Overclock, Firewall, Data Breaker, Kernel e Overlord. | Bônus aplicados nos cálculos pertinentes e validados para 1, 2 e 3 peças, conforme a tabela de conjuntos. | [Plano de equipamentos](EQUIPMENT-PLAN.md#bônus-de-conjunto). Item preexistente; revalidar seu estado antes de executar. |

## Torre Infinita — melhorias funcionais

As linhas abaixo registram as **16 propostas discutidas** para evolução futura. Sua ordem não define automaticamente a ordem de implementação.

| Check | ID | Prioridade | Atividade | Critério de conclusão | Referência / andamento |
| --- | --- | --- | --- | --- | --- |
| ☐ | TOR-001 | Alta | Criar chefes exclusivos para os andares de chefe e marcos. | Seleção própria de guardiões a cada 5 e 10 andares, com prévia na tela e regras documentadas; deixar de usar apenas a rotação comum nesses encontros. | Definir catálogo e distribuição. |
| ☐ | TOR-002 | Alta | Adicionar mecânicas próprias aos chefes. | Mecânicas como ataque em área, escudo, regeneração ou aumento de força com HP baixo implementadas e visíveis na prévia e no resultado; validar interações e limites. | Depende da definição dos encontros em TOR-001. |
| ☐ | TOR-003 | Média | Criar andares com múltiplos inimigos. | Encontros com um, dois e três inimigos; seleção de alvo, ordem de ações e condição de vitória definidas e apresentadas ao jogador. | Adaptar o combate atual de guardião único. |
| ☐ | TOR-004 | Média | Adicionar modificadores de andar. | Condições como redução de defesa ou bônus de ataque/elemento exibidas antes do desafio e aplicadas de forma consistente para todos os jogadores naquele andar. | Definir modificadores e combinações permitidas. |
| ☐ | TOR-005 | Média | Aplicar vantagens de atributo e elemento. | Relações e multiplicadores definidos, documentados e considerados no combate; interface explica como a composição influencia o desafio. | Avaliar consistência com os demais combates do jogo. |
| ☐ | TOR-006 | Média | Dar uma função ao capitão do time. | Capitão concede um bônus ou estratégia definida, com limites para evitar acúmulo indevido; efeito visível e validado no combate. | Definir ataque concentrado, postura defensiva ou outra regra. |
| ☐ | TOR-007 | Alta | Ampliar o relatório do combate. | Exibir dano causado e recebido por membro, inimigos derrotados quando aplicável e histórico das rodadas; permitir entender a causa da derrota. | O HP final já existe; esta pendência é a ampliação do relatório. |
| ☐ | TOR-008 | Alta | Exibir prévia dos atributos efetivos do time. | Mostrar HP, ATK e DEF de cada membro com equipamentos e bônus de clã, usando o mesmo cálculo do combate e atualização ao trocar a formação. | A seleção de time já existe; falta a prévia dos atributos. |
| ☐ | TOR-009 | Média | Criar histórico consultável de tentativas. | Listar vitórias, derrotas, andares, datas e formações anteriores com paginação, autorização e política de retenção que preserve o progresso. | Hoje a tela mantém apenas o último resultado; auditoria não substitui uma tela de histórico. |
| ☐ | TOR-010 | Alta | Diversificar recompensas dos marcos. | Distribuição definida para discos de experiência, dados de raridade, cristais de preservação e incubadoras; prévia e concessão transacional seguem a mesma configuração. | Balancear frequência, quantidade e impacto econômico antes de liberar. |
| ☐ | TOR-011 | Média | Criar moeda e loja da torre. | Moeda obtida por regras definidas, saldo persistente e loja com custos/limites; compra e entrega atômicas, sem duplicação ou saldo negativo. | Avaliar junto à economia de TOR-010. |
| ☐ | TOR-012 | Alta | Integrar conquistas da torre. | Metas de andares, chefes e desafios integradas à rotina agendada de conquistas, com registros duráveis e recompensas únicas; atualizar o catálogo documentado. | Não contar repetição de pedido como novo evento. |
| ☐ | TOR-013 | Média | Criar ranking de progressão. | Ranking paginado pelo maior andar vencido, com desempate explícito e atualização eficiente; decidir elegibilidade de contas administrativas/de teste. | Priorizar após validar a curva de dificuldade. |
| ☐ | TOR-014 | Média | Adicionar desafios opcionais e revisita de andares. | Desafios como vencer sozinho, com estágio específico ou sem equipamentos; revisita autorizada e recompensa extra única, separada da primeira vitória normal. | Exige controle próprio de objetivos/recompensas para impedir farm indevido. |
| ☐ | TOR-015 | Média | Dar identidade visual às faixas da torre. | Cenários, cores e apresentação por setor/faixa, com boa legibilidade e funcionamento em celular e desktop. | Não alterar a dificuldade apenas por mudar o tema visual. |
| ☐ | TOR-016 | Baixa | Criar torre sazonal paralela. | Temporadas com regras, ranking, início/fim e recompensas próprios; progresso da torre permanente preservado e premiação sem duplicação. | Definir regras após consolidar a torre permanente. |

### Sequência inicial sugerida

1. Chefes exclusivos e mecânicas: **TOR-001 e TOR-002**.
2. Clareza para o jogador: **TOR-007 e TOR-008**.
3. Objetivos de progressão: **TOR-010 e TOR-012**.
4. Competição: **TOR-013**, após validar o balanceamento.

A sequência pode mudar conforme o retorno dos jogadores. Itens dependentes devem ter suas regras definidas antes da implementação.

## Torre Infinita — validações e pendências técnicas

Estes itens complementam as propostas funcionais. São trabalhos de validação e preparação técnica, sem presumir falhas já demonstradas.

| Check | ID | Prioridade | Atividade | Critério de conclusão | Referência / andamento |
| --- | --- | --- | --- | --- | --- |
| ☐ | TEC-TOR-001 | Alta | Executar e registrar a regressão das alterações de HP e da flag de cooldown local. | Compilação e testes Java/DOM/SQL executados sobre a versão integrada; validar HP após vitória, derrota e timeout, recarga da página, resultados antigos, cooldown ligado/desligado e repetição segura de pedidos. | A execução dessas atualizações ficou pendente na sessão de desenvolvimento por indisponibilidade do ambiente; confirmar se já foi validada posteriormente. |
| ☐ | TEC-TOR-002 | Alta | Homologar consistência com PostgreSQL real e múltiplas instâncias. | Tentativas simultâneas da mesma conta não duplicam recompensa ou avanço; validar primeira participação, retry, pedidos antigos, inventário cheio, limite de Bits e rollback conjunto de progresso e HP. Registrar evidências. | Os testes iniciais de SQL usaram PGlite; não equivalem a teste de concorrência entre servidores reais. |
| ☐ | TEC-TOR-003 | Alta | Validar a curva de dificuldade e a economia da torre. | Simular e jogar com formações de diferentes estágios, equipamentos e bônus; medir progressão e recursos distribuídos e registrar ajustes aprovados. | Fórmulas e recompensas atuais são iniciais de protótipo. |
| ☐ | TEC-TOR-004 | Média | Avaliar configuração versionada de encontros e recompensas. | Definir e implementar uma fonte de configuração com validação, prévia consistente e comportamento explícito para mudanças enquanto há tentativas em andamento. | Hoje nomes, perfis, fórmulas e recompensas ficam em TowerRules. Definir abordagem antes de substituir as regras. |
| ☐ | TEC-TOR-005 | Média | Medir desempenho e definir retenção dos dados adicionais. | Medir consultas, bloqueios, payloads e volume de dados com carga representativa; definir índices, paginação e retenção para histórico/rodadas, preservando progresso e controle de recompensas. | Especialmente relevante antes de TOR-007, TOR-009 e TOR-013. |

## Ranking de Treinadores — evolução

| Check | ID | Prioridade | Atividade | Critério de conclusão | Referência / andamento |
| --- | --- | --- | --- | --- | --- |
| ☑ | RANK-001 | Alta | Busca por jogador. | Nome parcial, paginação filtrada e posições globais preservadas; proteção contra respostas antigas e caracteres especiais. | PR #302; primeira etapa. |
| ☑ | RANK-002 | Alta | Perfil público do treinador. | Modal com coleção, conquistas, recorde da torre, pontuação e clã; apenas dados públicos e participantes elegíveis. | PR #302; primeira etapa. |
| ☑ | RANK-003 | Alta | Preparar histórico diário. | Captura imutável por conta/dia/regra em São Paulo, junto à transação da classificação, sem inventar datas anteriores. | PR #302; migration V256. |
| ☐ | RANK-004 | Alta | Classificação semanal de progresso. | Semana segunda às 00h de São Paulo; bases definidas para contas novas/primeira semana e mudanças de fórmula sem ganho fictício; paginação e posição pessoal. | Segunda etapa; usar histórico de RANK-003 e documentar regras de fechamento. |
| ☐ | RANK-005 | Alta | Variação de posição diária. | Comparação com registro válido do dia anterior e indicação de subida, queda, estabilidade ou nova entrada; tratar dias sem coleta e versões diferentes explicitamente. | Segunda etapa; depende de RANK-003. |

## Documentação relacionada

- [Funcionamento da Torre Infinita](TORRE-INFINITA.md): guardiões, perfis, recompensas, fórmulas, API e configuração local.
- [Plano de equipamentos](EQUIPMENT-PLAN.md): conjuntos e seus bônus.
- [Catálogo de conquistas](CONQUISTAS.md): metas existentes e recompensas.

- [Ranking de Treinadores](RANKING-TREINADORES.md): pontuação, busca, perfil e histórico.
