"""Gera relatórios HTML autocontidos a partir de uma execução do simulador.

Uso:
    python report.py runs/<run-id> [--charts]

Gera `runs/<run-id>/report.html` (comparação por perfil e visão geral) e uma página por bot em
`runs/<run-id>/bots/<bot>.html` (linha do tempo dia a dia). Com `--charts`, inclui gráficos
(Chart.js via CDN, precisa de internet para abrir).
"""

from __future__ import annotations

import argparse
import csv
import html
import json
import math
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path

CHART_JS = "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"
SERIES = [("level", "Nível"), ("bits", "Bits"), ("energy", "Energia"), ("arena_rating", "Rating arena"),
          ("missions_claimed", "Missões coletadas"), ("chests_opened", "Baús abertos"),
          ("mission_slots", "Slots de missão desbloqueados"), ("helper_missions_claimed", "Missões dos Digimons extras"),
          ("xp_from_discs", "XP acumulado de XP_DISC"),
          ("world_boss_damage", "Dano acumulado — Chefe Mundial"),
          ("clan_raid_damage", "Dano acumulado — Chefe de Incursão")]
BOSS_HITS = [("world_boss_hit", "Chefe Mundial"), ("clan_raid_hit", "Chefe de Incursão")]
MILESTONE_DAYS = (1, 3, 7)
STYLE = """body{font-family:system-ui,sans-serif;margin:24px;background:#0f172a;color:#e2e8f0}
a{color:#7dd3fc}h1,h2,h3{margin:.4em 0}.grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(420px,1fr));gap:16px}
.card{background:#1e293b;border-radius:10px;padding:12px}table{border-collapse:collapse;width:100%;font-size:13px;margin-bottom:12px}
th,td{border-bottom:1px solid #334155;padding:4px 8px;text-align:left}th{color:#94a3b8}.muted{color:#94a3b8}"""


def load(run_dir: Path):
    snapshots = list(csv.DictReader(open(run_dir / "snapshots.csv", encoding="utf-8")))
    events = [json.loads(line) for line in open(run_dir / "events.jsonl", encoding="utf-8") if line.strip()]
    return snapshots, events


def num(value):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def n0(value) -> float:
    return num(value) or 0.0


def fmt(value, digits: int = 0) -> str:
    if value is None:
        return "—"
    return f"{value:,.0f}".replace(",", ".") if digits == 0 else f"{value:.{digits}f}".replace(".", ",")


def table(headers, rows) -> str:
    head = "".join(f"<th>{html.escape(str(h))}</th>" for h in headers)
    body = "".join("<tr>" + "".join(f"<td>{c if isinstance(c, Raw) else html.escape(str(c))}</td>" for c in row)
                   + "</tr>" for row in rows)
    return f"<table><thead><tr>{head}</tr></thead><tbody>{body or '<tr><td>—</td></tr>'}</tbody></table>"


class Raw(str):
    """Célula que já é HTML (ex.: link)."""


def page(title: str, body: str, datasets: dict | None) -> str:
    script = ""
    if datasets is not None:
        script = f"""<script src="{CHART_JS}"></script><script>const D={json.dumps(datasets)};
for (const k in D) new Chart(document.getElementById('c_'+k),{{type:'line',data:{{datasets:D[k]}},
options:{{parsing:false,pointRadius:0,scales:{{x:{{type:'linear',title:{{display:true,text:'horas'}}}}}}}}}});</script>"""
    return f"""<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
<title>{html.escape(title)}</title><style>{STYLE}</style></head><body>
{body}{script}</body></html>"""


def charts_section(by_bot: dict[str, list[dict]]) -> tuple[str, dict]:
    datasets = {key: [{"label": bot, "data": [{"x": num(r["game_hours"]), "y": num(r.get(key))} for r in rows
                                              if num(r.get(key)) is not None]}
                      for bot, rows in sorted(by_bot.items())] for key, _ in SERIES}
    cards = "".join(f'<div class="card"><h3>{label}</h3><canvas id="c_{key}"></canvas></div>' for key, label in SERIES)
    return f'<h2>Evolução no tempo (eixo X = horas de jogo)</h2><div class="grid">{cards}</div>', datasets


class Run:
    def __init__(self, run_dir: Path):
        self.dir = run_dir
        self.snapshots, self.events = load(run_dir)
        self.by_bot: dict[str, list[dict]] = defaultdict(list)
        for row in self.snapshots:
            self.by_bot[row["bot"]].append(row)
        self.events_by_bot: dict[str, list[dict]] = defaultdict(list)
        for e in self.events:
            self.events_by_bot[e["bot"]].append(e)
        self.start: dict[str, datetime] = {}
        for bot, evs in self.events_by_bot.items():
            first = next((e for e in evs if e["action"] == "run_start"), evs[0])
            self.start[bot] = datetime.fromisoformat(first["game_time"])
        self.drops: dict[str, Counter] = defaultdict(Counter)
        for e in self.events:
            if e["action"] == "run_end":
                self.drops[e["bot"]].update(e.get("drops") or {})

    def bots(self) -> list[str]:
        return sorted(self.by_bot)

    def profile(self, bot: str) -> str:
        return self.by_bot[bot][-1]["profile"]

    def hours(self, bot: str) -> float:
        return n0(self.by_bot[bot][-1]["game_hours"])

    def event_day(self, bot: str, event: dict) -> int:
        hours = (datetime.fromisoformat(event["game_time"]) - self.start[bot]).total_seconds() / 3600
        return max(int(hours // 24) + 1, 1)

    def day_end(self, bot: str, day: int) -> dict | None:
        rows = [r for r in self.by_bot[bot] if n0(r["game_hours"]) <= day * 24]
        return rows[-1] if rows else None

    def actions(self, bot: str, action: str, ok_only: bool = True) -> list[dict]:
        return [e for e in self.events_by_bot[bot] if e["action"] == action and (e["ok"] or not ok_only)]

    # ------------------------------------------------------------------ linhas
    def final_row(self, bot: str, link: bool) -> list:
        last = self.by_bot[bot][-1]
        name = Raw(f'<a href="bots/{html.escape(bot)}.html">{html.escape(bot)}</a>') if link else bot
        return [name, last["profile"], last["stage"], last["level"], last["bits"], last["missions_claimed"],
                last.get("mission_slots") or "—", last.get("helper_digimons") or 0,
                f'{last["bosses_won"]}/{last["bosses_lost"]}', f'{last["arena_won"]}/{last["arena_lost"]}',
                last["arena_rating"], last["chests_opened"], last["evolutions"], last.get("clan") or "—",
                f'{last.get("world_boss_attacks") or 0}/{last.get("world_boss_damage") or 0}',
                f'{last.get("clan_raid_attacks") or 0}/{last.get("clan_raid_damage") or 0}',
                last["api_errors"], last["game_hours"]]

    FINAL_HEADERS = ["Bot", "Perfil", "Estágio", "Nível", "Bits", "Missões", "Slots", "Digimons extras",
                     "Boss V/D", "Arena V/D", "Rating", "Baús", "Evoluções", "Clã", "Chefe Mundial ataques/dano",
                     "Incursão ataques/dano", "Erros API", "Horas de jogo"]

    def digimon_row(self, bot: str) -> list:
        rows = self.by_bot[bot]
        path, previous = [], None
        for r in rows:
            current = (r.get("digimon"), r.get("stage"))
            if current[0] and current != previous:
                path.append(f'{current[0]} ({current[1]}, nv {r.get("level")}, {n0(r["game_hours"]):.0f}h)')
                previous = current
        last = rows[-1]
        start = next(iter(self.actions(bot, "hatched")), {})
        lines = sorted({e.get("line") for e in self.actions(bot, "evolve")} - {None})
        return [bot, last["profile"], start.get("digimon") or (path[0].split(" (")[0] if path else "—"),
                start.get("rarity") or "—", ", ".join(lines) or "—",
                f'{last.get("digimon")} ({last.get("stage")}, nv {last.get("level")})', " → ".join(path) or "—"]

    DIGIMON_HEADERS = ["Bot", "Perfil", "Digitama inicial", "Raridade", "Linha(s) de evolução", "Atual", "Trajetória"]

    def boss_rows(self, bot: str) -> list[list]:
        result = []
        days = max(self.hours(bot) / 24, 1e-9)
        for action, label in BOSS_HITS:
            hits = self.actions(bot, action, ok_only=False)
            if not hits:
                continue
            damage = sum(int(h.get("damage") or 0) for h in hits)
            result.append([label, bot, len(hits), round(len(hits) / days, 1), damage, round(damage / len(hits)),
                           sum(int(h.get("xp") or 0) for h in hits), sum(int(h.get("bits") or 0) for h in hits),
                           sum(1 for h in hits if h.get("defeated"))])
        return result

    BOSS_HEADERS = ["Chefe", "Bot", "Ataques", "Ataques/dia", "Dano total", "Dano médio", "XP", "Bits", "Golpes finais"]

    def slot_row(self, bot: str) -> list:
        unlocks = self.actions(bot, "mission_slot_unlocked")
        discs = self.actions(bot, "xp_disc_used")
        born = self.actions(bot, "helper_hatched")
        last = self.by_bot[bot][-1]
        return [bot, last["profile"], last.get("mission_slots") or "—",
                "; ".join(f'{e.get("slots")}º slot: nv {e.get("level")}, {n0(e.get("game_hours")):.0f}h'
                          for e in unlocks) or "—",
                "; ".join(f'{e.get("digimon")} ({e.get("rarity")}), {n0(e.get("game_hours")):.0f}h' for e in born) or "—",
                last.get("helper_missions_claimed") or 0, last.get("bits_from_helper_missions") or 0,
                sum(int(e.get("price") or 0) for e in unlocks),
                sum(int(e.get("quantity") or 0) for e in discs), sum(int(e.get("xp") or 0) for e in discs)]

    SLOT_HEADERS = ["Bot", "Perfil", "Slots", "Desbloqueios", "Digimons extras chocados", "Missões dos extras",
                    "Bits dos extras", "Bits gastos em slots", "XP_DISC usados", "XP de XP_DISC"]

    def daily_rows(self, bot: str) -> list[list]:
        rows = self.by_bot[bot]
        per_day: dict[int, Counter] = defaultdict(Counter)
        for e in self.events_by_bot[bot]:
            day = self.event_day(bot, e)
            if e["action"] in ("world_boss_hit", "clan_raid_hit"):
                key = "wb" if e["action"] == "world_boss_hit" else "cr"
                per_day[day][f"{key}_hits"] += 1
                per_day[day][f"{key}_damage"] += int(e.get("damage") or 0)
                per_day[day]["boss_xp"] += int(e.get("xp") or 0)
                per_day[day]["boss_bits"] += int(e.get("bits") or 0)
            elif not e["ok"]:
                per_day[day]["errors"] += 1
        result, prev = [], rows[0]
        for day in range(1, max(math.ceil(self.hours(bot) / 24), 1) + 1):
            end = self.day_end(bot, day) or prev
            d = per_day[day]

            def delta(key):
                return int(n0(end.get(key)) - n0(prev.get(key)))

            result.append([day, f'{end.get("digimon")} ({end.get("stage")})', end.get("level"),
                           f'+{delta("level")}', end.get("bits"), f'{delta("bits"):+d}', delta("missions_claimed"),
                           delta("helper_missions_claimed"), end.get("mission_slots") or "—",
                           f'{d["wb_hits"]} / {d["wb_damage"]}', f'{d["cr_hits"]} / {d["cr_damage"]}',
                           d["boss_xp"], d["boss_bits"], delta("xp_from_discs"),
                           f'{delta("arena_won")}/{delta("arena_lost")}', delta("chests_opened"),
                           end.get("energy"), d["errors"]])
            prev = end
        return result

    DAILY_HEADERS = ["Dia", "Digimon", "Nível (fim)", "Níveis no dia", "Bits (fim)", "Saldo de Bits", "Missões",
                     "Missões dos extras", "Slots", "Mundial golpes/dano", "Incursão golpes/dano", "XP dos chefes",
                     "Bits dos chefes", "XP de XP_DISC", "Arena V/D", "Baús", "Energia (fim)", "Erros"]

    def errors(self, bots: list[str]) -> list[list]:
        counter = Counter((e["action"], e.get("status"), str(e.get("error"))[:120])
                          for bot in bots for e in self.events_by_bot[bot] if not e["ok"])
        return [[*k, v] for k, v in counter.most_common()]

    def evolutions(self, bots: list[str]) -> list[list]:
        return [[bot, e.get("from_stage"), e.get("line"), e.get("level"), e.get("game_hours")]
                for bot in bots for e in self.actions(bot, "evolve")]

    # ----------------------------------------------------------------- perfis
    def profile_rows(self) -> tuple[list[list], list[list]]:
        groups: dict[str, list[str]] = defaultdict(list)
        for bot in self.bots():
            groups[self.profile(bot)].append(bot)

        def avg(values):
            values = [v for v in values if v is not None]
            return sum(values) / len(values) if values else None

        summary, milestones = [], []
        for profile, bots in sorted(groups.items()):
            last = [self.by_bot[b][-1] for b in bots]
            days = [max(self.hours(b) / 24, 1e-9) for b in bots]
            per_day = lambda key: avg(n0(r.get(key)) / d for r, d in zip(last, days))  # noqa: E731
            boss_xp = [sum(int(h.get("xp") or 0) for a, _ in BOSS_HITS for h in self.actions(b, a, False)) for b in bots]
            summary.append([profile, len(bots), fmt(avg(self.hours(b) for b in bots), 1),
                            fmt(avg(n0(r["level"]) for r in last), 1), fmt(avg(n0(r["bits"]) for r in last)),
                            fmt(per_day("missions_claimed"), 1), fmt(per_day("helper_missions_claimed"), 1),
                            fmt(avg(n0(r.get("mission_slots")) for r in last), 1),
                            fmt(per_day("world_boss_attacks"), 1), fmt(per_day("world_boss_damage")),
                            fmt(per_day("clan_raid_attacks"), 1), fmt(per_day("clan_raid_damage")),
                            fmt(avg(x / d for x, d in zip(boss_xp, days))), fmt(avg(n0(r.get("xp_from_discs")) for r in last)),
                            f'{sum(1 for r in last if n0(r["evolutions"]) > 0)}/{len(bots)}',
                            fmt(avg(n0(r["arena_rating"]) for r in last))])
            row = [profile]
            for day in MILESTONE_DAYS:
                ends = [self.day_end(b, day) for b in bots if self.hours(b) >= day * 24 - 0.5]
                ends = [e for e in ends if e]
                row += [fmt(avg(n0(e["level"]) for e in ends), 1), fmt(avg(n0(e["bits"]) for e in ends)),
                        fmt(avg(n0(e["missions_claimed"]) for e in ends))]
            milestones.append(row)
        return summary, milestones

    PROFILE_HEADERS = ["Perfil", "Bots", "Horas de jogo (média)", "Nível", "Bits", "Missões/dia", "Missões dos extras/dia",
                       "Slots", "Golpes Mundial/dia", "Dano Mundial/dia", "Golpes Incursão/dia", "Dano Incursão/dia",
                       "XP dos chefes/dia", "XP de XP_DISC", "Evoluíram", "Rating arena"]
    MILESTONE_HEADERS = ["Perfil"] + [f"Dia {d} — {k}" for d in MILESTONE_DAYS for k in ("nível", "Bits", "missões")]


def build_index(run: Run, charts: bool) -> str:
    summary, milestones = run.profile_rows()
    bots = run.bots()
    chart_html, datasets = charts_section(run.by_bot) if charts else ("", None)
    drop_total = sum(run.drops.values(), Counter())
    body = f"""<h1>Simulação {html.escape(run.dir.name)}</h1>
<p class="muted">{len(bots)} bot(s), {len(run.snapshots)} snapshots, {len(run.events)} eventos.
Médias por perfil; valores "/dia" usam as horas de jogo de cada bot.</p>
<h2>Comparação por perfil</h2>{table(Run.PROFILE_HEADERS, summary)}
<h2>Marcos por perfil (média no fim do dia)</h2>{table(Run.MILESTONE_HEADERS, milestones)}
<h2>Bots (clique para ver a página de cada um)</h2>{table(Run.FINAL_HEADERS, [run.final_row(b, True) for b in bots])}
<h2>Digimon por bot</h2>{table(Run.DIGIMON_HEADERS, [run.digimon_row(b) for b in bots])}
<h2>Chefe Mundial e Chefe de Incursão</h2>{table(Run.BOSS_HEADERS, [r for b in bots for r in run.boss_rows(b)])}
<h2>Slots de missão, Digimons extras e XP_DISC</h2>{table(Run.SLOT_HEADERS, [run.slot_row(b) for b in bots])}
{chart_html}
<h2>Evoluções</h2>{table(["Bot", "De", "Linha", "Nível", "Horas de jogo"], run.evolutions(bots))}
<h2>Drops (todos os bots)</h2>{table(["Item", "Quantidade"], drop_total.most_common())}
<h2>Erros da API</h2>{table(["Ação", "HTTP", "Mensagem", "Ocorrências"], run.errors(bots))}"""
    return page(f"DRO — simulação {run.dir.name}", body, datasets)


def build_bot(run: Run, bot: str, charts: bool) -> str:
    chart_html, datasets = charts_section({bot: run.by_bot[bot]}) if charts else ("", None)
    body = f"""<p><a href="../report.html">← voltar para a simulação {html.escape(run.dir.name)}</a></p>
<h1>{html.escape(bot)} <span class="muted">({html.escape(run.profile(bot))})</span></h1>
<h2>Resumo</h2>{table(Run.FINAL_HEADERS, [run.final_row(bot, False)])}
<h2>Digimon</h2>{table(Run.DIGIMON_HEADERS, [run.digimon_row(bot)])}
<h2>Dia a dia</h2>{table(Run.DAILY_HEADERS, run.daily_rows(bot))}
<h2>Chefes</h2>{table(Run.BOSS_HEADERS, run.boss_rows(bot))}
<h2>Slots de missão, Digimons extras e XP_DISC</h2>{table(Run.SLOT_HEADERS, [run.slot_row(bot)])}
{chart_html}
<h2>Evoluções</h2>{table(["Bot", "De", "Linha", "Nível", "Horas de jogo"], run.evolutions([bot]))}
<h2>Drops</h2>{table(["Item", "Quantidade"], run.drops[bot].most_common())}
<h2>Erros da API</h2>{table(["Ação", "HTTP", "Mensagem", "Ocorrências"], run.errors([bot]))}"""
    return page(f"DRO — {bot}", body, datasets)


def build(run_dir: Path, charts: bool = False) -> Path:
    run = Run(run_dir)
    bots_dir = run_dir / "bots"
    bots_dir.mkdir(exist_ok=True)
    for bot in run.bots():
        (bots_dir / f"{bot}.html").write_text(build_bot(run, bot, charts), encoding="utf-8")
    out = run_dir / "report.html"
    out.write_text(build_index(run, charts), encoding="utf-8")
    return out


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("run_dir", type=Path)
    parser.add_argument("--charts", action="store_true", help="inclui gráficos (Chart.js via CDN); padrão: desligado")
    args = parser.parse_args()
    print(f"Relatório: {build(args.run_dir, args.charts)} (+ páginas em {args.run_dir / 'bots'})")


if __name__ == "__main__":
    main()
