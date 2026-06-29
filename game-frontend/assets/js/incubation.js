let incubTimerInterval = null;

async function renderIncubationPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">🥚 Incubação</h2>
      <div id="incub-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  if (incubTimerInterval) { clearInterval(incubTimerInterval); incubTimerInterval = null; }

  try {
    const [incubation, dashboard] = await Promise.all([
      incubFetchActive(),
      apiGet("/players/me/dashboard")
    ]);

    window._incubSlotInfo = dashboard.slotInfo;

    if (incubation) {
      incubRenderActive(incubation);
    } else {
      await incubRenderStart();
    }
  } catch (err) {
    document.getElementById("incub-content").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

async function incubFetchActive() {
  return await apiGet("/incubation/me");
}

// ==================== ACTIVE INCUBATION ====================

function incubRenderActive(inc) {
  const content = document.getElementById("incub-content");
  const remaining = Math.max(0, inc.remainingSeconds);
  const done = remaining <= 0;

  const digitamaName = incubItemName(inc.digitamaType);
  const digitamaEmoji = incubDigitamaEmoji(inc.digitamaType);
  const incubatorName = incubItemName(inc.incubatorType);

  content.innerHTML = `
    <div class="card mb-4 text-center" style="border-color:#854d0e">
      <div class="text-5xl mb-3" id="incub-egg">${digitamaEmoji}</div>
      <h3 class="font-bold text-lg mb-1">${escapeHtml(digitamaName)}</h3>
      <p class="text-xs text-slate-500 mb-3">Incubadora: ${escapeHtml(incubatorName)}</p>

      <div class="mb-4">
        <p class="text-2xl font-bold ${done ? 'text-green-400' : 'text-amber-400'}" id="incub-timer">
          ${done ? "Pronta para chocar!" : incubFormatTime(remaining)}
        </p>
        ${!done ? `<p class="text-xs text-slate-500 mt-1">Aguardando incubação...</p>` : ""}
      </div>

      ${done ? incubClaimButton() : `
        <div class="w-full bg-slate-800 rounded-full h-2 mb-2">
          <div class="h-2 rounded-full" style="background:#f59e0b;width:${incubProgress(inc)}%" id="incub-bar"></div>
        </div>
        <p class="text-xs text-slate-600">Volte quando o tempo acabar</p>
      `}
    </div>
  `;

  if (!done) {
    incubStartTimer(inc);
  }
}

function incubProgress(inc) {
  const total = (new Date(inc.finishAt) - new Date(inc.startedAt)) / 1000;
  const elapsed = total - Math.max(0, inc.remainingSeconds);
  return Math.min(100, Math.round((elapsed / total) * 100));
}

function incubStartTimer(inc) {
  const finishAt = new Date(inc.finishAt).getTime();

  incubTimerInterval = setInterval(() => {
    const remaining = Math.max(0, Math.floor((finishAt - Date.now()) / 1000));
    const timerEl = document.getElementById("incub-timer");
    const barEl = document.getElementById("incub-bar");

    if (!timerEl) { clearInterval(incubTimerInterval); return; }

    if (remaining <= 0) {
      clearInterval(incubTimerInterval);
      renderIncubationPage();
      return;
    }

    timerEl.textContent = incubFormatTime(remaining);

    if (barEl) {
      const total = (new Date(inc.finishAt) - new Date(inc.startedAt)) / 1000;
      const elapsed = total - remaining;
      barEl.style.width = Math.min(100, Math.round((elapsed / total) * 100)) + "%";
    }
  }, 1000);
}

async function incubClaim() {
  const btn = document.getElementById("incub-claim-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Chocando..."; }

  try {
    const digimon = await apiPost("/incubation/claim", {});
    showToast(`${digimon.name} nasceu! (${digimon.rarity})`);
    renderIncubationPage();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "🐣 Chocar!"; }
  }
}

function incubClaimButton() {
  const si = window._incubSlotInfo;
  if (si && si.activeDigimons >= si.maxDigimonSlots) {
    return `
      <div class="mb-2">
        <p class="text-xs text-red-400 font-bold mb-1">Slots ativos cheios (${si.activeDigimons}/${si.maxDigimonSlots})</p>
        <p class="text-xs text-slate-500">Guarde um Digimon no Storage para poder chocar.</p>
      </div>
      <button class="btn-primary w-full text-lg py-3 opacity-50 cursor-not-allowed" disabled>
        🐣 Chocar!
      </button>
      <button class="btn-sm w-full mt-2" style="background:#1e3a5f;color:#7dd3fc" onclick="navigateTo('digimon-select')">
        📦 Ir para Digimon / Storage
      </button>
    `;
  }
  return `
    ${si ? `<p class="text-xs text-slate-500 mb-2">Slots: ${si.activeDigimons}/${si.maxDigimonSlots}</p>` : ''}
    <button class="btn-primary w-full text-lg py-3" id="incub-claim-btn" onclick="incubClaim()">
      🐣 Chocar!
    </button>
  `;
}

// ==================== START INCUBATION ====================

async function incubRenderStart() {
  const content = document.getElementById("incub-content");

  let inventory = [];
  try {
    inventory = await apiGet("/inventory") || [];
  } catch (e) {
    content.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(e.message)}</p></div>`;
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

  if (digitamas.length === 0 && incubators.length === 0) {
    content.innerHTML = `
      <div class="card text-center">
        <p class="text-2xl mb-2">🥚</p>
        <p class="text-slate-400">Você não tem digitamas nem incubadoras.</p>
        <p class="text-xs text-slate-500 mt-1">Compre na Loja ou ganhe em missões!</p>
        <button class="btn-primary mt-3" onclick="navigateTo('shop')">🛒 Ir para Loja</button>
      </div>
    `;
    return;
  }

  if (digitamas.length === 0) {
    content.innerHTML = `
      <div class="card text-center">
        <p class="text-2xl mb-2">🥚</p>
        <p class="text-slate-400">Você não tem nenhuma digitama.</p>
        <p class="text-xs text-slate-500 mt-1">Compre na Loja ou ganhe em missões!</p>
        <button class="btn-primary mt-3" onclick="navigateTo('shop')">🛒 Ir para Loja</button>
      </div>
    `;
    return;
  }

  if (incubators.length === 0) {
    content.innerHTML = `
      <div class="card text-center">
        <p class="text-2xl mb-2">📦</p>
        <p class="text-slate-400">Você não tem nenhuma incubadora.</p>
        <p class="text-xs text-slate-500 mt-1">Compre na Loja ou ganhe em missões!</p>
        <button class="btn-primary mt-3" onclick="navigateTo('shop')">🛒 Ir para Loja</button>
      </div>
    `;
    return;
  }

  let html = `
    <p class="text-sm text-slate-400 mb-3 px-1">Selecione uma digitama e uma incubadora para começar.</p>

    <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Digitamas</h3>
    <div class="flex flex-col gap-2 mb-4" id="incub-digitamas">
      ${digitamas.map(d => {
        const def = d.itemDefinition;
        const name = def ? def.name : incubItemName(d.itemType);
        const emoji = incubDigitamaEmoji(d.itemType);
        return `
        <button class="card-sm flex items-center gap-3 text-left w-full incub-select-btn" data-type="digitama" data-value="${d.itemType}" onclick="incubSelect(this, 'digitama')">
          <span class="text-2xl">${emoji}</span>
          <div class="flex-1">
            <p class="font-bold text-sm">${escapeHtml(name)}</p>
            <p class="text-xs text-slate-500">Qtd: ${d.quantity}</p>
          </div>
        </button>
      `;
      }).join("")}
    </div>

    <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Incubadoras</h3>
    <div class="flex flex-col gap-2 mb-4" id="incub-incubators">
      ${incubators.map(i => {
        const def = i.itemDefinition;
        const name = def ? def.name : incubItemName(i.itemType);
        const emoji = incubIncubatorEmoji(i.itemType);
        return `
        <button class="card-sm flex items-center gap-3 text-left w-full incub-select-btn" data-type="incubator" data-value="${i.itemType}" onclick="incubSelect(this, 'incubator')">
          <span class="text-2xl">${emoji}</span>
          <div class="flex-1">
            <p class="font-bold text-sm">${escapeHtml(name)}</p>
            <p class="text-xs text-slate-500">Qtd: ${i.quantity} · ${incubDuration(i.itemType)}</p>
          </div>
        </button>
      `;
      }).join("")}
    </div>

    <button class="btn-primary w-full py-3 opacity-50 cursor-not-allowed" id="incub-start-btn" disabled onclick="incubStart()">
      Selecione digitama e incubadora
    </button>
  `;

  content.innerHTML = html;
  window._incubSelected = { digitama: null, incubator: null };
}

function incubSelect(el, type) {
  document.querySelectorAll(`[data-type="${type}"]`).forEach(btn => {
    btn.classList.remove("border-cyan-500");
    btn.style.borderColor = "";
  });
  el.classList.add("border-cyan-500");
  el.style.borderColor = "#06b6d4";

  window._incubSelected[type] = el.dataset.value;

  const startBtn = document.getElementById("incub-start-btn");
  if (window._incubSelected.digitama && window._incubSelected.incubator) {
    startBtn.disabled = false;
    startBtn.classList.remove("opacity-50", "cursor-not-allowed");
    startBtn.textContent = "🥚 Iniciar Incubação";
  }
}

async function incubStart() {
  const btn = document.getElementById("incub-start-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Iniciando..."; }

  try {
    await apiPost("/incubation/start", {
      digitamaType: window._incubSelected.digitama,
      incubatorType: window._incubSelected.incubator
    });
    showToast("Incubação iniciada!");
    renderIncubationPage();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "🥚 Iniciar Incubação"; }
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
