const shopState = {
  products: [],
  activeOnly: false,
  editing: null,
  equipmentTemplates: [],
  itemDefinitions: [],
  catalogTab: "items",
  catalogSearch: "",
  catalogCategory: "",
  catalogRarity: "",
  catalogSelected: null,
  catalogPage: 1,
  catalogPageSize: 10
};

const PRODUCT_TYPES = ["ITEM", "EQUIPMENT"];
const PRODUCT_CATEGORIES = ["POTION", "MATERIAL", "FRAGMENT", "CONSUMABLE", "CHEST", "EQUIPMENT"];
const ITEM_TYPES = [
  "POTION_SMALL", "TRAINING_STONE", "DATA_CORE",
  "DIGITAMA_STARTER", "DIGITAMA_FIRE", "DIGITAMA_WATER", "DIGITAMA_NATURE",
  "INCUBATOR_COMMON", "INCUBATOR_RARE", "INCUBATOR_EPIC", "INCUBATION_SLOT_UNLOCK",
  "FRAGMENT_ROOKIE", "FRAGMENT_CHAMPION", "FRAGMENT_ULTIMATE", "FRAGMENT_MEGA",
  "EVOLUTION_MATERIAL", "LOOT_CHEST"
];

const SHOP_PRODUCT_TYPE_LABELS = {
  ITEM: "Item",
  EQUIPMENT: "Equipamento"
};

const SHOP_PRODUCT_CATEGORY_LABELS = {
  POTION: "Poção",
  MATERIAL: "Material",
  FRAGMENT: "Fragmento",
  CONSUMABLE: "Consumível",
  CHEST: "Baú",
  EQUIPMENT: "Equipamento",
  DIGITAMA: "Digitama",
  INCUBATOR: "Incubadora",
  EVOLUTION_MATERIAL: "Material de Evolução"
};

const SHOP_ITEM_TYPE_LABELS = {
  POTION_SMALL: "Poção Pequena",
  TRAINING_STONE: "Pedra de Treino",
  DATA_CORE: "Núcleo de Dados",
  DIGITAMA_STARTER: "Digitama Inicial",
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
  LOOT_CHEST: "Baú de Recompensa",
  CHEST_FRAGMENT_ROOKIE: "Baú de Fragmentos - Rookie",
  CHEST_FRAGMENT_CHAMPION: "Baú de Fragmentos - Champion",
  CHEST_FRAGMENT_ULTIMATE: "Baú de Fragmentos - Ultimate",
  CHEST_FRAGMENT_MEGA: "Baú de Fragmentos - Mega"
};

const SHOP_RARITY_LABELS = {
  COMMON: "Comum",
  RARE: "Rara",
  EPIC: "Épica",
  LEGENDARY: "Lendária"
};

const SHOP_SLOT_LABELS = {
  WEAPON: "Arma",
  ARMOR: "Armadura",
  ACCESSORY: "Acessório"
};

function renderShopProductsPage() {
  setPageHeader(
    "Produtos da Loja",
    "Gerencie os produtos da loja do jogo"
  );

  const app = document.getElementById("app");

  app.innerHTML = `
    <div class="card mb-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <label class="flex items-center gap-2 text-sm text-slate-400 cursor-pointer">
            <input type="checkbox" id="shop-active-only" ${shopState.activeOnly ? "checked" : ""}
              onchange="shopToggleActiveFilter()" class="accent-cyan-500" />
            Apenas ativos
          </label>
        </div>

        <button class="btn-primary" onclick="shopShowCreateModal()">
          + Novo Produto
        </button>
      </div>
    </div>

    <div id="shop-result"></div>
    <div id="shop-modal"></div>
  `;

  loadShopProducts();
}

async function loadShopProducts() {
  const container = document.getElementById("shop-result");
  container.innerHTML = `<div class="card"><p class="text-slate-400">Carregando produtos...</p></div>`;

  try {
    const params = {};
    if (shopState.activeOnly) params.activeOnly = "true";

    const products = await apiGet("/admin/shop-products", params);
    shopState.products = products;
    renderShopProductsTable(products);
  } catch (error) {
    container.innerHTML = `
      <div class="card border-red-900 bg-red-950/30">
        <h3 class="font-bold text-red-300 mb-2">Erro ao carregar produtos</h3>
        <p class="text-red-200">${escapeHtml(error.message)}</p>
      </div>
    `;
  }
}

function renderShopProductsTable(products) {
  const container = document.getElementById("shop-result");

  container.innerHTML = `
    <div class="mb-4">
      <h3 class="text-lg font-bold">Produtos da Loja</h3>
      <p class="text-sm text-slate-400">Total: ${products.length}</p>
    </div>

    <div class="table-wrapper">
      <table class="table">
        <thead>
          <tr>
            <th>Código</th>
            <th>Nome</th>
            <th>Tipo</th>
            <th>Categoria</th>
            <th>Item/Template</th>
            <th>Compra</th>
            <th>Venda</th>
            <th>Status</th>
            <th>Atualizado</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          ${products.map(renderShopRow).join("")}
        </tbody>
      </table>
    </div>

    ${products.length === 0 ? '<div class="card mt-4"><p class="text-slate-400">Nenhum produto encontrado.</p></div>' : ""}
  `;

  shopBindTableActions();
}

function renderShopRow(p) {
  const statusClass = p.active ? "badge-success" : "badge-danger";
  const statusText = p.active ? "Ativo" : "Inativo";
  const ref = p.productType === "EQUIPMENT"
    ? p.equipmentTemplateName
    : (p.itemDefinitionCode || p.itemType || "-");

  return `
    <tr>
      <td><span class="font-mono text-cyan-300">${escapeHtml(p.code)}</span></td>
      <td>
        <div class="font-semibold">${escapeHtml(p.name)}</div>
        ${p.description ? `<div class="text-xs text-slate-500 line-clamp-1">${escapeHtml(p.description)}</div>` : ""}
      </td>
      <td><span class="badge">${escapeHtml(shopProductTypeLabel(p.productType))}</span></td>
      <td><span class="badge">${escapeHtml(shopProductCategoryLabel(p.category))}</span></td>
      <td><span class="text-sm text-slate-300">${escapeHtml(shopProductReferenceLabel(p))}</span></td>
      <td>${shopFormatBits(p.price)}</td>
      <td>${shopFormatBits(p.sellPrice)}</td>
      <td><span class="badge ${statusClass}">${statusText}</span></td>
      <td>
        <div class="text-xs text-slate-400">${shopFormatDate(p.updatedAt)}</div>
        <div class="text-xs text-slate-500">por ${escapeHtml(p.updatedBy || "-")}</div>
      </td>
      <td>
        <div class="flex gap-2">
          <button type="button" class="btn-sm btn-secondary js-shop-edit" data-shop-code="${escapeAttr(p.code)}">
            Editar
          </button>
          <button type="button" class="btn-sm ${p.active ? 'btn-warning' : 'btn-success-outline'} js-shop-toggle" data-shop-code="${escapeAttr(p.code)}">
            ${p.active ? "Desativar" : "Ativar"}
          </button>
        </div>
      </td>
    </tr>
  `;
}


function shopBindTableActions() {
  document.querySelectorAll(".js-shop-edit").forEach(button => {
    button.addEventListener("click", () => shopShowEditModal(button.dataset.shopCode));
  });

  document.querySelectorAll(".js-shop-toggle").forEach(button => {
    button.addEventListener("click", () => shopToggleActive(button.dataset.shopCode));
  });
}

function shopToggleActiveFilter() {
  shopState.activeOnly = document.getElementById("shop-active-only").checked;
  loadShopProducts();
}

async function shopShowCreateModal() {
  shopState.editing = null;
  shopState.catalogTab = "items";
  shopState.catalogSearch = "";
  shopState.catalogCategory = "";
  shopState.catalogRarity = "";
  shopState.catalogSelected = null;

  shopRenderCatalogLoading();
  await Promise.all([shopLoadEquipmentTemplates(), shopLoadItemDefinitions()]);
  shopRenderCatalogBrowser();
}

async function shopShowEditModal(code) {
  const product = shopState.products.find(p => p.code === code);
  if (!product) return;
  shopState.editing = code;
  await shopLoadEquipmentTemplates();
  shopRenderModal("Editar Produto", product, true);
}

function shopRenderCatalogLoading() {
  const modal = document.getElementById("shop-modal");
  modal.innerHTML = `
    <div class="modal-overlay" onclick="shopCloseModal()">
      <div class="modal-content modal-catalog" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xl font-bold">Selecionar Produto</h3>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="shopCloseModal()">&times;</button>
        </div>
        <p class="text-slate-400">Carregando catálogo...</p>
      </div>
    </div>
  `;
}

function shopRenderModal(title, data, isEdit) {
  const modal = document.getElementById("shop-modal");
  const readonlyCatalogFields = !isEdit;
  const selectedDetails = data._catalogSource ? shopRenderSelectedCatalogSummary(data._catalogSource) : "";
  const effectiveItemType = data.itemType || (data.itemDefinitionCode && ITEM_TYPES.includes(data.itemDefinitionCode) ? data.itemDefinitionCode : "");

  modal.innerHTML = `
    <div class="modal-overlay" onclick="shopCloseModal()">
      <div class="modal-content" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold">${escapeHtml(title)}</h3>
            ${!isEdit ? '<p class="text-sm text-slate-400 mt-1">Confira os dados preenchidos e informe apenas os preços da loja.</p>' : ''}
          </div>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="shopCloseModal()">&times;</button>
        </div>

        ${selectedDetails}

        <form id="shop-form" onsubmit="shopSubmitForm(event)">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-slate-400">Código</label>
              <input id="shop-code" class="input mt-1" value="${escapeAttr(data.code)}"
                ${isEdit ? "disabled" : "readonly"} required />
            </div>

            <div>
              <label class="text-sm text-slate-400">Nome</label>
              <input id="shop-name" class="input mt-1" value="${escapeAttr(data.name)}" ${readonlyCatalogFields ? "readonly" : ""} required />
            </div>

            <div class="md:col-span-2">
              <label class="text-sm text-slate-400">Descrição</label>
              <input id="shop-description" class="input mt-1" value="${escapeAttr(data.description || "")}" ${readonlyCatalogFields ? "readonly" : ""} />
            </div>

            <div>
              <label class="text-sm text-slate-400">Tipo do Produto</label>
              <select id="shop-product-type" class="input mt-1" onchange="shopToggleTypeFields()" ${readonlyCatalogFields ? "disabled" : ""}>
                ${shopSelectOptions(PRODUCT_TYPES, data.productType, shopProductTypeLabel)}
              </select>
            </div>

            <div>
              <label class="text-sm text-slate-400">Categoria</label>
              <select id="shop-category" class="input mt-1" ${readonlyCatalogFields ? "disabled" : ""}>
                ${shopSelectOptions(PRODUCT_CATEGORIES, data.category, shopProductCategoryLabel)}
              </select>
            </div>

            <div id="shop-item-type-group" class="${data.productType === 'EQUIPMENT' ? 'hidden' : ''}">
              <label class="text-sm text-slate-400">Tipo do item</label>
              <select id="shop-item-type" class="input mt-1" ${readonlyCatalogFields ? "disabled" : ""}>
                <option value="">-- Selecione --</option>
                ${shopSelectOptions(ITEM_TYPES, effectiveItemType, shopItemTypeLabel)}
              </select>
              <p class="text-xs text-slate-500 mt-1">Opcional quando o código da definição identifica um item específico; nesse caso o backend deriva o tipo automaticamente.</p>
              ${data._materialCode ? `<p class="text-xs text-amber-300 mt-1">Material específico detectado: ${escapeHtml(data._materialCode)}.</p>` : ""}
            </div>

            <div id="shop-item-definition-code-group" class="${data.productType === 'EQUIPMENT' ? 'hidden' : ''}">
              <label class="text-sm text-slate-400">Código da definição do item</label>
              <input id="shop-item-definition-code" class="input mt-1" value="${escapeAttr(data.itemDefinitionCode || data._catalogSource?.itemDefinitionCode || '')}"
                ${readonlyCatalogFields ? "readonly" : ""} placeholder="Ex.: CHEST_FRAGMENT_ROOKIE" />
              <p class="text-xs text-slate-500 mt-1">Usado para itens específicos, como baús. Para itens comuns, pode acompanhar o tipo do item.</p>
            </div>

            <div id="shop-eqt-name-group" class="${data.productType === 'ITEM' ? 'hidden' : ''}">
              <label class="text-sm text-slate-400">Modelo de equipamento</label>
              <select id="shop-eqt-name" class="input mt-1" ${readonlyCatalogFields ? "disabled" : ""}>
                <option value="">-- Selecione --</option>
                ${shopState.equipmentTemplates.map(t => `<option value="${escapeAttr(t.name)}" ${t.name === data.equipmentTemplateName ? 'selected' : ''}>${escapeHtml(t.name)} (${escapeHtml(shopSlotLabel(t.slot))} | ${escapeHtml(shopRarityLabel(t.rarity))})</option>`).join('')}
              </select>
            </div>

            <div>
              <label class="text-sm text-slate-400">Preço de compra (Bits)</label>
              <input id="shop-price" type="number" min="0" class="input mt-1" value="${Number(data.price || 0)}" required autofocus />
            </div>

            <div>
              <label class="text-sm text-slate-400">Preço de venda (Bits)</label>
              <input id="shop-sell-price" type="number" min="0" class="input mt-1" value="${Number(data.sellPrice || 0)}" required />
            </div>
          </div>

          <div id="shop-form-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/30 border border-red-900 text-red-200 text-sm"></div>

          <div class="flex gap-3 mt-6">
            ${!isEdit ? '<button type="button" class="btn-secondary" onclick="shopRenderCatalogBrowser()">Voltar ao catálogo</button>' : ''}
            <button type="submit" class="btn-primary flex-1">${isEdit ? "Salvar" : "Adicionar à Loja"}</button>
            <button type="button" class="btn-secondary flex-1" onclick="shopCloseModal()">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  `;
}

function shopToggleTypeFields() {
  const type = document.getElementById("shop-product-type").value;
  document.getElementById("shop-item-type-group").classList.toggle("hidden", type === "EQUIPMENT");
  document.getElementById("shop-item-definition-code-group").classList.toggle("hidden", type === "EQUIPMENT");
  document.getElementById("shop-eqt-name-group").classList.toggle("hidden", type === "ITEM");
}

// --- Catalog Browser ---

function shopRenderCatalogBrowser() {
  const modal = document.getElementById("shop-modal");
  const isItems = shopState.catalogTab === "items";
  const catalog = isItems ? shopFilteredItems() : shopFilteredEquipmentTemplates();
  const totalCatalogItems = catalog.length;
  const totalCatalogPages = Math.max(1, Math.ceil(totalCatalogItems / shopState.catalogPageSize));
  if (shopState.catalogPage > totalCatalogPages) shopState.catalogPage = totalCatalogPages;
  if (shopState.catalogPage < 1) shopState.catalogPage = 1;
  const pageStart = (shopState.catalogPage - 1) * shopState.catalogPageSize;
  const pagedCatalog = catalog.slice(pageStart, pageStart + shopState.catalogPageSize);
  const categories = isItems ? shopUniqueValues(shopState.itemDefinitions, "category") : shopUniqueValues(shopState.equipmentTemplates, "slot");
  const rarities = isItems ? shopUniqueValues(shopState.itemDefinitions, "rarity") : shopUniqueValues(shopState.equipmentTemplates, "rarity");

  modal.innerHTML = `
    <div class="modal-overlay" onclick="shopCloseModal()">
      <div class="modal-content modal-catalog" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-xl font-bold">Selecionar Produto</h3>
            <p class="text-sm text-slate-400 mt-1">Escolha um item ou equipamento já cadastrado para adicionar à loja.</p>
          </div>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="shopCloseModal()">&times;</button>
        </div>

        <div class="flex gap-2 mb-4">
          <button class="catalog-tab ${isItems ? 'catalog-tab-active' : ''}" onclick="shopSetCatalogTab('items')">
            Itens (${shopState.itemDefinitions.length})
          </button>
          <button class="catalog-tab ${!isItems ? 'catalog-tab-active' : ''}" onclick="shopSetCatalogTab('equipments')">
            Equipamentos (${shopState.equipmentTemplates.length})
          </button>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
          <div class="md:col-span-1">
            <label class="text-xs text-slate-500">Buscar</label>
            <input id="shop-catalog-search" class="input mt-1" value="${escapeAttr(shopState.catalogSearch)}"
              placeholder="Nome, código ou descrição" oninput="shopUpdateCatalogFilters()" />
          </div>
          <div>
              <label class="text-xs text-slate-500">${isItems ? 'Categoria' : 'Posição'}</label>
            <select id="shop-catalog-category" class="input mt-1" onchange="shopUpdateCatalogFilters()">
              <option value="">Todos</option>
              ${categories.map(v => `<option value="${escapeAttr(v)}" ${shopState.catalogCategory === v ? 'selected' : ''}>${escapeHtml(isItems ? shopProductCategoryLabel(v) : shopSlotLabel(v))}</option>`).join('')}
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500">Raridade</label>
            <select id="shop-catalog-rarity" class="input mt-1" onchange="shopUpdateCatalogFilters()">
              <option value="">Todas</option>
              ${rarities.map(v => `<option value="${escapeAttr(v)}" ${shopState.catalogRarity === v ? 'selected' : ''}>${escapeHtml(shopRarityLabel(v))}</option>`).join('')}
            </select>
          </div>
        </div>

        <div class="flex items-center justify-between mb-3">
          <p class="text-sm text-slate-400">Exibindo ${totalCatalogItems === 0 ? 0 : pageStart + 1}-${Math.min(pageStart + shopState.catalogPageSize, totalCatalogItems)} de ${totalCatalogItems} resultado(s)</p>
          <button class="btn-sm btn-secondary" onclick="shopClearCatalogFilters()">Limpar filtros</button>
        </div>

        <div class="catalog-list">
          ${totalCatalogItems === 0 ? '<p class="text-slate-500 text-sm">Nenhum resultado encontrado.</p>' : (isItems ? shopRenderItemCatalog(pagedCatalog) : shopRenderEquipmentCatalog(pagedCatalog))}
        </div>

        ${shopRenderCatalogPagination(totalCatalogItems, totalCatalogPages)}
      </div>
    </div>
  `;

  shopBindCatalogActions();
}

function shopRenderCatalogPagination(totalItems, totalPages) {
  if (totalItems <= shopState.catalogPageSize) return '';

  const pages = shopCatalogPageWindow(totalPages);

  return `
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 mt-4 pt-4 border-t border-slate-800">
      <div class="flex items-center gap-2 text-sm text-slate-400">
        <span>Por página</span>
        <select class="input !w-24" onchange="shopSetCatalogPageSize(this.value)">
          ${[5, 10, 20, 50].map(size => `<option value="${size}" ${shopState.catalogPageSize === size ? 'selected' : ''}>${size}</option>`).join('')}
        </select>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <button class="btn-sm btn-secondary" ${shopState.catalogPage === 1 ? 'disabled' : ''} onclick="shopSetCatalogPage(${shopState.catalogPage - 1})">Anterior</button>
        ${pages.map(page => page === '...'
          ? `<span class="px-2 text-slate-500">...</span>`
          : `<button class="btn-sm ${page === shopState.catalogPage ? 'btn-primary' : 'btn-secondary'}" onclick="shopSetCatalogPage(${page})">${page}</button>`
        ).join('')}
        <button class="btn-sm btn-secondary" ${shopState.catalogPage === totalPages ? 'disabled' : ''} onclick="shopSetCatalogPage(${shopState.catalogPage + 1})">Próxima</button>
      </div>
    </div>
  `;
}

function shopCatalogPageWindow(totalPages) {
  const current = shopState.catalogPage;
  if (totalPages <= 7) return Array.from({ length: totalPages }, (_, i) => i + 1);

  const pages = [1];
  const start = Math.max(2, current - 1);
  const end = Math.min(totalPages - 1, current + 1);

  if (start > 2) pages.push('...');
  for (let page = start; page <= end; page++) pages.push(page);
  if (end < totalPages - 1) pages.push('...');
  pages.push(totalPages);

  return pages;
}

function shopSetCatalogPage(page) {
  shopState.catalogPage = Number(page) || 1;
  shopRenderCatalogBrowser();
}

function shopSetCatalogPageSize(size) {
  shopState.catalogPageSize = Number(size) || 10;
  shopState.catalogPage = 1;
  shopRenderCatalogBrowser();
}

function shopResetCatalogPage() {
  shopState.catalogPage = 1;
}
function shopSetCatalogTab(tab) {
  shopState.catalogTab = tab;
  shopState.catalogSearch = "";
  shopState.catalogCategory = "";
  shopState.catalogRarity = "";
  shopState.catalogSelected = null;
  shopResetCatalogPage();
  shopRenderCatalogBrowser();
}

function shopUpdateCatalogFilters() {
  shopState.catalogSearch = document.getElementById("shop-catalog-search").value;
  shopState.catalogCategory = document.getElementById("shop-catalog-category").value;
  shopState.catalogRarity = document.getElementById("shop-catalog-rarity").value;
  shopResetCatalogPage();
  shopRenderCatalogBrowser();
}

function shopClearCatalogFilters() {
  shopState.catalogSearch = "";
  shopState.catalogCategory = "";
  shopState.catalogRarity = "";
  shopResetCatalogPage();
  shopRenderCatalogBrowser();
}

function shopFilteredItems() {
  const term = shopNormalize(shopState.catalogSearch);
  return shopState.itemDefinitions.filter(item => {
    const matchesTerm = !term || [item.name, item.code, item.description, item.category, item.rarity]
      .some(value => shopNormalize(value).includes(term));
    const matchesCategory = !shopState.catalogCategory || item.category === shopState.catalogCategory;
    const matchesRarity = !shopState.catalogRarity || item.rarity === shopState.catalogRarity;
    return matchesTerm && matchesCategory && matchesRarity;
  });
}

function shopFilteredEquipmentTemplates() {
  const term = shopNormalize(shopState.catalogSearch);
  return shopState.equipmentTemplates.filter(t => {
    const matchesTerm = !term || [t.name, t.slot, t.rarity, `HP ${t.bonusHp}`, `ATK ${t.bonusAttack}`, `DEF ${t.bonusDefense}`]
      .some(value => shopNormalize(value).includes(term));
    const matchesCategory = !shopState.catalogCategory || t.slot === shopState.catalogCategory;
    const matchesRarity = !shopState.catalogRarity || t.rarity === shopState.catalogRarity;
    return matchesTerm && matchesCategory && matchesRarity;
  });
}

function shopRenderItemCatalog(items) {
  const existingRefs = new Set();
  shopState.products
    .filter(p => p.productType === "ITEM")
    .forEach(p => {
      existingRefs.add(p.code);
      if (p.itemDefinitionCode) existingRefs.add(p.itemDefinitionCode);
      if (p.code && p.code.startsWith("SHOP_")) {
        existingRefs.add(p.code.substring(5));
      }
      if (p.itemType && p.itemType !== "EVOLUTION_MATERIAL") {
        existingRefs.add(p.itemType);
      }
    });

  return items.map(item => {
    const alreadyInShop = existingRefs.has(item.code);
    const itemArg = shopJsString(item.code);
    const buy = item.buyPrice ?? 0;
    const sell = item.sellPrice ?? 0;

    return `
      <div class="catalog-card group">
        <div class="flex flex-col md:flex-row md:items-start justify-between gap-3">
          <div class="flex-1 min-w-0">
            <div class="flex flex-wrap items-center gap-2">
              <span class="font-semibold text-slate-100">${escapeHtml(item.name)}</span>
              <span class="badge badge-${shopBadgeSuffix(item.rarity || 'common')}">${escapeHtml(shopRarityLabel(item.rarity || 'COMMON'))}</span>
              <span class="badge">${escapeHtml(shopProductCategoryLabel(item.category || '-'))}</span>
              ${alreadyInShop ? '<span class="badge badge-success text-xs">Na loja</span>' : ''}
            </div>
            <div class="text-xs text-slate-500 mt-1 line-clamp-2">${escapeHtml(item.description || 'Sem descrição')}</div>
            <div class="flex flex-wrap gap-x-3 gap-y-1 mt-2 text-xs text-slate-400">
              <span>Código: <span class="font-mono text-cyan-400">${escapeHtml(item.code)}</span></span>
              <span>Compra sugerida: ${shopFormatBits(buy)}</span>
              <span>Venda sugerida: ${shopFormatBits(sell)}</span>
              ${item.stackable ? `<span>Acúmulo máximo: ${escapeHtml(item.maxStack ?? '-')}</span>` : '<span>Não acumula</span>'}
            </div>
          </div>
          <div class="flex gap-2 md:flex-col md:items-stretch">
            <button type="button" class="btn-sm btn-secondary whitespace-nowrap js-shop-item-details" data-item-code="${escapeAttr(item.code)}">Detalhes</button>
            <button type="button" class="btn-sm btn-primary whitespace-nowrap js-shop-item-add ${alreadyInShop ? 'opacity-50 cursor-not-allowed' : ''}"
              ${alreadyInShop ? 'disabled' : ''} data-item-code="${escapeAttr(item.code)}">
              + Adicionar
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function shopRenderEquipmentCatalog(templates) {
  const existingTemplates = new Set(shopState.products.filter(p => p.equipmentTemplateName).map(p => p.equipmentTemplateName));

  return templates.map(t => {
    const alreadyInShop = existingTemplates.has(t.name);
    const templateArg = shopJsString(t.name);
    const suggestedPrice = shopSuggestedEquipmentPrice(t);
    const suggestedSellPrice = Math.floor(suggestedPrice * 0.25);

    return `
      <div class="catalog-card group">
        <div class="flex flex-col md:flex-row md:items-start justify-between gap-3">
          <div class="flex-1 min-w-0">
            <div class="flex flex-wrap items-center gap-2">
              <span class="font-semibold text-slate-100">${escapeHtml(t.name)}</span>
              <span class="badge badge-${shopBadgeSuffix(t.rarity)}">${escapeHtml(shopRarityLabel(t.rarity))}</span>
              <span class="badge">${escapeHtml(shopSlotLabel(t.slot))}</span>
              ${alreadyInShop ? '<span class="badge badge-success text-xs">Na loja</span>' : ''}
            </div>
            <div class="flex flex-wrap gap-x-3 gap-y-1 mt-2 text-xs text-slate-400">
              ${shopStatBadge('Vida', t.bonusHp)}
              ${shopStatBadge('Ataque', t.bonusAttack)}
              ${shopStatBadge('Defesa', t.bonusDefense)}
              <span>Compra sugerida: ${shopFormatBits(suggestedPrice)}</span>
              <span>Venda sugerida: ${shopFormatBits(suggestedSellPrice)}</span>
            </div>
          </div>
          <div class="flex gap-2 md:flex-col md:items-stretch">
            <button type="button" class="btn-sm btn-secondary whitespace-nowrap js-shop-template-details" data-template-name="${escapeAttr(t.name)}">Detalhes</button>
            <button type="button" class="btn-sm btn-primary whitespace-nowrap js-shop-template-add ${alreadyInShop ? 'opacity-50 cursor-not-allowed' : ''}"
              ${alreadyInShop ? 'disabled' : ''} data-template-name="${escapeAttr(t.name)}">
              + Adicionar
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function shopBindCatalogActions() {
  document.querySelectorAll(".js-shop-item-details").forEach(button => {
    button.addEventListener("click", () => shopShowItemDetails(button.dataset.itemCode));
  });

  document.querySelectorAll(".js-shop-item-add").forEach(button => {
    button.addEventListener("click", () => shopAddFromItem(button.dataset.itemCode));
  });

  document.querySelectorAll(".js-shop-template-details").forEach(button => {
    button.addEventListener("click", () => shopShowTemplateDetails(button.dataset.templateName));
  });

  document.querySelectorAll(".js-shop-template-add").forEach(button => {
    button.addEventListener("click", () => shopAddFromTemplate(button.dataset.templateName));
  });
}

function shopShowItemDetails(itemCode) {
  const item = shopState.itemDefinitions.find(i => i.code === itemCode);
  if (!item) return;

  const content = `
    <div class="catalog-card border-cyan-900 bg-cyan-950/20 mb-4">
      <div class="flex items-start justify-between gap-3">
        <div>
          <h4 class="font-bold text-cyan-200">${escapeHtml(item.name)}</h4>
          <p class="text-sm text-slate-400 mt-1">${escapeHtml(item.description || 'Sem descrição')}</p>
        </div>
        <button class="text-slate-400 hover:text-white" onclick="shopRenderCatalogBrowser()">&times;</button>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2 mt-4 text-xs">
        ${shopDetailCell('Código', item.code)}
        ${shopDetailCell('Categoria', shopProductCategoryLabel(item.category || '-'))}
        ${shopDetailCell('Raridade', shopRarityLabel(item.rarity || '-'))}
        ${shopDetailCell('Acúmulo máximo', item.stackable ? (item.maxStack ?? '-') : 'Não acumula')}
        ${shopDetailCell('Usável', item.usable ? 'Sim' : 'Não')}
        ${shopDetailCell('Vendável', item.sellable ? 'Sim' : 'Não')}
        ${shopDetailCell('Negociável', item.tradable ? 'Sim' : 'Não')}
        ${shopDetailCell('Ícone', item.icon || '-')}
      </div>
    </div>
  `;

  shopInsertCatalogDetails(content);
}

function shopShowTemplateDetails(templateName) {
  const t = shopState.equipmentTemplates.find(e => e.name === templateName);
  if (!t) return;

  const content = `
    <div class="catalog-card border-cyan-900 bg-cyan-950/20 mb-4">
      <div class="flex items-start justify-between gap-3">
        <div>
          <h4 class="font-bold text-cyan-200">${escapeHtml(t.name)}</h4>
          <p class="text-sm text-slate-400 mt-1">Modelo de equipamento: ${escapeHtml(shopSlotLabel(t.slot))} — ${escapeHtml(shopRarityLabel(t.rarity))}.</p>
        </div>
        <button class="text-slate-400 hover:text-white" onclick="shopRenderCatalogBrowser()">&times;</button>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2 mt-4 text-xs">
        ${shopDetailCell('Posição', shopSlotLabel(t.slot))}
        ${shopDetailCell('Raridade', shopRarityLabel(t.rarity))}
        ${shopDetailCell('Vida', shopSignedStat(t.bonusHp))}
        ${shopDetailCell('Ataque', shopSignedStat(t.bonusAttack))}
        ${shopDetailCell('Defesa', shopSignedStat(t.bonusDefense))}
        ${shopDetailCell('Status', t.active ? 'Ativo' : 'Inativo')}
        ${shopDetailCell('Atualizado', shopFormatDate(t.updatedAt))}
        ${shopDetailCell('Por', t.updatedBy || '-')}
      </div>
    </div>
  `;

  shopInsertCatalogDetails(content);
}

function shopInsertCatalogDetails(content) {
  const list = document.querySelector(".catalog-list");
  if (!list) return;
  const current = document.getElementById("shop-catalog-details");
  if (current) current.remove();
  const wrapper = document.createElement("div");
  wrapper.id = "shop-catalog-details";
  wrapper.innerHTML = content;
  list.prepend(wrapper);
}

function shopAddFromItem(itemCode) {
  const item = shopState.itemDefinitions.find(i => i.code === itemCode);
  if (!item) return;

  shopState.editing = null;

  const category = shopMapItemCategoryToShopCategory(item.category);
  const itemType = item.category === 'CHEST'
    ? 'LOOT_CHEST'
    : ITEM_TYPES.includes(item.code) ? item.code : 'EVOLUTION_MATERIAL';
  const materialCode = itemType === 'EVOLUTION_MATERIAL' && item.code !== 'EVOLUTION_MATERIAL' ? item.code : null;

  shopRenderModal("Adicionar Item à Loja", {
    code: shopBuildProductCode(item.code),
    name: item.name,
    description: item.description || '',
    productType: 'ITEM',
    category: category,
    itemType: itemType,
    itemDefinitionCode: item.code,
    equipmentTemplateName: '',
    price: item.buyPrice ?? 0,
    sellPrice: item.sellPrice ?? 0,
    _materialCode: materialCode,
    _catalogSource: {
      type: 'ITEM',
      title: item.name,
      subtitle: item.code,
      badges: [shopProductCategoryLabel(item.category), shopRarityLabel(item.rarity), item.stackable ? `Acúmulo máximo: ${item.maxStack ?? '-'}` : 'Não acumula'].filter(Boolean)
    }
  }, false);
}

function shopAddFromTemplate(templateName) {
  const template = shopState.equipmentTemplates.find(t => t.name === templateName);
  if (!template) return;

  shopState.editing = null;
  const suggestedPrice = shopSuggestedEquipmentPrice(template);

  shopRenderModal("Adicionar Equipamento à Loja", {
    code: shopBuildProductCode(template.name),
    name: template.name,
    description: shopBuildEquipmentDescription(template),
    productType: 'EQUIPMENT',
    category: 'EQUIPMENT',
    itemType: '',
    equipmentTemplateName: template.name,
    price: suggestedPrice,
    sellPrice: Math.floor(suggestedPrice * 0.25),
    _catalogSource: {
      type: 'EQUIPMENT',
      title: template.name,
      subtitle: `${shopSlotLabel(template.slot)} | ${shopRarityLabel(template.rarity)}`,
      badges: [shopSignedStat(template.bonusHp, 'Vida'), shopSignedStat(template.bonusAttack, 'Ataque'), shopSignedStat(template.bonusDefense, 'Defesa')].filter(Boolean)
    }
  }, false);
}

async function shopSubmitForm(event) {
  event.preventDefault();

  const errorDiv = document.getElementById("shop-form-error");
  errorDiv.classList.add("hidden");

  const productType = document.getElementById("shop-product-type").value;

  const body = {
    name: document.getElementById("shop-name").value.trim(),
    description: document.getElementById("shop-description").value.trim() || null,
    productType: productType,
    category: document.getElementById("shop-category").value,
    itemType: productType === "ITEM" ? document.getElementById("shop-item-type").value || null : null,
    itemDefinitionCode: productType === "ITEM" ? document.getElementById("shop-item-definition-code").value.trim() || null : null,
    equipmentTemplateName: productType === "EQUIPMENT" ? document.getElementById("shop-eqt-name").value.trim() || null : null,
    price: Number(document.getElementById("shop-price").value),
    sellPrice: Number(document.getElementById("shop-sell-price").value)
  };

  if (Number.isNaN(body.price) || body.price < 0) {
    shopShowFormError(errorDiv, "Preço de compra deve ser maior ou igual a zero.");
    return;
  }

  if (Number.isNaN(body.sellPrice) || body.sellPrice < 0) {
    shopShowFormError(errorDiv, "Preço de venda deve ser maior ou igual a zero.");
    return;
  }

  try {
    if (shopState.editing) {
      await apiPut(`/admin/shop-products/${encodeURIComponent(shopState.editing)}`, body);
    } else {
      body.code = document.getElementById("shop-code").value.trim();
      if (!body.code) throw new Error("Código é obrigatório");
      await apiPost("/admin/shop-products", body);
    }

    shopCloseModal();
    loadShopProducts();
  } catch (error) {
    shopShowFormError(errorDiv, error.message);
  }
}

async function shopToggleActive(code) {
  try {
    await apiPatch(`/admin/shop-products/${encodeURIComponent(code)}/toggle-active`);
    loadShopProducts();
  } catch (error) {
    alert("Erro ao alterar status: " + error.message);
  }
}

function shopCloseModal() {
  document.getElementById("shop-modal").innerHTML = "";
}

function shopSelectOptions(options, selected, labeler = shopOptionLabel) {
  return options.map(o => `<option value="${escapeAttr(o)}" ${o === selected ? "selected" : ""}>${escapeHtml(labeler(o))}</option>`).join("");
}

function shopOptionLabel(value) {
  return String(value || "-");
}

function shopProductTypeLabel(value) {
  return SHOP_PRODUCT_TYPE_LABELS[value] || shopOptionLabel(value);
}

function shopProductCategoryLabel(value) {
  return SHOP_PRODUCT_CATEGORY_LABELS[value] || shopOptionLabel(value);
}

function shopItemTypeLabel(value) {
  return SHOP_ITEM_TYPE_LABELS[value] || shopOptionLabel(value);
}

function shopRarityLabel(value) {
  return SHOP_RARITY_LABELS[value] || shopOptionLabel(value);
}

function shopSlotLabel(value) {
  return SHOP_SLOT_LABELS[value] || shopOptionLabel(value);
}

function shopProductReferenceLabel(product) {
  if (product.productType === "EQUIPMENT") {
    return product.equipmentTemplateName || "-";
  }
  return shopItemTypeLabel(product.itemDefinitionCode || product.itemType);
}

function shopFormatDate(dateStr) {
  if (!dateStr) return "-";
  const d = new Date(dateStr);
  return d.toLocaleDateString("pt-BR") + " " + d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

async function shopLoadEquipmentTemplates() {
  try {
    shopState.equipmentTemplates = await apiGet("/admin/equipment-templates", { activeOnly: "true" });
  } catch (error) {
    console.error("Erro ao carregar equipment templates:", error);
    shopState.equipmentTemplates = [];
  }
}

async function shopLoadItemDefinitions() {
  try {
    const firstPage = await apiGet("/items", { size: "100", page: "0" });
    const allItems = [...(firstPage.items || [])];
    const totalPages = firstPage.totalPages || 1;

    for (let page = 1; page < totalPages; page++) {
      const result = await apiGet("/items", { size: "100", page: String(page) });
      allItems.push(...(result.items || []));
    }

    shopState.itemDefinitions = allItems;
  } catch (error) {
    console.error("Erro ao carregar item definitions:", error);
    shopState.itemDefinitions = [];
  }
}

function shopMapItemCategoryToShopCategory(category) {
  const normalized = String(category || '').toUpperCase();
  const categoryMap = {
    POTION: 'POTION',
    MATERIAL: 'MATERIAL',
    FRAGMENT: 'FRAGMENT',
    CONSUMABLE: 'CONSUMABLE',
    EQUIPMENT: 'EQUIPMENT',
    DIGITAMA: 'CONSUMABLE',
    INCUBATOR: 'CONSUMABLE',
    EVOLUTION_MATERIAL: 'MATERIAL',
    CHEST: 'CHEST'
  };
  return categoryMap[normalized] || 'CONSUMABLE';
}

function shopBuildProductCode(source) {
  const normalized = String(source || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toUpperCase()
    .replace(/[^A-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '');

  if (!normalized) return 'SHOP_PRODUCT';
  return normalized.startsWith('SHOP_') ? normalized : `SHOP_${normalized}`;
}

function shopSuggestedEquipmentPrice(template) {
  const rarityMultiplier = {
    COMMON: 1,
    RARE: 2,
    EPIC: 4,
    LEGENDARY: 8
  }[template.rarity] || 1;

  const statTotal = Number(template.bonusHp || 0) + Number(template.bonusAttack || 0) + Number(template.bonusDefense || 0);
  return Math.max(100, statTotal * 20 * rarityMultiplier);
}

function shopBuildEquipmentDescription(template) {
  const stats = [
    shopSignedStat(template.bonusHp, 'Vida'),
    shopSignedStat(template.bonusAttack, 'Ataque'),
    shopSignedStat(template.bonusDefense, 'Defesa')
  ].filter(Boolean).join(', ');
  return `${shopRarityLabel(template.rarity)} ${shopSlotLabel(template.slot)}${stats ? ` (${stats})` : ''}`;
}

function shopRenderSelectedCatalogSummary(source) {
  return `
    <div class="catalog-card border-cyan-900 bg-cyan-950/20 mb-5">
      <div class="flex items-start justify-between gap-3">
        <div>
          <div class="text-xs text-cyan-300 uppercase tracking-wide">Origem: ${escapeHtml(source.type === 'EQUIPMENT' ? 'Equipamento' : 'Item')}</div>
          <div class="font-bold text-slate-100 mt-1">${escapeHtml(source.title)}</div>
          <div class="text-xs text-slate-500 mt-1">${escapeHtml(source.subtitle || '')}</div>
        </div>
        <div class="flex flex-wrap justify-end gap-1">
          ${(source.badges || []).map(b => `<span class="badge">${escapeHtml(b)}</span>`).join('')}
        </div>
      </div>
    </div>
  `;
}

function shopUniqueValues(items, key) {
  return [...new Set(items.map(item => item[key]).filter(Boolean))].sort((a, b) => String(a).localeCompare(String(b)));
}

function shopDetailCell(label, value) {
  return `
    <div class="bg-slate-950/60 rounded-lg p-2 border border-slate-800">
      <div class="text-slate-500">${escapeHtml(label)}</div>
      <div class="text-slate-200 font-semibold mt-1 break-words">${escapeHtml(value)}</div>
    </div>
  `;
}

function shopStatBadge(label, value) {
  const number = Number(value || 0);
  return number > 0 ? `<span>${label} +${number}</span>` : '';
}

function shopSignedStat(value, label = '') {
  const number = Number(value || 0);
  if (number === 0) return '';
  return `${label ? label + ' ' : ''}${number > 0 ? '+' : ''}${number}`;
}

function shopFormatBits(value) {
  const number = Number(value || 0);
  return `${number.toLocaleString('pt-BR')} Bits`;
}

function shopShowFormError(errorDiv, message) {
  errorDiv.textContent = message;
  errorDiv.classList.remove("hidden");
}

function shopBadgeSuffix(value) {
  return String(value || 'common').toLowerCase().replace(/[^a-z0-9_-]/g, '');
}

function shopNormalize(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();
}

function shopJsString(value) {
  return JSON.stringify(String(value ?? ''));
}

// Explicita as funções usadas por onclick para evitar falhas quando o arquivo é carregado em ambientes que não expõem function declarations no window.
Object.assign(window, {
  renderShopProductsPage,
  loadShopProducts,
  shopToggleActiveFilter,
  shopShowCreateModal,
  shopShowEditModal,
  shopToggleTypeFields,
  shopSetCatalogTab,
  shopUpdateCatalogFilters,
  shopClearCatalogFilters,
  shopSetCatalogPage,
  shopSetCatalogPageSize,
  shopShowItemDetails,
  shopShowTemplateDetails,
  shopAddFromItem,
  shopAddFromTemplate,
  shopSubmitForm,
  shopToggleActive,
  shopCloseModal,
  shopRenderCatalogBrowser
});
