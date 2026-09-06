let invItems = [];
let invEquipments = [];
let invSelectedDismantleIds = new Set();
let invDigimonId = null;
let invTab = "items"; // "items" or "equipment"
let invChestOpeningInProgress = false;
let invItemUseInProgress = false;
let invPagination = { items: 0, equipment: 0, pageSize: 5 };
let invPageData = { items: { content: [], totalElements: 0, totalPages: 0 }, equipment: { content: [], totalElements: 0, totalPages: 0 } };
let invFilterState = {
  search: "",
  category: "ALL",
  fragmentStage: "ALL",
  rarity: "ALL",
  slot: "ALL",
  sort: "name-asc",
  open: false
};

async function renderInventoryPage() {
  const app = document.getElementById("app");
  showBottomNav("inventory");

  app.innerHTML = `
    <div class="page-container inventory-page">
      <header class="progression-page-header inventory-page-header">
        <div>
          <p class="progression-eyebrow progression-eyebrow-cyan">Gestão de recursos</p>
          <h2 class="progression-page-title">Mochila</h2>
          <p class="progression-page-subtitle">Organize seus itens e equipamentos para manter seu Digimon pronto para qualquer desafio.</p>
        </div>
        <button
          id="inv-config-btn"
          type="button"
          class="inventory-config-button"
          aria-expanded="false"
        >
          <span aria-hidden="true">⚙</span> Filtros
        </button>
      </header>

      <section class="progression-hero progression-hero-cyan inventory-hero mb-4">
        <div class="progression-hero-topline">
          <span class="progression-hero-kicker">Mochila do jogador</span>
          <span class="progression-hero-status">Organização ativa</span>
        </div>
        <div class="flex items-center gap-3">
          <div class="progression-hero-visual inventory-hero-visual" aria-hidden="true">🎒</div>
          <div class="min-w-0">
            <h3 class="progression-panel-title">Tudo em um só lugar</h3>
            <p class="inventory-hero-copy">Pesquise, filtre e use seus recursos sem perder de vista o que está disponível.</p>
          </div>
        </div>
      </section>

      <div class="inventory-workspace">
        <aside class="inventory-control-rail">
          <div class="inventory-control-heading">
            <span class="progression-eyebrow progression-eyebrow-cyan">Organização</span>
            <p>Filtre e encontre seus recursos</p>
          </div>

      <div id="inv-config-panel" class="card-sm inventory-config-panel mb-3 hidden">
        <div class="mb-3 rounded-lg border border-slate-700 bg-slate-900/50 px-3 py-2">
          <label class="flex items-center gap-2 text-sm text-slate-300 cursor-pointer">
            <input id="inv-pagination-enabled" type="checkbox" class="accent-cyan-500" checked>
            Usar paginação no inventário
          </label>
          <p class="text-xs text-slate-500 mt-1">Desative para exibir todos os itens de uma vez.</p>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <label class="text-xs text-slate-400 flex flex-col min-w-0">
            <span class="min-h-8 leading-4 flex items-start">Categoria dos itens</span>
            <select id="inv-category-filter" class="input mt-1" aria-label="Filtrar itens por categoria">
              <option value="ALL">Todas as categorias</option>
              <option value="CONSUMABLE">Consumíveis</option>
              <option value="MATERIAL">Materiais</option>
              <option value="EVOLUTION_MATERIAL">Evolução</option>
              <option value="FRAGMENT">Fragmentos</option>
              <option value="DIGITAMA">Digitamas</option>
              <option value="INCUBATOR">Incubadoras</option>
              <option value="CHEST">Baús</option>
              <option value="OTHER">Outros</option>
            </select>
          </label>
          <label id="inv-fragment-stage-filter-wrapper" class="text-xs text-slate-400 flex flex-col min-w-0 hidden">
            <span class="min-h-8 leading-4 flex items-start">Estágio do fragmento</span>
            <select id="inv-fragment-stage-filter" class="input mt-1" aria-label="Filtrar fragmentos por estágio">
              <option value="ALL">Todos os estágios</option>
              <option value="BABY_II">Baby II</option>
              <option value="ROOKIE">Rookie</option>
              <option value="CHAMPION">Champion</option>
              <option value="ULTIMATE">Ultimate</option>
              <option value="MEGA">Mega</option>
            </select>
          </label>
          <label class="text-xs text-slate-400 flex flex-col min-w-0">
            <span class="min-h-8 leading-4 flex items-start">Raridade</span>
            <select id="inv-rarity-filter" class="input mt-1" aria-label="Filtrar por raridade">
              <option value="ALL">Todas as raridades</option>
              <option value="LEGENDARY">Lendária</option>
              <option value="EPIC">Épica</option>
              <option value="RARE">Rara</option>
              <option value="COMMON">Comum</option>
            </select>
          </label>
          <label class="text-xs text-slate-400 flex flex-col min-w-0">
            <span class="min-h-8 leading-4 flex items-start">Slot de equipamento</span>
            <select id="inv-slot-filter" class="input mt-1" aria-label="Filtrar equipamentos por slot">
              <option value="ALL">Todos os slots</option>
              <option value="WEAPON">Armas</option>
              <option value="ARMOR">Armaduras</option>
              <option value="ACCESSORY">Acessórios</option>
            </select>
          </label>
          <label class="text-xs text-slate-400 flex flex-col min-w-0">
            <span class="min-h-8 leading-4 flex items-start">Ordenar por</span>
            <select id="inv-sort" class="input mt-1" aria-label="Ordenar Inventário">
              <option value="name-asc">Nome: A–Z</option>
              <option value="name-desc">Nome: Z–A</option>
              <option value="quantity-desc">Quantidade: maior para menor</option>
              <option value="quantity-asc">Quantidade: menor para maior</option>
              <option value="category-asc">Categoria: A–Z</option>
              <option value="rarity-desc">Raridade: maior para menor</option>
              <option value="rarity-asc">Raridade: menor para maior</option>
              <option value="level-desc">Tier do equipamento: maior para menor</option>
              <option value="refinement-desc">Refinamento: maior para menor</option>
            </select>
          </label>
        </div>
        <p id="inv-filter-summary" class="text-xs text-slate-500 mt-3"></p>
      </div>

      <div class="inventory-mode-switch" id="inv-tabs" role="tablist" aria-label="Tipo de inventário">
        <button class="inventory-mode-button active" data-tab="items" role="tab" aria-selected="true" onclick="invSwitchTab('items')"><span aria-hidden="true">▦</span> Itens</button>
        <button class="inventory-mode-button" data-tab="equipment" role="tab" aria-selected="false" onclick="invSwitchTab('equipment')"><span aria-hidden="true">⚔</span> Equipamentos</button>
      </div>

      <nav id="inv-category-tabs" class="inventory-category-nav mb-3" aria-label="Categorias do inventário">
        <button class="inventory-category-button" data-category="ALL" onclick="invSwitchCategory('ALL')">Todos</button>
        <button class="inventory-category-button" data-category="CONSUMABLE" onclick="invSwitchCategory('CONSUMABLE')">Consumíveis</button>
        <button class="inventory-category-button" data-category="MATERIAL" onclick="invSwitchCategory('MATERIAL')">Materiais</button>
        <button class="inventory-category-button" data-category="EVOLUTION_MATERIAL" onclick="invSwitchCategory('EVOLUTION_MATERIAL')">Evolução</button>
        <button class="inventory-category-button" data-category="FRAGMENT" onclick="invSwitchCategory('FRAGMENT')">Fragmentos</button>
        <button class="inventory-category-button" data-category="DIGITAMA" onclick="invSwitchCategory('DIGITAMA')">Digitamas</button>
        <button class="inventory-category-button" data-category="INCUBATOR" onclick="invSwitchCategory('INCUBATOR')">Incubadoras</button>
        <button class="inventory-category-button" data-category="CHEST" onclick="invSwitchCategory('CHEST')">Baús</button>
        <button class="inventory-category-button" data-category="OTHER" onclick="invSwitchCategory('OTHER')">Outros</button>
      </nav>

        </aside>

        <main class="inventory-content-column">
      <form id="inv-search-form" class="inventory-search-row mb-3">
        <span class="inventory-search-icon" aria-hidden="true">⌕</span>
        <input
          id="inv-search"
          class="input inventory-search-input flex-1"
          type="search"
          value="${escapeHtml(invFilterState.search)}"
          placeholder="Pesquisar item ou equipamento..."
          aria-label="Pesquisar no Inventário"
        />
        <div class="flex gap-2">
          <button type="submit" class="inventory-search-button">Buscar</button>
          <button id="inv-clear-search" type="button" class="inventory-clear-button">Limpar</button>
        </div>
      </form>

      <div class="inventory-list-heading">
        <div>
          <p class="inventory-list-title">Conteúdo da mochila</p>
          <p id="inv-visible-summary" class="inventory-list-summary">Carregando inventário...</p>
        </div>
        <span class="inventory-list-state">${invFilterState.open ? "Filtros visíveis" : "Filtros rápidos"}</span>
      </div>

      <div id="inv-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
        </main>
      </div>
    </div>
  `;

  invSetupFilterControls();
  await loadPlayerPaginationPreference();
  const paginationToggle = document.getElementById("inv-pagination-enabled");
  if (paginationToggle) {
    paginationToggle.checked = playerPaginationEnabled;
    paginationToggle.addEventListener("change", async () => {
      paginationToggle.disabled = true;
      try {
        await savePlayerPaginationPreference(paginationToggle.checked);
        invPagination.items = 0;
        invPagination.equipment = 0;
        await invRenderActiveTab();
      } finally {
        paginationToggle.disabled = false;
      }
    });
  }

  try {
    const [, dashboard] = await Promise.all([
      invLoadItemsPage(),
      apiGet("/players/me/dashboard")
    ]);

    invDigimonId = dashboard.activeDigimon ? dashboard.activeDigimon.id : null;
    invItems = invPageData.items.content;
    invEquipments = [];
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
  invPagination[tab] = 0;
  document.querySelectorAll("#inv-tabs [data-tab]").forEach(btn => {
    const active = btn.dataset.tab === tab;
    btn.classList.toggle("active", active);
    btn.setAttribute("aria-selected", String(active));
  });
  invRenderActiveTab();
}

function invSwitchCategory(category) {
  invFilterState.category = category;
  invPagination.items = 0;
  invSyncFilterControls();
  invRenderActiveTab();
}

function invSyncCategoryTabs() {
  document.querySelectorAll("#inv-category-tabs [data-category]").forEach(btn => {
    const active = btn.dataset.category === invFilterState.category;
    btn.classList.toggle("active", active);
    btn.setAttribute("aria-current", active ? "page" : "false");
  });
}

function invSetupFilterControls() {
  invFilterState.open = false;
  document.getElementById("inv-config-btn")?.addEventListener("click", invToggleConfig);
  document.getElementById("inv-search-form")?.addEventListener("submit", invSubmitSearch);
  document.getElementById("inv-clear-search")?.addEventListener("click", invClearSearch);
  document.getElementById("inv-category-filter")?.addEventListener("change", (event) => {
    invFilterState.category = event.target.value;
    invPagination.items = 0;
    invRenderActiveTab();
  });
  document.getElementById("inv-fragment-stage-filter")?.addEventListener("change", (event) => {
    invFilterState.fragmentStage = event.target.value;
    invPagination.items = 0;
    invRenderActiveTab();
  });
  document.getElementById("inv-rarity-filter")?.addEventListener("change", (event) => {
    invFilterState.rarity = event.target.value;
    invPagination[invTab] = 0;
    invRenderActiveTab();
  });
  document.getElementById("inv-slot-filter")?.addEventListener("change", (event) => {
    invFilterState.slot = event.target.value;
    invPagination.equipment = 0;
    invRenderActiveTab();
  });
  document.getElementById("inv-sort")?.addEventListener("change", (event) => {
    invFilterState.sort = event.target.value;
    invPagination[invTab] = 0;
    invRenderActiveTab();
  });
  invSyncFilterControls();
  invUpdateFilterVisibility();
}

function invToggleConfig() {
  const panel = document.getElementById("inv-config-panel");
  const button = document.getElementById("inv-config-btn");
  if (!panel || !button) return;

  invFilterState.open = panel.classList.contains("hidden");
  panel.classList.toggle("hidden", !invFilterState.open);
  button.setAttribute("aria-expanded", String(invFilterState.open));
}

function invSubmitSearch(event) {
  event.preventDefault();
  invFilterState.search = document.getElementById("inv-search")?.value.trim() || "";
  invPagination[invTab] = 0;
  invRenderActiveTab();
}

function invClearSearch() {
  invFilterState.search = "";
  invPagination[invTab] = 0;
  const input = document.getElementById("inv-search");
  if (input) input.value = "";
  invRenderActiveTab();
}

function invSyncFilterControls() {
  const search = document.getElementById("inv-search");
  const category = document.getElementById("inv-category-filter");
  const fragmentStage = document.getElementById("inv-fragment-stage-filter");
  const rarity = document.getElementById("inv-rarity-filter");
  const slot = document.getElementById("inv-slot-filter");
  const sort = document.getElementById("inv-sort");
  if (search) search.value = invFilterState.search;
  if (category) category.value = invFilterState.category;
  if (fragmentStage) fragmentStage.value = invFilterState.fragmentStage;
  if (rarity) rarity.value = invFilterState.rarity;
  invSyncCategoryTabs();
  if (slot) slot.value = invFilterState.slot;
  if (sort) sort.value = invFilterState.sort;
}

function invPageParams(tab) {
  const params = {
    page: invPagination[tab],
    size: invPagination.pageSize,
    search: invFilterState.search,
    rarity: invFilterState.rarity,
    sort: invFilterState.sort
  };
  if (tab === "items") {
    params.category = invFilterState.category;
    params.fragmentStage = invFilterState.fragmentStage;
  } else {
    params.slot = invFilterState.slot;
  }
  return params;
}

async function invLoadItemsPage() {
  if (playerPaginationEnabled) {
    invPageData.items = await apiGet("/inventory/page", invPageParams("items"));
  } else {
    const items = await apiGet("/inventory") || [];
    invPageData.items = { content: items, totalElements: items.length, totalPages: 1 };
  }
  invItems = invPageData.items.content || [];
  return invPageData.items;
}

async function invLoadEquipmentPage() {
  if (!invDigimonId) {
    invPageData.equipment = { content: [], totalElements: 0, totalPages: 0 };
    invEquipments = [];
    return invPageData.equipment;
  }
  if (playerPaginationEnabled) {
    invPageData.equipment = await apiGet("/equipment/inventory/page", invPageParams("equipment"));
  } else {
    const equipment = await apiGet("/equipment/inventory") || [];
    invPageData.equipment = { content: equipment, totalElements: equipment.length, totalPages: 1 };
  }
  invEquipments = invPageData.equipment.content || [];
  return invPageData.equipment;
}

async function invRenderActiveTab() {
  invUpdateFilterVisibility();
  const content = document.getElementById("inv-content");
  if (!content) return;
  content.innerHTML = '<div class="card animate-pulse"><div class="h-32"></div></div>';
  try {
    if (invTab === "items") {
      await invLoadItemsPage();
      invRenderItems();
    } else {
      await invLoadEquipmentPage();
      invRenderEquipment();
    }
  } catch (err) {
    content.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

function invUpdateFilterVisibility() {
  const category = document.getElementById("inv-category-filter");
  const categoryTabs = document.getElementById("inv-category-tabs");
  const slot = document.getElementById("inv-slot-filter");
  const fragmentStageWrapper = document.getElementById("inv-fragment-stage-filter-wrapper");
  const isFragmentCategory = invFilterState.category === "FRAGMENT";
  if (category) category.disabled = invTab !== "items";
  if (categoryTabs) categoryTabs.classList.toggle("hidden", invTab !== "items");
  if (fragmentStageWrapper) fragmentStageWrapper.classList.toggle("hidden", invTab !== "items" || !isFragmentCategory);
  if (slot) slot.disabled = invTab !== "equipment";
}

function invUpdateFilterSummary(visible, total, label) {
  const text = `Exibindo ${visible} de ${total} ${label}${total === 1 ? "" : "s"}.`;
  const summary = document.getElementById("inv-filter-summary");
  const visibleSummary = document.getElementById("inv-visible-summary");
  if (summary) summary.textContent = text;
  if (visibleSummary) visibleSummary.textContent = text;
}

function invNormalize(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

function invResolvedCategory(item) {
  const type = String(item?.itemType || "").toUpperCase();
  const definitionCode = String(item?.itemDefinition?.code || "").toUpperCase();
  if (type.startsWith("FRAGMENT_") || definitionCode.startsWith("FRAGMENT_")) return "FRAGMENT";

  const definitionCategory = item?.itemDefinition?.category;
  if (definitionCategory) return String(definitionCategory).toUpperCase();

  if (type.startsWith("DIGITAMA_")) return "DIGITAMA";
  if (type.startsWith("INCUBATOR_")) return "INCUBATOR";
  if (type === "LOOT_CHEST") return "CHEST";
  if (type === "EVOLUTION_MATERIAL") return "EVOLUTION_MATERIAL";
  if (type.startsWith("FRAGMENT_")) return "FRAGMENT";
  if (type === "POTION_SMALL" || type.startsWith("XP_DISC_") || type.startsWith("STORAGE_SLOT_") || type === "INCUBATION_SLOT_UNLOCK" || type === "MISSION_SLOT_UNLOCK") return "CONSUMABLE";
  if (type === "TRAINING_STONE" || type === "DATA_CORE" || type === "REFINEMENT_STONE") return "MATERIAL";
  return "OTHER";
}

function invItemDisplayName(item) {
  return item?.itemDefinition?.name || invItemName(item?.itemType);
}

function invFragmentStage(item) {
  const type = String(item?.itemType || "").toUpperCase();
  const icon = String(item?.itemDefinition?.icon || "").toUpperCase();

  if (type === "FRAGMENT_BABY_II" || icon.includes("BABY2") || icon.includes("BABY_II")) return "BABY_II";
  if (type === "FRAGMENT_ROOKIE" || icon.includes("ROOKIE")) return "ROOKIE";
  if (type === "FRAGMENT_CHAMPION" || icon.includes("CHAMPION")) return "CHAMPION";
  if (type === "FRAGMENT_ULTIMATE" || icon.includes("ULTIMATE")) return "ULTIMATE";
  if (type === "FRAGMENT_MEGA" || icon.includes("MEGA")) return "MEGA";
  return null;
}

function invGetFilteredItems() {
  const search = invNormalize(invFilterState.search);
  return invAggregateItems(invItems)
    .filter(item => Number(item.quantity) > 0)
    .filter(item => {
      const matchesSearch = !search || [
        invItemDisplayName(item),
        item.itemType,
        item.itemDefinition?.code,
        item.itemDefinition?.category,
        item.itemDefinition?.rarity
      ].some(value => invNormalize(value).includes(search));
      const matchesCategory = invFilterState.category === "ALL" || invResolvedCategory(item) === invFilterState.category;
      const matchesFragmentStage = invFilterState.category !== "FRAGMENT"
        || invFilterState.fragmentStage === "ALL"
        || invFragmentStage(item) === invFilterState.fragmentStage;
      const itemRarity = String(item.itemDefinition?.rarity || "").toUpperCase();
      const matchesRarity = invFilterState.rarity === "ALL" || itemRarity === invFilterState.rarity;
      return matchesSearch && matchesCategory && matchesFragmentStage && matchesRarity;
    });
}

function invRarityRank(rarity) {
  return {
    COMMON: 1,
    RARE: 2,
    EPIC: 3,
    LEGENDARY: 4
  }[String(rarity || "").toUpperCase()] || 0;
}

function invCompareText(a, b) {
  return String(a || "").localeCompare(String(b || ""), "pt-BR", { sensitivity: "base" });
}

// ==================== ITEMS TAB ====================

function invRenderItems() {
  const content = document.getElementById("inv-content");
  const page = invPageData.items;
  const filteredItems = playerPaginationEnabled ? null : invSortItems(invGetFilteredItems());
  const items = playerPaginationEnabled ? (page.content || []) : filteredItems;
  invUpdateFilterSummary(
    playerPaginationEnabled ? invGetPageSummary("items", page.totalElements) : items.length,
    playerPaginationEnabled ? page.totalElements : items.length,
    "item"
  );

  if (items.length === 0) {
    const emptyMessage = page.totalElements === 0 ? "Nenhum item no inventário." : "Nenhum item encontrado com os filtros atuais.";
    content.innerHTML = `<div class="inventory-empty-state"><span class="inventory-empty-icon" aria-hidden="true">⌁</span><p>${emptyMessage}</p><span>Tente ajustar a categoria, a busca ou os filtros avançados.</span></div>`;
    return;
  }

  content.innerHTML = items.map(item => {
    const def = item.itemDefinition;
    const isXpDiskItem = invIsXpDisk(item.itemType);
    const isBatchUsableItem = isXpDiskItem || item.itemType === "POTION_SMALL" || item.itemType === "TRAINING_STONE" || item.itemType === "DATA_CORE";
    const name = def ? def.name : invItemName(item.itemType);
    const emoji = isXpDiskItem ? invItemEmoji(item.itemType) : def ? invCategoryEmoji(def.category) : invItemEmoji(item.itemType);
    const catName = def ? invCategoryLabel(def.category) : invItemCategoryName(item.itemType);
    const category = invResolvedCategory(item);
    const catBadge = def ? invCategoryBadge(category) : invItemCategory(item.itemType);
    const chestCode = category === "CHEST" && def ? def.code : null;
    const isChest = item.itemType === "LOOT_CHEST" || !!chestCode;
    const chestQuantityInputId = chestCode ? `inv-chest-quantity-${String(chestCode).replace(/[^a-zA-Z0-9_-]/g, "-")}` : null;
    const batchQuantityInputId = isBatchUsableItem ? `inv-batch-quantity-${String(item.itemType).replace(/[^a-zA-Z0-9_-]/g, "-")}` : null;
    const maxUseQuantity = Math.min(999, Math.max(1, Number(item.quantity) || 1));
    const digitamaItem = category === "DIGITAMA" || item.itemType.startsWith("DIGITAMA_");
    const incubatorItem = category === "INCUBATOR" || item.itemType.startsWith("INCUBATOR_");
    const incubationOnly = digitamaItem || incubatorItem;
    const usable = !incubationOnly && (def ? def.usable : invIsUsable(item.itemType));
    const action = isChest && chestCode ? `
      <div class="inventory-chest-controls flex items-center gap-1">
        <input id="${chestQuantityInputId}" class="input inventory-quantity-input text-center" type="number" min="1" max="${maxUseQuantity}" value="1" aria-label="Quantidade de baús" />
        <button class="btn-sm btn-primary inventory-chest-open-btn whitespace-nowrap" onclick="invOpenChest('${escapeHtml(chestCode)}', document.getElementById('${chestQuantityInputId}').value)">Abrir</button>
        <button type="button" class="btn-sm btn-secondary inventory-max-btn whitespace-nowrap" onclick="document.getElementById('${chestQuantityInputId}').value = ${maxUseQuantity}">Máx.</button>
      </div>
    ` : incubatorItem ? `
      <button class="btn-sm btn-primary whitespace-nowrap" onclick="navigateTo('incubation')">Usar</button>
    ` : isBatchUsableItem ? `
      <div class="inventory-batch-controls">
        <input id="${batchQuantityInputId}" class="input w-16 text-center" type="number" min="1" max="${maxUseQuantity}" value="1" aria-label="Quantidade de ${escapeAttr(name)}" />
        <button class="btn-sm btn-primary whitespace-nowrap" onclick="invUseItem('${escapeHtml(item.itemType)}', document.getElementById('${batchQuantityInputId}').value)">Usar</button>
        <button type="button" class="btn-sm btn-secondary inventory-max-btn whitespace-nowrap" onclick="document.getElementById('${batchQuantityInputId}').value = ${maxUseQuantity}">Máx.</button>
      </div>
    ` : usable ? `
      <button class="btn-sm btn-primary" onclick="invUseItem('${escapeHtml(item.itemType)}')">Usar</button>
    ` : "";

    return `
      <article class="inventory-item-card">
        <div class="inventory-item-icon ${isChest ? "inventory-item-icon-chest" : ""}" aria-hidden="true">${isChest ? renderChestIcon("w-12 h-12") : emoji}</div>
        <div class="inventory-item-body">
          <div class="inventory-item-heading">
            <p class="inventory-item-name" title="${escapeAttr(name)}" aria-label="${escapeAttr(name)}">${escapeHtml(name)}</p>
            <span class="inventory-quantity-badge">x${item.quantity}</span>
          </div>
          <div class="inventory-item-meta">
            <span class="badge badge-${catBadge}">${escapeHtml(catName)}</span>
          </div>
        </div>
        <div class="inventory-item-action">${action}</div>
      </article>
    `;
  }).join("") + invRenderPagination("items", page.totalElements, page.totalPages);
}

function invGetPagedEntries(tab, entries) {
  const totalPages = Math.max(1, Math.ceil(entries.length / invPagination.pageSize));
  invPagination[tab] = Math.min(Math.max(0, invPagination[tab]), totalPages - 1);
  const start = invPagination[tab] * invPagination.pageSize;
  return entries.slice(start, start + invPagination.pageSize);
}

function invGetPageSummary(tab, total) {
  if (total === 0) return 0;
  const start = invPagination[tab] * invPagination.pageSize + 1;
  return `${start}-${Math.min(start + invPagination.pageSize - 1, total)}`;
}

function invRenderPagination(tab, total, backendTotalPages = null) {
  if (!playerPaginationEnabled) return "";
  const totalPages = backendTotalPages == null ? Math.ceil(total / invPagination.pageSize) : backendTotalPages;
  if (totalPages <= 1) return "";
  const currentPage = invPagination[tab];
  return `<div class="flex items-center justify-between gap-3 mt-4 pt-3 border-t border-slate-800"><span class="text-xs text-slate-500">Página ${currentPage + 1} de ${totalPages}</span><div class="flex gap-2"><button type="button" class="btn-sm btn-secondary" ${currentPage === 0 ? "disabled" : ""} onclick="invSetPage('${tab}', ${currentPage - 1})">Anterior</button><button type="button" class="btn-sm btn-secondary" ${currentPage >= totalPages - 1 ? "disabled" : ""} onclick="invSetPage('${tab}', ${currentPage + 1})">Próxima</button></div></div>`;
}

function invSetPage(tab, page) {
  invPagination[tab] = Math.max(0, Number(page) || 0);
  invRenderActiveTab();
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
              : (String(item.itemType || "") === "POTION_SMALL" || String(item.itemType || "").startsWith("XP_DISC_"))
                ? "CONSUMABLE"
                : String(item.itemType || "").startsWith("STORAGE_SLOT_")
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
    const aName = invItemDisplayName(a);
    const bName = invItemDisplayName(b);
    const aCategory = invResolvedCategory(a);
    const bCategory = invResolvedCategory(b);
    const aRarity = a.itemDefinition?.rarity;
    const bRarity = b.itemDefinition?.rarity;
    let comparison = 0;

    switch (invFilterState.sort) {
      case "name-desc":
        comparison = -invCompareText(aName, bName);
        break;
      case "quantity-desc":
        comparison = (Number(b.quantity) || 0) - (Number(a.quantity) || 0);
        break;
      case "quantity-asc":
        comparison = (Number(a.quantity) || 0) - (Number(b.quantity) || 0);
        break;
      case "category-asc":
        comparison = invCompareText(invCategoryLabel(aCategory), invCategoryLabel(bCategory));
        break;
      case "rarity-desc":
        comparison = invRarityRank(bRarity) - invRarityRank(aRarity);
        break;
      case "rarity-asc":
        comparison = invRarityRank(aRarity) - invRarityRank(bRarity);
        break;
      case "name-asc":
      default:
        comparison = invCompareText(aName, bName);
        break;
    }

    if (comparison !== 0) return comparison;
    const aCode = a.itemDefinition?.code || a.itemType || "";
    const bCode = b.itemDefinition?.code || b.itemType || "";
    return invCompareText(aCode, bCode);
  });
}

function invCategoryEmoji(category) {
  const map = {
    CONSUMABLE: "🧪", MATERIAL: "🔮", FRAGMENT: "🧩",
    EVOLUTION_MATERIAL: "⭐", DIGITAMA: "🥚", INCUBATOR: "📦", CHEST: renderChestIcon("w-10 h-10")
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
  await invLoadItemsPage();
  if (document.getElementById("inv-content")) invRenderItems();
}

function invStackLimitDetails(err) {
  const message = String(err && err.message || "");
  const portugueseMatch = message.match(/limite máximo de (\d+) unidades para o item (.+?)\.\s*$/i);
  if (portugueseMatch) return { itemName: portugueseMatch[2], maxStack: portugueseMatch[1] };
  const englishMatch = message.match(/stack limit exceeded for item ['"](.+?)['"]\. Maximum stack: (\d+)/i);
  if (englishMatch) return { itemName: englishMatch[1], maxStack: englishMatch[2] };
  return { itemName: "o item recebido", maxStack: "999" };
}

function invIsStackLimitError(err) {
  return /stack limit exceeded|limite máximo de|limite de stack/i.test(String(err && err.message || ""));
}

function invShowStackLimitModal(err) {
  const existing = document.getElementById("inventory-stack-limit-modal");
  if (existing) existing.remove();
  const details = invStackLimitDetails(err);
  const overlay = document.createElement("div");
  overlay.id = "inventory-stack-limit-modal";
  overlay.className = "fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/80";
  overlay.setAttribute("role", "alertdialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.setAttribute("aria-labelledby", "inventory-stack-limit-title");
  overlay.innerHTML = `
    <div class="card w-full max-w-md border border-amber-700 bg-slate-900" onclick="event.stopPropagation()">
      <div class="flex items-start gap-3">
        <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-amber-700 bg-amber-950/50 text-xl text-amber-300" aria-hidden="true">!</div>
        <div>
          <p class="text-xs uppercase tracking-wider text-amber-400 font-bold">Limite do inventário</p>
          <h3 id="inventory-stack-limit-title" class="text-xl font-bold mt-1">Não foi possível concluir</h3>
        </div>
      </div>
      <p class="mt-4 text-sm leading-relaxed text-slate-200">O item <strong class="text-amber-300">${escapeHtml(details.itemName)}</strong> já atingiu o limite de <strong class="text-amber-300">${escapeHtml(details.maxStack)} unidades</strong> no inventário.</p>
      <p class="mt-3 text-sm leading-relaxed text-slate-400">A operação foi cancelada e nenhum item foi consumido. Libere espaço ou use parte desse item antes de tentar novamente.</p>
      <button id="inventory-stack-limit-confirm" class="btn-primary mt-6 w-full">Entendi</button>
    </div>
  `;
  document.body.appendChild(overlay);
  const confirmButton = overlay.querySelector("#inventory-stack-limit-confirm");
  confirmButton?.focus();
  confirmButton?.addEventListener("click", () => overlay.remove());
}

async function invUseItem(itemType, quantity = null) {
  if (itemType === "RARITY_REROLL") {
    await invStartRarityReroll();
    return;
  }
  const isXpDiskItem = invIsXpDisk(itemType);
  const isBatchUsableItem = isXpDiskItem || itemType === "POTION_SMALL" || itemType === "TRAINING_STONE" || itemType === "DATA_CORE";
  let requestedQuantity = 1;
  if (isBatchUsableItem) {
    requestedQuantity = quantity == null ? 1 : Number.parseInt(quantity, 10);
    if (!Number.isInteger(requestedQuantity) || requestedQuantity < 1 || requestedQuantity > 999) {
      showToast("Informe uma quantidade válida (1 a 999).", "error");
      return;
    }
  }
  if (invItemUseInProgress) return;

  invItemUseInProgress = true;
  try {
    const payload = { itemType: itemType };
    if (isBatchUsableItem) payload.quantity = requestedQuantity;
    const result = await apiPost("/inventory/use", payload);
    const usedQuantity = Math.max(1, Number(result && result.quantity) || requestedQuantity);
    const levelMessage = result && result.levelUp ? ` Nível ${result.currentLevel}!` : "";
    if (result && result.xpGranted > 0) {
      const quantityMessage = usedQuantity === 1 ? "1 unidade utilizada" : `${usedQuantity} unidades utilizadas`;
      showToast(`${invItemName(itemType)}: ${quantityMessage}, +${result.xpGranted} XP.${levelMessage}`);
    } else {
      showToast(result && result.message
        ? result.message
        : itemType === "INCUBATION_SLOT_UNLOCK"
          ? "Slot de incubação desbloqueado!"
          : `${invItemName(itemType)} usado!`);
    }
    await invReloadItems();
    if (result && typeof showNewlyUnlockedContent === "function") {
      showNewlyUnlockedContent(result.newlyUnlockedContent);
    }
  } catch (err) {
    if (invIsStackLimitError(err)) {
      invShowStackLimitModal(err);
    } else {
      showToast(err.message, "error");
    }
  } finally {
    invItemUseInProgress = false;
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
    if (invIsStackLimitError(err)) {
      invShowStackLimitModal(err);
    } else {
      showToast(err.message, "error");
    }
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
    <div class="card w-full max-w-md max-h-[88vh] border-cyan-800 shadow-2xl flex flex-col overflow-hidden">
      <div class="text-center mb-4 shrink-0">
        <div class="flex justify-center mb-2">${renderChestIcon("w-24 h-24")}</div>
        <h3 class="text-xl font-bold">${escapeHtml(title)}</h3>
        <p class="text-sm text-slate-400 mt-1">${escapeHtml(result && result.chestName || "Baú")} · ${chestQuantity} ${chestQuantity === 1 ? "baú" : "baús"}</p>
        <p class="text-xs text-slate-500 mt-2">Cada item possui sua própria raridade</p>
      </div>
      <div class="card-sm mb-4 flex min-h-0 max-h-[52vh] flex-col overflow-hidden shrink-0">
        <p class="text-xs text-slate-400 mb-2 shrink-0">Recompensas</p>
        <div class="min-h-0 overflow-y-auto overscroll-contain pr-3">
        ${items.length > 0 ? items.map(item => {
          const isEquipment = String(item.itemType || "") === "EQUIPMENT";
          const effectiveRarity = isEquipment ? item.equipmentRarity : item.rarity;
          const rarityLabel = effectiveRarity ? formatRarity(effectiveRarity) : "Indefinida";
          const itemName = item.itemName || item.equipmentTemplateName || item.materialCode || invItemName(item.itemType);
          return `
          <div class="flex items-center justify-between py-3 border-b border-slate-800 last:border-0">
            <div class="min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <p class="font-semibold text-sm truncate">${escapeHtml(itemName)}</p>
                ${effectiveRarity ? `<span class="badge badge-${String(effectiveRarity).toLowerCase()}">${escapeHtml(rarityLabel)}</span>` : ""}
              </div>
              ${isEquipment ? `<p class="text-xs font-semibold text-slate-300 mt-1">Raridade do equipamento: ${escapeHtml(rarityLabel)}</p><p class="text-xs text-slate-500 mt-1">Template: ${escapeHtml(item.equipmentTemplateName || item.itemCode || itemName)}</p>` : item.materialCode ? `<p class="text-xs text-slate-500">Material de evolução</p>` : ""}
            </div>
            <span class="font-bold text-cyan-300 ml-3">x${item.quantity}</span>
          </div>
        `;
        }).join("") : `<p class="text-sm text-slate-400">Nenhum item foi informado.</p>`}
        </div>
      </div>
      <p class="text-xs text-slate-400 text-center mb-4 shrink-0">${escapeHtml(message)}</p>
      <button class="btn-primary w-full shrink-0" onclick="document.getElementById('chest-opening-overlay').remove()">Continuar</button>
    </div>
  `;

  document.body.appendChild(overlay);
}


function invIsXpDisk(itemType) {
  return String(itemType || "").startsWith("XP_DISC_");
}

function invItemName(itemType) {
  const map = {
    POTION_SMALL: "Poção Pequena",
    TRAINING_STONE: "Pedra de Treino",
    DATA_CORE: "Núcleo de Dados",
    DIGITAMA_STARTER: "Digitama Inicial",
    DIGITAMA_FIRE: "Digitama de Fogo",
    DIGITAMA_WATER: "Digitama de Água",
    DIGITAMA_NATURE: "Digitama de Planta",
    DIGITAMA_EARTH: "Digitama de Terra",
    DIGITAMA_WIND: "Digitama de Vento",
    DIGITAMA_LIGHT: "Digitama de Luz",
    DIGITAMA_DARK: "Digitama de Trevas",
    DIGITAMA_THUNDER: "Digitama de Trovão",
    DIGITAMA_NEUTRAL: "Digitama Neutro",
    DIGITAMA_ICE: "Digitama de Gelo",
    DIGITAMA_STEEL: "Digitama de Metal",
    INCUBATOR_COMMON: "Incubadora Comum",
    INCUBATOR_RARE: "Incubadora Rara",
    INCUBATOR_EPIC: "Incubadora Épica",
    INCUBATOR_LEGENDARY: "Incubadora Lendária",
    INCUBATION_SLOT_UNLOCK: "Expansor de Slot de Incubação",
    MISSION_SLOT_UNLOCK: "Expansor de Slot de Missão",
    STORAGE_SLOT_1: "+1 Storage",
    STORAGE_SLOT_5: "+5 Storage",
    STORAGE_SLOT_10: "+10 Storage",
    XP_DISC_1: "Disco de XP +1%",
    XP_DISC_3: "Disco de XP +3%",
    XP_DISC_5: "Disco de XP +5%",
    XP_DISC_10: "Disco de XP +10%",
    XP_DISC_15: "Disco de XP +15%",
    XP_DISC_20: "Disco de XP +20%",
    FRAGMENT_ROOKIE: "Fragmento Rookie",
    FRAGMENT_CHAMPION: "Fragmento Champion",
    FRAGMENT_ULTIMATE: "Fragmento Ultimate",
    FRAGMENT_MEGA: "Fragmento Mega",
    EVOLUTION_MATERIAL: "Material de Evolução",
    LOOT_CHEST: "Baú",
    RARITY_REROLL: "Dado de Raridade"
  };
  return map[itemType] || itemType;
}

function invItemEmoji(itemType) {
  const map = {
    POTION_SMALL: "🧪", TRAINING_STONE: "💎", DATA_CORE: "🔮",
    DIGITAMA_STARTER: "⭐", DIGITAMA_FIRE: "🔥", DIGITAMA_WATER: "💧", DIGITAMA_NATURE: "🌿",
    DIGITAMA_EARTH: "🌍", DIGITAMA_WIND: "🌪️", DIGITAMA_LIGHT: "✨", DIGITAMA_DARK: "🌑",
    DIGITAMA_THUNDER: "⚡", DIGITAMA_NEUTRAL: "⚪", DIGITAMA_ICE: "❄️", DIGITAMA_STEEL: "⚙️",
    INCUBATOR_COMMON: "📦", INCUBATOR_RARE: "📦", INCUBATOR_EPIC: "📦", INCUBATOR_LEGENDARY: "🌟",
    INCUBATION_SLOT_UNLOCK: "🔓", MISSION_SLOT_UNLOCK: "🚀",
    STORAGE_SLOT_1: "🗄️", STORAGE_SLOT_5: "🗄️", STORAGE_SLOT_10: "🗄️",
    XP_DISC_1: "💿", XP_DISC_3: "💿", XP_DISC_5: "💿",
    XP_DISC_10: "💿", XP_DISC_15: "💿", XP_DISC_20: "💿",
    FRAGMENT_ROOKIE: "🧩", FRAGMENT_CHAMPION: "🧩", FRAGMENT_ULTIMATE: "🧩", FRAGMENT_MEGA: "🧩",
    EVOLUTION_MATERIAL: "⭐",
    LOOT_CHEST: renderChestIcon("w-10 h-10"),
    RARITY_REROLL: "🎲"
  };
  return map[itemType] || "📦";
}

function invIsUsable(itemType) {
  const usable = ["POTION_SMALL", "TRAINING_STONE", "DATA_CORE", "INCUBATION_SLOT_UNLOCK", "MISSION_SLOT_UNLOCK",
    "STORAGE_SLOT_1", "STORAGE_SLOT_5", "STORAGE_SLOT_10",
    "XP_DISC_1", "XP_DISC_3", "XP_DISC_5", "XP_DISC_10", "XP_DISC_15", "XP_DISC_20", "RARITY_REROLL"];
  return usable.includes(itemType);
}

function invItemCategory(itemType) {
  if (itemType === "POTION_SMALL" || itemType === "INCUBATION_SLOT_UNLOCK" || itemType === "MISSION_SLOT_UNLOCK"
      || itemType.startsWith("STORAGE_SLOT_")) return "common";
  if (itemType.startsWith("XP_DISC_")) return "rare";
  if (itemType.startsWith("DIGITAMA_")) return "rare";
  if (itemType.startsWith("INCUBATOR_")) return "epic";
  if (itemType.startsWith("FRAGMENT_")) return "champion";
  if (itemType === "EVOLUTION_MATERIAL") return "legendary";
  if (itemType === "RARITY_REROLL") return "epic";
  if (itemType === "LOOT_CHEST") return "rare";
  return "common";
}

function invItemCategoryName(itemType) {
  if (itemType === "POTION_SMALL") return "Poção";
  if (itemType === "INCUBATION_SLOT_UNLOCK") return "Incubação";
  if (itemType === "MISSION_SLOT_UNLOCK") return "Missões";
  if (itemType.startsWith("STORAGE_SLOT_")) return "Storage";
  if (itemType.startsWith("XP_DISC_")) return "Experiência";
  if (itemType === "TRAINING_STONE" || itemType === "DATA_CORE") return "Material";
  if (itemType.startsWith("DIGITAMA_")) return "Digitama";
  if (itemType.startsWith("INCUBATOR_")) return "Incubadora";
  if (itemType.startsWith("FRAGMENT_")) return "Fragmento";
  if (itemType === "EVOLUTION_MATERIAL") return "Evolução";
  if (itemType === "LOOT_CHEST") return "Baú";
  if (itemType === "RARITY_REROLL") return "Raridade";
  return "Item";
}

async function invStartRarityReroll() {
  if (invItemUseInProgress) return;
  invItemUseInProgress = true;
  try {
    const result = await apiPost("/inventory/rarity-reroll/start", {});
    invShowRarityRerollModal(result);
    await invReloadItems();
  } catch (err) {
    showToast(err.message || "Não foi possível usar o Dado de Raridade.", "error");
  } finally {
    invItemUseInProgress = false;
  }
}

function invRarityBadgeClass(rarity) {
  const map = {
    COMMON: "badge-common",
    RARE: "badge-rare",
    EPIC: "badge-epic",
    LEGENDARY: "badge-legendary"
  };
  return map[String(rarity || "").toUpperCase()] || "badge-common";
}

function invRarityBoxClass(rarity) {
  const map = {
    COMMON: "rarity-box-common",
    RARE: "rarity-box-rare",
    EPIC: "rarity-box-epic",
    LEGENDARY: "rarity-box-legendary"
  };
  return map[String(rarity || "").toUpperCase()] || "rarity-box-common";
}

function invShowRarityRerollModal(result) {
  document.getElementById("rarity-reroll-overlay")?.remove();
  const currentRarityClass = invRarityBadgeClass(result.currentRarity);
  const newRarityClass = invRarityBadgeClass(result.newRarity);
  const newRarityLabel = result.newRarity ? formatRarity(result.newRarity) : "Sem alteração";
  const currentRarityBoxClass = invRarityBoxClass(result.currentRarity);
  const newRarityBoxClass = invRarityBoxClass(result.newRarity);
  const actionButtons = result.rerollId
    ? `<div class="grid grid-cols-1 gap-2"><button class="btn-primary" onclick="invResolveRarityReroll('${result.rerollId}','accept')">Aceitar nova</button><button class="btn-secondary" onclick="invResolveRarityReroll('${result.rerollId}','keep',false)">Manter e fechar</button><button class="btn-secondary" onclick="invResolveRarityReroll('${result.rerollId}','keep',true)">Manter e usar outro Dado</button></div>`
    : `<div class="rounded-lg border border-slate-700 bg-slate-900/70 p-3 text-center text-sm text-slate-300 mb-4">${result.message || "O Dado de Raridade não alterou a raridade do Digimon."}</div><button class="btn-secondary w-full" onclick="document.getElementById('rarity-reroll-overlay')?.remove()">Fechar</button>`;
  const overlay = document.createElement("div");
  overlay.id = "rarity-reroll-overlay";
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(2,6,23,.82);z-index:70;display:flex;align-items:center;justify-content:center;padding:1rem;";
  overlay.innerHTML = `<div class="card w-full max-w-md" role="dialog" aria-modal="true"><div class="flex justify-between items-start gap-4 mb-5"><div><p class="text-xs uppercase tracking-wider text-fuchsia-400 font-bold">Dado de Raridade</p>
<h3 class="text-xl font-bold mt-1">Escolha o destino</h3></div><button class="text-slate-400 hover:text-white text-2xl" aria-label="Fechar" onclick="document.getElementById('rarity-reroll-overlay')?.remove()">&times;</button></div><div class="grid grid-cols-2 gap-3 mb-4"><div class="rarity-box ${currentRarityBoxClass} p-4 text-center"><p class="rarity-box-label text-xs">Raridade atual</p><p class="mt-2"><span class="badge ${currentRarityClass}">${formatRarity(result.currentRarity)}</span></p></div><div class="rarity-box ${newRarityBoxClass} p-4 text-center"><p class="rarity-box-label text-xs">Nova raridade</p><p class="mt-2"><span class="badge ${newRarityClass}">${newRarityLabel}</span></p></div></div><p class="text-sm text-slate-300 mb-5">Aceitar substitui a raridade atual. Para manter a anterior, será cobrado <strong class="text-amber-300">${Number(result.keepCostBits).toLocaleString('pt-BR')} Bits</strong>.</p>${actionButtons}</div>`;
  overlay.onclick = (event) => { if (event.target === overlay) overlay.remove(); };
  document.body.appendChild(overlay);
}

async function invResolveRarityReroll(id, action, retry = false) {
  const overlay = document.getElementById("rarity-reroll-overlay");
  try {
    const result = await apiPost(`/inventory/rarity-reroll/${id}/${action}`, {});
    overlay?.remove();
    showToast(result.message || "Dado de Raridade processado com sucesso.");
    await invReloadItems();
    if (action === "keep" && retry) await invStartRarityReroll();
  } catch (err) { showToast(err.message || "Não foi possível processar o Dado de Raridade.", "error");
 }
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

function invGetFilteredEquipments() {
  const search = invNormalize(invFilterState.search);
  return invEquipments.filter(equipment => {
    const matchesSearch = !search || [
      equipment.name,
      equipment.setCode,
      equipment.slot,
      equipment.rarity,
      equipment.tier,
      equipment.refinementLevel
    ].some(value => invNormalize(value).includes(search));
    const matchesSlot = invFilterState.slot === "ALL" || String(equipment.slot || "").toUpperCase() === invFilterState.slot;
    const matchesRarity = invFilterState.rarity === "ALL" || String(equipment.rarity || "").toUpperCase() === invFilterState.rarity;
    return matchesSearch && matchesSlot && matchesRarity;
  });
}

function invSortEquipments(equipments) {
  return [...equipments].sort((a, b) => {
    if (a.equipped && !b.equipped) return -1;
    if (!a.equipped && b.equipped) return 1;

    const aName = a.name || "";
    const bName = b.name || "";
    let comparison = 0;
    switch (invFilterState.sort) {
      case "name-desc":
        comparison = -invCompareText(aName, bName);
        break;
      case "rarity-desc":
        comparison = invRarityRank(b.rarity) - invRarityRank(a.rarity);
        break;
      case "rarity-asc":
        comparison = invRarityRank(a.rarity) - invRarityRank(b.rarity);
        break;
      case "level-desc":
        comparison = (Number(b.tier) || 0) - (Number(a.tier) || 0);
        break;
      case "refinement-desc":
        comparison = (Number(b.refinementLevel) || 0) - (Number(a.refinementLevel) || 0);
        break;
      case "name-asc":
      default:
        comparison = invCompareText(aName, bName);
        break;
    }

    if (comparison !== 0) return comparison;
    return invCompareText(aName, bName);
  });
}

function invEnhancementCoreInfo(targetTier) {
  if (targetTier <= 4) return { code: "BASIC_ENHANCEMENT_CORE", label: "Núcleo de Aprimoramento" };
  if (targetTier <= 7) return { code: "ADVANCED_ENHANCEMENT_CORE", label: "Núcleo Avançado" };
  return { code: "SUPREME_ENHANCEMENT_CORE", label: "Núcleo Supremo" };
}
function invDismantleReward(tier) {
  if (tier <= 3) return { code: "BASIC_ENHANCEMENT_CORE", label: "Núcleo de Aprimoramento", quantity: tier === 3 ? 2 : 1 };
  if (tier <= 6) return { code: "ADVANCED_ENHANCEMENT_CORE", label: "Núcleo Avançado", quantity: tier === 6 ? 2 : 1 };
  return { code: "SUPREME_ENHANCEMENT_CORE", label: "Núcleo Supremo", quantity: tier === 9 ? 2 : 1 };
}
function invExactEquipmentKey(equipment) {
  return [equipment.name, equipment.setCode, equipment.slot, equipment.tier, equipment.rarity].map(value => String(value || "").toUpperCase()).join("|");
}
function invCoreQuantity(items, code) {
  return (items || []).filter(item => item.itemDefinition?.code === code || item.itemType === code).reduce((sum, item) => sum + Number(item.quantity || 0), 0);
}
async function invOpenEnhancementModal(equipmentId) {
  const target = invEquipments.find(equipment => equipment.id === equipmentId);
  if (!target) { showToast("Equipamento não encontrado no inventário.", "error"); return; }
  let equipments;
  let items;
  try {
    [equipments, items] = await Promise.all([apiGet("/equipment/inventory"), apiGet("/inventory")]);
  } catch (err) { showToast(err.message, "error"); return; }
  const freshTarget = equipments.find(equipment => equipment.id === equipmentId) || target;
  const targetTier = Number(freshTarget.tier) || 1;
  if (targetTier >= 10) { showToast("Este equipamento já está no tier máximo.", "error"); return; }
  if (freshTarget.locked) { showToast("Destranque o equipamento antes de aprimorar.", "error"); return; }
  const candidates = equipments.filter(equipment => equipment.id !== equipmentId && !equipment.equipped && !equipment.locked && invExactEquipmentKey(equipment) === invExactEquipmentKey(freshTarget));
  const nextTier = targetTier + 1;
  const requiredCopies = nextTier <= 5 ? 3 : nextTier <= 8 ? 4 : 5;
  const requiredMaterials = requiredCopies - 1;
  const core = invEnhancementCoreInfo(nextTier);
  const coreQuantity = invCoreQuantity(items, core.code);
  const overlay = document.createElement("div");
  overlay.id = "inventory-enhancement-modal";
  overlay.className = "fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/80";
  overlay.innerHTML = `<div class="card w-full max-w-lg max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()" role="dialog" aria-modal="true" aria-labelledby="inventory-enhancement-title"><div class="flex items-start justify-between gap-3 mb-4"><div><p class="text-xs uppercase tracking-wider text-cyan-300 font-bold">Aprimoramento de equipamento</p><h3 id="inventory-enhancement-title" class="font-bold text-xl mt-1">${escapeHtml(freshTarget.name)} → T${nextTier}</h3><p class="text-xs text-slate-400 mt-1">Selecione ${requiredMaterials} cópia(s) exata(s). A raridade será preservada.</p></div><button class="text-slate-400 text-2xl leading-none" aria-label="Fechar" onclick="invCloseEnhancementModal()">&times;</button></div><div class="rounded-xl border border-cyan-900/60 bg-cyan-950/20 p-3 mb-4"><div class="flex justify-between text-sm"><span class="text-slate-400">Equipamentos necessários</span><strong class="text-cyan-200">${requiredCopies} cópias totais</strong></div><div class="flex justify-between text-sm mt-2"><span class="text-slate-400">Equipamento principal</span><strong class="text-cyan-200">T${targetTier} · ${escapeHtml(formatRarity(freshTarget.rarity))}</strong></div><div class="flex justify-between text-sm mt-2"><span class="text-slate-400">Núcleo necessário</span><strong class="${coreQuantity > 0 ? "text-emerald-300" : "text-red-300"}">${escapeHtml(core.label)} · ${coreQuantity}/1</strong></div></div><p class="text-xs uppercase tracking-wider text-slate-500 mb-2">Cópias compatíveis (${candidates.length})</p><div id="inventory-enhancement-materials" class="flex flex-col gap-2 mb-4">${candidates.length ? candidates.map(candidate => `<label class="flex items-center gap-3 rounded-lg border border-slate-700 bg-slate-950/50 p-3 cursor-pointer hover:border-cyan-700"><input type="checkbox" class="inventory-enhancement-material h-4 w-4 accent-cyan-500" value="${candidate.id}"><span class="flex-1"><strong class="text-sm text-slate-200">${escapeHtml(candidate.name)}</strong><span class="block text-xs text-slate-500">T${candidate.tier} · ${escapeHtml(formatRarity(candidate.rarity))} · ${escapeHtml(invSetLabel(candidate.setCode))}</span></span><span class="text-xs text-slate-500">Material</span></label>`).join("") : `<div class="rounded-lg border border-amber-900/60 bg-amber-950/20 p-3 text-sm text-amber-200">Você não possui cópias compatíveis disponíveis.</div>`}</div><p id="inventory-enhancement-selection" class="text-xs text-slate-500 mb-4">0 de ${requiredMaterials} cópias selecionadas</p><div class="flex gap-2"><button class="btn-sm flex-1" style="background:#334155;color:#cbd5e1;padding:.7rem" onclick="invCloseEnhancementModal()">Cancelar</button><button id="inventory-enhancement-submit" class="btn-sm flex-1" style="background:#0e7490;color:#ecfeff;padding:.7rem" disabled>Aprimorar</button></div></div>`;
  overlay.onclick = event => { if (event.target === overlay) invCloseEnhancementModal(); };
  document.body.appendChild(overlay);
  const updateSelection = () => {
    const selected = [...overlay.querySelectorAll(".inventory-enhancement-material:checked")];
    if (selected.length > requiredMaterials) { selected[selected.length - 1].checked = false; return updateSelection(); }
    overlay.querySelector("#inventory-enhancement-selection").textContent = `${selected.length} de ${requiredMaterials} cópias selecionadas`;
    const submit = overlay.querySelector("#inventory-enhancement-submit");
    submit.disabled = selected.length !== requiredMaterials || coreQuantity < 1;
  };
  overlay.querySelectorAll(".inventory-enhancement-material").forEach(input => input.addEventListener("change", updateSelection));
  overlay.querySelector("#inventory-enhancement-submit")?.addEventListener("click", async event => {
    const button = event.currentTarget;
    const materialEquipmentIds = [...overlay.querySelectorAll(".inventory-enhancement-material:checked")].map(input => input.value);
    button.disabled = true; button.textContent = "Aprimorando...";
    try {
      const result = await apiPost("/equipment/enhance", { equipmentId, materialEquipmentIds });
      invCloseEnhancementModal();
      showToast(result.message || `Equipamento aprimorado para T${result.currentTier || nextTier}!`);
      await invReloadEquipment();
    } catch (err) { showToast(err.message, "error"); button.disabled = false; button.textContent = "Aprimorar"; }
  });
}
function invCloseEnhancementModal() { document.getElementById("inventory-enhancement-modal")?.remove(); }
function invOpenDismantleModal(equipmentId) {
  const equipment = invEquipments.find(item => item.id === equipmentId);
  if (!equipment) return;
  if (equipment.locked) { showToast("Destranque o equipamento antes de desmontar.", "error"); return; }
  const tier = Number(equipment.tier) || 1;
  if (tier >= 10) { showToast("Equipamentos T10 não podem ser desmontados.", "error"); return; }
  const reward = invDismantleReward(tier);
  document.getElementById("inventory-dismantle-modal")?.remove();
  const overlay = document.createElement("div");
  overlay.id = "inventory-dismantle-modal";
  overlay.className = "fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/80";
  overlay.innerHTML = `<div class="card w-full max-w-md" onclick="event.stopPropagation()" role="dialog" aria-modal="true"><div class="flex items-start justify-between gap-3"><div><p class="text-xs uppercase tracking-wider text-amber-300 font-bold">Desmontagem</p><h3 class="font-bold text-xl mt-1">Converter equipamento em núcleos?</h3></div><button class="text-slate-400 text-2xl leading-none" aria-label="Fechar" onclick="invCloseDismantleModal()">&times;</button></div><div class="rounded-xl border border-amber-900/60 bg-amber-950/20 p-4 my-4"><p class="font-bold text-slate-100">${escapeHtml(equipment.name)}</p><p class="text-sm text-slate-400 mt-1">T${tier} · ${escapeHtml(formatRarity(equipment.rarity))} · ${escapeHtml(invSetLabel(equipment.setCode))}</p><p class="text-sm text-amber-200 mt-3">Você receberá <strong>${reward.quantity}x ${escapeHtml(reward.label)}</strong>.</p></div><p class="text-xs leading-relaxed text-slate-500 mb-5">Esta ação é permanente. O equipamento será removido e a raridade não altera o retorno.</p><div class="flex gap-2"><button class="btn-sm flex-1" style="background:#334155;color:#cbd5e1;padding:.7rem" onclick="invCloseDismantleModal()">Cancelar</button><button id="inventory-dismantle-submit" class="btn-sm flex-1" style="background:#92400e;color:#fef3c7;padding:.7rem">Desmontar</button></div></div>`;
  overlay.onclick = event => { if (event.target === overlay) invCloseDismantleModal(); };
  document.body.appendChild(overlay);
  overlay.querySelector("#inventory-dismantle-submit")?.addEventListener("click", async event => {
    const button = event.currentTarget; button.disabled = true; button.textContent = "Desmontando...";
    try {
      const result = await apiPost("/equipment/dismantle", { equipmentId });
      invCloseDismantleModal();
      showToast(`Desmontagem concluída: ${result.quantityGranted || reward.quantity}x ${reward.label}.`);
      await Promise.all([invReloadEquipment(), invReloadItems()]);
    } catch (err) { showToast(err.message, "error"); button.disabled = false; button.textContent = "Desmontar"; }
  });
}
function invCloseDismantleModal() { document.getElementById("inventory-dismantle-modal")?.remove(); }
async function invToggleEquipmentLock(equipmentId, locked) {
  try {
    await apiPost("/equipment/lock", { equipmentId, locked });
    showToast(locked ? "Equipamento trancado." : "Equipamento destrancado.");
    await invReloadEquipment();
  } catch (err) { showToast(err.message, "error"); }
}
async function invSelectAllDismantlable() {
  try {
    const equipments = await apiGet("/equipment/inventory");
    const eligibleIds = equipments.filter(equipment => !equipment.equipped && !equipment.locked && Number(equipment.tier) < 10).map(equipment => equipment.id);
    const allSelected = eligibleIds.length > 0 && eligibleIds.every(id => invSelectedDismantleIds.has(id));
    if (allSelected) eligibleIds.forEach(id => invSelectedDismantleIds.delete(id));
    else eligibleIds.forEach(id => invSelectedDismantleIds.add(id));
    invRenderEquipment();
  } catch (err) { showToast(err.message, "error"); }
}
function invToggleDismantleSelection(equipmentId, selected) {
  if (selected) invSelectedDismantleIds.add(equipmentId); else invSelectedDismantleIds.delete(equipmentId);
  const count = document.getElementById("inventory-dismantle-selected-count");
  const button = document.getElementById("inventory-dismantle-batch-button");
  if (count) count.textContent = String(invSelectedDismantleIds.size);
  if (button) button.disabled = invSelectedDismantleIds.size === 0;
}
async function invDismantleSelected() {
  const equipmentIds = [...invSelectedDismantleIds];
  if (!equipmentIds.length) return;
  if (!window.confirm(`Desmontar ${equipmentIds.length} equipamento(s) selecionado(s)? Esta ação não pode ser desfeita.`)) return;
  const button = document.getElementById("inventory-dismantle-batch-button");
  if (button) { button.disabled = true; button.textContent = "Desmontando..."; }
  try {
    const result = await apiPost("/equipment/dismantle/batch", { equipmentIds });
    invSelectedDismantleIds.clear();
    const summary = Object.entries(result.coresGranted || {}).map(([code, quantity]) => `${quantity}x ${code}`).join(", ");
    showToast(`Desmontagem concluída: ${summary || `${result.dismantledCount} equipamento(s)`}.`);
    await Promise.all([invReloadEquipment(), invReloadItems()]);
  } catch (err) { showToast(err.message, "error"); if (button) { button.disabled = false; button.textContent = "Desmontar selecionados"; } }
}
function invRenderEquipment() {
  const content = document.getElementById("inv-content");
  const page = invPageData.equipment;
  const filteredEquipments = playerPaginationEnabled ? null : invSortEquipments(invGetFilteredEquipments());
  const equipments = playerPaginationEnabled ? (page.content || []) : filteredEquipments;
  invUpdateFilterSummary(
    playerPaginationEnabled ? invGetPageSummary("equipment", page.totalElements) : equipments.length,
    playerPaginationEnabled ? page.totalElements : equipments.length,
    "equipamento"
  );

  if (equipments.length === 0) {
    const emptyMessage = page.totalElements === 0 ? "Nenhum equipamento no inventário." : "Nenhum equipamento encontrado com os filtros atuais.";
    content.innerHTML = `<div class="inventory-empty-state"><span class="inventory-empty-icon" aria-hidden="true">⚔</span><p>${emptyMessage}</p><span>Tente ajustar a busca, raridade, slot ou ordenação.</span></div>`;
    return;
  }

  const eligibleOnPage = equipments.filter(eq => !eq.equipped && !eq.locked && Number(eq.tier) < 10);
  const allPageSelected = eligibleOnPage.length > 0 && eligibleOnPage.every(eq => invSelectedDismantleIds.has(eq.id));
  const dismantleToolbar = `<div class="inventory-bulk-toolbar"><div><p class="text-xs uppercase tracking-wider text-orange-300 font-bold">Desmontagem em lote</p><p class="text-xs text-slate-500 mt-1">Selecione equipamentos destrancados para converter vários de uma vez.</p></div><div class="flex gap-2"><button class="btn-sm inventory-action-enhance" onclick="invSelectAllDismantlable()">${allPageSelected ? "Limpar seleção" : "Selecionar todos"}</button><button id="inventory-dismantle-batch-button" class="btn-sm inventory-action-dismantle" onclick="invDismantleSelected()" ${invSelectedDismantleIds.size === 0 ? "disabled" : ""}>Desmontar selecionados (<span id="inventory-dismantle-selected-count">${invSelectedDismantleIds.size}</span>)</button></div></div>`;
  content.innerHTML = dismantleToolbar + equipments.map(eq => {
    const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
    const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
    const emoji = slotEmoji[eq.slot] || "⚔️";
    const refLabel = eq.refinementLevel > 0 ? ` +${eq.refinementLevel}` : "";
    const ascensionLevel = Number(eq.ascensionLevel) || 0;
    const ascensionLabel = ascensionLevel > 0 ? `<span class="badge badge-legendary">Ascensão ${ascensionLevel}</span>` : "";
    const lockLabel = eq.locked ? '<span class="badge badge-rare">Trancado</span>' : '';
    const canAscend = !eq.equipped && ascensionLevel < 3 && Number(eq.refinementLevel) >= 11;

    const stats = [];
    if (eq.effectiveBonusHp > 0) stats.push(`<span class="text-red-400">HP+${eq.effectiveBonusHp}</span>`);
    if (eq.effectiveBonusAttack > 0) stats.push(`<span class="text-orange-400">ATK+${eq.effectiveBonusAttack}</span>`);
    if (eq.effectiveBonusDefense > 0) stats.push(`<span class="text-blue-400">DEF+${eq.effectiveBonusDefense}</span>`);

    return `
      <article class="inventory-equipment-card ${eq.equipped ? "inventory-equipment-card-equipped" : ""}">
        <div class="inventory-equipment-icon" aria-hidden="true">${emoji}</div>
        <div class="inventory-equipment-body">
          <div class="inventory-equipment-heading">
            <p class="inventory-item-name" title="${escapeAttr(`${eq.name}${refLabel}`)}" aria-label="${escapeAttr(`${eq.name}${refLabel}`)}">${escapeHtml(eq.name)}${refLabel}</p>
            ${eq.equipped ? '<span class="badge badge-success">Equipado</span>' : `<span class="inventory-equipment-state">Disponível</span>${lockLabel}`}
          </div>
          <div class="inventory-equipment-meta">
            ${eq.setCode ? `<span class="badge badge-${invSetBadge(eq.setCode)}">${escapeHtml(invSetLabel(eq.setCode))}</span>` : ''}
            <span class="badge badge-${eq.rarity ? eq.rarity.toLowerCase() : 'common'}">T${eq.tier || '?'}</span>
            ${ascensionLabel}
            <span class="inventory-equipment-slot">${slotName[eq.slot] || eq.slot}</span>
          </div>
          ${stats.length > 0 ? `<div class="inventory-equipment-stats">${stats.join(" ")}</div>` : ""}
        </div>
        <div class="inventory-item-action inventory-equipment-action">
          ${eq.equipped ? `
            <button class="btn-sm inventory-action-danger" onclick="invUnequip('${eq.id}')">Desequipar</button>
          ` : `
            <button class="btn-sm btn-primary" onclick="invEquip('${eq.id}')">Equipar</button>
            ${canAscend ? `<button class="btn-sm inventory-action-ascend" onclick="invOpenAscensionPreview('${eq.id}')">Ascender</button>` : ''}
            ${!eq.locked && Number(eq.tier) < 10 ? `<button class="btn-sm inventory-action-enhance" onclick="invOpenEnhancementModal('${eq.id}')">Aprimorar</button><button class="btn-sm inventory-action-dismantle" onclick="invOpenDismantleModal('${eq.id}')">Desmontar</button>` : ''}
            <button class="btn-sm inventory-action-lock" onclick="invToggleEquipmentLock('${eq.id}', ${!eq.locked})">${eq.locked ? "Destrancar" : "Trancar"}</button>
            ${!eq.locked && Number(eq.tier) < 10 ? `<label class="inventory-dismantle-check" title="Selecionar para desmontagem"><input type="checkbox" ${invSelectedDismantleIds.has(eq.id) ? "checked" : ""} onchange="invToggleDismantleSelection('${eq.id}', this.checked)"> Lote</label>` : ''}
          `}
        </div>
      </article>
    `;
  }).join("") + invRenderPagination("equipment", page.totalElements, page.totalPages);
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
    if (typeof renderDashboardPage === "function") renderDashboardPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function invOpenAscensionPreview(equipmentId) {
  try {
    const preview = await apiGet(`/equipment/${equipmentId}/ascend-preview`);
    invRenderAscensionPreview(preview);
  } catch (err) {
    showToast(err.message, "error");
  }
}

function invFormatNumber(value) {
  return Number(value || 0).toLocaleString("pt-BR");
}

function invRenderAscensionPreview(preview) {
  document.getElementById("inventory-ascension-preview")?.remove();
  const overlay = document.createElement("div");
  overlay.id = "inventory-ascension-preview";
  overlay.className = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70";
  const ascensionBonus = { 1: "+30%", 2: "+50%", 3: "+100%" }[preview.nextAscensionLevel] || "";
  const statRow = (label, before, after, color) => `<div class="flex items-center justify-between rounded-lg bg-slate-900/70 px-3 py-2"><span class="text-xs text-slate-400">${label}</span><span class="text-sm font-bold"><span class="${color}">${invFormatNumber(before)}</span><span class="text-slate-500 mx-2">→</span><span class="text-emerald-300">${invFormatNumber(after)}</span></span></div>`;
  const costRow = (label, current, required) => `<div class="flex items-center justify-between text-sm"><span class="text-slate-400">${label}</span><span class="font-bold ${current >= required ? "text-emerald-300" : "text-red-300"}">${invFormatNumber(current)} / ${invFormatNumber(required)}</span></div>`;
  const restriction = preview.restrictionMessage ? `<div class="rounded-lg border border-red-900/70 bg-red-950/30 px-3 py-2 text-xs text-red-300">${escapeHtml(preview.restrictionMessage)}</div>` : `<div class="rounded-lg border border-emerald-900/70 bg-emerald-950/30 px-3 py-2 text-xs text-emerald-300">Todos os requisitos foram atendidos.</div>`;
  overlay.innerHTML = `
    <div class="card w-full max-w-md max-h-[90vh] overflow-y-auto" onclick="event.stopPropagation()">
      <div class="flex items-start justify-between gap-3 mb-4">
        <div><p class="text-xs uppercase tracking-wider text-amber-400 font-bold">Evolução de equipamento</p><h3 class="font-bold text-lg">Preview da Ascensão ${preview.nextAscensionLevel}</h3><p class="text-xs text-slate-400 mt-1">${escapeHtml(preview.equipment.name)} +${preview.equipment.refinementLevel} <span class="text-amber-300">(${ascensionBonus} atributos)</span></p></div>
        <button class="text-slate-400 text-2xl leading-none" aria-label="Fechar" onclick="invCloseAscensionPreview()">&times;</button>
      </div>
      <div class="rounded-xl border border-amber-900/60 bg-amber-950/20 p-3 mb-4"><p class="text-xs uppercase tracking-wider text-slate-500 mb-2">Status do equipamento</p><div class="grid grid-cols-2 gap-2 text-center"><div class="rounded-lg bg-slate-900/70 p-2"><p class="text-[10px] text-slate-500">Atual</p><p class="font-black text-lg">Ascensão ${preview.equipment.ascensionLevel || 0}</p><p class="text-xs text-slate-400">${preview.equipment.refinementLevel >= preview.requiredRefinementLevel ? "Refinamento máximo" : `Refinamento +${preview.equipment.refinementLevel}`}</p></div><div class="rounded-lg bg-amber-900/30 p-2"><p class="text-[10px] text-amber-300">Depois</p><p class="font-black text-lg text-amber-200">Ascensão ${preview.nextAscensionLevel}</p><p class="text-xs text-slate-400">Mesmo cap de refinamento</p></div></div></div>
      <div class="mb-4"><p class="text-xs uppercase tracking-wider text-slate-500 mb-2">Atributos efetivos</p><div class="flex flex-col gap-2">${statRow("HP", preview.beforeHp, preview.afterHp, "text-red-300")}${statRow("ATK", preview.beforeAttack, preview.afterAttack, "text-orange-300")}${statRow("DEF", preview.beforeDefense, preview.afterDefense, "text-blue-300")}</div></div>
      <div class="mb-4"><p class="text-xs uppercase tracking-wider text-slate-500 mb-2">Custos e requisitos</p><div class="rounded-lg border border-slate-700 bg-slate-950/50 p-3 flex flex-col gap-2">${costRow("Núcleos de Ascensão", preview.currentCores, preview.coreCost)}${costRow("Bits", preview.currentBits, preview.bitsCost)}<div class="text-[11px] text-slate-500 pt-1">Refinamento necessário: +${preview.requiredRefinementLevel}</div></div></div>
      ${restriction}
      <div class="flex gap-2 mt-4"><button class="btn-sm flex-1" style="background:#334155;color:#cbd5e1;padding:0.65rem" onclick="invCloseAscensionPreview()">Fechar</button><button id="inventory-ascend-submit" class="btn-sm flex-1" style="background:${preview.canAscend ? "#854d0e" : "#334155"};color:${preview.canAscend ? "#fde68a" : "#64748b"};padding:0.65rem" ${preview.canAscend ? `onclick="invSubmitAscension('${preview.equipment.id}', ${preview.nextAscensionLevel})"` : "disabled"}>Realizar Ascensão</button></div>
    </div>`;
  overlay.onclick = event => { if (event.target === overlay) invCloseAscensionPreview(); };
  document.body.appendChild(overlay);
}

function invCloseAscensionPreview() {
  document.getElementById("inventory-ascension-preview")?.remove();
}

async function invSubmitAscension(equipmentId, targetLevel) {
  const button = document.getElementById("inventory-ascend-submit");
  if (button) { button.disabled = true; button.textContent = "Ascendendo..."; }
  try {
    const result = await apiPost("/equipment/ascend", { equipmentId });
    invCloseAscensionPreview();
    showToast(result.message || `Ascensão ${targetLevel} realizada!`);
    await invReloadEquipment();
  } catch (err) {
    showToast(err.message, "error");
    if (button) { button.disabled = false; button.textContent = "Realizar Ascensão"; }
  }
}

async function invReloadEquipment() {
  if (!invDigimonId) return;
  try {
    await invLoadEquipmentPage();
    invRenderEquipment();
  } catch (err) {
    showToast(err.message, "error");
  }
}
