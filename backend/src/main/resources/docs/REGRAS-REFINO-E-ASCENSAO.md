# Regras de refino e ascensão

## Refino de +0 a +11

No Ferreiro, abra a prévia da peça e selecione os itens de suporte que deseja usar. Cada tentativa custa **1 Pedra de Refinamento** e os Bits da tabela, inclusive em caso de falha.

| Etapa | Bits | Sucesso base |
|---|---:|---:|
| +0 → +1 | 1.000 | 100% |
| +1 → +2 | 1.500 | 90% |
| +2 → +3 | 2.000 | 80% |
| +3 → +4 | 2.500 | 70% |
| +4 → +5 | 3.000 | 60% |
| +5 → +6 | 3.500 | 50% |
| +6 → +7 | 4.000 | 40% |
| +7 → +8 | 4.500 | 30% |
| +8 → +9 | 5.000 | 20% |
| +9 → +10 | 5.500 | 10% |
| +10 → +11 | 6.000 | 5% |

O Pergaminho de Refinamento acrescenta 10 pontos percentuais à chance de sucesso, até 100%, e é consumido na tentativa. O risco de quebra ocorre em **+10 → +11**. Após uma falha nessa etapa, há uma segunda rolagem com **25% de chance de quebra**. Se ela indicar quebra e um Cristal de Proteção tiver sido selecionado, o cristal é consumido e a peça permanece em +10. Nas demais falhas, a peça mantém seu nível.

**Limite vigente: +11.** A mensagem de erro do backend para uma peça que já chegou ao máximo ainda menciona +10.

## Ascensão

A peça precisa estar desequipada e no refino +11. Cada ascensão consome Bits e Núcleos de Ascensão e aumenta o multiplicador aplicado aos bônus refinados. A exigência de Rebirths é validada ao equipar a peça ascendida.

| Ascensão | Bits | Núcleos de Ascensão | Multiplicador | Rebirths para equipar |
|---|---:|---:|---:|---:|
| 1 | 100.000 | 10 | 1,30× | 1 |
| 2 | 500.000 | 30 | 1,50× | 10 |
| 3 | 2.000.000 | 100 | 2,00× | 20 |

O [aprimoramento de tier](REGRAS-APRIMORAMENTO.md) zera o refino e a ascensão. Consulte também [EquipmentRules](../../java/com/dro/modules/equipment/domain/EquipmentRules.java).
