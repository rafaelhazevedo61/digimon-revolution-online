# Plano de teste manual — Correção e organização do inventário

## Objetivo

Validar que as linhas históricas do mesmo item são apresentadas como uma única quantidade lógica, que digitamas e incubadoras não possuem mais o botão de uso genérico e que a listagem segue uma ordem estável.

## Pré-requisitos

Inicie o backend com a migration V115 disponível. Ela corrige `usable` para `false` nas categorias `DIGITAMA` e `INCUBATOR`, amplia o `max_stack` desses itens para 99 e consolida linhas antigas do mesmo Digimon e código de item. Migrations anteriores não devem ser editadas.

## Inventário

1. Acesse **Inventário → Itens**.
2. Confirme que existe apenas uma linha para cada item lógico, mesmo que o banco tivesse uma linha legada e uma linha catalogada.
3. Confirme que a quantidade é a soma das linhas anteriores. Por exemplo, duas linhas de Poção Pequena com quantidades 4 e 8 devem aparecer como uma linha com quantidade 12.
4. Confirme a ordem padrão: **Consumíveis**, **Materiais**, **Evolução**, **Fragmentos**, **Digitamas**, **Incubadoras** e **Baús**. Dentro de cada categoria, os nomes devem estar em ordem alfabética, sem depender da ordem de inserção no banco.
5. Confirme que poções e pedras continuam exibindo **Usar** quando forem itens consumíveis permitidos.
6. Confirme que digitamas e incubadoras não exibem **Usar**. Esses itens devem ser selecionados na tela **Incubação**.
7. Confirme que Baús continuam exibindo **Abrir** e que o fluxo de abertura não foi alterado.

## Incubação

Acesse **Incubação** e confirme que cada tipo de digitama e incubadora aparece uma única vez, com a quantidade consolidada. Selecione uma digitama e uma incubadora, inicie a incubação e confirme que os consumos ocorrem normalmente.

## Endpoint legado

Mesmo que uma requisição manual seja enviada para o endpoint antigo, digitamas e incubadoras devem ser rejeitados:

```http
POST /inventory/use
Content-Type: application/json

{"itemType":"INCUBATOR_EPIC"}
```

A resposta deve ser HTTP 400 com mensagem informando que esses itens devem ser usados pela tela de incubação. O endpoint não deve alterar quantidade, experiência ou inventário.

## Queries PostgreSQL

Conferir as definições de catálogo:

```sql
SELECT code, category, usable, max_stack
FROM item_definitions
WHERE code IN (
    'POTION_SMALL',
    'DIGITAMA_FIRE',
    'DIGITAMA_WATER',
    'DIGITAMA_NATURE',
    'INCUBATOR_COMMON',
    'INCUBATOR_RARE',
    'INCUBATOR_EPIC'
)
ORDER BY category, code;
```

As categorias `DIGITAMA` e `INCUBATOR` devem possuir `usable = false`, enquanto `POTION_SMALL` deve continuar utilizável.

Para conferir possíveis duplicidades catalogadas:

```sql
SELECT
    digimon_id,
    item_definition_id,
    COUNT(*) AS rows_found,
    SUM(quantity) AS total_quantity,
    STRING_AGG(id::text, ', ' ORDER BY id) AS inventory_item_ids
FROM inventory_items
WHERE item_definition_id IS NOT NULL
GROUP BY digimon_id, item_definition_id
HAVING COUNT(*) > 1;
```

Essa consulta deve retornar zero linhas.

Para conferir linhas legadas restantes dos itens padronizados:

```sql
SELECT
    ii.digimon_id,
    ii.item_type,
    COUNT(*) AS rows_found,
    SUM(ii.quantity) AS total_quantity
FROM inventory_items ii
WHERE ii.item_definition_id IS NULL
  AND ii.item_type IN (
      'POTION_SMALL',
      'TRAINING_STONE',
      'DATA_CORE',
      'DIGITAMA_STARTER',
      'DIGITAMA_FIRE',
      'DIGITAMA_WATER',
      'DIGITAMA_NATURE',
      'INCUBATOR_COMMON',
      'INCUBATOR_RARE',
      'INCUBATOR_EPIC'
  )
GROUP BY ii.digimon_id, ii.item_type
ORDER BY ii.digimon_id, ii.item_type;
```

Essa consulta deve retornar zero linhas para os itens catalogados pela V115. A quantidade lógica deve estar em uma linha com `item_definition_id` preenchido.

## Critérios de aceite

- [ ] Incubadora Épica e Poção Pequena não aparecem duas vezes no inventário.
- [ ] A quantidade consolidada corresponde à soma das linhas anteriores.
- [ ] A ordenação do inventário permanece estável após recarregar a página.
- [ ] Digitamas não possuem botão **Usar**.
- [ ] Incubadoras não possuem botão **Usar**.
- [ ] A tela de incubação lista cada opção uma única vez.
- [ ] Poções e pedras continuam utilizáveis quando permitido.
- [ ] Baús continuam com o botão **Abrir**.
- [ ] O endpoint legado rejeita digitamas e incubadoras.
- [ ] Não existem duplicidades catalogadas após a V115.
