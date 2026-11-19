# Convenções de endpoints internos

## Objetivo

Ferramentas de simulação, debug, grant, reset e seed são operações internas de desenvolvimento ou administração. Elas não fazem parte do namespace público do jogador e devem ficar protegidas pelo interceptor de autenticação administrativa.

## Regra obrigatória

Todo endpoint de simulação, debug, grant, reset ou seed deve usar um caminho sob `/admin/**`.

O caminho deve ser organizado pelo recurso, por exemplo:

- `/admin/digimon/simulator/trait-hatch`;
- `/admin/inventory/grant`;
- `/admin/tools/reset-daily-arena-attacks`.

O `AdminAuthInterceptor` é registrado para `/admin/**` e exige um JWT válido cujo `userType` seja `ADMIN`. Não crie uma rota pública equivalente para a mesma ferramenta.

## Checklist para novos endpoints

Antes de adicionar um endpoint interno:

- [ ] A operação é realmente uma ferramenta de simulação, debug, grant, reset ou seed?
- [ ] O `@RequestMapping` final começa com `/admin/`?
- [ ] A operação exige o header `Authorization` e é coberta pelo `AdminAuthInterceptor`?
- [ ] A coleção cURL e a coleção Postman usam `ADMIN_TOKEN` ou `adminToken`?
- [ ] O frontend público não chama essa rota?
- [ ] O endpoint e sua autorização foram incluídos na documentação e nos testes relevantes?
