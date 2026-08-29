# Dado de Raridade de Digimon

O item técnico `RARITY_REROLL` é exibido ao jogador como **Dado de Raridade**. O código interno foi preservado para manter compatibilidade com itens e registros já existentes; o nome, a descrição e o ícone persistidos foram atualizados para representar o novo conceito.

Ao usar o Dado de Raridade, o item é consumido e uma nova proposta de raridade é gerada para o Digimon ativo. A proposta é persistida para que o jogador possa escolher uma única vez entre aceitar a nova raridade ou manter a anterior.

| Ação | Resultado | Custo em Bits |
|---|---|---:|
| Aceitar nova raridade | Atualiza a raridade do Digimon para a proposta | 0 |
| Manter raridade anterior | Mantém o valor atual e encerra a proposta | 5.000 por padrão |

O custo para manter a raridade é configurável por `DRO_RARITY_REROLL_KEEP_COST_BITS`. A configuração está presente em `application.yml`, `application-alpha.yml` e `application-local.yml`, sempre dentro de `dro.gameplay.rarity-reroll.keep-cost-bits`. O valor padrão é 5.000 Bits e pode ser sobrescrito pela variável de ambiente. O Dado utiliza uma matriz explícita de transição, sem alterar as probabilidades de hatch ou rebirth. A raridade atual nunca é repetida:

| Raridade atual | Comum | Rara | Épica | Lendária | Sem alteração |
|---|---:|---:|---:|---:|---:|
| `COMMON` | — | 28% | 1,8% | 0,2% | 70% |
| `RARE` | 98% | — | 1,8% | 0,2% | — |
| `EPIC` | 71,29% | 28,51% | — | 0,2% | — |
| `LEGENDARY` | 65% | 25% | 10% | — | — |

Quando o Digimon é Comum, existe 70% de chance de o Dado ser consumido sem alterar a raridade. Nos demais casos, uma nova raridade é sempre escolhida. A linha da Épica mantém a proporção aproximada 70/28 usada como referência na queda da Lendária, ajustada para preservar os 0,2% de avanço para Lendária.

A tabela `digimon_rarity_rerolls` registra jogador, Digimon, raridade atual, nova raridade, estado e timestamps. As confirmações utilizam bloqueio pessimista e só aceitam propostas com estado `PENDING`, evitando confirmação duplicada ou cobrança concorrente.

Os endpoints permanecem `POST /inventory/rarity-reroll/start`, `POST /inventory/rarity-reroll/{id}/accept` e `POST /inventory/rarity-reroll/{id}/keep`. A interface do inventário agora mostra o ícone de dado (`🎲`), o nome **Dado de Raridade** e o modal com a comparação das raridades e o custo de manutenção.


## Indicativo de raridade alterada

Quando o jogador aceita uma nova raridade pelo Dado, o Digimon passa a registrar a origem da alteração. A primeira raridade original é preservada, mesmo que o Dado seja utilizado novamente no futuro.

A migration `V172__add_rarity_die_indicator.sql` adiciona os campos `rarity_changed_by_die`, `original_rarity_before_die` e `rarity_changed_by_die_at` à tabela `digimons`. O indicador não recalcula IVs ou atributos já armazenados.

Nas listas e resumos do Dashboard, Storage, Ranking e Evolução, um ícone discreto de dado é exibido ao lado da raridade. O Dashboard exibe somente esse indicativo compacto, sem mostrar a raridade original, a data ou a observação sobre IVs e atributos. O tooltip e os modais de detalhes continuam disponíveis nas telas apropriadas, onde o jogador pode consultar a origem da alteração.
