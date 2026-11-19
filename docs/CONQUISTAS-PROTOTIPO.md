# Conquistas — protótipo

Acesso: **Mais → Conquistas**. São 33 conquistas permanentes por conta, com Bits e um item garantido por meta. Sem pontos, títulos, ranking ou temporadas nesta versão.

## Experiência

- Resumo de concluídas, disponíveis para resgate e resgatadas.
- Categorias, busca por nome/descrição/recompensa e filtro por status.
- Progresso por etapa, com recompensas disponíveis primeiro.
- Resgate manual e único, com atualização do inventário global e dos Bits da conta.
- Progresso atualizado ao abrir/atualizar a página ou tentar um resgate, sem notificações em tempo real nesta versão.
- Uma conclusão observada nunca é perdida, mesmo após renascimento ou troca de parceiro.

Para visualizar com dados fictícios sem login nem backend, abra `docs/prototypes/achievements.html` no navegador. Essa demonstração usa a mesma página e CSS do jogo. Seus resgates não alteram contas reais.

## Recompensas

O catálogo completo está na migration `V250__achievement_prototype_and_vip_pass.sql`. Nome, descrição, meta, Bits, item, quantidade, ordem e disponibilidade são configuráveis em `achievement_definitions`. Não edite a migration depois de aplicada: altere o catálogo com uma nova migration.

| Família | Marcos | Itens, além dos Bits |
|---|---|---|
| Primeiros passos | Primeiro nascimento, primeira missão, primeiro equipamento | Células, discos de XP |
| Criação | 10 / 50 / 100 nascimentos | Incubadoras rara/épica, Dado de Raridade |
| Evolução | Baby II até Mega | Discos, Células, Cargas Totais, Passe VIP |
| Renascimento | 1 / 10 / 25 renascimentos | Cristal de Preservação de Raridade, Dado de Raridade |
| Coleção | 10 / 25 / 50 / 100 espécies | Expansores de storage, Dado de Raridade |
| Missões | 10 / 100 / 500 / 1.000 resgates | Células, Cargas Totais, Passe VIP, incubadora lendária |
| Combate | Vitórias na arena, ataques ao Chefe Mundial | Células, discos, Dado de Raridade, Núcleo de Ascensão |
| Clã | 10 / 50 ataques em incursões | Carga Total, Núcleo de Ascensão |
| Equipamentos | Possuir refinamento +5 / +10 e ascensão 1 | Cristais de Proteção, Núcleo de Ascensão |

Há **+20 espaços de storage** no catálogo, distribuídos em quatro unidades do item existente `STORAGE_SLOT_5`. O jogador usa o expansor no inventário. Há somente **dois Passes VIP** no catálogo: primeiro Mega e 500 missões.

O Cristal de Preservação de Raridade mantém sua função existente no renascimento; não foi confundido com o Cristal de Proteção de refinamento.

## Passe VIP de 1 Dia

Novo item `VIP_DAY_PASS`, empilhável, sem venda ou negociação. O resgate de uma conquista apenas entrega o item; não inicia o VIP.

Ao usar uma unidade:

- VIP inativo/vencido: começa no momento do uso e dura 24 horas.
- VIP ativo: soma 24 horas ao vencimento atual.
- Funciona sem Digimon ativo e consome exatamente uma unidade por solicitação.
- Não altera os planos VIP nem renova automaticamente. O uso não resgata automaticamente a Carga Total diária.

## Contagem e dados anteriores

- Nascimentos: histórico de nascimento direto + incubações resgatadas. Renascimento, compra ou troca de parceiro não contam como nascimento.
- Missões: contador permanente incrementado pela transição para `CLAIMED`, abrangendo resgate manual e automático. A migration inicializa o contador com o histórico ainda disponível. O trigger e a missão participam da mesma transação; regravar `CLAIMED` não incrementa novamente. A limpeza de instâncias antigas não reduz o contador.
- Coleção: espécies distintas no álbum, sem contar novamente as raridades da mesma espécie.
- Estágio: maior estágio comprovado pelo álbum ou pelos registros de Digimons. Estágios superiores também satisfazem as metas anteriores.
- Renascimentos: registros de Digimons com status `REBORN`, sem somar contadores de gerações da mesma linhagem.
- Arena: apenas vitórias de desafios iniciados pela própria conta; não conta defesa passiva.
- Chefes: ataques registrados, sem contar de novo uma repetição idempotente da mesma requisição.
- Primeiro equipamento: conclusão persistida do tutorial de equipar.
- Refinamento/ascensão: a condição é **possuir** uma peça no patamar ao consultar a página. Após reconhecida, a conquista permanece concluída mesmo se a peça for vendida. Não se tenta reconstruir peças já vendidas antes da primeira consulta.

Históricos já apagados antes da instalação não são inventados nem reconstruídos a partir do bônus de recompensa de missões (esse contador tem teto de 100). Administradores podem experimentar o protótipo na própria conta; não há ranking público nesta versão.

## Consistência

`player_achievements` possui chave composta por conta e conquista. Leitura/sincronização e resgate obtêm o lock da conta. O resgate também faz uma atualização condicional de `claimed_at`; Bits, item, auditoria e marca de resgate estão na mesma transação. Se o stack não comportar o item, o resgate falha inteiro e permanece disponível. Nenhuma quantidade ou recompensa é aceita do cliente.

Rotas autenticadas:

- `GET /players/me/achievements`
- `POST /players/me/achievements/{code}/claim`

## Validação

- Testes Java: `AchievementServiceTest`, `VipPassRulesTest`, `UseVipPassTest` e regressão `EventRewardMessageTextTest`.
- Testes PostgreSQL (PGlite) e DOM (jsdom): `scripts/achievement-prototype.test.cjs`. O script executa a migration e as consultas SQL reais, verifica isolamento de conta, resgate único, rollback, progresso permanente, proteção contra limpeza e os filtros/resgate da página.

```sh
npm install --prefix /tmp/dro-achievement-tests jsdom @electric-sql/pglite
NODE_PATH=/tmp/dro-achievement-tests/node_modules node --test scripts/achievement-prototype.test.cjs
```

O ambiente de desenvolvimento desta revisão tem runtime Java 21 sem `ct.sym`; a compilação foi feita com `source/target=17`, sem alterar o POM. Mockito foi carregado como agente para evitar self-attach indisponível no ambiente.

A suíte existente `UseItemUseCaseTest` apresenta 14 falhas de mocks desatualizados (`findById` versus `findByIdForUpdate`). As mesmas 14 falhas foram reproduzidas com a implementação original da branch base. Não foram alteradas neste PR.

## Roteiro de homologação

1. Abra Mais → Conquistas e confira o reconhecimento de feitos que já constam na conta.
2. Filtre por categoria/status e busque pelo nome de uma recompensa.
3. Resgate uma conquista: confira Bits e inventário; repita a chamada para verificar a rejeição.
4. Encha o stack da recompensa e confira que o resgate não dá Bits nem desaparece.
5. Complete uma missão manual e outra automática; confira o avanço. A limpeza de histórico não deve reduzi-lo.
6. Resgate um expansor e use-o: o storage deve aumentar em +5 por unidade.
7. Resgate um Passe VIP: apenas o inventário muda. Use com VIP inativo e depois com VIP ativo para conferir início e extensão.
8. Confira a página em celular e desktop. Validação visual em navegador completo e homologação contra o banco do jogo ainda devem ser feitas antes do merge.
