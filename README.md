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

✔ Estrutura base do projeto  
✔ Integração com PostgreSQL  
✔ Flyway configurado  
✔ Arquitetura modular definida  
✔ Módulo `auth` implementado  
✔ Endpoint de cadastro de jogador  

### Endpoint disponível:

POST /auth/register

Exemplo de requisição:

```json
{
  "username": "rafa",
  "email": "rafa@email.com",
  "password": "123456"
}

```

▶ Como Rodar o Projeto
1️⃣ Criar banco PostgreSQL

Criar banco:

CREATE DATABASE dro_db;

2️⃣ Configurar application.yml

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dro_db
    username: postgres
    password: SUA_SENHA_AQUI

3️⃣ Rodar aplicação

Via Maven:

mvn spring-boot:run

ou via IDE.

🛣 Roadmap
MVP 1 (Fundação)

 Cadastro de jogador

 Login

 Digitama inicial

 Criação de Digimon

 Sistema simples de missão

 Progressão de level

 Inventário básico

MVP 2

Expedição offline

Sistema completo de evolução

EV treinável

Múltiplos Digimons

MVP 3

PvP manual estratégico

Sistema de habilidades

Ranking individual

Equipamentos completos

MVP 4

Clãs

Guerra de clã

Ranking global

Sistema social robusto

🎯 Objetivo do Projeto

Construir um jogo browser-based inspirado no universo Digimon, com foco em:

Progressão estratégica

Colecionismo

Competição

Sistemas sociais avançados

Arquitetura escalável e bem estruturada

📌 Status Atual

Projeto em desenvolvimento ativo – nova base arquitetural criada.

---

# ✅ Próximo passo recomendado

Faça commit com algo como:

