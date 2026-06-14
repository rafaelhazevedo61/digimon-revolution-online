let shopData = null;
let shopPlayerBits = 0;
let shopModalUnitPrice = 0;

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

      <!-- Category tabs -->
      <div class="flex gap-2 mb-4 overflow-x-auto pb-1" id="shop-tabs">
        <button class="tab-btn active" data-cat="potions" onclick="shopSwitchTab('potions')">Poções</button>
        <button class="tab-btn" data-cat="materials" onclick="shopSwitchTab('materials')">Materiais</button>
        <button class="tab-btn" data-cat="equipments" onclick="shopSwitchTab('equipments')">Equip.</button>
        <button class="tab-btn" data-cat="fragments" onclick="shopSwitchTab('fragments')">Fragmentos</button>
        <button class="tab-btn" data-cat="consumables" onclick="shopSwitchTab('consumables')">Consumíveis</button>
      </div>

      <div id="shop-list">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  try {
    shopData = await apiGet("/shop");
    const dashboard = await apiGet("/players/me/dashboard");
    shopPlayerBits = dashboard.activeDigimon ? dashboard.activeDigimon.bits : 0;
    document.getElementById("shop-bits").textContent = shopPlayerBits;
    shopSwitchTab("potions");
  } catch (err) {
    document.getElementById("shop-list").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function shopSwitchTab(cat) {
  document.querySelectorAll("#shop-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.cat === cat);
  });

  const items = shopData[cat] || [];
  const container = document.getElementById("shop-list");

  if (items.length === 0) {
    container.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum produto nesta categoria.</p>`;
    return;
  }

  container.innerHTML = items.map(p => `
    <div class="card-sm mb-2 flex items-center gap-3">
      <div class="text-2xl">${shopItemEmoji(p)}</div>
      <div class="flex-1 min-w-0">
        <p class="font-bold text-sm truncate">${escapeHtml(p.name)}</p>
        <p class="text-xs text-slate-400 truncate">${escapeHtml(p.description || "")}</p>
        <div class="flex gap-2 mt-1">
          <span class="text-xs text-yellow-400 font-bold">${p.price} Bits</span>
          ${p.sellPrice > 0 ? `<span class="text-xs text-slate-500">Venda: ${p.sellPrice}</span>` : ""}
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
    DIGITAMA_STARTER: "🥚", DIGITAMA_FIRE: "🔥", DIGITAMA_WATER: "💧", DIGITAMA_NATURE: "🌿",
    INCUBATOR_COMMON: "📦", INCUBATOR_RARE: "📦", INCUBATOR_EPIC: "📦",
    FRAGMENT_ROOKIE: "🧩", FRAGMENT_CHAMPION: "🧩", FRAGMENT_ULTIMATE: "🧩", FRAGMENT_MEGA: "🧩",
    EVOLUTION_MATERIAL: "⭐"
  };
  return map[p.itemType] || "📦";
}

function shopOpenBuy(code) {
  const allItems = [
    ...(shopData.potions || []),
    ...(shopData.materials || []),
    ...(shopData.equipments || []),
    ...(shopData.fragments || []),
    ...(shopData.consumables || [])
  ];
  const product = allItems.find(p => p.code === code);
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
        <span class="text-yellow-400 font-bold">${product.price} Bits</span>
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
        <span class="text-yellow-400 font-bold" id="shop-total">${product.price} Bits</span>
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
  shopQtyUpdate();
}

function shopQtyUpdate() {
  const input = document.getElementById("shop-qty");
  const totalEl = document.getElementById("shop-total");
  if (!input || !totalEl) return;
  const qty = parseInt(input.value) || 1;
  totalEl.textContent = `${qty * shopModalUnitPrice} Bits`;
}

async function shopConfirmBuy(code, unitPrice) {
  const qtyInput = document.getElementById("shop-qty");
  const qty = qtyInput ? parseInt(qtyInput.value) || 1 : 1;
  const btn = document.getElementById("shop-buy-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Comprando..."; }

  try {
    const result = await apiPost("/shop/buy", { productCode: code, quantity: qty });
    shopPlayerBits = result.remainingBits;
    document.getElementById("shop-bits").textContent = shopPlayerBits;
    shopCloseModal();
    showToast(`${escapeHtml(result.name)} x${result.quantity} comprado! -${result.totalPrice} Bits`);
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "Confirmar"; }
  }
}

function shopCloseModal() {
  const modal = document.getElementById("shop-modal");
  if (modal) modal.remove();
}
