# Digimon Revolution Online (DRO)

Backend do jogo **Digimon Revolution Online**, desenvolvido como um **monólito modular** utilizando Spring Boot.

Este projeto representa a nova fundação do DRO, com foco em arquitetura organizada, domínio bem definido e evolução incremental por MVPs.

---

## 🧱 Arquitetura

O projeto segue o padrão de **Monólito Modular**, organizado por módulos de domínio.

src/main/java/com/dro
├── modules
│ └── auth
│ ├── api
│ ├── application
│ ├── domain
│ └── infra
└── shared


Cada módulo possui separação em:

- **api** → Controllers / DTOs
- **application** → Casos de uso
- **domain** → Entidades e regras de negócio
- **infra** → Repositórios e integrações

Essa estrutura permite evolução futura para múltiplos bounded contexts (PvP, Clãs, Torre, etc.) sem necessidade de migração para microserviços.

---

## 🚀 Stack Tecnológica

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Flyway (controle de migrations)
- PostgreSQL
- Maven

---

## 🗄 Banco de Dados

Banco utilizado: **PostgreSQL**

As migrations são controladas via **Flyway**, localizadas em:

src/main/resources/db/migration


Migration atual:

- `V1__init_schema.sql`

---

## 🔥 Funcionalidades Implementadas (MVP 1 – Fase Inicial)

✔ Cadastro simples de usuário
✔ Login de usuário
✔ Selecionar e chocar digitama
✔ Adicionar experiência a um digimon manualmente
✔ Digievolução
✔ Renascimento
✔ Missões
✔ Inventário
✔ Incubação




