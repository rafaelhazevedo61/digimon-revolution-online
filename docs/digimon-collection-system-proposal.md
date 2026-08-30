# Proposta de sistema de coleção de Digimons

**Projeto:** Digimon Revolution Online  
**Versão:** 2.0 — proposta refinada  
**Data:** 29 de agosto de 2026  
**Autor:** Manus AI

## 1. Conceito central

A coleção deve ser uma progressão paralela à evolução, aos atributos e ao Rebirth. O jogador obtém um Digimon, decide se deseja preservá-lo para uso ou sacrificá-lo para registrar uma descoberta permanente na coleção.

A unidade recomendada é uma combinação única de **espécie + raridade**. Um Agumon Comum e um Agumon Raro são duas entradas diferentes; três Agumons Comuns continuam valendo apenas uma entrada.

A coleção é, portanto, um **álbum de descobertas consumidas voluntariamente**, e não apenas uma lista automática de Digimons que passaram pela conta.

## 2. Item de registro: Digivice

O registro deverá exigir um item consumível. O nome provisório recomendado é **Digivice de Registro**. O nome pode ser alterado futuramente para algo mais temático, como Arquivo Digital, Scanner de Dados ou Digivice de Coleção.

O item deve funcionar como uma confirmação de que o jogador decidiu abrir mão daquele Digimon em troca de progresso permanente na coleção.

### Fluxo do jogador

1. O jogador abre a tela **Coleção**.
2. Seleciona uma espécie/raridade ainda não registrada.
3. O sistema exibe quais Digimons elegíveis podem ser usados.
4. O jogador seleciona um Digimon e um Digivice de Registro.
5. A interface mostra uma confirmação clara: o Digimon será consumido e não poderá ser recuperado.
6. Após a confirmação, o Digimon é consumido, o Digivice é consumido e a entrada é registrada.
7. O ponto de coleção, os marcos alcançados e eventual maestria da espécie são atualizados na mesma transação.

> **Regra importante:** o Digimon não é registrado automaticamente ao ser chocado, recebido ou renascido. Ele só entra na coleção quando o jogador executa o fluxo de registro e aceita seu consumo.

### Elegibilidade

O Digimon selecionado deve pertencer ao jogador, estar no Storage ou ser o Digimon ativo conforme a regra de interface escolhida, não estar em missão, não possuir equipamento equipado e não estar bloqueado por outro fluxo. Para evitar acidentes, a primeira versão deve impedir o consumo do Digimon ativo e exigir que ele seja enviado ao Storage antes do registro.

A combinação espécie/raridade já registrada não deve aparecer como objetivo disponível. Cópias duplicadas podem continuar sendo exibidas no inventário, mas o sistema deve informar que elas não concederão pontos adicionais.

| Recurso | Consumido? | Momento |
|---|---:|---|
| Digimon selecionado | Sim | Muda de `STORED` ou `HATCHED` para `COLLECTION_CONSUMED` após confirmação e validação final. |
| Digivice de Registro | Sim | Na mesma transação do consumo do Digimon. |
| Entrada da coleção | Não | Permanente após a transação confirmada. |
| Ponto de coleção | Não é item | Derivado da quantidade de entradas únicas. |

## 3. Regras de duplicidade e Rebirth

Cada jogador pode possuir apenas uma entrada para cada combinação `digimon_info_id + rarity`. A restrição recomendada no banco é `UNIQUE (player_id, digimon_info_id, rarity)`.

O Digimon consumido deixa de existir como unidade jogável, mas a descoberta permanece para sempre. Tecnicamente, a recomendação é adicionar um novo estado terminal `COLLECTION_CONSUMED` ao enum `DigimonStatus`, em vez de reutilizar `SACRIFICED`. Vender, sacrificar ou renascer outros Digimons não remove entradas já registradas.

O projeto já possui os estados `REBORN` e `SACRIFICED` — não `REBORNED`. `REBORN` identifica o Digimon antigo que originou um novo ciclo de Rebirth, enquanto `SACRIFICED` identifica a conversão em Dados Digitais. `COLLECTION_CONSUMED` identifica especificamente o consumo voluntário para registro na coleção, preservando a finalidade correta para auditoria, consultas, suporte e eventuais regras futuras.

O Rebirth também não deve registrar nada automaticamente. Se o jogador quiser colecionar a nova raridade ou a nova forma resultante do Rebirth, deverá voltar à tela Coleção, selecionar o novo Digimon e usar outro Digivice de Registro.

Essa regra torna a coleção uma escolha estratégica. O jogador deverá decidir se vale a pena consumir um Digimon raro ou se é melhor mantê-lo para combate, Rebirth, troca futura ou progressão de linhagem.

A proteção de raridade desenvolvida em paralelo permanece independente da coleção:

1. **Raridade atual** continua sendo o valor usado no Digimon.
2. **Origem da raridade** continua indicando uma alteração feita pelo Dado de Raridade.
3. **Entrada da coleção** registra que o jogador consumiu voluntariamente um Digimon daquela espécie e raridade.

O Dado de Raridade não deve criar entrada de coleção automaticamente. O jogador pode usar o Dado, aguardar o resultado e depois decidir se consumirá o Digimon resultante para registrá-lo.

## 4. Pontos de coleção

Cada entrada nova vale **1 ponto de coleção**. O total deve ser obtido a partir da quantidade de entradas únicas persistidas, em vez de depender somente de um contador incrementado por eventos.

Uma tentativa concorrente de registrar duas cópias da mesma combinação deve resultar em apenas uma entrada e apenas um ponto. A criação da entrada, o consumo do Digimon, o consumo do Digivice e a avaliação de marcos devem ocorrer dentro da mesma transação PostgreSQL.

## 5. Marcos e recompensa provisória

Enquanto as recompensas definitivas não forem decididas, todos os marcos podem conceder uma recompensa genérica: **1 Disco de XP de 20%**.

Isso permite testar o ciclo completo — registrar, pontuar, desbloquear e resgatar — sem criar dependência de uma economia final ainda não definida.

| Marco | Recompensa provisória | Observação |
|---:|---|---|
| 10 pontos | 1 Disco de XP de 20% | Primeiro marco. |
| 50 pontos | 1 Disco de XP de 20% | Marco intermediário. |
| 100 pontos | 1 Disco de XP de 20% | Marco de longo prazo. |
| 150 pontos | 1 Disco de XP de 20% | Pode ser repetido enquanto o catálogo crescer. |
| 200 pontos ou mais | 1 Disco de XP de 20% | Marcos configuráveis pelo conteúdo. |
| 250 pontos | 1 Disco de XP de 20% | Marco avançado. |
| 300 pontos | 1 Disco de XP de 20% | Marco avançado de longo prazo. |

O item deve ser entregue apenas uma vez por marco. O jogador pode receber a recompensa automaticamente ao atingir o marco ou clicar em **Resgatar** na tela de coleção. A recomendação para a primeira versão é usar resgate manual, pois isso torna o efeito visível e facilita a substituição futura das recompensas.

Os estados recomendados são `LOCKED`, `AVAILABLE`, `CLAIMED` e `REVOKED`. O resgate precisa ser idempotente: reconexões, cliques repetidos e requisições simultâneas não podem gerar dois Discos de XP.

## 6. Completar todas as raridades de uma espécie

O jogador completa a coleção de uma espécie quando registra uma entrada para cada raridade válida daquela espécie. Considerando as raridades atuais Comum, Rara, Épica e Lendária, o conjunto de Agumon exige quatro registros:

- Agumon Comum;
- Agumon Raro;
- Agumon Épico;
- Agumon Lendário.

A comparação deve usar o catálogo oficial de raridades ativo no servidor, e não uma lista fixa gravada no código. Se uma nova raridade for adicionada no futuro, a regra de completude deverá passar a considerar essa raridade conforme a política de conteúdo definida para o sistema.

## 7. Quando o bônus é aplicado

O bônus deve ser ativado **imediatamente após a transação que registra a última raridade faltante ser confirmada**.

Exemplo:

1. O jogador já registrou Agumon Comum, Raro e Épico.
2. Ele seleciona um Agumon Lendário.
3. Confirma o uso do Digivice.
4. O Agumon Lendário é consumido.
5. A entrada Lendária é gravada.
6. O sistema verifica que as quatro raridades agora existem.
7. A Maestria de Agumon é desbloqueada na mesma transação.
8. Depois do commit, a interface informa que o bônus está ativo.

O bônus não deve ser aplicado antes do commit. Se o consumo ou o registro falhar, a maestria não pode ser desbloqueada.

### O Digimon consumido recebe o bônus?

Não. O Digimon usado no registro é consumido e deixa de existir como unidade jogável. Portanto, não faria sentido aplicar o bônus a ele.

A interpretação recomendada é que o bônus pertence à **maestria da espécie do jogador**. Assim, depois de completar todas as raridades, o bônus passa a valer imediatamente para:

- qualquer Digimon daquela espécie que o jogador já possua;
- qualquer Digimon daquela espécie criado ou recebido no futuro;
- o novo Digimon gerado por um Rebirth futuro, caso ele pertença àquela espécie.

Se o Digimon consumido era o único daquela espécie na conta, a maestria fica desbloqueada mesmo sem existir uma unidade atual para receber o efeito. Quando o jogador obtiver outro Digimon da mesma espécie, o bônus já estará pronto e será aplicado automaticamente.

## 8. Forma de cálculo do bônus

O bônus deve ser aplicado como um modificador dinâmico no cálculo final dos atributos, e não por meio de uma alteração permanente nos valores armazenados de HP, ATK ou DEF.

A fórmula conceitual seria:

```text
statFinal = floor(
  statBaseCalculado
  * rarityMultiplier
  * personalityMultiplier
  * traitMultiplier
  * rebirthMultiplier
  * collectionMasteryMultiplier
  * equipmentMultiplier
)
```

O `collectionMasteryMultiplier` deve ser `1.00` enquanto a espécie não estiver completa. Ao completar a espécie, ele passa a usar o valor configurado para aquela maestria.

Para a primeira implementação, recomendo começar com uma bonificação pequena e igual para os três atributos, por exemplo **+0,5% de HP, ATK e DEF**. Uma alternativa ainda mais segura é iniciar apenas com um efeito visual ou bônus de experiência. Caso seja usado bônus de atributos:

| Regra | Valor inicial recomendado |
|---|---:|
| Bônus por espécie completa | +0,5% em HP, ATK e DEF |
| Acúmulo por cópias da mesma espécie | Não acumula |
| Acúmulo por outras espécies completas | Não aplicar globalmente; cada espécie afeta somente a própria espécie |
| Alteração de raridade | Não altera |
| Alteração de IV ou grade | Não altera |
| Interação com Rebirth | Multiplicador separado e com teto próprio |

A atualização deve aparecer imediatamente após a confirmação do registro. Se o jogador tiver outro Digimon da espécie, seus atributos exibidos devem ser recalculados ao recarregar o dashboard ou a tela de detalhes. Não é necessário reescrever os valores persistidos do Digimon.

## 9. Escopo do bônus por espécie

A maestria deve ser vinculada a `player_id + digimon_info_id`, e não a uma cópia individual consumida. O jogador não ganha um bônus global para todos os Digimons, nem precisa manter os quatro Digimons usados na coleção.

Exemplo:

| Espécie | Estado | Efeito |
|---|---|---|
| Agumon | Todas as raridades registradas | Agumons do jogador recebem a maestria de Agumon. |
| Gabumon | Apenas Comum e Raro | Nenhum bônus de maestria ainda. |
| Patamon | Nenhuma entrada | Nenhum bônus de maestria. |

A maestria deve ser desbloqueada uma única vez e não deve ser desfeita se o jogador vender ou consumir o último Digimon restante daquela espécie. Isso preserva o valor da decisão de coleção e evita que o jogador perca um bônus já conquistado por causa de uma operação posterior.

## 10. Modelo técnico recomendado

O módulo pode existir como um domínio próprio chamado `collection`, sem acoplar seu estado ao ciclo de vida de Rebirth.

| Componente | Responsabilidade |
|---|---|
| `digimon_collection_entries` | Registros únicos de espécie/raridade consumidos pelo jogador. |
| `collection_milestones` | Catálogo de marcos, pontuação necessária e recompensa configurável. |
| `player_collection_milestones` | Estado de desbloqueio e resgate por jogador. |
| `collection_species_masteries` | Maestrias de espécies completadas. |
| `CollectionRegistrationUseCase` | Validar e executar consumo do Digimon e do Digivice. |
| `GetCollectionUseCase` | Retornar álbum, pontos, marcos e maestrias. |
| `ClaimCollectionMilestoneUseCase` | Entregar o Disco de XP com lock e idempotência. |

A entrada de coleção deve conter pelo menos `id`, `player_id`, `digimon_info_id`, `rarity`, `source_digimon_id`, `source_event`, `discovered_at` e timestamps. O `source_digimon_id` é útil para auditoria, mas não deve fazer parte da chave única.

A maestria deve conter `player_id`, `digimon_info_id`, `unlocked_at`, `status` e, se necessário, uma versão do catálogo de raridades usada no momento da conclusão. O status pode começar com `ACTIVE` e `REVOKED`, embora a revogação deva ficar restrita a correções administrativas auditadas.

## 11. Fluxo transacional de registro

O fluxo deve ser executado dentro de uma transação PostgreSQL:

1. Bloquear o jogador e o Digimon selecionado.
2. Validar ownership, status, Storage, missão, equipamentos e elegibilidade.
3. Verificar se a combinação espécie/raridade ainda não foi registrada.
4. Verificar e consumir um Digivice de Registro.
5. Marcar o Digimon como `COLLECTION_CONSUMED`, mantendo o registro histórico da entidade no banco.
6. Inserir a entrada de coleção com a raridade final.
7. Calcular o novo total de pontos.
8. Criar os marcos que passaram para `AVAILABLE`.
9. Verificar se todas as raridades da espécie foram registradas.
10. Criar a maestria da espécie, se for a última raridade faltante.
11. Criar auditoria no Transactional Outbox.
12. Confirmar a transação e retornar o resumo atualizado.

Se qualquer passo falhar, nenhum recurso deve ser consumido e nenhuma entrada deve ser criada. Caso a combinação já tenha sido registrada, o sistema deve impedir o consumo do Digimon e do Digivice, informando que aquela descoberta já existe.

## 12. Interface e mensagens

A tela deve deixar o custo irreversível explícito. Uma confirmação recomendada seria:

> **Registrar Agumon — Lendário?**  
> Este Digimon será consumido permanentemente. Você gastará 1 Digivice de Registro e receberá 1 ponto de coleção. Esta ação não pode ser desfeita.

Após o sucesso, a interface deve informar:

> **Nova entrada registrada:** Agumon — Lendário.  
> **Pontos de coleção:** 50.  
> **Maestria de Agumon desbloqueada:** bônus aplicado aos seus Agumons.

A tela de detalhes da espécie deve mostrar as raridades já registradas, as raridades faltantes, o estado da maestria e o efeito atualmente ativo. O Digivice deve aparecer no inventário com descrição clara e quantidade disponível.

## 13. Ordem recomendada de implementação

| Etapa | Entrega | Critério de aceite |
|---:|---|---|
| 1 | Item Digivice de Registro | O item possui definição, quantidade e consumo transacional. |
| 2 | Registro manual de Digimon | Digimon e Digivice são consumidos somente após confirmação válida. |
| 3 | Restrição de espécie/raridade | Duplicatas não podem consumir recursos nem gerar pontos. |
| 4 | Pontos e marcos | 10, 50 e 100 pontos liberam 1 Disco de XP de 20% uma única vez. |
| 5 | Tela Coleção | O jogador vê entradas, raridades faltantes, pontos e marcos. |
| 6 | Maestria por espécie | A última raridade desbloqueia a maestria na mesma transação. |
| 7 | Aplicação dinâmica do bônus | Digimons atuais e futuros da espécie recebem o modificador. |
| 8 | Recompensas definitivas | Substituir o Disco de XP por recompensas de design aprovadas. |

A etapa 7 pode ficar protegida por configuração ou feature flag. O sistema já será útil nas etapas 1 a 5, permitindo validar se os jogadores entendem e aceitam a decisão de consumir um Digimon.

## 14. Decisões recomendadas

As decisões consolidadas são: **(1)** o jogador precisa usar um Digivice de Registro; **(2)** o Digimon selecionado é consumido permanentemente; **(3)** cada combinação única de espécie e raridade vale um ponto; **(4)** os marcos concedem inicialmente um Disco de XP de 20%; **(5)** a coleção não é automática após hatch ou Rebirth; **(6)** a maestria é desbloqueada no commit da última raridade faltante; e **(7)** o bônus pertence à espécie na conta, não ao Digimon consumido.

Esse desenho cria uma escolha real para o jogador, protege o sistema contra farm de cópias, oferece uma recompensa provisória simples e deixa claro que o bônus começa a valer imediatamente após a conclusão do conjunto — para qualquer Digimon atual ou futuro daquela espécie.

## Referências internas

[1]: ../docs/digimon-rarity-reroll.md "Dado de Raridade de Digimon"
[2]: ../docs/roadmap-next-sprints.md "Roadmap de próximas sprints — Digimon Revolution Online"
[3]: ../official-site/wiki/estagios.html "Estágios, evolução e Rebirth"
[4]: ../official-site/wiki/digimons.html "Digimons, atributos e cálculo de status"
