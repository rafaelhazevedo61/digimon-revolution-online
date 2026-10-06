"""Perfis de comportamento dos bots.

Cada perfil define quando o "jogador" está online (janelas por dia, em horas UTC
do relógio do jogo) e quais sistemas ele usa durante a sessão.
"""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(frozen=True)
class Profile:
    name: str
    description: str
    # Janelas online: (hora de início, duração em minutos de jogo).
    sessions: tuple[tuple[float, int], ...]
    do_missions: bool = True
    do_bosses: bool = True
    do_world_boss: bool = True
    do_arena: bool = True
    open_chests: bool = True
    auto_evolve: bool = True
    auto_equip: bool = True
    claim_tutorial: bool = True
    claim_calendar: bool = True
    min_boss_win_chance: int = 50
    min_arena_win_chance: int = 40
    # Intervalo máximo entre verificações dentro de uma sessão online (minutos de jogo).
    max_idle_minutes: int = 15
    extra: dict = field(default_factory=dict)


PROFILES: dict[str, Profile] = {
    "casual": Profile(
        name="casual",
        description="Entra 3 vezes por dia por ~20 minutos; joga missões e coleta recompensas.",
        sessions=((8, 20), (13, 20), (21, 30)),
        do_world_boss=False,
        min_boss_win_chance=70,
        min_arena_win_chance=60,
    ),
    "regular": Profile(
        name="regular",
        description="Entra 5 vezes por dia por ~40 minutos; usa todos os sistemas principais.",
        sessions=((7, 40), (11, 30), (15, 40), (19, 60), (22, 40)),
    ),
    "hardcore": Profile(
        name="hardcore",
        description="Online 16h por dia, reage a cada fim de missão, cooldown e energia.",
        sessions=((7, 16 * 60),),
        min_boss_win_chance=35,
        min_arena_win_chance=30,
        max_idle_minutes=5,
    ),
    "afk": Profile(
        name="afk",
        description="Entra 1 vez por dia só para coletar e reiniciar missões.",
        sessions=((20, 15),),
        do_bosses=False,
        do_world_boss=False,
        do_arena=False,
        auto_equip=False,
    ),
}


def get_profile(name: str) -> Profile:
    try:
        return PROFILES[name]
    except KeyError as exc:
        raise SystemExit(f"Perfil desconhecido: {name}. Opções: {', '.join(PROFILES)}") from exc
