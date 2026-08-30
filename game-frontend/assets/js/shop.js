let shopData = null;
let shopPlayerBits = 0;
let shopModalUnitPrice = 0;
let shopMode = "buy"; // "buy" or "sell"
let shopInventoryItems = [];
let shopInventoryEquipments = [];

const SHOP_RARITY_LABELS = {
  COMMON: "Comum",
  RARE: "Rara",
  EPIC: "Épica",
  LEGENDARY: "Lendária"
};

async function renderShopPage() {
  const app = document.getElementById("app");
  showBottomNav("shop");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4 px-1">
        <h2 class="text-lg font-bold">Loja</h2>
        <div class="flex items-center gap-2">
          <span class="text-yellow-400 font-bold" id="shop-bits">--</span>
          <span class="text-xs text-slate-400">Bits</span>
        </div>
      </div>

      <!-- Buy/Sell toggle -->
      <div class="flex gap-2 mb-4" id="shop-mode-tabs">
        <button class="tab-btn active" data-mode="buy" onclick="shopSetMode('buy')">Comprar</button>
        <button class="tab-btn" data-mode="sell" onclick="shopSetMode('sell')">Vender</button>
      </div>

      <div id="shop-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  try {
    shopData = await apiGet("/shop");
    const dashboard = await apiGet("/players/me/dashboard");
    shopPlayerBits = dashboard.activeDigimon ? dashboard.activeDigimon.bits : 0;
    document.getElementById("shop-bits").textContent = shopFormatNumber(shopPlayerBits);
    shopMode = "buy";
    shopRenderBuyMode();
  } catch (err) {
    document.getElementById("shop-content").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function shopSetMode(mode) {
  shopMode = mode;
  document.querySelectorAll("#shop-mode-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.mode === mode);
  });
  if (mode === "buy") {
    shopRenderBuyMode();
  } else {
    shopRenderSellMode();
  }
}

// ==================== BUY MODE ====================

function shopRenderBuyMode() {
  const content = document.getElementById("shop-content");
  content.innerHTML = `
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-2 mb-4" id="shop-tabs">
      <button class="tab-btn w-full" data-cat="ALL" onclick="shopSwitchTab('ALL')">Todos</button>
      <button class="tab-btn w-full" data-cat="CONSUMABLE" onclick="shopSwitchTab('CONSUMABLE')">Consumíveis</button>
      <button class="tab-btn w-full" data-cat="MATERIAL" onclick="shopSwitchTab('MATERIAL')">Materiais</button>
      <button class="tab-btn w-full" data-cat="EVOLUTION_MATERIAL" onclick="shopSwitchTab('EVOLUTION_MATERIAL')">Evolução</button>
      <button class="tab-btn w-full" data-cat="FRAGMENT" onclick="shopSwitchTab('FRAGMENT')">Fragmentos</button>
      <button class="tab-btn w-full" data-cat="DIGITAMA" onclick="shopSwitchTab('DIGITAMA')">Digitamas</button>
      <button class="tab-btn w-full" data-cat="INCUBATOR" onclick="shopSwitchTab('INCUBATOR')">Incubadoras</button>
      <button class="tab-btn w-full" data-cat="CHEST" onclick="shopSwitchTab('CHEST')">Baús</button>
      <button class="tab-btn w-full" data-cat="OTHER" onclick="shopSwitchTab('OTHER')">Outros</button>
      <button class="tab-btn w-full" data-cat="EQUIPMENT" onclick="shopSwitchTab('EQUIPMENT')">Equipamentos</button>
    </div>
    <div id="shop-list"></div>
  `;
  shopSwitchTab("ALL");
}
function shopAllProducts() {
  const products = [
    ...(shopData.potions || []),
    ...(shopData.materials || []),
    ...(shopData.fragments || []),
    ...(shopData.consumables || []),
    ...(shopData.equipments || []),
    ...(shopData.chests || [])
  ];
  return [...new Map(products.map(product => [product.code, product])).values()];
}
function shopProductsForCategory(category) {
  const products = shopAllProducts();
  if (category === "ALL") return products;
  return products.filter(product => {
    const itemType = String(product.itemType || "").toUpperCase();
    const productCategory = String(product.category || "").toUpperCase();
    if (category === "CONSUMABLE") return productCategory === "POTION" || productCategory === "CONSUMABLE";
    if (category === "MATERIAL") return productCategory === "MATERIAL" && itemType !== "EVOLUTION_MATERIAL";
    if (category === "EVOLUTION_MATERIAL") return itemType === "EVOLUTION_MATERIAL";
    if (category === "FRAGMENT") return productCategory === "FRAGMENT" || itemType.startsWith("FRAGMENT_");
    if (category === "DIGITAMA") return itemType.startsWith("DIGITAMA_");
    if (category === "INCUBATOR") return itemType.startsWith("INCUBATOR_");
    if (category === "CHEST") return productCategory === "CHEST" || itemType === "LOOT_CHEST";
    if (category === "EQUIPMENT") return productCategory === "EQUIPMENT" || product.productType === "EQUIPMENT";
    if (category === "OTHER") return !["CONSUMABLE", "MATERIAL", "EVOLUTION_MATERIAL", "FRAGMENT", "DIGITAMA", "INCUBATOR", "CHEST", "EQUIPMENT"].some(group => shopProductsForCategory(group).some(item => item.code === product.code));
    return false;
  });
}
function shopSwitchTab(cat) {
  document.querySelectorAll("#shop-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.cat === cat);
  });
  const items = shopProductsForCategory(cat);
  const container = document.getElementById("shop-list");

  if (items.length === 0) {
    container.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum produto nesta categoria.</p>`;
    return;
  }

  container.innerHTML = items.map(p => `
    <div class="card-sm mb-2 flex items-center gap-3">
      <div class="text-2xl">${shopItemEmoji(p)}</div>
      <div class="flex-1 min-w-0">
        <p class="font-bold text-sm break-words">${escapeHtml(p.name)}</p>
        <p class="text-xs text-slate-400 break-words">${escapeHtml(p.description || "")}</p>
        <div class="flex gap-2 mt-1">
          <span class="text-xs text-yellow-400 font-bold">${shopFormatBits(p.price)}</span>
          ${p.sellPrice > 0 ? `<span class="text-xs text-slate-500">Venda: ${shopFormatBits(p.sellPrice)}</span>` : ""}
        </div>
      </div>
      <button class="btn-sm btn-primary" onclick="shopOpenBuy('${escapeHtml(p.code)}')">Comprar</button>
    </div>
  `).join("");
}

function shopItemEmoji(p) {
  if (p.productType === "EQUIPMENT") return "⚔️";
  const map = {
    POTION_SMALL: "🧪", TRAINING_STONE: "💎", DATA_CORE: "🔮",
    DIGITAMA_STARTER: "⭐", DIGITAMA_FIRE: "🔥", DIGITAMA_WATER: "💧", DIGITAMA_NATURE: "🌿",
    DIGITAMA_EARTH: "🌍", DIGITAMA_WIND: "🌪️", DIGITAMA_LIGHT: "✨", DIGITAMA_DARK: "🌑",
    DIGITAMA_THUNDER: "⚡", DIGITAMA_NEUTRAL: "⚪", DIGITAMA_ICE: "❄️", DIGITAMA_STEEL: "⚙️",
    INCUBATOR_COMMON: "📦", INCUBATOR_RARE: "📦", INCUBATOR_EPIC: "📦", INCUBATOR_LEGENDARY: "🌟",
    FRAGMENT_ROOKIE: "🧩", FRAGMENT_CHAMPION: "🧩", FRAGMENT_ULTIMATE: "🧩", FRAGMENT_MEGA: "🧩",
    EVOLUTION_MATERIAL: "⭐", LOOT_CHEST: renderChestIcon("w-10 h-10")
  };
  return map[p.itemType] || "📦";
}

function shopOpenBuy(code) {
  const product = shopAllProducts().find(p => p.code === code);
  if (!product) return;

  const isEquip = product.productType === "EQUIPMENT";
  const maxQty = isEquip ? 1 : Math.floor(shopPlayerBits / product.price) || 1;
  shopModalUnitPrice = product.price;

  const overlay = document.createElement("div");
  overlay.className = "shop-modal-overlay";
  overlay.id = "shop-modal";
  overlay.innerHTML = `
    <div class="shop-modal">
      <h3 class="font-bold text-lg mb-1">${escapeHtml(product.name)}</h3>
      <p class="text-xs text-slate-400 mb-3">${escapeHtml(product.description || "")}</p>
      <div class="flex justify-between text-sm mb-4">
        <span class="text-slate-400">Preço unitário</span>
        <span class="text-yellow-400 font-bold">${shopFormatBits(product.price)}</span>
      </div>
      ${!isEquip ? `
      <div class="mb-4">
        <label class="label">Quantidade</label>
        <div class="flex items-center gap-2">
          <button class="btn-sm btn-primary" onclick="shopQtyChange(-1)">−</button>
          <input type="number" id="shop-qty" class="input text-center" value="1" min="1" max="${maxQty}" style="width:4rem" oninput="shopQtyUpdate()">
          <button class="btn-sm btn-primary" onclick="shopQtyChange(1)">+</button>
        </div>
      </div>
      ` : ""}
      <div class="flex justify-between text-sm mb-4">
        <span class="text-slate-400">Total</span>
        <span class="text-yellow-400 font-bold" id="shop-total">${shopFormatBits(product.price)}</span>
      </div>
      <div class="flex gap-2">
        <button class="btn-primary flex-1" onclick="shopConfirmBuy('${escapeHtml(code)}', ${product.price})" id="shop-buy-btn">Confirmar</button>
        <button class="btn-sm flex-1" style="background:#334155;color:#94a3b8" onclick="shopCloseModal()">Cancelar</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);
  overlay.addEventListener("click", e => { if (e.target === overlay) shopCloseModal(); });
}

function shopQtyChange(delta) {
  const input = document.getElementById("shop-qty");
  if (!input) return;
  const max = parseInt(input.max) || 1;
  let val = parseInt(input.value) || 1;
  val = Math.max(1, Math.min(max, val + delta));
  input.value = val;
  if (shopMode === "sell") {
    shopSellQtyUpdate(shopModalUnitPrice);
  } else {
    shopQtyUpdate();
  }
}

function shopQtyUpdate() {
  const input = document.getElementById("shop-qty");
  const totalEl = document.getElementById("shop-total");
  if (!input || !totalEl) return;
  const qty = parseInt(input.value) || 1;
  totalEl.textContent = shopFormatBits(qty * shopModalUnitPrice);
}

async function shopConfirmBuy(code, unitPrice) {
  const qtyInput = document.getElementById("shop-qty");
  const qty = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
  const btn = document.getElementById("shop-buy-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Comprando..."; }

  try {
    const result = await apiPost("/shop/buy", { productCode: code, quantity: qty });
    shopPlayerBits = result.remainingBits;
    document.getElementById("shop-bits").textContent = shopFormatNumber(shopPlayerBits);
    shopCloseModal();
    showToast(`${escapeHtml(result.name)} x${result.quantity} comprado! -${shopFormatBits(result.totalPrice)}`);
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "Confirmar"; }
  }
}

function shopCloseModal() {
  const modal = document.getElementById("shop-modal");
  if (modal) modal.remove();
}

// ==================== SELL MODE ====================

async function shopRenderSellMode() {
  const content = document.getElementById("shop-content");
  content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;

  try {
    const [inventory, dashboard] = await Promise.all([
      apiGet("/inventory"),
      apiGet("/players/me/dashboard")
    ]);

    shopInventoryItems = inventory || [];
    shopInventoryEquipments = await apiGet(`/equipment/inventory`) || [];

    shopRenderSellList();
  } catch (err) {
    content.innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function shopSellItemEmoji(category) {
  const map = {
    CONSUMABLE: "🧪", MATERIAL: "🔮", FRAGMENT: "🧩",
    EVOLUTION_MATERIAL: "⭐", DIGITAMA: "🥚", INCUBATOR: "📦", CHEST: renderChestIcon("w-10 h-10")
  };
  return map[category] || "📦";
}

function shopRenderSellList() {
  const content = document.getElementById("shop-content");

  const sellableItems = shopInventoryItems
    .filter(item => item.quantity > 0 && item.itemDefinition && item.itemDefinition.sellable && item.itemDefinition.sellPrice > 0);

  // For equipment sell, still use shop catalog to find sell price
  const allShopProducts = [
    ...(shopData.potions || []),
    ...(shopData.materials || []),
    ...(shopData.equipments || []),
    ...(shopData.consumables || []),
    ...(shopData.chests || [])
  ];

  const sellableEquipments = shopInventoryEquipments
    .filter(eq => !eq.equipped)
    .map(eq => {
      const shopProduct = allShopProducts.find(p => p.equipmentTemplateName && p.equipmentTemplateName.toLowerCase() === eq.name.toLowerCase());
      return { ...eq, shopProduct };
    })
    .filter(eq => eq.shopProduct && eq.shopProduct.sellPrice > 0);

  if (sellableItems.length === 0 && sellableEquipments.length === 0) {
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum item vendível no inventário.</p>`;
    return;
  }

  let html = "";

  if (sellableItems.length > 0) {
    html += `<h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Itens</h3>`;
    html += sellableItems.map(item => {
      const def = item.itemDefinition;
      return `
      <div class="card-sm mb-2 flex items-center gap-3">
        <div class="text-2xl">${shopSellItemEmoji(def.category)}</div>
        <div class="flex-1 min-w-0">
          <p class="font-bold text-sm truncate">${escapeHtml(def.name)}</p>
          <div class="flex gap-2 mt-1">
            <span class="text-xs text-slate-400">Qtd: ${item.quantity}</span>
            <span class="text-xs text-green-400 font-bold">+${shopFormatBits(def.sellPrice)}/un.</span>
          </div>
        </div>
        <button class="btn-sm" style="background:#166534;color:#86efac" onclick="shopOpenSell('${escapeHtml(def.code)}', ${item.quantity}, ${def.sellPrice})">Vender</button>
      </div>
    `;}).join("");
  }

  if (sellableEquipments.length > 0) {
    html += `<h3 class="text-sm font-bold text-slate-300 mb-2 mt-4 px-1">Equipamentos</h3>`;
    html += sellableEquipments.map(eq => `
      <div class="card-sm mb-2 flex items-center gap-3">
        <div class="text-2xl">⚔️</div>
        <div class="flex-1 min-w-0">
          <p class="font-bold text-sm truncate">${escapeHtml(eq.name)}</p>
          <div class="flex gap-2 mt-1">
            <span class="badge badge-${eq.rarity ? eq.rarity.toLowerCase() : 'common'}">${escapeHtml(shopRarityLabel(eq.rarity))}</span>
            <span class="text-xs text-green-400 font-bold">+${shopFormatBits(eq.shopProduct.sellPrice)}</span>
          </div>
        </div>
        <button class="btn-sm" style="background:#166534;color:#86efac" onclick="shopConfirmSellEquipment('${eq.id}')">Vender</button>
      </div>
    `).join("");
  }

  content.innerHTML = html;
}

function shopOpenSell(itemDefCode, maxQty, sellPrice) {
  shopModalUnitPrice = sellPrice;

  const overlay = document.createElement("div");
  overlay.className = "shop-modal-overlay";
  overlay.id = "shop-modal";
  overlay.innerHTML = `
    <div class="shop-modal">
      <h3 class="font-bold text-lg mb-3">Vender Item</h3>
      <div class="flex justify-between text-sm mb-4">
        <span class="text-slate-400">Preço unitário</span>
        <span class="text-green-400 font-bold">+${shopFormatBits(sellPrice)}</span>
      </div>
      <div class="mb-4">
        <label class="label">Quantidade (máx: ${maxQty})</label>
        <div class="flex items-center gap-2">
          <button class="btn-sm btn-primary" onclick="shopQtyChange(-1)">−</button>
          <input type="number" id="shop-qty" class="input text-center" value="1" min="1" max="${maxQty}" style="width:4rem" oninput="shopSellQtyUpdate(${sellPrice})">
          <button class="btn-sm btn-primary" onclick="shopQtyChange(1)">+</button>
        </div>
      </div>
      <div class="flex justify-between text-sm mb-4">
        <span class="text-slate-400">Total</span>
        <span class="text-green-400 font-bold" id="shop-total">+${shopFormatBits(sellPrice)}</span>
      </div>
      <div class="flex gap-2">
        <button class="flex-1 btn-sm" style="background:#166534;color:#86efac;padding:0.6rem" onclick="shopConfirmSellItem('${escapeHtml(itemDefCode)}')" id="shop-sell-btn">Confirmar Venda</button>
        <button class="btn-sm flex-1" style="background:#334155;color:#94a3b8;padding:0.6rem" onclick="shopCloseModal()">Cancelar</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);
  overlay.addEventListener("click", e => { if (e.target === overlay) shopCloseModal(); });
}

function shopSellQtyUpdate(sellPrice) {
  const input = document.getElementById("shop-qty");
  const totalEl = document.getElementById("shop-total");
  if (!input || !totalEl) return;
  let qty = parseInt(input.value) || 0;
  qty = Math.max(1, qty);
  input.value = qty;
  totalEl.textContent = `+${shopFormatBits(qty * sellPrice)}`;
}

async function shopConfirmSellItem(itemDefCode) {
  const qtyInput = document.getElementById("shop-qty");
  const qty = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
  const btn = document.getElementById("shop-sell-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Vendendo..."; }

  try {
    const result = await apiPost("/shop/sell", { itemType: itemDefCode, quantity: qty });
    shopPlayerBits = result.remainingBits;
    document.getElementById("shop-bits").textContent = shopFormatNumber(shopPlayerBits);
    shopCloseModal();
    showToast(`${escapeHtml(result.name)} x${result.quantity} vendido! +${shopFormatBits(result.totalSellPrice)}`);
    shopRenderSellMode();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "Confirmar Venda"; }
  }
}

function shopRarityLabel(value) {
  const normalized = String(value || "COMMON").toUpperCase();
  return SHOP_RARITY_LABELS[normalized] || value || "Comum";
}

function shopFormatNumber(value) {
  return Number(value || 0).toLocaleString("pt-BR");
}

function shopFormatBits(value) {
  return `${shopFormatNumber(value)} Bits`;
}

async function shopConfirmSellEquipment(equipmentId) {
  try {
    const result = await apiPost("/shop/sell", { equipmentId: equipmentId, quantity: 1 });
    shopPlayerBits = result.remainingBits;
    document.getElementById("shop-bits").textContent = shopFormatNumber(shopPlayerBits);
    showToast(`${escapeHtml(result.name)} vendido! +${shopFormatBits(result.totalSellPrice)}`);
    shopRenderSellMode();
  } catch (err) {
    showToast(err.message, "error");
  }
}
