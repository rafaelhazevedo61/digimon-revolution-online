let invItems = [];
let invEquipments = [];
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
      <div class="page-container">
      <div class="flex items-center justify-between gap-2 mb-4 px-1">
        <h2 class="text-lg font-bold truncate">Inventário</h2>
        <button
          id="inv-config-btn"
          type="button"
          class="btn-sm whitespace-nowrap"
          style="background:#334155;color:#cbd5e1"
          aria-expanded="false"
        >
          ⚙ Configurar
        </button>
      </div>

      <div id="inv-config-panel" class="card-sm mb-3 hidden">
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

      <!-- Tabs -->
      <div class="flex gap-2 mb-4" id="inv-tabs">
        <button class="tab-btn active" data-tab="items" onclick="invSwitchTab('items')">Itens</button>
        <button class="tab-btn" data-tab="equipment" onclick="invSwitchTab('equipment')">Equipamentos</button>
      </div>

      <div id="inv-category-tabs" class="grid grid-cols-2 sm:grid-cols-4 gap-2 mb-4">
        <button class="tab-btn w-full" data-category="ALL" onclick="invSwitchCategory('ALL')">Todos</button>
        <button class="tab-btn w-full" data-category="CONSUMABLE" onclick="invSwitchCategory('CONSUMABLE')">Consumíveis</button>
        <button class="tab-btn w-full" data-category="MATERIAL" onclick="invSwitchCategory('MATERIAL')">Materiais</button>
        <button class="tab-btn w-full" data-category="EVOLUTION_MATERIAL" onclick="invSwitchCategory('EVOLUTION_MATERIAL')">Evolução</button>
        <button class="tab-btn w-full" data-category="FRAGMENT" onclick="invSwitchCategory('FRAGMENT')">Fragmentos</button>
        <button class="tab-btn w-full" data-category="DIGITAMA" onclick="invSwitchCategory('DIGITAMA')">Digitamas</button>
        <button class="tab-btn w-full" data-category="INCUBATOR" onclick="invSwitchCategory('INCUBATOR')">Incubadoras</button>
        <button class="tab-btn w-full" data-category="CHEST" onclick="invSwitchCategory('CHEST')">Baús</button>
        <button class="tab-btn w-full" data-category="OTHER" onclick="invSwitchCategory('OTHER')">Outros</button>
      </div>

      <form id="inv-search-form" class="flex flex-col sm:flex-row gap-2 mb-4">
        <input
          id="inv-search"
          class="input flex-1"
          type="search"
          value="${escapeHtml(invFilterState.search)}"
          placeholder="Pesquisar item ou equipamento..."
          aria-label="Pesquisar no Inventário"
        />
        <div class="flex gap-2">
          <button type="submit" class="btn-primary flex-1 sm:flex-none">Buscar</button>
          <button id="inv-clear-search" type="button" class="btn-secondary flex-1 sm:flex-none">Limpar</button>
        </div>
      </form>

      <div id="inv-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
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
  document.querySelectorAll("#inv-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.tab === tab);
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
  document.querySelectorAll("#inv-category-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.category === invFilterState.category);
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
  const summary = document.getElementById("inv-filter-summary");
  if (!summary) return;
  summary.textContent = `Exibindo ${visible} de ${total} ${label}${total === 1 ? "" : "s"}.`;
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
  if (type === "POTION_SMALL" || type.startsWith("XP_DISC_") || type.startsWith("STORAGE_SLOT_") || type === "INCUBATION_SLOT_UNLOCK") return "CONSUMABLE";
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
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">${page.totalElements === 0 ? "Nenhum item no inventário." : "Nenhum item nesta página."}</p>`;
    return;
  }

  content.innerHTML = items.map(item => {
    const def = item.itemDefinition;
    const isXpDiskItem = invIsXpDisk(item.itemType);
    const isBatchUsableItem = isXpDiskItem || item.itemType === "POTION_SMALL" || item.itemType === "TRAINING_STONE";
    const name = def ? def.name : invItemName(item.itemType);
    const emoji = isXpDiskItem ? invItemEmoji(item.itemType) : def ? invCategoryEmoji(def.category) : invItemEmoji(item.itemType);
    const catName = def ? invCategoryLabel(def.category) : invItemCategoryName(item.itemType);
    const category = invResolvedCategory(item);
    const catBadge = def ? invCategoryBadge(category) : invItemCategory(item.itemType);
    const chestCode = category === "CHEST" && def ? def.code : null;
    const isChest = item.itemType === "LOOT_CHEST" || !!chestCode;
    const chestQuantityInputId = chestCode ? `inv-chest-quantity-${String(chestCode).replace(/[^a-zA-Z0-9_-]/g, "-")}` : null;
    const batchQuantityInputId = isBatchUsableItem ? `inv-batch-quantity-${String(item.itemType).replace(/[^a-zA-Z0-9_-]/g, "-")}` : null;
    const maxUseQuantity = Math.max(1, Number(item.quantity) || 1);
    const digitamaItem = category === "DIGITAMA" || item.itemType.startsWith("DIGITAMA_");
    const incubatorItem = category === "INCUBATOR" || item.itemType.startsWith("INCUBATOR_");
    const incubationOnly = digitamaItem || incubatorItem;
    const usable = !incubationOnly && (def ? def.usable : invIsUsable(item.itemType));
    const action = isChest && chestCode ? `
      <div class="inventory-chest-controls flex items-center gap-1">
        <input id="${chestQuantityInputId}" class="input inventory-quantity-input text-center" type="number" min="1" max="${maxUseQuantity}" value="1" aria-label="Quantidade de baús" />
        <button class="btn-sm btn-primary inventory-chest-open-btn whitespace-nowrap" onclick="invOpenChest('${escapeHtml(chestCode)}', document.getElementById('${chestQuantityInputId}').value)">Abrir</button>
      </div>
    ` : incubatorItem ? `
      <button class="btn-sm btn-primary whitespace-nowrap" onclick="navigateTo('incubation')">Usar</button>
    ` : isBatchUsableItem ? `
      <div class="flex items-center gap-2">
        <input id="${batchQuantityInputId}" class="input w-16 text-center" type="number" min="1" max="${maxUseQuantity}" value="1" aria-label="Quantidade de ${escapeAttr(name)}" />
        <button class="btn-sm btn-primary whitespace-nowrap" onclick="invUseItem('${escapeHtml(item.itemType)}', document.getElementById('${batchQuantityInputId}').value)">Usar</button>
      </div>
    ` : usable ? `
      <button class="btn-sm btn-primary" onclick="invUseItem('${escapeHtml(item.itemType)}')">Usar</button>
    ` : "";

    return `
      <div class="card-sm mb-2 flex items-center gap-3">
        <div class="shrink-0">${isChest ? renderChestIcon("w-14 h-14") : emoji}</div>
        <div class="flex-1 min-w-0">
          <p class="font-bold text-sm inventory-item-name" title="${escapeAttr(name)}" aria-label="${escapeAttr(name)}">${escapeHtml(name)}</p>
          <div class="flex gap-2 mt-1">
            <span class="text-xs text-slate-400">Qtd: ${item.quantity}</span>
            <span class="badge badge-${catBadge}">${escapeHtml(catName)}</span>
          </div>
        </div>
        ${action}
      </div>
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

async function invUseItem(itemType, quantity = null) {
  if (itemType === "RARITY_REROLL") {
    await invStartRarityReroll();
    return;
  }
  const isXpDiskItem = invIsXpDisk(itemType);
  const isBatchUsableItem = isXpDiskItem || itemType === "POTION_SMALL" || itemType === "TRAINING_STONE";
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
    showToast(err.message, "error");
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
    INCUBATION_SLOT_UNLOCK: "Expansor de Slot de Incubação",
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
    INCUBATOR_COMMON: "📦", INCUBATOR_RARE: "📦", INCUBATOR_EPIC: "📦",
    INCUBATION_SLOT_UNLOCK: "🔓",
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
  const usable = ["POTION_SMALL", "TRAINING_STONE", "DATA_CORE", "INCUBATION_SLOT_UNLOCK",
    "STORAGE_SLOT_1", "STORAGE_SLOT_5", "STORAGE_SLOT_10",
    "XP_DISC_1", "XP_DISC_3", "XP_DISC_5", "XP_DISC_10", "XP_DISC_15", "XP_DISC_20", "RARITY_REROLL"];
  return usable.includes(itemType);
}

function invItemCategory(itemType) {
  if (itemType === "POTION_SMALL" || itemType === "INCUBATION_SLOT_UNLOCK"
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
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">${page.totalElements === 0 ? "Nenhum equipamento no inventário." : "Nenhum equipamento nesta página."}</p>`;
    return;
  }

  content.innerHTML = equipments.map(eq => {
    const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
    const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
    const emoji = slotEmoji[eq.slot] || "⚔️";
    const refLabel = eq.refinementLevel > 0 ? ` +${eq.refinementLevel}` : "";
    const ascensionLevel = Number(eq.ascensionLevel) || 0;
    const ascensionLabel = ascensionLevel > 0 ? `<span class="badge badge-legendary">Ascensão ${ascensionLevel}</span>` : "";
    const canAscend = !eq.equipped && ascensionLevel < 3 && Number(eq.refinementLevel) >= 11;

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
              <p class="font-bold text-sm inventory-item-name" title="${escapeAttr(`${eq.name}${refLabel}`)}" aria-label="${escapeAttr(`${eq.name}${refLabel}`)}">${escapeHtml(eq.name)}${refLabel}</p>
              ${eq.equipped ? '<span class="badge badge-success">Equipado</span>' : ''}
            </div>
            <div class="flex gap-2 mt-1 flex-wrap">
              ${eq.setCode ? `<span class="badge badge-${invSetBadge(eq.setCode)}">${escapeHtml(invSetLabel(eq.setCode))}</span>` : ''}
              <span class="badge badge-${eq.rarity ? eq.rarity.toLowerCase() : 'common'}">T${eq.tier || '?'}</span>
              ${ascensionLabel}
              <span class="text-xs text-slate-500">${slotName[eq.slot] || eq.slot}</span>
            </div>
            ${stats.length > 0 ? `<div class="flex gap-2 mt-1 text-xs font-bold">${stats.join(" ")}</div>` : ""}
          </div>
          <div class="flex flex-col gap-1">
            ${eq.equipped ? `
              <button class="btn-sm" style="background:#7f1d1d;color:#fca5a5" onclick="invUnequip('${eq.id}')">Desequipar</button>
            ` : `
              <button class="btn-sm btn-primary" onclick="invEquip('${eq.id}')">Equipar</button>
              ${canAscend ? `<button class="btn-sm" style="background:#854d0e;color:#fde68a" onclick="invAscend('${eq.id}', ${ascensionLevel + 1})">Ascender</button>` : ''}
            `}
          </div>
        </div>
      </div>
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

async function invAscend(equipmentId, targetLevel) {
  const confirmed = window.confirm(`Realizar Ascensão ${targetLevel}? O equipamento deve estar no refinamento +11 e serão consumidos Núcleos de Ascensão e Bits.`);
  if (!confirmed) return;
  try {
    const result = await apiPost("/equipment/ascend", { equipmentId });
    showToast(result.message || `Ascensão ${targetLevel} realizada!`);
    await invReloadEquipment();
  } catch (err) {
    showToast(err.message, "error");
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
