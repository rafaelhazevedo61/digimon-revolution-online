"""Cliente HTTP mínimo da API do DRO usado pelos bots."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Any

import requests

from .clock import GameClock


@dataclass
class ApiResult:
    status: int
    data: Any
    error: str | None = None

    @property
    def ok(self) -> bool:
        return 200 <= self.status < 300


class ApiClient:
    def __init__(self, base_url: str, clock: GameClock, timeout: float = 30.0):
        self.base_url = base_url.rstrip("/")
        self.clock = clock
        self.timeout = timeout
        self.session = requests.Session()
        self.token: str | None = None

    def request(self, method: str, path: str, json: Any = None, headers: dict | None = None) -> ApiResult:
        all_headers = {"Accept": "application/json"}
        if self.token:
            all_headers["Authorization"] = f"Bearer {self.token}"
        if headers:
            all_headers.update(headers)
        try:
            response = self.session.request(
                method, self.base_url + path, json=json, headers=all_headers, timeout=self.timeout
            )
        except requests.RequestException as exc:
            return ApiResult(0, None, f"{type(exc).__name__}: {exc}")
        self.clock.observe_date_header(response.headers.get("Date"))
        data: Any = None
        if response.content:
            try:
                data = response.json()
            except ValueError:
                data = response.text
        error = None
        if not 200 <= response.status_code < 300:
            if isinstance(data, dict):
                error = data.get("message") or data.get("error") or str(data)
            else:
                error = str(data)[:300] if data else response.reason
        return ApiResult(response.status_code, data, error)

    def get(self, path: str) -> ApiResult:
        return self.request("GET", path)

    def post(self, path: str, json: Any = None, headers: dict | None = None) -> ApiResult:
        return self.request("POST", path, json=json, headers=headers)

    def patch(self, path: str, json: Any = None) -> ApiResult:
        return self.request("PATCH", path, json=json)
