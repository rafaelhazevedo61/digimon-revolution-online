let invItems = [];
let invEquipments = [];
let invDigimonId = null;
let invTab = "items"; // "items" or "equipment"

async function renderInventoryPage() {
  const app = document.getElementById("app");
  showBottomNav("inventory");

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
      invEquipments = await apiGet(`/equipment/digimon/${invDigimonId}/inventory`);
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
  const items = invItems.filter(i => i.quantity > 0);

  if (items.length === 0) {
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum item no inventário.</p>`;
    return;
  }

  content.innerHTML = items.map(item => {
    const name = invItemName(item.itemType);
    const emoji = invItemEmoji(item.itemType);
    const usable = invIsUsable(item.itemType);

    return `
      <div class="card-sm mb-2 flex items-center gap-3">
        <div class="text-2xl">${emoji}</div>
        <div class="flex-1 min-w-0">
          <p class="font-bold text-sm truncate">${escapeHtml(name)}</p>
          <div class="flex gap-2 mt-1">
            <span class="text-xs text-slate-400">Qtd: ${item.quantity}</span>
            <span class="badge badge-${invItemCategory(item.itemType)}">${invItemCategoryName(item.itemType)}</span>
          </div>
        </div>
        ${usable ? `
          <button class="btn-sm btn-primary" onclick="invUseItem('${escapeHtml(item.itemType)}')">Usar</button>
        ` : ""}
      </div>
    `;
  }).join("");
}

async function invUseItem(itemType) {
  try {
    await apiPost("/inventory/use", { itemType: itemType });
    showToast(`${invItemName(itemType)} usado!`);
    // Reload inventory
    const inventory = await apiGet("/inventory");
    invItems = inventory || [];
    invRenderItems();
  } catch (err) {
    showToast(err.message, "error");
  }
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
    FRAGMENT_ROOKIE: "Fragmento Rookie",
    FRAGMENT_CHAMPION: "Fragmento Champion",
    FRAGMENT_ULTIMATE: "Fragmento Ultimate",
    FRAGMENT_MEGA: "Fragmento Mega",
    EVOLUTION_MATERIAL: "Material de Evolução"
  };
  return map[itemType] || itemType;
}

function invItemEmoji(itemType) {
  const map = {
    POTION_SMALL: "🧪", TRAINING_STONE: "💎", DATA_CORE: "🔮",
    DIGITAMA_STARTER: "🥚", DIGITAMA_FIRE: "🔥", DIGITAMA_WATER: "💧", DIGITAMA_NATURE: "🌿",
    INCUBATOR_COMMON: "📦", INCUBATOR_RARE: "📦", INCUBATOR_EPIC: "📦",
    FRAGMENT_ROOKIE: "🧩", FRAGMENT_CHAMPION: "🧩", FRAGMENT_ULTIMATE: "🧩", FRAGMENT_MEGA: "🧩",
    EVOLUTION_MATERIAL: "⭐"
  };
  return map[itemType] || "📦";
}

function invIsUsable(itemType) {
  const usable = ["POTION_SMALL", "TRAINING_STONE", "DATA_CORE"];
  return usable.includes(itemType);
}

function invItemCategory(itemType) {
  if (itemType === "POTION_SMALL") return "common";
  if (itemType.startsWith("DIGITAMA_")) return "rare";
  if (itemType.startsWith("INCUBATOR_")) return "epic";
  if (itemType.startsWith("FRAGMENT_")) return "champion";
  if (itemType === "EVOLUTION_MATERIAL") return "legendary";
  return "common";
}

function invItemCategoryName(itemType) {
  if (itemType === "POTION_SMALL") return "Poção";
  if (itemType === "TRAINING_STONE" || itemType === "DATA_CORE") return "Material";
  if (itemType.startsWith("DIGITAMA_")) return "Digitama";
  if (itemType.startsWith("INCUBATOR_")) return "Incubadora";
  if (itemType.startsWith("FRAGMENT_")) return "Fragmento";
  if (itemType === "EVOLUTION_MATERIAL") return "Evolução";
  return "Item";
}

// ==================== EQUIPMENT TAB ====================

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

    const stats = [];
    if (eq.bonusHp > 0) stats.push(`<span class="text-red-400">HP+${eq.bonusHp}</span>`);
    if (eq.bonusAttack > 0) stats.push(`<span class="text-orange-400">ATK+${eq.bonusAttack}</span>`);
    if (eq.bonusDefense > 0) stats.push(`<span class="text-blue-400">DEF+${eq.bonusDefense}</span>`);

    return `
      <div class="card-sm mb-2 ${eq.equipped ? 'border-cyan-800' : ''}">
        <div class="flex items-center gap-3">
          <div class="text-2xl">${emoji}</div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <p class="font-bold text-sm truncate">${escapeHtml(eq.name)}</p>
              ${eq.equipped ? '<span class="badge badge-success">Equipado</span>' : ''}
            </div>
            <div class="flex gap-2 mt-1 flex-wrap">
              <span class="badge badge-${eq.rarity ? eq.rarity.toLowerCase() : 'common'}">${escapeHtml(eq.rarity)}</span>
              <span class="text-xs text-slate-500">${slotName[eq.slot] || eq.slot}</span>
            </div>
            ${stats.length > 0 ? `<div class="flex gap-2 mt-1 text-xs font-bold">${stats.join(" ")}</div>` : ""}
          </div>
          <div>
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
    invEquipments = await apiGet(`/equipment/digimon/${invDigimonId}/inventory`);
    invRenderEquipment();
  } catch (err) {
    showToast(err.message, "error");
  }
}
