# Plano de teste manual — PR #66: Missões com Baús da Área

## Objetivo

Validar que o painel administrativo deixou de configurar o loot legado diretamente na Missão e passou a operar com o vínculo `Missão → Baú da Área → Loot Table nomeada`.

## Pré-requisitos

O PR #65 deve estar disponível, pois o painel usa o catálogo administrativo de Loot Tables. Execute o backend atualizado e inicie o painel:

```powershell
cd admin-frontend
py -m http.server 3001
```

Acesse `http://localhost:3001` com um usuário administrador.

## Navegação

A barra lateral deve apresentar os grupos nesta ordem operacional:

| Grupo | Ordem |
|---|---|
| Operação | Dashboard, Missões, Loot Tables, Bosses |
| Catálogo | Catálogo de Itens, Informações de Digimon, Linhas Evolutivas, Templates de Equipamentos, Produtos da Loja |
| Administração | Jogadores, Comunicados do Correio, Premiações de Eventos |
| Ferramentas | Grant / XP, Simulador de Equipamentos, Simulador de Digimon |

Ao abrir **Missões**, o título e os filtros devem estar em português. O menu não deve sugerir configuração direta de recompensas aleatórias.

## Cenários obrigatórios

| Cenário | Procedimento | Resultado esperado |
|---|---|---|
| Catálogo de Baús | Abrir Missões e expandir o filtro **Baú da Área** | A combobox lista baús ativos com nome amigável. |
| Listagem | Selecionar um baú | A lista mostra somente missões vinculadas ao baú escolhido. |
| Tabela | Observar uma missão | As colunas mostram **Baú da Área** e **Loot Table**, não `Rewards` e `Loot`. |
| Nova Missão | Clicar em **Nova Missão** | O modal apresenta seleção obrigatória de Baú da Área e informa a Loot Table vinculada. |
| Criação válida | Preencher os campos e salvar com um baú selecionado | HTTP 201; a missão aparece na listagem com o baú e a Loot Table corretos. |
| Edição | Editar uma missão e trocar o baú | HTTP 200; o vínculo anterior é substituído e os dados legados permanecem vazios. |
| Loot centralizado | Clicar no nome/menu Loot Tables | A composição da pool é editada no painel de Loot Tables, não no modal de Missões. |
| Baú ausente | Tentar salvar sem selecionar um baú | O frontend bloqueia o envio com mensagem clara. |
| Loot legado | Enviar manualmente `rewards`, `lootChances` ou `lootItems` não vazios junto com `chestCode` | A API retorna HTTP 400 e não salva configuração ambígua. |
| Baú inválido | Enviar `chestCode` inexistente ou inativo | A API retorna HTTP 404. |
| Responsividade | Testar desktop e viewport mobile | Filtros e modal continuam utilizáveis sem sobreposição ou corte. |

## Contrato esperado

O corpo de criação/edição deve usar o novo formato:

```json
{
  "name": "Missão Administrativa de Teste",
  "description": "Missão vinculada a um Baú da Área.",
  "area": "NATIVE_FOREST",
  "requiredStage": "ROOKIE",
  "requiredLevel": 1,
  "baseXp": 100,
  "baseBits": 50,
  "energyCost": 5,
  "durationSeconds": 60,
  "chestCode": "CHEST_AREA_NATIVE_FOREST",
  "rewards": [],
  "lootChances": [],
  "lootItems": []
}
```

A Loot Table não deve ser duplicada no request da Missão. Para alterar pesos, itens ou faixas de quantidade, use a tela administrativa de Loot Tables.

## Validação no PostgreSQL

Após criar ou editar uma missão, confirme o vínculo:

```sql
SELECT
    md.id,
    md.name,
    md.chest_definition_id,
    cd.code AS chest_code,
    cd.name AS chest_name,
    lt.code AS loot_table_code,
    lt.name AS loot_table_name,
    COUNT(DISTINCT mr.id) AS legacy_rewards,
    COUNT(DISTINCT mlc.id) AS legacy_chances,
    COUNT(DISTINCT mli.id) AS legacy_items
FROM mission_definitions md
LEFT JOIN chest_definitions cd ON cd.id = md.chest_definition_id
LEFT JOIN loot_tables lt ON lt.id = cd.loot_table_id
LEFT JOIN mission_rewards mr ON mr.mission_id = md.id
LEFT JOIN mission_loot_chances mlc ON mlc.mission_id = md.id
LEFT JOIN mission_loot_items mli ON mli.mission_id = md.id
WHERE md.id = 'MISSION_ADMIN_TEST'
GROUP BY md.id, md.name, md.chest_definition_id, cd.code, cd.name, lt.code, lt.name;
```

Para uma missão criada pelo novo painel, o resultado esperado é `chest_code` e `loot_table_code` preenchidos e todas as contagens de estruturas legadas iguais a zero.
