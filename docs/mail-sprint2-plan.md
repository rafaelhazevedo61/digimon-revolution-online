# Sprint 2 — Notificações da Casa de Leilões no Correio

## Objetivo

Integrar a Casa de Leilões ao Correio do jogador para que eventos importantes do marketplace sejam comunicados de forma persistente, mesmo quando o jogador não estiver online no momento em que o evento ocorrer.

## Eventos previstos

A Sprint 2 deve gerar mensagens automáticas para o jogador quando ocorrerem os seguintes eventos:

| Evento | Destinatário | Informações esperadas |
|---|---|---|
| Venda concluída | Vendedor | Item vendido, quantidade, valor bruto, comissão cobrada e valor líquido recebido. |
| Compra concluída | Comprador | Item comprado, quantidade, valor pago e anúncio de origem. |
| Anúncio cancelado | Vendedor | Item devolvido, quantidade e confirmação do cancelamento. |
| Anúncio expirado | Vendedor | Item devolvido, quantidade e motivo da expiração. |
| Devolução pendente | Vendedor | Explicação de que o item aguarda processamento e orientação para acompanhar o Correio. |

## Regras de segurança e consistência

As mensagens devem ser criadas no backend junto da operação que confirma o evento do marketplace. A entrega precisa ser idempotente: repetir a mesma operação ou reprocessar uma transação não pode gerar mensagens duplicadas. O campo `delivery_key` deve identificar unicamente cada notificação.

Os campos `source_type` e `source_id` devem permitir rastrear que a mensagem foi gerada pela Casa de Leilões e qual anúncio ou operação originou o comunicado. Os dados exibidos ao jogador devem ser derivados dos valores efetivamente confirmados pelo backend, nunca de informações enviadas pelo frontend.

A Sprint 2 não deve adicionar anexos, Bits, itens resgatáveis ou ações dentro da mensagem. Nesta etapa, o Correio será apenas o canal persistente de comunicação sobre o resultado de uma operação já concluída pela Casa de Leilões.

## Critérios de aceite

1. Cada evento suportado gera no máximo uma mensagem correspondente por operação.
2. A mensagem aparece na Entrada do jogador correto e incrementa o contador de não lidas.
3. A mensagem também pode ser consultada em Enviadas quando houver um remetente de sistema definido pela implementação.
4. A exclusão da mensagem continua sendo independente entre as partes, conforme o MVP do Correio.
5. O texto é exibido em português e apresenta valores coerentes com o resultado da operação.
6. Falhas na geração da notificação não podem confirmar uma operação financeira ou de inventário parcialmente; a estratégia transacional deve ser definida antes da implementação de cada fluxo.
7. Existem testes unitários para a chave idempotente e testes focados para cada evento integrado.

## Fora do escopo

A Sprint 2 não inclui resposta direta, conversas encadeadas, envio manual de anexos, transferência de Bits pelo Correio, resgate de itens dentro da mensagem ou suporte a equipamentos na Casa de Leilões.
