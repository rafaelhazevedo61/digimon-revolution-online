let forgeEquipments = [];
let forgeSearchQuery = "";
let forgeInventory = [];

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
      <div class="grid grid-cols-2 gap-2 mb-4" role="tablist" aria-label="Funções do Ferreiro">
        <button id="forge-tab-refine" class="btn-primary py-2" role="tab" aria-selected="true" onclick="forgeSelectTab('refine')">🔨 Refinamento</button>
        <button id="forge-tab-craft" class="py-2 rounded-lg font-bold text-sm bg-slate-800 text-slate-400 hover:bg-slate-700" role="tab" aria-selected="false" onclick="forgeSelectTab('craft')">⚒️ Forja</button>
      </div>
      <div id="forge-content"><div class="card animate-pulse"><div class="h-24"></div></div></div>
    </div>
  `;

  try {
    [forgeEquipments, forgeInventory] = await Promise.all([apiGet("/equipment/inventory"), apiGet("/inventory")]);
    renderForgeEquipmentList();
  } catch (err) {
    document.getElementById("forge-content").innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

function forgeSelectTab(tab) {
  const refineTab = document.getElementById("forge-tab-refine");
  const craftTab = document.getElementById("forge-tab-craft");
  if (!refineTab || !craftTab) return;
  const refining = tab === "refine";
  refineTab.className = refining ? "btn-primary py-2" : "py-2 rounded-lg font-bold text-sm bg-slate-800 text-slate-400 hover:bg-slate-700";
  craftTab.className = refining ? "py-2 rounded-lg font-bold text-sm bg-slate-800 text-slate-400 hover:bg-slate-700" : "btn-primary py-2";
  refineTab.setAttribute("aria-selected", String(refining));
  craftTab.setAttribute("aria-selected", String(!refining));
  if (refining) renderForgeEquipmentList();
  else renderForgeCraftTab();
}

function renderForgeCraftTab() {
  const container = document.getElementById("forge-content");
  if (!container) return;
  const recipes = [
    { name: "Placa de Aprimoramento", icon: "🛡️", result: "1x Placa de Aprimoramento", materials: ["3x Fragmento de Metal", "1x Núcleo Digital", "500 Bits"] },
    { name: "Lâmina do Tamer", icon: "⚔️", result: "1x Lâmina do Tamer", materials: ["5x Fragmento de Metal", "2x Núcleo Digital", "1.500 Bits"] },
    { name: "Anel de Energia", icon: "💍", result: "1x Anel de Energia", materials: ["4x Fragmento de Metal", "3x Núcleo Digital", "2.000 Bits"] }
  ];
  container.innerHTML = `
    <div class="card border-cyan-900/60 bg-cyan-950/10 mb-3"><p class="text-xs text-cyan-300 font-semibold">Prévia do sistema</p><p class="text-xs text-slate-400 mt-1">As receitas abaixo são apenas visuais. A forja ainda não consome materiais nem Bits.</p></div>
    <div class="flex items-center justify-between mb-2 px-1"><h3 class="text-sm font-bold text-slate-300">Receitas disponíveis</h3><span class="badge text-amber-300">Em breve</span></div>
    <div class="flex flex-col gap-2">
      ${recipes.map(recipe => `<div class="card-sm flex items-start gap-3 opacity-80"><span class="text-3xl">${recipe.icon}</span><div class="flex-1 min-w-0"><p class="font-bold text-sm">${recipe.name}</p><p class="text-xs text-emerald-300 mt-1">Resultado: ${recipe.result}</p><div class="flex flex-wrap gap-1 mt-2">${recipe.materials.map(material => `<span class="badge text-[10px]">${material}</span>`).join("")}</div></div><button class="btn-sm shrink-0 opacity-60" disabled title="O craft será habilitado futuramente">Criar</button></div>`).join("")}
    </div>
  `;
}

function renderForgeEquipmentList() {
  const container = document.getElementById("forge-content");
  if (!container) return;
  container.innerHTML = `
    <div class="flex items-center gap-2 mb-3">
      <input id="forge-search" class="input flex-1" type="search" value="${escapeAttr(forgeSearchQuery)}" placeholder="Buscar equipamento por nome..." aria-label="Buscar equipamento por nome" oninput="forgeSearchQuery=this.value; renderForgeEquipmentCards()">
      <span id="forge-count" class="text-xs text-slate-500 whitespace-nowrap"></span>
    </div>
    <div id="forge-equipment-items" class="flex flex-col gap-2"></div>
  `;
  renderForgeEquipmentCards();
}

function renderForgeEquipmentCards() {
  const container = document.getElementById("forge-equipment-items");
  const count = document.getElementById("forge-count");
  if (!container || !count) return;
  const query = forgeSearchQuery.trim().toLocaleLowerCase("pt-BR");
  const filteredEquipments = forgeEquipments.filter(eq => !query || String(eq.name || "").toLocaleLowerCase("pt-BR").includes(query));
  count.textContent = `${filteredEquipments.length}/${forgeEquipments.length}`;
  if (!filteredEquipments.length) {
    const emptyMessage = forgeEquipments.length ? "Nenhum equipamento encontrado" : "Nenhum equipamento no inventário";
    const emptyHint = forgeEquipments.length ? "Tente buscar por outro nome." : "Equipamentos não equipados aparecerão aqui.";
    container.innerHTML = `<div class="card text-center py-8"><div class="text-4xl mb-3">🧰</div><p class="font-semibold text-slate-300">${emptyMessage}</p><p class="text-xs text-slate-500 mt-1">${emptyHint}</p></div>`;
    return;
  }
  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  container.innerHTML = `
      ${filteredEquipments.map(eq => {
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
          <div class="text-center"><span id="forge-success-rate" class="font-bold ${forgeSuccessRateClass(preview.successRate)}">${preview.successRate}%</span><span class="text-slate-400"> Chance</span></div>
        </div>
      </div>
      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Itens de suporte</p>
        <label class="flex items-center justify-between gap-2 text-xs ${forgeFindItemCount("REFINEMENT_SUCCESS_BOOST") > 0 ? "text-slate-300" : "text-slate-600"}">
          <span>Usar Pergaminho de Refinamento (+10 pontos percentuais)</span>
          <input id="forge-success-boost" type="checkbox" onchange="forgeUpdateRefineChance(${preview.baseSuccessRate ?? preview.successRate})" ${forgeFindItemCount("REFINEMENT_SUCCESS_BOOST") < 1 ? "disabled" : ""}>
        </label>
        <label class="flex items-center justify-between gap-2 text-xs mt-2 ${forgeFindItemCount("REFINEMENT_PROTECTION") > 0 && preview.breakChance > 0 ? "text-slate-300" : "text-slate-600"}">
          <span>Usar Cristal de Proteção (${forgeFindItemCount("REFINEMENT_PROTECTION")} disponível(is))</span>
          <input id="forge-protection" type="checkbox" ${forgeFindItemCount("REFINEMENT_PROTECTION") < 1 || preview.breakChance < 1 ? "disabled" : ""}>
        </label>
        ${preview.breakChance > 0 ? `<p class="text-[10px] text-red-300 mt-2">Atenção: esta tentativa tem ${preview.breakChance}% de chance de quebrar o equipamento.</p>` : ""}
      </div>
      <button id="forge-refine-btn" data-next-level="${nextLevel}" class="btn-primary w-full py-3 text-base font-bold ${!preview.canRefine ? "opacity-60" : ""}" onclick="forgeDoRefine('${equipmentId}', ${Boolean(preview.canRefine)})">🔨 Refinar para +${nextLevel} (<span id="forge-refine-button-rate">${preview.successRate}%</span>)</button>
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
    const result = await apiPost("/equipment/refine", {
      equipmentId,
      successBoostItemCode: document.getElementById("forge-success-boost")?.checked ? "REFINEMENT_SUCCESS_BOOST" : null,
      protectionItemCode: document.getElementById("forge-protection")?.checked ? "REFINEMENT_PROTECTION" : null
    });
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

function forgeSuccessRateClass(rate) {
  return rate >= 70 ? "text-green-400" : rate >= 40 ? "text-yellow-400" : "text-red-400";
}

function forgeUpdateRefineChance(baseRate) {
  const boost = document.getElementById("forge-success-boost");
  const rate = Math.min(100, Number(baseRate || 0) + (boost?.checked ? 10 : 0));
  const rateElement = document.getElementById("forge-success-rate");
  const buttonRate = document.getElementById("forge-refine-button-rate");
  if (rateElement) {
    rateElement.textContent = `${rate}%`;
    rateElement.className = `font-bold ${forgeSuccessRateClass(rate)}`;
  }
  if (buttonRate) buttonRate.textContent = `${rate}%`;
}

function forgeFindItemCount(code) {
  return forgeInventory.filter(item => item.quantity > 0 && (item.itemDefinition?.code === code || item.itemType === code))
    .reduce((sum, item) => sum + Number(item.quantity || 0), 0);
}
