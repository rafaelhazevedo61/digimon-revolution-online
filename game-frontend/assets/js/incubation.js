let incubTimerIntervals = new Map();

async function renderIncubationPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  incubStopTimer();

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4 px-1">
        <div>
          <p class="text-xs uppercase tracking-wider text-cyan-400 font-semibold">Sistema de Digimons</p>
          <h2 class="text-xl font-bold mt-1">Incubação</h2>
        </div>
        <span class="badge text-cyan-200">3 SLOTS</span>
      </div>
      <div id="incub-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  try {
    const [slotsResponse, dashboard] = await Promise.all([
      incubFetchSlots(),
      apiGet("/players/me/dashboard")
    ]);

    window._incubSlotInfo = dashboard?.slotInfo;
    window._incubSlotsResponse = slotsResponse;
    incubRenderSlots(slotsResponse);
    await incubRenderStart(slotsResponse);
  } catch (err) {
    const content = document.getElementById("incub-content");
    if (content) {
      content.innerHTML = `
        <div class="card border-red-900">
          <p class="text-red-300">${escapeHtml(err.message)}</p>
        </div>
      `;
    }
  }
}

async function incubFetchSlots() {
  return await apiGet("/incubation/me");
}

function incubRenderSlots(response) {
  const content = document.getElementById("incub-content");
  if (!content) return;

  const slots = Array.isArray(response?.slots) ? response.slots : [];
  const totalSlots = Number(response?.totalSlots) || 3;
  const unlockedSlots = Number(response?.unlockedSlots) || 1;
  const renderSlots = Array.from({ length: totalSlots }, (_, index) => slots.find(item => Number(item.slotNumber) === index + 1) || {
    slotNumber: index + 1,
    unlocked: index < unlockedSlots,
    incubation: null
  });

  content.innerHTML = `
    <div class="card mb-4 border-cyan-800 bg-cyan-950/20">
      <div class="flex items-center justify-between gap-3">
        <div>
          <p class="text-xs text-slate-400">Slots desbloqueados</p>
          <p class="text-lg font-bold text-cyan-300 mt-1">${unlockedSlots}/${totalSlots}</p>
        </div>
        <p class="text-xs text-right text-slate-500 max-w-[12rem]">Cada slot possui sua própria incubação e contador.</p>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-3 mb-5" id="incub-slots">
      ${renderSlots.map(incubRenderSlot).join("")}
    </div>

    <div id="incub-start-form"></div>
  `;

  slots.forEach(slot => {
    if (slot.unlocked && slot.incubation) {
      incubRenderTimer(slot);
    }
  });
}

function incubRenderSlot(slot) {
  const slotNumber = Number(slot.slotNumber);
  const slotLabel = `Slot ${slotNumber}`;

  if (!slot.unlocked) {
    return `
      <article class="card border-slate-700 bg-slate-900/60 opacity-80" data-incub-slot="${slotNumber}">
        <div class="flex items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <span class="text-3xl">🔒</span>
            <div>
              <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold">${slotLabel}</p>
              <h3 class="font-bold text-slate-300 mt-1">Slot bloqueado</h3>
            </div>
          </div>
          <span class="badge">BLOQUEADO</span>
        </div>
        <p class="text-sm text-slate-500 mt-3">Libere este slot futuramente para incubar um segundo ovo em paralelo.</p>
      </article>
    `;
  }

  if (!slot.incubation) {
    return `
      <article class="card border-emerald-800 bg-emerald-950/10" data-incub-slot="${slotNumber}">
        <div class="flex items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <span class="text-3xl">🥚</span>
            <div>
              <p class="text-xs uppercase tracking-wider text-emerald-400 font-semibold">${slotLabel}</p>
              <h3 class="font-bold text-slate-200 mt-1">Disponível</h3>
            </div>
          </div>
          <span class="badge text-emerald-300">LIVRE</span>
        </div>
        <button class="btn-primary w-full mt-4" onclick="incubChooseEmptySlot(${slotNumber})">Usar este slot</button>
      </article>
    `;
  }

  const incubation = slot.incubation;
  const remaining = Math.max(0, Number(incubation.remainingSeconds) || 0);
  const done = incubation.status === "READY" || remaining <= 0;
  const digitamaName = incubItemName(incubation.digitamaType);
  const digitamaEmoji = incubDigitamaEmoji(incubation.digitamaType);
  const incubatorName = incubItemName(incubation.incubatorType);

  return `
    <article class="card border-amber-800 bg-amber-950/10" data-incub-slot="${slotNumber}" data-incubation-id="${escapeAttr(String(incubation.id))}">
      <div class="flex items-start justify-between gap-3">
        <div class="flex items-center gap-3">
          <span class="text-3xl">${digitamaEmoji}</span>
          <div>
            <p class="text-xs uppercase tracking-wider text-amber-400 font-semibold">${slotLabel}</p>
            <h3 class="font-bold text-slate-200 mt-1">${escapeHtml(digitamaName)}</h3>
            <p class="text-xs text-slate-500 mt-1">${escapeHtml(incubatorName)}</p>
          </div>
        </div>
        <span class="badge ${done ? "text-green-300" : "text-amber-300"}">${done ? "PRONTO" : "EM ANDAMENTO"}</span>
      </div>

      <div class="mt-4">
        <p class="text-2xl font-bold ${done ? "text-green-400" : "text-amber-400"}" id="incub-timer-${slotNumber}">
          ${done ? "Pronta para chocar!" : incubFormatTime(remaining)}
        </p>
        ${done ? "" : `<div class="w-full bg-slate-800 rounded-full h-2 mt-3"><div class="h-2 rounded-full" style="background:#f59e0b;width:${incubProgress(incubation)}%" id="incub-bar-${slotNumber}"></div></div>`}
      </div>

      <div id="incub-slot-action-${slotNumber}" class="mt-4">
        ${done ? incubClaimButton(incubation.id) : `<p class="text-xs text-slate-500">Aguardando incubação...</p>`}
      </div>
    </article>
  `;
}

function incubRenderTimer(slot) {
  const incubation = slot.incubation;
  if (!incubation || incubation.status === "READY") return;

  const remaining = Math.max(0, Number(incubation.remainingSeconds) || 0);
  if (remaining <= 0) {
    incubMarkReady(slot.slotNumber, incubation.id);
    return;
  }

  incubStartTimer({
    key: `slot-${slot.slotNumber}`,
    finishAt: incubation.finishAt,
    remainingSeconds: remaining,
    timerId: `incub-timer-${slot.slotNumber}`,
    barId: `incub-bar-${slot.slotNumber}`,
    startedAt: incubation.startedAt,
    formatter: incubFormatTime,
    onComplete: () => incubMarkReady(slot.slotNumber, incubation.id)
  });
}

function incubMarkReady(slotNumber, incubationId) {
  const timerEl = document.getElementById(`incub-timer-${slotNumber}`);
  const actionEl = document.getElementById(`incub-slot-action-${slotNumber}`);
  const slotEl = document.querySelector(`[data-incub-slot="${slotNumber}"]`);
  if (!timerEl || !actionEl || !slotEl) return;

  timerEl.textContent = "Pronta para chocar!";
  timerEl.className = "text-2xl font-bold text-green-400";
  slotEl.classList.remove("border-amber-800", "bg-amber-950/10");
  slotEl.classList.add("border-green-800", "bg-green-950/10");
  actionEl.innerHTML = incubClaimButton(incubationId);
  const badge = slotEl.querySelector(".badge");
  if (badge) {
    badge.textContent = "PRONTO";
    badge.className = "badge text-green-300";
  }
}

function incubProgress(inc) {
  const total = (new Date(inc.finishAt) - new Date(inc.startedAt)) / 1000;
  if (!Number.isFinite(total) || total <= 0) return 0;

  const remaining = Math.max(0, Number(inc.remainingSeconds) || 0);
  const elapsed = Math.max(0, total - remaining);
  return Math.min(100, Math.round((elapsed / total) * 100));
}

function incubStopTimer(key = null) {
  if (key !== null) {
    const interval = incubTimerIntervals.get(key);
    if (interval) clearInterval(interval);
    incubTimerIntervals.delete(key);
    return;
  }

  incubTimerIntervals.forEach(interval => clearInterval(interval));
  incubTimerIntervals.clear();
}

function incubStartTimer({ key, finishAt, remainingSeconds, timerId, barId, startedAt, formatter, onComplete }) {
  const timerKey = key || timerId;
  incubStopTimer(timerKey);

  const timerEl = document.getElementById(timerId);
  if (!timerEl) return;

  const initialRemaining = Number(remainingSeconds);
  if (!Number.isFinite(initialRemaining)) {
    timerEl.textContent = "--:--";
    return;
  }

  const deadline = Date.now() + Math.max(0, initialRemaining) * 1000;
  const finishTimestamp = new Date(finishAt).getTime();
  const startedTimestamp = new Date(startedAt).getTime();

  const updateProgress = remaining => {
    const barEl = document.getElementById(barId);
    if (!barEl) return;

    const total = finishTimestamp - startedTimestamp;
    if (!Number.isFinite(total) || total <= 0) return;

    const elapsed = Math.max(0, total - remaining * 1000);
    barEl.style.width = Math.min(100, Math.round((elapsed / total) * 100)) + "%";
  };

  const tick = () => {
    const currentTimerEl = document.getElementById(timerId);
    if (!currentTimerEl) {
      incubStopTimer(timerKey);
      return false;
    }

    const remaining = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
    if (remaining <= 0) {
      incubStopTimer(timerKey);
      onComplete?.();
      return false;
    }

    currentTimerEl.textContent = formatter(remaining);
    updateProgress(remaining);
    return true;
  };

  if (tick()) {
    incubTimerIntervals.set(timerKey, setInterval(tick, 1000));
  }
}

async function incubClaim(incubationId) {
  const btn = document.getElementById(`incub-claim-${incubationId}`);
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Chocando...";
  }

  try {
    const digimon = await apiPost(`/incubation/${encodeURIComponent(incubationId)}/claim`, {});
    if (!digimon || !digimon.id) {
      throw new Error("O servidor não retornou o Digimon chocado.");
    }

    showToast(`${digimon.name} nasceu! (${digimon.rarity})`);
    renderIncubationPage();
    incubShowHatchResult(digimon);
  } catch (err) {
    showToast(err.message, "error");
    if (btn) {
      btn.disabled = false;
      btn.textContent = "🐣 Chocar!";
    }
  }
}

function incubCloseHatchResult() {
  document.getElementById("incub-hatch-result-modal")?.remove();
}

function incubShowHatchResult(digimon) {
  incubCloseHatchResult();

  const overlay = document.createElement("div");
  overlay.id = "incub-hatch-result-modal";
  overlay.className = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.setAttribute("aria-labelledby", "incub-hatch-result-title");
  overlay.innerHTML = `
    <div class="card w-full max-w-sm text-center border-cyan-700 shadow-2xl">
      <div class="text-5xl mb-3">🐣</div>
      <h3 id="incub-hatch-result-title" class="text-xl font-bold text-cyan-300">${escapeHtml(digimon.name || "Novo Digimon")} nasceu!</h3>
      <p class="text-sm text-slate-400 mt-2">O Digimon foi adicionado à sua coleção.</p>
      <div class="mt-4 rounded-lg bg-slate-900/70 p-3 text-left text-xs text-slate-300">
        <div class="flex justify-between"><span>Estágio</span><strong>${escapeHtml(digimon.stage || "BABY")}</strong></div>
        <div class="flex justify-between mt-1"><span>Raridade</span><strong>${escapeHtml(digimon.rarity || "COMMON")}</strong></div>
      </div>
      <div class="grid gap-2 mt-5">
        <button class="btn-primary w-full" id="incub-select-hatched-btn" onclick="incubSelectHatched('${escapeAttr(String(digimon.id))}')">Tornar ativo</button>
        <button class="btn-sm w-full" id="incub-store-hatched-btn" style="background:#164e63;color:#67e8f9" onclick="incubStoreHatched('${escapeAttr(String(digimon.id))}')">Enviar para Storage</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);
}

async function incubSelectHatched(digimonId) {
  const btn = document.getElementById("incub-select-hatched-btn");
  const storeBtn = document.getElementById("incub-store-hatched-btn");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Tornando ativo...";
  }
  if (storeBtn) storeBtn.disabled = true;

  try {
    await apiPost("/digimon/select", { digimonId });
    incubCloseHatchResult();
    showToast("O Digimon chocado agora é seu parceiro ativo!");
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
    if (btn) {
      btn.disabled = false;
      btn.textContent = "Tornar ativo";
    }
    if (storeBtn) storeBtn.disabled = false;
  }
}

async function incubStoreHatched(digimonId) {
  const btn = document.getElementById("incub-store-hatched-btn");
  const selectBtn = document.getElementById("incub-select-hatched-btn");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Enviando...";
  }
  if (selectBtn) selectBtn.disabled = true;

  try {
    await apiPost(`/digimon/${encodeURIComponent(digimonId)}/store`, {});
    incubCloseHatchResult();
    showToast("O Digimon chocado foi enviado para o Storage!");
    renderIncubationPage();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) {
      btn.disabled = false;
      btn.textContent = "Enviar para Storage";
    }
    if (selectBtn) selectBtn.disabled = false;
  }
}

function incubClaimButton(incubationId) {
  const safeId = escapeAttr(String(incubationId));
  return `<button class="btn-primary w-full text-lg py-3" id="incub-claim-${safeId}" onclick="incubClaim('${safeId}')">🐣 Chocar!</button>`;
}

// ==================== START INCUBATION ====================

async function incubRenderStart(response) {
  const form = document.getElementById("incub-start-form");
  if (!form) return;

  const slots = Array.isArray(response?.slots) ? response.slots : [];
  const availableSlots = slots
    .filter(slot => slot.unlocked && !slot.incubation)
    .sort((a, b) => Number(a.slotNumber) - Number(b.slotNumber));

  if (availableSlots.length === 0) {
    form.innerHTML = `
      <div class="card border-slate-700 text-center">
        <p class="text-2xl mb-2">⏳</p>
        <p class="text-slate-300 font-bold">Nenhum slot desbloqueado está livre</p>
        <p class="text-xs text-slate-500 mt-1">Aguarde uma incubação terminar ou libere novos slots futuramente.</p>
      </div>
    `;
    return;
  }

  let inventory = [];
  try {
    inventory = invAggregateItems(await apiGet("/inventory") || []);
  } catch (e) {
    form.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(e.message)}</p></div>`;
    return;
  }

  const digitamas = inventory.filter(i => {
    const def = i.itemDefinition;
    if (def && def.category === "DIGITAMA" && i.quantity > 0) return true;
    return ["DIGITAMA_FIRE", "DIGITAMA_WATER", "DIGITAMA_NATURE", "DIGITAMA_STARTER"].includes(i.itemType) && i.quantity > 0;
  });

  const incubators = inventory.filter(i => {
    const def = i.itemDefinition;
    if (def && def.category === "INCUBATOR" && i.quantity > 0) return true;
    return ["INCUBATOR_COMMON", "INCUBATOR_RARE", "INCUBATOR_EPIC"].includes(i.itemType) && i.quantity > 0;
  });

  if (digitamas.length === 0 || incubators.length === 0) {
    const itemLabel = digitamas.length === 0 && incubators.length === 0
      ? "digitamas nem incubadoras"
      : digitamas.length === 0 ? "digitamas" : "incubadoras";
    form.innerHTML = `
      <div class="card text-center">
        <p class="text-2xl mb-2">${digitamas.length === 0 ? "🥚" : "📦"}</p>
        <p class="text-slate-400">Você não tem ${itemLabel} disponíveis.</p>
        <p class="text-xs text-slate-500 mt-1">Compre na Loja ou ganhe em missões!</p>
        <button class="btn-primary mt-3" onclick="navigateTo('shop')">🛒 Ir para Loja</button>
      </div>
    `;
    return;
  }

  window._incubSelected = {
    slotNumber: Number(availableSlots[0].slotNumber),
    digitama: null,
    incubator: null
  };

  form.innerHTML = `
    <div class="card border-cyan-800" id="incub-start-panel">
      <h3 class="text-sm font-bold text-slate-300 mb-1">Iniciar nova incubação</h3>
      <p class="text-xs text-slate-500 mb-4">Escolha um slot desbloqueado e vazio, uma digitama e uma incubadora.</p>

      <h4 class="text-xs uppercase tracking-wider text-slate-500 font-semibold mb-2">Slot</h4>
      <div class="grid grid-cols-3 gap-2 mb-4" id="incub-slot-options">
        ${availableSlots.map(slot => `
          <button class="card-sm text-center incub-slot-select-btn ${Number(slot.slotNumber) === Number(availableSlots[0].slotNumber) ? "border-cyan-500" : ""}" data-slot-number="${Number(slot.slotNumber)}" onclick="incubSelectSlot(${Number(slot.slotNumber)}, this)">
            <span class="text-lg">🥚</span>
            <p class="text-xs font-bold mt-1">Slot ${Number(slot.slotNumber)}</p>
          </button>
        `).join("")}
      </div>

      <h4 class="text-xs uppercase tracking-wider text-slate-500 font-semibold mb-2">Digitamas</h4>
      <div class="flex flex-col gap-2 mb-4" id="incub-digitamas">
        ${digitamas.map(d => {
          const def = d.itemDefinition;
          const name = def ? def.name : incubItemName(d.itemType);
          const emoji = incubDigitamaEmoji(d.itemType);
          return `
            <button class="card-sm flex items-center gap-3 text-left w-full incub-select-btn" data-type="digitama" data-value="${escapeAttr(d.itemType)}" onclick="incubSelect(this, 'digitama')">
              <span class="text-2xl">${emoji}</span>
              <div class="flex-1"><p class="font-bold text-sm">${escapeHtml(name)}</p><p class="text-xs text-slate-500">Qtd: ${d.quantity}</p></div>
            </button>
          `;
        }).join("")}
      </div>

      <h4 class="text-xs uppercase tracking-wider text-slate-500 font-semibold mb-2">Incubadoras</h4>
      <div class="flex flex-col gap-2 mb-4" id="incub-incubators">
        ${incubators.map(i => {
          const def = i.itemDefinition;
          const name = def ? def.name : incubItemName(i.itemType);
          const emoji = incubIncubatorEmoji(i.itemType);
          return `
            <button class="card-sm flex items-center gap-3 text-left w-full incub-select-btn" data-type="incubator" data-value="${escapeAttr(i.itemType)}" onclick="incubSelect(this, 'incubator')">
              <span class="text-2xl">${emoji}</span>
              <div class="flex-1"><p class="font-bold text-sm">${escapeHtml(name)}</p><p class="text-xs text-slate-500">Qtd: ${i.quantity} · ${incubDuration(i.itemType)}</p></div>
            </button>
          `;
        }).join("")}
      </div>

      <button class="btn-primary w-full py-3 opacity-50 cursor-not-allowed" id="incub-start-btn" disabled onclick="incubStart()">Selecione digitama e incubadora</button>
    </div>
  `;
}

function incubChooseEmptySlot(slotNumber) {
  const form = document.getElementById("incub-start-form");
  if (!form) return;
  const button = form.querySelector(`[data-slot-number="${Number(slotNumber)}"]`);
  if (button) incubSelectSlot(Number(slotNumber), button);
  form.scrollIntoView({ behavior: "smooth", block: "start" });
}

function incubSelectSlot(slotNumber, el) {
  document.querySelectorAll(".incub-slot-select-btn").forEach(btn => {
    btn.classList.remove("border-cyan-500");
    btn.style.borderColor = "";
  });
  el.classList.add("border-cyan-500");
  el.style.borderColor = "#06b6d4";
  if (window._incubSelected) window._incubSelected.slotNumber = Number(slotNumber);
}

function incubSelect(el, type) {
  document.querySelectorAll(`[data-type="${type}"]`).forEach(btn => {
    btn.classList.remove("border-cyan-500");
    btn.style.borderColor = "";
  });
  el.classList.add("border-cyan-500");
  el.style.borderColor = "#06b6d4";

  if (!window._incubSelected) window._incubSelected = { slotNumber: 1, digitama: null, incubator: null };
  window._incubSelected[type] = el.dataset.value;

  const startBtn = document.getElementById("incub-start-btn");
  if (!startBtn) return;
  if (window._incubSelected.digitama && window._incubSelected.incubator && window._incubSelected.slotNumber) {
    startBtn.disabled = false;
    startBtn.classList.remove("opacity-50", "cursor-not-allowed");
    startBtn.textContent = "🥚 Iniciar Incubação";
  }
}

async function incubStart() {
  const btn = document.getElementById("incub-start-btn");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Iniciando...";
  }

  try {
    await apiPost("/incubation/start", {
      slotNumber: Number(window._incubSelected.slotNumber),
      digitamaType: window._incubSelected.digitama,
      incubatorType: window._incubSelected.incubator
    });
    showToast(`Incubação iniciada no slot ${window._incubSelected.slotNumber}!`);
    renderIncubationPage();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) {
      btn.disabled = false;
      btn.textContent = "🥚 Iniciar Incubação";
    }
  }
}

// ==================== HELPERS ====================

function incubItemName(type) {
  const map = {
    DIGITAMA_FIRE: "Digitama de Fogo",
    DIGITAMA_WATER: "Digitama de Água",
    DIGITAMA_NATURE: "Digitama de Natureza",
    INCUBATOR_COMMON: "Incubadora Comum",
    INCUBATOR_RARE: "Incubadora Rara",
    INCUBATOR_EPIC: "Incubadora Épica"
  };
  return map[type] || type;
}

function incubDigitamaEmoji(type) {
  const map = { DIGITAMA_FIRE: "🔥", DIGITAMA_WATER: "💧", DIGITAMA_NATURE: "🌿", DIGITAMA_STARTER: "⭐" };
  return map[type] || "🥚";
}

function incubIncubatorEmoji(type) {
  const map = { INCUBATOR_COMMON: "📦", INCUBATOR_RARE: "💎", INCUBATOR_EPIC: "👑" };
  return map[type] || "📦";
}

function incubDuration(type) {
  const map = { INCUBATOR_COMMON: "5 min", INCUBATOR_RARE: "2 min", INCUBATOR_EPIC: "30 seg" };
  return map[type] || "?";
}

function incubFormatTime(seconds) {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s.toString().padStart(2, "0")}`;
}
