# Guia operacional de observabilidade

Este documento descreve a operação local da infraestrutura de auditoria do **Digimon Revolution Online (DRO)**. O desenho separa o estado oficial do jogo, a auditoria positiva, os erros HTTP e os caches de catálogo.

> **Regra de integridade:** PostgreSQL continua sendo a fonte oficial para jogadores, inventário, Bits, equipamentos, Digimon, anúncios, resgates e mensagens. MongoDB armazena somente documentos de auditoria e erros; Caffeine armazena somente leituras seguras de catálogo.

## Topologia

| Componente | Responsabilidade | Pode substituir o PostgreSQL? |
|---|---|---:|
| PostgreSQL | Estado transacional oficial do jogo e Transactional Outbox | Não se aplica; é a fonte oficial |
| MongoDB | Collections `dro_transaction_audits` e `dro_error_logs` | Não |
| API Spring Boot | Regras de negócio, escrita transacional e publicação assíncrona | Não |
| Caffeine | Cache local de `shopCatalog`, `itemDefinitions` e `equipmentTemplates` | Não |
| Audit Outbox Processor | Publica eventos confirmados no MongoDB com retry e idempotência | Não |

As operações críticas gravam o evento no Outbox na mesma transação PostgreSQL que altera o jogo. O processador agendado lê eventos `PENDING` e `FAILED`, publica a auditoria positiva no MongoDB e marca o evento como `PUBLISHED`. Um rollback da transação oficial também remove o evento outbox, evitando auditoria positiva de uma operação que não foi efetivada.

## Execução local

Na raiz do projeto, prepare o ambiente a partir do exemplo, substituindo as senhas locais pelos valores desejados:

```bash
cp docker/.env.example docker/.env
# edite docker/.env antes de usar em qualquer ambiente compartilhado
set -a
source docker/.env
set +a
docker compose --env-file docker/.env -f docker/docker-compose.yml up --build -d
```

Verifique os serviços e os healthchecks:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml ps
docker compose --env-file docker/.env -f docker/docker-compose.yml logs --tail=200 dro-api
docker compose --env-file docker/.env -f docker/docker-compose.yml logs --tail=200 dro-postgres
docker compose --env-file docker/.env -f docker/docker-compose.yml logs --tail=200 dro-mongodb
curl -i http://localhost:${API_PORT:-8080}/actuator/health
```

Para reiniciar somente a API sem perder os volumes:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml restart dro-api
```

Para remover os containers e a rede, preservando dados persistidos:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml down
```

A remoção dos volumes é destrutiva e deve ser reservada para um ambiente descartável:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml down -v
```

## Variáveis de observabilidade

| Variável | Padrão | Uso |
|---|---:|---|
| `SPRING_DATASOURCE_URL` | JDBC local | URL do PostgreSQL usado pela API |
| `SPRING_DATASOURCE_USERNAME` | exemplo local | Usuário do PostgreSQL usado pela API |
| `SPRING_DATASOURCE_PASSWORD` | exemplo local | Senha do PostgreSQL usada pela API |
| `SPRING_MONGODB_URI` | URI local | Conexão da API com o MongoDB de auditoria no Spring Boot 4 |
| `SPRING_DATA_MONGODB_AUTO_INDEX_CREATION` | `false` | Criação automática dos índices MongoDB; habilite localmente para validar TTL |
| `DRO_AUDIT_OUTBOX_FIXED_DELAY_MS` | `5000` | Intervalo entre ciclos do processador |
| `DRO_AUDIT_OUTBOX_MAX_ATTEMPTS` | `5` | Número de tentativas já consumidas antes de mover o evento para `DEAD_LETTER` |
| `DRO_CACHE_CATALOGS_MAX_SIZE` | `500` | Limite por cache de catálogo |
| `DRO_CACHE_CATALOGS_TTL_SECONDS` | `300` | Tempo de expiração por inatividade do catálogo |
| `MONGO_ROOT_USERNAME` | exemplo local | Usuário administrativo do MongoDB no Compose |
| `MONGO_ROOT_PASSWORD` | exemplo local | Senha administrativa do MongoDB no Compose |
| `MONGO_DATABASE` | `dro_audit` | Banco MongoDB usado pela auditoria |
| `MONGO_PORT` | `27017` | Porta publicada localmente |

Segredos não devem ser commitados. Em ambientes reais, injete as variáveis por secret manager ou mecanismo equivalente e use uma URI MongoDB protegida por TLS e credenciais de menor privilégio.

## Retenção e segurança dos documentos

A collection `dro_transaction_audits` usa TTL de **180 dias** no campo `occurredAt`. A collection `dro_error_logs` usa TTL de **365 dias** no mesmo campo. O MongoDB remove os documentos de forma assíncrona quando o índice TTL está ativo; portanto, o TTL é uma política de retenção aproximada, não um agendamento exato por segundo.

Os eventos positivos carregam apenas dados necessários para investigação da operação, como tipo, aggregate, ator, item, quantidade, Bits, comissão, taxa e resumo. Não devem ser adicionados tokens, senhas, cookies, payloads de autenticação ou dados pessoais desnecessários. Documentos de erro usam mensagem sanitizada, campos de validação e stack trace truncado.

Confirme a existência dos índices TTL em ambiente local:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_transaction_audits.getIndexes(); db.dro_error_logs.getIndexes()'
```

## Consultar auditorias positivas

Consulte os eventos mais recentes por operação:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_transaction_audits.find({operation:"SHOP_PURCHASE_COMPLETED"}).sort({occurredAt:-1}).limit(20).pretty()'
```

Consulte uma requisição completa por correlation ID:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_transaction_audits.find({correlationId:"COLE-O-ID-AQUI"}).sort({occurredAt:-1}).pretty()'
```

O `eventId` é único. Para verificar se um evento já foi publicado:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_transaction_audits.findOne({eventId:"COLE-O-EVENT-ID-AQUI"})'
```

## Consultar erros HTTP

A collection de erros permite correlacionar uma resposta HTTP com os logs da aplicação por `correlationId`:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_error_logs.find({correlationId:"COLE-O-ID-AQUI"}).sort({occurredAt:-1}).limit(20).pretty()'
```

Uma resposta de erro deve devolver o header `X-Correlation-Id`. Se o cliente não enviou um identificador válido, a API cria um novo identificador e o inclui tanto na resposta quanto no documento sanitizado do erro.

## Operação do Outbox

Eventos pendentes e falhos são processados automaticamente. O intervalo padrão é de cinco segundos, com backoff exponencial limitado a cinco minutos. O processador consulta apenas `PENDING` e `FAILED`; eventos `PUBLISHED` e `DEAD_LETTER` não voltam para o fluxo automático.

Diagnóstico dos eventos no PostgreSQL:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c 'SELECT status, COUNT(*) FROM audit_outbox_events GROUP BY status ORDER BY status;'
```

Liste os eventos que exigem investigação:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "SELECT event_id, event_type, aggregate_type, aggregate_id, attempts, available_at, last_error FROM audit_outbox_events WHERE status IN ('FAILED','DEAD_LETTER') ORDER BY created_at ASC LIMIT 100;"
```

### Reprocessamento manual de DEAD_LETTER

`DEAD_LETTER` é uma fila de exceção persistida no PostgreSQL. O evento não aparece em `dro_transaction_audits` enquanto não for publicado com sucesso. O procedimento abaixo deve ser executado somente por uma pessoa autorizada, em ambiente controlado e depois de confirmar que o MongoDB está saudável e que a causa da falha foi corrigida.

#### 1. Investigar antes de alterar

Primeiro liste o evento e registre seus dados para auditoria operacional:

```sql
SELECT id,
       event_id,
       event_type,
       aggregate_type,
       aggregate_id,
       correlation_id,
       payload_json,
       status,
       attempts,
       available_at,
       published_at,
       last_error,
       version,
       created_at
FROM audit_outbox_events
WHERE event_id = 'COLE-O-EVENT-ID-AQUI'
  AND status = 'DEAD_LETTER';
```

Não prossiga se `payload_json` contiver token, senha, cookie ou estrutura inválida. Nesse caso, preserve o evento como `DEAD_LETTER`, corrija a origem no código e registre a decisão. Não altere o evento para `PUBLISHED` manualmente.

#### 2. Confirmar a saúde dos destinos

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml ps dro-api dro-postgres dro-mongodb
curl -fsS http://localhost:${API_PORT:-8080}/actuator/health
```

O MongoDB e a API devem estar saudáveis antes da reabertura do evento. Também confirme se já não existe uma auditoria com o mesmo identificador:

```javascript
db.dro_transaction_audits.findOne({ eventId: "COLE-O-EVENT-ID-AQUI" })
```

Se já existir um documento, não reabra o Outbox; investigue a divergência de estado, pois a publicação pode ter ocorrido antes de uma falha ao salvar o status PostgreSQL.

#### 3. Reabrir somente o evento escolhido

Use uma transação e bloqueio de linha para impedir duas recuperações concorrentes. A condição `status = 'DEAD_LETTER'` é obrigatória:

```sql
BEGIN;

SELECT event_id, status, attempts, last_error, version
FROM audit_outbox_events
WHERE event_id = 'COLE-O-EVENT-ID-AQUI'
  AND status = 'DEAD_LETTER'
FOR UPDATE;

UPDATE audit_outbox_events
SET status = 'FAILED',
    attempts = 0,
    available_at = CURRENT_TIMESTAMP,
    published_at = NULL,
    last_error = NULL
WHERE event_id = 'COLE-O-EVENT-ID-AQUI'
  AND status = 'DEAD_LETTER';

COMMIT;
```

Se o `SELECT` não retornar exatamente uma linha, faça `ROLLBACK` e não execute a atualização:

```sql
ROLLBACK;
```

#### 4. Acompanhar o reprocessamento

Confirme que o evento voltou para `FAILED` e está disponível:

```sql
SELECT event_id,
       status,
       attempts,
       available_at,
       available_at <= CURRENT_TIMESTAMP AS disponivel,
       published_at,
       last_error
FROM audit_outbox_events
WHERE event_id = 'COLE-O-EVENT-ID-AQUI';
```

Aguarde o próximo ciclo do processador. O resultado final esperado é:

```text
status = PUBLISHED
published_at preenchido
last_error = null
```

Confirme a auditoria no MongoDB usando o mesmo `eventId`:

```javascript
db.dro_transaction_audits.findOne({ eventId: "COLE-O-EVENT-ID-AQUI" })
```

A operação é considerada recuperada somente quando o documento MongoDB existir e o evento PostgreSQL estiver `PUBLISHED`. Se o evento voltar para `FAILED` ou `DEAD_LETTER`, preserve o erro, não tente indefinidamente e abra uma nova investigação.

## Falha do MongoDB

Quando o MongoDB fica indisponível, existem dois comportamentos diferentes. Uma operação de negócio já confirmada no PostgreSQL não deve ser revertida apenas porque a auditoria não pôde ser entregue; o evento permanece no Outbox e será reprocessado. Já um erro HTTP deve continuar sendo devolvido ao cliente, enquanto o `ErrorAuditService` tenta gravar o documento e usa fallback para o log técnico se o MongoDB estiver indisponível.

O teste operacional deve verificar que a API continua viva, que o PostgreSQL continua aceitando transações e que os eventos positivos não desaparecem. Depois da recuperação do MongoDB, confirme a redução de `PENDING`/`FAILED` e o aumento correspondente de `PUBLISHED`.


Diagnóstico básico:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml ps dro-mongodb
docker compose --env-file docker/.env -f docker/docker-compose.yml logs --tail=200 dro-mongodb
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb mongosh --eval 'db.adminCommand({ping:1})'
```

Depois da recuperação, confirme a redução de `PENDING`/`FAILED` e o aumento correspondente de `PUBLISHED`. Não copie registros de auditoria diretamente para tabelas de jogadores, inventário, equipamentos ou Bits.

## Checklist de incidente

| Verificação | Resultado esperado |
|---|---|
| Healthcheck da API | Serviço saudável e respondendo na porta configurada |
| Healthcheck do PostgreSQL | Banco pronto para conexões |
| Healthcheck do MongoDB | `ping` retorna `ok: 1` |
| Header de correlação | `X-Correlation-Id` presente em respostas de erro |
| Outbox | Eventos `PENDING`/`FAILED` diminuem após a recuperação do MongoDB |
| Dead letter | Nenhum retry automático para `DEAD_LETTER` |
| TTL | Índices de `occurredAt` presentes nas duas collections |
| Cache | Apenas catálogos cacheados; mutações administrativas invalidam as entradas relacionadas |
| Segredos | Nenhuma senha, token ou cookie em payloads de auditoria |

Para parar a stack ao final do diagnóstico, use `docker compose ... down`. Preserve os volumes quando precisar manter os dados para análise; remova-os somente em ambiente local descartável.

## Referência de código

A implementação correspondente está nos pacotes `com.dro.shared.audit`, `com.dro.shared.observability`, `com.dro.shared.exception` e `com.dro.shared.cache`. As operações críticas publicam eventos pelo `TransactionAuditPublisher`, enquanto o `AuditOutboxProcessor` realiza a entrega assíncrona e idempotente no MongoDB.

**Autor:** Manus AI

**Última atualização:** 2026-08-20
