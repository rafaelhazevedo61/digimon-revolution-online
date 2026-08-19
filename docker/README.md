# Execução local com Docker

Esta pasta contém a stack local do Digimon Revolution Online. O `docker-compose.yml` sobe o PostgreSQL e o backend Spring Boot na mesma rede Docker, com persistência do banco em volume nomeado.

## Primeira execução

Copie o arquivo de exemplo e altere os valores locais quando necessário:

```bash
cp docker/.env.example docker/.env
```

Suba a stack a partir da raiz do repositório. O parâmetro `--env-file` garante que o Compose use o arquivo correto:

```bash
docker compose --env-file docker/.env -f docker/docker-compose.yml up --build -d
```

O PostgreSQL ficará exposto em `localhost:5432` e a API em `http://localhost:8080`. O backend utiliza o nome do serviço `dro-postgres` para acessar o banco dentro da rede Docker.

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

O serviço da API só é iniciado depois que o healthcheck do PostgreSQL fica saudável. O Spring Boot executa as migrations Flyway durante a inicialização.

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

Se a API não conectar ao banco, confirme se o healthcheck está saudável e se as variáveis `POSTGRES_DB`, `POSTGRES_USER` e `POSTGRES_PASSWORD` são iguais para os dois serviços. Não utilize credenciais de desenvolvimento em produção.
