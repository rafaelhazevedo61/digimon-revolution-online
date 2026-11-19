# Progressão do Digimon

## Nível, estágio e atributos

A experiência eleva o nível até **100**. O estágio segue a linha da espécie, de **Baby → Baby II → Rookie → Champion → Ultimate → Mega**. A evolução segue as linhas disponíveis para a espécie e verifica nível e materiais de catálogo. Ela atualiza a espécie e o estágio da mesma instância e recalcula os atributos.

HP, ATK e DEF são calculados a partir dos atributos da espécie e dos IVs correspondentes, com modificadores de progressão. Raridade, personalidade e trait podem afetar atributos ou XP conforme suas regras. A raridade também define o piso dos IVs em sorteios aplicáveis. Equipamentos acrescentam bônus ao Digimon que os usa. A capacidade de energia cresce com o nível e pode receber bônus de trait.

## Grade de IVs

| Condição | Grade |
|---|---|
| 3 IVs iguais a 100 | SSS |
| 2 IVs iguais a 100 | SS |
| 1 IV igual a 100 | S |
| Sem IV perfeito, média ≥ 85 | A |
| Média de 70 a 84 | B |
| Média de 55 a 69 | C |
| Média de 40 a 54 | D |
| Média abaixo de 40 | E |

## Rebirth

O Rebirth exige um parceiro ativo no nível 100 e estágio Champion ou superior. Desequipe as peças e resolva missões em andamento ou recompensas pendentes. Consulte a prévia para conferir os custos e os IVs mínimos. A operação consome os recursos exigidos para o próximo Rebirth. Códigos Infinitos podem ser alocados por atributo e a opção de preservar raridade exige o item correspondente.

O Digimon anterior fica no histórico da linhagem. O novo nasce como Baby, nível 1, com contador de Rebirth incrementado. Os IVs são sorteados com regras de herança, a grade é recalculada e personalidade e trait são sorteados novamente. A raridade pode ser preservada ou sorteada. O novo Digimon herda rating, vitórias e derrotas da Arena. Bits e inventário pertencem à conta.

- [Regras no módulo Digimon](../../java/com/dro/modules/digimon)
- [Dados Digitais e Código Infinito](../../../../../docs/digital-data-and-infinite-code-spec.md)
- [Dado de Raridade](../../../../../docs/digimon-rarity-reroll.md)
- [Coleção](../../../../../docs/digimon-collection-system-proposal.md)
