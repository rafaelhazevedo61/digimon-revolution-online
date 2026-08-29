let dashEquippedItems = [];

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
  dashEquippedItems = data.equippedItems || [];

  container.innerHTML = `
    <!-- Player header -->
    <div class="flex items-center justify-between mb-4 px-1">
      <div>
        <h2 class="text-lg font-bold">${escapeHtml(data.username)}</h2>
        <p class="text-xs text-slate-400">Tamer</p>
      </div>
      <button class="text-xs text-slate-500 hover:text-red-400" onclick="authLogout()">Sair</button>
    </div>

    <div id="dash-mail-notice"></div>

    <div id="dash-weekend-double-reward"></div>

    <div id="tutorial-card"></div>

    ${d ? renderDigimonCard(d) : `
      <div class="card text-center mb-4">
        <p class="text-slate-400">Nenhum Digimon ativo</p>
      </div>
    `}

    <!-- Resources -->
    <div class="grid grid-cols-2 gap-3 mb-4">
      ${d ? `
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">Bits</p>
        <p class="text-lg font-bold text-yellow-400">${d.bits}</p>
      </div>
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">Energia</p>
        <p class="text-lg font-bold text-green-400">${d.energy}/${d.maxEnergy}${d.clanBonusMaxEnergy ? `<span class="text-xs text-cyan-400">+${d.clanBonusMaxEnergy}</span>` : ""}</p>
      </div>
      ` : ""}
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">Dados Digitais</p>
        <p class="text-lg font-bold text-cyan-400">${Number(data.digitalData || 0).toLocaleString()}</p>
      </div>
    </div>

    <!-- Equipped items -->
    ${d ? `
    <div class="mb-4">
      <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Equipamentos</h3>
      <div class="grid grid-cols-3 gap-2">
        ${renderEquipSlots(data.equippedItems || [])}
      </div>
      ${renderSetBonus(data.setBonus)}
    </div>

    <!-- Actions -->
    <div class="grid grid-cols-3 gap-2 mb-4">
      <button class="btn-primary w-full" onclick="navigateTo('evolution')">⚡ Evoluir</button>
      <button class="w-full py-2 rounded-lg font-bold text-sm" style="background:#854d0e;color:#fbbf24" onclick="navigateTo('rebirth')">🔄 Rebirth</button>
      <button class="w-full py-2 rounded-lg font-bold text-sm" style="background:#164e63;color:#67e8f9" onclick="navigateTo('storage')">📦 Armazém</button>
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

  container.querySelectorAll("[data-rename-digimon-id]").forEach(button => {
    button.addEventListener("click", event => {
      event.stopPropagation();
      openRenameModal(button.dataset.renameDigimonId, button.dataset.renameDigimonName);
    });
  });
  startMissionTimers();
  startIncubationTimer(data.incubation);
  loadTutorialCard();
  loadDashboardMailNotice();
  loadDashboardWeekendDoubleReward();
}

async function loadDashboardWeekendDoubleReward() {
  const banner = document.getElementById("dash-weekend-double-reward");
  if (!banner) return;

  try {
    const result = await apiGet("/events/weekend-double-reward");
    if (!result || !result.active) {
      banner.innerHTML = "";
      return;
    }

    banner.innerHTML = `
      <div
        class="card-sm w-full mb-4 text-left border-amber-400/80 bg-gradient-to-r from-amber-950/90 via-yellow-950/70 to-amber-900/50 shadow-lg shadow-amber-950/40"
        role="status"
        aria-label="Evento de Double XP e Double Bits ativo"
      >
        <div class="flex items-center justify-between gap-3">
          <div class="min-w-0">
            <p class="text-[0.65rem] uppercase tracking-[0.18em] font-bold text-amber-300">Evento ativo</p>
            <p class="font-bold text-sm text-yellow-100 mt-1">Dobro de XP &amp; Bits</p>
            <p class="text-xs text-amber-200/80 mt-1">Apenas XP e Bits recebem bônus neste fim de semana</p>
          </div>
          <span class="shrink-0 rounded-lg border border-yellow-300/60 bg-yellow-400/20 px-2 py-1 text-lg font-black text-yellow-200">2×</span>
        </div>
      </div>
    `;
  } catch (err) {
    banner.innerHTML = "";
  }
}

async function loadDashboardMailNotice() {
  const notice = document.getElementById("dash-mail-notice");
  if (!notice) return;

  try {
    const result = await apiGet("/mail/unread-count");
    const count = Number(result?.count || 0);
    if (count <= 0) {
      notice.innerHTML = "";
      return;
    }

    const label = count === 1 ? "mensagem não lida" : "mensagens não lidas";
    const badge = count > 99 ? "99+" : String(count);
    notice.innerHTML = `
      <button class="card-sm w-full mb-4 text-left border-cyan-700 bg-cyan-950/30 hover:bg-cyan-950/50" onclick="navigateTo('mail')" aria-label="Abrir Correio com ${count} ${label}">
        <div class="flex items-center justify-between gap-3">
          <div class="min-w-0">
            <p class="text-xs uppercase tracking-wider text-cyan-300">Correio</p>
            <p class="font-bold text-sm text-slate-100 mt-1">Você tem ${count} ${label}</p>
            <p class="text-xs text-slate-400 mt-1">Toque aqui para abrir sua Entrada.</p>
          </div>
          <span class="badge text-cyan-200 shrink-0">${badge}</span>
        </div>
      </button>
    `;
  } catch (err) {
    notice.innerHTML = "";
  }
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
        ${renderDigimonVisual(d.imageUrl, d.stage, "w-16 h-16", "text-5xl")}
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <h3 class="font-bold text-lg truncate">${escapeHtml(d.name)}</h3>
            <button class="text-slate-500 hover:text-slate-300 text-xs" data-rename-digimon-id="${escapeAttr(d.id)}" data-rename-digimon-name="${escapeAttr(d.name)}" title="Renomear">✏️</button>
            <span class="text-sm font-bold text-cyan-400">Lv.${d.level}</span>
          </div>
          <p class="text-xs text-slate-400">${escapeHtml(formatDigimonType(d.type))}</p>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge badge-${d.stage.toLowerCase()}">${escapeHtml(formatStage(d.stage))}</span>
            <span class="badge badge-${d.rarity.toLowerCase()}">${escapeHtml(formatRarity(d.rarity))}</span>${renderRarityDieIndicator(d)}
            ${d.attribute ? `<span class="badge badge-common">${escapeHtml(formatAttribute(d.attribute))}</span>` : ""}
            ${d.element ? `<span class="badge badge-common">${escapeHtml(formatElement(d.element))}</span>` : ""}
          </div>
        </div>
      </div>

      <!-- XP bar -->
      <div class="mb-3">
        <div class="flex justify-between text-xs text-slate-500 mb-1">
          <span>XP</span>
          <span>${d.experience} / ${xpNeeded}</span>
        </div>
        <div class="xp-bar xp-bar-with-label" role="progressbar" aria-valuenow="${xpPercent}" aria-valuemin="0" aria-valuemax="100" aria-label="${xpPercent}% da experiência para o próximo nível">
          <div class="xp-bar-fill" style="width: ${xpPercent}%"></div>
          <span class="xp-bar-label">${xpPercent}%</span>
        </div>
      </div>

      <!-- Stats -->
      <div class="grid grid-cols-3 gap-2 text-center text-sm">
        <div>
          <p class="text-xs text-slate-500">HP</p>
          <p class="font-bold text-red-400">${d.hp}${d.equipBonusHp ? `<span class="text-xs text-green-400">+${d.equipBonusHp}</span>` : ""}${d.clanBonusHp ? `<span class="text-xs text-cyan-400">+${d.clanBonusHp}</span>` : ""}</p>
        </div>
        <div>
          <p class="text-xs text-slate-500">ATK</p>
          <p class="font-bold text-orange-400">${d.attack}${d.equipBonusAttack ? `<span class="text-xs text-green-400">+${d.equipBonusAttack}</span>` : ""}${d.clanBonusAttack ? `<span class="text-xs text-cyan-400">+${d.clanBonusAttack}</span>` : ""}</p>
        </div>
        <div>
          <p class="text-xs text-slate-500">DEF</p>
          <p class="font-bold text-blue-400">${d.defense}${d.equipBonusDefense ? `<span class="text-xs text-green-400">+${d.equipBonusDefense}</span>` : ""}${d.clanBonusDefense ? `<span class="text-xs text-cyan-400">+${d.clanBonusDefense}</span>` : ""}</p>
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
      const refLabel = item.refinementLevel > 0 ? ` +${item.refinementLevel}` : "";
      return `
        <div class="card-sm text-center ${border}" style="cursor:pointer" onclick="showEquipDetailModal('${item.id}')">
          <p class="text-lg">${slotEmoji[slot]}</p>
          <p class="text-xs font-bold truncate">${escapeHtml(item.name)}${refLabel}</p>
          <div class="flex gap-1 justify-center flex-wrap">
            <span class="badge badge-${item.rarity ? item.rarity.toLowerCase() : 'common'}" style="font-size:0.6rem">T${item.tier || '?'}</span>
          </div>
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

function renderSetBonus(sb) {
  if (!sb || !sb.setCode || sb.pieceCount < 2) return "";
  const setLabels = { BERSERKER: "Berserker", GUARDIAN: "Guardiao", VITALITY: "Vitalidade", BALANCED: "Equilibrado" };
  const label = setLabels[sb.setCode] || sb.setCode;
  const bonuses = [];
  if (sb.bonusHpPercent > 0) bonuses.push(`<span class="text-red-400">HP +${sb.bonusHpPercent}%</span>`);
  if (sb.bonusAtkPercent > 0) bonuses.push(`<span class="text-orange-400">ATK +${sb.bonusAtkPercent}%</span>`);
  if (sb.bonusDefPercent > 0) bonuses.push(`<span class="text-blue-400">DEF +${sb.bonusDefPercent}%</span>`);
  if (bonuses.length === 0) return "";
  return `
    <div class="mt-2 px-2 py-1.5 rounded-lg bg-slate-800 text-xs text-center">
      <span class="text-yellow-400 font-bold">Set ${escapeHtml(label)} (${sb.pieceCount}/3)</span>: ${bonuses.join(" ")}
    </div>
  `;
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
  if (!inc || !Array.isArray(inc.slots)) return "";
  const activeSlots = inc.slots.filter(slot => slot.incubation);
  if (activeSlots.length === 0) return "";

  return `
    <div class="mb-4" id="dash-incubation">
      <div class="flex items-center justify-between mb-2 px-1">
        <h3 class="text-sm font-bold text-slate-300">Incubação</h3>
        <button class="text-xs text-cyan-400" onclick="navigateTo('incubation')">Ver slots</button>
      </div>
      <div class="grid grid-cols-1 gap-2">
        ${activeSlots.map(slot => renderDashboardIncubationSlot(slot)).join("")}
      </div>
    </div>
  `;
}

function renderDashboardIncubationSlot(slot) {
  const slotNumber = Number(slot.slotNumber);
  if (!slot.unlocked) {
    return `
      <div class="card-sm flex items-center justify-between opacity-70" data-dash-incub-slot="${slotNumber}">
        <div class="flex items-center gap-2"><span>🔒</span><span class="text-sm">Slot ${slotNumber}</span></div>
        <span class="badge">Bloqueado</span>
      </div>
    `;
  }

  if (!slot.incubation) {
    return `
      <div class="card-sm flex items-center justify-between" data-dash-incub-slot="${slotNumber}" onclick="navigateTo('incubation')">
        <div class="flex items-center gap-2"><span>🥚</span><span class="text-sm">Slot ${slotNumber}</span></div>
        <span class="badge text-emerald-300">Livre</span>
      </div>
    `;
  }

  const incubation = slot.incubation;
  const remaining = Math.max(0, Number(incubation.remainingSeconds) || 0);
  const done = incubation.status === "READY" || remaining <= 0;
  return `
    <div class="card-sm flex items-center justify-between cursor-pointer" data-dash-incub-slot="${slotNumber}" data-finish-at="${escapeAttr(incubation.finishAt)}" data-started-at="${escapeAttr(incubation.startedAt)}" data-remaining-seconds="${remaining}" onclick="navigateTo('incubation')">
      <div class="min-w-0">
        <p class="font-bold text-sm truncate">Slot ${slotNumber} · ${formatItemType(incubation.digitamaType)}</p>
        <p class="text-xs text-slate-500">${formatItemType(incubation.incubatorType)}</p>
      </div>
      <div class="text-right shrink-0 ml-2">
        <p class="text-xs ${done ? "text-green-400 font-bold" : "text-amber-400"}" id="incub-dash-timer-${slotNumber}">${done ? "Pronta! 🐣" : formatTime(remaining)}</p>
        ${done ? `<button class="btn-sm btn-primary mt-1" onclick="event.stopPropagation(); navigateTo('incubation')">Chocar</button>` : ""}
      </div>
    </div>
  `;
}

async function claimMission(instanceId) {
  try {
    const result = await apiPost(`/missions/${instanceId}/claim`);
    showMissionClaimModal(result);
    renderDashboardPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

// Incubation timers
function startIncubationTimer(inc = null) {
  if (typeof incubStopTimer === "function") incubStopTimer();
  if (!inc || !Array.isArray(inc.slots)) return;

  inc.slots.forEach(slot => {
    const incubation = slot.incubation;
    if (!slot.unlocked || !incubation || incubation.status === "READY") return;

    const remaining = Math.max(0, Number(incubation.remainingSeconds) || 0);
    if (remaining <= 0) {
      dashboardMarkIncubationReady(Number(slot.slotNumber));
      return;
    }

    incubStartTimer({
      key: `dashboard-slot-${Number(slot.slotNumber)}`,
      finishAt: incubation.finishAt,
      startedAt: incubation.startedAt,
      remainingSeconds: remaining,
      timerId: `incub-dash-timer-${Number(slot.slotNumber)}`,
      formatter: formatTime,
      onComplete: () => dashboardMarkIncubationReady(Number(slot.slotNumber))
    });
  });
}

function dashboardMarkIncubationReady(slotNumber) {
  const timerEl = document.getElementById(`incub-dash-timer-${slotNumber}`);
  if (!timerEl) return;

  timerEl.textContent = "Pronta! 🐣";
  timerEl.className = "text-xs text-green-400 font-bold";
  const parent = timerEl.parentElement;
  if (parent && !parent.querySelector("button")) {
    parent.insertAdjacentHTML("beforeend", `<button class="btn-sm btn-primary mt-1" onclick="event.stopPropagation(); navigateTo('incubation')">Chocar</button>`);
  }
}

// Mission timer logic
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
  const map = {
    DIGITAMA_STARTER: "Digitama Inicial",
    DIGITAMA_FIRE: "Digitama de Fogo",
    DIGITAMA_WATER: "Digitama de Água",
    DIGITAMA_NATURE: "Digitama de Planta",
    DIGITAMA_EARTH: "Digitama de Terra",
    DIGITAMA_WIND: "Digitama de Vento",
    DIGITAMA_LIGHT: "Digitama de Luz",
    DIGITAMA_DARK: "Digitama de Trevas",
    DIGITAMA_THUNDER: "Digitama de Trovão",
    DIGITAMA_NEUTRAL: "Digitama Neutro",
    DIGITAMA_ICE: "Digitama de Gelo",
    DIGITAMA_STEEL: "Digitama de Metal",
    INCUBATOR_COMMON: "Incubadora Comum",
    INCUBATOR_RARE: "Incubadora Rara",
    INCUBATOR_EPIC: "Incubadora Épica"
  };
  return map[t] || t.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
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

function openRenameModal(digimonId, currentName) {
  const overlay = document.createElement("div");
  overlay.id = "rename-overlay";
  overlay.className = "fixed inset-0 bg-black/70 z-50 flex items-center justify-center animate-fade-in";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  overlay.innerHTML = `
    <div class="card" style="max-width:360px;width:90%;">
      <h3 class="font-bold text-lg mb-3 text-center">Renomear Digimon</h3>
      <input id="rename-input" type="text" maxlength="20" value="${escapeHtml(currentName)}"
        class="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-600 text-white text-sm mb-1 outline-none focus:border-cyan-500"
        placeholder="Novo nome (max 20 caracteres)">
      <p class="text-xs text-slate-500 mb-3 text-right"><span id="rename-char-count">${currentName.length}</span>/20</p>
      <div class="flex gap-2">
        <button class="flex-1 py-2 rounded-lg font-bold text-sm bg-slate-700 text-slate-300" onclick="document.getElementById('rename-overlay').remove()">Cancelar</button>
        <button id="rename-confirm-btn" class="flex-1 py-2 rounded-lg font-bold text-sm btn-primary" onclick="confirmRename('${digimonId}')">Salvar</button>
      </div>
    </div>
  `;

  document.body.appendChild(overlay);

  const input = document.getElementById("rename-input");
  input.focus();
  input.select();
  input.addEventListener("input", () => {
    document.getElementById("rename-char-count").textContent = input.value.length;
  });
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") confirmRename(digimonId);
  });
}

async function confirmRename(digimonId) {
  const input = document.getElementById("rename-input");
  const newName = input.value.trim();
  if (!newName || newName.length > 20) {
    showToast("Nome deve ter entre 1 e 20 caracteres", "error");
    return;
  }

  const btn = document.getElementById("rename-confirm-btn");
  btn.disabled = true;
  btn.textContent = "Salvando...";

  try {
    await apiPut("/digimon/rename", { digimonId, newName });
    document.getElementById("rename-overlay").remove();
    showToast("Digimon renomeado!");
    renderDashboardPage();
  } catch (err) {
    showToast(err.message, "error");
    btn.disabled = false;
    btn.textContent = "Salvar";
  }
}

function showEquipDetailModal(equipmentId) {
  const eq = dashEquippedItems.find(e => e.id === equipmentId);
  if (!eq) return;

  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
  const emoji = slotEmoji[eq.slot] || "⚔️";
  const refLabel = eq.refinementLevel > 0 ? ` +${eq.refinementLevel}` : "";
  const setLabel = typeof invSetLabel === "function" ? invSetLabel(eq.setCode) : (eq.setCode || "");
  const setBadge = typeof invSetBadge === "function" ? invSetBadge(eq.setCode) : "common";
  const rarityLabel = { COMMON: "Common", RARE: "Rare", EPIC: "Epic", LEGENDARY: "Legendary" };

  const overlay = document.createElement("div");
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(0,0,0,0.7);z-index:50;display:flex;align-items:flex-end;justify-content:center;";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  overlay.innerHTML = `
    <div class="card" style="max-width:420px;width:100%;max-height:85vh;overflow-y:auto;border-radius:1rem 1rem 0 0;margin:0 auto;">
      <div class="text-center mb-3">
        <div class="text-3xl mb-1">${emoji}</div>
        <h3 class="text-lg font-bold">${escapeHtml(eq.name)}${refLabel}</h3>
        <p class="text-xs text-slate-400 mb-2">${slotName[eq.slot] || eq.slot}</p>
        <div class="flex gap-1 justify-center flex-wrap">
          ${eq.setCode ? `<span class="badge badge-${setBadge}">${escapeHtml(setLabel)}</span>` : ''}
          <span class="badge badge-${eq.rarity ? eq.rarity.toLowerCase() : 'common'}">${rarityLabel[eq.rarity] || eq.rarity}</span>
          <span class="badge badge-common">T${eq.tier || '?'}</span>
        </div>
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Stats Base</p>
        <div class="grid grid-cols-3 gap-2 text-center text-sm">
          ${eq.bonusHp > 0 ? `<div><span class="text-slate-400">HP</span><br><span class="text-red-400">${eq.bonusHp}</span></div>` : ''}
          ${eq.bonusAttack > 0 ? `<div><span class="text-slate-400">ATK</span><br><span class="text-orange-400">${eq.bonusAttack}</span></div>` : ''}
          ${eq.bonusDefense > 0 ? `<div><span class="text-slate-400">DEF</span><br><span class="text-blue-400">${eq.bonusDefense}</span></div>` : ''}
        </div>
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Stats Efetivos</p>
        <div class="grid grid-cols-3 gap-2 text-center text-sm">
          ${eq.effectiveBonusHp > 0 ? `<div><span class="text-slate-400">HP</span><br><span class="text-red-400 font-bold">${eq.effectiveBonusHp}</span></div>` : ''}
          ${eq.effectiveBonusAttack > 0 ? `<div><span class="text-slate-400">ATK</span><br><span class="text-orange-400 font-bold">${eq.effectiveBonusAttack}</span></div>` : ''}
          ${eq.effectiveBonusDefense > 0 ? `<div><span class="text-slate-400">DEF</span><br><span class="text-blue-400 font-bold">${eq.effectiveBonusDefense}</span></div>` : ''}
        </div>
      </div>

      ${eq.refinementLevel > 0 ? `
      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-1">Refinamento</p>
        <p class="text-center text-sm font-bold text-yellow-400">+${eq.refinementLevel} (+${eq.refinementLevel * 2} em cada stat)</p>
      </div>
      ` : ''}

      <button class="btn-primary w-full py-3 text-base" onclick="this.closest('div[style]').remove()">Fechar</button>
    </div>
  `;

  document.body.appendChild(overlay);
}
