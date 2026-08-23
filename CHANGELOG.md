# Changelog

Todas as mudanças relevantes do **Digimon Revolution Online** são documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto segue versionamento incremental por entregas (PRs).

## [Não lançado]

### Adicionado
- **Modo Arena / PvP assíncrono** (#32): desafie o Digimon ativo de outro jogador; o resultado é resolvido no servidor comparando o poder efetivo (stats + equipamentos) com a mesma fórmula dos bosses, mais um fator de aleatoriedade.
  - Rating por Digimon estilo **ELO** (inicial 1000, K=32, piso 100), com vitórias/derrotas registradas.
  - **Matchmaking** por janela de rating (±200 pts) **e** por stage (mesmo stage ou adjacente).
  - **Bits de vitória proporcionais** à diferença de rating: `clamp(100 + (ratingOponente − seuRating) × 0.25, 25, 200)`.
  - Custo de **10 de energia** por desafio (ADMIN não consome).
  - **Bots de preenchimento**: pool fixo de 30 bots (5 por stage, ratings 860–1140) que entram no lobby apenas quando há menos de 10 oponentes reais. Rating fixo (não muda com partidas) e fora dos rankings.
  - Endpoints `GET /arena/lobby`, `POST /arena/challenge`, `GET /arena/ranking`, `GET /arena/history`.
  - Telas de Arena no game frontend: lobby, resultado do duelo, ranking e histórico.
  - Migrations `V74` (rating/estatísticas + tabela `arena_matches`) e `V75`/`V76` (flag `is_bot`, player de sistema e seed de bots).

### Corrigido
- **Superfície administrativa**: autorização administrativa passou a consultar o `userType` atual no banco, ações destrutivas e grants passaram a gerar auditoria, e o wipe exige confirmação explícita.
- **Proteção de ferramentas internas**: o simulador de choco de traits agora está sob `/admin/digimon/simulator/trait-hatch` e exige um usuário `ADMIN`.
- Sessão expirada agora força retorno à tela de login: qualquer resposta 401/403 no game frontend limpa o token e redireciona para `#login`.

## Entregas anteriores

### Onboarding e economia
- **Tutorial inicial** (#31): checklist "Primeiros Passos" no dashboard, com 6 passos do ciclo principal marcados automaticamente e recompensas por passo. Missões passaram a redirecionar para o dashboard ao iniciar e a exibir "Missões em Andamento" com resgate. Missões agora concedem **bits** além de XP (coluna `base_bits`, configurável por missão).

### Segurança e robustez
- **Correção de ownership** (#29): endpoints `equipment/grant`, `inventory/grant` e `digimon/add-xp` movidos para `/admin/**`; `GET /digimon/{id}` passou a exigir JWT; `refine-preview` valida dono. Adicionada tela de "Ferramentas" no admin (grant de equipamento/item e add XP).
- **Transações** (#28): `@Transactional` aplicado em 21 use cases críticos (missões, bosses, incubação, equipamentos, registro, recompensas).
- **Proteção do admin** (#27): interceptor de `/admin/**` validando JWT + `userType = ADMIN`; login no admin frontend.

### Ferramentas de teste / administração
- **Tipo de usuário ADMIN + wipe** (#24): admins sem cooldown/energia/tempo de incubação; endpoint `POST /admin/players/wipe` que apaga apenas dados de jogadores/Digimons, preservando o conteúdo do jogo.
- **Documento funcional** (#23): `backend/src/main/resources/docs/FUNCIONALIDADES.md`.

### Sistema de Digimons
- **Slots ativos e storage** (#22): 3 slots ativos por jogador + storage de 50; claim de incubação bloqueado com slots cheios; guardar/retirar Digimon (com desequipe automático).

### Bosses e equipamentos
- **Raridade separada do template** (#21): raridade vira propriedade da instância; perfis de raridade nomeados por tipo de boss; loot table de equipamentos por boss (drop único de equipamento a 25% + roll de raridade).
- **Sistema de bosses** (#17): combate instantâneo por poder, drops e administração.
- **Sets, tiers e refinamento de equipamentos** (#16).

### Frontends do jogo (fases)
- **Game frontend PWA** (#8–#15): Login/Starter/Dashboard/Missões, Loja, Inventário/Equipamentos, Evolução, Rebirth, Ranking, Incubação e Pokédex. Renomear Digimon (#18) e destaque do próprio jogador no ranking (#19).
- **Simulador de criação de Digimon no admin** (#20).

### Migração de conteúdo para o banco
- **Catálogos migrados de código para tabelas** (#1–#3): missões, loja e equipamentos.
- **CRUD administrativo** (#4–#6): EquipmentTemplate, ShopProducts e MissionCatalog.
- **Novas missões** (#7): 12 missões cobrindo as 6 áreas.
