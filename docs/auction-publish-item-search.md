# Busca e paginação na publicação de leilões

A publicação de anúncios na Casa de Leilões agora possui um botão **Selecionar item** que abre um modal dedicado, seguindo o padrão do modal **Adicionar item** do painel administrativo. O fluxo continua exibindo apenas itens que pertencem ao Digimon ativo e que são negociáveis e empilháveis.

Dentro do modal, a busca é feita sobre os itens elegíveis já carregados do endpoint `GET /inventory`. Ela aceita correspondência por nome, código da definição e tipo técnico do item. O resultado é dividido em páginas de 10 itens, com cards selecionáveis, controles **Anterior** e **Próxima** e indicação da quantidade encontrada. Ao selecionar, o modal é fechado e o item aparece resumido no formulário de publicação.

A paginação altera somente a apresentação da lista; a quantidade disponível continua sendo obtida do inventário atual. Ao selecionar um item, o limite máximo do campo de quantidade é atualizado conforme o saldo daquele item. Se a busca não encontrar resultados, a publicação fica bloqueada até que um item válido seja selecionado.

A publicação continua usando `POST /auction/listings` com `itemDefinitionId`, `quantity`, `unitPrice` e `durationHours`. Nenhuma regra de segurança ou validação do backend foi removida; o backend permanece responsável por confirmar posse, quantidade, negociabilidade, empilhamento, Bits e limites do anúncio.


## Correção dos Fragmentos de Mega

Os Fragmentos de Mega não apareciam no modal de publicação porque suas definições possuíam `tradable = FALSE`, embora fossem itens empilháveis. Como o frontend e o backend da Casa de Leilões validam a propriedade `tradable`, o bloqueio ocorria corretamente segundo o cadastro existente.

A migration `V173__enable_mega_fragments_in_auction.sql` altera `tradable` para `TRUE` nos itens da categoria `EVOLUTION_MATERIAL` com o ícone `fragment_mega_specific`. A alteração é limitada aos Fragmentos de Mega e não remove as validações de posse, quantidade ou empilhamento da publicação.
