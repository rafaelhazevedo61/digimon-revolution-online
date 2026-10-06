"""Gera um relatório HTML autocontido a partir de uma execução do simulador.

Uso:
    python report.py runs/<run-id>
"""

from __future__ import annotations

import csv
import html
import json
import sys
from collections import Counter, defaultdict
from pathlib import Path

CHART_JS = "https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"
SERIES = [("level", "Nível"), ("bits", "Bits"), ("energy", "Energia"), ("arena_rating", "Rating arena"),
          ("missions_claimed", "Missões coletadas"), ("chests_opened", "Baús abertos")]


def load(run_dir: Path):
    snapshots = list(csv.DictReader(open(run_dir / "snapshots.csv", encoding="utf-8")))
    events = [json.loads(line) for line in open(run_dir / "events.jsonl", encoding="utf-8") if line.strip()]
    return snapshots, events


def num(value):
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def build(run_dir: Path) -> str:
    snapshots, events = load(run_dir)
    by_bot: dict[str, list[dict]] = defaultdict(list)
    for row in snapshots:
        by_bot[row["bot"]].append(row)

    final_rows = []
    for bot, rows in sorted(by_bot.items()):
        last = rows[-1]
        final_rows.append([bot, last["profile"], last["stage"], last["level"], last["bits"],
                           last["missions_claimed"], f'{last["bosses_won"]}/{last["bosses_lost"]}',
                           f'{last["arena_won"]}/{last["arena_lost"]}', last["arena_rating"],
                           last["chests_opened"], last["evolutions"], last["api_errors"], last["game_hours"]])

    evolutions = [[e["bot"], e.get("from_stage"), e.get("line"), e.get("level"), e.get("game_hours")]
                  for e in events if e["action"] == "evolve" and e["ok"]]
    errors = Counter((e["action"], e.get("status"), str(e.get("error"))[:120]) for e in events if not e["ok"])
    drops: dict[str, Counter] = defaultdict(Counter)
    for e in events:
        if e["action"] == "run_end":
            drops[e["bot"]].update(e.get("drops") or {})
    drop_total = sum(drops.values(), Counter())

    datasets = {}
    for key, _ in SERIES:
        datasets[key] = [{"label": bot, "data": [{"x": num(r["game_hours"]), "y": num(r[key])} for r in rows
                                                 if num(r[key]) is not None]}
                         for bot, rows in sorted(by_bot.items())]

    def table(headers, rows):
        head = "".join(f"<th>{html.escape(str(h))}</th>" for h in headers)
        body = "".join("<tr>" + "".join(f"<td>{html.escape(str(c))}</td>" for c in row) + "</tr>" for row in rows)
        return f"<table><thead><tr>{head}</tr></thead><tbody>{body or '<tr><td>—</td></tr>'}</tbody></table>"

    charts = "".join(f'<div class="card"><h3>{label}</h3><canvas id="c_{key}"></canvas></div>' for key, label in SERIES)
    return f"""<!doctype html><html lang="pt-BR"><head><meta charset="utf-8">
<title>DRO — simulação {html.escape(run_dir.name)}</title><script src="{CHART_JS}"></script>
<style>body{{font-family:system-ui,sans-serif;margin:24px;background:#0f172a;color:#e2e8f0}}
h1,h2,h3{{margin:.4em 0}}.grid{{display:grid;grid-template-columns:repeat(auto-fit,minmax(420px,1fr));gap:16px}}
.card{{background:#1e293b;border-radius:10px;padding:12px}}table{{border-collapse:collapse;width:100%;font-size:13px}}
th,td{{border-bottom:1px solid #334155;padding:4px 8px;text-align:left}}th{{color:#94a3b8}}</style></head><body>
<h1>Simulação {html.escape(run_dir.name)}</h1>
<p>{len(by_bot)} bot(s), {len(snapshots)} snapshots, {len(events)} eventos.</p>
<h2>Resumo final por bot</h2>
{table(["Bot", "Perfil", "Estágio", "Nível", "Bits", "Missões", "Boss V/D", "Arena V/D", "Rating", "Baús",
        "Evoluções", "Erros API", "Horas de jogo"], final_rows)}
<h2>Evolução no tempo (eixo X = horas de jogo)</h2><div class="grid">{charts}</div>
<h2>Evoluções</h2>{table(["Bot", "De", "Linha", "Nível", "Horas de jogo"], evolutions)}
<h2>Drops (todos os bots)</h2>{table(["Item", "Quantidade"], drop_total.most_common())}
<h2>Erros da API</h2>{table(["Ação", "HTTP", "Mensagem", "Ocorrências"], [[*k, v] for k, v in errors.most_common()])}
<script>const D={json.dumps(datasets)};
for (const k in D) new Chart(document.getElementById('c_'+k),{{type:'line',data:{{datasets:D[k]}},
options:{{parsing:false,pointRadius:0,scales:{{x:{{type:'linear',title:{{display:true,text:'horas'}}}}}}}}}});</script>
</body></html>"""


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit(__doc__)
    run_dir = Path(sys.argv[1])
    out = run_dir / "report.html"
    out.write_text(build(run_dir), encoding="utf-8")
    print(f"Relatório: {out}")


if __name__ == "__main__":
    main()
