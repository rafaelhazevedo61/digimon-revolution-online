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

      <div class="flex gap-2 mb-4 overflow-x-auto">
        <button id="auction-tab-market" class="btn-primary text-sm whitespace-nowrap" onclick="auctionSetMode('market')">Mercado</button>
        <button id="auction-tab-mine" class="btn-secondary text-sm whitespace-nowrap" onclick="auctionSetMode('mine')">Meus anúncios</button>
        <button id="auction-tab-history" class="btn-secondary text-sm whitespace-nowrap" onclick="auctionSetMode('history')">Histórico</button>
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
      ? "btn-primary text-sm whitespace-nowrap"
      : "btn-secondary text-sm whitespace-nowrap";
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
      <div class="flex flex-col md:flex-row gap-2">
        <input id="auction-search" class="input flex-1" placeholder="Buscar item..." value="${escapeHtml(auctionState.search)}" />
        <input id="auction-category" class="input md:w-40" placeholder="Categoria" value="${escapeHtml(auctionState.category)}" />
        <input id="auction-rarity" class="input md:w-32" placeholder="Raridade" value="${escapeHtml(auctionState.rarity)}" />
        <button class="btn-primary" onclick="auctionApplyFilters()">Buscar</button>
      </div>
      <div class="flex items-center justify-between text-xs text-slate-400">
        <span>${Number(listings.totalElements || 0).toLocaleString("pt-BR")} anúncio(s) encontrado(s)</span>
        <button class="btn-secondary" onclick="auctionShowCreateForm()">+ Publicar item</button>
      </div>
    </div>
    ${cards || `<div class="card text-center text-slate-400">Nenhum anúncio encontrado.</div>`}
    ${auctionPagination(listings, "auctionRenderMarket")}
  `;
}

function auctionListingCard(listing) {
  const isMine = listing.sellerPlayerId === auctionState.dashboard?.id;
  const icon = auctionIconMarkup(listing);
  return `
    <div class="card flex flex-col sm:flex-row gap-3 sm:items-center justify-between">
      <div class="flex items-center gap-3 min-w-0">
        <div class="w-12 h-12 rounded-lg bg-slate-800 flex items-center justify-center shrink-0">${icon}</div>
        <div class="min-w-0">
          <p class="font-bold truncate">${escapeHtml(listing.itemName)}</p>
          <p class="text-xs text-slate-400">${escapeHtml(listing.itemCode)} · ${escapeHtml(listing.rarity || "Comum")}</p>
          <p class="text-xs text-slate-500">Vendedor: ${escapeHtml(listing.sellerUsername)} · ${listing.remainingQuantity}/${listing.quantity} disponível(is)</p>
        </div>
      </div>
      <div class="flex items-center justify-between sm:justify-end gap-3">
        <div class="text-right">
          <p class="font-bold text-amber-300">${Number(listing.unitPrice).toLocaleString("pt-BR")} Bits</p>
          <p class="text-xs text-slate-500">por unidade</p>
        </div>
        ${isMine
          ? `<span class="text-xs text-cyan-300">Seu anúncio</span>`
          : `<button class="btn-primary text-sm" onclick="auctionBuy('${listing.id}', ${listing.remainingQuantity}, ${listing.unitPrice})">Comprar</button>`}
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
            <p class="text-xs text-slate-400">A taxa de publicação é 100 Bits. O item ficará reservado até a venda, cancelamento ou expiração.</p>
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
              <label class="block text-xs text-slate-400">Duração<select id="auction-create-duration" class="input w-full mt-1"><option value="24">24 horas</option><option value="48" selected>48 horas</option><option value="72">72 horas</option></select></label>
            </div>
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

async function auctionBuy(listingId, maxQuantity, unitPrice) {
  const value = window.prompt(`Quantidade para comprar (máximo ${maxQuantity}). Preço: ${unitPrice.toLocaleString("pt-BR")} Bits por unidade.` , "1");
  if (value === null) return;
  const quantity = Number(value);
  if (!Number.isInteger(quantity) || quantity < 1 || quantity > maxQuantity) {
    showToast("Informe uma quantidade válida.", "error");
    return;
  }
  if (!window.confirm(`Confirmar compra de ${quantity} unidade(s) por ${(quantity * unitPrice).toLocaleString("pt-BR")} Bits?`)) return;
  try {
    const result = await apiPost(`/auction/listings/${listingId}/buy`, { quantity });
    showToast(result.message || "Compra realizada!");
    auctionState.dashboard = await apiGet("/players/me/dashboard");
    auctionRenderBits();
    await auctionRenderMarket();
  } catch (error) {
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
        <p class="text-xs text-slate-500">Status: ${escapeHtml(listing.status)} · expira em ${new Date(listing.expiresAt).toLocaleString("pt-BR")}</p>
      </div>
      ${listing.status === "ACTIVE" && listing.remainingQuantity > 0
        ? `<button class="btn-secondary text-sm" onclick="auctionCancel('${listing.id}')">Cancelar e devolver</button>`
        : `<span class="text-xs text-slate-500">Sem ações disponíveis</span>`}
    </div>
  `).join("");
  document.getElementById("auction-content").innerHTML = `
    <div class="card"><h3 class="font-bold">Meus anúncios</h3><p class="text-xs text-slate-400 mt-1">Os itens publicados ficam reservados até a venda ou devolução.</p></div>
    ${cards || `<div class="card text-center text-slate-400">Você ainda não publicou anúncios.</div>`}
    ${auctionPagination(data, "auctionRenderMine")}
  `;
}

async function auctionCancel(listingId) {
  if (!window.confirm("Cancelar este anúncio e devolver os itens ao inventário?")) return;
  try {
    await apiPost(`/auction/listings/${listingId}/cancel`, {});
    showToast("Anúncio cancelado e itens devolvidos.");
    await auctionRenderMine();
  } catch (error) {
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
