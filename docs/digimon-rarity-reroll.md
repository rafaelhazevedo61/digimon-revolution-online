# Reroll de raridade de Digimon

O item `RARITY_REROLL` consome uma unidade do inventário e gera uma nova proposta de raridade para o Digimon ativo. A proposta é persistida para que o jogador possa escolher uma única vez entre aceitar a nova raridade ou manter a anterior.

| Ação | Resultado | Custo em Bits |
|---|---|---:|
| Aceitar nova raridade | Atualiza a raridade do Digimon para a proposta | 0 |
| Manter raridade anterior | Mantém o valor atual e encerra a proposta | 5.000 por padrão |

O custo para manter a raridade é configurável por `DRO_RARITY_REROLL_KEEP_COST_BITS`. O reroll é sorteado usando as mesmas raridades globais (`COMMON`, `RARE`, `EPIC`, `LEGENDARY`) e uma nova raridade igual à atual é descartada para que a proposta sempre apresente uma mudança real.

O item é consumido no momento em que a proposta é criada. A tabela `digimon_rarity_rerolls` registra jogador, Digimon, raridade atual, nova raridade, estado e timestamps. As confirmações utilizam bloqueio pessimista e só aceitam propostas com estado `PENDING`, evitando confirmação duplicada ou cobrança concorrente.

Os endpoints são `POST /inventory/rarity-reroll/start`, `POST /inventory/rarity-reroll/{id}/accept` e `POST /inventory/rarity-reroll/{id}/keep`. A interface do inventário abre um modal em português com a comparação das raridades e informa claramente que a opção de manter cobra Bits.
