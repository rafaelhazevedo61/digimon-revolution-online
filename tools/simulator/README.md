# Simulador de jogabilidade (DRO)

Bots em Python que jogam o Digimon Revolution Online **pelos mesmos endpoints HTTP do frontend**,
sem alterar o backend, para gerar dados de balanceamento (progressão, economia, drops, gargalos).

## 1. Subir a stack de simulação (isolada)

```bash
cd docker/simulation
DRO_SIM_SPEED=60 docker compose up -d --build   # API em http://localhost:18080
```

- Bancos e volumes próprios (`dro_sim_*`); nunca aponta para a beta.
- A API roda com `libfaketime`: em `DRO_SIM_SPEED=60` o servidor acha que o tempo corre 60x mais rápido
  (missão de 10 min ≈ 10 s; 1 dia de jogo ≈ 24 min). Use `DRO_SIM_SPEED=1` para tempo real.
- Datas geradas pelo PostgreSQL (`DEFAULT NOW()`) não são aceleradas; as regras de jogo usam o relógio da JVM.
- Para zerar os dados: `docker compose down -v`.

## 2. Rodar os bots

```bash
cd tools/simulator
python -m venv .venv && . .venv/bin/activate   # Windows: .venv\Scripts\Activate.ps1
pip install -r requirements.txt
python run_simulation.py --speed 60 --days 1 --bots casual:2 regular:2 hardcore:1
python report.py runs/<run-id>
```

`--speed` deve ser igual ao `DRO_SIM_SPEED` da API. O bot sincroniza o relógio pelo cabeçalho `Date`
da API e espera pelos horários reais devolvidos (`endsAt`, cooldowns), então funciona igual em 1x ou 60x.

## 3. Perfis (`dro_sim/profiles.py`)

| Perfil | Comportamento |
| --- | --- |
| `casual` | 3 sessões/dia de ~20 min; boss/arena só com chance alta |
| `regular` | 5 sessões/dia; usa todos os sistemas |
| `hardcore` | online 16h/dia, reage a cada fim de missão/cooldown |
| `afk` | 1 sessão/dia, só missões e coletas |

As sessões são em horas UTC do relógio do jogo. Cada bot é uma conta comum (nunca ADMIN, que pula
duração de missão e ganha slots).

Em cada sessão o bot: coleta recompensas do tutorial, abre baús, equipa o melhor item por slot, evolui
quando possível, enfrenta bosses e arena (respeitando chance mínima e energia), coleta/inicia missões
e coleta o calendário de atividades.

## 4. Saída (`runs/<run-id>/`)

- `events.jsonl` — cada chamada/ação com horário de jogo, resultado e erro (sem tokens/senhas).
- `snapshots.csv` — estado do bot a cada tick (nível, XP, bits, energia, stats, rating, contadores).
- `report.html` — resumo por bot, gráficos por hora de jogo, evoluções, drops e erros da API.
