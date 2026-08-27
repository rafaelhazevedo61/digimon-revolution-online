async function renderStarterPage() {
  const app = document.getElementById("app");
  const nav = document.getElementById("bottom-nav");
  if (nav) nav.classList.add("hidden");

  try {
    const startup = await apiGet("/players/me/startup");
    if (startup && startup.redirectTo) {
      if (startup.redirectTo === "DIGITAMA_HATCHING") {
        const hatched = await apiPost("/digitama/hatch");
        renderHatchingAnimation(hatched);
        return;
      }
      if (startup.redirectTo !== "DIGITAMA_SELECTION") {
        const route = startup.redirectTo === "DIGIMON_SELECTION" ? "digimon-select" : "dashboard";
        navigateTo(route);
        return;
      }
    }
  } catch (err) {
    console.error("renderStarterPage: erro ao verificar estado inicial", err);
  }

  app.innerHTML = `
    <div class="flex items-center justify-center min-h-screen p-4">
      <div class="w-full max-w-lg text-center">
        <h2 class="text-2xl font-bold text-cyan-400 mb-2">Escolha seu Digitama!</h2>
        <p class="text-slate-400 text-sm mb-8">Selecione um Digitama para começar sua jornada no Mundo Digital.</p>
        <div id="starter-list" class="grid grid-cols-3 gap-4">
          <div class="card animate-pulse"><div class="h-32"></div></div>
          <div class="card animate-pulse"><div class="h-32"></div></div>
          <div class="card animate-pulse"><div class="h-32"></div></div>
        </div>
        <div id="starter-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/50 border border-red-900 text-red-300 text-sm"></div>
      </div>
    </div>
  `;

  try {
    const pools = await apiGet("/digitama-pools/available");
    const starterPool = pools.find(p => p.code === "DIGITAMA_STARTER");
    if (!starterPool) throw new Error("Pool de starters não encontrada");
    renderStarterCards(starterPool);
  } catch (err) {
    document.getElementById("starter-error").textContent = err.message;
    document.getElementById("starter-error").classList.remove("hidden");
  }
}

function renderStarterCards(pool) {
  const types = [
    { type: "FIRE", emoji: "🔥", color: "border-orange-500", bg: "bg-orange-950/30", label: "Fogo" },
    { type: "WATER", emoji: "💧", color: "border-blue-500", bg: "bg-blue-950/30", label: "Água" },
    { type: "NATURE", emoji: "🌿", color: "border-green-500", bg: "bg-green-950/30", label: "Natureza" }
  ];

  const container = document.getElementById("starter-list");
  container.innerHTML = types.map(t => {
    const entries = pool.entries.filter(e =>
      e.element && e.element.toUpperCase().includes(t.type)
    );

    return `
      <button class="card ${t.color} ${t.bg} hover:scale-105 transition-transform cursor-pointer text-center"
        onclick="starterSelect('${t.type}')">
        <div class="text-4xl mb-3">${t.emoji}</div>
        <h3 class="font-bold text-sm mb-1">${t.label}</h3>
        <p class="text-xs text-slate-400">Digitama ${t.label}</p>
        ${entries.length > 0 ? `<p class="text-xs text-slate-500 mt-2">${entries.map(e => escapeHtml(e.digimonName)).join(", ")}</p>` : ""}
      </button>
    `;
  }).join("");
}

async function starterSelect(type) {
  const errorDiv = document.getElementById("starter-error");
  errorDiv.classList.add("hidden");

  try {
    await apiPost("/digitama/select", { type: "STARTER" });
    const hatched = await apiPost("/digitama/hatch");
    renderHatchingAnimation(hatched);
  } catch (err) {
    errorDiv.textContent = err.message;
    errorDiv.classList.remove("hidden");
  }
}

function renderHatchingAnimation(digimon) {
  const app = document.getElementById("app");
  const nav = document.getElementById("bottom-nav");
  if (nav) nav.classList.add("hidden");

  app.innerHTML = `
    <div class="flex items-center justify-center min-h-screen p-4">
      <div class="w-full max-w-sm text-center">
        <div id="hatch-stage-egg" class="hatch-container">
          <div class="hatch-glow"></div>
          <div id="hatch-egg" class="hatch-egg">
            <div class="egg-body">🥚</div>
          </div>
          <p class="text-slate-400 text-sm mt-6 animate-pulse">Chocando...</p>
          <div class="hatch-particles" id="hatch-particles"></div>
        </div>

        <div id="hatch-stage-crack" class="hatch-container hidden">
          <div class="hatch-glow hatch-glow-intense"></div>
          <div class="hatch-egg hatch-egg-crack">
            <div class="egg-body">🥚</div>
            <div class="crack-lines">✨</div>
          </div>
          <p class="text-cyan-400 text-sm mt-6 font-bold animate-pulse">Está nascendo!</p>
        </div>

        <div id="hatch-stage-reveal" class="hatch-container hidden">
          <div class="hatch-burst"></div>
          <div class="hatch-reveal-digimon">
            ${renderDigimonVisual(digimon.imageUrl, digimon.stage, "w-28 h-28", "text-6xl")}
          </div>
          <h2 class="text-2xl font-bold text-cyan-400 mt-6">${escapeHtml(digimon.name)}</h2>
          <p class="text-slate-400 text-sm mt-1">${escapeHtml(digimon.type)}</p>
          <div class="flex justify-center gap-2 mt-3">
            <span class="badge badge-${digimon.stage ? digimon.stage.toLowerCase() : 'baby'}">${escapeHtml(digimon.stage)}</span>
            <span class="badge badge-${digimon.rarity ? digimon.rarity.toLowerCase() : 'common'}">${escapeHtml(digimon.rarity)}</span>
          </div>
          <div class="grid grid-cols-3 gap-3 mt-4">
            <div class="card-sm text-center">
              <p class="text-xs text-slate-400">HP</p>
              <p class="text-sm font-bold text-green-400">${digimon.hp}</p>
            </div>
            <div class="card-sm text-center">
              <p class="text-xs text-slate-400">ATK</p>
              <p class="text-sm font-bold text-red-400">${digimon.attack}</p>
            </div>
            <div class="card-sm text-center">
              <p class="text-xs text-slate-400">DEF</p>
              <p class="text-sm font-bold text-blue-400">${digimon.defense}</p>
            </div>
          </div>
          <button id="hatch-confirm-btn" type="button" class="btn-primary w-full mt-6" onclick="hatchConfirm('${digimon.id}')">
            Começar Jornada!
          </button>
          <div id="hatch-error" class="hidden mt-3 p-3 rounded-lg bg-red-950/50 border border-red-900 text-red-300 text-sm" role="alert"></div>
        </div>
      </div>
    </div>
  `;

  runHatchSequence();
}

function runHatchSequence() {
  const egg = document.getElementById("hatch-egg");
  const stageEgg = document.getElementById("hatch-stage-egg");
  const stageCrack = document.getElementById("hatch-stage-crack");
  const stageReveal = document.getElementById("hatch-stage-reveal");

  egg.classList.add("hatch-wobble");

  setTimeout(() => {
    egg.classList.remove("hatch-wobble");
    egg.classList.add("hatch-wobble-intense");
  }, 2000);

  setTimeout(() => {
    stageEgg.classList.add("hidden");
    stageCrack.classList.remove("hidden");
  }, 3500);

  setTimeout(() => {
    stageCrack.classList.add("hidden");
    stageReveal.classList.remove("hidden");
    stageReveal.classList.add("hatch-reveal-animate");
  }, 5000);
}

async function hatchConfirm(digimonId) {
  const btn = document.getElementById("hatch-confirm-btn");
  const errorDiv = document.getElementById("hatch-error");

  if (errorDiv) {
    errorDiv.textContent = "";
    errorDiv.classList.add("hidden");
  }

  if (!digimonId || digimonId === "undefined" || digimonId === "null") {
    console.error("hatchConfirm: digimonId inválido", digimonId);
    showToast("Não foi possível identificar o Digimon chocado.", "error");
    return;
  }

  if (btn) {
    btn.disabled = true;
    btn.textContent = "Iniciando jornada...";
  }

  try {
    console.log("hatchConfirm: selecionando Digimon", digimonId);
    await apiPost("/digimon/select", { digimonId: digimonId });
    console.log("hatchConfirm: sucesso, navegando para dashboard");
    // Remove a tela de seleção do histórico para o botão "voltar" não voltar ao starter
    history.replaceState(null, "", window.location.pathname + window.location.search + "#dashboard");
    navigateTo("dashboard");
  } catch (err) {
    console.error("hatchConfirm: erro", err);
    const message = err && err.message ? err.message : "Não foi possível iniciar a jornada.";
    showToast(message, "error");
    if (errorDiv) {
      errorDiv.textContent = message;
      errorDiv.classList.remove("hidden");
    }

    if (btn) {
      btn.disabled = false;
      btn.textContent = "Começar Jornada!";
    }
  }
}

async function renderDigimonSelectPage() {
  const app = document.getElementById("app");
  showBottomNav("digimons");

  app.innerHTML = `
    <div class="page-container">
      <div class="w-full max-w-lg mx-auto">
        <h2 class="text-2xl font-bold text-cyan-400 mb-2 text-center">Selecione seu Digimon</h2>
        <div id="digimon-new-notice"></div>
        <div id="digimon-slot-info" class="mb-3"></div>
        <p class="text-slate-400 text-sm mb-4 text-center">Escolha qual Digimon será seu parceiro ativo. Você pode manter os demais no Storage.</p>
        <div id="digimon-select-list" class="flex flex-col gap-3">
          <div class="card animate-pulse"><div class="h-20"></div></div>
        </div>
        <div class="mt-4 text-center">
          <button class="btn-sm" style="background:#1e3a5f;color:#7dd3fc" onclick="navigateTo('storage')">📦 Storage (ver guardados)</button>
        </div>
        <div id="digimon-select-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/50 border border-red-900 text-red-300 text-sm"></div>
      </div>
    </div>
  `;

  try {
    const [digimons, dashboard] = await Promise.all([
      apiGet("/digimon/me"),
      apiGet("/players/me/dashboard")
    ]);

    const slotInfo = dashboard.slotInfo;
    const newDigimonId = window._routeParams?.newDigimonId || null;
    const newDigimon = newDigimonId
      ? digimons.find(d => String(d.id) === String(newDigimonId))
      : null;

    if (newDigimonId) {
      const noticeEl = document.getElementById("digimon-new-notice");
      if (noticeEl) {
        noticeEl.innerHTML = newDigimon
          ? `<div class="card-sm mb-3 border-emerald-700 bg-emerald-950/30 text-center"><p class="text-emerald-300 font-bold text-sm">${escapeHtml(newDigimon.name)} foi adicionado à sua coleção.</p><p class="text-xs text-slate-400 mt-1">Escolha entre “Tornar ativo” ou “Enviar para Storage”.</p></div>`
          : `<div class="card-sm mb-3 border-amber-700 bg-amber-950/30 text-center"><p class="text-amber-300 font-bold text-sm">O Digimon chocado não foi localizado na sua coleção.</p><p class="text-xs text-slate-400 mt-1">Atualize a página ou verifique o Storage.</p></div>`;
      }
    }

    if (slotInfo) {
      const infoEl = document.getElementById("digimon-slot-info");
      infoEl.innerHTML = `
        <div class="flex justify-center text-xs">
          <span class="text-slate-400">Storage: ${slotInfo.storedDigimons}/${slotInfo.maxStorageSlots}</span>
        </div>
      `;
    }

    renderDigimonSelectCards(digimons, dashboard, newDigimonId);
  } catch (err) {
    const errorDiv = document.getElementById("digimon-select-error");
    errorDiv.textContent = err.message;
    errorDiv.classList.remove("hidden");
  }
}

function renderDigimonSelectCards(digimons, dashboard, newDigimonId = null) {
  const container = document.getElementById("digimon-select-list");

  if (!digimons || digimons.length === 0) {
    container.innerHTML = `<p class="text-slate-400 text-sm">Nenhum Digimon encontrado.</p>`;
    return;
  }

  const activeDigimonId = dashboard && dashboard.activeDigimon ? dashboard.activeDigimon.id : null;

  container.innerHTML = digimons.map(d => {
    const isActive = d.id === activeDigimonId;
    const isHatched = d.status === "HATCHED";
    const isNew = newDigimonId && String(d.id) === String(newDigimonId);

    return `
    <div id="digimon-card-${escapeAttr(String(d.id))}" class="card flex items-center gap-4 text-left ${isActive ? 'border-cyan-700' : isHatched ? 'border-amber-700 ring-1 ring-amber-500/30' : isNew ? 'border-emerald-600 ring-1 ring-emerald-500/40' : ''}">
      ${renderDigimonVisual(d.imageUrl, d.stage, "w-14 h-14", "text-4xl")}
      <div class="flex-1 min-w-0">
        <h3 class="font-bold text-sm truncate">${escapeHtml(d.name)} ${isActive ? '<span class="text-cyan-400 text-xs">(Ativo)</span>' : isHatched ? '<span class="text-amber-400 text-xs">(Aguardando escolha)</span>' : isNew ? '<span class="text-emerald-400 text-xs">(Recém-chocado)</span>' : ''}</h3>
        <div class="flex gap-2 mt-1">
          <span class="badge badge-${d.stage ? d.stage.toLowerCase() : 'baby'}">${escapeHtml(d.stage)}</span>
          <span class="badge badge-${d.rarity ? d.rarity.toLowerCase() : 'common'}">${escapeHtml(formatRarity(d.rarity))}</span>
          <span class="badge-xs">Lv.${d.level}</span>
        </div>
        <div class="flex gap-3 mt-2 text-xs text-slate-400">
          <span class="text-green-400">HP ${d.hp}</span>
          <span class="text-red-400">ATK ${d.attack}</span>
          <span class="text-blue-400">DEF ${d.defense}</span>
        </div>
      </div>
      <div class="flex flex-col gap-1">
        ${isActive ? '<span class="text-xs text-cyan-400 font-bold text-right">Ativo</span>' : isHatched ? `<button class="btn-primary btn-sm" onclick="selectDigimon('${d.id}')">Tornar ativo</button><button class="btn-sm text-xs" style="background:#78350f;color:#fbbf24" onclick="storeDigimon('${d.id}')">Enviar para Storage</button>` : `<button class="btn-primary btn-sm" onclick="selectDigimon('${d.id}')">Tornar ativo</button>`}
      </div>
    </div>
  `;
  }).join("");

  if (newDigimonId) {
    document.getElementById(`digimon-card-${newDigimonId}`)?.scrollIntoView({ block: "center", behavior: "smooth" });
  }
}

async function selectDigimon(digimonId) {
  try {
    await apiPost("/digimon/select", { digimonId: digimonId });
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function storeDigimon(digimonId) {
  try {
    await apiPost(`/digimon/${digimonId}/store`, {});
    showToast("Digimon enviado para o Storage!");
    renderDigimonSelectPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}