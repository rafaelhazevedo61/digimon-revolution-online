"""Bot que joga o DRO pela API HTTP, seguindo um perfil de comportamento."""

from __future__ import annotations

import time
import uuid
from collections import Counter
from urllib.parse import quote
from datetime import datetime, timedelta, timezone

from .client import ApiClient, ApiResult
from .clock import GameClock, parse_instant
from .profiles import Profile
from .recorder import Recorder

DAY = 86400.0
# Custos de energia de boss_definitions (WORLD_BOSS_APOCALYMON e CLAN_RAID_OMEGAMON);
# os endpoints /world-boss/me e /clan-raids/me não os expõem.
WORLD_BOSS_ENERGY = 20
CLAN_RAID_ENERGY = 15
MISSION_SLOT_ITEM = "MISSION_SLOT_UNLOCK"


class GameplayBot:
    def __init__(self, name: str, profile: Profile, base_url: str, speed: float, recorder: Recorder,
                 password: str = "sim-bot-password", digitama: str = "STARTER", clan_name: str | None = None):
        self.name = name
        self.profile = profile
        self.clock = GameClock(speed)
        self.api = ApiClient(base_url, self.clock)
        self.recorder = recorder
        self.password = password
        self.digitama = digitama
        self.clan_name = clan_name
        self.clan: dict | None = None
        self.email = f"{name}@sim.local"
        self.digimon: dict = {}
        self.arena: dict = {}
        self.equip: dict = {}
        self.inventory: list = []
        self.mission_slots = 1
        self.slot_price: int | None = None
        self.counters: Counter = Counter()
        self.drops: Counter = Counter()
        self.start_game: float = 0.0

    # ------------------------------------------------------------------ infra
    def _log(self, action: str, result: ApiResult, **fields) -> ApiResult:
        if not result.ok:
            self.counters["api_errors"] += 1
            fields["status"] = result.status
            fields["error"] = result.error
        self.recorder.event(self.clock.now(), self.name, action, result.ok, **fields)
        return result

    def _call(self, action: str, method: str, path: str, json=None, headers: dict | None = None,
              **fields) -> ApiResult:
        result = self.api.request(method, path, json=json, headers=headers)
        if result.status == 401 and action != "login":
            if self.login():
                result = self.api.request(method, path, json=json, headers=headers)
        return self._log(action, result, **fields)

    def _count_drops(self, items, key_code="code", key_qty="quantity") -> None:
        for item in items or []:
            code = item.get(key_code) or item.get("itemCode") or item.get("name")
            if code:
                self.drops[code] += int(item.get(key_qty) or 1)

    # ------------------------------------------------------------- onboarding
    def login(self, quiet: bool = False) -> bool:
        result = self.api.post("/auth/login", {"email": self.email, "password": self.password})
        if not (quiet and result.status == 401):
            self._log("login", result)
        if result.ok and isinstance(result.data, dict):
            self.api.token = result.data.get("token")
            return bool(self.api.token)
        return False

    def register_login(self) -> bool:
        if self.login(quiet=True):
            return True
        reg = self.api.post("/auth/register", {"username": self.name, "email": self.email,
                                                "password": self.password})
        self._log("register", reg)
        return self.login()

    def onboarding(self) -> bool:
        startup = self._call("startup", "GET", "/players/me/startup")
        if startup.ok and not (startup.data or {}).get("hasSelectedStarter"):
            self._call("digitama_select", "POST", "/digitama/select", {"type": self.digitama})
            hatch = self._call("digitama_hatch", "POST", "/digitama/hatch")
            if hatch.ok:
                self._log("hatched", hatch, digimon=hatch.data.get("name"), rarity=hatch.data.get("rarity"))
        return self.refresh_digimon()

    def refresh_digimon(self) -> bool:
        result = self._call("digimon_me", "GET", "/digimon/me")
        if not result.ok or not result.data:
            return False
        active = [d for d in result.data if d.get("status") == "ACTIVE"]
        self.digimon = (active or result.data)[0]
        return True

    # ---------------------------------------------------------------- rotinas
    def do_tutorial(self) -> None:
        result = self._call("tutorial", "GET", "/tutorial")
        if not result.ok or (result.data or {}).get("finished"):
            return
        for step in result.data.get("steps", []):
            if step.get("completed") and not step.get("rewardClaimed"):
                self._call("tutorial_claim", "POST", f"/tutorial/steps/{step['step']}/claim",
                           step=step["step"], bits=step.get("rewardBits"))
        refreshed = self._call("tutorial", "GET", "/tutorial")
        if refreshed.ok and refreshed.data.get("canFinish"):
            self._call("tutorial_finish", "POST", "/tutorial/finish")

    def do_missions(self) -> float | None:
        """Coleta e reinicia missões. Retorna o próximo endsAt (epoch) se houver."""
        active = self._call("missions_active", "GET", "/missions/active")
        running = []
        for mission in active.data or [] if active.ok else []:
            if mission.get("status") == "COMPLETED" or (
                mission.get("status") == "RUNNING"
                and (parse_instant(mission.get("endsAt")) or 0) <= self.clock.now()
            ):
                claim = self._call("mission_claim", "POST", f"/missions/{mission['missionInstanceId']}/claim",
                                   mission=mission.get("missionId"))
                if claim.ok:
                    data = claim.data or {}
                    self.counters["missions_claimed"] += 1
                    self.counters["bits_from_missions"] += int(data.get("bitsGained") or 0)
                    self.counters["xp_from_missions"] += int(data.get("xpGained") or 0)
                    self._count_drops(data.get("rewards"))
                    if data.get("levelUp"):
                        self.counters["level_ups"] += 1
                else:
                    running.append(mission)
            elif mission.get("status") == "RUNNING":
                running.append(mission)

        self.refresh_digimon()
        slots = self._call("mission_slots", "GET", "/missions/slots")
        unlocked = int((slots.data or {}).get("unlockedSlots") or 1) if slots.ok else 1
        free = max(unlocked - len(running), 0)
        if free:
            catalog = self._call("missions_list", "GET", "/missions")
            options = [m for m in (catalog.data or []) if m.get("requiredLevel", 1) <= self.digimon.get("level", 1)]
            options.sort(key=lambda m: (m.get("xpReward", 0) / max(m.get("durationSeconds", 1), 1),
                                        m.get("xpReward", 0)), reverse=True)
            energy = int(self.digimon.get("energy") or 0)
            for mission in options:
                if not free:
                    break
                if mission.get("energyCost", 0) > energy:
                    self.counters["energy_blocked"] += 1
                    continue
                start = self._call("mission_start", "POST", "/missions/start", {"missionId": mission["id"]},
                                   mission=mission["id"], energy_cost=mission.get("energyCost"))
                if start.ok:
                    self.counters["missions_started"] += 1
                    energy -= int(mission.get("energyCost") or 0)
                    running.append({"endsAt": (start.data or {}).get("endsAt")})
                    free -= 1
                else:
                    break
        ends = [parse_instant(m.get("endsAt")) for m in running]
        ends = [e for e in ends if e]
        return min(ends) if ends else None

    def do_bosses(self) -> float | None:
        result = self._call("bosses_available", "GET", "/bosses/available")
        if not result.ok:
            return None
        next_ready = None
        for boss in result.data or []:
            cooldown = boss.get("cooldownRemainingSeconds") or 0
            if not boss.get("available"):
                continue
            if cooldown > 0:
                ready = self.clock.now() + cooldown
                next_ready = ready if next_ready is None else min(next_ready, ready)
                continue
            chance = boss.get("winChance") or 0
            if chance < self.profile.min_boss_win_chance or boss.get("energyCost", 0) > self.digimon.get("energy", 0):
                continue
            fight = self._call("boss_challenge", "POST", f"/bosses/{boss['code']}/challenge",
                               {"digimonId": self.digimon["id"]}, boss=boss["code"], win_chance=chance)
            if fight.ok:
                data = fight.data or {}
                won = str(data.get("result", "")).upper() in {"VICTORY", "WIN", "WON"}
                self.counters["bosses_won" if won else "bosses_lost"] += 1
                self.counters["bits_from_bosses"] += int(data.get("bitsGained") or 0)
                self._count_drops(data.get("drops"))
                self.refresh_digimon()
        return next_ready

    def ensure_clan(self) -> None:
        """Entra no clã do grupo do bot (cria se ainda não existir) para liberar a Incursão."""
        mine = self.api.get("/clans/me")
        if mine.ok and isinstance(mine.data, dict):
            self.clan = mine.data
            return
        for _ in range(3):
            found = self._call("clan_search", "GET", f"/clans?query={quote(self.clan_name)}&size=20")
            for clan in ((found.data or {}).get("content") or []) if found.ok else []:
                if clan.get("name") == self.clan_name and clan.get("memberCount", 0) < clan.get("maxMembers", 0):
                    if self._call("clan_join", "POST", f"/clans/{clan['id']}/join", clan=self.clan_name).ok:
                        self.clan = clan
                        return
            created = self._call("clan_create", "POST", "/clans",
                                 {"name": self.clan_name, "tag": uuid.uuid4().hex[:3].upper(),
                                  "description": "Clã de simulação"}, clan=self.clan_name)
            if created.ok:
                self.clan = created.data
                return
            time.sleep(2)

    def _boss_energy_ok(self, cost: int) -> bool:
        return int(self.digimon.get("energy") or 0) >= cost + self.profile.boss_energy_reserve

    def do_world_boss(self) -> float | None:
        state = self._call("world_boss", "GET", "/world-boss/me")
        if not state.ok:
            return None
        data = state.data or {}
        if data.get("status") != "ACTIVE":
            return None
        ready = parse_instant(data.get("nextAttackAvailableAt"))
        if ready and ready > self.clock.now():
            return ready
        if not self._boss_energy_ok(WORLD_BOSS_ENERGY):
            return None
        hit = self._call("world_boss_attack", "POST", "/world-boss/attack",
                         headers={"Idempotency-Key": str(uuid.uuid4())}, boss=data.get("bossCode"))
        if not hit.ok:
            return None
        result = hit.data or {}
        self.counters["world_boss_attacks"] += 1
        self.counters["world_boss_damage"] += int(result.get("damage") or 0)
        self.counters["bits_from_world_boss"] += int(result.get("bitsGained") or 0)
        self.counters["xp_from_world_boss"] += int(result.get("xpGained") or 0)
        if result.get("defeated"):
            self.counters["world_boss_kills"] += 1
        self.recorder.event(self.clock.now(), self.name, "world_boss_hit", True,
                            damage=result.get("damage"), win_chance=result.get("winChance"),
                            remaining_hp=result.get("remainingHp"), max_hp=result.get("maxHp"),
                            xp=result.get("xpGained"), bits=result.get("bitsGained"),
                            defeated=result.get("defeated"))
        self.refresh_digimon()
        return self.clock.now() + int(data.get("attackCooldownMinutes") or 5) * 60

    def do_clan_raid(self) -> float | None:
        if not self.clan:
            return None
        state = self._call("clan_raid", "GET", "/clan-raids/me")
        if not state.ok:
            return None
        data = state.data or {}
        if data.get("status") != "ACTIVE":
            return None
        ready = parse_instant(data.get("nextAttackAvailableAt"))
        if ready and ready > self.clock.now():
            return ready
        if not self._boss_energy_ok(CLAN_RAID_ENERGY):
            return None
        hit = self._call("clan_raid_attack", "POST", "/clan-raids/attack", boss=data.get("bossCode"))
        if not hit.ok:
            return None
        result = hit.data or {}
        self.counters["clan_raid_attacks"] += 1
        self.counters["clan_raid_damage"] += int(result.get("damage") or 0)
        self.counters["bits_from_clan_raid"] += int(result.get("bitsGained") or 0)
        self.counters["xp_from_clan_raid"] += int(result.get("xpGained") or 0)
        self.counters["clan_honor_marks"] += int(result.get("clanHonorMarksGained") or 0)
        if result.get("defeated"):
            self.counters["clan_raid_kills"] += 1
        self.recorder.event(self.clock.now(), self.name, "clan_raid_hit", True,
                            damage=result.get("damage"), win_chance=result.get("winChance"),
                            remaining_hp=result.get("remainingHp"), max_hp=result.get("maxHp"),
                            xp=result.get("xpGained"), bits=result.get("bitsGained"),
                            defeated=result.get("defeated"))
        self.refresh_digimon()
        return self.clock.now() + int(data.get("attackCooldownMinutes") or 5) * 60

    def do_arena(self) -> None:
        lobby = self._call("arena_lobby", "GET", "/arena/lobby")
        if not lobby.ok:
            return
        self.arena = lobby.data or {}
        while self.arena.get("challengesRemaining", 0) > 0 and self.arena.get("energy", 0) >= self.arena.get("energyCost", 0):
            opponents = [o for o in self.arena.get("opponents", [])
                         if o.get("cooldownSecondsRemaining", 0) <= 0
                         and o.get("winChance", 0) >= self.profile.min_arena_win_chance]
            if not opponents:
                return
            target = max(opponents, key=lambda o: (o.get("winChance", 0), o.get("rating", 0)))
            match = self._call("arena_challenge", "POST", "/arena/challenge",
                               {"opponentDigimonId": target["digimonId"]}, win_chance=target.get("winChance"))
            if not match.ok:
                return
            data = match.data or {}
            self.counters["arena_won" if data.get("victory") else "arena_lost"] += 1
            self.counters["bits_from_arena"] += int(data.get("bitsGained") or 0)
            lobby = self._call("arena_lobby", "GET", "/arena/lobby")
            if not lobby.ok:
                return
            self.arena = lobby.data or {}

    def _load_inventory(self) -> bool:
        result = self._call("inventory", "GET", "/inventory")
        if result.ok:
            self.inventory = result.data or []
        return result.ok

    def do_chests(self) -> None:
        if not self._load_inventory():
            return
        reload = False
        for item in self.inventory:
            definition = item.get("itemDefinition") or {}
            if item.get("itemType") != "LOOT_CHEST" and definition.get("category") != "CHEST":
                continue
            code, qty = definition.get("code"), int(item.get("quantity") or 0)
            if not code or qty <= 0:
                continue
            opened = self._call("chest_open", "POST", "/inventory/chests/open",
                                {"chestCode": code, "requestId": str(uuid.uuid4()), "quantity": min(qty, 999)},
                                chest=code, quantity=qty)
            if opened.ok:
                self.counters["chests_opened"] += qty
                self._count_drops((opened.data or {}).get("items"), key_code="itemCode")
                reload = True
        if reload:
            self._load_inventory()

    def _owned(self, item_type: str) -> int:
        return sum(int(i.get("quantity") or 0) for i in self.inventory if i.get("itemType") == item_type)

    def _mission_slot_price(self) -> int | None:
        if self.slot_price is None:
            shop = self._call("shop", "GET", "/shop")
            for group in (shop.data or {}).values() if shop.ok else []:
                for product in group or []:
                    if product.get("code") == MISSION_SLOT_ITEM:
                        self.slot_price = int(product.get("price") or 0)
        return self.slot_price

    def do_mission_slots(self) -> None:
        """Compra e usa o Expansor de Slot de Missão até liberar todos os slots."""
        slots = self._call("mission_slots", "GET", "/missions/slots")
        if not slots.ok:
            return
        data = slots.data or {}
        self.mission_slots = int(data.get("unlockedSlots") or 1)
        if self.mission_slots >= int(data.get("totalSlots") or 3):
            return
        price = 0
        if not self._owned(MISSION_SLOT_ITEM):
            price = self._mission_slot_price()
            if not price or int(self.digimon.get("bits") or 0) < price + self.profile.slot_bits_reserve:
                return
            if not self._call("shop_buy", "POST", "/shop/buy", {"productCode": MISSION_SLOT_ITEM, "quantity": 1},
                              product=MISSION_SLOT_ITEM, price=price).ok:
                return
        if not self._call("item_use", "POST", "/inventory/use", {"itemType": MISSION_SLOT_ITEM},
                          item=MISSION_SLOT_ITEM).ok:
            return
        self.mission_slots += 1
        self.counters["mission_slots_unlocked"] += 1
        self.counters["bits_on_mission_slots"] += price
        self.recorder.event(self.clock.now(), self.name, "mission_slot_unlocked", True, slots=self.mission_slots,
                            price=price, level=self.digimon.get("level"),
                            game_hours=round((self.clock.now() - self.start_game) / 3600, 2))
        self.refresh_digimon()

    def do_xp_discs(self) -> None:
        owned = Counter()
        for item in self.inventory:
            if str(item.get("itemType") or "").startswith("XP_DISC_"):
                owned[item["itemType"]] += int(item.get("quantity") or 0)
        for item_type, qty in sorted(owned.items()):
            if qty <= 0:
                continue
            used = self._call("item_use", "POST", "/inventory/use", {"itemType": item_type, "quantity": min(qty, 999)},
                              item=item_type, quantity=qty)
            if not used.ok:
                continue
            data = used.data or {}
            self.counters["xp_discs_used"] += int(data.get("quantity") or qty)
            self.counters["xp_from_discs"] += int(data.get("xpGranted") or 0)
            self.recorder.event(self.clock.now(), self.name, "xp_disc_used", True, item=item_type,
                                quantity=data.get("quantity"), xp=data.get("xpGranted"),
                                level_from=data.get("previousLevel"), level_to=data.get("currentLevel"))
        if owned:
            self.inventory = [i for i in self.inventory if not str(i.get("itemType") or "").startswith("XP_DISC_")]
            self.refresh_digimon()

    def do_equipment(self) -> None:
        inventory = self._call("equipment_inventory", "GET", "/equipment/inventory")
        current = self._call("equipment_current", "GET", f"/equipment/digimon/{self.digimon['id']}")
        if not inventory.ok or not current.ok:
            return
        self.equip = current.data or {}
        power = lambda e: (e.get("effectiveBonusAttack", 0) + e.get("effectiveBonusDefense", 0)
                           + e.get("effectiveBonusHp", 0) / 5)
        equipped = {e.get("slot"): e for e in self.equip.get("equippedItems", [])}
        best: dict[str, dict] = {}
        for item in inventory.data or []:
            if item.get("equipped"):
                continue
            slot = item.get("slot")
            if slot and (slot not in best or power(item) > power(best[slot])):
                best[slot] = item
        for slot, item in best.items():
            if slot not in equipped or power(item) > power(equipped[slot]):
                res = self._call("equip", "POST", "/equipment/equip", {"equipmentId": item["id"]},
                                 slot=slot, item=item.get("name"), rarity=item.get("rarity"))
                if res.ok:
                    self.counters["equips"] += 1
        self.equip = self._call("equipment_current", "GET", f"/equipment/digimon/{self.digimon['id']}").data or self.equip

    def do_evolution(self) -> None:
        options = self._call("evolution_options", "GET", f"/digimon/{self.digimon['id']}/evolution-options")
        if not options.ok:
            return
        for option in (options.data or {}).get("options", []):
            if option.get("canEvolve"):
                before = self.digimon.get("stage")
                res = self._call("evolve", "POST", "/digimon/evolve",
                                 {"evolutionLineId": option.get("evolutionLineId")},
                                 line=option.get("evolutionLineCode"), from_stage=before,
                                 level=self.digimon.get("level"),
                                 game_hours=round((self.clock.now() - self.start_game) / 3600, 2))
                if res.ok:
                    self.counters["evolutions"] += 1
                    self.refresh_digimon()
                return

    def do_calendar(self) -> None:
        cal = self._call("calendar", "GET", "/activity-calendar/current")
        if not cal.ok:
            return
        data = cal.data or {}
        for day in data.get("days", []):
            if day.get("goalReached") and not day.get("rewardClaimed"):
                if self._call("calendar_day_claim", "POST", f"/activity-calendar/days/{day['date']}/claim",
                              date=day["date"]).ok:
                    self.counters["calendar_days"] += 1
        if data.get("monthlyCompletionEligible") and not data.get("monthlyRewardClaimed"):
            self._call("calendar_month_claim", "POST",
                       f"/activity-calendar/months/{data['yearMonth']}/claim-completion")

    # -------------------------------------------------------------- snapshot
    def snapshot(self) -> None:
        d = self.digimon
        self.recorder.snapshot(self.clock.now(), {
            "game_hours": round((self.clock.now() - self.start_game) / 3600, 3),
            "bot": self.name, "profile": self.profile.name,
            "digimon": d.get("name"), "stage": d.get("stage"), "level": d.get("level"),
            "experience": d.get("experience"), "rebirths": d.get("rebirthCount"), "bits": d.get("bits"),
            "energy": d.get("energy"), "max_energy": d.get("maxEnergy"),
            "hp": d.get("hp"), "attack": d.get("attack"), "defense": d.get("defense"),
            "equip_attack": d.get("equipBonusAttack"), "equip_defense": d.get("equipBonusDefense"),
            "equip_hp": d.get("equipBonusHp"),
            "arena_rating": self.arena.get("rating"), "arena_coins": self.arena.get("arenaCoins"),
            "inventory_items": sum(int(i.get("quantity") or 0) for i in self.inventory),
            "equipment_count": len(self.equip.get("equippedItems", [])),
            "clan": (self.clan or {}).get("name"),
            "mission_slots": self.mission_slots,
            **{k: self.counters.get(k, 0) for k in (
                "missions_claimed", "bosses_won", "bosses_lost", "arena_won", "arena_lost",
                "chests_opened", "evolutions", "world_boss_attacks", "world_boss_damage",
                "clan_raid_attacks", "clan_raid_damage", "xp_discs_used", "xp_from_discs", "api_errors")},
        })

    # ------------------------------------------------------------ agendamento
    def _session_bounds(self, now: float) -> tuple[float, float] | None:
        """Retorna (início, fim) da sessão online atual, ou None se offline."""
        day = datetime.fromtimestamp(now, tz=timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
        for offset in (-1, 0):
            base = day + timedelta(days=offset)
            for hour, minutes in self.profile.sessions:
                start = (base + timedelta(hours=hour)).timestamp()
                end = start + minutes * 60
                if start <= now < end:
                    return start, end
        return None

    def _next_session_start(self, now: float) -> float:
        day = datetime.fromtimestamp(now, tz=timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
        candidates = []
        for offset in (0, 1):
            base = day + timedelta(days=offset)
            for hour, _ in self.profile.sessions:
                start = (base + timedelta(hours=hour)).timestamp()
                if start > now:
                    candidates.append(start)
        return min(candidates)

    def tick(self) -> float | None:
        p = self.profile
        if p.claim_tutorial:
            self.do_tutorial()
        if p.open_chests:
            self.do_chests()
        elif p.use_xp_discs or p.buy_mission_slots:
            self._load_inventory()
        if p.use_xp_discs:
            self.do_xp_discs()
        if p.auto_equip:
            self.do_equipment()
        if p.auto_evolve:
            self.do_evolution()
        boss_ready = self.do_bosses() if p.do_bosses else None
        if p.do_arena:
            self.do_arena()
        if p.do_missions and p.buy_mission_slots:
            self.do_mission_slots()
        mission_end = self.do_missions() if p.do_missions else None
        raid_ready = world_ready = None
        if p.do_clan_raid or p.do_world_boss:
            self.refresh_digimon()
        if p.do_clan_raid and self.clan_name:
            if not self.clan:
                self.ensure_clan()
            raid_ready = self.do_clan_raid()
        if p.do_world_boss:
            world_ready = self.do_world_boss()
        if p.claim_calendar:
            self.do_calendar()
        self.refresh_digimon()
        self.snapshot()
        wakes = [t for t in (mission_end, boss_ready, raid_ready, world_ready) if t]
        return min(wakes) if wakes else None

    def run(self, days: float) -> None:
        for attempt in range(1, 6):
            if self.register_login() and self.onboarding():
                break
            if attempt == 5:
                self.recorder.event(self.clock.now(), self.name, "abort", False, reason="onboarding failed")
                return
            time.sleep(5 * attempt)
        self.start_game = self.clock.now()
        end_game = self.start_game + days * DAY
        self.recorder.event(self.start_game, self.name, "run_start", True, profile=self.profile.name, days=days)
        while self.clock.now() < end_game:
            now = self.clock.now()
            session = self._session_bounds(now)
            if session is None:
                wake = min(self._next_session_start(now), end_game)
            else:
                next_event = self.tick()
                idle_limit = self.clock.now() + self.profile.max_idle_minutes * 60
                wake = min(next_event + 2 if next_event else idle_limit, idle_limit, session[1] + 1, end_game)
            self.clock.sleep(max(wake - self.clock.now(), 1))
        self.refresh_digimon()
        self.snapshot()
        self.recorder.event(self.clock.now(), self.name, "run_end", True, counters=dict(self.counters),
                            drops=dict(self.drops))
