# Requisitos de evolução dos Digimons

Para evoluir, selecione o Digimon como parceiro ativo e abra **Evoluir** no dashboard ou em **Mais → Digimon**. A tela mostra as próximas formas disponíveis, os requisitos e quantos fragmentos você já tem.

## Níveis e fragmentos por estágio

| Evolução | Nível mínimo do Digimon | Fragmentos da forma de destino |
| --- | ---: | ---: |
| Baby → Baby II | 10 | 5 |
| Baby II → Rookie | 15 | 10 |
| Rookie → Champion | 25 | 20 |
| Champion → Ultimate | 50 | 30 |
| Ultimate → Mega | 75 | 50 |

Uma linha completa de Baby a Mega exige **115 fragmentos**, distribuídos entre as cinco formas de destino. Por exemplo, para evoluir **Agumon → Greymon**, o Agumon precisa estar no **nível 25 ou superior** e o inventário precisa ter **20 Fragmentos do Greymon**.

## Como funciona

- O Digimon ativo precisa ter uma espécie vinculada ao catálogo e fazer parte de uma linha ativa com uma próxima etapa. A evolução avança uma etapa por vez.
- Quando há várias rotas, escolha a linha desejada na tela de evolução. Cada rota usa os requisitos da sua própria etapa de destino.
- O backend verifica o nível e consome os materiais da etapa ao confirmar. No catálogo padrão, o custo é composto pelos fragmentos da espécie de destino. Se uma etapa tiver outros materiais cadastrados, eles também são exigidos.
- A evolução mantém o nível e a raridade do Digimon e recalcula HP, ataque e defesa para a nova forma.

Consulte o [guia de drops](../backend/src/main/resources/docs/DROPS.md) para as fontes de fragmentos.

## Referência no código

- O nível mínimo fica em `evolution_line_steps.required_level`. O padrão inicial vem da [migração V37](../backend/src/main/resources/db/migration/V37__add_required_level_to_evolution_steps.sql).
- Os materiais e as quantidades ficam em `evolution_step_materials`, por etapa. A [migração V40](../backend/src/main/resources/db/migration/V40__seed_evolution_step_materials.sql) cadastra os custos das linhas iniciais.
- [GetEvolutionOptionsUseCase](../backend/src/main/java/com/dro/modules/evolution/application/GetEvolutionOptionsUseCase.java) consulta as opções e seus requisitos. [EvolveDigimonUseCase](../backend/src/main/java/com/dro/modules/digimon/application/EvolveDigimonUseCase.java) valida e aplica a evolução.
