# Roadmap de próximas sprints — Digimon Revolution Online

**Versão:** 1.0
**Data:** 20 de agosto de 2026
**Estado de referência:** infraestrutura de Docker, PostgreSQL, MongoDB de auditoria, correlation ID, healthcheck, Transactional Outbox, Caffeine, auditoria positiva, TTL, retry e `DEAD_LETTER` concluída.

## 1. Ponto de partida

A primeira grande etapa técnica do DRO foi concluída. O projeto agora possui uma base capaz de executar a aplicação de forma reproduzível, manter o PostgreSQL como fonte oficial do estado do jogo, registrar auditorias positivas e erros no MongoDB, correlacionar requisições HTTP, cachear somente catálogos seguros e recuperar falhas de publicação sem retries infinitos.

> **Decisão arquitetural:** não há necessidade de introduzir Kafka neste momento. O Transactional Outbox no PostgreSQL atende ao volume e ao número atual de consumidores. Kafka deve permanecer como uma possibilidade futura, condicionada a métricas reais de volume, latência, múltiplos consumidores independentes ou necessidade de replay em larga escala.

A próxima fase deve evitar adicionar funcionalidades econômicas de alta concorrência imediatamente. Antes de expandir o comércio entre jogadores, é importante transformar a infraestrutura recém-criada em uma base verificável por testes de integração, execução contínua e procedimentos operacionais repetíveis.

## 2. Princípios de priorização

| Princípio | Aplicação no roadmap |
|---|---|
| Integridade do estado | PostgreSQL continua sendo a autoridade para jogadores, Digimon, inventário, Bits, equipamentos, anúncios e resgates. |
| Segurança econômica | Operações que movimentam itens ou moeda devem ser transacionais, idempotentes e protegidas contra concorrência. |
| Entrega incremental | Cada sprint deve gerar uma PR própria contra `develop`, com critérios de aceite verificáveis. |
| Observabilidade útil | Toda operação crítica deve deixar rastros suficientes para investigação sem persistir segredos. |
| Complexidade proporcional | Não adicionar Kafka, microsserviços ou infraestrutura distribuída sem uma necessidade medida. |
| Experiência do jogador | A interface, mensagens, estados e erros devem permanecer em Português Brasileiro, preservando os estágios e tipagens oficiais de Digimon. |
| Reversibilidade | Features econômicas devem possuir limites, auditoria, expiração e mecanismos administrativos de correção. |

## 3. Sequência recomendada

| Ordem | Sprint | Tema | Prioridade | Dependências |
|---:|---|---|:---:|---|
| 0 | Release Hardening | Consolidar a base técnica, testes de integração e operação | P0 | Infraestrutura mergeada |
| 1 | Economy Safety | Concorrência, idempotência e proteção contra duplicidade econômica | P0 | Sprint 0 |
| 2 | Troca Direta | Negociação segura entre dois jogadores | P0 | Sprint 1 |
| 3 | Marketplace 2.0 | Expansão da Casa de Leilões e maturidade comercial | P1 | Sprints 1 e 2 |
| 4 | Segurança de Conta | Sessões, rate limiting, auditoria administrativa e proteção de abuso | P1 | Sprint 0 |
| 5 | Comunidade 2.0 | Respostas, filtros e melhorias de comunicação no Correio e Clãs | P1 | Sprint 1 |
| 6 | Eventos e Live Ops | Ferramentas para eventos recorrentes, premiações e calendário | P1 | Sprints 4 e 5 |
| 7 | World Boss 2.0 | Escala, ranking, concorrência e experiência de participação | P1 | Sprints 0 e 1 |
| 8 | Performance e Escala | Testes de carga, consultas, filas e decisão baseada em métricas | P2 | Sprints 0 a 7 |

A ordem não significa que todas as sprints precisam ter o mesmo tamanho. A Sprint 0 deve ser tratada como uma etapa de estabilização curta; as demais podem ser divididas em PRs menores quando houver mudança de domínio, backend, frontend e documentação suficientemente independentes.

---

## Sprint 0 — Release Hardening e testes de integração

### Objetivo

Transformar a infraestrutura recém-mergeada em uma base confiável para as próximas features, reduzindo a dependência de validações manuais e documentando o caminho de execução local, CI e recuperação operacional.

### Entregáveis

| Área | Entrega |
|---|---|
| Testes | Testes de integração com PostgreSQL e MongoDB para Outbox, auditoria positiva, erro HTTP, TTL e `DEAD_LETTER`. |
| CI | Pipeline com compilação, testes focados, `git diff --check`, verificação de migrations e build Docker. |
| Operação | Comando ou script seguro para listar `FAILED`/`DEAD_LETTER`, validar healthchecks e verificar índices TTL. |
| Segurança | Revisão de segredos hardcoded, `.env`, collections e payloads de auditoria. |
| Banco | Verificação de índices PostgreSQL, duração das queries do Outbox e comportamento após reinício da API. |
| Documentação | README, guia de observabilidade e roteiro manual alinhados com os comandos reais. |

### Critérios de aceite

A sprint será concluída quando uma execução limpa conseguir subir PostgreSQL, MongoDB e API; aplicar todas as migrations; executar uma transação positiva; publicar o Outbox; simular falha e retry; mover um evento para `DEAD_LETTER`; reprocessá-lo com segurança; e concluir sem credenciais rastreadas pelo Git.

### Por que vem primeiro

A infraestrutura já funciona, mas ainda depende de testes manuais e de conhecimento operacional acumulado durante a implementação. Automatizar essa validação reduz o risco de uma feature econômica mascarar um problema de banco, cache, auditoria ou concorrência.

---

## Sprint 1 — Economy Safety e concorrência

### Objetivo

Criar uma camada comum de proteção para operações que movimentam Bits, itens, equipamentos, anúncios e recompensas antes de iniciar a troca direta entre jogadores.

### Entregáveis

A sprint deve revisar locks otimistas e pessimistas onde houver saldo ou estoque, padronizar respostas de conflito, adicionar idempotency keys para comandos sensíveis, garantir que eventos de auditoria sejam criados somente após a transação oficial e criar testes de concorrência para compras, resgates e cancelamentos.

Também deve definir limites administrativos para correção de saldo e inventário, registrando quem executou a correção, o motivo e o identificador da operação. O Caffeine deve continuar fora de qualquer estado mutável ou sensível.

### Critérios de aceite

Duas requisições simultâneas não podem criar saldo, item, anúncio ou resgate duplicado. Um retry HTTP do mesmo comando não pode duplicar a operação quando a idempotência estiver prevista. Conflitos devem resultar em resposta controlada, e o Outbox deve conter no máximo o evento correspondente à transação efetivamente confirmada.

---

## Sprint 2 — Troca direta entre jogadores

### Objetivo

Implementar a negociação segura de itens, equipamentos e Bits entre dois jogadores, com confirmação dos dois lados e garantia de que a troca ocorre de forma atômica ou não ocorre.

### Escopo recomendado

A primeira versão deve usar uma sessão de troca com dois participantes, estado explícito e expiração automática. O fluxo pode ser modelado como `CREATED`, `OFFERING`, `BOTH_CONFIRMED`, `COMPLETED`, `CANCELLED` e `EXPIRED`. Cada lado deve confirmar novamente após qualquer alteração na oferta; uma mudança invalida a confirmação anterior.

A troca deve validar ownership, quantidade, disponibilidade, equipamentos equipados, saldo, bloqueios de conta e compatibilidade dos itens. O commit final deve ocorrer em uma transação PostgreSQL, com locks nos recursos envolvidos. O MongoDB recebe apenas a auditoria positiva ou o erro sanitizado.

### Critérios de aceite

Uma troca válida transfere todos os recursos exatamente uma vez. Cancelamento, expiração, desconexão, alteração de oferta, saldo insuficiente e item removido devem deixar os recursos intactos. O jogador não pode negociar o Digimon ativo ou um equipamento que deixou de possuir. Cada troca concluída deve gerar um evento auditável com os dois participantes e sem dados secretos.

### Divisão sugerida de PRs

| PR | Escopo |
|---:|---|
| A | Modelo, migration e máquina de estados da troca |
| B | Criação, convite, oferta e cancelamento |
| C | Confirmação dupla e commit atômico |
| D | Frontend, expiração, notificações e testes de concorrência |

---

## Sprint 3 — Marketplace 2.0

### Objetivo

Evoluir a Casa de Leilões sem comprometer a segurança econômica já estabilizada.

### Possíveis entregas

A prioridade deve ser definida entre suporte a equipamentos negociáveis, melhorias de expiração/devolução pendente, filtros e ordenação, histórico de vendas, limites por jogador, proteção contra anúncios duplicados e refinamento das mensagens de compra/cancelamento.

Equipamentos só devem entrar no marketplace depois que ownership, equipados, raridade, refinamento e remoção do inventário estiverem cobertos por transações e testes de concorrência. A taxa por duração de 24, 48 e 72 horas deve permanecer configurável e auditável.

### Critérios de aceite

Anúncios expirados não podem permanecer compráveis. Um cancelamento concorrente com uma compra deve produzir somente um resultado válido. A comissão e a devolução devem ser calculadas uma única vez. O cache `shopCatalog` não deve ser confundido com cache de anúncios, saldo ou inventário.

---

## Sprint 4 — Segurança de conta e abuso

### Objetivo

Reduzir riscos de fraude, abuso de endpoints e comprometimento de contas antes da abertura de sistemas econômicos mais valiosos.

### Entregáveis prioritários

A sprint deve avaliar expiração e renovação de sessão, rotação ou revogação de tokens, limites de tentativas de login, rate limiting por IP/conta/endpoint, proteção de rotas administrativas, política de senha, auditoria de ações admin e sanitização de logs de autenticação.

Também é importante separar claramente identificadores técnicos de dados pessoais e definir uma política para retenção de logs de segurança. Nenhuma mudança deve registrar JWT, senha ou header `Authorization` em texto puro.

### Critérios de aceite

Tentativas repetidas são limitadas; operações admin ficam auditadas; tokens expirados não permitem mutações; respostas não revelam se um usuário ou credencial existe além do necessário; e os testes confirmam que dados sensíveis não aparecem no MongoDB, logs da API ou collections de teste.

---

## Sprint 5 — Comunidade 2.0

### Objetivo

Melhorar a comunicação social depois da conclusão do Correio completo.

### Entregáveis possíveis

A primeira versão pode incluir respostas encadeadas, filtros por tipo, busca, arquivamento, leitura em massa, expiração visual, agrupamento de notificações e melhorias de comunicados administrativos. Para clãs, pode incluir histórico de convites, notificações de entrada/saída e permissões mais claras para comunicados e recompensas.

A evolução deve evitar transformar mensagens comuns em mecanismo de transferência informal de itens ou Bits. Transferências econômicas permanecem na troca direta, marketplace ou fluxos oficiais de recompensa.

### Critérios de aceite

A caixa de correio permanece rápida para listas grandes, mantém exclusão independente entre remetente e destinatário, sinaliza não lidas na home e não permite que uma mensagem seja interpretada como transação econômica sem passar pelo fluxo oficial.

---

## Sprint 6 — Eventos e Live Ops

### Objetivo

Permitir que o painel admin opere eventos recorrentes com menos intervenção manual e menor risco de distribuição incorreta de prêmios.

### Entregáveis possíveis

O escopo pode incluir calendário de eventos, templates de premiação, pré-visualização de destinatários, validação de clã/lista de jogadores, idempotência de distribuição, reprocessamento de falhas e relatório de entrega. O painel deve mostrar claramente o que será enviado, quem receberá e quais recompensas já foram entregues.

Toda distribuição deve continuar usando PostgreSQL para estado e Outbox para auditoria. Uma falha parcial deve ser identificável por destinatário, sem duplicação quando o administrador repetir a operação.

### Critérios de aceite

Uma premiação de evento pode ser simulada antes do envio, não duplica recompensas em retry, identifica destinatários inválidos, mostra falhas no painel e gera auditoria positiva por operação concluída.

---

## Sprint 7 — World Boss 2.0

### Objetivo

Fortalecer o Boss Mundial para maior concorrência, melhor leitura de contribuição e maior previsibilidade das recompensas.

### Entregáveis possíveis

A sprint deve revisar concorrência na redução do HP global, criação única da instância diária, retry curto ou resposta controlada para conflitos, ranking de dano, proteção contra ataques duplicados, limites diários e histórico de recompensas. O uso de `@Version` precisa resultar em comportamento compreensível para o jogador, não em erro 500 genérico.

Também é uma boa oportunidade para testes de carga controlados e para validar que o cálculo de dano documentado na wiki permanece igual ao código. O PostgreSQL deve continuar sendo a fonte oficial do HP, contribuições e recompensas do boss.

### Critérios de aceite

Ataques simultâneos não sobrescrevem HP silenciosamente, a instância diária é única, o jogador recebe resposta controlada em conflito, ranking e recompensas são consistentes e nenhum ataque aceito é processado duas vezes.

---

## Sprint 8 — Performance, carga e escala medida

### Objetivo

Medir a aplicação em condições próximas do uso real antes de decidir se a arquitetura precisa de novos componentes.

### Entregáveis

A sprint deve criar cenários de carga para login, dashboard, loja, catálogo, Outbox, correio, boss e consultas de ranking. Deve acompanhar latência, erro, conexões PostgreSQL, tamanho do Outbox, tempo de publicação no MongoDB, acertos do Caffeine e crescimento das collections.

A decisão sobre Kafka deve ser tomada somente depois dessa medição. Se o volume continuar moderado e houver um único consumidor principal, o Outbox no PostgreSQL permanece a opção mais simples. Se surgirem múltiplos consumidores, latência de publicação insuficiente ou backlog persistente, será possível desenhar uma migração gradual sem remover o Outbox.

### Critérios de aceite

Os limites de carga são registrados, os gargalos são priorizados por evidência, as queries críticas possuem plano aceitável, o backlog do Outbox é observável e qualquer decisão de nova infraestrutura apresenta custo, benefício, plano de rollback e impacto operacional.

## 4. O que não priorizar agora

Kafka, microsserviços, troca de banco principal, cache de saldo/inventário, reescrita completa dos frontends e grandes features econômicas sem testes de concorrência não devem ser a próxima iniciativa. Cada uma pode voltar ao roadmap quando houver uma necessidade demonstrada, mas introduzi-las agora aumentaria o risco justamente após a consolidação da infraestrutura.

Também não é recomendável iniciar simultaneamente troca direta, equipamentos na Casa de Leilões e eventos automatizados. Todas movimentam recursos e precisam compartilhar a mesma camada de segurança econômica. A sequência mais segura é estabilizar concorrência, implementar troca direta em escopo reduzido e somente depois expandir o marketplace.

## 5. Próxima sprint recomendada

A recomendação é iniciar pela **Sprint 0 — Release Hardening e testes de integração**. Ela deve produzir uma base curta e objetiva para que as próximas features não dependam de validações manuais difíceis de repetir.

O primeiro PR dessa sprint pode ser `test(infra): add observability integration test foundation`, contendo a infraestrutura de testes, cenários do Outbox, fixture segura de MongoDB/PostgreSQL e critérios de execução local. Em seguida, PRs menores podem cobrir CI, segurança de configuração e scripts operacionais.

Após a aprovação da Sprint 0, a próxima feature de produto recomendada é a **Troca Direta entre Jogadores**, começando pelo modelo de estado e pelas garantias transacionais antes de qualquer refinamento visual.

## 6. Definition of Done para cada sprint

Uma sprint só deve ser considerada concluída quando possuir PR própria contra `develop`, documentação atualizada, migrations novas validadas, testes focados aprovados, `git diff --check` sem problemas, critérios de aceite executados e nenhuma credencial no repositório. Features econômicas devem apresentar também teste de concorrência, idempotência, rollback, auditoria e comportamento de retry.

## 7. Indicadores para acompanhar

| Indicador | Por que acompanhar |
|---|---|
| Taxa de erros HTTP por endpoint | Identificar regressões funcionais e abuso. |
| Tamanho e idade do backlog Outbox | Detectar atraso de publicação ou MongoDB indisponível. |
| Quantidade de `FAILED` e `DEAD_LETTER` | Medir falhas persistentes e qualidade operacional. |
| Latência p50/p95/p99 | Priorizar gargalos reais antes de escalar infraestrutura. |
| Conflitos de atualização | Avaliar concorrência em economia e World Boss. |
| Acerto dos caches seguros | Confirmar benefício do Caffeine sem esconder estado mutável. |
| Crescimento das collections MongoDB | Planejar retenção e custo de armazenamento. |
| Denúncias, duplicidades e correções admin | Medir risco de abuso econômico. |

Este roadmap deve ser revisado ao fim de cada sprint. A ordem pode mudar por evidência operacional, mas a regra de preservar o PostgreSQL como autoridade e de exigir transações seguras para economia deve permanecer.
