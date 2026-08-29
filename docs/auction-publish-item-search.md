# Busca e paginação na publicação de leilões

A publicação de anúncios na Casa de Leilões agora possui busca e paginação no seletor de itens. O fluxo continua exibindo apenas itens que pertencem ao Digimon ativo e que são negociáveis e empilháveis.

A busca é feita no cliente sobre os itens elegíveis já carregados do endpoint `GET /inventory`. Ela aceita correspondência por nome, código da definição e tipo técnico do item. O resultado é dividido em páginas de 10 itens, com controles **Anterior** e **Próxima** e indicação da quantidade encontrada.

A paginação altera somente a apresentação da lista; a quantidade disponível continua sendo obtida do inventário atual. Ao selecionar um item, o limite máximo do campo de quantidade é atualizado conforme o saldo daquele item. Se a busca não encontrar resultados, a publicação fica bloqueada até que um item válido seja selecionado.

A publicação continua usando `POST /auction/listings` com `itemDefinitionId`, `quantity`, `unitPrice` e `durationHours`. Nenhuma regra de segurança ou validação do backend foi removida; o backend permanece responsável por confirmar posse, quantidade, negociabilidade, empilhamento, Bits e limites do anúncio.
