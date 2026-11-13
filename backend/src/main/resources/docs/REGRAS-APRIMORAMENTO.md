# Regras de aprimoramento de equipamentos

O aprimoramento aumenta o tier da peça de T1 a T10. Veja o catálogo e as fontes de obtenção das peças no [EQUIPMENT-PLAN.md](EQUIPMENT-PLAN.md).

## Como aprimorar um equipamento do T1 ao T10

1. Abra **Inventário → Equipamentos** e escolha a peça principal.
2. Desequipe e desbloqueie a peça principal e as cópias. Use peças da sua conta com o mesmo nome, set, slot, tier e raridade. Peças reservadas na Casa de Leilões ficam indisponíveis.
3. Clique em **Aprimorar**, selecione as cópias extras e confirme. Cada operação consome as cópias e **1 Núcleo de Aprimoramento** do tipo indicado para o tier de destino.
4. Repita o processo com cópias do novo tier até alcançar o T10. A peça principal sobe um tier com sucesso garantido, preserva a raridade e volta a **refino +0 e ascensão 0**.

### Materiais por etapa

| Etapa | Peças totais, incluindo a principal | Cópias extras consumidas | Núcleo consumido |
|---|---:|---:|---|
| T1 → T2 | 3 | 2 | Básico |
| T2 → T3 | 3 | 2 | Básico |
| T3 → T4 | 3 | 2 | Básico |
| T4 → T5 | 3 | 2 | Avançado |
| T5 → T6 | 4 | 3 | Avançado |
| T6 → T7 | 4 | 3 | Avançado |
| T7 → T8 | 4 | 3 | Supremo |
| T8 → T9 | 5 | 4 | Supremo |
| T9 → T10 | 5 | 4 | Supremo |

Por exemplo, para levar uma **Garra Berserker T1 Rara** ao T2, selecione outras duas Garras Berserker T1 Raras e use um núcleo básico. A próxima etapa exige uma Garra Berserker T2 Rara como principal e outras duas do mesmo tier e raridade.

### Onde obter os núcleos

| Núcleo | Baús da Arena | Raid de Clã e Boss Mundial | Desmonte |
|---|---|---|---|
| Núcleo de Aprimoramento (básico) | Prata | Baú de tentativa | T1 e T2 rendem 1, T3 rende 2 |
| Núcleo Avançado de Aprimoramento | Platina | Baú de maior dano | T4 e T5 rendem 1, T6 rende 2 |
| Núcleo Supremo de Aprimoramento | Diamante | Baú de golpe final | T7 e T8 rendem 1, T9 rende 2 |

O núcleo básico também pode cair no baú da missão **Ascensão à Montanha Infinita**. Os núcleos dos baús fazem parte das respectivas tabelas de sorteio.

Consulte as [regras de aprimoramento](../../java/com/dro/modules/equipment/domain/EquipmentEnhancementRules.java) e as fontes de núcleos nas migrations [V227](../db/migration/V227__seed_equipment_enhancement_core_loot.sql) e [V228](../db/migration/V228__move_enhancement_cores_to_arena_chests.sql).
