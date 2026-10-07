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
python run_simulation.py --speed 60 --days 7 --bots casual:2 regular:2 hardcore:1
python report.py runs/<run-id>
```

Padrão: 7 dias de jogo (~2h50 reais em 60x). `--speed` deve ser igual ao `DRO_SIM_SPEED` da API. O bot sincroniza o relógio pelo cabeçalho `Date`
da API e espera pelos horários reais devolvidos (`endsAt`, cooldowns), então funciona igual em 1x ou 60x.

## 3. Perfis (`dro_sim/profiles.py`)

| Perfil | Comportamento |
| --- | --- |
| `casual` | 4 sessões/dia de 15 min (8h, 12h, 18h, 22h); boss/arena só com chance alta |
| `regular` | 6 sessões/dia de 30 min (7h, 10h, 13h, 16h, 19h, 22h); usa todos os sistemas |
| `hardcore` | online 16h/dia, reage a cada fim de missão/cooldown |
| `afk` | 1 sessão/dia, só missões e coletas |

As sessões são em horas UTC do relógio do jogo. Cada bot é uma conta comum (nunca ADMIN, que pula
duração de missão e ganha slots).

Em cada sessão o bot: coleta recompensas do tutorial, abre baús, equipa o melhor item por slot, evolui
quando possível, enfrenta bosses e arena (respeitando chance mínima e energia), coleta/inicia missões
e coleta o calendário de atividades. Missões são sempre manuais (a automação não é usada na alfa).

Depois das missões, o bot ataca o **Chefe de Incursão** (`/clan-raids/attack`, 15 de energia) e o
**Chefe Mundial** (`/world-boss/attack`, 20 de energia) sempre que o cooldown (`nextAttackAvailableAt`)
liberar e sobrar energia acima de `boss_energy_reserve` (padrão 5, o custo de uma missão). Para a
Incursão, os bots de uma rodada são agrupados em clãs de até 5 (`Sim <run-id> 1`, `Sim <run-id> 2`, …):
o primeiro do grupo cria o clã (custo 0) e os demais entram. O perfil `afk` não ataca chefes.

Antes das missões, o bot compra o **Expansor de Slot de Missão** (`MISSION_SLOT_UNLOCK`, preço lido de
`GET /shop`) assim que tiver Bits acima de `slot_bits_reserve` (padrão 0) e o usa em `/inventory/use`,
até liberar os 3 slots (`buy_mission_slots`). Também usa na hora todos os **XP_DISC** do inventário
(`use_xp_discs`). O relatório mostra quando cada slot foi liberado, os Bits gastos e o XP vindo dos discos.

## 4. Saída (`runs/<run-id>/`)

- `events.jsonl` — cada chamada/ação com horário de jogo, resultado e erro (sem tokens/senhas).
- `snapshots.csv` — estado do bot a cada tick (nível, XP, bits, energia, stats, rating, contadores).
- `report.html` — resumo por bot, gráficos por hora de jogo, ataques/dano/recompensas do Chefe Mundial e
  da Incursão, evoluções, drops e erros da API.
