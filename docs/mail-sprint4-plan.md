# Sprint 4 — Premiações de eventos no Correio

## Objetivo

Permitir que premiações de eventos sejam entregues pelo Correio como mensagens persistentes, com um botão seguro de resgate e garantia de que cada prêmio seja entregue no máximo uma vez.

## Escopo

A Sprint 4 cobre premiações compostas por Bits e/ou um item do catálogo para um jogador específico. O prêmio será criado por uma operação administrativa autenticada e aparecerá como uma mensagem do tipo `EVENT` na Entrada do jogador.

A mensagem terá a ação `EVENT_REWARD_CLAIM`. O jogador poderá clicar em **Resgatar prêmio**. O backend revalidará o estado do prêmio, a validade, o destinatário e a existência de Digimon ativo antes de entregar Bits ao Digimon ativo e/ou o item ao inventário. A ação será transacional e idempotente.

## Regras

O prêmio terá validade configurável, com limite máximo de 30 dias. Um prêmio pendente, resgatado ou expirado não poderá ser entregue novamente. O jogador não poderá alterar o conteúdo da mensagem nem o prêmio associado à ação.

Nesta primeira implementação, o inventário de prêmio utiliza os tipos de item já suportados pelo jogo e o saldo de Bits será entregue ao Digimon ativo do jogador. Se não houver Digimon ativo, o resgate será bloqueado sem consumir o prêmio.

## Fora do escopo

A Sprint 4 não inclui ranking automático, cálculo de vencedores, anexos, troca direta, envio de Digimons, equipamentos, múltiplos itens por prêmio ou resgate parcial.

## Critérios de aceite

1. Um administrador consegue criar um prêmio para um jogador específico.
2. O jogador recebe uma mensagem `EVENT` com botão de resgate.
3. O resgate entrega Bits e/ou item em uma única transação.
4. Repetir o clique não duplica a recompensa.
5. Prêmios expirados não podem ser resgatados.
6. Ausência de Digimon ativo não consome o prêmio.
7. A mensagem deixa de exibir a ação após resgate ou expiração.
8. A collection curl e a collection Postman são atualizadas.
