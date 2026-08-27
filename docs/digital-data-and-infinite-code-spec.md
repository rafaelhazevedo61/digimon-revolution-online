# Dados Digitais e Código Infinito

## Escopo deste PR

Este PR adiciona a base da nova economia de Rebirth sem alterar loot tables. A configuração de drops permanece manual pelo Admin e poderá ser consolidada em uma migration futura.

## Modelagem

**Dados Digitais** são uma moeda de conta persistida em `players.digital_data`. Não são item de inventário, não podem ser vendidos ou negociados e começam em zero para cada jogador.

**Código Infinito** é um item de inventário com código `CODE_INFINITE`, tipo `ItemType.CODE_INFINITE`, raridade `LEGENDARY`, limite de pilha 999, inicialmente não negociável e não vendável. Seu uso planejado é melhorar o IV mínimo de HP, ATK ou DEF na tela de Rebirth.

| Recurso | Tipo | Função |
|---|---|---|
| Bits | Saldo do Digimon | Custo financeiro do Rebirth |
| Data Core | Item de inventário | Catalisador obrigatório |
| Dados Digitais | Moeda de conta | Requisito obrigatório desde o primeiro Rebirth |
| Código Infinito | Item de inventário | Refinamento opcional do IV mínimo |

## Custo de Dados Digitais

O custo é calculado pelo número atual de Rebirths antes da operação:

| Rebirth realizado | Custo |
|---:|---:|
| 1º | 25 |
| 2º | 50 |
| 3º | 80 |
| 4º | 120 |
| 5º | 170 |
| 6º ou superior | `floor(170 × 1,35^(rebirthCount - 4))` |

O primeiro Rebirth continua exigindo nível 100 e estágio Champion ou superior, mas agora exige também 25 Dados Digitais e 1 Data Core.

## Cálculo de Dados Digitais por sacrifício

O fluxo de sacrifício no Storage aceita apenas Digimons com status `STORED`, nunca o parceiro ativo, e bloqueia Digimons em missões em andamento. A operação é transacional: calcula a recompensa, credita a moeda na conta e remove o Digimon. A regra está centralizada em `DigitalDataRules` para evitar fórmulas duplicadas:

```text
ivMedio = floor((ivHp + ivAttack + ivDefense) / 3)
nivelFator = 25 + floor((75 × min(max(nivel, 1), 100)) / 100)
ivFator = 50 + floor(ivMedio / 2)
dadosDigitais = max(1, floor((baseEstagio × nivelFator × ivFator) / 10000))
```

| Estágio | Base |
|---|---:|
| Baby | 1 |
| Baby II | 2 |
| Rookie | 5 |
| Champion | 12 |
| Ultimate | 30 |
| Mega | 60 |

## Código Infinito no Rebirth

A regra de design aprovada é `10 Códigos Infinitos = +1 ponto de IV mínimo`. O bônus será aplicado ao intervalo de sorteio do novo Digimon, nunca diretamente ao IV final. O limite recomendado é de 100 Códigos Infinitos por Rebirth, distribuídos entre HP, ATK e DEF.

```text
bonusIv = floor(codigosInvestidosNoAtributo / 10)
ivMinimoFinal = min(ivMinimoBase + bonusIv, 100)
```

A seleção por atributo está disponível no request e na tela de Rebirth. O servidor valida o limite total, verifica o saldo de Código Infinito no inventário do Digimon e consome o item de forma transacional. O bônus é aplicado ao IV mínimo antes do sorteio, sempre limitado a 100.

## Alterações realizadas

- Adicionado `players.digital_data` com default zero.
- Adicionados métodos de domínio para consultar, creditar e consumir Dados Digitais.
- Adicionado `ItemType.CODE_INFINITE`.
- Criada a regra `DigitalDataRules`.
- Criada a migration `V137__add_digital_data_currency_and_code_infinite.sql`.
- Rebirth passou a validar e consumir Dados Digitais desde o primeiro ciclo.
- Adicionado o endpoint `POST /digimon/{digimonId}/sacrifice` para Digimons armazenados.
- A tela de Storage passou a exibir a ação de sacrifício com confirmação irreversível.
- A tela e a API de Rebirth passaram a distribuir e consumir Código Infinito por atributo.
- Nenhuma loot table foi alterada.

## Alterações deliberadamente não realizadas

As tabelas de drops existentes não foram modificadas. A remoção de Data Core das áreas anteriores à Infinity Mountain e a inclusão de Código Infinito na Infinity Mountain serão feitas manualmente pelo Admin, conforme o planejamento.

Auditoria detalhada por evento ainda pode ser adicionada em uma etapa posterior. A operação já possui transação, bloqueio pessimista dos registros e confirmação explícita na interface.
