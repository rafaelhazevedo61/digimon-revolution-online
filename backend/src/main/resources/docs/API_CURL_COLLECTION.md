# Collection oficial de curls

Os arquivos `backend/src/main/resources/api-curl-collection.sh` e `backend/src/main/resources/collection/DRO - MODULES.postman_collection.json` são as collections oficiais de API do Digimon Revolution Online. Ambos são gerados a partir dos controllers Java do backend e devem ser atualizados sempre que uma rota for criada, removida ou alterada.

> **Importante:** por segurança, todos os comandos curl ficam comentados no arquivo. Descomente e execute apenas a chamada que deseja testar. Não execute a collection inteira, pois ela contém operações de criação, compra, exclusão, alteração e administração.

## Importar no Postman

No Postman, use **Import**, selecione `backend/src/main/resources/collection/DRO - MODULES.postman_collection.json` e importe a collection. Na aba **Variables**, preencha `baseUrl`, `playerToken` e `adminToken`. Os demais valores de rota e query podem ser preenchidos conforme o cenário de teste.

A collection Postman possui 142 requests organizados por módulo, incluindo as rotas de Correio, Casa de Leilões, clãs, convites, ações de mensagens, comunicados administrativos, premiações de eventos e seleção de destinatários. Os exemplos de corpo JSON dos principais fluxos já estão preenchidos, mas devem ser revisados antes do envio.

## Como configurar

A collection usa `localhost:8080` como endereço padrão. É possível substituir a URL e os tokens por variáveis de ambiente:

```bash
BASE_URL=http://localhost:8080 \
TOKEN='Bearer SEU_TOKEN_DE_JOGADOR' \
ADMIN_TOKEN='Bearer SEU_TOKEN_DE_ADMIN' \
source backend/src/main/resources/api-curl-collection.sh
```

O arquivo também declara variáveis de rota, como `CLAN_ID`, `MESSAGE_ID`, `LISTING_ID` e `PLAYER_ID`. Elas podem ser substituídas antes de copiar um comando para o terminal.

```bash
export BASE_URL=http://localhost:8080
export TOKEN='Bearer SEU_TOKEN_DE_JOGADOR'
export ADMIN_TOKEN='Bearer SEU_TOKEN_DE_ADMIN'
export CLAN_ID='id-do-clã'
```

Os comandos protegidos usam o header `Authorization`. Os endpoints administrativos usam `ADMIN_TOKEN`; os endpoints de jogador usam `TOKEN`. Endpoints públicos, como autenticação e listagens públicas, não precisam desse header.

## Como atualizar

Depois de alterar qualquer `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping` ou `@DeleteMapping`, execute:

```bash
python3 scripts/generate_api_curl_collection.py
bash -n backend/src/main/resources/api-curl-collection.sh
```

O gerador percorre os controllers em `backend/src/main/java`, cria um curl e um request Postman para cada rota encontrada e preserva a organização por grupo de endpoint. Os corpos JSON dos principais endpoints possuem exemplos; os demais podem aparecer como `{}` e devem ser preenchidos conforme o DTO do request antes de executar.

## Grupos cobertos

A collection inclui os endpoints públicos e autenticados de autenticação, jogadores, Digimons, evolução, inventário, storage, incubação, missões, loja, arena, ranking, World Boss, Casa de Leilões, clãs, raids, Correio e tutorial. Ela também inclui as rotas administrativas de jogadores, Digimons, equipamentos, missões, loja, bosses, ferramentas, servidor e comunicados oficiais.

Os fluxos adicionados recentemente também estão presentes:

| Fluxo | Endpoint |
|---|---|
| Enviar mensagem de jogador | `POST /mail` |
| Aceitar ou recusar convite de clã | `POST /mail/{messageId}/action` |
| Enviar convite de clã | `POST /clans/{id}/invite` |
| Enviar comunicado administrativo | `POST /admin/mail/announcements` |
| Criar premiação de evento | `POST /admin/mail/event-rewards` |
| Listar clãs para premiação | `GET /admin/mail/recipients/clans` |
| Listar membros de um clã | `GET /admin/mail/recipients/clans/{clanId}/members` |
| Pesquisar jogadores para seleção | `GET /admin/mail/recipients/players` |
| Comprar anúncio | `POST /auction/listings/{listingId}/buy` |
| Cancelar anúncio | `POST /auction/listings/{listingId}/cancel` |

A collection não armazena tokens reais nem dados pessoais. Use variáveis de ambiente ou substitua os placeholders apenas localmente.
