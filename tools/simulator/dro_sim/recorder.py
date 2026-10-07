"""Gravação dos eventos e snapshots da simulação em JSONL/CSV."""

from __future__ import annotations

import csv
import json
import threading
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

SNAPSHOT_FIELDS = [
    "game_time",
    "game_hours",
    "bot",
    "profile",
    "digimon",
    "stage",
    "level",
    "experience",
    "rebirths",
    "bits",
    "energy",
    "max_energy",
    "hp",
    "attack",
    "defense",
    "equip_attack",
    "equip_defense",
    "equip_hp",
    "arena_rating",
    "arena_coins",
    "inventory_items",
    "equipment_count",
    "missions_claimed",
    "bosses_won",
    "bosses_lost",
    "arena_won",
    "arena_lost",
    "chests_opened",
    "evolutions",
    "clan",
    "mission_slots",
    "xp_discs_used",
    "xp_from_discs",
    "world_boss_attacks",
    "world_boss_damage",
    "clan_raid_attacks",
    "clan_raid_damage",
    "api_errors",
]


class Recorder:
    def __init__(self, run_dir: Path):
        self.run_dir = run_dir
        run_dir.mkdir(parents=True, exist_ok=True)
        self._lock = threading.Lock()
        self._events = open(run_dir / "events.jsonl", "a", encoding="utf-8")
        snapshot_path = run_dir / "snapshots.csv"
        new_file = not snapshot_path.exists()
        self._snapshots_file = open(snapshot_path, "a", encoding="utf-8", newline="")
        self._snapshots = csv.DictWriter(self._snapshots_file, fieldnames=SNAPSHOT_FIELDS, extrasaction="ignore")
        if new_file:
            self._snapshots.writeheader()

    @staticmethod
    def _iso(epoch: float) -> str:
        return datetime.fromtimestamp(epoch, tz=timezone.utc).isoformat(timespec="seconds")

    def event(self, game_time: float, bot: str, action: str, ok: bool, **fields: Any) -> None:
        record = {"game_time": self._iso(game_time), "bot": bot, "action": action, "ok": ok, **fields}
        line = json.dumps(record, ensure_ascii=False, default=str)
        with self._lock:
            self._events.write(line + "\n")
            self._events.flush()

    def snapshot(self, game_time: float, row: dict[str, Any]) -> None:
        row = {**row, "game_time": self._iso(game_time)}
        with self._lock:
            self._snapshots.writerow(row)
            self._snapshots_file.flush()

    def close(self) -> None:
        with self._lock:
            self._events.close()
            self._snapshots_file.close()
