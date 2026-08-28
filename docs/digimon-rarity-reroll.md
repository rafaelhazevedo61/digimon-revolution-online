# Dado de Raridade de Digimon

O item técnico `RARITY_REROLL` é exibido ao jogador como **Dado de Raridade**. O código interno foi preservado para manter compatibilidade com itens e registros já existentes; o nome, a descrição e o ícone persistidos foram atualizados para representar o novo conceito.

Ao usar o Dado de Raridade, o item é consumido e uma nova proposta de raridade é gerada para o Digimon ativo. A proposta é persistida para que o jogador possa escolher uma única vez entre aceitar a nova raridade ou manter a anterior.

| Ação | Resultado | Custo em Bits |
|---|---|---:|
| Aceitar nova raridade | Atualiza a raridade do Digimon para a proposta | 0 |
| Manter raridade anterior | Mantém o valor atual e encerra a proposta | 5.000 por padrão |

O custo para manter a raridade é configurável por `DRO_RARITY_REROLL_KEEP_COST_BITS`. O sorteio utiliza as mesmas raridades globais (`COMMON`, `RARE`, `EPIC`, `LEGENDARY`) e uma raridade igual à atual é descartada para que a proposta sempre apresente uma mudança real.

A tabela `digimon_rarity_rerolls` registra jogador, Digimon, raridade atual, nova raridade, estado e timestamps. As confirmações utilizam bloqueio pessimista e só aceitam propostas com estado `PENDING`, evitando confirmação duplicada ou cobrança concorrente.

Os endpoints permanecem `POST /inventory/rarity-reroll/start`, `POST /inventory/rarity-reroll/{id}/accept` e `POST /inventory/rarity-reroll/{id}/keep`. A interface do inventário agora mostra o ícone de dado (`🎲`), o nome **Dado de Raridade** e o modal com a comparação das raridades e o custo de manutenção.
