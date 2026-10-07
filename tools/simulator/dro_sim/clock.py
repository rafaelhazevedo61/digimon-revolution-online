"""Relógio de jogo sincronizado com o horário do servidor.

O servidor de simulação roda com libfaketime, então o cabeçalho HTTP ``Date``
reflete o horário acelerado. O bot ancora o relógio local nesse horário e
converte esperas de "tempo de jogo" para "tempo real" usando a velocidade.
"""

from __future__ import annotations

import re
import threading
import time
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime


# Java serializa até 9 casas decimais; o Python 3.10 só aceita 3 ou 6.
_FRACTION = re.compile(r"\.(\d+)")


class GameClock:
    def __init__(self, speed: float = 1.0):
        self.speed = max(float(speed), 0.001)
        self._lock = threading.Lock()
        self._anchor_game: float | None = None
        self._anchor_real = time.monotonic()

    def observe_date_header(self, header: str | None) -> None:
        if not header:
            return
        try:
            server = parsedate_to_datetime(header).timestamp()
        except (TypeError, ValueError):
            return
        with self._lock:
            predicted = self._predict_locked()
            # O cabeçalho tem resolução de 1s; só reancora quando o desvio supera essa margem.
            if predicted is None or abs(server - predicted) > 1.5 * self.speed + 1:
                self._anchor_game = server
                self._anchor_real = time.monotonic()

    def _predict_locked(self) -> float | None:
        if self._anchor_game is None:
            return None
        return self._anchor_game + (time.monotonic() - self._anchor_real) * self.speed

    def now(self) -> float:
        with self._lock:
            predicted = self._predict_locked()
        return predicted if predicted is not None else time.time()

    def now_dt(self) -> datetime:
        return datetime.fromtimestamp(self.now(), tz=timezone.utc)

    def sleep(self, game_seconds: float) -> None:
        if game_seconds > 0:
            time.sleep(game_seconds / self.speed)


def parse_instant(value: str | None) -> float | None:
    """Converte Instant/LocalDateTime serializado pelo Jackson em epoch (UTC)."""
    if not value:
        return None
    text = _FRACTION.sub(lambda m: "." + (m.group(1) + "000000")[:6], value.replace("Z", "+00:00"))
    try:
        parsed = datetime.fromisoformat(text)
    except ValueError:
        return None
    if parsed.tzinfo is None:
        parsed = parsed.replace(tzinfo=timezone.utc)
    return parsed.timestamp()
