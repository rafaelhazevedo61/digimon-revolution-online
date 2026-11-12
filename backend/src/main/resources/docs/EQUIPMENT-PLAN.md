# Sistema de equipamentos

Referência do catálogo versionado na `develop` (`edcba47`) e do mapeamento de drops das planilhas `DROPS - ATT 020926(1).xlsm` e `DRO_Mapeamento_Consolidado_de_Drops (1).xlsm`. Os dados implantados podem variar conforme o catálogo do ambiente.

## Catálogo e atributos-base

O catálogo tem **270 templates**. Cada um dos nove sets tem arma, armadura e acessório do T1 ao T10. Os valores abaixo estão na ordem **HP / ATK / DEF** para T1. Nos seeds, os atributos-base escalam de 1× no T1 a 5,5× no T10, em incrementos de 0,5× com arredondamento. Tier e raridade são independentes. Os templates têm raridade-base neutra e a obtenção pode definir ou sortear a raridade da instância.

| Set | Arma T1 | Armadura T1 | Acessório T1 |
|---|---|---|---|
| Berserker (`BERSERKER`) | Garra Berserker T1 (0 / 10 / 0) | Couraça Berserker T1 (5 / 4 / 3) | Emblema Berserker T1 (0 / 6 / 2) |
| Guardião (`GUARDIAN`) | Lança Guardiã T1 (0 / 4 / 4) | Armadura Guardiã T1 (15 / 0 / 8) | Medalha Guardiã T1 (8 / 0 / 5) |
| Vitalidade (`VITALITY`) | Cetro da Vitalidade T1 (8 / 3 / 0) | Vestes da Vitalidade T1 (20 / 0 / 4) | Emblema da Vitalidade T1 (15 / 0 / 2) |
| Equilibrado (`BALANCED`) | Lâmina do Equilíbrio T1 (4 / 4 / 4) | Armadura do Equilíbrio T1 (10 / 3 / 6) | Símbolo do Equilíbrio T1 (5 / 4 / 4) |
| Overclock (`OVERCLOCK`) | Lâmina Overclock T1 (3 / 8 / 0) | Frame Overclock T1 (10 / 4 / 0) | Chip Overclock T1 (5 / 5 / 0) |
| Firewall (`FIREWALL`) | Lança Firewall T1 (0 / 2 / 6) | Couraça Firewall T1 (10 / 0 / 9) | Núcleo Firewall T1 (5 / 0 / 5) |
| Data Breaker (`DATA_BREAKER`) | Garra Data Breaker T1 (0 / 11 / 0) | Couraça Data Breaker T1 (4 / 4 / 0) | Fragmento Data Breaker T1 (0 / 5 / 0) |
| Kernel (`KERNEL`) | Martelo Kernel T1 (5 / 2 / 3) | Kernel Shell T1 (17 / 0 / 6) | Kernel Core T1 (10 / 0 / 3) |
| Overlord (`OVERLORD`) | Lâmina do Overlord T1 (4 / 6 / 3) | Couraça do Overlord T1 (11 / 3 / 6) | Coroa do Overlord T1 (5 / 5 / 3) |

Seeds e normalização de raridade em [V60](../db/migration/V60__seed_equipment_sets.sql), [V67](../db/migration/V67__normalize_equipment_template_rarity.sql) e [V219](../db/migration/V219__seed_additional_equipment_sets.sql).

### Bônus de conjunto

O backend aplica o percentual sobre a soma dos bônus efetivos das peças equipadas. Ao equipar três peças do mesmo set, vale o bônus de três peças.

| Set | 2 peças HP | 2 peças ATK | 2 peças DEF | 3 peças HP | 3 peças ATK | 3 peças DEF | Set completo · 3 peças |
|---|---:|---:|---:|---:|---:|---:|---:|
| Berserker | 0% | 10% | 0% | 0% | 20% | 0% | HP 0%<br>ATK **+20%**<br>DEF 0% |
| Guardião | 5% | 0% | 10% | 10% | 0% | 20% | HP **+10%**<br>ATK 0%<br>DEF **+20%** |
| Vitalidade | 10% | 0% | 0% | 20% | 0% | 0% | HP **+20%**<br>ATK 0%<br>DEF 0% |
| Equilibrado | 5% | 5% | 5% | 10% | 10% | 10% | HP **+10%**<br>ATK **+10%**<br>DEF **+10%** |
| Overclock | 5% | 10% | 0% | 10% | 20% | 0% | HP **+10%**<br>ATK **+20%**<br>DEF 0% |
| Firewall | 0% | 0% | 15% | 10% | 0% | 25% | HP **+10%**<br>ATK 0%<br>DEF **+25%** |
| Data Breaker | 0% | 10% | 5% | 5% | 20% | 10% | HP **+5%**<br>ATK **+20%**<br>DEF **+10%** |
| Kernel | 10% | 0% | 5% | 20% | 0% | 15% | HP **+20%**<br>ATK 0%<br>DEF **+15%** |
| Overlord | 5% | 5% | 5% | 15% | 15% | 10% | HP **+15%**<br>ATK **+15%**<br>DEF **+10%** |

A última coluna mostra o bônus efetivo de cada atributo com o conjunto completo.

Os percentuais de Overclock, Firewall, Data Breaker, Kernel e Overlord seguem a tabela de balanceamento fornecida. **A implementação desses cinco bônus ainda está pendente no backend.**

Acompanhe esta implementação em [Pendências de código](PENDENCIAS.md#equipamentos).

Consulte [EquipmentRules](../../java/com/dro/modules/equipment/domain/EquipmentRules.java).

## Onde obter

### Missões

As duas planilhas mapeiam as mesmas 18 peças T1 nas seis áreas abaixo. As peças constam nas entradas lendárias das Loot Tables de missão. A coluna lendária indica a faixa do sorteio da tabela. A raridade da peça é resolvida na concessão.

| Área | Arma e missão | Armadura e missão | Acessório e missão |
|---|---|---|---|
| Floresta Nativa | Garra Berserker T1 (`MISSION_1`) | Couraça Berserker T1 (`MISSION_NF_2`) | Emblema Berserker T1 (`MISSION_NF_3`) |
| Savana Gear | Lança Guardiã T1 (`MISSION_2`) | Armadura Guardiã T1 (`MISSION_GS_2`) | Medalha Guardiã T1 (`MISSION_GS_3`) |
| Cidade Fatorial | Cetro da Vitalidade T1 (`MISSION_3`) | Vestes da Vitalidade T1 (`MISSION_FT_2`) | Emblema da Vitalidade T1 (`MISSION_FT_3`) |
| Terra Congelada | Lâmina do Equilíbrio T1 (`MISSION_4`) | Armadura do Equilíbrio T1 (`MISSION_FL_2`) | Símbolo do Equilíbrio T1 (`MISSION_FL_3`) |
| Deserto Server | Lâmina Overclock T1 (`MISSION_5`) | Frame Overclock T1 (`MISSION_SD_2`) | Chip Overclock T1 (`MISSION_SD_3`) |
| Montanha Infinita | Lança Firewall T1 (`MISSION_6`) | Couraça Firewall T1 (`MISSION_IM_2`) | Núcleo Firewall T1 (`MISSION_IM_3`) |

Consulte [V223](../db/migration/V223__add_mission_equipment_drops.sql) e o ajuste posterior da [missão final da Montanha Infinita](../db/migration/V238__fix_infinity_mountain_final_mission_rarity_weights.sql).

### Loja da Arena

| Equipamento T1 | Preço em Moedas da Arena |
|---|---:|
| Garra Data Breaker | 2.000 |
| Couraça Data Breaker | 2.500 |
| Fragmento Data Breaker | 2.000 |

Os produtos concedem uma peça com raridade aleatória. Consulte [V229](../db/migration/V229__add_data_breaker_arena_shop_products.sql).

### Raid de Clã e Boss Mundial

| Atividade | Baú de tentativa | Baú de maior dano acumulado | Baú de golpe final |
|---|---|---|---|
| Raid de Clã (Omegamon) | Martelo Kernel T1 | Kernel Shell T1 | Kernel Core T1 |
| Boss Mundial (Apocalymon) | Lâmina do Overlord T1 | Couraça do Overlord T1 | Coroa do Overlord T1 |

A tabela lista as peças possíveis, conforme [V224](../db/migration/V224__add_world_boss_and_clan_raid_t1_equipment.sql). A concessão dos três tipos de baú da Raid está implementada em [ClanRaidRewardService](../../java/com/dro/modules/clan/raid/application/ClanRaidRewardService.java), resolvendo a pendência anotada na planilha consolidada.

### Outras fontes no repositório

- Os bosses convencionais podem conceder equipamento pelo fluxo de drops do boss, conforme o catálogo e o perfil de raridade. Os tiers das planilhas variam entre os dois arquivos. Consulte [V69](../db/migration/V69__add_equipment_pool_drops_to_bosses.sql) para os pools versionados.
- Existem nove baús por set e dez por tier. Cada abertura concede uma peça do pool correspondente. A existência do baú no catálogo indica que ele pode ser aberto quando obtido. A distribuição depende do conteúdo configurado. Consulte [V222](../db/migration/V222__seed_equipment_set_and_tier_chests.sql).
- A Casa de Leilões permite adquirir peças individuais de outros jogadores. A peça anunciada deve estar desequipada e desbloqueada. Consulte [Casa de Leilões](../../../../../docs/auction-equipment-listings.md).

### Progressão de drops proposta nas planilhas

A aba **Mapa Geral** da planilha consolidada propõe a distribuição abaixo. Os intervalos da Raid e do Boss Mundial descrevem metas de progressão. As entradas nomeadas confirmadas no repositório são as peças T1 da tabela anterior.

| Tier | Fonte proposta no Mapa Geral |
|---|---|
| T2 | Boss normal |
| T3 | Boss diário |
| T4 | Boss semanal |
| T5 | Boss mensal |
| T6–T7 | Raid de Clã, set Kernel |
| T8–T9 | Boss Mundial, set Overlord |
| T10 | Conversão de Fragmentos Supremos |

A conversão por Fragmentos Supremos consta como proposta. O Ferreiro já permite aprimorar peças até T10 e o catálogo já define um Baú do Tier T10. A aba **Calendário** de `DROPS - ATT 020926(1).xlsm` repete as tabelas de Incursão. Para o calendário, a referência usada foi a aba correspondente da planilha consolidada. A recompensa mensal em Fragmentos Supremos faz parte da proposta.

## Inventário, Ferreiro e administração

- A peça pertence à conta. Quando equipada, ocupa um dos três slots de um Digimon próprio. Trocar a peça de um slot devolve a anterior ao inventário da conta.
- O refino usa Bits e Pedras de Refinamento. O limite implementado é **+11**. A tentativa de +10 para +11 pode quebrar a peça em caso de falha. Pergaminhos aumentam a chance de sucesso e Cristais de Proteção podem impedir a quebra.
- A ascensão exige peça desequipada com refino +11, Bits e Núcleos de Ascensão. Há três níveis. Equipar peças ascendidas pode exigir Rebirths.
- O aprimoramento sobe o tier usando cópias exatas da peça e um núcleo de aprimoramento. O refino e a ascensão voltam a zero. O Ferreiro também oferece desmonte individual e em lote. Peças bloqueadas ficam protegidas contra desmonte e uso como material.
- A concessão administrativa usa `POST /admin/equipment-templates/grant`. As operações do jogador estão em [EquipmentController](../../java/com/dro/modules/equipment/api/EquipmentController.java).
