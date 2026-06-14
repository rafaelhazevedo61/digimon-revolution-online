async function renderStarterPage() {
  const app = document.getElementById("app");
  const nav = document.getElementById("bottom-nav");
  if (nav) nav.classList.add("hidden");

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
            <div class="digimon-sprite">🐉</div>
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
          <button class="btn-primary w-full mt-6" onclick="hatchConfirm('${digimon.id}')">
            Começar Jornada!
          </button>
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
  try {
    await apiPost("/digimon/select", { digimonId: digimonId });
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
  }
}
