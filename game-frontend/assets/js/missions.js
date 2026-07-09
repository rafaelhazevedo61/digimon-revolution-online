const AREA_INFO = {
  NATIVE_FOREST: { name: "Floresta Nativa", emoji: "🌲", color: "from-green-900 to-green-950", border: "border-green-700" },
  GEAR_SAVANNA: { name: "Gear Savanna", emoji: "⚙️", color: "from-amber-900 to-amber-950", border: "border-amber-700" },
  FACTORIAL_TOWN: { name: "Factorial Town", emoji: "🏭", color: "from-slate-800 to-slate-900", border: "border-slate-600" },
  FREEZELAND: { name: "Freezeland", emoji: "❄️", color: "from-blue-900 to-blue-950", border: "border-blue-700" },
  SERVER_DESERT: { name: "Server Desert", emoji: "🏜️", color: "from-orange-900 to-orange-950", border: "border-orange-700" },
  INFINITY_MOUNTAIN: { name: "Infinity Mountain", emoji: "🏔️", color: "from-purple-900 to-purple-950", border: "border-purple-700" }
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

function renderActiveMissionCard(m) {
  const now = Date.now();
  const endsAt = new Date(m.endsAt).getTime();
  const remaining = Math.max(0, Math.floor((endsAt - now) / 1000));
  const done = m.status === "COMPLETED" || remaining <= 0;

  return `
    <div class="card-sm mb-2 flex items-center justify-between" data-mp-instance="${m.missionInstanceId}" data-mp-ends-at="${m.endsAt}">
      <div class="min-w-0">
        <p class="font-bold text-sm truncate">${escapeHtml(m.missionName || m.missionId)}</p>
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
    showToast(`+${result.xpGained} XP${result.levelUp ? " — LEVEL UP!" : ""}`);
    loadActiveMissions();
  } catch (err) {
    showToast(err.message, "error");
  }
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
    return STAGE_ORDER.indexOf(getAreaStage(a.area)) - STAGE_ORDER.indexOf(getAreaStage(b.area));
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
            <p class="text-xs text-slate-400">Stage mínimo: ${formatStage(getAreaStage(a.area))}</p>
          </div>
          ${locked ? `<span class="text-xl">🔒</span>` : `<span class="text-slate-400">›</span>`}
        </div>
      </div>
    `;
  }).join("");
}

function getAreaStage(area) {
  const map = {
    NATIVE_FOREST: "BABY",
    GEAR_SAVANNA: "ROOKIE",
    FACTORIAL_TOWN: "ROOKIE",
    FREEZELAND: "CHAMPION",
    SERVER_DESERT: "ULTIMATE",
    INFINITY_MOUNTAIN: "MEGA"
  };
  return map[area] || "BABY";
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
          <h3 class="font-bold text-sm">${escapeHtml(m.name)}</h3>
          <p class="text-xs text-slate-400 mt-1">${escapeHtml(m.description) || ""}</p>
        </div>
      </div>

      <div class="flex gap-3 text-xs text-slate-400 mb-3 flex-wrap">
        <span>⚡ Nível ${m.requiredLevel}</span>
        <span>✨ ${m.xpReward} XP</span>
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
