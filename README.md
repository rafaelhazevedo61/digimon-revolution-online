# Digimon Revolution Online

Digimon Revolution Online (DRO) é um MMORPG de progressão inspirado no universo Digimon, desenvolvido como projeto independente de estudo e entretenimento. O repositório reúne a API do jogo, a aplicação web dos jogadores, o painel administrativo e o site oficial.

O projeto utiliza um **monólito modular** no backend. Cada domínio possui suas próprias entidades, regras, casos de uso, controllers, DTOs e repositórios, enquanto os frontends permanecem independentes e consomem a API por HTTP.

## Estado atual

A base atual contém os principais sistemas de progressão, economia e comunidade. O sistema de Correio foi concluído nas Sprints 1 a 4 e permite mensagens entre jogadores, notificações de sistemas, convites de clã, comunicados administrativos e premiações de eventos.

A documentação Javadoc do backend também foi expandida para todos os módulos Java da aplicação. A geração da documentação é reproduzível pelo Maven e as convenções estão registradas em [`docs/javadoc-guidelines.md`](docs/javadoc-guidelines.md).

| Área | Estado atual |
|---|---|
| API e regras de negócio | Implementadas em Java com Spring Boot e PostgreSQL |
| Jogo web | SPA/PWA em JavaScript vanilla |
| Painel administrativo | SPA independente com rotas para catálogos, jogadores, eventos e ferramentas |
| Site oficial | Site estático com notícias, galeria, roadmap e wiki |
| Casa de Leilões | Anúncios de itens empilháveis, compra parcial, taxas, cancelamento e expiração |
| Correio | Mensagens, notificações, convites, comunicados e premiações de eventos |
| Javadoc | Cobertura de classe nos módulos Java do backend |
| Troca direta entre jogadores | Planejada; ainda não faz parte da versão atual |
| Equipamentos na Casa de Leilões | Planejado; a Casa de Leilões atual negocia itens empilháveis |

## Estrutura do repositório

```text
.
├── backend/             # API, regras de negócio, migrations e collections
├── game-frontend/       # SPA/PWA dos jogadores em HTML, CSS e JavaScript
├── admin-frontend/      # SPA do painel administrativo
├── official-site/       # Site oficial estático e wiki pública
├── docker/              # Docker Compose para PostgreSQL, MongoDB e API local
├── docs/                # Planos, convenções e documentação de engenharia
├── scripts/             # Geradores e utilitários do projeto
├── CHANGELOG.md         # Histórico de entregas
└── README.md            # Este documento
```

## Arquitetura do backend

Os módulos do backend estão em `backend/src/main/java/com/dro/modules`. O código é organizado por domínio, e cada módulo pode conter as camadas abaixo.

| Camada | Responsabilidade |
|---|---|
| `api` | Controllers HTTP e contratos de entrada e saída |
| `application` | Casos de uso, orquestração e transações |
| `domain` | Entidades, enums, regras e fórmulas de negócio |
| `infra` | Repositórios, consultas e integrações persistentes |

Os módulos atuais são:

| Módulo | Responsabilidade |
|---|---|
| `admin` | Ferramentas administrativas, comunicados e configurações |
| `arena` | PvP assíncrono, matchmaking, ELO, ranking e recompensas |
| `auction` | Casa de Leilões, anúncios, compras, taxas e expirações |
| `auth` | Cadastro, login e emissão de JWT |
| `boss` | Bosses por estágio, Boss Mundial, combate, cooldowns e drops |
| `clan` | Clãs, cargos, convites, upgrades, missões e raid |
| `content` | Catálogos e definições de conteúdo |
| `digimon` | Digimons, atributos, IVs, raridade, personalidade, trait e progressão |
| `digitama` | Pools, seleção e histórico de Digitamas |
| `equipment` | Equipamentos, slots, sets, tiers, raridade e refinamento |
| `event` | Premiações de eventos e ciclo de resgate |
| `evolution` | Linhas de Digievolução e materiais específicos |
| `incubation` | Incubação e resgate de Digimons |
| `inventory` | Itens, materiais, consumíveis e definições de itens |
| `loot` | Tabelas de drops e recompensas aleatórias |
| `mail` | Correio do jogador e mensagens geradas pelo sistema |
| `mission` | Missões, áreas, timers, energia e recompensas |
| `player` | Conta, dashboard, Digimon ativo e storage |
| `ranking` | Rankings globais e por Digimon |
| `server` | Informações e estado do servidor |
| `shop` | Loja de itens, equipamentos e venda de recursos |
| `tutorial` | Progresso do onboarding inicial |

## Funcionalidades principais

O jogo possui cadastro e autenticação com JWT, seleção de Digitama, incubação, criação e progressão de Digimons, níveis, IVs, grade, raridade, personalidade, trait, energia, Digievolução e Rebirth.

A economia inclui Bits vinculados ao Digimon, inventário, equipamentos com slots, sets, tiers e refinamento, Loja de itens e equipamentos e Casa de Leilões. A Casa de Leilões atual permite anunciar itens empilháveis por 24, 48 ou 72 horas, com taxa de publicação, comissão proporcional à duração, compra parcial, cancelamento, expiração e notificações pelo Correio.

Os sistemas de combate e comunidade incluem missões com duração real, Bosses por estágio, Boss Mundial, Arena PvP assíncrona, rankings, clãs, cargos, convites, upgrades, missões diárias e Raid de Clã.

### Correio do jogador

O Correio suporta mensagens de texto simples entre jogadores, sem anexos ou transferência direta de Bits e itens em mensagens comuns. As mensagens do sistema podem ser geradas pela Casa de Leilões, por convites de clã, pela administração e por premiações de eventos.

As premiações de eventos podem ser destinadas a um jogador, a todos os membros de um clã ou a uma lista de até 100 jogadores. Uma premiação pode conter Bits e/ou item, possui validade, só pode ser resgatada uma vez e registra no corpo da mensagem o que foi entregue, para qual Digimon e em qual horário. O resgate exige um Digimon ativo pertencente ao jogador.

### Javadoc do backend

O backend possui Javadoc de classe em todos os arquivos Java dos módulos atuais. As regras de maior risco também possuem documentação detalhada sobre fórmulas, limites, locks, idempotência, transações e efeitos persistentes.

Para conhecer o padrão adotado, consulte [`docs/javadoc-guidelines.md`](docs/javadoc-guidelines.md). Para gerar a documentação localmente, consulte a seção [Javadoc e qualidade](#javadoc-e-qualidade).

## Stack tecnológica

### Backend

- Java 17.
- Spring Boot 4.0.2.
- Spring Data JPA e Hibernate.
- Spring MVC e Bean Validation.
- Spring Security Crypto.
- Flyway para migrations.
- PostgreSQL 16 como banco transacional principal.
- MongoDB 7 para auditoria e logs operacionais.
- Maven Wrapper.

### Frontends

- HTML5 e CSS3.
- JavaScript vanilla.
- Tailwind CSS via CDN.
- SPA com roteamento por hash.
- PWA e service worker no `game-frontend/`.

### Site oficial

- HTML, CSS e JavaScript estáticos.
- Tema escuro com `localStorage` e `prefers-color-scheme`.
- Notícias, galeria, patch notes, roadmap e wiki pública.

## Observabilidade, resiliência e auditoria

A aplicação possui uma camada de observabilidade projetada para separar o estado oficial do jogo da investigação operacional. O **PostgreSQL continua sendo a fonte oficial** de jogadores, Digimon, inventário, Bits, equipamentos, anúncios, resgates e mensagens. O MongoDB armazena somente documentos de auditoria positiva e logs de erro; ele nunca deve ser usado para reconstruir ou substituir o estado transacional do jogo.

As operações críticas gravam o estado do jogo e o evento de auditoria na mesma transação PostgreSQL por meio do padrão **Transactional Outbox**. Um processador agendado entrega posteriormente a auditoria ao MongoDB. Essa separação garante que uma falha no MongoDB não desfaça uma compra, um resgate ou um refinamento que já foi confirmado pelo PostgreSQL.

```text
Transação oficial
      |
      +--> PostgreSQL: estado do jogo
      +--> PostgreSQL: audit_outbox_events
                            |
                            v
                  AuditOutboxProcessor
                            |
              +-------------+-------------+
              |                           |
       MongoDB disponível          MongoDB indisponível
              |                           |
              v                           v
  dro_transaction_audits       FAILED + retry com backoff
                                          |
                              limite atingido: DEAD_LETTER
```

### Estados do Transactional Outbox

| Estado | Significado operacional | Processamento automático |
|---|---|---:|
| `PENDING` | Evento criado junto da transação oficial e aguardando publicação | Sim |
| `FAILED` | Tentativa de publicação falhou; o erro e a próxima tentativa foram registrados | Sim, quando `available_at` chegar |
| `PUBLISHED` | Auditoria foi persistida no MongoDB e a entrega foi confirmada | Não |
| `DEAD_LETTER` | Falha persistente atingiu o limite; o evento foi separado para investigação manual | Não |

O ciclo normal é `PENDING → PUBLISHED`. Em uma falha transitória, o ciclo é `PENDING → FAILED → FAILED → PUBLISHED`. Se a causa persistir até o limite configurado, o ciclo termina em `DEAD_LETTER`. O processador nunca deve marcar manualmente um evento como `PUBLISHED`; essa transição só ocorre depois da persistência idempotente no MongoDB.

### Retry, backoff e DEAD_LETTER

O processador executa a cada cinco segundos por padrão e consulta somente eventos `PENDING` e `FAILED` cujo `available_at` já foi alcançado. O backoff cresce entre as tentativas e é limitado a cinco minutos. O limite padrão é de cinco tentativas já consumidas; quando uma nova falha ocorre com `attempts >= 5`, o evento é marcado como `DEAD_LETTER` e a falha é preservada em `last_error`. Por isso, o registro de teste que começa com `attempts = 5` passa a exibir `attempts = 6` após a falha definitiva.

Para diagnosticar o Outbox no PostgreSQL:

```sql
SELECT status, COUNT(*) AS total
FROM audit_outbox_events
GROUP BY status
ORDER BY status;
```

Para listar eventos que precisam de atenção:

```sql
SELECT event_id,
       event_type,
       aggregate_type,
       aggregate_id,
       correlation_id,
       status,
       attempts,
       available_at,
       published_at,
       last_error,
       created_at
FROM audit_outbox_events
WHERE status IN ('FAILED', 'DEAD_LETTER')
ORDER BY created_at ASC
LIMIT 100;
```

Para investigar um evento específico:

```sql
SELECT id,
       event_id,
       event_type,
       aggregate_type,
       aggregate_id,
       correlation_id,
       payload_json,
       status,
       attempts,
       created_at,
       available_at,
       published_at,
       last_error,
       version
FROM audit_outbox_events
WHERE event_id = 'COLE-O-EVENT-ID-AQUI';
```

### Procedimento manual para DEAD_LETTER

O reprocessamento manual deve ser feito somente depois de confirmar que o MongoDB está saudável, que a causa da falha foi corrigida e que o `payload_json` não contém segredo, token, cookie ou estrutura inválida. Primeiro faça uma cópia dos dados da linha para o registro operacional e examine `last_error`, `attempts`, `payload_json` e `correlation_id`.

Nunca altere um evento diretamente para `PUBLISHED` e nunca apague a linha para fazê-la desaparecer da fila. Para reprocessar **um único evento** de forma controlada, use uma transação SQL e mantenha a condição `status = 'DEAD_LETTER'`, evitando que uma segunda pessoa reabra o mesmo evento sem perceber:

```sql
BEGIN;

SELECT event_id,
       status,
       attempts,
       available_at,
       last_error,
       payload_json
FROM audit_outbox_events
WHERE event_id = 'COLE-O-EVENT-ID-AQUI'
  AND status = 'DEAD_LETTER'
FOR UPDATE;

UPDATE audit_outbox_events
SET status = 'FAILED',
    attempts = 0,
    available_at = CURRENT_TIMESTAMP,
    published_at = NULL,
    last_error = NULL
WHERE event_id = 'COLE-O-EVENT-ID-AQUI'
  AND status = 'DEAD_LETTER';

COMMIT;
```

Depois confirme a transição e aguarde o próximo ciclo do processador:

```sql
SELECT event_id,
       status,
       attempts,
       available_at,
       published_at,
       last_error
FROM audit_outbox_events
WHERE event_id = 'COLE-O-EVENT-ID-AQUI';
```

O resultado esperado é `FAILED` imediatamente e, após a publicação bem-sucedida, `PUBLISHED` com `published_at` preenchido. Em seguida, confirme no MongoDB que existe exatamente um documento com o mesmo `eventId`:

```javascript
db.dro_transaction_audits.findOne({ eventId: "COLE-O-EVENT-ID-AQUI" })
```

Se o problema for payload inválido ou dado inconsistente, **não faça reprocessamento cego**. Preserve o evento como `DEAD_LETTER`, corrija a causa no código ou nos dados por procedimento revisado e registre a decisão operacional. O reprocessamento manual não deve ser usado para alterar o estado do jogo, somente para tentar novamente a entrega de uma auditoria já criada.

### Retenção TTL

As auditorias positivas são retidas por aproximadamente 180 dias e os logs de erro por aproximadamente 365 dias. Os índices são aplicados ao campo `occurredAt`:

| Collection MongoDB | Campo | `expireAfterSeconds` | Retenção aproximada |
|---|---|---:|---:|
| `dro_transaction_audits` | `occurredAt` | `15552000` | 180 dias |
| `dro_error_logs` | `occurredAt` | `31536000` | 365 dias |

A remoção é executada de forma assíncrona pelo monitor TTL do MongoDB e não ocorre necessariamente no segundo exato do vencimento. Em ambientes operacionais, mantenha a criação automática de índices desabilitada por padrão e gerencie a criação dos índices durante a preparação do ambiente; habilite `SPRING_DATA_MONGODB_AUTO_INDEX_CREATION=true` apenas quando isso fizer parte de uma inicialização controlada.

### Erros HTTP e correlação

Respostas de erro incluem `X-Correlation-Id`, e o mesmo identificador é persistido em `dro_error_logs`. Para consultar um erro no MongoDB:

```javascript
db.dro_error_logs.find({ correlationId: "COLE-O-CORRELATION-ID-AQUI" })
  .sort({ occurredAt: -1 })
  .limit(20)
```

O documento de erro deve conter contexto suficiente para investigação, mas nunca deve persistir senha, token, cookie ou o valor completo de um header de autenticação. Mensagens e stack traces são sanitizados e limitados.

### Variáveis de observabilidade

| Variável | Padrão | Finalidade |
|---|---:|---|
| `SPRING_MONGODB_URI` | URI local | Conexão da API com o MongoDB de auditoria |
| `SPRING_DATA_MONGODB_AUTO_INDEX_CREATION` | `false` | Criação automática dos índices MongoDB |
| `DRO_AUDIT_OUTBOX_FIXED_DELAY_MS` | `5000` | Intervalo entre ciclos do processador |
| `DRO_AUDIT_OUTBOX_MAX_ATTEMPTS` | `5` | Limite de tentativas antes de `DEAD_LETTER` |
| `DRO_CACHE_CATALOGS_MAX_SIZE` | `500` | Limite de cada cache de catálogo |
| `DRO_CACHE_CATALOGS_TTL_SECONDS` | `300` | TTL dos catálogos seguros |

Para o runbook completo, incluindo diagnóstico Docker, recuperação do MongoDB e consultas de auditoria, consulte [`docs/observability-guide.md`](docs/observability-guide.md).

## Como executar localmente

### Pré-requisitos

Instale Java 17, Docker com Docker Compose e Python 3. O Maven instalado globalmente é opcional, pois o backend possui Maven Wrapper (`./mvnw`).

### 1. Subir o PostgreSQL

Na raiz do repositório, execute:

```bash
docker compose -f docker/docker-compose.yml up -d
```

O serviço ficará disponível em `localhost:5432` com os valores definidos no Compose:

| Configuração | Valor local |
|---|---|
| Banco | `dro_db` |
| Usuário | `dro_user` |
| Senha | `dro_pass` |
| Porta | `5432` |

A configuração padrão do `application.yml` possui valores históricos diferentes dos usados pelo Compose. Para executar localmente com o banco do Docker, exporte as variáveis abaixo antes de iniciar o backend.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dro_db
export SPRING_DATASOURCE_USERNAME=dro_user
export SPRING_DATASOURCE_PASSWORD=dro_pass
```

As migrations do Flyway são executadas automaticamente na inicialização. Nunca utilize credenciais de desenvolvimento em produção.

### 2. Iniciar o backend

```bash
cd backend
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

Para apenas compilar o backend:

```bash
./mvnw -q -DskipTests compile
```

### 3. Iniciar o frontend do jogo

Em outro terminal:

```bash
cd game-frontend
python3 -m http.server 3000
```

Acesse `http://localhost:3000`. O frontend espera a API em `http://localhost:8080`; essa URL pode ser ajustada em `game-frontend/assets/js/config.js`.

### 4. Iniciar o painel administrativo

Em outro terminal:

```bash
cd admin-frontend
python3 -m http.server 4000
```

Acesse `http://localhost:4000`. As rotas administrativas exigem autenticação com usuário de tipo `ADMIN` no backend.

### 5. Iniciar o site oficial

Em outro terminal:

```bash
cd official-site
python3 -m http.server 5000
```

Acesse `http://localhost:5000`.

## Variáveis de ambiente

O backend pode ser configurado sem alterar o `application.yml` por meio das variáveis abaixo.

| Variável | Descrição | Valor usado pelo Compose local |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/dro_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `dro_user` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `dro_pass` |
| `SPRING_MONGODB_URI` | URI do MongoDB de auditoria | Definida pelo ambiente local |
| `SPRING_DATA_MONGODB_AUTO_INDEX_CREATION` | Criação automática dos índices MongoDB | `false` |
| `DRO_AUDIT_OUTBOX_FIXED_DELAY_MS` | Intervalo do processador do Outbox | `5000` |
| `DRO_AUDIT_OUTBOX_MAX_ATTEMPTS` | Limite antes de `DEAD_LETTER` | `5` |
| `DRO_CACHE_CATALOGS_MAX_SIZE` | Limite de cada cache seguro | `500` |
| `DRO_CACHE_CATALOGS_TTL_SECONDS` | TTL dos caches de catálogo | `300` |
| `DRO_JWT_SECRET` | Chave secreta usada para assinar JWT | Definida pelo `application.yml` apenas no desenvolvimento |
| `DRO_JWT_ISSUER` | Emissor do JWT | `digimon-revolution-online` |
| `DRO_JWT_EXPIRATION_MINUTES` | Tempo de expiração do token | `1440` |

Em produção, substitua obrigatoriamente a senha do banco e o segredo JWT por valores mantidos em um gerenciador de segredos. Não versione credenciais reais.

## Testes e qualidade

O backend possui testes unitários de domínio e de casos de uso. Os comandos principais são:

```bash
cd backend
./mvnw -q test
```

Para validar apenas a compilação e os testes de uma área específica:

```bash
./mvnw -q -DskipTests compile
./mvnw -q -Dtest=MailRulesTest,MailMessageTest,EventRewardTest,EventRewardMessageTextTest test
```

A suíte completa ainda possui testes legados que precisam de manutenção em algumas áreas do projeto, especialmente fixtures de JWT, stubs Mockito e expectativas antigas de regras de evolução. Essas falhas devem ser consideradas ao interpretar o resultado geral; os testes focados da alteração devem continuar sendo executados em cada PR.

### Javadoc e qualidade

Gere o Javadoc do backend com:

```bash
cd backend
./mvnw -q -DskipTests javadoc:javadoc
```

A saída será criada em `backend/target/reports/apidocs`. Os padrões de escrita, uso de tags, regras de revisão e critérios para comentários estão em [`docs/javadoc-guidelines.md`](docs/javadoc-guidelines.md).

Antes de criar um commit, valide também o espaço em branco do diff:

```bash
git diff --check
```

## Migrations

As migrations do Flyway estão em `backend/src/main/resources/db/migration` e são executadas automaticamente pelo backend. A migration mais recente do estado atual cria a tabela do Transactional Outbox (`V100__create_audit_outbox_events.sql`). A tabela registra eventos junto das transações oficiais para permitir publicação assíncrona e recuperação operacional.

Não edite uma migration já aplicada. Para alterar o schema, crie uma nova migration com o próximo número disponível e valide a inicialização contra PostgreSQL.

## Collections da API

O repositório mantém duas collections geradas a partir dos controllers Java:

- [`api-curl-collection.sh`](backend/src/main/resources/api-curl-collection.sh), com comandos curl comentados e variáveis de token.
- [`DRO - MODULES.postman_collection.json`](backend/src/main/resources/collection/DRO%20-%20MODULES.postman_collection.json), organizada por módulos para importação no Postman.
- [`API_CURL_COLLECTION.md`](backend/src/main/resources/docs/API_CURL_COLLECTION.md), com instruções, variáveis e grupos cobertos.

Depois de alterar qualquer rota, execute o gerador a partir da raiz:

```bash
python3 scripts/generate_api_curl_collection.py
bash -n backend/src/main/resources/api-curl-collection.sh
```

A collection não deve armazenar tokens reais, senhas ou dados pessoais. Preencha os placeholders apenas no ambiente local.

## Documentação adicional

| Documento | Conteúdo |
|---|---|
| [`CHANGELOG.md`](CHANGELOG.md) | Histórico de entregas e mudanças relevantes |
| [`FUNCIONALIDADES.md`](backend/src/main/resources/docs/FUNCIONALIDADES.md) | Regras, fórmulas, endpoints e fluxos do backend |
| [`API_CURL_COLLECTION.md`](backend/src/main/resources/docs/API_CURL_COLLECTION.md) | Uso e regeneração das collections |
| [`javadoc-guidelines.md`](docs/javadoc-guidelines.md) | Padrão de documentação do código Java |
| [`observability-guide.md`](docs/observability-guide.md) | Operação de PostgreSQL, MongoDB, Outbox, TTL, retry e DEAD_LETTER |
| [`observability-manual-test-plan.md`](docs/observability-manual-test-plan.md) | Roteiro manual da cadeia de observabilidade |
| [`official-site/README.md`](official-site/README.md) | Execução e manutenção do site oficial |
| [Wiki de Sistemas](official-site/wiki/sistemas.html) | Explicações voltadas aos jogadores |

## Fluxo de contribuição

Para iniciar uma alteração funcional ou documental, atualize o `develop` e crie uma branch própria:

```bash
git switch develop
git pull --ff-only origin develop
git switch -c feat/nome-da-feature
```

Cada feature deve possuir uma PR própria contra `develop`. Antes de solicitar revisão, execute os testes adequados, gere ou valide as collections quando houver mudança de rota, rode `git diff --check` e descreva limitações conhecidas na PR.

## Roadmap

As próximas evoluções planejadas incluem suporte a equipamentos na Casa de Leilões, troca direta entre jogadores, automação do motor de eventos usando as premiações já existentes e melhorias de experiência no Correio, como respostas encadeadas, filtros e busca.

A prioridade técnica recomendada é manter a suíte de testes saudável e ampliar os testes de integração PostgreSQL antes de adicionar operações econômicas concorrentes, como troca direta e negociação de equipamentos.

## Nota legal

Este é um projeto não oficial e sem afiliação à Bandai, Toei Animation ou aos detentores da marca Digimon. Foi desenvolvido por fãs para fins de estudo e entretenimento.
