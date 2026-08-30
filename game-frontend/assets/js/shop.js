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
    <div class="page-container shop-page">
      <header class="progression-page-header shop-page-header">
        <div>
          <p class="progression-eyebrow progression-eyebrow-cyan">Economia do servidor</p>
          <h2 class="progression-page-title">Loja</h2>
          <p class="progression-page-subtitle">Compre recursos, venda equipamentos e mantenha seu Digimon pronto para a próxima missão.</p>
        </div>
        <div class="shop-balance-card" aria-label="Saldo atual de Bits">
          <span class="shop-balance-label">Saldo atual</span>
          <strong class="shop-balance-value" id="shop-bits">--</strong>
          <span class="shop-balance-currency">Bits</span>
        </div>
      </header>

      <section class="progression-hero progression-hero-cyan shop-hero mb-4">
        <div class="progression-hero-topline">
          <span class="progression-hero-kicker">Central de comércio</span>
          <span class="progression-hero-status">Mercado ativo</span>
        </div>
        <div class="flex items-center gap-3">
          <div class="progression-hero-visual shop-hero-visual" aria-hidden="true">🛒</div>
          <div class="min-w-0">
            <h3 class="progression-panel-title">Recursos para sua jornada</h3>
            <p class="shop-hero-copy">Use seus Bits com estratégia ou transforme equipamentos parados em novas oportunidades.</p>
          </div>
        </div>
      </section>

      <div class="shop-mode-switch" id="shop-mode-tabs" role="tablist" aria-label="Modo da loja">
        <button class="shop-mode-button active" data-mode="buy" role="tab" aria-selected="true" onclick="shopSetMode('buy')"><span aria-hidden="true">＋</span> Comprar</button>
        <button class="shop-mode-button" data-mode="sell" role="tab" aria-selected="false" onclick="shopSetMode('sell')"><span aria-hidden="true">↗</span> Vender</button>
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
  document.querySelectorAll("#shop-mode-tabs [data-mode]").forEach(btn => {
    const active = btn.dataset.mode === mode;
    btn.classList.toggle("active", active);
    btn.setAttribute("aria-selected", String(active));
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
    <section class="shop-section">
      <div class="shop-section-heading">
        <div>
          <p class="progression-eyebrow progression-eyebrow-cyan">Catálogo</p>
          <h3 class="shop-section-title">Comprar recursos</h3>
        </div>
        <span class="shop-section-note">Escolha uma categoria</span>
      </div>
      <nav class="shop-category-nav" id="shop-tabs" aria-label="Categorias da loja">
        <button class="shop-category-button" data-cat="ALL" onclick="shopSwitchTab('ALL')">Todos</button>
        <button class="shop-category-button" data-cat="CONSUMABLE" onclick="shopSwitchTab('CONSUMABLE')">Consumíveis</button>
        <button class="shop-category-button" data-cat="MATERIAL" onclick="shopSwitchTab('MATERIAL')">Materiais</button>
        <button class="shop-category-button" data-cat="EVOLUTION_MATERIAL" onclick="shopSwitchTab('EVOLUTION_MATERIAL')">Evolução</button>
        <button class="shop-category-button" data-cat="FRAGMENT" onclick="shopSwitchTab('FRAGMENT')">Fragmentos</button>
        <button class="shop-category-button" data-cat="DIGITAMA" onclick="shopSwitchTab('DIGITAMA')">Digitamas</button>
        <button class="shop-category-button" data-cat="INCUBATOR" onclick="shopSwitchTab('INCUBATOR')">Incubadoras</button>
        <button class="shop-category-button" data-cat="CHEST" onclick="shopSwitchTab('CHEST')">Baús</button>
        <button class="shop-category-button" data-cat="OTHER" onclick="shopSwitchTab('OTHER')">Outros</button>
        <button class="shop-category-button" data-cat="EQUIPMENT" onclick="shopSwitchTab('EQUIPMENT')">Equipamentos</button>
      </nav>
      <div class="shop-list-heading">
        <p class="shop-list-title" id="shop-list-title">Todos os produtos</p>
        <span class="shop-list-count" id="shop-list-count"></span>
      </div>
      <div id="shop-list"></div>
    </section>
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
  document.querySelectorAll("#shop-tabs [data-cat]").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.cat === cat);
    btn.setAttribute("aria-current", btn.dataset.cat === cat ? "page" : "false");
  });
  const items = shopProductsForCategory(cat);
  const container = document.getElementById("shop-list");
  const listTitle = document.getElementById("shop-list-title");
  const listCount = document.getElementById("shop-list-count");
  const categoryName = document.querySelector(`#shop-tabs [data-cat="${cat}"]`)?.textContent || "Produtos";
  if (listTitle) listTitle.textContent = categoryName;
  if (listCount) listCount.textContent = `${items.length} ${items.length === 1 ? "produto" : "produtos"}`;

  if (items.length === 0) {
    container.innerHTML = `
      <div class="shop-empty-state">
        <span class="shop-empty-icon" aria-hidden="true">⌁</span>
        <p>Nenhum produto nesta categoria.</p>
        <span>Volte ao catálogo para explorar outras opções.</span>
      </div>
    `;
    return;
  }

  container.innerHTML = `<div class="shop-product-list">${items.map(p => `
    <article class="shop-product-card">
      <div class="shop-product-icon" aria-hidden="true">${shopItemEmoji(p)}</div>
      <div class="shop-product-body">
        <div class="shop-product-heading">
          <p class="shop-product-name">${escapeHtml(p.name)}</p>
          ${p.productType === "EQUIPMENT" ? `<span class="shop-product-type">Equipamento</span>` : ""}
        </div>
        <p class="shop-product-description">${escapeHtml(p.description || "")}</p>
        <div class="shop-product-meta">
          <span class="shop-product-price">${shopFormatBits(p.price)}</span>
          ${p.sellPrice > 0 ? `<span class="shop-product-resale">Venda: ${shopFormatBits(p.sellPrice)}</span>` : ""}
        </div>
      </div>
      <button class="shop-action-button shop-action-buy" onclick="shopOpenBuy('${escapeHtml(p.code)}')">Comprar</button>
    </article>
  `).join("")}</div>`;
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
      <div class="shop-modal-heading">
        <div>
          <p class="shop-modal-kicker shop-modal-kicker-buy">Comprar recurso</p>
          <h3 class="shop-modal-title">${escapeHtml(product.name)}</h3>
        </div>
        <span class="shop-modal-mark shop-modal-mark-buy" aria-hidden="true">＋</span>
      </div>
      <p class="shop-modal-description">${escapeHtml(product.description || "")}</p>
      <div class="shop-modal-summary">
        <div class="shop-modal-row">
          <span>Preço unitário</span>
          <strong class="shop-modal-value-buy">${shopFormatBits(product.price)}</strong>
        </div>
        ${!isEquip ? `
        <div class="shop-modal-quantity">
          <label class="label" for="shop-qty">Quantidade</label>
          <div class="flex items-center gap-2">
            <button class="btn-sm btn-primary" onclick="shopQtyChange(-1)" aria-label="Diminuir quantidade">−</button>
            <input type="number" id="shop-qty" class="input text-center" value="1" min="1" max="${maxQty}" style="width:4rem" oninput="shopQtyUpdate()">
            <button class="btn-sm btn-primary" onclick="shopQtyChange(1)" aria-label="Aumentar quantidade">+</button>
          </div>
        </div>
        ` : ""}
        <div class="shop-modal-total shop-modal-total-buy">
          <span>Total</span>
          <strong id="shop-total">${shopFormatBits(product.price)}</strong>
        </div>
      </div>
      <div class="shop-modal-actions">
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
    content.innerHTML = `
      <section class="shop-section shop-sell-section">
        <div class="shop-empty-state">
          <span class="shop-empty-icon" aria-hidden="true">↗</span>
          <p>Nenhum item vendível no inventário.</p>
          <span>Equipamentos equipados não podem ser vendidos.</span>
        </div>
      </section>
    `;
    return;
  }

  let html = `
    <section class="shop-section shop-sell-section">
      <div class="shop-section-heading">
        <div>
          <p class="progression-eyebrow progression-eyebrow-amber">Inventário</p>
          <h3 class="shop-section-title">Vender itens</h3>
        </div>
        <span class="shop-section-note">Converta excessos em Bits</span>
      </div>
  `;

  if (sellableItems.length > 0) {
    html += `<div class="shop-subsection-heading"><span>Itens do inventário</span><strong>${sellableItems.length}</strong></div>`;
    html += `<div class="shop-product-list">${sellableItems.map(item => {
      const def = item.itemDefinition;
      return `
      <article class="shop-product-card shop-product-card-sell">
        <div class="shop-product-icon" aria-hidden="true">${shopSellItemEmoji(def.category)}</div>
        <div class="shop-product-body">
          <div class="shop-product-heading">
            <p class="shop-product-name">${escapeHtml(def.name)}</p>
            <span class="shop-product-type">${item.quantity} disponíveis</span>
          </div>
          <div class="shop-product-meta">
            <span class="shop-product-resale">+${shopFormatBits(def.sellPrice)} / un.</span>
          </div>
        </div>
        <button class="shop-action-button shop-action-sell" onclick="shopOpenSell('${escapeHtml(def.code)}', ${item.quantity}, ${def.sellPrice})">Vender</button>
      </article>
    `;}).join("")}</div>`;
  }

  if (sellableEquipments.length > 0) {
    html += `<div class="shop-subsection-heading ${sellableItems.length > 0 ? "shop-subsection-heading-spaced" : ""}"><span>Equipamentos disponíveis</span><strong>${sellableEquipments.length}</strong></div>`;
    html += `<div class="shop-product-list">${sellableEquipments.map(eq => `
      <article class="shop-product-card shop-product-card-sell">
        <div class="shop-product-icon shop-equipment-icon" aria-hidden="true">⚔️</div>
        <div class="shop-product-body">
          <div class="shop-product-heading">
            <p class="shop-product-name">${escapeHtml(eq.name)}</p>
            <span class="shop-product-type">${escapeHtml(shopRarityLabel(eq.rarity))}</span>
          </div>
          <div class="shop-product-meta">
            <span class="shop-product-resale">+${shopFormatBits(eq.shopProduct.sellPrice)}</span>
          </div>
        </div>
        <button class="shop-action-button shop-action-sell" onclick="shopConfirmSellEquipment('${eq.id}')">Vender</button>
      </article>
    `).join("")}</div>`;
  }

  html += `</section>`;
  content.innerHTML = html;
}

function shopOpenSell(itemDefCode, maxQty, sellPrice) {
  shopModalUnitPrice = sellPrice;

  const overlay = document.createElement("div");
  overlay.className = "shop-modal-overlay";
  overlay.id = "shop-modal";
  overlay.innerHTML = `
    <div class="shop-modal">
      <div class="shop-modal-heading">
        <div>
          <p class="shop-modal-kicker shop-modal-kicker-sell">Vender recurso</p>
          <h3 class="shop-modal-title">Vender Item</h3>
        </div>
        <span class="shop-modal-mark shop-modal-mark-sell" aria-hidden="true">↗</span>
      </div>
      <div class="shop-modal-summary shop-modal-summary-sell">
        <div class="shop-modal-row">
          <span>Preço unitário</span>
          <strong class="shop-modal-value-sell">+${shopFormatBits(sellPrice)}</strong>
        </div>
        <div class="shop-modal-quantity">
          <label class="label" for="shop-qty">Quantidade (máx: ${maxQty})</label>
          <div class="flex items-center gap-2">
            <button class="btn-sm btn-primary" onclick="shopQtyChange(-1)" aria-label="Diminuir quantidade">−</button>
            <input type="number" id="shop-qty" class="input text-center" value="1" min="1" max="${maxQty}" style="width:4rem" oninput="shopSellQtyUpdate(${sellPrice})">
            <button class="btn-sm btn-primary" onclick="shopQtyChange(1)" aria-label="Aumentar quantidade">+</button>
          </div>
        </div>
        <div class="shop-modal-total shop-modal-total-sell">
          <span>Total</span>
          <strong id="shop-total">+${shopFormatBits(sellPrice)}</strong>
        </div>
      </div>
      <div class="shop-modal-actions">
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
