# Revisão da superfície administrativa

## Escopo e convenção

Esta revisão cobre os endpoints administrativos existentes na base `develop` após
a proteção do simulador de traits. Toda a superfície abaixo está sob `/admin/**`
e é protegida pelo `AdminAuthInterceptor`. Ferramentas de simulação, debug,
grant, reset e seed seguem a convenção documentada em
[`internal-endpoint-guidelines.md`](internal-endpoint-guidelines.md).

O interceptor valida assinatura, issuer e expiração do JWT, extrai o `sub`,
carrega o jogador atual no banco e exige `userType == ADMIN`. Assim, uma
promoção ou rebaixamento passa a valer imediatamente, inclusive para tokens
emitidos anteriormente. Tokens ausentes ou inválidos retornam `401`; jogadores
inexistentes ou que não são administradores retornam `403`.

## Inventário

| Endpoint | Auditoria | Observação |
|---|---:|---|
| `POST/GET/PUT/PATCH /admin/equipment-templates[...]` | não | CRUD de catálogo |
| `POST /admin/equipment-templates/grant` | sim | concede equipamento a Digimon |
| `POST/GET/PUT/PATCH /admin/loot-tables[...]` | existente | configuração de Loot Tables |
| `GET /admin/loot-tables/catalog/items` | existente | catálogo de itens |
| `GET/PUT/PATCH /admin/chests[...]` | existente | configuração de baús |
| `POST/GET/PUT/PATCH /admin/shop-products[...]` | não | CRUD de catálogo |
| `POST/GET/PUT/PATCH /admin/missions[...]` | não | CRUD de catálogo |
| `GET /admin/missions/chest-options` | não | opções de baús |
| `GET/POST/PUT/DELETE /admin/bosses[...]` | não | CRUD e drops |
| `GET/PUT /admin/bosses/rarity-profiles[...]` | existente | perfis de raridade |
| `POST /admin/inventory/grant` | sim | concede item a Digimon |
| `GET /admin/inventory/item-definitions` | não | catálogo |
| `PUT /admin/items/{id}` | não | catálogo |
| `GET /admin/players` | não | consulta administrativa |
| `POST /admin/players/wipe` | sim | exige `{"confirmation":"WIPE"}` |
| `POST /admin/players/{id}/reset-password` | sim | retorna a senha ao painel; auditoria não contém senha |
| `POST /admin/mail/announcements` | não | envio global de correio |
| `POST /admin/mail/event-rewards` | não | criação de premiações |
| `GET /admin/mail/recipients/clans` | não | consulta de destinatários |
| `GET /admin/mail/recipients/clans/{id}/members` | não | consulta de destinatários |
| `GET /admin/mail/recipients/players` | não | consulta de destinatários |
| `POST /admin/digimon/add-xp` | sim | concede experiência |
| `GET /admin/digimon/simulator/trait-hatch` | não | simulador interno |
| `GET /admin/digimon/by-player/{playerId}` | não | consulta administrativa |
| `POST /admin/tools/reset-daily-arena-attacks` | sim | ferramenta global |
| `POST /admin/tools/reset-clan-raid-daily` | sim | ferramenta global |
| `POST /admin/tools/reset-world-boss-daily` | sim | ferramenta global |
| `POST /admin/tools/force-new-world-boss-cycle` | sim | ferramenta global |
| `POST /admin/tools/complete-clan-missions` | sim | ferramenta global |
| `GET /admin/server/damage-buff` | não | consulta estado |
| `POST /admin/server/damage-buff` | sim | altera balanceamento global |
| `POST /admin/server/damage-buff/toggle` | sim | alterna balanceamento global |

## Convenções resultantes

- Todo endpoint administrativo usa caminho sob `/admin/**`.
- O `AdminAuthInterceptor` é a fonte única da autorização de administrador.
- Controllers só recebem `Authorization` quando precisam identificar o ator da
  operação ou encaminhar o token para auditoria.
- Auditorias incluem `actorId`, módulo, operação, alvo e parâmetros relevantes.
- Senhas, hashes, tokens e outros segredos nunca entram em auditoria.
- Operações destrutivas devem exigir confirmação explícita e documentar o
  contrato nas coleções cURL e Postman.
- O `game-frontend` não chama endpoints administrativos.
