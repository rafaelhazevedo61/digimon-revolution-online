# Sprint 3 — Convites de clã e comunicados administrativos no Correio

## Objetivo

Expandir o Correio para suportar mensagens de sistema com ações seguras. A primeira frente será o convite de clã, permitindo que um jogador autorizado convide outro jogador e que o destinatário aceite ou recuse o convite diretamente pela mensagem. A segunda frente será preparar comunicados administrativos persistentes, sem transformar o Correio em um canal de envio livre para jogadores.

## Convites de clã

Um convite deverá ser criado por um líder ou oficial autorizado, desde que o destinatário não esteja em outro clã, o clã ainda tenha capacidade e não exista outro convite pendente para a mesma pessoa e o mesmo clã.

O convite será uma mensagem do tipo `CLAN`, com remetente de sistema na interface, metadados de origem apontando para o clã e uma ação segura identificada por `action_type`. O destinatário poderá escolher **Aceitar** ou **Recusar**. A ação deverá ser validada novamente no backend no momento do clique, pois o estado do clã, a capacidade e o vínculo do jogador podem ter mudado desde o recebimento.

Ao aceitar, o backend deverá verificar que o convite ainda está pendente, que o jogador continua sem clã, que o clã ainda tem espaço e que o convite não expirou. A entrada no clã e a conclusão do convite devem ocorrer em uma única transação. Ao recusar, o convite deve ser concluído como recusado e não poderá ser reutilizado.

## Comunicados administrativos

Comunicados administrativos serão mensagens do tipo `ADMIN`, criadas apenas por uma operação interna autorizada. A Sprint 3 deve preparar o modelo e a exibição para mensagens administrativas, mas não deve criar um endpoint público que permita a qualquer jogador enviar comunicados.

Os comunicados devem ser somente informativos nesta etapa, sem ações, sem anexos, sem envio de Bits, itens ou equipamentos. O conteúdo deve ser exibido como texto seguro no Correio.

## Regras de segurança

Toda ação de convite deve usar um identificador próprio e idempotente. O texto exibido na mensagem não é uma autorização; a autorização deve ser determinada pelo backend consultando o convite e o estado atual do jogador e do clã.

As mensagens não devem expor dados internos desnecessários. A exclusão pelo destinatário deve remover a mensagem da Entrada sem apagar o registro de convite antes que ele seja concluído, para que uma ação não possa ser contornada ou repetida por exclusão e reprocessamento.

## Critérios de aceite

1. Um líder ou oficial autorizado consegue convidar um jogador elegível.
2. O destinatário recebe o convite na Entrada e vê as ações Aceitar e Recusar.
3. Aceitar adiciona o jogador ao clã apenas se todas as regras ainda forem verdadeiras.
4. Recusar encerra o convite sem alterar o vínculo do jogador.
5. Repetir a mesma ação não duplica a entrada no clã nem gera efeitos adicionais.
6. Convites expirados ou já concluídos não podem ser aceitos ou recusados novamente.
7. A mensagem continua segura quando o clã ou o nome do jogador contém caracteres especiais.
8. Comunicados administrativos aparecem como mensagens informativas e não podem ser criados pelo frontend público.
9. Existem testes unitários para estados do convite, autorização, expiração e idempotência.

## Fora do escopo

A Sprint 3 não inclui convites de amizade, conversas encadeadas, resposta livre a mensagens, anexos, transferência de Bits, resgate de itens, premiações de eventos ou ações administrativas expostas publicamente.
