# Digimon Revolution Online (DRO)

Jogo online de progressão no universo Digimon, com backend em Spring Boot, frontend de jogo em JavaScript vanilla, painel administrativo e site oficial estático.

O projeto é um **monólito modular** que evolui por entregas pequenas (MVP), com domínios separados e frontends independentes.

---

## 🗂 Repositório

```text
.
├── backend/          # API e regras de negócio (Spring Boot)
├── game-frontend/    # PWA do jogo (HTML/CSS/JS, Tailwind CDN)
├── admin-frontend/   # Painel administrativo (HTML/CSS/JS, Tailwind CDN)
├── official-site/    # Site público estático
├── docker/           # docker-compose com PostgreSQL
├── CHANGELOG.md      # Histórico de entregas
└── README.md         # Este arquivo
```

---

## 🧱 Arquitetura do backend

O backend segue o padrão **Monólito Modular**, organizado por domínios em `backend/src/main/java/com/dro/modules`:

```text
modules/
├── admin        → Ferramentas administrativas e configurações do servidor
├── arena        → PvP assíncrono, lobby, ranking e histórico
├── auth         → Cadastro e login com JWT
├── boss         → Bosses por estágio, combate e drops
├── clan         → Clãs, Honor Marks, upgrades, missões diárias e raid
├── content      → Catálogos e definições de conteúdo
├── digimon      → Digimons, stats, IVs, raridade, personalidade e trait
├── digitama     → Seleção e incubação de Digitamas
├── equipment    → Equipamentos, sets, tiers e refinamento
├── evolution    → Digievolução por linhas e requisitos
├── incubation   → Processo de chocar Digitamas
├── inventory    → Itens, materiais e consumíveis
├── loot         → Tabelas de drops e recompensas
├── mission      → Missões com duração real, energia e recompensas
├── player       → Jogador, dashboard e configurações
├── ranking      → Rankings globais e por Digimon
├── server       → Informações e status do servidor
├── shop         → Loja de itens, equipamentos e venda
└── tutorial     → Onboarding inicial do jogador
```

Cada módulo possui separação em:

- **api** → Controllers e DTOs
- **application** → Casos de uso
- **domain** → Entidades, enums e regras
- **infra** → Repositórios e integrações

---

## 🚀 Stack Tecnológica

### Backend

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Spring Validation
- Spring Security Crypto
- Flyway (migrations de banco)
- PostgreSQL
- Maven

### Frontends

- HTML5 + CSS3 + JavaScript vanilla
- Tailwind CSS via CDN
- PWA com service worker (`game-frontend/`)

### Site oficial

- HTML/CSS/JS estático
- Modo escuro com `localStorage` e `prefers-color-scheme`

---

## 🎮 Funcionalidades principais

- Cadastro e login com JWT
- Seleção e incubação de Digitamas
- Digimons únicos com raridade, personalidade, trait, IVs e grade
- Sistema de missões com duração real e custo de energia
- Digievolução e renascimento (Rebirth)
- Inventário e equipamentos (armas, armaduras, acessórios, sets, tiers, refinamento)
- Bosses por estágio com drops de equipamentos
- Arena PvP assíncrona com matchmaking por rating e stage
- Sistema de clãs com Honor Marks, upgrades, missões diárias, ranking de contribuição e Raid de Clã
- Painel administrativo para gerenciar catálogos, jogadores, itens e eventos
- Site oficial com galeria, notícias, patch notes, roadmap e dark mode

Para detalhes completos de regras, endpoints e fórmulas, veja `backend/src/main/resources/docs/FUNCIONALIDADES.md`.

---

## ⚙️ Como executar

### Pré-requisitos

- Java 17
- Maven
- Docker e Docker Compose (para subir o PostgreSQL)
- Python 3 (apenas para servir os frontends e o site oficial localmente)

### 1. Banco de dados

```bash
cd docker
docker-compose up -d
```

O container sobe o PostgreSQL na porta `5432`.

> A configuração padrão do backend está em `backend/src/main/resources/application.yml`. Para produção, sobrescreva `spring.datasource.*` e `dro.security.jwt.*` por variáveis de ambiente.

### 2. Backend

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### 3. Game frontend

```bash
cd game-frontend
python3 -m http.server 3000
```

Acesse `http://localhost:3000`.

O frontend espera a API em `http://localhost:8080` (configurável em `assets/js/config.js`).

### 4. Admin frontend

```bash
cd admin-frontend
python3 -m http.server 4000
```

Acesse `http://localhost:4000`.

### 5. Site oficial

```bash
cd official-site
python3 -m http.server 5000
```

Acesse `http://localhost:5000`.

---

## 🔑 Variáveis de ambiente

As seguintes variáveis podem ser usadas para configurar o backend sem alterar o `application.yml`:

| Variável | Descrição | Padrão (desenvolvimento) |
|----------|-----------|--------------------------|
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/dro_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | `arisco2017` |
| `DRO_JWT_SECRET` | Chave secreta do JWT | `dro-local-development-secret-change-this-before-production-2026` |
| `DRO_JWT_ISSUER` | Emissor do JWT | `digimon-revolution-online` |
| `DRO_JWT_EXPIRATION_MINUTES` | Tempo de expiração do token | `1440` |

> **Atenção:** nunca use os valores padrão em produção.

---

## 🧪 Testes

```bash
cd backend
mvn test
```

---

## 🗄 Migrations

As migrations do Flyway estão em:

```text
backend/src/main/resources/db/migration
```

Elas são executadas automaticamente na inicialização da aplicação.

---

## 📝 Documentação

- `CHANGELOG.md` — histórico de entregas e mudanças relevantes
- `backend/src/main/resources/docs/FUNCIONALIDADES.md` — documento funcional completo (regras, endpoints, fórmulas)
- `official-site/README.md` — como rodar e manter o site oficial

---

## ⚠️ Nota legal

Projeto não oficial e sem afiliação à Bandai, Toei Animation ou detentores da marca Digimon. Desenvolvido por fãs para fins de estudo e entretenimento.
