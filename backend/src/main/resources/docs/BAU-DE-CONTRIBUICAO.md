# Baú de Contribuição

Cada missão de clã aceita individualmente concede **1 Baú de Contribuição** ao jogador no resgate. O clã continua recebendo seu XP e seus Honor Marks, inclusive o multiplicador de HM existente. Aceitar ou apenas concluir o progresso não entrega o item: é necessário resgatar.

## Regras

- Todas as missões fixas usam o mesmo baú; vale também para missões já aceitas e ainda não resgatadas quando a atualização entrar.
- Missões já resgatadas não recebem baús retroativos.
- Continua uma missão ativa por vez e cada missão pode ser aceita uma vez por dia, seguindo o calendário e a expiração já existentes.
- A missão precisa pertencer à conta autenticada, estar em andamento/concluída, ter atingido a meta e pertencer ao clã atual do jogador. Missões expiradas ou de um clã anterior não podem ser resgatadas.
- Não exige Digimon ativo para receber; abrir depois segue as exigências normais de abertura de baús (inclusive parceiro ativo).
- O baú é pessoal, não negociável, não vendável, empilhável até 999. Não é vendido nas lojas.
- Estoque cheio impede o resgate inteiro: abra baús para liberar espaço e tente novamente. Não se perde XP, HM ou baú.

## Pool inicial

Cada abertura sorteia **um único tipo de prêmio**, com quantidade aleatória uniforme no intervalo indicado.

| Chance final | Prêmio | Quantidade | Código |
| --- | --- | --- | --- |
| 35% | Célula de Energia | 2–4 | `ENERGY_CELL` |
| 30% | Disco de XP +3% | 1–2 | `XP_DISC_3` |
| 15% | Cristal de Preservação | 1 | `RARITY_PRESERVATION` |
| 10% | Dado de Raridade | 1 | `RARITY_REROLL` |
| 6% | Núcleo de Ascensão | 1 | `ASCENSION_CORE` |
| 3% | Carga de Energia Total | 1 | `VIP_ENERGY_CHARGE` |
| 1% | Passe VIP de 1 Dia | 1 | `VIP_DAY_PASS` |

Bits não participam deste pool inicial: a abertura existente entrega itens de inventário. Não foram adicionados tipos de moeda, equipamentos ou incubadoras. Os itens mantêm seus efeitos atuais; ganhar um passe não ativa VIP automaticamente.

## Implementação

Migration `V260__clan_contribution_chest.sql`: registra item `CHEST_CLAN_CONTRIBUTION`, baú e tabela `LOOT_TABLE_CLAN_CONTRIBUTION`. Reaproveita abertura, prévia de conteúdo, histórico e auditoria de baús existentes. Abertura em lote continua disponível.

O sorteio existente ocorre em duas etapas: pesos de raridade 35/33/31/1 (Common/Rare/Epic/Legendary) e pesos das entradas dentro de cada grupo. Dentro de Rare, XP tem peso 30 e carga total 3; dentro de Epic, preservação 15, dado 10 e núcleo 6. Isso produz as chances finais da tabela. As raridades de sorteio não alteram o efeito ou a definição dos itens. Ajustes futuros no administrador de loot devem considerar as duas etapas.

O POST de resgate usa uma única transação para item, XP, HM e status CLAIMED. Bloqueia conta, missão e clã para serializar pedidos e evitar atualização perdida entre resgates de membros. A missão é consultada por ID e dono sob lock; uma segunda tentativa encontra CLAIMED e é rejeitada sem novo crédito. Aceite também bloqueia a conta; atualização de progresso e conclusão administrativa bloqueiam as missões para não sobrescrever um resgate concorrente.

Os contratos do catálogo e da missão individual incluem `personalRewardItemCode`, `personalRewardItemName` e `personalRewardQuantity`. A interface separa “Para o clã” e “Para você” e confirma a entrega no inventário após sucesso.

## Validação

Testes de unidade cobrem recompensa, multiplicador de HM, repetição, dono, troca de clã, expiração, progresso insuficiente e falha de inventário; regressões de aceite/progresso e abertura de baús também são executadas. O teste de SQL executa a migration e verifica vínculos, chances e quantidades; o teste DOM valida a apresentação da recompensa. Testes com mocks não substituem teste de carga com múltiplas instâncias e PostgreSQL real.
