# Arena PvP

## Lobby e desafios

A Arena é assíncrona e usa o parceiro ativo. O lobby busca adversários no mesmo estágio ou em estágio adjacente, com diferença de rating de até **200 pontos**. Quando há menos de 10 adversários reais, o lobby tenta completar a lista com bots de estágio compatível. A janela de rating vale para adversários reais. Partidas contra bots não alteram o rating.

O desafio considera o poder efetivo, incluindo equipamentos. A chance de vitória é limitada à faixa de 5% a 95%. Quando o consumo está habilitado, o custo base é de **10 de energia**. O limite diário vem da configuração do ambiente, e o mesmo adversário tem cooldown de **30 minutos** para novos desafios quando a restrição se aplica.

## Rating e recompensas

O rating do Digimon começa em **1.000** e usa cálculo estilo Elo, com fator K de 32 e piso de 100. A vitória pode conceder Bits e **15 a 40 Moedas da Arena**, conforme a diferença de rating. A derrota concede **5 moedas**. As moedas pertencem à conta e são usadas na Loja da Arena. Consulte [DROPS.md](DROPS.md#arena) para os baús e a loja.

## Rankings

O ranking geral usa o rating dos Digimons elegíveis. O sazonal agrega resultados por jogador dentro da janela configurada no código. A janela revisada vai de **1º de agosto a 31 de dezembro de 2026**, no fuso do servidor. A classificação sazonal ordena pela variação acumulada de rating e usa vitórias como desempate.

[Código da Arena](../../java/com/dro/modules/arena)
