const AREA_INFO = {
  NATIVE_FOREST: { name: "Floresta Nativa", emoji: "🌲", color: "from-green-900 to-green-950", border: "border-green-700" },
  GEAR_SAVANNA: { name: "Savana Gear", emoji: "⚙️", color: "from-amber-900 to-amber-950", border: "border-amber-700" },
  FACTORIAL_TOWN: { name: "Cidade Fatorial", emoji: "🏭", color: "from-slate-800 to-slate-900", border: "border-slate-600" },
  FREEZELAND: { name: "Terra Congelada", emoji: "❄️", color: "from-blue-900 to-blue-950", border: "border-blue-700" },
  SERVER_DESERT: { name: "Deserto Server", emoji: "🏜️", color: "from-orange-900 to-orange-950", border: "border-orange-700" },
  INFINITY_MOUNTAIN: { name: "Montanha Infinita", emoji: "🏔️", color: "from-purple-900 to-purple-950", border: "border-purple-700" }
};

const STAGE_ORDER = ["BABY", "BABY_II", "ROOKIE", "CHAMPION", "ULTIMATE", "MEGA"];

async function renderMissionsPage() {
  const app = document.getElementById("app");
  showBottomNav("missions");

  app.innerHTML = `
    <div class="page-container">
      <div id="active-missions"></div>
      <h2 class="text-lg font-bold mb-4 px-1">Mapa de Áreas</h2>
      <div id="areas-list">
        <div class="card animate-pulse mb-3"><div class="h-20"></div></div>
        <div class="card animate-pulse mb-3"><div class="h-20"></div></div>
      </div>
    </div>
  `;

  loadActiveMissions();

  try {
    const areas = await apiGet("/areas");
    renderAreaCards(areas);
  } catch (err) {
    document.getElementById("areas-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

async function loadActiveMissions() {
  const container = document.getElementById("active-missions");
  if (!container) return;

  let active;
  try {
    active = await apiGet("/missions/active");
  } catch (err) {
    return;
  }

  if (!active || active.length === 0) {
    container.innerHTML = "";
    return;
  }

  container.innerHTML = `
    <div class="mb-4">
      <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Missões em Andamento</h3>
      ${active.map(renderActiveMissionCard).join("")}
    </div>
  `;

  startMissionsPageTimers();
}

function formatMissionName(mission) {
  const id = mission.id || mission.missionId;
  const names = {
    MISSION_ADMIN: "Missão de Teste",
    MISSION_1: "Patrulha na Floresta Nativa",
    MISSION_2: "Caçada na Savana Gear",
    MISSION_3: "Investigação na Cidade Fatorial",
    MISSION_4: "Expedição na Terra Congelada",
    MISSION_5: "Travessia do Deserto Server",
    MISSION_6: "Ascensão à Montanha Infinita"
  };
  if (names[id]) return names[id];

  const friendlyName = mission.name || mission.missionName;
  if (!friendlyName) return "Missão";

  return String(friendlyName)
    .replace(/Gear Savanna/g, "Savana Gear")
    .replace(/Factorial Town/g, "Cidade Fatorial")
    .replace(/Freezeland/g, "Terra Congelada")
    .replace(/Server Desert/g, "Deserto Server")
    .replace(/Infinity Mountain/g, "Montanha Infinita");
}

function renderActiveMissionCard(m) {
  const now = Date.now();
  const endsAt = new Date(m.endsAt).getTime();
  const remaining = Math.max(0, Math.floor((endsAt - now) / 1000));
  const done = m.status === "COMPLETED" || remaining <= 0;

  return `
    <div class="card-sm mb-2 flex items-center justify-between" data-mp-instance="${m.missionInstanceId}" data-mp-ends-at="${m.endsAt}">
      <div class="min-w-0">
        <p class="font-bold text-sm truncate">${escapeHtml(formatMissionName(m))}</p>
        <p class="text-xs mp-timer ${done ? "text-green-400 font-bold" : "text-slate-500"}">${done ? "Concluída!" : formatTime(remaining)}</p>
      </div>
      ${done ? `
        <button class="btn-sm btn-primary" onclick="claimMissionFromList('${m.missionInstanceId}')">Resgatar</button>
      ` : `
        <span class="badge">Em andamento</span>
      `}
    </div>
  `;
}

async function claimMissionFromList(instanceId) {
  try {
    const result = await apiPost(`/missions/${instanceId}/claim`);
    showMissionClaimModal(result);
    await loadActiveMissions();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function missionRewardLabel(reward) {
  if (reward && reward.itemName) return reward.itemName;
  if (reward && String(reward.itemCode || "").startsWith("CHEST_")) return "Baú";
  if (reward && reward.itemCode) return reward.itemCode;

  const labels = {
    POTION_SMALL: "Poção Pequena",
    TRAINING_STONE: "Pedra de Treino",
    DATA_CORE: "Núcleo de Dados",
    DIGITAMA_FIRE: "Digitama de Fogo",
    DIGITAMA_WATER: "Digitama de Água",
    DIGITAMA_NATURE: "Digitama de Natureza",
    INCUBATOR_COMMON: "Incubadora Comum",
    INCUBATOR_RARE: "Incubadora Rara",
    INCUBATOR_EPIC: "Incubadora Épica",
    LOOT_CHEST: "Baú"
  };
  return labels[reward && reward.item] || (reward && reward.item) || "Recompensa";
}

function missionRewardIcon(reward) {
  if (reward && (reward.item === "LOOT_CHEST" || String(reward.itemCode || "").startsWith("CHEST_"))) {
    return "🎁";
  }
  if (reward && String(reward.item || "").startsWith("DIGITAMA")) return "🥚";
  if (reward && String(reward.item || "").startsWith("INCUBATOR")) return "📦";
  if (reward && String(reward.item || "").includes("FRAGMENT")) return "🧩";
  return "✨";
}

function showMissionClaimModal(result) {
  const existing = document.getElementById("mission-claim-modal");
  if (existing) existing.remove();

  const rewards = Array.isArray(result && result.rewards) ? result.rewards : [];
  const missionName = formatMissionName({ id: result && result.missionId });
  const hasFriendlyMissionName = missionName !== "Missão";
  const rewardMarkup = rewards.length > 0
    ? rewards.map(reward => {
        const isChest = reward.item === "LOOT_CHEST" || String(reward.itemCode || "").startsWith("CHEST_");
        return `
          <div class="flex items-center gap-3 rounded-lg border ${isChest ? "border-cyan-700 bg-cyan-950/40" : "border-slate-700 bg-slate-900/60"} px-3 py-3">
            <span class="text-2xl" aria-hidden="true">${missionRewardIcon(reward)}</span>
            <div class="min-w-0 flex-1">
              <p class="font-semibold ${isChest ? "text-cyan-200" : "text-slate-100"}">${escapeHtml(missionRewardLabel(reward))}</p>
              ${isChest ? "<p class=\"text-xs text-cyan-400 mt-1\">Abra pelo Inventário para revelar o loot</p>" : ""}
            </div>
            <span class="font-bold text-cyan-300">x${Number(reward.quantity) || 0}</span>
          </div>
        `;
      }).join("")
    : `<p class="text-sm text-slate-400">Nenhuma recompensa de item foi registrada.</p>`;

  const overlay = document.createElement("div");
  overlay.id = "mission-claim-modal";
  overlay.className = "fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/75";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.setAttribute("aria-labelledby", "mission-claim-title");
  overlay.innerHTML = `
    <div class="card w-full max-w-lg max-h-[88vh] overflow-y-auto" onclick="event.stopPropagation()">
      <div class="flex items-start justify-between gap-4 mb-5">
        <div>
          <p class="text-xs uppercase tracking-wider text-cyan-400 font-bold">Recompensa recebida</p>
          <h3 id="mission-claim-title" class="text-xl font-bold mt-1">Missão concluída!</h3>
          ${hasFriendlyMissionName ? `<p class="text-sm text-slate-400 mt-1">${escapeHtml(missionName)}</p>` : ""}
        </div>
        <button class="text-slate-400 hover:text-white text-2xl leading-none" aria-label="Fechar" onclick="document.getElementById('mission-claim-modal')?.remove()">&times;</button>
      </div>

      <div class="grid grid-cols-2 gap-3 mb-5">
        <div class="rounded-lg border border-purple-800 bg-purple-950/40 px-3 py-3 text-center">
          <p class="text-xs text-purple-300">Experiência</p>
          <p class="text-xl font-bold text-purple-200 mt-1">+${Number(result && result.xpGained) || 0} XP</p>
        </div>
        <div class="rounded-lg border border-yellow-800 bg-yellow-950/40 px-3 py-3 text-center">
          <p class="text-xs text-yellow-300">Bits</p>
          <p class="text-xl font-bold text-yellow-200 mt-1">+${Number(result && result.bitsGained) || 0}</p>
        </div>
      </div>

      ${result && result.levelUp ? `
        <div class="rounded-lg border border-emerald-700 bg-emerald-950/40 px-3 py-3 mb-5 text-center">
          <p class="font-bold text-emerald-300">Level Up!</p>
          <p class="text-xs text-emerald-200 mt-1">Seu Digimon subiu de nível.</p>
        </div>
      ` : ""}

      <div>
        <h4 class="text-sm font-bold text-slate-300 mb-2">Itens recebidos</h4>
        <div class="space-y-2">${rewardMarkup}</div>
      </div>

      <button class="btn-primary w-full mt-5" onclick="document.getElementById('mission-claim-modal')?.remove()">Continuar</button>
    </div>
  `;
  overlay.addEventListener("click", event => {
    if (event.target === overlay) overlay.remove();
  });
  document.body.appendChild(overlay);
}

let missionsPageTimerInterval = null;

function startMissionsPageTimers() {
  if (missionsPageTimerInterval) clearInterval(missionsPageTimerInterval);
  missionsPageTimerInterval = setInterval(() => {
    const cards = document.querySelectorAll("[data-mp-instance]");
    if (cards.length === 0) { clearInterval(missionsPageTimerInterval); return; }

    cards.forEach(el => {
      const endsAt = new Date(el.dataset.mpEndsAt).getTime();
      const remaining = Math.max(0, Math.floor((endsAt - Date.now()) / 1000));
      const timerEl = el.querySelector(".mp-timer");
      if (!timerEl) return;

      if (remaining <= 0) {
        timerEl.textContent = "Concluída!";
        timerEl.className = "text-xs mp-timer text-green-400 font-bold";
        const badge = el.querySelector(".badge");
        if (badge) {
          badge.outerHTML = `<button class="btn-sm btn-primary" onclick="claimMissionFromList('${el.dataset.mpInstance}')">Resgatar</button>`;
        }
      } else {
        timerEl.textContent = formatTime(remaining);
      }
    });
  }, 1000);
}

function renderAreaCards(areas) {
  const container = document.getElementById("areas-list");

  const sorted = [...areas].sort((a, b) => {
    return STAGE_ORDER.indexOf(a.requiredStage) - STAGE_ORDER.indexOf(b.requiredStage);
  });

  container.innerHTML = sorted.map(a => {
    const info = AREA_INFO[a.area] || { name: a.area, emoji: "📍", color: "from-slate-800 to-slate-900", border: "border-slate-700" };
    const locked = !a.unlocked;

    return `
      <div class="card mb-3 bg-gradient-to-r ${info.color} ${info.border} ${locked ? "opacity-40" : "cursor-pointer hover:scale-[1.02] transition-transform"}"
        ${locked ? "" : `onclick="navigateTo('mission-area', { area: '${a.area}' })"`}>
        <div class="flex items-center gap-4">
          <span class="text-3xl">${info.emoji}</span>
          <div class="flex-1">
            <h3 class="font-bold">${info.name}</h3>
            <p class="text-xs text-slate-400">Stage mínimo: ${formatStage(a.requiredStage)}</p>
          </div>
          ${locked ? `<span class="text-xl">🔒</span>` : `<span class="text-slate-400">›</span>`}
        </div>
      </div>
    `;
  }).join("");
}


// Mission list for a specific area
async function renderMissionAreaPage(params) {
  const area = (params && params.area) || (window._routeParams && window._routeParams.area);
  if (!area) return navigateTo("missions");

  const app = document.getElementById("app");
  const info = AREA_INFO[area] || { name: area, emoji: "📍" };
  showBottomNav("missions");

  app.innerHTML = `
    <div class="page-container">
      <button class="text-sm text-cyan-400 mb-3 flex items-center gap-1" onclick="navigateTo('missions')">
        ← Voltar ao Mapa
      </button>

      <div class="flex items-center gap-3 mb-4">
        <span class="text-3xl">${info.emoji}</span>
        <div>
          <h2 class="text-lg font-bold">${info.name}</h2>
          <p class="text-xs text-slate-400">Missões disponíveis</p>
        </div>
      </div>

      <div id="mission-list">
        <div class="card animate-pulse mb-3"><div class="h-24"></div></div>
      </div>
    </div>
  `;

  try {
    const missions = await apiGet("/missions");
    const areaMissions = missions.filter(m => {
      const missionArea = getMissionArea(m);
      return missionArea === area;
    });
    renderMissionCards(areaMissions, area);
  } catch (err) {
    document.getElementById("mission-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function getMissionArea(mission) {
  return mission.area || null;
}

function renderMissionCards(missions, area) {
  const container = document.getElementById("mission-list");

  if (missions.length === 0) {
    container.innerHTML = `
      <div class="card text-center">
        <p class="text-slate-400">Nenhuma missão disponível nesta área.</p>
        <p class="text-xs text-slate-500 mt-1">Seu Digimon pode não atender os requisitos.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = missions.map(m => `
    <div class="card mb-3">
      <div class="flex justify-between items-start mb-2">
        <div class="flex-1">
          <h3 class="font-bold text-sm">${escapeHtml(formatMissionName(m))}</h3>
          <p class="text-xs text-slate-400 mt-1">${escapeHtml(m.description) || ""}</p>
        </div>
      </div>

      <div class="flex gap-3 text-xs text-slate-400 mb-3 flex-wrap">
        <span>⚡ Nível ${m.requiredLevel}</span>
        <span>✨ ${m.xpReward} XP</span>
        ${m.bitsReward > 0 ? `<span class="text-yellow-500">💰 ${m.bitsReward} bits</span>` : ""}
        <span>🔋 ${m.energyCost} energia</span>
        <span>⏱️ ${formatTime(m.durationSeconds)}</span>
      </div>

      <button class="btn-primary w-full text-sm" onclick="startMission('${m.id}', '${area}')">
        Iniciar Missão
      </button>
    </div>
  `).join("");
}

async function startMission(missionId, area) {
  try {
    await apiPost("/missions/start", { missionId: missionId });
    showToast("Missão iniciada!");
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
  }
}
