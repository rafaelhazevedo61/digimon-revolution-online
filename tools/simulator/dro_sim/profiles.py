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
    do_clan_raid: bool = True
    do_arena: bool = True
    open_chests: bool = True
    auto_evolve: bool = True
    auto_equip: bool = True
    claim_tutorial: bool = True
    claim_calendar: bool = True
    min_boss_win_chance: int = 50
    min_arena_win_chance: int = 40
    # Energia mínima mantida após atacar Chefe Mundial/Incursão, para não travar missões.
    boss_energy_reserve: int = 5
    # Intervalo máximo entre verificações dentro de uma sessão online (minutos de jogo).
    max_idle_minutes: int = 15
    extra: dict = field(default_factory=dict)


PROFILES: dict[str, Profile] = {
    "casual": Profile(
        name="casual",
        description="Entra 4 vezes por dia por 15 minutos; joga missões e coleta recompensas.",
        sessions=((8, 15), (12, 15), (18, 15), (22, 15)),
        min_boss_win_chance=70,
        min_arena_win_chance=60,
    ),
    "regular": Profile(
        name="regular",
        description="Entra 6 vezes por dia por 30 minutos; usa todos os sistemas principais.",
        sessions=((7, 30), (10, 30), (13, 30), (16, 30), (19, 30), (22, 30)),
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
        do_clan_raid=False,
        do_arena=False,
        auto_equip=False,
    ),
}


def get_profile(name: str) -> Profile:
    try:
        return PROFILES[name]
    except KeyError as exc:
        raise SystemExit(f"Perfil desconhecido: {name}. Opções: {', '.join(PROFILES)}") from exc
