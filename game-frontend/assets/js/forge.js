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
            <button class="btn-sm shrink-0" style="background:#4a2800;color:#f59e0b" onclick="forgeShowRefine('${eq.id}')">🔨 Refinar</button>
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

async function forgeShowRefine(equipmentId) {
  const eq = forgeEquipments.find(e => e.id === equipmentId);
  if (!eq) return;

  let preview;
  try {
    preview = await apiGet(`/equipment/${equipmentId}/refine-preview`);
  } catch (err) {
    showToast(err.message, "error");
    return;
  }

  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  const emoji = slotEmoji[eq.slot] || "⚙️";
  const refLabel = Number(eq.refinementLevel) > 0 ? ` +${eq.refinementLevel}` : "";
  const nextLevel = preview.nextRefinementLevel;
  const nextHp = eq.bonusHp > 0 ? Number(eq.effectiveBonusHp || 0) + 2 : 0;
  const nextAtk = eq.bonusAttack > 0 ? Number(eq.effectiveBonusAttack || 0) + 2 : 0;
  const nextDef = eq.bonusDefense > 0 ? Number(eq.effectiveBonusDefense || 0) + 2 : 0;
  const overlay = document.createElement("div");
  overlay.id = "forge-refine-overlay";
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(0,0,0,0.7);z-index:50;display:flex;align-items:flex-end;justify-content:center;";
  overlay.onclick = event => { if (event.target === overlay) overlay.remove(); };
  overlay.innerHTML = `
    <div class="card" style="max-width:420px;width:100%;max-height:85vh;overflow-y:auto;border-radius:1rem 1rem 0 0;margin:0 auto;">
      <div class="text-center mb-3">
        <div class="text-3xl mb-1">${emoji}</div>
        <h3 class="text-lg font-bold">${escapeHtml(eq.name)}${refLabel}</h3>
        <span class="badge badge-${forgeRarityBadge(eq.rarity)}">T${eq.tier || "?"}</span>
      </div>
      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Refinamento: +${preview.currentRefinementLevel} → +${nextLevel}</p>
        <div class="grid grid-cols-3 gap-2 text-center text-sm">
          ${eq.bonusHp > 0 ? `<div><span class="text-slate-400">HP</span><br><span class="text-red-400 font-bold">${eq.effectiveBonusHp} → ${nextHp}</span></div>` : ""}
          ${eq.bonusAttack > 0 ? `<div><span class="text-slate-400">ATK</span><br><span class="text-orange-400 font-bold">${eq.effectiveBonusAttack} → ${nextAtk}</span></div>` : ""}
          ${eq.bonusDefense > 0 ? `<div><span class="text-slate-400">DEF</span><br><span class="text-blue-400 font-bold">${eq.effectiveBonusDefense} → ${nextDef}</span></div>` : ""}
        </div>
      </div>
      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Custo e chance</p>
        <div class="flex justify-around text-sm">
          <div class="text-center"><span class="text-yellow-400 font-bold">${Number(preview.costBits).toLocaleString("pt-BR")}</span><span class="text-slate-400"> Bits</span><br><span class="text-xs ${preview.currentBits >= preview.costBits ? "text-green-400" : "text-red-400"}">(tem: ${Number(preview.currentBits).toLocaleString("pt-BR")})</span></div>
          <div class="text-center"><span class="text-purple-400 font-bold">${preview.costStones}</span><span class="text-slate-400"> Pedra</span><br><span class="text-xs ${preview.currentStones >= preview.costStones ? "text-green-400" : "text-red-400"}">(tem: ${preview.currentStones})</span></div>
          <div class="text-center"><span class="font-bold ${preview.successRate >= 70 ? "text-green-400" : preview.successRate >= 40 ? "text-yellow-400" : "text-red-400"}">${preview.successRate}%</span><span class="text-slate-400"> Chance</span></div>
        </div>
      </div>
      <button id="forge-refine-btn" class="btn-primary w-full py-3 text-base font-bold ${!preview.canRefine ? "opacity-60" : ""}" onclick="forgeDoRefine('${equipmentId}', ${Boolean(preview.canRefine)})">🔨 Refinar para +${nextLevel} (${preview.successRate}%)</button>
      ${!preview.canRefine ? `<p class="text-red-400 text-xs text-center mt-2">Recursos insuficientes</p>` : ""}
    </div>
  `;
  document.body.appendChild(overlay);
}

async function forgeDoRefine(equipmentId, canRefine = true) {
  if (!canRefine) {
    const preview = await apiGet(`/equipment/${equipmentId}/refine-preview`).catch(() => null);
    if (preview) {
      const missing = [];
      if (preview.currentBits < preview.costBits) missing.push(`Bits: ${preview.currentBits}/${preview.costBits}`);
      if (preview.currentStones < preview.costStones) missing.push(`Pedras: ${preview.currentStones}/${preview.costStones}`);
      showToast(missing.length ? `Recursos insuficientes — ${missing.join(" · ")}` : "Este equipamento não pode ser refinado agora.", "error");
    } else {
      showToast("Não foi possível verificar os recursos para o refinamento.", "error");
    }
    return;
  }
  const button = document.getElementById("forge-refine-btn");
  if (button) { button.disabled = true; button.textContent = "Refinando..."; }
  try {
    const result = await apiPost("/equipment/refine", { equipmentId });
    showToast(result.success ? (result.message || "Refinamento bem-sucedido!") : (result.message || "Refinamento falhou!"), result.success ? "success" : "error");
    document.getElementById("forge-refine-overlay")?.remove();
    await renderForgePage();
  } catch (err) {
    showToast(err.message, "error");
    if (button) { button.disabled = false; button.textContent = "🔨 Refinar"; }
  }
}

function forgeRarityBadge(rarity) {
  return String(rarity || "COMMON").toLowerCase();
}
