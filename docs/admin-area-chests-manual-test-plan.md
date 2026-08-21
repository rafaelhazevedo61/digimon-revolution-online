# Plano de teste manual — PR #67: Baús da Área

## Objetivo

Validar a tela administrativa que permite alterar o vínculo entre um Baú da Área e uma Loot Table nomeada ativa.

> O fluxo de jogo continua sendo `Missão → Baú da Área → abertura → loot`. A Missão escolhe o baú; a tela de Baús escolhe a Loot Table usada na abertura.

## Pré-requisitos

Os PRs #65 e #66 devem estar disponíveis na branch de teste, pois a tela de Baús usa a listagem de Loot Tables e a navegação administrativa atualizada.

```powershell
cd backend
.\mvnw.cmd -q -DskipTests compile
cd ..\admin-frontend
py -m http.server 3001
```

Acesse `http://localhost:3001` com um JWT de administrador.

## Cenários obrigatórios

| Cenário | Procedimento | Resultado esperado |
|---|---|---|
| Navegação | Abrir o menu Operação | A opção **Baús da Área** aparece entre Loot Tables e Bosses. |
| Listagem | Abrir Baús da Área | Os baús aparecem com nome, código, Loot Table, status e negociação. |
| Filtro | Marcar **Apenas ativos** | A API usa `GET /admin/chests?activeOnly=true` e oculta baús inativos. |
| Edição | Abrir um baú e alterar o nome ou descrição | HTTP 200; os dados públicos são atualizados. |
| Vínculo | Selecionar uma Loot Table ativa criada no PR #65 e salvar | HTTP 200; a Loot Table vinculada muda na listagem. |
| Negociação | Marcar/desmarcar **Negociável** | O status é salvo sem alterar o código do item do inventário. |
| Desativação | Clicar em **Desativar** | HTTP 200; o baú permanece no catálogo, mas fica Inativo. |
| Ativação | Clicar em **Ativar** | HTTP 200 somente se a Loot Table vinculada estiver ativa. |
| Loot Table inativa | Desativar a Loot Table e tentar ativar o baú | A API rejeita a operação com erro de conflito; o baú continua inativo. |
| Loot Table inexistente | Enviar manualmente um código inexistente | A API rejeita a atualização e não altera o vínculo anterior. |
| Não administrador | Usar JWT de jogador comum | A API retorna HTTP 403. |
| Missão | Voltar ao menu Missões | A combobox de Baú continua carregando o baú e exibindo a Loot Table recém-vinculada. |
| Responsividade | Testar desktop e viewport mobile | A tabela e o modal continuam utilizáveis sem corte ou sobreposição. |

## Requests principais

```http
GET /admin/chests?activeOnly=false
GET /admin/chests/CHEST_AREA_NATIVE_FOREST
PUT /admin/chests/CHEST_AREA_NATIVE_FOREST
PATCH /admin/chests/CHEST_AREA_NATIVE_FOREST/toggle-active
```

O corpo de atualização deve conter o código de uma Loot Table ativa:

```json
{
  "name": "Baú Floresta Nativa",
  "description": "Baú entregue nas missões da Floresta Nativa.",
  "icon": "chest-native-forest",
  "lootTableCode": "LOOT_TEST_ADMIN",
  "tradable": true,
  "active": true
}
```

## Validação no PostgreSQL

Após salvar o vínculo, confirme:

```sql
SELECT
    cd.code AS chest_code,
    cd.name AS chest_name,
    cd.active AS chest_active,
    cd.tradable,
    lt.code AS loot_table_code,
    lt.name AS loot_table_name,
    lt.active AS loot_table_active,
    cd.updated_by,
    cd.updated_at
FROM chest_definitions cd
JOIN loot_tables lt ON lt.id = cd.loot_table_id
WHERE cd.code = 'CHEST_AREA_NATIVE_FOREST';
```

Depois de alterar o vínculo por uma missão, valide a relação:

```sql
SELECT
    md.id AS mission_id,
    md.name AS mission_name,
    cd.code AS chest_code,
    lt.code AS loot_table_code
FROM mission_definitions md
JOIN chest_definitions cd ON cd.id = md.chest_definition_id
JOIN loot_tables lt ON lt.id = cd.loot_table_id
WHERE md.id = 'MISSION_1';
```

As alterações devem gerar eventos `ADMIN_CHEST_UPDATED`, `ADMIN_CHEST_ACTIVATED` ou `ADMIN_CHEST_DEACTIVATED` no Outbox transacional, com `aggregate_type = 'ChestDefinition'` e `aggregate_id` igual ao código do baú.
