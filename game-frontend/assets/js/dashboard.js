async function renderDashboardPage() {
  const app = document.getElementById("app");
  showBottomNav("dashboard");

  app.innerHTML = `
    <div class="page-container">
      <div id="dash-content">
        <div class="card animate-pulse mb-4"><div class="h-40"></div></div>
      </div>
    </div>
  `;

  try {
    const data = await apiGet("/players/me/dashboard");
    renderDashContent(data);
  } catch (err) {
    document.getElementById("dash-content").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function renderDashContent(data) {
  const container = document.getElementById("dash-content");
  const d = data.activeDigimon;

  container.innerHTML = `
    <!-- Player header -->
    <div class="flex items-center justify-between mb-4 px-1">
      <div>
        <h2 class="text-lg font-bold">${escapeHtml(data.username)}</h2>
        <p class="text-xs text-slate-400">Tamer</p>
      </div>
      <button class="text-xs text-slate-500 hover:text-red-400" onclick="authLogout()">Sair</button>
    </div>

    ${d ? renderDigimonCard(d) : `
      <div class="card text-center mb-4">
        <p class="text-slate-400">Nenhum Digimon ativo</p>
      </div>
    `}

    <!-- Resources -->
    ${d ? `
    <div class="grid grid-cols-2 gap-3 mb-4">
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">Bits</p>
        <p class="text-lg font-bold text-yellow-400">${d.bits}</p>
      </div>
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">Energia</p>
        <p class="text-lg font-bold text-green-400">${d.energy}/${d.maxEnergy}</p>
      </div>
    </div>
    ` : ""}

    <!-- Equipped items -->
    ${d ? `
    <div class="mb-4">
      <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Equipamentos</h3>
      <div class="grid grid-cols-3 gap-2">
        ${renderEquipSlots(data.equippedItems || [])}
      </div>
    </div>
    ` : ""}

    <!-- Active missions -->
    ${data.activeMissions && data.activeMissions.length > 0 ? `
    <div class="mb-4">
      <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Missões Ativas</h3>
      ${data.activeMissions.map(renderActiveMission).join("")}
    </div>
    ` : ""}

    <!-- Incubation -->
    ${data.incubation ? renderIncubation(data.incubation) : ""}
  `;

  startMissionTimers();
}

function renderDigimonCard(d) {
  const rarityColors = {
    COMMON: "border-slate-600",
    RARE: "border-blue-500",
    EPIC: "border-purple-500",
    LEGENDARY: "border-yellow-500"
  };
  const borderClass = rarityColors[d.rarity] || "border-slate-700";

  const xpNeeded = getXpForLevel(d.level);
  const xpPercent = d.level >= 100 ? 100 : Math.min(100, Math.round((d.experience / xpNeeded) * 100));

  return `
    <div class="card ${borderClass} mb-4">
      <div class="flex items-center gap-3 mb-3">
        <div class="text-5xl">🐉</div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <h3 class="font-bold text-lg truncate">${escapeHtml(d.name)}</h3>
            <span class="text-sm font-bold text-cyan-400">Lv.${d.level}</span>
          </div>
          <p class="text-xs text-slate-400">${escapeHtml(d.type) || "Desconhecido"}</p>
          <div class="flex gap-2 mt-1">
            <span class="badge badge-${d.stage.toLowerCase()}">${formatStage(d.stage)}</span>
            <span class="badge badge-${d.rarity.toLowerCase()}">${d.rarity}</span>
          </div>
        </div>
      </div>

      <!-- XP bar -->
      <div class="mb-3">
        <div class="flex justify-between text-xs text-slate-500 mb-1">
          <span>XP</span>
          <span>${d.experience} / ${xpNeeded}</span>
        </div>
        <div class="xp-bar">
          <div class="xp-bar-fill" style="width: ${xpPercent}%"></div>
        </div>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-3 gap-2 text-center text-sm">
        <div>
          <p class="text-xs text-slate-500">HP</p>
          <p class="font-bold text-red-400">${d.hp}${d.equipBonusHp ? `<span class="text-xs text-green-400">+${d.equipBonusHp}</span>` : ""}</p>
        </div>
        <div>
          <p class="text-xs text-slate-500">ATK</p>
          <p class="font-bold text-orange-400">${d.attack}${d.equipBonusAttack ? `<span class="text-xs text-green-400">+${d.equipBonusAttack}</span>` : ""}</p>
        </div>
        <div>
          <p class="text-xs text-slate-500">DEF</p>
          <p class="font-bold text-blue-400">${d.defense}${d.equipBonusDefense ? `<span class="text-xs text-green-400">+${d.equipBonusDefense}</span>` : ""}</p>
        </div>
      </div>

      <!-- Traits -->
      <div class="flex gap-2 mt-3 flex-wrap">
        <span class="badge-xs">${d.grade}</span>
        <span class="badge-xs">${formatPersonality(d.personality)}</span>
        ${d.trait ? `<span class="badge-xs badge-trait">${formatTrait(d.trait)}</span>` : ""}
        ${d.rebirthCount > 0 ? `<span class="badge-xs badge-rebirth">Rebirth ×${d.rebirthCount}</span>` : ""}
      </div>
    </div>
  `;
}

function renderEquipSlots(items) {
  const slots = ["WEAPON", "ARMOR", "ACCESSORY"];
  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
  const rarityBorder = {
    COMMON: "border-slate-600",
    RARE: "border-blue-500",
    EPIC: "border-purple-500",
    LEGENDARY: "border-yellow-500"
  };

  return slots.map(slot => {
    const item = items.find(i => i.slot === slot);
    if (item) {
      const border = rarityBorder[item.rarity] || "border-slate-600";
      return `
        <div class="card-sm text-center ${border}">
          <p class="text-lg">${slotEmoji[slot]}</p>
          <p class="text-xs font-bold truncate">${escapeHtml(item.name)}</p>
          <span class="badge badge-${item.rarity ? item.rarity.toLowerCase() : 'common'}" style="font-size:0.6rem">${item.rarity}</span>
        </div>
      `;
    }
    return `
      <div class="card-sm text-center opacity-40">
        <p class="text-lg">${slotEmoji[slot]}</p>
        <p class="text-xs text-slate-500">${slotName[slot]}</p>
      </div>
    `;
  }).join("");
}

function renderActiveMission(m) {
  const now = Date.now();
  const endsAt = new Date(m.endsAt).getTime();
  const remaining = Math.max(0, Math.floor((endsAt - now) / 1000));
  const done = remaining <= 0;

  return `
    <div class="card-sm mb-2 flex items-center justify-between" data-mission-instance="${m.instanceId}" data-ends-at="${m.endsAt}">
      <div>
        <p class="font-bold text-sm">${escapeHtml(m.missionName)}</p>
        <p class="text-xs text-slate-500 mission-timer">${done ? "Concluída!" : formatTime(remaining)}</p>
      </div>
      ${done ? `
        <button class="btn-sm btn-primary" onclick="claimMission('${m.instanceId}')">Resgatar</button>
      ` : `
        <span class="badge">Em andamento</span>
      `}
    </div>
  `;
}

function renderIncubation(inc) {
  if (!inc) return "";
  const remaining = Math.max(0, inc.remainingSeconds);
  const done = remaining <= 0;

  return `
    <div class="mb-4">
      <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Incubação</h3>
      <div class="card-sm flex items-center justify-between">
        <div>
          <p class="font-bold text-sm">${formatItemType(inc.digitamaType)}</p>
          <p class="text-xs text-slate-500">${formatItemType(inc.incubatorType)}</p>
        </div>
        <div class="text-right">
          <p class="text-xs text-slate-400">${done ? "Pronta!" : formatTime(remaining)}</p>
          <span class="badge badge-${inc.status.toLowerCase()}">${inc.status}</span>
        </div>
      </div>
    </div>
  `;
}

async function claimMission(instanceId) {
  try {
    const result = await apiPost(`/missions/${instanceId}/claim`);
    showToast(`+${result.xpGained} XP${result.levelUp ? " — LEVEL UP!" : ""}`);
    renderDashboardPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

// Timer logic
let missionTimerInterval = null;

function startMissionTimers() {
  if (missionTimerInterval) clearInterval(missionTimerInterval);
  missionTimerInterval = setInterval(() => {
    document.querySelectorAll("[data-mission-instance]").forEach(el => {
      const endsAt = new Date(el.dataset.endsAt).getTime();
      const remaining = Math.max(0, Math.floor((endsAt - Date.now()) / 1000));
      const timerEl = el.querySelector(".mission-timer");
      if (!timerEl) return;

      if (remaining <= 0) {
        timerEl.textContent = "Concluída!";
        const btn = el.querySelector("button");
        if (!btn) {
          const badgeEl = el.querySelector(".badge");
          if (badgeEl) {
            badgeEl.outerHTML = `<button class="btn-sm btn-primary" onclick="claimMission('${el.dataset.missionInstance}')">Resgatar</button>`;
          }
        }
      } else {
        timerEl.textContent = formatTime(remaining);
      }
    });
  }, 1000);
}

// Helpers
function formatStage(stage) {
  const map = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  return map[stage] || stage;
}

function formatPersonality(p) {
  const map = { DURABLE: "Durável", LIVELY: "Vivaz", FIGHTER: "Lutador", DEFENDER: "Defensor", BRAINY: "Esperto", NIMBLE: "Ágil" };
  return map[p] || p;
}

function formatTrait(t) {
  const map = { FAST_LEARNER: "XP+10%", ENERGETIC: "Energia+5", VITALITY: "HP+10%", BERSERKER: "ATK+10%", IRON_BODY: "DEF+10%" };
  return map[t] || t;
}

function formatItemType(t) {
  if (!t) return "";
  return t.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
}

function formatTime(seconds) {
  if (seconds <= 0) return "0s";
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}

function getXpForLevel(level) {
  if (level >= 100) return 0;
  return level * 100;
}
