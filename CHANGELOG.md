# Changelog

Todas as mudanças relevantes do **Digimon Revolution Online** são documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/),
e o projeto segue versionamento incremental por entregas (PRs).

## [Não lançado]

### Adicionado

- **Incubadora em paralelo**: três slots fixos sempre visíveis, com o slot 1 desbloqueado por padrão e slots 2 e 3 preparados para futura liberação por progressão.
  - Incubações independentes por slot, com timers e claims individuais.
  - Consulta da incubadora retornando estados bloqueado, livre, em andamento e pronto para cada posição.
  - Capacidade de slots desbloqueados persistida por jogador, iniciando em 1 e limitada a 3.
  - A tela dedicada mantém os três slots visíveis; o dashboard resume apenas os slots que possuem incubação ativa ou pronta.
  - Nomes de Digitamas e incubadoras localizados em português no resumo do dashboard.
  - Item consumível **Expansor de Slot de Incubação**, que incrementa em um a capacidade do jogador até o máximo de três slots.

- **Modo Arena / PvP assíncrono** (#32): desafie o Digimon ativo de outro jogador; o resultado é resolvido no servidor comparando o poder efetivo (stats + equipamentos) com a mesma fórmula dos bosses, mais um fator de aleatoriedade.
  - Rating por Digimon estilo **ELO** (inicial 1000, K=32, piso 100), com vitórias e derrotas registradas.
  - **Matchmaking** por janela de rating (±200 pts) e por stage (mesmo stage ou adjacente).
  - **Bits de vitória proporcionais** à diferença de rating: `clamp(100 + (ratingOponente − seuRating) × 0.25, 25, 200)`.
  - Custo de **10 de energia** por desafio; usuários `ADMIN` não consomem energia.
  - **Bots de preenchimento**: pool fixo de 30 bots, cinco por stage, com ratings entre 860 e 1140. Eles entram no lobby apenas quando há menos de 10 oponentes reais, não alteram rating e não aparecem nos rankings.
  - Endpoints `GET /arena/lobby`, `POST /arena/challenge`, `GET /arena/ranking` e `GET /arena/history`.
  - Telas de Arena no game frontend: lobby, resultado do duelo, ranking e histórico.
  - Migrations `V74` (rating, estatísticas e tabela `arena_matches`) e `V75`/`V76` (flag `is_bot`, jogador de sistema e seed de bots).

### Corrigido

- **Premiação global de eventos**: administradores podem selecionar todos os jogadores do servidor, visualizar a quantidade elegível antes da confirmação e gerar uma mensagem individual para cada conta do tipo `PLAYER`.
- **Busca de jogadores nas ferramentas administrativas**: a etapa de seleção do jogador no fluxo de grant agora mantém o filtro por username, exibe o total encontrado e permite navegar entre páginas de resultados, sem limitar o operador aos dez primeiros jogadores.
- **Identificador de origem da premiação**: o formulário administrativo agora oferece geração automática de identificador, mantendo a edição manual e a idempotência por jogador.
- **Seleção de itens na premiação de eventos**: o item deixou de depender de uma lista fixa no formulário e passou a ser escolhido em modal com pesquisa, catálogo pré-carregado, paginação e resumo do item selecionado.
- **Catálogo completo na premiação de eventos**: o modal passou a exibir todas as definições de `item_definitions`, incluindo itens específicos como fragmentos e baús, com entrega persistida pelo código da definição.
- **Múltiplos itens por premiação**: administradores podem adicionar até 10 itens distintos no mesmo envio e definir a quantidade individual de cada item.
- **Restart automático de bosses no alpha**: após a derrota, o Boss Mundial e a Raid de Clã passam a abrir um novo ciclo automaticamente quando `dro.gameplay.auto-boss-respawn-after-defeat-enabled` está ativo; a flag permanece desligada por padrão fora do perfil alpha.
- **Cooldown da Raid de Clã**: reativado o intervalo entre ataques com a mesma regra do Boss Mundial, usando cinco minutos como configuração do boss e como fallback para valores antigos.

## [0.0.3-SNAPSHOT]

Versão focada em estabilidade dos fluxos de incubação e autenticação.

### Corrigido

- **Contador da incubação**: dashboard e tela dedicada passaram a compartilhar um único controlador de timer, com atualização imediata, contagem baseada no restante real informado pelo servidor e encerramento correto ao trocar de rota.
- **Claim do Digimon**: o Digimon recém-chocado passou a ser confirmado visualmente, com acesso direto para selecioná-lo como parceiro ativo ou visualizá-lo na coleção.
- **Visibilidade do recém-chocado**: a coleção destaca o Digimon novo e navega até o card correspondente, evitando a impressão de que o registro desapareceu quando outro parceiro continua ativo.
- **Estados da incubação**: a tela dedicada passou a considerar também a incubação pronta (`READY`) durante a transição após expiração.
- **Reautenticação após sessão expirada ou revogada**: login e cadastro deixaram de enviar o token antigo armazenado no navegador, permitindo obter uma nova sessão normalmente.
- **Interceptor de versão de sessão**: as rotas públicas de autenticação (`/auth/**`) não são mais bloqueadas pela versão de um token antigo.
- **Atualização do PWA**: cache do game frontend atualizado para `dro-game-v39`, incluindo os scripts necessários para distribuir as correções de sessão e incubação.

## [0.0.2-SNAPSHOT]

Versão de expansão dos sistemas de progressão, coleção, administração e segurança.

### Adicionado

#### Onboarding e economia

- **Tutorial inicial** (#31): checklist “Primeiros Passos” no dashboard, com seis passos do ciclo principal marcados automaticamente e recompensas por passo.
- Missões passaram a redirecionar para o dashboard ao iniciar e a exibir “Missões em Andamento” com resgate.
- Missões passaram a conceder **Bits** além de XP, com recompensa configurável por missão através da coluna `base_bits`.

#### Sistema de Digimons

- **Slots ativos e Storage** (#22): três slots ativos por jogador e Storage para 50 Digimons.
- Ações para guardar e retirar Digimons, com desequipamento automático quando necessário.
- Evolução, rebirth, renomeação, personalidade, traits, raridade, grade, IVs e progressão individual.
- Claim de incubação bloqueado quando os slots ativos estão cheios.

#### Bosses e equipamentos

- **Sistema de bosses** (#17): combate instantâneo por poder, drops e administração.
- **Raridade separada do template** (#21): raridade como propriedade da instância, perfis de raridade por tipo de boss e loot tables de equipamentos.
- **Sets, tiers e refinamento de equipamentos** (#16).

#### Comunidade e conteúdo

- Loja, inventário, Pokedex, rankings, correio, clãs e atividades de progressão.
- Estrutura de administração para gerenciamento de conteúdo e ferramentas de operação.

### Corrigido e reforçado

- **Segurança de sessões**: invalidação de tokens após troca de senha, com opção de encerrar todas as sessões do jogador.
- **Segurança dos frontends**: helpers de escape centralizados, campos controlados por usuários tratados antes da renderização HTML e handlers inline frágeis substituídos por atributos e listeners seguros.
- **Collections da API**: gerador ajustado para delimitar assinaturas dos controllers, derivar corpos de exemplo dos DTOs e manter collections curl e Postman sincronizadas.
- **Superfície administrativa**: autorização administrativa consultando o `userType` atual no banco, auditoria para ações destrutivas e grants e confirmação explícita para wipe.
- **Proteção de ferramentas internas**: simulador de hatch de traits movido para `/admin/digimon/simulator/trait-hatch` e protegido para usuários `ADMIN`.
- **Expiração de sessão**: game e admin passaram a validar a expiração do JWT antes de carregar telas protegidas, redirecionar após 401/403 com aviso e distinguir falhas de rede.
- **Ownership e autorização** (#29): endpoints de grant de equipamentos, grant de inventário e add XP movidos para `/admin/**`; `GET /digimon/{id}` passou a exigir JWT e `refine-preview` passou a validar propriedade.
- **Transações** (#28): `@Transactional` aplicado a 21 casos de uso críticos, incluindo missões, bosses, incubação, equipamentos, registro e recompensas.
- **Proteção do admin** (#27): interceptor de `/admin/**` validando JWT e `userType = ADMIN`, com login no admin frontend.

### Ferramentas e documentação

- **Tipo de usuário ADMIN e wipe** (#24): admins sem cooldown, consumo de energia ou tempo de incubação; endpoint `POST /admin/players/wipe` preservando o conteúdo do jogo.
- **Documento funcional** (#23): `backend/src/main/resources/docs/FUNCIONALIDADES.md`.
- **Game frontend PWA** (#8–#15): Login, Starter, Dashboard, Missões, Loja, Inventário, Equipamentos, Evolução, Rebirth, Ranking, Incubação e Pokédex.
- Simulador de criação de Digimon no admin.

## [0.0.1-SNAPSHOT]

Versão de fundação da conta, da jornada inicial e da estrutura de conteúdo do jogo.

### Adicionado

#### Primeiros passos do jogador

- Criação de conta, autenticação e inicialização da jornada do Tamer.
- Seleção e hatch do Digitama inicial com criação do primeiro Digimon.
- Dashboard com parceiro ativo, recursos e navegação principal.
- Modelo inicial de progressão com atributos, energia, experiência e evolução básica do Digimon.

#### Frontend e experiência base

- Estrutura inicial do **game frontend PWA** para acesso pelo navegador e dispositivos móveis.
- Fluxos iniciais de Login, Starter, Dashboard, Missões, Loja, Inventário, Incubação e Pokédex.
- Catálogo inicial de Digimons, itens, missões e equipamentos.

#### Migração e administração de conteúdo

- Catálogos de missões, loja e equipamentos migrados do código para tabelas (#1–#3).
- CRUD administrativo de `EquipmentTemplate`, `ShopProducts` e `MissionCatalog` (#4–#6).
- Novas missões cobrindo as seis áreas do jogo (#7).

[Não lançado]: https://github.com/rafaelhazevedo61/digimon-revolution-online/compare/develop...HEAD
[0.0.3-SNAPSHOT]: https://github.com/rafaelhazevedo61/digimon-revolution-online/releases/tag/0.0.3-SNAPSHOT
[0.0.2-SNAPSHOT]: https://github.com/rafaelhazevedo61/digimon-revolution-online/releases/tag/0.0.2-SNAPSHOT
[0.0.1-SNAPSHOT]: https://github.com/rafaelhazevedo61/digimon-revolution-online/releases/tag/0.0.1-SNAPSHOT
