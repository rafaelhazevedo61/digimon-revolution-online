let auctionState = {
  mode: "market",
  page: 0,
  pageSize: 20,
  search: "",
  category: "",
  rarity: "",
  dashboard: null,
  inventory: []
};

async function renderAuctionHousePage() {
  showBottomNav("more");
  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="page-container pb-24">
      <div class="flex items-center justify-between mb-4 px-1">
        <div>
          <button class="text-xs text-cyan-400 mb-2" onclick="navigateTo('more')">← Voltar</button>
          <h2 class="text-xl font-bold">Casa de Leilões</h2>
          <p class="text-xs text-slate-400 mt-1">Compre e venda itens com outros jogadores.</p>
        </div>
        <div id="auction-bits" class="card-sm text-right text-xs text-amber-300">Carregando Bits...</div>
      </div>

      <div class="flex gap-2 mb-4 w-full">
        <button id="auction-tab-market" class="btn-primary flex-1 min-w-0 text-sm text-center whitespace-nowrap" onclick="auctionSetMode('market')">Mercado</button>
        <button id="auction-tab-mine" class="btn-secondary flex-1 min-w-0 text-sm text-center whitespace-nowrap" onclick="auctionSetMode('mine')">Meus anúncios</button>
        <button id="auction-tab-history" class="btn-secondary flex-1 min-w-0 text-sm text-center whitespace-nowrap" onclick="auctionSetMode('history')">Histórico</button>
      </div>

      <div id="auction-content" class="space-y-3">
        <div class="card text-center text-slate-400">Carregando Casa de Leilões...</div>
      </div>
    </div>
  `;

  try {
    auctionState.dashboard = await apiGet("/players/me/dashboard");
    auctionRenderBits();
    await auctionSetMode("market");
  } catch (error) {
    document.getElementById("auction-content").innerHTML =
      `<div class="card border-red-900 text-red-300">${escapeHtml(error.message)}</div>`;
  }
}

function auctionRenderBits() {
  const bits = auctionState.dashboard?.activeDigimon?.bits;
  const element = document.getElementById("auction-bits");
  if (element) element.innerHTML = `💰 ${Number(bits || 0).toLocaleString("pt-BR")} Bits`;
}

async function auctionSetMode(mode) {
  auctionState.mode = mode;
  auctionState.page = 0;
  document.querySelectorAll("[id^='auction-tab-']").forEach(button => {
    button.className = button.id === `auction-tab-${mode}`
      ? "btn-primary flex-1 min-w-0 text-sm text-center whitespace-nowrap"
      : "btn-secondary flex-1 min-w-0 text-sm text-center whitespace-nowrap";
  });

  const content = document.getElementById("auction-content");
  if (!content) return;
  content.innerHTML = `<div class="card text-center text-slate-400">Carregando...</div>`;

  try {
    if (mode === "market") await auctionRenderMarket();
    if (mode === "mine") await auctionRenderMine();
    if (mode === "history") await auctionRenderHistory();
  } catch (error) {
    content.innerHTML = `<div class="card border-red-900 text-red-300">${escapeHtml(error.message)}</div>`;
  }
}

async function auctionRenderMarket() {
  const content = document.getElementById("auction-content");
  const listings = await apiGet("/auction/listings", {
    search: auctionState.search,
    category: auctionState.category,
    rarity: auctionState.rarity,
    page: auctionState.page,
    size: auctionState.pageSize
  });

  const cards = (listings.content || []).map(auctionListingCard).join("");
  content.innerHTML = `
    <div class="card space-y-3">
      <div class="grid grid-cols-2 gap-2">
        <label class="min-w-0 col-span-2">
          <span class="sr-only">Buscar item por nome ou código</span>
          <input id="auction-search" class="input min-w-0" placeholder="Buscar por nome ou código..." value="${escapeHtml(auctionState.search)}" />
        </label>
        <select id="auction-category" class="input" aria-label="Filtrar por categoria">
          ${auctionFilterOption("", "Todas as categorias", auctionState.category)}
          ${auctionFilterOption("CONSUMABLE", "Consumíveis", auctionState.category)}
          ${auctionFilterOption("MATERIAL", "Materiais", auctionState.category)}
          ${auctionFilterOption("FRAGMENT", "Fragmentos", auctionState.category)}
          ${auctionFilterOption("EVOLUTION_MATERIAL", "Evolução", auctionState.category)}
          ${auctionFilterOption("DIGITAMA", "Digitamas", auctionState.category)}
          ${auctionFilterOption("INCUBATOR", "Incubadoras", auctionState.category)}
        </select>
        <select id="auction-rarity" class="input" aria-label="Filtrar por raridade">
          ${auctionFilterOption("", "Todas as raridades", auctionState.rarity)}
          ${auctionFilterOption("COMMON", "Comum", auctionState.rarity)}
          ${auctionFilterOption("RARE", "Rara", auctionState.rarity)}
          ${auctionFilterOption("EPIC", "Épica", auctionState.rarity)}
          ${auctionFilterOption("LEGENDARY", "Lendária", auctionState.rarity)}
        </select>
      </div>
      <div class="grid grid-cols-2 gap-2 mt-3">
        <button class="btn-primary w-full" onclick="auctionApplyFilters()">Buscar</button>
        <button class="btn-secondary w-full" onclick="auctionShowCreateForm()">+ Publicar item</button>
      </div>
      <p class="text-center text-xs text-slate-400 mt-2">${Number(listings.totalElements || 0).toLocaleString("pt-BR")} anúncio(s) encontrado(s)</p>
    </div>
    ${cards || `<div class="card text-center text-slate-400">Nenhum anúncio encontrado.</div>`}
    ${auctionPagination(listings, "auctionRenderMarket")}
  `;
}

function auctionFilterOption(value, label, selectedValue) {
  const selected = value === selectedValue ? " selected" : "";
  return `<option value="${escapeHtml(value)}"${selected}>${escapeHtml(label)}</option>`;
}

function auctionFeeRateLabel(durationHours) {
  return {
    24: "5%",
    48: "7,5%",
    72: "10%"
  }[Number(durationHours)] || "5%";
}

function auctionFeeRateLabelFromBps(feeRateBps) {
  return {
    500: "5%",
    750: "7,5%",
    1000: "10%"
  }[Number(feeRateBps)] || "5%";
}

function auctionUpdateCreateFee() {
  const duration = document.getElementById("auction-create-duration")?.value;
  const element = document.getElementById("auction-create-fee");
  if (element) element.textContent = `Comissão sobre cada venda: ${auctionFeeRateLabel(duration)}`;
}

function auctionStatusLabel(status) {
  return {
    ACTIVE: "Ativo",
    SOLD: "Vendido",
    CANCELLED: "Cancelado",
    EXPIRED: "Expirado"
  }[status] || "Indisponível";
}

function auctionRarityLabel(rarity) {
  const normalized = String(rarity || "COMMON").toUpperCase();
  return {
    COMMON: "Comum",
    RARE: "Rara",
    EPIC: "Épica",
    LEGENDARY: "Lendária"
  }[normalized] || rarity || "Comum";
}

function auctionListingCard(listing) {
  const isMine = listing.sellerPlayerId === auctionState.dashboard?.id;
  const icon = auctionIconMarkup(listing);
  const buyPayload = auctionEncodedPayload({
    id: listing.id,
    itemName: listing.itemName,
    itemCode: listing.itemCode,
    rarity: listing.rarity || "Comum",
    category: listing.category || "",
    icon: listing.icon || "",
    sellerUsername: listing.sellerUsername,
    remainingQuantity: listing.remainingQuantity,
    quantity: listing.quantity,
    unitPrice: listing.unitPrice
  });
  return `
    <div class="card min-h-[8.5rem] p-4">
      <div class="grid grid-cols-[3.5rem_minmax(0,1fr)] sm:grid-cols-[3.5rem_minmax(0,1fr)_9.5rem] gap-x-3 gap-y-3 items-center h-full">
        <div class="w-14 h-14 rounded-xl bg-slate-800 flex items-center justify-center shrink-0">${icon}</div>
        <div class="min-w-0 self-stretch flex flex-col justify-center">
          <p class="font-bold text-sm leading-tight break-words">${escapeHtml(listing.itemName)}</p>
          <p class="text-[11px] text-slate-400 truncate mt-1">${escapeHtml(auctionRarityLabel(listing.rarity))}</p>
          <div class="flex flex-wrap gap-x-2 gap-y-0.5 mt-1 text-[11px] leading-tight">
            <span class="text-slate-500">Vendedor: ${escapeHtml(listing.sellerUsername)}</span>
            <span class="text-slate-400">${listing.remainingQuantity}/${listing.quantity} disponível(is)</span>
          </div>
        </div>
        <div class="col-span-2 sm:col-span-1 flex items-center justify-between sm:flex-col sm:items-end sm:justify-center gap-3 border-t sm:border-t-0 sm:border-l border-slate-800 pt-3 sm:pt-0 sm:pl-4 self-stretch">
          <div class="text-left sm:text-right shrink-0">
            <p class="text-[10px] uppercase tracking-wide text-slate-500">Preço unitário</p>
            <p class="font-bold text-lg leading-tight text-amber-300">${Number(listing.unitPrice).toLocaleString("pt-BR")} Bits</p>
          </div>
          ${isMine
            ? `<span class="inline-flex items-center min-h-9 px-3 rounded-lg bg-cyan-950/60 text-xs font-semibold text-cyan-300">Seu anúncio</span>`
            : `<button class="btn-primary text-sm min-h-9 px-4" onclick="auctionOpenBuyModalFromPayload('${buyPayload}')">Comprar</button>`}
        </div>
      </div>
    </div>
  `;
}

function auctionIconMarkup(listing) {
  const icon = String(listing.icon || "").trim();
  const isImageUrl = icon.startsWith("http://")
    || icon.startsWith("https://")
    || icon.startsWith("/")
    || icon.startsWith("./")
    || icon.startsWith("../")
    || icon.startsWith("assets/")
    || /\\.(png|jpe?g|gif|webp|svg)(\\?.*)?$/i.test(icon);

  if (isImageUrl) {
    return `<img src="${escapeHtml(icon)}" alt="" class="w-10 h-10 object-contain" onerror="this.replaceWith(auctionIconFallbackElement('${escapeHtml(listing.category || "")}'))" />`;
  }

  const emojiByIcon = {
    potion_small: "🧪",
    training_stone: "💎",
    data_core: "🔮",
    fragment_baby2: "⭐",
    fragment_rookie: "🧩",
    fragment_rookie_specific: "🧩",
    fragment_champion: "🧩",
    fragment_champion_specific: "🧩",
    fragment_ultimate: "🧩",
    fragment_ultimate_specific: "🧩",
    fragment_mega: "🧩",
    fragment_mega_specific: "🧩",
    digitama_starter: "🥚",
    digitama_fire: "🔥",
    digitama_water: "💧",
    digitama_nature: "🌿",
    incubator_common: "📦",
    incubator_rare: "📦",
    incubator_epic: "📦"
  };
  return `<span class="text-3xl" aria-hidden="true">${emojiByIcon[icon] || auctionCategoryEmoji(listing.category)}</span>`;
}

function auctionIconFallbackElement(category) {
  const element = document.createElement("span");
  element.className = "text-3xl";
  element.setAttribute("aria-hidden", "true");
  element.textContent = auctionCategoryEmoji(category);
  return element;
}

function auctionCategoryEmoji(category) {
  return {
    CONSUMABLE: "🧪",
    MATERIAL: "🔮",
    FRAGMENT: "🧩",
    EVOLUTION_MATERIAL: "⭐",
    DIGITAMA: "🥚",
    INCUBATOR: "📦"
  }[category] || "📦";
}

async function auctionShowCreateForm() {
  const content = document.getElementById("auction-content");
  content.innerHTML = `<div class="card text-center text-slate-400">Carregando inventário...</div>`;
  try {
    auctionState.inventory = await apiGet("/inventory") || [];
    const eligible = auctionState.inventory.filter(item =>
      item.quantity > 0 && item.itemDefinition && item.itemDefinition.tradable && item.itemDefinition.stackable
    );
    content.innerHTML = `
      <div class="card space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="font-bold">Publicar item</h3>
            <p class="text-xs text-slate-400">Publicar custa 100 Bits. Limite de 10 anúncios ativos por jogador. A comissão da venda varia conforme a duração.</p>
          </div>
          <button class="text-xs text-slate-400" onclick="auctionSetMode('market')">Fechar</button>
        </div>
        ${eligible.length ? `
          <form onsubmit="auctionCreate(event)" class="space-y-3">
            <label class="block text-xs text-slate-400">Item
              <select id="auction-create-item" class="input w-full mt-1" required>
                ${eligible.map(item => `<option value="${item.itemDefinition.id}" data-quantity="${item.quantity}">${escapeHtml(item.itemDefinition.name)} — ${item.quantity} disponível(is)</option>`).join("")}
              </select>
            </label>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-2">
              <label class="block text-xs text-slate-400">Quantidade<input id="auction-create-quantity" class="input w-full mt-1" type="number" min="1" value="1" required /></label>
              <label class="block text-xs text-slate-400">Preço por unidade<input id="auction-create-price" class="input w-full mt-1" type="number" min="1" required /></label>
              <label class="block text-xs text-slate-400">Duração<select id="auction-create-duration" class="input w-full mt-1" onchange="auctionUpdateCreateFee()"><option value="24">24 horas</option><option value="48" selected>48 horas</option><option value="72">72 horas</option></select></label>
            </div>
            <p id="auction-create-fee" class="text-xs text-amber-300">Comissão sobre cada venda: 7,5%</p>
            <button class="btn-primary w-full" type="submit">Publicar anúncio</button>
          </form>
        ` : `<p class="text-sm text-slate-400">Você não possui itens negociáveis disponíveis no Digimon ativo.</p>`}
      </div>
    `;
  } catch (error) {
    content.innerHTML = `<div class="card border-red-900 text-red-300">${escapeHtml(error.message)}</div>`;
  }
}

async function auctionCreate(event) {
  event.preventDefault();
  const itemDefinitionId = Number(document.getElementById("auction-create-item").value);
  const quantity = Number(document.getElementById("auction-create-quantity").value);
  const unitPrice = Number(document.getElementById("auction-create-price").value);
  const durationHours = Number(document.getElementById("auction-create-duration").value);
  try {
    await apiPost("/auction/listings", { itemDefinitionId, quantity, unitPrice, durationHours });
    showToast("Anúncio publicado com sucesso!");
    auctionState.dashboard = await apiGet("/players/me/dashboard");
    auctionRenderBits();
    await auctionSetMode("mine");
  } catch (error) {
    showToast(error.message, "error");
  }
}

function auctionEncodedPayload(payload) {
  return encodeURIComponent(JSON.stringify(payload)).replace(/'/g, "%27");
}

function auctionOpenBuyModalFromPayload(encodedPayload) {
  try {
    auctionOpenBuyModal(JSON.parse(decodeURIComponent(encodedPayload)));
  } catch (error) {
    showToast("Não foi possível abrir a confirmação da compra.", "error");
  }
}

function auctionOpenBuyModal(listing) {
  const existing = document.getElementById("auction-buy-modal");
  if (existing) existing.remove();

  const balance = Number(auctionState.dashboard?.activeDigimon?.bits || 0);
  const overlay = document.createElement("div");
  overlay.id = "auction-buy-modal";
  overlay.className = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70";
  overlay.dataset.listingId = listing.id;
  overlay.dataset.maxQuantity = listing.remainingQuantity;
  overlay.dataset.unitPrice = listing.unitPrice;
  overlay.dataset.balance = balance;
  overlay.addEventListener("click", event => {
    if (event.target === overlay) auctionCloseBuyModal();
  });
  overlay.innerHTML = `
    <div class="bg-slate-900 border border-cyan-900 rounded-xl max-w-md w-full p-5 shadow-2xl">
      <div class="flex items-start justify-between gap-3 mb-4">
        <div>
          <p class="text-xs uppercase tracking-wide text-cyan-400">Casa de Leilões</p>
          <h3 class="font-bold text-lg text-white">Confirmar compra</h3>
        </div>
        <button class="text-slate-400 text-2xl leading-none" aria-label="Fechar" onclick="auctionCloseBuyModal()">&times;</button>
      </div>

      <div class="flex items-center gap-3 rounded-xl bg-slate-800/80 p-3 mb-4">
        <div class="w-14 h-14 rounded-xl bg-slate-700 flex items-center justify-center shrink-0">${auctionIconMarkup(listing)}</div>
        <div class="min-w-0">
          <p class="font-bold text-white break-words">${escapeHtml(listing.itemName)}</p>
          <p class="text-xs text-slate-400 truncate">${escapeHtml(auctionRarityLabel(listing.rarity))}</p>
          <p class="text-xs text-slate-500 mt-1">Vendedor: ${escapeHtml(listing.sellerUsername)}</p>
        </div>
      </div>

      <form onsubmit="auctionConfirmBuy(event)" class="space-y-4">
        <label class="block text-sm text-slate-300">
          Quantidade
          <span class="text-xs text-slate-500">(até ${Number(listing.remainingQuantity).toLocaleString("pt-BR")})</span>
          <input id="auction-buy-quantity" class="input w-full mt-1" type="number" min="1" max="${listing.remainingQuantity}" value="1" inputmode="numeric" required />
        </label>

        <div class="grid grid-cols-2 gap-2">
          <div class="rounded-lg bg-slate-800 p-3">
            <p class="text-[10px] uppercase tracking-wide text-slate-500">Total da compra</p>
            <p id="auction-buy-total" class="font-bold text-lg text-amber-300 mt-1"></p>
          </div>
          <div class="rounded-lg bg-slate-800 p-3">
            <p class="text-[10px] uppercase tracking-wide text-slate-500">Saldo após compra</p>
            <p id="auction-buy-balance" class="font-bold text-lg mt-1"></p>
          </div>
        </div>

        <p class="text-xs text-slate-500">O valor exibido é calculado pelo preço de ${Number(listing.unitPrice).toLocaleString("pt-BR")} Bits por unidade. A compra será concluída somente após a confirmação.</p>
        <div class="grid grid-cols-2 gap-2">
          <button type="button" class="btn-secondary w-full" onclick="auctionCloseBuyModal()">Cancelar</button>
          <button id="auction-buy-submit" type="submit" class="btn-primary w-full">Confirmar compra</button>
        </div>
      </form>
    </div>
  `;
  document.body.appendChild(overlay);

  const quantityInput = document.getElementById("auction-buy-quantity");
  quantityInput.addEventListener("input", auctionUpdateBuyModalSummary);
  auctionUpdateBuyModalSummary();
  quantityInput.focus();
}

function auctionUpdateBuyModalSummary() {
  const modal = document.getElementById("auction-buy-modal");
  const quantityInput = document.getElementById("auction-buy-quantity");
  if (!modal || !quantityInput) return;

  const quantity = Number(quantityInput.value);
  const maxQuantity = Number(modal.dataset.maxQuantity);
  const unitPrice = Number(modal.dataset.unitPrice);
  const balance = Number(modal.dataset.balance);
  const isValid = Number.isInteger(quantity) && quantity >= 1 && quantity <= maxQuantity;
  const total = isValid ? quantity * unitPrice : 0;
  const remainingBalance = balance - total;
  const totalElement = document.getElementById("auction-buy-total");
  const balanceElement = document.getElementById("auction-buy-balance");
  const submitButton = document.getElementById("auction-buy-submit");

  totalElement.textContent = `${total.toLocaleString("pt-BR")} Bits`;
  balanceElement.textContent = `${remainingBalance.toLocaleString("pt-BR")} Bits`;
  balanceElement.className = `font-bold text-lg mt-1 ${remainingBalance < 0 ? "text-red-300" : "text-emerald-300"}`;
  submitButton.disabled = !isValid || remainingBalance < 0;
}

function auctionCloseBuyModal() {
  document.getElementById("auction-buy-modal")?.remove();
}

async function auctionConfirmBuy(event) {
  event.preventDefault();
  const modal = document.getElementById("auction-buy-modal");
  const quantityInput = document.getElementById("auction-buy-quantity");
  const submitButton = document.getElementById("auction-buy-submit");
  if (!modal || !quantityInput || !submitButton) return;

  const quantity = Number(quantityInput.value);
  const maxQuantity = Number(modal.dataset.maxQuantity);
  const unitPrice = Number(modal.dataset.unitPrice);
  const balance = Number(modal.dataset.balance);
  const total = quantity * unitPrice;
  if (!Number.isInteger(quantity) || quantity < 1 || quantity > maxQuantity || total > balance) {
    showToast("Confira a quantidade e o saldo antes de confirmar.", "error");
    return;
  }

  submitButton.disabled = true;
  submitButton.textContent = "Comprando...";
  try {
    const result = await apiPost(`/auction/listings/${modal.dataset.listingId}/buy`, { quantity });
    auctionCloseBuyModal();
    showToast(result.message || "Compra realizada com sucesso!");
    auctionState.dashboard = await apiGet("/players/me/dashboard");
    auctionRenderBits();
    await auctionRenderMarket();
  } catch (error) {
    submitButton.disabled = false;
    submitButton.textContent = "Confirmar compra";
    showToast(error.message, "error");
  }
}

async function auctionRenderMine() {
  const data = await apiGet("/auction/my-listings", { page: auctionState.page, size: auctionState.pageSize });
  const cards = (data.content || []).map(listing => `
    <div class="card flex flex-col sm:flex-row gap-3 sm:items-center justify-between">
      <div>
        <p class="font-bold">${escapeHtml(listing.itemName)}</p>
        <p class="text-xs text-slate-400">${listing.remainingQuantity}/${listing.quantity} restante(s) · ${Number(listing.unitPrice).toLocaleString("pt-BR")} Bits/unidade</p>
        <p class="text-xs text-slate-400">${listing.durationHours || "—"}h · comissão ${auctionFeeRateLabelFromBps(listing.sellerFeeRateBps)}</p>
        <p class="text-xs text-slate-500">Status: ${escapeHtml(auctionStatusLabel(listing.status))} · expira em ${new Date(listing.expiresAt).toLocaleString("pt-BR")}</p>
      </div>
      ${listing.status === "ACTIVE" && listing.remainingQuantity > 0
        ? `<button class="btn-secondary text-sm" onclick="auctionOpenCancelModalFromPayload('${auctionEncodedPayload({
            id: listing.id,
            itemName: listing.itemName,
            itemCode: listing.itemCode,
            rarity: listing.rarity || "Comum",
            category: listing.category || "",
            icon: listing.icon || "",
            remainingQuantity: listing.remainingQuantity,
            quantity: listing.quantity,
            unitPrice: listing.unitPrice,
            durationHours: listing.durationHours,
            expiresAt: listing.expiresAt
          })}')">Cancelar e devolver</button>`
        : `<span class="text-xs text-slate-500">Sem ações disponíveis</span>`}
    </div>
  `).join("");
  document.getElementById("auction-content").innerHTML = `
    <div class="card"><h3 class="font-bold">Meus anúncios</h3><p class="text-xs text-slate-400 mt-1">Os itens publicados ficam reservados até a venda ou devolução.</p></div>
    ${cards || `<div class="card text-center text-slate-400">Você ainda não publicou anúncios.</div>`}
    ${auctionPagination(data, "auctionRenderMine")}
  `;
}

function auctionOpenCancelModalFromPayload(encodedPayload) {
  try {
    auctionOpenCancelModal(JSON.parse(decodeURIComponent(encodedPayload)));
  } catch (error) {
    showToast("Não foi possível abrir a confirmação do cancelamento.", "error");
  }
}

function auctionOpenCancelModal(listing) {
  const existing = document.getElementById("auction-cancel-modal");
  if (existing) existing.remove();

  const overlay = document.createElement("div");
  overlay.id = "auction-cancel-modal";
  overlay.className = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70";
  overlay.dataset.listingId = listing.id;
  overlay.addEventListener("click", event => {
    if (event.target === overlay) auctionCloseCancelModal();
  });
  overlay.innerHTML = `
    <div class="bg-slate-900 border border-amber-900 rounded-xl max-w-md w-full p-5 shadow-2xl">
      <div class="flex items-start justify-between gap-3 mb-4">
        <div>
          <p class="text-xs uppercase tracking-wide text-amber-400">Meus anúncios</p>
          <h3 class="font-bold text-lg text-white">Cancelar anúncio</h3>
        </div>
        <button class="text-slate-400 text-2xl leading-none" aria-label="Fechar" onclick="auctionCloseCancelModal()">&times;</button>
      </div>

      <div class="flex items-center gap-3 rounded-xl bg-slate-800/80 p-3 mb-4">
        <div class="w-14 h-14 rounded-xl bg-slate-700 flex items-center justify-center shrink-0">${auctionIconMarkup(listing)}</div>
        <div class="min-w-0">
          <p class="font-bold text-white break-words">${escapeHtml(listing.itemName)}</p>
          <p class="text-xs text-slate-400 truncate">${escapeHtml(auctionRarityLabel(listing.rarity))}</p>
          <p class="text-xs text-slate-500 mt-1">${Number(listing.unitPrice).toLocaleString("pt-BR")} Bits por unidade</p>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-2 mb-4">
        <div class="rounded-lg bg-slate-800 p-3">
          <p class="text-[10px] uppercase tracking-wide text-slate-500">Itens a devolver</p>
          <p class="font-bold text-lg text-emerald-300 mt-1">${Number(listing.remainingQuantity).toLocaleString("pt-BR")}</p>
        </div>
        <div class="rounded-lg bg-slate-800 p-3">
          <p class="text-[10px] uppercase tracking-wide text-slate-500">Taxa da publicação</p>
          <p class="font-bold text-lg text-amber-300 mt-1">100 Bits</p>
        </div>
      </div>

      <p class="text-xs text-slate-400 mb-4">As unidades restantes serão devolvidas ao Digimon que originou este anúncio. A taxa de publicação não é reembolsada.</p>
      <div class="grid grid-cols-2 gap-2">
        <button type="button" class="btn-secondary w-full" onclick="auctionCloseCancelModal()">Voltar</button>
        <button id="auction-cancel-submit" type="button" class="btn-primary w-full" onclick="auctionConfirmCancel()">Confirmar cancelamento</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);
}

function auctionCloseCancelModal() {
  document.getElementById("auction-cancel-modal")?.remove();
}

async function auctionConfirmCancel() {
  const modal = document.getElementById("auction-cancel-modal");
  const submitButton = document.getElementById("auction-cancel-submit");
  if (!modal || !submitButton) return;

  submitButton.disabled = true;
  submitButton.textContent = "Cancelando...";
  try {
    await apiPost(`/auction/listings/${modal.dataset.listingId}/cancel`, {});
    auctionCloseCancelModal();
    showToast("Anúncio cancelado e itens devolvidos.");
    await auctionRenderMine();
  } catch (error) {
    submitButton.disabled = false;
    submitButton.textContent = "Confirmar cancelamento";
    showToast(error.message, "error");
  }
}

async function auctionRenderHistory() {
  const transactions = await apiGet("/auction/history", { page: auctionState.page, size: auctionState.pageSize });
  const rows = (transactions || []).map(transaction => `
    <div class="card flex items-center justify-between gap-3">
      <div>
        <p class="font-bold">${escapeHtml(transaction.itemName)}</p>
        <p class="text-xs text-slate-400">${transaction.direction === "BUY" ? "Compra" : "Venda"} · ${transaction.quantity} unidade(s) · ${new Date(transaction.createdAt).toLocaleString("pt-BR")}</p>
      </div>
      <div class="text-right text-xs">
        <p class="${transaction.direction === "BUY" ? "text-red-300" : "text-emerald-300"}">${transaction.direction === "BUY" ? "-" : "+"}${Number(transaction.direction === "BUY" ? transaction.grossAmount : transaction.sellerNetAmount).toLocaleString("pt-BR")} Bits</p>
        <p class="text-slate-500">Taxa: ${Number(transaction.fee).toLocaleString("pt-BR")}</p>
      </div>
    </div>
  `).join("");
  document.getElementById("auction-content").innerHTML = `
    <div class="card"><h3 class="font-bold">Histórico</h3><p class="text-xs text-slate-400 mt-1">Últimas compras e vendas realizadas por você.</p></div>
    ${rows || `<div class="card text-center text-slate-400">Nenhuma transação registrada.</div>`}
  `;
}

function auctionApplyFilters() {
  auctionState.search = document.getElementById("auction-search")?.value || "";
  auctionState.category = document.getElementById("auction-category")?.value || "";
  auctionState.rarity = document.getElementById("auction-rarity")?.value || "";
  auctionState.page = 0;
  auctionRenderMarket().catch(error => showToast(error.message, "error"));
}

function auctionPagination(data, renderer) {
  if (!data || (data.totalPages || 0) <= 1) return "";
  const previous = data.page > 0 ? `<button class="btn-secondary text-xs" onclick="auctionState.page--; ${renderer}()">Anterior</button>` : "";
  const next = data.page + 1 < data.totalPages ? `<button class="btn-secondary text-xs" onclick="auctionState.page++; ${renderer}()">Próxima</button>` : "";
  return `<div class="flex justify-between items-center text-xs text-slate-400">${previous}<span>Página ${(data.page || 0) + 1} de ${data.totalPages}</span>${next}</div>`;
}
