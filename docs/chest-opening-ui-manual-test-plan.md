# Teste manual — abertura de baú pela interface

## Objetivo

Confirmar que o botão **Abrir** do inventário usa o fluxo transacional `POST /inventory/chests/open`, e não o endpoint legado `POST /inventory/use`. O roteiro também cobre o modal específico exibido após o resgate de uma missão.

## Modal de recompensa da missão

Ao clicar em **Resgatar** em uma missão concluída, a interface deve abrir um modal próprio de conclusão, em vez de exibir somente um push no topo. O modal deve apresentar o nome da missão, a experiência recebida, os Bits, eventual `Level Up!` e todas as recompensas de item retornadas pelo backend. Quando a recompensa for um Baú da Área, ela deve aparecer identificada como baú, com a quantidade e a orientação para abrir o item pelo Inventário.

O push de sucesso não deve substituir esse modal. Mensagens de erro continuam podendo usar o push global da aplicação.

## Fluxo esperado

Ao visualizar um item cuja definição possui categoria `CHEST`, o inventário deve exibir o botão **Abrir**. Ao clicar nele, o frontend deve enviar o código da definição do baú e um `requestId` novo:

```json
{
  "chestCode": "CHEST_MISSION_NATIVE_FOREST",
  "requestId": "UUID_UNICO_DA_ABERTURA"
}
```

A interface deve abrir um modal com os itens recebidos, suas quantidades e a raridade individual de cada item. Uma abertura pode combinar Common, Rare, Epic e Legendary. Depois da resposta, o inventário deve ser recarregado e a quantidade do baú deve diminuir uma unidade.

O `requestId` é gerado para cada abertura nova. O bloqueio contra duplo clique impede duas requisições simultâneas durante o processamento.

## Passos

1. Atualize a aplicação com a branch do PR.
2. Faça login e selecione um Digimon ativo.
3. Conclua ou resgate uma missão disponível e confirme que aparece o modal **Missão concluída!**.
4. No modal, confirme XP, Bits, eventual Level Up e a recompensa **Baú** com sua quantidade.
5. Feche o modal e garanta que o inventário possua pelo menos um baú.
6. Abra a tela **Inventário**.
7. Confirme que o item aparece como **Baú** e possui o botão **Abrir**.
8. Clique em **Abrir** uma vez.
9. Confirme que o modal de abertura mostra a raridade, o nome dos itens e as quantidades.
10. Feche o modal e confirme que a quantidade do baú diminuiu uma unidade.
11. Confirme que os itens recebidos aparecem no inventário, respeitando a quantidade informada no modal.
12. Observe a aba Network do navegador e confirme uma requisição `POST /inventory/chests/open` com `chestCode` e `requestId`.
13. Confirme que não foi chamada a rota legada `POST /inventory/use` para o baú.
14. Para uma Loot Table com uma entrada ativa em cada raridade e `minItems = 2`, confirme que a abertura não falha por haver apenas uma entrada em cada pool e que cada item do modal exibe sua própria raridade.

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
    coi.rarity,
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
