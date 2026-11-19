# Troca de username

## Objetivo

A funcionalidade permite que o jogador altere o username na tela de configurações mediante pagamento em **Bits**. Como os Bits pertencem aos Digimons, o custo é debitado do Digimon que estiver com status `ACTIVE` no momento da operação.

## Fórmula de custo

A configuração padrão é uma sequência crescente definida em `application.yml`:

| Número de trocas anteriores | Custo da próxima troca |
|---:|---:|
| 0 | 1.000 Bits |
| 1 | 5.000 Bits |
| 2 | 10.000 Bits |
| 3 | 20.000 Bits |
| 4 | 30.000 Bits |

Os três primeiros valores podem ser alterados pela variável de ambiente `DRO_USERNAME_CHANGE_COSTS`, usando uma lista separada por vírgulas. O índice da lista é o número de trocas anteriores. Depois do último valor configurado, a regra continua aumentando pelo último custo configurado; com `1000,5000,10000`, as próximas trocas custam 20.000, 30.000, 40.000 Bits e assim por diante.

Valores inválidos ou não crescentes são rejeitados na inicialização do caso de uso. A coluna `players.username_change_count` registra quantas trocas já foram efetivadas e começa em zero para jogadores existentes e novos.

## API

`GET /players/me/change-username` retorna o username atual, o custo da próxima troca, o saldo de Bits do Digimon ativo e a quantidade de trocas já feitas. O endpoint é usado exclusivamente para o preview da tela de configurações.

`POST /players/me/change-username` recebe `{ "newUsername": "novo-nome" }`. A operação valida tamanho entre 3 e 50 caracteres, diferença em relação ao username atual, unicidade sem distinção entre maiúsculas e minúsculas e saldo suficiente. Em seguida, debita os Bits, altera o username, incrementa o contador, incrementa a versão do token e retorna um novo JWT.

A operação bloqueia o jogador e o Digimon ativo durante a transação. Isso evita que duas requisições simultâneas utilizem o mesmo contador ou debitem o mesmo saldo de Bits. O token retornado deve substituir o token armazenado no navegador, pois a alteração invalida as sessões anteriores por meio do versionamento de token.

## Interface

A seção foi adicionada ao card de segurança das configurações, com todos os textos em português. O jogador visualiza o username atual, o custo da próxima troca e os Bits disponíveis antes de enviar o formulário. Após o sucesso, o formulário é limpo, o novo token é armazenado e o preview é atualizado para mostrar o próximo custo.

## Migration

A migration `V169__add_username_change_count.sql` adiciona `username_change_count INTEGER NOT NULL DEFAULT 0` à tabela `players`. A migration não altera usernames nem saldos existentes.

## Validação

O teste unitário `ChangeUsernameUseCaseTest` cobre a sequência padrão, a progressão após o último valor configurado e a rejeição de custos não crescentes. A sintaxe JavaScript de `settings.js` também foi verificada com `node --check`.
