"""Executa bots do DRO contra a API da stack de simulação.

Exemplo:
    python run_simulation.py --base-url http://localhost:18080 --speed 60 --days 7 \
        --bots casual:2 regular:2 hardcore:1
"""

from __future__ import annotations

import argparse
import threading
import time
from datetime import datetime
from pathlib import Path

from dro_sim.bot import GameplayBot
from dro_sim.profiles import PROFILES, get_profile
from dro_sim.recorder import Recorder


# Bots são agrupados em clãs de até 5 membros (ClanRules.INITIAL_MAX_MEMBERS).
CLAN_SIZE = 5


def parse_bots(specs: list[str]) -> list[tuple[str, int]]:
    parsed = []
    for spec in specs:
        name, _, count = spec.partition(":")
        get_profile(name)
        parsed.append((name, int(count or 1)))
    return parsed


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base-url", default="http://localhost:18080")
    parser.add_argument("--speed", type=float, default=60, help="Mesmo valor de DRO_SIM_SPEED da API (1 = tempo real).")
    parser.add_argument("--days", type=float, default=7, help="Dias de jogo a simular.")
    parser.add_argument("--bots", nargs="+", default=["regular:1"],
                        help=f"perfil:quantidade. Perfis: {', '.join(PROFILES)}")
    parser.add_argument("--run-id", default=datetime.now().strftime("%Y%m%d-%H%M%S"))
    parser.add_argument("--out", default="runs")
    args = parser.parse_args()

    run_dir = Path(args.out) / args.run_id
    recorder = Recorder(run_dir)
    threads = []
    run_tag = args.run_id.replace("-", "")
    for profile_name, count in parse_bots(args.bots):
        for index in range(1, count + 1):
            clan_name = f"Sim {run_tag[:20]} {len(threads) // CLAN_SIZE + 1}"
            bot = GameplayBot(f"sim_{run_tag}_{profile_name}_{index}",
                              get_profile(profile_name), args.base_url, args.speed, recorder,
                              clan_name=clan_name)
            thread = threading.Thread(target=bot.run, args=(args.days,), name=bot.name, daemon=True)
            threads.append(thread)
            thread.start()
            time.sleep(0.5)
    print(f"{len(threads)} bot(s) rodando; saída em {run_dir}")
    try:
        for thread in threads:
            thread.join()
    except KeyboardInterrupt:
        print("Interrompido; dados parciais já gravados.")
    finally:
        recorder.close()
    print(f"Concluído: {run_dir / 'events.jsonl'} e {run_dir / 'snapshots.csv'}")


if __name__ == "__main__":
    main()
