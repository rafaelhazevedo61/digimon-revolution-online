# Missões e equipes

## Participantes e capacidade

O jogador pode enviar o parceiro ativo sozinho ou selecionar uma equipe salva com até três Digimons elegíveis da conta, incluindo Digimons no Storage. Cada missão ocupa um slot de execução. A conta começa com **1 slot de missão**, expansível até **3**. A capacidade de equipes salvas é independente: começa em **3** e chega a **10** com o Expansor de Slot de Time.

Ao iniciar, o servidor verifica a disponibilidade do slot, os requisitos de estágio e nível e a disponibilidade dos participantes. Com o consumo de energia ativo, cada participante precisa pagar o custo aplicável.

## Resgate e recompensas

Quando o timer termina, o resgate concede XP a cada participante, com os modificadores individuais aplicáveis. Os Bits são creditados uma vez à conta, e o baú ou loot da missão é processado uma vez por execução. O histórico de conclusões por missão influencia as recompensas futuras. Consulte [DROPS.md](DROPS.md) para os pools.

## Automação

Em missões de equipe, o resgate automático coleta a recompensa quando o timer termina. Com repetição habilitada, o fluxo tenta iniciar a mesma missão com a mesma equipe após o resgate. Os requisitos são validados novamente a cada ciclo.

Se o limite de pilha do inventário impedir o resgate automático, o jogo pausa a automação e envia um aviso pelo Correio. Libere espaço no item indicado e use a ação de retomada da mensagem. Outras falhas podem pausar o processamento. Confira o estado da missão, corrija a causa e reative a automação quando disponível.

[Código de missões](../../java/com/dro/modules/mission)
