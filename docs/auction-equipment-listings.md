# Equipamentos na Casa de Leilões

## Contrato e compatibilidade

A publicação existente de itens não muda: `itemDefinitionId`, `quantity`,
`unitPrice`, `durationHours`. `listingType` pode ser omitido no contrato legado.

Equipamento usa `POST /auction/listings`:

```json
{
  "listingType": "EQUIPMENT",
  "equipmentId": "UUID-da-instancia",
  "quantity": 1,
  "unitPrice": 10000,
  "durationHours": 24
}
```

Exatamente um identificador deve ser informado. Quantidade de equipamento deve ser
1 na publicação e na compra. Preço, taxa de 100 Bits, limite de 10 anúncios e
comissões de 5%/7,5%/10% continuam regidos por AuctionRules.

Respostas de listagem, compra e histórico acrescentam `listingType`,
`equipmentId` e `equipment` (snapshot de nome, slot, raridade, set, tier,
refinamento, ascensão, bônus base e efetivos). Os campos legados de apresentação
continuam disponíveis; `itemDefinitionId` é null para equipamento.

O filtro `category=EQUIPMENT` lista peças. Busca aceita nome/set; raridade usa a
raridade da instância. O formulário possui seleção Itens/Equipamentos.

## Custódia e consistência

- Migration nova: V232. Não alterar migrations já aplicadas.
- A mesma linha em inventory_equipments passa de AVAILABLE para AUCTION_ESCROW.
- player_id continua identificando o vendedor durante a custódia, mas todas as
  consultas de inventário utilizável excluem a peça; ela não pode ser equipada,
  refinada, ascendida, aprimorada, desmontada, bloqueada ou vendida na loja.
- Publicação rejeita peças equipadas, com digimon_id preenchido, bloqueadas,
  de outro jogador ou já reservadas.
- Compra troca apenas a posse e disponibilidade; o UUID, created_at e atributos
  são preservados. Cancelamento/expiração liberam a mesma peça ao vendedor sem
  depender do Digimon de origem.
- Equipamento, anúncio, Bits, transação, correio e auditoria usam a transação
  existente da aplicação. Custódia exige uma transação ativa.
- O anúncio é bloqueado antes da liquidação. Mutações de equipamentos usam
  bloqueio pessimista, revalidam posse e disponibilidade após adquirir o lock.
- O índice único impede dois anúncios ACTIVE da mesma peça, incluindo anúncios
  expirados cuja devolução ainda não tenha sido concluída.
- Snapshot pertence ao anúncio e é reutilizado pelo histórico da transação.
  equipment_id é identidade histórica sem FK: destruir uma peça após sua compra
  não deve apagar ou impedir consultar o histórico.
- Não há transferência para um jogador fictício nem exclusão/recriação da peça.

## Testes automatizados

```sh
node --check game-frontend/assets/js/auction-house.js
node --test scripts/auction-equipment.test.cjs
cd backend
sh mvnw -Dtest=AuctionEquipmentFlowTest,AuctionAssetRequestTest,AuctionListingTest,AuctionRulesTest,AuctionMailNotificationFactoryTest,EquipUseCaseTest,UnequipUseCaseTest,UnequipAllUseCaseTest,SellShopProductUseCaseTest test
```

Os testes JS cobrem seleção por UUID, quantidade fixa, exclusão de equipamentos
bloqueados/equipados, payload legado, snapshot nos cards, escape de HTML e busca.

Os testes Java cobrem publicação, taxas/limite, posse, reserva duplicada, compra,
saldo insuficiente, segunda compra, cancelamento, expiração idempotente,
preservação de atributos, snapshot/correio, bloqueio das ações paralelas e itens
empilháveis com compra parcial. São testes de unidade/aplicação com repositórios
mockados: não substituem validação transacional no PostgreSQL.

Nesta execução, os 6 testes JS passaram. A compilação e os testes Java ficaram
bloqueados por falha de resolução de repo.maven.apache.org, antes da compilação.
Não foram executadas migrations ou alterações em banco de produção.

## Homologação obrigatória antes do merge

Usar ambiente de testes com PostgreSQL e dois jogadores, nunca banco de produção.

1. Aplicar V232 e iniciar a aplicação com validação JPA; confirmar queries de
   mercado, meus anúncios, histórico e inventário. Confirmar anúncios antigos.
2. Publicar peça com refinamento/ascensão, registrar UUID/atributos/created_at e
   confirmar que desaparece de todas as listas de inventário utilizável.
3. Consultar mercado misto, filtros EQUIPMENT/raridade e busca por set; abrir
   confirmação em desktop e mobile. Conferir todos os atributos e preço.
4. Comprar, conferir mesmo UUID/atributos, posse do comprador, não equipada,
   débito único e crédito líquido da comissão.
5. Publicar outra peça e cancelar; repetir com expiração. Trocar/remover o
   Digimon ativo do vendedor antes da devolução e confirmar retorno ao jogador.
6. Enviar duas compras simultâneas para o mesmo anúncio usando conexões distintas:
   apenas uma conclui; a outra retorna conflito sem cobrar ou transferir.
7. Disputar compra contra cancelamento e execução do job de expiração; confirmar
   resultado único e ausência de duplicação/devolução após venda.
8. Disputar publicação contra equip/refine/ascend/enhance/dismantle/shop-sell/lock;
   uma operação deve ser recusada após revalidar a reserva e a posse.
9. Em teste transacional, provocar exceção após a transferência/cobrança (por
   exemplo no repositório de transações), e conferir rollback integral de Bits,
   estoque, equipamento e status do anúncio.
10. Após compra, refinar/desmontar/revender a peça: o histórico da venda anterior
    deve conservar o snapshot original e continuar acessível após exclusão.
11. Regressão: publicar itens empilháveis, comprar parcialmente, cancelar,
    expirar, conferir maxStack, taxas, correio e histórico.
