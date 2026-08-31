# Validação do modal de informações do storage

A prévia executada com o mesmo `storage.js` e helpers da aplicação exibiu o modal com identidade do Digimon, raridade, status da coleção, nível, estágio, tipo, observação do Dado de Raridade, progressão de XP, renascimentos, personalidade, especialidade, atributo, elemento, tier, energia, potencial base, estatísticas efetivas com decomposição de bônus e Bits.

A composição desktop foi conferida em viewport ampla. O overlay recebeu `role="dialog"`, `aria-modal="true"`, título associado por `aria-labelledby`, fechamento por botão, clique fora e tecla Escape. O console não apresentou erros durante a montagem.

## Validação responsiva

A primeira captura mobile revelou que a animação global `fade-in` aplicava `translateX(-50%)` ao modal, deslocando-o parcialmente para fora da viewport. Foi criada uma animação específica, `storage-info-modal-fade-in`, que usa apenas deslocamento vertical. Após a correção, o modal ficou centralizado no desktop e ocupou corretamente a largura mobile, com rolagem vertical para o conteúdo completo.

A captura desktop confirmou uma largura confortável, perfil em quatro colunas, potencial em três colunas e estatísticas efetivas distribuídas horizontalmente. A captura mobile confirmou perfil em duas colunas, estatísticas de combate empilhadas e acesso ao restante do conteúdo por rolagem.
