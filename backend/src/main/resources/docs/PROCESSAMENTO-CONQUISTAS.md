# Processamento agendado de conquistas

## Fluxo

1. A ação do jogador salva suas evidências na mesma transação de gameplay: missão resgatada, nascimento, equipamento, coleção ou combate.
2. Triggers da V252 inserem ou atualizam uma pendência para a conta. Várias ações são agrupadas na mesma linha.
3. `AchievementProcessingJob` chama `AchievementProcessor` em lotes. Cada conta tem sua própria transação e timeout de 15 segundos.
4. O processador avalia somente conquistas ativas ainda não concluídas e grava apenas progresso que mudou. Não existe recálculo nas rotas HTTP.
5. Ao terminar, atualiza `achievement_processing_state.processed_at` e remove a pendência na mesma transação.
6. A página consulta o progresso e a última atualização. O resgate valida uma conclusão já persistida e entrega a recompensa em transação.

## Configuração inicial

| Propriedade | Variável | Padrão |
|---|---|---:|
| `dro.achievements.scheduler.enabled` | `DRO_ACHIEVEMENTS_SCHEDULER_ENABLED` | `true` |
| `dro.achievements.scheduler.delay-ms` | `DRO_ACHIEVEMENTS_SCHEDULER_DELAY_MS` | 30000 |
| `dro.achievements.scheduler.initial-delay-ms` | `DRO_ACHIEVEMENTS_SCHEDULER_INITIAL_DELAY_MS` | 30000 |
| `dro.achievements.scheduler.batch-size` | `DRO_ACHIEVEMENTS_SCHEDULER_BATCH_SIZE` | 100 |
| `dro.achievements.scheduler.retry-delay-ms` | `DRO_ACHIEVEMENTS_SCHEDULER_RETRY_DELAY_MS` | 60000 |

O lote é limitado entre 1 e 1.000 contas; o retry tem mínimo de 1 segundo. O agendamento usa fixed delay: os 30 segundos começam ao terminar o lote anterior. Fila acumulada, tempo de processamento e retries podem aumentar o atraso percebido pelo jogador. Ao desabilitar o job, os eventos continuam sendo capturados e as pendências permanecem até reativá-lo.

## O que gera uma pendência

- Criação da conta; carga inicial única para as contas já existentes.
- Nascimento direto e resgate de incubação, além da fotografia de raridade/IVs/trait.
- Alterações reais de estágio, status ou dono de um Digimon. Atualizar energia sem mudar esses valores não conta como nova atividade de conquistas.
- Novas entradas no álbum e registros do tutorial.
- Contadores de missões, missões distintas concluídas e missões com time completo.
- Vitórias como atacante na arena; vitórias contra chefes.
- Ataques ao Chefe Mundial/incursões e o encerramento de um ciclo em derrota do chefe. A derrota enfileira os participantes com dano positivo.
- Mudanças relevantes de equipamentos e registros de ações de refinamento/ascensão/conjunto.
- Edição do catálogo, que enfileira as contas uma vez para reconhecer novas metas. Não há varredura periódica de todas as contas.

O worker não grava em fontes que enfileiram atividade. Atualizar a página, resgatar Bits/itens ou usar energia/VIP não gera um ciclo infinito de reprocessamento.

## Concorrência e falhas

- `achievement_pending_players` tem chave primária por conta.
- A seleção usa `FOR UPDATE SKIP LOCKED` dentro da transação de processamento. Instâncias diferentes ignoram pendências já bloqueadas.
- O worker não obtém lock de Player. As ações de gameplay podem bloquear Player e depois a pendência; essa separação evita a ordem inversa de locks.
- O enfileiramento usa `ON CONFLICT DO UPDATE`, não `DO NOTHING`. A atualização espera um processamento em curso. Se o worker remover a linha, a ação concorrente insere uma nova pendência após o commit.
- `requested_at` conserva a primeira atividade ainda pendente, evitando que contas muito ativas sejam empurradas continuamente para o fim da fila.
- Em caso de erro, progresso, timestamp e remoção da fila são revertidos. O job adia a conta e continua com outras; a pendência não é descartada.
- Se o processo cair, o PostgreSQL libera o lock; a transação não confirmada é revertida e outra execução pode assumir a conta.
- O catálogo é lido após bloquear cada pendência, evitando consumir uma invalidação nova com uma versão antiga do catálogo.

## Interface e API

`GET /players/me/achievements` é somente leitura e retorna, além do catálogo/progresso:

- `lastUpdatedAt`: instante do último processamento concluído, ou `null` antes da primeira execução.
- `updatePending`: informa se existe atividade aguardando processamento.

O botão Atualizar só faz nova consulta. A página não usa polling contínuo. Antes do primeiro lote, as conquistas aparecem em andamento com progresso zero e aviso de espera. `POST /players/me/achievements/{code}/claim` não dispara processamento e não permite resgatar uma meta ainda não processada.

## Histórico e marcos transitórios

O expurgo de missões não apaga contadores ou missões distintas já registradas. A V252 também persiste máximos de refinamento/ascensão quando a peça é gravada: vender ou perder uma peça antes do próximo lote não apaga o marco capturado. Fotografias de nascimento e ações continuam sendo registradas imediatamente, sem esperar o scheduler.

## Verificação operacional

Consultas de leitura úteis:

```sql
SELECT COUNT(*) AS pending_accounts,
       MIN(requested_at) AS oldest_pending,
       COUNT(*) FILTER (WHERE failures > 0) AS accounts_with_failures
FROM achievement_pending_players;

SELECT player_id, processed_at
FROM achievement_processing_state
ORDER BY processed_at DESC
LIMIT 20;
```

Homologar com múltiplas instâncias reais e PostgreSQL antes de afirmar capacidade de carga. A implementação não fixa uma promessa de quantos usuários simultâneos suporta; ajustar intervalo/lote conforme duração dos lotes, idade da fila e taxa de falhas.
