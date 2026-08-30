let forgeEquipments = [];

async function renderForgePage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `
    <div class="page-container pb-24">
      <div class="flex items-center gap-3 mb-4 px-1">
        <button class="text-xs text-cyan-400" onclick="navigateTo('more')">← Voltar</button>
        <div>
          <h2 class="text-xl font-bold">Ferreiro</h2>
          <p class="text-xs text-slate-400 mt-1">Aprimore seus equipamentos e prepare-se para novos desafios.</p>
        </div>
      </div>
      <div class="card mb-4 border-amber-900/60 bg-gradient-to-br from-amber-950/40 to-slate-900">
        <div class="flex items-center gap-3">
          <span class="text-4xl">🔨</span>
          <div>
            <p class="font-bold text-amber-200">Oficina de equipamentos</p>
            <p class="text-xs text-slate-400 mt-1">Selecione um equipamento para consultar o próximo refinamento.</p>
          </div>
        </div>
      </div>
      <div id="forge-content"><div class="card animate-pulse"><div class="h-24"></div></div></div>
    </div>
  `;

  try {
    forgeEquipments = await apiGet("/equipment/inventory");
    renderForgeEquipmentList();
  } catch (err) {
    document.getElementById("forge-content").innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

function renderForgeEquipmentList() {
  const container = document.getElementById("forge-content");
  if (!container) return;
  if (!forgeEquipments.length) {
    container.innerHTML = `<div class="card text-center py-8"><div class="text-4xl mb-3">🧰</div><p class="font-semibold text-slate-300">Nenhum equipamento no inventário</p><p class="text-xs text-slate-500 mt-1">Equipamentos não equipados aparecerão aqui.</p></div>`;
    return;
  }

  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  container.innerHTML = `
    <div class="flex items-center justify-between mb-2 px-1">
      <h3 class="text-sm font-bold text-slate-300">Equipamentos disponíveis</h3>
      <span class="text-xs text-slate-500">${forgeEquipments.length} item(ns)</span>
    </div>
    <div class="flex flex-col gap-2">
      ${forgeEquipments.map(eq => {
        const rarity = String(eq.rarity || "COMMON").toLowerCase();
        const ref = Number(eq.refinementLevel || 0);
        return `
          <div class="card-sm flex items-center gap-3 border-${rarity === "legendary" ? "yellow" : rarity === "epic" ? "purple" : rarity === "rare" ? "blue" : "slate"}-500/60">
            <span class="text-2xl">${slotEmoji[eq.slot] || "⚙️"}</span>
            <div class="flex-1 min-w-0">
              <p class="font-bold text-sm truncate">${escapeHtml(eq.name)}${ref > 0 ? ` +${ref}` : ""}</p>
              <p class="text-xs text-slate-500 mt-0.5">${escapeHtml(eq.slot || "Equipamento")} · ${escapeHtml(eq.rarity || "COMMON")} · T${eq.tier || "?"}</p>
            </div>
            <button class="btn-sm shrink-0" style="background:#4a2800;color:#f59e0b" onclick="invShowRefine('${eq.id}')">🔨 Refinar</button>
          </div>
        `;
      }).join("")}
    </div>
  `;
}

async function forgeReload() {
  forgeEquipments = await apiGet("/equipment/inventory");
  renderForgeEquipmentList();
}

window.forgeReload = forgeReload;
window.renderForgePage = renderForgePage;
window.forgeEquipments = forgeEquipments;
// Crafting will be added here in a future iteration without changing the refinement flow.
