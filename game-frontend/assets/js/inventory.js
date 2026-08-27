let invItems = [];
let invEquipments = [];
let invDigimonId = null;
let invTab = "items"; // "items" or "equipment"
let invChestOpeningInProgress = false;

async function renderInventoryPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4 px-1">
        <h2 class="text-lg font-bold">Inventário</h2>
      </div>

      <!-- Tabs -->
      <div class="flex gap-2 mb-4" id="inv-tabs">
        <button class="tab-btn active" data-tab="items" onclick="invSwitchTab('items')">Itens</button>
        <button class="tab-btn" data-tab="equipment" onclick="invSwitchTab('equipment')">Equipamentos</button>
      </div>

      <div id="inv-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  try {
    const [inventory, dashboard] = await Promise.all([
      apiGet("/inventory"),
      apiGet("/players/me/dashboard")
    ]);

    invItems = inventory || [];
    invDigimonId = dashboard.activeDigimon ? dashboard.activeDigimon.id : null;

    if (invDigimonId) {
      invEquipments = await apiGet(`/equipment/digimon/${invDigimonId}/inventory`) || [];
    } else {
      invEquipments = [];
    }

    invTab = "items";
    invRenderItems();
  } catch (err) {
    document.getElementById("inv-content").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function invSwitchTab(tab) {
  invTab = tab;
  document.querySelectorAll("#inv-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.tab === tab);
  });
  if (tab === "items") {
    invRenderItems();
  } else {
    invRenderEquipment();
  }
}

// ==================== ITEMS TAB ====================

function invRenderItems() {
  const content = document.getElementById("inv-content");
  const items = invSortItems(invAggregateItems(invItems).filter(i => i.quantity > 0));

  if (items.length === 0) {
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum item no inventário.</p>`;
    return;
  }

  content.innerHTML = items.map(item => {
    const def = item.itemDefinition;
    const name = def ? def.name : invItemName(item.itemType);
    const emoji = def ? invCategoryEmoji(def.category) : invItemEmoji(item.itemType);
    const catName = def ? invCategoryLabel(def.category) : invItemCategoryName(item.itemType);
    const category = def ? String(def.category || "").toUpperCase() : "";
    const catBadge = def ? invCategoryBadge(category) : invItemCategory(item.itemType);
    const chestCode = category === "CHEST" ? def.code : null;
    const isChest = item.itemType === "LOOT_CHEST" || !!chestCode;
    const chestQuantityInputId = chestCode ? `inv-chest-quantity-${String(chestCode).replace(/[^a-zA-Z0-9_-]/g, "-")}` : null;
    const incubationOnly = category === "DIGITAMA" || category === "INCUBATOR"
      || item.itemType.startsWith("DIGITAMA_") || item.itemType.startsWith("INCUBATOR_");
    const usable = !incubationOnly && (def ? def.usable : invIsUsable(item.itemType));
    const action = isChest && chestCode ? `
      <div class="flex items-center gap-2">
        <input id="${chestQuantityInputId}" class="input w-16 text-center" type="number" min="1" max="${Math.max(1, Number(item.quantity) || 1)}" value="1" aria-label="Quantidade de baús" />
        <button class="btn-sm btn-primary whitespace-nowrap" onclick="invOpenChest('${escapeHtml(chestCode)}', document.getElementById('${chestQuantityInputId}').value)">Abrir</button>
      </div>
    ` : usable ? `
      <button class="btn-sm btn-primary" onclick="invUseItem('${escapeHtml(item.itemType)}')">Usar</button>
    ` : "";

    return `
      <div class="card-sm mb-2 flex items-center gap-3">
        <div class="text-2xl">${emoji}</div>
        <div class="flex-1 min-w-0">
          <p class="font-bold text-sm truncate">${escapeHtml(name)}</p>
          <div class="flex gap-2 mt-1">
            <span class="text-xs text-slate-400">Qtd: ${item.quantity}</span>
            <span class="badge badge-${catBadge}">${escapeHtml(catName)}</span>
          </div>
        </div>
        ${action}
      </div>
    `;
  }).join("");
}

function invAggregateItems(items) {
  const grouped = new Map();

  for (const item of items || []) {
    const definitionCode = item.itemDefinition && item.itemDefinition.code;
    const key = definitionCode || item.itemType;
    const existing = grouped.get(key);

    if (!existing) {
      grouped.set(key, { ...item });
      continue;
    }

    existing.quantity += item.quantity || 0;
    if (!existing.itemDefinition && item.itemDefinition) {
      existing.itemDefinition = item.itemDefinition;
    }
  }

  return Array.from(grouped.values());
}

function invItemCategoryOrder(item) {
  const category = item.itemDefinition
    ? String(item.itemDefinition.category || "").toUpperCase()
    : String(item.itemType || "").startsWith("DIGITAMA_")
      ? "DIGITAMA"
      : String(item.itemType || "").startsWith("INCUBATOR_")
        ? "INCUBATOR"
        : String(item.itemType || "") === "LOOT_CHEST"
          ? "CHEST"
          : String(item.itemType || "") === "EVOLUTION_MATERIAL"
            ? "EVOLUTION_MATERIAL"
            : String(item.itemType || "").startsWith("FRAGMENT_")
              ? "FRAGMENT"
              : String(item.itemType || "") === "POTION_SMALL"
              ? "CONSUMABLE"
              : String(item.itemType || "") === "TRAINING_STONE" || String(item.itemType || "") === "DATA_CORE"
                || String(item.itemType || "") === "REFINEMENT_STONE"
                ? "MATERIAL"
                : "OTHER";

  const order = {
    CONSUMABLE: 10,
    MATERIAL: 20,
    EVOLUTION_MATERIAL: 30,
    FRAGMENT: 40,
    DIGITAMA: 50,
    INCUBATOR: 60,
    CHEST: 70,
    OTHER: 99
  };
  return order[category] || order.OTHER;
}

function invSortItems(items) {
  return [...items].sort((a, b) => {
    const categoryDifference = invItemCategoryOrder(a) - invItemCategoryOrder(b);
    if (categoryDifference !== 0) return categoryDifference;

    const aDefinition = a.itemDefinition || {};
    const bDefinition = b.itemDefinition || {};
    const aName = aDefinition.name || invItemName(a.itemType);
    const bName = bDefinition.name || invItemName(b.itemType);
    const nameDifference = aName.localeCompare(bName, "pt-BR", { sensitivity: "base" });
    if (nameDifference !== 0) return nameDifference;

    const aCode = aDefinition.code || a.itemType || "";
    const bCode = bDefinition.code || b.itemType || "";
    return aCode.localeCompare(bCode, "pt-BR", { sensitivity: "base" });
  });
}

function invCategoryEmoji(category) {
  const map = {
    CONSUMABLE: "🧪", MATERIAL: "🔮", FRAGMENT: "🧩",
    EVOLUTION_MATERIAL: "⭐", DIGITAMA: "🥚", INCUBATOR: "📦", CHEST: "🎁"
  };
  return map[category] || "📦";
}

function invCategoryLabel(category) {
  const map = {
    CONSUMABLE: "Consumível", MATERIAL: "Material", FRAGMENT: "Fragmento",
    EVOLUTION_MATERIAL: "Evolução", DIGITAMA: "Digitama", INCUBATOR: "Incubadora", CHEST: "Baú"
  };
  return map[category] || "Item";
}

function invCategoryBadge(category) {
  const map = {
    CONSUMABLE: "common", MATERIAL: "common", FRAGMENT: "champion",
    EVOLUTION_MATERIAL: "legendary", DIGITAMA: "rare", INCUBATOR: "epic", CHEST: "rare"
  };
  return map[category] || "common";
}

async function invReloadItems() {
  invItems = await apiGet("/inventory") || [];
  if (document.getElementById("inv-content")) {
    invRenderItems();
  }
}

async function invUseItem(itemType) {
  try {
    await apiPost("/inventory/use", { itemType: itemType });
    showToast(itemType === "INCUBATION_SLOT_UNLOCK"
      ? "Slot de incubação desbloqueado!"
      : `${invItemName(itemType)} usado!`);
    await invReloadItems();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function createChestRequestId() {
  if (window.crypto && typeof window.crypto.randomUUID === "function") {
    return window.crypto.randomUUID();
  }
  return `chest-open-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

async function invOpenChest(chestCode, quantity = 1) {
  if (!chestCode) {
    showToast("Definição do baú não encontrada.", "error");
    return null;
  }
  const requestedQuantity = Number.parseInt(quantity, 10);
  if (!Number.isInteger(requestedQuantity) || requestedQuantity < 1) {
    showToast("Informe uma quantidade válida de baús.", "error");
    return null;
  }
  if (invChestOpeningInProgress) return null;

  invChestOpeningInProgress = true;
  try {
    const result = await apiPost("/inventory/chests/open", {
      chestCode,
      quantity: requestedQuantity,
      requestId: createChestRequestId()
    });
    invShowChestOpeningResult(result);
    await invReloadItems();
    return result;
  } catch (err) {
    showToast(err.message, "error");
    return null;
  } finally {
    invChestOpeningInProgress = false;
  }
}

function invShowChestOpeningResult(result) {
  const existing = document.getElementById("chest-opening-overlay");
  if (existing) existing.remove();

  const items = Array.isArray(result && result.items) ? result.items : [];
  const chestQuantity = Math.max(1, Number(result && result.quantity) || 1);
  const title = result && result.replayed ? "Abertura já processada" : chestQuantity > 1 ? "Baús abertos!" : "Baú aberto!";
  const message = result && result.message ? result.message : "Recompensas recebidas";

  const overlay = document.createElement("div");
  overlay.id = "chest-opening-overlay";
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(2,6,23,.82);z-index:60;display:flex;align-items:center;justify-content:center;padding:1rem;";
  overlay.onclick = (event) => {
    if (event.target === overlay) overlay.remove();
  };

  overlay.innerHTML = `
    <div class="card w-full max-w-md border-cyan-800 shadow-2xl">
      <div class="text-center mb-4">
        <div class="text-5xl mb-2">🎁</div>
        <h3 class="text-xl font-bold">${escapeHtml(title)}</h3>
        <p class="text-sm text-slate-400 mt-1">${escapeHtml(result && result.chestName || "Baú")} · ${chestQuantity} ${chestQuantity === 1 ? "baú" : "baús"}</p>
        <p class="text-xs text-slate-500 mt-2">Cada item possui sua própria raridade</p>
      </div>
      <div class="card-sm mb-4">
        <p class="text-xs text-slate-400 mb-2">Recompensas</p>
        ${items.length > 0 ? items.map(item => `
          <div class="flex items-center justify-between py-2 border-b border-slate-800 last:border-0">
            <div class="min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <p class="font-semibold text-sm truncate">${escapeHtml(item.itemName || item.materialCode || invItemName(item.itemType))}</p>
                ${item.rarity ? `<span class="badge badge-${String(item.rarity).toLowerCase()}">${escapeHtml(formatRarity(item.rarity))}</span>` : ""}
              </div>
              ${item.materialCode ? `<p class="text-xs text-slate-500">Material de evolução</p>` : ""}
            </div>
            <span class="font-bold text-cyan-300 ml-3">x${item.quantity}</span>
          </div>
        `).join("") : `<p class="text-sm text-slate-400">Nenhum item foi informado.</p>`}
      </div>
      <p class="text-xs text-slate-400 mb-4">${escapeHtml(message)}</p>
      <button class="btn-primary w-full" onclick="document.getElementById('chest-opening-overlay').remove()">Continuar</button>
    </div>
  `;

  document.body.appendChild(overlay);
}


function invItemName(itemType) {
  const map = {
    POTION_SMALL: "Poção Pequena",
    TRAINING_STONE: "Pedra de Treino",
    DATA_CORE: "Núcleo de Dados",
    DIGITAMA_STARTER: "Digitama Starter",
    DIGITAMA_FIRE: "Digitama de Fogo",
    DIGITAMA_WATER: "Digitama de Água",
    DIGITAMA_NATURE: "Digitama de Natureza",
    INCUBATOR_COMMON: "Incubadora Comum",
    INCUBATOR_RARE: "Incubadora Rara",
    INCUBATOR_EPIC: "Incubadora Épica",
    INCUBATION_SLOT_UNLOCK: "Expansor de Slot de Incubação",
    FRAGMENT_ROOKIE: "Fragmento Rookie",
    FRAGMENT_CHAMPION: "Fragmento Champion",
    FRAGMENT_ULTIMATE: "Fragmento Ultimate",
    FRAGMENT_MEGA: "Fragmento Mega",
    EVOLUTION_MATERIAL: "Material de Evolução",
    LOOT_CHEST: "Baú"
  };
  return map[itemType] || itemType;
}

function invItemEmoji(itemType) {
  const map = {
    POTION_SMALL: "🧪", TRAINING_STONE: "💎", DATA_CORE: "🔮",
    DIGITAMA_STARTER: "🥚", DIGITAMA_FIRE: "🔥", DIGITAMA_WATER: "💧", DIGITAMA_NATURE: "🌿",
    INCUBATOR_COMMON: "📦", INCUBATOR_RARE: "📦", INCUBATOR_EPIC: "📦",
    INCUBATION_SLOT_UNLOCK: "🔓",
    FRAGMENT_ROOKIE: "🧩", FRAGMENT_CHAMPION: "🧩", FRAGMENT_ULTIMATE: "🧩", FRAGMENT_MEGA: "🧩",
    EVOLUTION_MATERIAL: "⭐",
    LOOT_CHEST: "🎁"
  };
  return map[itemType] || "📦";
}

function invIsUsable(itemType) {
  const usable = ["POTION_SMALL", "TRAINING_STONE", "DATA_CORE", "INCUBATION_SLOT_UNLOCK"];
  return usable.includes(itemType);
}

function invItemCategory(itemType) {
  if (itemType === "POTION_SMALL" || itemType === "INCUBATION_SLOT_UNLOCK") return "common";
  if (itemType.startsWith("DIGITAMA_")) return "rare";
  if (itemType.startsWith("INCUBATOR_")) return "epic";
  if (itemType.startsWith("FRAGMENT_")) return "champion";
  if (itemType === "EVOLUTION_MATERIAL") return "legendary";
  if (itemType === "LOOT_CHEST") return "rare";
  return "common";
}

function invItemCategoryName(itemType) {
  if (itemType === "POTION_SMALL") return "Poção";
  if (itemType === "INCUBATION_SLOT_UNLOCK") return "Incubação";
  if (itemType === "TRAINING_STONE" || itemType === "DATA_CORE") return "Material";
  if (itemType.startsWith("DIGITAMA_")) return "Digitama";
  if (itemType.startsWith("INCUBATOR_")) return "Incubadora";
  if (itemType.startsWith("FRAGMENT_")) return "Fragmento";
  if (itemType === "EVOLUTION_MATERIAL") return "Evolução";
  if (itemType === "LOOT_CHEST") return "Baú";
  return "Item";
}

// ==================== EQUIPMENT TAB ====================

function invSetLabel(setCode) {
  const map = {
    BERSERKER: "Berserker", GUARDIAN: "Guardião",
    VITALITY: "Vitalidade", BALANCED: "Equilibrado"
  };
  return map[setCode] || setCode;
}

function invSetBadge(setCode) {
  const map = {
    BERSERKER: "legendary", GUARDIAN: "rare",
    VITALITY: "epic", BALANCED: "champion"
  };
  return map[setCode] || "common";
}

function invRenderEquipment() {
  const content = document.getElementById("inv-content");

  if (invEquipments.length === 0) {
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum equipamento no inventário.</p>`;
    return;
  }

  // Show equipped first, then unequipped
  const sorted = [...invEquipments].sort((a, b) => {
    if (a.equipped && !b.equipped) return -1;
    if (!a.equipped && b.equipped) return 1;
    const slotOrder = { WEAPON: 0, ARMOR: 1, ACCESSORY: 2 };
    return (slotOrder[a.slot] || 0) - (slotOrder[b.slot] || 0);
  });

  content.innerHTML = sorted.map(eq => {
    const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
    const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
    const emoji = slotEmoji[eq.slot] || "⚔️";
    const refLabel = eq.refinementLevel > 0 ? ` +${eq.refinementLevel}` : "";

    const stats = [];
    if (eq.effectiveBonusHp > 0) stats.push(`<span class="text-red-400">HP+${eq.effectiveBonusHp}</span>`);
    if (eq.effectiveBonusAttack > 0) stats.push(`<span class="text-orange-400">ATK+${eq.effectiveBonusAttack}</span>`);
    if (eq.effectiveBonusDefense > 0) stats.push(`<span class="text-blue-400">DEF+${eq.effectiveBonusDefense}</span>`);

    return `
      <div class="card-sm mb-2 ${eq.equipped ? 'border-cyan-800' : ''}">
        <div class="flex items-center gap-3">
          <div class="text-2xl">${emoji}</div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <p class="font-bold text-sm truncate">${escapeHtml(eq.name)}${refLabel}</p>
              ${eq.equipped ? '<span class="badge badge-success">Equipado</span>' : ''}
            </div>
            <div class="flex gap-2 mt-1 flex-wrap">
              ${eq.setCode ? `<span class="badge badge-${invSetBadge(eq.setCode)}">${escapeHtml(invSetLabel(eq.setCode))}</span>` : ''}
              <span class="badge badge-${eq.rarity ? eq.rarity.toLowerCase() : 'common'}">T${eq.tier || '?'}</span>
              <span class="text-xs text-slate-500">${slotName[eq.slot] || eq.slot}</span>
            </div>
            ${stats.length > 0 ? `<div class="flex gap-2 mt-1 text-xs font-bold">${stats.join(" ")}</div>` : ""}
          </div>
          <div class="flex flex-col gap-1">
            ${eq.refinementLevel < 10 ? `
              <button class="btn-sm" style="background:#4a2800;color:#f59e0b" onclick="invShowRefine('${eq.id}')">Refinar</button>
            ` : ''}
            ${eq.equipped ? `
              <button class="btn-sm" style="background:#7f1d1d;color:#fca5a5" onclick="invUnequip('${eq.id}')">Desequipar</button>
            ` : `
              <button class="btn-sm btn-primary" onclick="invEquip('${eq.id}')">Equipar</button>
            `}
          </div>
        </div>
      </div>
    `;
  }).join("");
}

async function invEquip(equipmentId) {
  try {
    await apiPost("/equipment/equip", { equipmentId: equipmentId });
    showToast("Equipamento equipado!");
    await invReloadEquipment();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function invUnequip(equipmentId) {
  try {
    await apiPost("/equipment/unequip", { equipmentId: equipmentId });
    showToast("Equipamento removido!");
    await invReloadEquipment();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function invReloadEquipment() {
  if (!invDigimonId) return;
  try {
    invEquipments = await apiGet(`/equipment/digimon/${invDigimonId}/inventory`) || [];
    invRenderEquipment();
  } catch (err) {
    showToast(err.message, "error");
  }
}

// ==================== REFINE MODAL ====================

async function invShowRefine(equipmentId) {
  const eq = invEquipments.find(e => e.id === equipmentId);
  if (!eq) return;

  let preview = null;
  try {
    preview = await apiGet(`/equipment/${equipmentId}/refine-preview`);
  } catch (err) {
    showToast(err.message, "error");
    return;
  }

  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  const emoji = slotEmoji[eq.slot] || "⚔️";
  const refLabel = eq.refinementLevel > 0 ? ` +${eq.refinementLevel}` : "";

  const overlay = document.createElement("div");
  overlay.id = "refine-overlay";
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(0,0,0,0.7);z-index:50;display:flex;align-items:flex-end;justify-content:center;";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  const nextLevel = preview.nextRefinementLevel;
  const nextHp = eq.bonusHp > 0 ? eq.effectiveBonusHp + 2 : 0;
  const nextAtk = eq.bonusAttack > 0 ? eq.effectiveBonusAttack + 2 : 0;
  const nextDef = eq.bonusDefense > 0 ? eq.effectiveBonusDefense + 2 : 0;

  overlay.innerHTML = `
    <div class="card" style="max-width:420px;width:100%;max-height:85vh;overflow-y:auto;border-radius:1rem 1rem 0 0;margin:0 auto;">
      <div class="text-center mb-3">
        <div class="text-3xl mb-1">${emoji}</div>
        <h3 class="text-lg font-bold">${escapeHtml(eq.name)}${refLabel}</h3>
        ${eq.setCode ? `<span class="badge badge-${invSetBadge(eq.setCode)}">${escapeHtml(invSetLabel(eq.setCode))}</span>` : ''}
        <span class="badge badge-${eq.rarity ? eq.rarity.toLowerCase() : 'common'}">T${eq.tier || '?'}</span>
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Refinamento: +${preview.currentRefinementLevel} → +${nextLevel}</p>
        <div class="grid grid-cols-3 gap-2 text-center text-sm">
          ${eq.bonusHp > 0 ? `<div><span class="text-slate-400">HP</span><br><span class="text-red-400 font-bold">${eq.effectiveBonusHp} → ${nextHp}</span></div>` : ''}
          ${eq.bonusAttack > 0 ? `<div><span class="text-slate-400">ATK</span><br><span class="text-orange-400 font-bold">${eq.effectiveBonusAttack} → ${nextAtk}</span></div>` : ''}
          ${eq.bonusDefense > 0 ? `<div><span class="text-slate-400">DEF</span><br><span class="text-blue-400 font-bold">${eq.effectiveBonusDefense} → ${nextDef}</span></div>` : ''}
        </div>
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Custo e Chance</p>
        <div class="flex justify-around text-sm">
          <div class="text-center">
            <span class="text-yellow-400 font-bold">${preview.costBits}</span>
            <span class="text-slate-400"> Bits</span>
            <br><span class="text-xs ${preview.currentBits >= preview.costBits ? 'text-green-400' : 'text-red-400'}">(tem: ${preview.currentBits})</span>
          </div>
          <div class="text-center">
            <span class="text-purple-400 font-bold">${preview.costStones}</span>
            <span class="text-slate-400"> Pedra</span>
            <br><span class="text-xs ${preview.currentStones >= preview.costStones ? 'text-green-400' : 'text-red-400'}">(tem: ${preview.currentStones})</span>
          </div>
          <div class="text-center">
            <span class="font-bold ${preview.successRate >= 70 ? 'text-green-400' : preview.successRate >= 40 ? 'text-yellow-400' : 'text-red-400'}">${preview.successRate}%</span>
            <span class="text-slate-400"> Chance</span>
          </div>
        </div>
      </div>

      <button id="refine-btn" class="btn-primary w-full py-3 text-base font-bold"
        ${!preview.canRefine ? 'disabled style="opacity:0.5;cursor:not-allowed"' : ''}
        onclick="invDoRefine('${equipmentId}')">
        🔨 Refinar para +${nextLevel} (${preview.successRate}%)
      </button>
      ${!preview.canRefine ? `<p class="text-red-400 text-xs text-center mt-2">Recursos insuficientes</p>` : ''}
    </div>
  `;

  document.body.appendChild(overlay);
}

async function invDoRefine(equipmentId) {
  const btn = document.getElementById("refine-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Refinando..."; }

  try {
    const result = await apiPost("/equipment/refine", { equipmentId: equipmentId });

    if (result.success) {
      showToast(result.message || "Refinamento bem-sucedido!");
    } else {
      showToast(result.message || "Refinamento falhou!", "error");
    }

    const overlay = document.getElementById("refine-overlay");
    if (overlay) overlay.remove();

    await invReloadEquipment();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "🔨 Refinar"; }
  }
}
