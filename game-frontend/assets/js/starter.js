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
        ${entries.length > 0 ? `<p class="text-xs text-slate-500 mt-2">${entries.map(e => e.digimonName).join(", ")}</p>` : ""}
      </button>
    `;
  }).join("");
}

async function starterSelect(type) {
  const errorDiv = document.getElementById("starter-error");
  errorDiv.classList.add("hidden");

  try {
    await apiPost("/digitama/select", { type: type });
    navigateTo("dashboard");
  } catch (err) {
    errorDiv.textContent = err.message;
    errorDiv.classList.remove("hidden");
  }
}
