# Execução local com Docker

Esta pasta contém a stack local do Digimon Revolution Online. O `docker-compose.yml` sobe o PostgreSQL, o MongoDB e o backend Spring Boot na mesma rede Docker, com persistência dos dois bancos em volumes nomeados.

## Primeira execução

Copie o arquivo de exemplo e altere os valores locais quando necessário:

```bash
cp docker/.env.example docker/.env
```

Suba a stack a partir da raiz do repositório. O parâmetro `--env-file` garante que o Compose use o arquivo correto:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml up --build -d
```

O PostgreSQL ficará exposto em `localhost:5432`, o MongoDB em `localhost:27017` e a API em `http://localhost:8080`. O backend utiliza os nomes dos serviços `dro-postgres` e `dro-mongodb` para acessar os bancos dentro da rede Docker.

| Serviço | Endereço local | Finalidade |
|---|---|---|
| PostgreSQL | `localhost:5432` | Estado oficial do jogo |
| MongoDB | `localhost:27017` | Auditoria e logs persistentes |
| API | `http://localhost:8080` | Backend Spring Boot |

## Verificação

Consulte o estado dos serviços:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml ps
```

Consulte os logs da API:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml logs -f dro-api
```

Consulte os logs do PostgreSQL:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml logs -f dro-postgres
```

Consulte os logs do MongoDB:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml logs -f dro-mongodb
```

O serviço da API só é iniciado depois que os healthchecks do PostgreSQL e do MongoDB ficam saudáveis. O Compose monta internamente a variável `SPRING_MONGODB_URI` com o hostname Docker `dro-mongodb`; não é necessário cadastrar essa URI manualmente no `.env`. O Spring Boot executa as migrations Flyway durante a inicialização e conecta a auditoria ao banco `dro_audit`.

A variável `SPRING_DATA_MONGODB_AUTO_INDEX_CREATION` fica desabilitada por padrão para que uma indisponibilidade temporária do MongoDB não impeça a API de iniciar. Em um ambiente local no qual seja necessário validar a criação automática dos índices, altere explicitamente essa variável para `true` no `docker/.env`.

## Parar e reiniciar

Para parar os containers mantendo os dados:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml down
```

Para reconstruir somente a API:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml build dro-api
docker compose --env-file docker/.env -f docker/docker-compose.yml up -d dro-api
```

Para remover também o volume local do PostgreSQL, destruindo os dados de desenvolvimento:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml down -v
```

Use `down -v` somente quando realmente desejar recriar o banco do zero.

## Diagnóstico

Renderize a configuração final sem iniciar os serviços:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml config
```

Se a API não conectar aos bancos, confirme se os healthchecks estão saudáveis e se as variáveis `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `MONGO_ROOT_USERNAME`, `MONGO_ROOT_PASSWORD` e `MONGO_DATABASE` são consistentes. Para confirmar a configuração efetiva da API sem expor a senha, verifique se o Compose renderiza `SPRING_MONGODB_URI` com `dro-mongodb:27017`. Não utilize credenciais de desenvolvimento em produção.
