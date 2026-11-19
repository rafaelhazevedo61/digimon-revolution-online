# Roteiro de testes manuais da observabilidade

Este roteiro valida a cadeia formada pelas PRs **#51 a #57**. Execute-o em um ambiente local descartável ou de desenvolvimento, nunca contra dados reais de jogadores. Antes de começar, confirme que as PRs foram mergeadas na ordem **#51 → #52 → #53 → #54 → #55 → #56 → #57** ou que a branch de teste contém essa cadeia completa.

## 1. Preparação do ambiente

Na raiz do repositório, crie o arquivo de ambiente e substitua todos os valores de exemplo:

```bash
cp docker/.env.example docker/.env
# edite docker/.env e altere as senhas locais
set -a
source docker/.env
set +a
docker compose --env-file docker/.env -f docker/docker-compose.yml up --build -d
```

Verifique os três serviços:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml ps
```

| Serviço | Resultado esperado |
|---|---|
| `dro-postgres` | `healthy` |
| `dro-mongodb` | `healthy` |
| `dro-api` | `running` e saudável após migrations |

Confirme também os logs de inicialização:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml logs --tail=200 dro-api
```

Não deve haver falha de conexão permanente com PostgreSQL ou MongoDB, erro de migration Flyway ou falha para criar o contexto Spring.

## 2. Confirmar healthchecks e conectividade

Execute o healthcheck da API:

```bash
curl -i "http://localhost:${API_PORT:-8080}/actuator/health"
```

O resultado esperado é HTTP `200` com status de saúde `UP`. Confirme o PostgreSQL:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c 'SELECT 1;'
```

Confirme o MongoDB:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.adminCommand({ping:1})'
```

O MongoDB deve retornar `ok: 1`. Se a API não iniciar, corrija primeiro o healthcheck ou as credenciais; não avance para os testes funcionais.

## 3. Compra na Loja e auditoria positiva

Use a interface do jogo para consultar a Loja e escolha um produto barato que o jogador de teste possa comprar. Antes da compra, abra um terminal para acompanhar a quantidade de eventos:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_transaction_audits.countDocuments({operation:"SHOP_PURCHASE_COMPLETED"})'
```

Com a interface, faça uma compra de quantidade `1`. Alternativamente, usando um token válido:

```bash
curl -i -X POST "http://localhost:${API_PORT:-8080}/shop/buy" \
  -H "Authorization: Bearer COLE-O-TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"productCode":"COLE-O-CODIGO-DO-PRODUTO","quantity":1}'
```

A compra deve alterar Bits e inventário no PostgreSQL e retornar sucesso. Aguarde o intervalo configurado em `DRO_AUDIT_OUTBOX_FIXED_DELAY_MS` — cinco segundos por padrão — e consulte o MongoDB:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_transaction_audits.find({operation:"SHOP_PURCHASE_COMPLETED"}).sort({occurredAt:-1}).limit(5).pretty()'
```

O documento deve conter `eventId`, `occurredAt`, `correlationId`, `actorId`, operação de compra e payload sanitizado. O inventário e o saldo devem continuar sendo consultados no PostgreSQL; o documento MongoDB não é uma cópia oficial desses dados.

## 4. Erro HTTP e correlation ID

Force um erro controlado, por exemplo, enviando uma compra com produto inexistente ou quantidade inválida:

```bash
curl -i -X POST "http://localhost:${API_PORT:-8080}/shop/buy" \
  -H "Authorization: Bearer COLE-O-TOKEN" \
  -H 'Content-Type: application/json' \
  -H 'X-Correlation-Id: manual-observability-001' \
  -d '{"productCode":"produto-inexistente","quantity":1}'
```

Valide os seguintes resultados:

| Verificação | Resultado esperado |
|---|---|
| Status HTTP | Resposta de erro controlado, sem stack trace para o cliente |
| Header | `X-Correlation-Id: manual-observability-001` |
| Corpo | Campo `correlationId` com o mesmo valor |
| MongoDB | Documento correspondente em `dro_error_logs` |
| Segurança | Nenhum token, senha ou cookie no documento |

Consulte o erro:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_error_logs.find({correlationId:"manual-observability-001"}).sort({occurredAt:-1}).limit(5).pretty()'
```

O stack trace, se presente, deve estar truncado. A operação de negócio não deve ser derrubada por falha de gravação da auditoria de erro.

## 5. Fallback quando MongoDB está indisponível

Este teste deve ser feito somente em ambiente local. Pare temporariamente o MongoDB:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml stop dro-mongodb
```

Force um erro HTTP ou consulte um endpoint público da API. O cliente ainda deve receber uma resposta HTTP coerente. A ausência do MongoDB não deve causar uma exceção adicional nem impedir a resposta funcional da aplicação. Verifique os logs da API:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml logs --tail=200 dro-api
```

O log técnico deve registrar que a auditoria não pôde ser persistida. Em seguida, recupere o MongoDB:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml start dro-mongodb
```

Aguarde o healthcheck voltar para `healthy` e confirme que a API continua conectada. As auditorias positivas pendentes no Outbox devem ser processadas depois da recuperação.

## 6. Publicação assíncrona pelo Outbox

Faça uma transação positiva, como uma compra de Loja, e consulte o PostgreSQL imediatamente:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "SELECT event_id, status, attempts, available_at FROM audit_outbox_events ORDER BY created_at DESC LIMIT 10;"
```

Logo após a compra, o evento pode aparecer como `PENDING`. Após o próximo ciclo do processador, ele deve aparecer como `PUBLISHED`, e o respectivo `eventId` deve existir em `dro_transaction_audits`:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "SELECT event_id, status, attempts, published_at FROM audit_outbox_events ORDER BY created_at DESC LIMIT 10;"
```

A publicação repetida do mesmo ciclo não deve criar documentos duplicados, pois `eventId` é único no MongoDB e a publicação é idempotente.

## 7. Dead letter e retry

Para validar o fluxo sem depender de uma indisponibilidade prolongada, use um evento de teste criado por uma transação local e interrompa o MongoDB antes do processamento. Observe o aumento de `attempts` e o reagendamento em `available_at`:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-postgres \
  psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c "SELECT event_id, status, attempts, available_at, last_error FROM audit_outbox_events WHERE status IN ('FAILED','DEAD_LETTER') ORDER BY created_at DESC LIMIT 20;"
```

O backoff deve aumentar entre tentativas, respeitando o limite máximo configurado no processador. Depois de atingir o limite, o evento deve ficar em `DEAD_LETTER` e não ser selecionado novamente pelo processador.

Não altere dados de produção para simular esse caso. Para recuperar um evento em ambiente de desenvolvimento, siga o procedimento documentado em `docs/observability-guide.md`, retorne-o explicitamente para `FAILED` e confirme a publicação após o MongoDB voltar.

## 8. Invalidação do cache de catálogo

Abra a tela da Loja ou consulte `GET /shop` e registre o nome, preço e disponibilidade de um produto. Edite o mesmo produto pelo painel administrativo, alterando um campo visível, como preço ou status ativo. Atualize a Loja.

O resultado esperado é que a próxima leitura mostre os dados novos, sem manter o valor antigo pelo TTL. A alteração administrativa deve invalidar `shopCatalog`. Repita o procedimento para catálogos de definições de item e templates de equipamento, quando houver telas administrativas correspondentes.

O Caffeine não deve ser usado para validar saldo, inventário, equipamento equipado, anúncios, resgates ou Digimon ativo. Para esses dados, confirme sempre o comportamento transacional e a consulta oficial ao PostgreSQL.

## 9. TTL das collections

Confirme que os índices foram criados:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml exec dro-mongodb \
  mongosh --username "$MONGO_ROOT_USERNAME" --password "$MONGO_ROOT_PASSWORD" \
  --authenticationDatabase admin "$MONGO_DATABASE" \
  --quiet --eval 'db.dro_transaction_audits.getIndexes(); db.dro_error_logs.getIndexes()'
```

Procure um índice de `occurredAt` com `expireAfterSeconds` equivalente a 180 dias na collection positiva e 365 dias na collection de erros. A remoção de um documento não é instantânea no momento exato do vencimento, pois o monitor TTL trabalha em ciclos internos do MongoDB.

## 10. Critérios de aprovação

A cadeia está aprovada quando a stack inicia com os três healthchecks, a compra atualiza o PostgreSQL e gera auditoria positiva no MongoDB, erros retornam o correlation ID, falhas de MongoDB não derrubam a operação, o Outbox reprocessa eventos após a recuperação, o limite de retries envia falhas persistentes para `DEAD_LETTER`, alterações administrativas invalidam os caches de catálogo e os índices TTL estão presentes.

Ao terminar, encerre a stack sem remover os volumes se precisar preservar evidências:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml down
```

Use `down -v` somente se o ambiente for descartável e os dados de teste não forem mais necessários.

**Autor:** Manus AI

**Última atualização:** 2026-08-19
