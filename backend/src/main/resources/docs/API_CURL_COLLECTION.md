# Collection oficial de curls

O arquivo `backend/src/main/resources/api-curl-collection.sh` é a collection oficial de exemplos curl do Digimon Revolution Online. Ele é gerado a partir dos controllers Java do backend e deve ser atualizado sempre que uma rota for criada, removida ou alterada.

> **Importante:** por segurança, todos os comandos curl ficam comentados no arquivo. Descomente e execute apenas a chamada que deseja testar. Não execute a collection inteira, pois ela contém operações de criação, compra, exclusão, alteração e administração.

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

O gerador percorre os controllers em `backend/src/main/java`, cria um curl para cada rota encontrada e preserva a organização por grupo de endpoint. Os corpos JSON aparecem como `{}` porque os DTOs e regras de cada operação podem exigir valores específicos; substitua esse placeholder pelos campos do request correspondente antes de executar.

## Grupos cobertos

A collection inclui os endpoints públicos e autenticados de autenticação, jogadores, Digimons, evolução, inventário, storage, incubação, missões, loja, arena, ranking, World Boss, Casa de Leilões, clãs, raids, Correio e tutorial. Ela também inclui as rotas administrativas de jogadores, Digimons, equipamentos, missões, loja, bosses, ferramentas, servidor e comunicados oficiais.

Os fluxos adicionados recentemente também estão presentes:

| Fluxo | Endpoint |
|---|---|
| Enviar mensagem de jogador | `POST /mail` |
| Aceitar ou recusar convite de clã | `POST /mail/{messageId}/action` |
| Enviar convite de clã | `POST /clans/{id}/invite` |
| Enviar comunicado administrativo | `POST /admin/mail/announcements` |
| Comprar anúncio | `POST /auction/listings/{listingId}/buy` |
| Cancelar anúncio | `POST /auction/listings/{listingId}/cancel` |

A collection não armazena tokens reais nem dados pessoais. Use variáveis de ambiente ou substitua os placeholders apenas localmente.
