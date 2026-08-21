# Plano de teste manual — PR #65: Loot Tables administrativas

## Objetivo

Validar a entrega vertical do painel administrativo de Loot Tables: listagem, catálogo de itens, criação, edição, ativação/desativação, validações e auditoria via Transactional Outbox.

A pool continua sendo uma configuração exclusiva do painel. Jogadores comuns não devem receber acesso aos endpoints `/admin/loot-tables` nem visualizar a composição da pool no jogo.

## Pré-requisitos

A aplicação deve estar executando com PostgreSQL atualizado e com os PRs de baús já aplicados. É necessário possuir um JWT de um jogador com `UserType.ADMIN` e abrir o painel em `admin-frontend`.

```powershell
cd admin-frontend
py -m http.server 3001
```

Acesse `http://localhost:3001`, informe o token administrativo e abra a opção **Loot Tables**.

## Cenários obrigatórios

| Cenário | Procedimento | Resultado esperado |
|---|---|---|
| Listagem | Abrir **Loot Tables** | As tabelas são carregadas com código, nome, intervalo de itens, quantidade de entradas, status e responsável pela última alteração. |
| Filtro de ativas | Marcar **Apenas ativas** | Tabelas inativas deixam de aparecer. |
| Catálogo | Clicar em **Nova Loot Table** | O editor oferece somente itens retornados pelo catálogo oficial; não há necessidade de digitar código de item. |
| Criação válida | Informar código, nome, quatro pesos positivos e pelo menos uma entrada válida | HTTP 201, tabela aparece na listagem e auditoria é enfileirada. |
| Material nomeado | Selecionar um item de categoria `EVOLUTION_MATERIAL` | O request usa `itemType=EVOLUTION_MATERIAL` e `materialCode` igual ao código catalogado. |
| Baú | Selecionar item de categoria `CHEST` | O request usa `itemType=LOOT_CHEST` e `materialCode` igual ao código do baú. |
| Edição | Alterar nome, pesos, faixa de quantidade ou entradas | HTTP 200, valores atualizados aparecem no editor e `updatedBy`/`updatedAt` mudam. |
| Desativação | Clicar em **Desativar** | O status passa para **Inativa** sem apagar a tabela ou seu histórico. |
| Ativação | Clicar em **Ativar** | O status volta para **Ativa** e a tabela aparece novamente no filtro de ativas. |
| Regras de quantidade | Informar mínimo maior que máximo, máximo maior que `max_stack` ou mínimo de tipos maior que o número de entradas | A tela bloqueia o envio ou a API retorna HTTP 400. |
| Pesos incompletos | Remover uma raridade ou usar peso zero/negativo | A operação é rejeitada; as quatro raridades oficiais são obrigatórias. |
| Item inexistente | Enviar manualmente um `materialCode` inexistente | A API retorna HTTP 409 e nada é persistido. |
| Código inválido | Usar espaços, hífen ou letras minúsculas no código | A API retorna HTTP 400 por formato inválido. |
| Não administrador | Repetir uma chamada com JWT de jogador comum | A API retorna HTTP 403 e não altera dados. |

## Requests principais

```http
GET /admin/loot-tables
GET /admin/loot-tables/catalog/items
POST /admin/loot-tables
GET /admin/loot-tables/{code}
PUT /admin/loot-tables/{code}
PATCH /admin/loot-tables/{code}/toggle-active
```

Exemplo mínimo de criação:

```json
{
  "code": "LOOT_TEST_ADMIN",
  "name": "Loot Table de Teste",
  "description": "Pool criada para validação do painel.",
  "minItems": 1,
  "maxItems": 2,
  "rarityWeights": [
    { "rarity": "COMMON", "weight": 70 },
    { "rarity": "RARE", "weight": 20 },
    { "rarity": "EPIC", "weight": 8 },
    { "rarity": "LEGENDARY", "weight": 2 }
  ],
  "entries": [
    {
      "rarity": "COMMON",
      "itemType": "TRAINING_STONE",
      "materialCode": null,
      "weight": 50,
      "minQuantity": 1,
      "maxQuantity": 3,
      "active": true
    },
    {
      "rarity": "RARE",
      "itemType": "EVOLUTION_MATERIAL",
      "materialCode": "FRAGMENT_AGUMON",
      "weight": 35,
      "minQuantity": 1,
      "maxQuantity": 5,
      "active": true
    }
  ],
  "active": true
}
```

## Auditoria e PostgreSQL

Após criação, edição, ativação ou desativação, confirme que a operação foi registrada no Outbox:

```sql
SELECT
    id,
    event_id,
    event_type,
    aggregate_type,
    aggregate_id,
    status,
    created_at
FROM audit_outbox
WHERE event_type LIKE 'ADMIN_LOOT_TABLE_%'
ORDER BY created_at DESC
LIMIT 20;
```

Confirme a configuração persistida:

```sql
SELECT
    lt.code,
    lt.name,
    lt.active,
    lt.min_items,
    lt.max_items,
    COUNT(DISTINCT lte.id) AS entries,
    COUNT(DISTINCT ltrw.id) AS rarity_weights
FROM loot_tables lt
LEFT JOIN loot_table_entries lte ON lte.loot_table_id = lt.id
LEFT JOIN loot_table_rarity_weights ltrw ON ltrw.loot_table_id = lt.id
GROUP BY lt.id, lt.code, lt.name, lt.active, lt.min_items, lt.max_items
ORDER BY lt.name;
```

O teste é aprovado quando o CRUD administrativo funciona sem permitir itens fora do catálogo, as regras de domínio são respeitadas, a autorização rejeita jogadores comuns e cada alteração gera uma auditoria positiva no Transactional Outbox.
