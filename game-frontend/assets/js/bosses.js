const BOSS_TYPE_INFO = {
  NORMAL: { label: "Normal", color: "bg-slate-700 text-slate-200", desc: "Cooldown: 6h" },
  DAILY: { label: "Diario", color: "bg-blue-700 text-blue-100", desc: "1x por dia" },
  WEEKLY: { label: "Semanal", color: "bg-purple-700 text-purple-100", desc: "1x por semana" },
  MONTHLY: { label: "Mensal", color: "bg-yellow-700 text-yellow-100", desc: "1x por mes" }
};

const BOSS_STAGE_TABS = [
  { key: "ROOKIE", label: "Rookie", color: "bg-green-700 text-green-100" },
  { key: "CHAMPION", label: "Champion", color: "bg-blue-700 text-blue-100" },
  { key: "ULTIMATE", label: "Ultimate", color: "bg-purple-700 text-purple-100" },
  { key: "MEGA", label: "Mega", color: "bg-red-700 text-red-100" }
];

const STAGE_LABELS = {
  BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega"
};

let bossesData = [];
let bossActiveStage = "ROOKIE";
let bossActiveType = "NORMAL";

async function renderBossesPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold px-1">Bosses</h2>
        <button class="text-xs text-cyan-400 hover:text-cyan-300" onclick="navigateTo('boss-history')">Historico</button>
      </div>
      <div class="flex gap-2 mb-3 overflow-x-auto pb-1" id="boss-stage-tabs"></div>
      <div class="flex gap-2 mb-4 overflow-x-auto pb-1" id="boss-type-tabs"></div>
      <div id="bosses-list">
        <div class="card animate-pulse mb-3"><div class="h-24"></div></div>
        <div class="card animate-pulse mb-3"><div class="h-24"></div></div>
      </div>
    </div>
  `;

  try {
    bossesData = await apiGet("/bosses/available");
    renderBossStageTabs();
    renderBossTypeTabs();
    renderBossList();
  } catch (err) {
    document.getElementById("bosses-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function renderBossStageTabs() {
  const tabs = document.getElementById("boss-stage-tabs");
  tabs.innerHTML = BOSS_STAGE_TABS.map(s => {
    const active = s.key === bossActiveStage;
    return `
      <button class="px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-colors
        ${active ? s.color : "bg-slate-800 text-slate-400 hover:bg-slate-700"}"
        onclick="bossActiveStage='${s.key}'; renderBossStageTabs(); renderBossTypeTabs(); renderBossList();">
        ${escapeHtml(s.label)}
      </button>
    `;
  }).join("");
}

function renderBossTypeTabs() {
  const tabs = document.getElementById("boss-type-tabs");
  const types = ["NORMAL", "DAILY", "WEEKLY", "MONTHLY"];
  tabs.innerHTML = types.map(t => {
    const info = BOSS_TYPE_INFO[t];
    const active = t === bossActiveType;
    const count = bossesData.filter(b => b.requiredStage === bossActiveStage && b.bossType === t).length;
    return `
      <button class="px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-colors
        ${active ? info.color : "bg-slate-800 text-slate-400 hover:bg-slate-700"}"
        onclick="bossActiveType='${t}'; renderBossTypeTabs(); renderBossList();">
        ${escapeHtml(info.label)} (${count})
      </button>
    `;
  }).join("");
}

function renderBossList() {
  const container = document.getElementById("bosses-list");
  const filtered = bossesData.filter(b => b.requiredStage === bossActiveStage && b.bossType === bossActiveType);

  if (filtered.length === 0) {
    container.innerHTML = `<div class="card text-center text-slate-400 text-sm">Nenhum boss neste stage</div>`;
    return;
  }

  container.innerHTML = filtered.map(boss => {
    const available = boss.available;
    const cooldown = boss.cooldownRemainingSeconds;
    const typeInfo = BOSS_TYPE_INFO[boss.bossType] || BOSS_TYPE_INFO.NORMAL;

    let statusBadge = "";
    if (!available && cooldown && cooldown > 0) {
      statusBadge = `<span class="text-xs text-orange-400">Cooldown: ${formatCooldown(cooldown)}</span>`;
    } else if (!available) {
      statusBadge = `<span class="text-xs text-red-400">Requisitos nao atendidos</span>`;
    } else {
      const chanceColor = boss.winChance >= 60 ? "text-green-400" : boss.winChance >= 30 ? "text-yellow-400" : "text-red-400";
      statusBadge = `<span class="text-xs ${chanceColor}">Chance: ${boss.winChance}%</span>`;
    }

    return `
      <div class="card mb-3 cursor-pointer hover:border-slate-500 ${!available ? "opacity-60" : ""}"
        onclick="openBossDetail('${boss.code}')">
        <div class="flex items-center gap-3">
          <div class="w-14 h-14 rounded-lg bg-slate-800 flex items-center justify-center overflow-hidden flex-shrink-0">
            ${boss.imageUrl
              ? `<img src="${escapeHtml(boss.imageUrl)}" class="w-full h-full object-cover" onerror="this.parentElement.innerHTML='<span class=\\'text-2xl\\'>👹</span>'">`
              : `<span class="text-2xl">👹</span>`}
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-0.5">
              <span class="font-bold text-sm">${escapeHtml(boss.name)}</span>
              <span class="px-1.5 py-0.5 rounded text-[10px] font-bold ${typeInfo.color}">${escapeHtml(typeInfo.label)}</span>
            </div>
            <div class="text-xs text-slate-400 mb-1">
              Lv.${boss.requiredLevel}
              ${boss.requiredRebirths > 0 ? ` | Rebirth ${boss.requiredRebirths}+` : ""}
            </div>
            <div class="flex items-center gap-3 text-xs">
              <span class="text-red-400">HP ${boss.hp}</span>
              <span class="text-orange-400">ATK ${boss.atk}</span>
              <span class="text-blue-400">DEF ${boss.def}</span>
            </div>
            <div class="mt-1">${statusBadge}</div>
          </div>
          <div class="text-right text-xs text-slate-400 flex-shrink-0">
            <div>⚡ ${boss.energyCost}</div>
            <div class="text-yellow-400">${boss.baseXpReward} XP</div>
            <div class="text-amber-400">${boss.baseBitsReward} Bits</div>
          </div>
        </div>
      </div>
    `;
  }).join("");
}

function openBossDetail(bossCode) {
  const boss = bossesData.find(b => b.code === bossCode);
  if (!boss) return;

  const typeInfo = BOSS_TYPE_INFO[boss.bossType] || BOSS_TYPE_INFO.NORMAL;

  const dropsHtml = (boss.drops && boss.drops.length > 0)
    ? boss.drops.map(d => {
      const name = d.dropType === "EQUIPMENT"
        ? `${escapeHtml(d.templateName || "Equipamento")} (Raridade Aleatoria)`
        : escapeHtml(d.itemCode || "Item");
      const qty = d.minQuantity === d.maxQuantity ? `x${d.minQuantity}` : `x${d.minQuantity}-${d.maxQuantity}`;
      return `<div class="flex justify-between text-xs py-1 border-b border-slate-700 last:border-0">
        <span class="text-slate-200">${name} ${qty}</span>
        <span class="text-slate-400">${d.chance}%</span>
      </div>`;
    }).join("")
    : `<p class="text-xs text-slate-500">Sem drops configurados</p>`;

  const overlay = document.createElement("div");
  overlay.id = "boss-detail-overlay";
  overlay.className = "fixed inset-0 bg-black/70 z-50 flex items-end justify-center animate-fade-in";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  overlay.innerHTML = `
    <div class="w-full max-w-md bg-slate-900 rounded-t-2xl p-5 max-h-[85vh] overflow-y-auto animate-slide-up">
      <div class="flex items-center gap-3 mb-4">
        <div class="w-16 h-16 rounded-xl bg-slate-800 flex items-center justify-center overflow-hidden flex-shrink-0">
          ${boss.imageUrl
            ? `<img src="${escapeHtml(boss.imageUrl)}" class="w-full h-full object-cover" onerror="this.parentElement.innerHTML='<span class=\\'text-3xl\\'>👹</span>'">`
            : `<span class="text-3xl">👹</span>`}
        </div>
        <div>
          <h3 class="font-bold text-lg">${escapeHtml(boss.name)}</h3>
          <div class="flex items-center gap-2 mt-0.5">
            <span class="px-2 py-0.5 rounded text-xs font-bold ${typeInfo.color}">${escapeHtml(typeInfo.label)}</span>
            <span class="text-xs text-slate-400">${escapeHtml(STAGE_LABELS[boss.requiredStage] || boss.requiredStage)} Lv.${boss.requiredLevel}</span>
          </div>
        </div>
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Stats do Boss</p>
        <div class="grid grid-cols-3 gap-2 text-center text-sm">
          <div><span class="text-red-400 font-bold">${boss.hp}</span><br><span class="text-[10px] text-slate-400">HP</span></div>
          <div><span class="text-orange-400 font-bold">${boss.atk}</span><br><span class="text-[10px] text-slate-400">ATK</span></div>
          <div><span class="text-blue-400 font-bold">${boss.def}</span><br><span class="text-[10px] text-slate-400">DEF</span></div>
        </div>
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Recompensas</p>
        <div class="flex gap-4 text-sm mb-2">
          <span class="text-yellow-400">${boss.baseXpReward} XP</span>
          <span class="text-amber-400">${boss.baseBitsReward} Bits</span>
          <span class="text-slate-400">⚡ ${boss.energyCost} energia</span>
        </div>
        <p class="text-xs text-slate-400 mb-1">Drops possiveis:</p>
        ${dropsHtml}
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Requisitos</p>
        <div class="flex gap-4 text-sm">
          <span class="text-slate-200">Stage: ${escapeHtml(STAGE_LABELS[boss.requiredStage] || boss.requiredStage)}</span>
          <span class="text-slate-200">Level: ${boss.requiredLevel}</span>
          ${boss.requiredRebirths > 0 ? `<span class="text-slate-200">Rebirth: ${boss.requiredRebirths}+</span>` : ""}
        </div>
      </div>

      ${boss.available && boss.winChance != null
        ? `<div class="card-sm mb-3">
            <div class="flex items-center justify-between">
              <span class="text-xs text-slate-400">Sua chance de vitoria</span>
              <span class="text-sm font-bold ${boss.winChance >= 60 ? "text-green-400" : boss.winChance >= 30 ? "text-yellow-400" : "text-red-400"}">${boss.winChance}%</span>
            </div>
            ${boss.winChance < 30 ? '<p class="text-xs text-red-400 mt-1">Chance minima de 30% necessaria para desafiar</p>' : ''}
          </div>`
        : ''}

      ${boss.available && boss.winChance != null && boss.winChance >= 30
        ? `<button class="btn-primary w-full text-sm py-3" onclick="startBossChallenge('${boss.code}')">
            Desafiar ${escapeHtml(boss.name)}
          </button>`
        : boss.available && boss.winChance != null && boss.winChance < 30
        ? `<button class="w-full text-sm py-3 rounded-xl font-bold bg-red-900/50 text-red-400 cursor-not-allowed" disabled>
            Muito fraco para desafiar (min 30%)
          </button>`
        : `<button class="w-full text-sm py-3 rounded-xl font-bold bg-slate-700 text-slate-400 cursor-not-allowed" disabled>
            ${boss.cooldownRemainingSeconds && boss.cooldownRemainingSeconds > 0
              ? `Cooldown: ${formatCooldown(boss.cooldownRemainingSeconds)}`
              : "Requisitos nao atendidos"}
          </button>`
      }
    </div>
  `;

  document.body.appendChild(overlay);
}

async function startBossChallenge(bossCode) {
  const overlay = document.getElementById("boss-detail-overlay");
  if (overlay) overlay.remove();

  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="page-container flex flex-col items-center justify-center min-h-[60vh]">
      <div class="text-4xl mb-4 animate-bounce">⚔️</div>
      <p class="text-lg font-bold mb-2">Combatendo...</p>
      <p class="text-sm text-slate-400">Calculando resultado...</p>
    </div>
  `;

  try {
    const dashboard = await apiGet("/players/me/dashboard");
    const digimonId = dashboard.activeDigimon.id;

    const result = await apiPost(`/bosses/${bossCode}/challenge`, { digimonId });
    renderBossResult(result);
  } catch (err) {
    app.innerHTML = `
      <div class="page-container">
        <div class="card border-red-900 mb-4">
          <p class="text-red-300 text-sm">${escapeHtml(err.message)}</p>
        </div>
        <button class="btn-primary w-full" onclick="renderBossesPage()">Voltar</button>
      </div>
    `;
  }
}

function renderBossResult(result) {
  const app = document.getElementById("app");
  const victory = result.result === "VICTORY";

  const rarityColor = { COMMON: "text-slate-300", RARE: "text-blue-400", EPIC: "text-purple-400", LEGENDARY: "text-yellow-400" };
  const dropsHtml = result.drops && result.drops.length > 0
    ? result.drops.map(d => {
      const label = d.type === "EQUIPMENT" && d.rarity
        ? `${escapeHtml(d.name || d.code)} <span class="${rarityColor[d.rarity] || 'text-slate-300'}">(${d.rarity})</span>`
        : escapeHtml(d.name || d.code);
      return `
      <div class="flex justify-between text-sm py-1.5 border-b border-slate-700 last:border-0">
        <span class="${d.type === "EQUIPMENT" ? "text-purple-300" : "text-slate-200"}">${label}</span>
        <span class="text-slate-400">x${d.quantity}</span>
      </div>`;
    }).join("")
    : "";

  app.innerHTML = `
    <div class="page-container flex flex-col items-center">
      <div class="text-5xl mb-3 mt-6">${victory ? "🏆" : "💀"}</div>
      <h2 class="text-xl font-bold mb-1 ${victory ? "text-yellow-400" : "text-red-400"}">
        ${victory ? "Vitoria!" : "Derrota"}
      </h2>
      <p class="text-sm text-slate-400 mb-4">vs ${escapeHtml(result.bossName)}</p>

      <div class="card-sm w-full mb-3">
        <div class="grid grid-cols-2 gap-3 text-center text-sm">
          <div>
            <span class="text-slate-400 text-xs">Chance</span><br>
            <span class="font-bold ${result.winChance >= 50 ? "text-green-400" : "text-red-400"}">${result.winChance}%</span>
          </div>
          <div>
            <span class="text-slate-400 text-xs">Poder</span><br>
            <span class="font-bold text-cyan-400">${Math.round(result.digimonPower)}</span>
            <span class="text-slate-500 text-xs">vs</span>
            <span class="font-bold text-red-400">${Math.round(result.bossPower)}</span>
          </div>
        </div>
      </div>

      <div class="card-sm w-full mb-3">
        <p class="text-xs text-slate-400 mb-2">Recompensas</p>
        <div class="flex gap-4 text-sm mb-2">
          <span class="text-yellow-400">+${result.xpGained} XP</span>
          ${result.bitsGained > 0 ? `<span class="text-amber-400">+${result.bitsGained} Bits</span>` : ""}
        </div>
        ${dropsHtml ? `<p class="text-xs text-slate-400 mb-1">Drops:</p>${dropsHtml}` : ""}
      </div>

      <div class="flex gap-2 w-full">
        <button class="btn-primary flex-1" onclick="renderBossesPage()">Voltar</button>
        <button class="flex-1 px-4 py-2.5 rounded-xl text-sm font-bold bg-slate-700 hover:bg-slate-600 transition-colors" onclick="navigateTo('bosses')">
          Desafiar Outro
        </button>
      </div>
    </div>
  `;
}

function formatCooldown(seconds) {
  if (seconds <= 0) return "Pronto";
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (h > 0) return `${h}h ${m}m`;
  if (m > 0) return `${m}m`;
  return `${seconds}s`;
}

async function renderBossHistoryPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold px-1">Historico de Bosses</h2>
        <button class="text-sm text-cyan-400" onclick="renderBossesPage()">Voltar</button>
      </div>
      <div id="boss-history-list">
        <div class="card animate-pulse mb-3"><div class="h-16"></div></div>
      </div>
    </div>
  `;

  try {
    const history = await apiGet("/bosses/history", { page: 0, size: 30 });
    const container = document.getElementById("boss-history-list");

    if (!history || history.length === 0) {
      container.innerHTML = `<div class="card text-center text-slate-400 text-sm">Nenhuma tentativa registrada</div>`;
      return;
    }

    container.innerHTML = history.map(a => {
      const victory = a.status === "VICTORY";
      const date = new Date(a.createdAt);
      const dateStr = date.toLocaleDateString("pt-BR") + " " + date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });

      return `
        <div class="card-sm mb-2 flex items-center gap-3">
          <span class="text-xl">${victory ? "🏆" : "💀"}</span>
          <div class="flex-1">
            <span class="font-bold text-sm">${escapeHtml(a.bossName)}</span>
            <span class="ml-2 text-xs ${victory ? "text-green-400" : "text-red-400"}">${victory ? "Vitoria" : "Derrota"}</span>
            <div class="text-xs text-slate-400 mt-0.5">
              +${a.xpGained} XP ${a.bitsGained > 0 ? `| +${a.bitsGained} Bits` : ""}
            </div>
          </div>
          <span class="text-[10px] text-slate-500">${dateStr}</span>
        </div>
      `;
    }).join("");
  } catch (err) {
    document.getElementById("boss-history-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>
    `;
  }
}
