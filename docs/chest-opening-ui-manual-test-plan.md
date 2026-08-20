# Teste manual — abertura de baú pela interface

## Objetivo

Confirmar que o botão **Abrir** do inventário usa o fluxo transacional `POST /inventory/chests/open`, e não o endpoint legado `POST /inventory/use`.

## Fluxo esperado

Ao visualizar um item cuja definição possui categoria `CHEST`, o inventário deve exibir o botão **Abrir**. Ao clicar nele, o frontend deve enviar o código da definição do baú e um `requestId` novo:

```json
{
  "chestCode": "CHEST_MISSION_NATIVE_FOREST",
  "requestId": "UUID_UNICO_DA_ABERTURA"
}
```

A interface deve abrir um modal com a raridade, os itens recebidos e as quantidades. Depois da resposta, o inventário deve ser recarregado e a quantidade do baú deve diminuir uma unidade.

O `requestId` é gerado para cada abertura nova. O bloqueio contra duplo clique impede duas requisições simultâneas durante o processamento.

## Passos

1. Atualize a aplicação com a branch do PR.
2. Faça login e selecione um Digimon ativo.
3. Garanta que o inventário possua pelo menos um baú.
4. Abra a tela **Inventário**.
5. Confirme que o item aparece como **Baú** e possui o botão **Abrir**.
6. Clique em **Abrir** uma vez.
7. Confirme que o modal mostra a raridade, o nome dos itens e as quantidades.
8. Feche o modal e confirme que a quantidade do baú diminuiu uma unidade.
9. Confirme que os itens recebidos aparecem no inventário, respeitando a quantidade informada no modal.
10. Observe a aba Network do navegador e confirme uma requisição `POST /inventory/chests/open` com `chestCode` e `requestId`.
11. Confirme que não foi chamada a rota legada `POST /inventory/use` para o baú.

## Consultas SQL

```sql
SELECT
    co.id,
    co.request_id,
    co.player_id,
    cd.code AS chest_code,
    co.rarity,
    co.source,
    co.opened_at
FROM chest_openings co
JOIN chest_definitions cd ON cd.id = co.chest_definition_id
ORDER BY co.opened_at DESC;
```

```sql
SELECT
    coi.chest_opening_id,
    coi.item_type,
    coi.material_code,
    coi.quantity
FROM chest_opening_items coi
JOIN chest_openings co ON co.id = coi.chest_opening_id
ORDER BY co.opened_at DESC, coi.id;
```

Para confirmar a entrega no inventário:

```sql
SELECT
    ii.digimon_id,
    ii.item_type,
    idf.code AS item_code,
    idf.name AS item_name,
    ii.quantity
FROM inventory_items ii
LEFT JOIN item_definitions idf ON idf.id = ii.item_definition_id
WHERE ii.digimon_id = 'UUID_DO_DIGIMON'
ORDER BY ii.id;
```

O comportamento correto é uma linha nova em `chest_openings`, uma ou mais linhas correspondentes em `chest_opening_items`, redução de uma unidade do baú e crédito dos itens recebidos no inventário.
