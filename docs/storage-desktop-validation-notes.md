# Validação visual do armazém desktop

## Objetivo

Aplicar à tela `storage` a mesma composição visual responsiva adotada recentemente em `inventory`, `shop` e `mission-area`, priorizando uma leitura mais ampla e organizada em telas desktop sem regredir o fluxo linear em dispositivos móveis.

## Alterações validadas

- cabeçalho com contexto de gestão, retorno para a Home, ação de filtros e emblema da tela;
- hero contextual para explicar a finalidade do armazém;
- workspace desktop com rail lateral sticky para capacidade, Dados Digitais e filtros;
- busca e resumo da coleção concentrados na coluna principal;
- cartões de Digimon em grade de duas colunas, com nome longo legível, metadados, atributos e ações agrupadas;
- estados bloqueado, vazio, erro e carregamento com tratamento visual próprio;
- controles de seleção em massa alinhados ao mesmo sistema de botões dos demais módulos;
- fallback mobile em uma coluna, com ações dos cartões distribuídas em três controles acessíveis.

## Verificações executadas

- `node --check game-frontend/assets/js/storage.js`;
- `git diff --check`;
- prévia estática em viewport desktop com cartões desbloqueados, bloqueados e nomes longos;
- revisão da grade desktop em duas colunas e do rail lateral sticky;
- revisão das classes sem estilos inline nos cartões e ações de storage.

## Critérios de aceite

A página deve aproveitar a largura desktop sem comprimir a informação em uma lista estreita. Os cartões devem manter nomes longos legíveis, diferenciar visualmente Digimons protegidos e manter as três ações principais acessíveis. Em mobile, a composição deve retornar a uma coluna, preservar a busca e manter os botões de ação utilizáveis lado a lado.

## Correção posterior no PR

Após a revisão visual, foi identificado que o breakpoint de telas pequenas havia reduzido indevidamente o cartão para uma imagem de `3.3rem` e um gap de `0.48rem`, aproximando os textos da imagem. O override mobile foi corrigido para recuperar a imagem de `4rem`, o gap de `0.75rem` e uma composição mais próxima da versão anterior. Os botões continuam reposicionados em uma linha própria com três colunas.

A correção foi conferida em capturas headless de 375×812 e 1440×1000. No mobile, o nome, nível e atributos agora possuem separação visual clara da imagem; no desktop, a grade e o rail lateral permanecem inalterados.
