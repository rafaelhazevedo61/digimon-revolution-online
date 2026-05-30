const shopState = {
  products: [],
  activeOnly: false,
  editing: null
};

const PRODUCT_TYPES = ["ITEM", "EQUIPMENT"];
const PRODUCT_CATEGORIES = ["POTION", "MATERIAL", "FRAGMENT", "CONSUMABLE", "EQUIPMENT"];
const ITEM_TYPES = [
  "POTION_SMALL", "TRAINING_STONE", "DATA_CORE",
  "DIGITAMA_STARTER", "DIGITAMA_FIRE", "DIGITAMA_WATER", "DIGITAMA_NATURE",
  "INCUBATOR_COMMON", "INCUBATOR_RARE", "INCUBATOR_EPIC",
  "FRAGMENT_ROOKIE", "FRAGMENT_CHAMPION", "FRAGMENT_ULTIMATE", "FRAGMENT_MEGA",
  "EVOLUTION_MATERIAL"
];

function renderShopProductsPage() {
  setPageHeader(
    "Shop Products",
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
        <p class="text-red-200">${error.message}</p>
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
}

function renderShopRow(p) {
  const statusClass = p.active ? "badge-success" : "badge-danger";
  const statusText = p.active ? "Ativo" : "Inativo";
  const ref = p.productType === "EQUIPMENT" ? p.equipmentTemplateName : (p.itemType || "-");

  return `
    <tr>
      <td><span class="font-mono text-cyan-300">${p.code}</span></td>
      <td>
        <div class="font-semibold">${p.name}</div>
        ${p.description ? `<div class="text-xs text-slate-500 line-clamp-1">${p.description}</div>` : ""}
      </td>
      <td><span class="badge">${p.productType}</span></td>
      <td><span class="badge">${p.category}</span></td>
      <td><span class="text-sm text-slate-300">${ref}</span></td>
      <td>${p.price} bits</td>
      <td>${p.sellPrice} bits</td>
      <td><span class="badge ${statusClass}">${statusText}</span></td>
      <td>
        <div class="text-xs text-slate-400">${shopFormatDate(p.updatedAt)}</div>
        <div class="text-xs text-slate-500">por ${p.updatedBy || "-"}</div>
      </td>
      <td>
        <div class="flex gap-2">
          <button class="btn-sm btn-secondary" onclick="shopShowEditModal('${p.code.replace(/'/g, "\\'")}')">
            Editar
          </button>
          <button class="btn-sm ${p.active ? 'btn-warning' : 'btn-success-outline'}"
            onclick="shopToggleActive('${p.code.replace(/'/g, "\\'")}')">
            ${p.active ? "Desativar" : "Ativar"}
          </button>
        </div>
      </td>
    </tr>
  `;
}

function shopToggleActiveFilter() {
  shopState.activeOnly = document.getElementById("shop-active-only").checked;
  loadShopProducts();
}

function shopShowCreateModal() {
  shopState.editing = null;
  shopRenderModal("Novo Produto", {
    code: "",
    name: "",
    description: "",
    productType: "ITEM",
    category: "CONSUMABLE",
    itemType: "POTION_SMALL",
    equipmentTemplateName: "",
    price: 0,
    sellPrice: 0
  }, false);
}

function shopShowEditModal(code) {
  const product = shopState.products.find(p => p.code === code);
  if (!product) return;
  shopState.editing = code;
  shopRenderModal("Editar Produto", product, true);
}

function shopRenderModal(title, data, isEdit) {
  const modal = document.getElementById("shop-modal");

  modal.innerHTML = `
    <div class="modal-overlay" onclick="shopCloseModal()">
      <div class="modal-content" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold">${title}</h3>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="shopCloseModal()">&times;</button>
        </div>

        <form id="shop-form" onsubmit="shopSubmitForm(event)">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-slate-400">Código</label>
              <input id="shop-code" class="input mt-1" value="${data.code}"
                ${isEdit ? "disabled" : ""} required />
            </div>

            <div>
              <label class="text-sm text-slate-400">Nome</label>
              <input id="shop-name" class="input mt-1" value="${data.name}" required />
            </div>

            <div class="md:col-span-2">
              <label class="text-sm text-slate-400">Descrição</label>
              <input id="shop-description" class="input mt-1" value="${data.description || ""}" />
            </div>

            <div>
              <label class="text-sm text-slate-400">Tipo do Produto</label>
              <select id="shop-product-type" class="input mt-1" onchange="shopToggleTypeFields()">
                ${shopSelectOptions(PRODUCT_TYPES, data.productType)}
              </select>
            </div>

            <div>
              <label class="text-sm text-slate-400">Categoria</label>
              <select id="shop-category" class="input mt-1">
                ${shopSelectOptions(PRODUCT_CATEGORIES, data.category)}
              </select>
            </div>

            <div id="shop-item-type-group" class="${data.productType === 'EQUIPMENT' ? 'hidden' : ''}">
              <label class="text-sm text-slate-400">Item Type</label>
              <select id="shop-item-type" class="input mt-1">
                <option value="">-- Selecione --</option>
                ${shopSelectOptions(ITEM_TYPES, data.itemType)}
              </select>
            </div>

            <div id="shop-eqt-name-group" class="${data.productType === 'ITEM' ? 'hidden' : ''}">
              <label class="text-sm text-slate-400">Equipment Template Name</label>
              <input id="shop-eqt-name" class="input mt-1" value="${data.equipmentTemplateName || ""}"
                placeholder="Ex: Iron Claw" />
            </div>

            <div>
              <label class="text-sm text-slate-400">Preço de Compra (bits)</label>
              <input id="shop-price" type="number" min="0" class="input mt-1" value="${data.price}" required />
            </div>

            <div>
              <label class="text-sm text-slate-400">Preço de Venda (bits)</label>
              <input id="shop-sell-price" type="number" min="0" class="input mt-1" value="${data.sellPrice}" required />
            </div>
          </div>

          <div id="shop-form-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/30 border border-red-900 text-red-200 text-sm"></div>

          <div class="flex gap-3 mt-6">
            <button type="submit" class="btn-primary flex-1">${isEdit ? "Salvar" : "Criar"}</button>
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
  document.getElementById("shop-eqt-name-group").classList.toggle("hidden", type === "ITEM");
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
    equipmentTemplateName: productType === "EQUIPMENT" ? document.getElementById("shop-eqt-name").value.trim() || null : null,
    price: Number(document.getElementById("shop-price").value),
    sellPrice: Number(document.getElementById("shop-sell-price").value)
  };

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
    errorDiv.textContent = error.message;
    errorDiv.classList.remove("hidden");
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

function shopSelectOptions(options, selected) {
  return options.map(o => `<option value="${o}" ${o === selected ? "selected" : ""}>${o}</option>`).join("");
}

function shopFormatDate(dateStr) {
  if (!dateStr) return "-";
  const d = new Date(dateStr);
  return d.toLocaleDateString("pt-BR") + " " + d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}
