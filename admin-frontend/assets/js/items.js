const itemState = {
    page: 0,
    size: 20,
    search: "",
    category: "",
    rarity: "",
    usable: "",
    sellable: "",
    tradable: "",
    lastResult: null,
    editing: null
};

function renderItemsPage() {
    setPageHeader(
        "Catálogo de Itens",
        "Consulte e edite os itens cadastrados em item_definitions"
    );

    const app = document.getElementById("app");

    app.innerHTML = `
      <div class="card mb-6">
        <div class="grid grid-cols-1 md:grid-cols-7 gap-4">
          <div class="md:col-span-2">
            <label class="text-sm text-slate-400">Buscar</label>
            <input id="filter-search" class="input mt-1" placeholder="Nome, código ou descrição"
              value="${itemEscapeAttr(itemState.search)}"
              onkeydown="if (event.key === 'Enter') applyItemFilters()" />
          </div>

          <div>
            <label class="text-sm text-slate-400">Categoria</label>
            <input id="filter-category" class="input mt-1" placeholder="Ex: MATERIAL" value="${itemEscapeAttr(itemState.category)}" />
          </div>

          <div>
            <label class="text-sm text-slate-400">Raridade</label>
            <select id="filter-rarity" class="input mt-1">
              ${rarityOptions(itemState.rarity)}
            </select>
          </div>

          <div>
            <label class="text-sm text-slate-400">Usável</label>
            <select id="filter-usable" class="input mt-1">
              ${booleanOptions(itemState.usable)}
            </select>
          </div>

          <div>
            <label class="text-sm text-slate-400">Vendável</label>
            <select id="filter-sellable" class="input mt-1">
              ${booleanOptions(itemState.sellable)}
            </select>
          </div>

          <div>
            <label class="text-sm text-slate-400">Negociável</label>
            <select id="filter-tradable" class="input mt-1">
              ${booleanOptions(itemState.tradable)}
            </select>
          </div>

          <div>
            <label class="text-sm text-slate-400">Itens por página</label>
            <select id="filter-size" class="input mt-1">
              ${sizeOptions(itemState.size)}
            </select>
          </div>
        </div>

        <div class="flex flex-col md:flex-row gap-3 mt-6">
          <button class="btn-primary" onclick="applyItemFilters()">Buscar</button>
          <button class="btn-secondary" onclick="clearItemFilters()">Limpar filtros</button>
        </div>
      </div>

      <div id="items-result"></div>
      <div id="item-modal"></div>
    `;

    loadItems();
}

async function loadItems() {
    const container = document.getElementById("items-result");

    container.innerHTML = `
      <div class="card">
        <p class="text-slate-400">Carregando itens...</p>
      </div>
    `;

    try {
        const result = await apiGet("/items", {
            search: itemState.search,
            page: itemState.page,
            size: itemState.size,
            category: itemState.category,
            rarity: itemState.rarity,
            usable: itemState.usable,
            sellable: itemState.sellable,
            tradable: itemState.tradable
        });

        itemState.lastResult = result;
        renderItemsResult(result);
    } catch (error) {
        container.innerHTML = `
        <div class="card border-red-900 bg-red-950/30">
          <h3 class="font-bold text-red-300 mb-2">Erro ao carregar itens</h3>
          <p class="text-red-200">${itemEscapeHtml(error.message)}</p>
          <p class="text-sm text-slate-400 mt-4">
            Verifique se o backend está rodando e se o CORS permite acesso deste frontend.
          </p>
        </div>
      `;
    }
}

function renderItemsResult(result) {
    const container = document.getElementById("items-result");
    const items = result.items || [];

    container.innerHTML = `
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-4">
        <div>
          <h3 class="text-lg font-bold">Itens encontrados</h3>
          <p class="text-sm text-slate-400">
            Total: ${result.totalItems ?? 0} | Página ${(result.page ?? 0) + 1} de ${result.totalPages || 1}
          </p>
        </div>

        <div class="flex gap-2">
          <button class="btn-secondary" ${!result.hasPrevious ? "disabled" : ""} onclick="previousItemsPage()">
            Anterior
          </button>
          <button class="btn-secondary" ${!result.hasNext ? "disabled" : ""} onclick="nextItemsPage()">
            Próxima
          </button>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Nome</th>
              <th>Categoria</th>
              <th>Raridade</th>
              <th>Compra</th>
              <th>Venda</th>
              <th>Stack</th>
              <th>Flags</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            ${items.map(renderItemRow).join("")}
          </tbody>
        </table>
      </div>

      ${items.length === 0 ? renderEmptyItems() : ""}
    `;
}

function renderItemRow(item) {
    return `
      <tr>
        <td>
          <div class="font-mono text-cyan-300">${itemEscapeHtml(item.code)}</div>
          <div class="text-xs text-slate-500">ID ${itemEscapeHtml(item.id)}</div>
        </td>

        <td>
          <div class="font-semibold">${itemEscapeHtml(item.name)}</div>
          <div class="text-xs text-slate-500 line-clamp-1">${itemEscapeHtml(item.description || "Sem descrição")}</div>
        </td>

        <td><span class="badge">${itemEscapeHtml(item.category || "-")}</span></td>
        <td><span class="badge">${itemEscapeHtml(item.rarity || "-")}</span></td>
        <td>${formatPrice(item.buyPrice)}</td>
        <td>${formatPrice(item.sellPrice)}</td>
        <td>${item.stackable ? `Máx. ${item.maxStack ?? "-"}` : "Não stacka"}</td>
        <td>
          <div class="flex flex-wrap gap-1">
            ${item.usable ? `<span class="badge">Usável</span>` : ""}
            ${item.sellable ? `<span class="badge">Vendável</span>` : ""}
            ${item.tradable ? `<span class="badge">Negociável</span>` : ""}
          </div>
        </td>
        <td>
          <button class="btn-sm btn-secondary" onclick="openItemEdit(${Number(item.id)})">Editar</button>
        </td>
      </tr>
    `;
}

function renderEmptyItems() {
    return `
      <div class="card mt-4">
        <p class="text-slate-400">Nenhum item encontrado com os filtros atuais.</p>
      </div>
    `;
}

function applyItemFilters() {
    itemState.search = document.getElementById("filter-search").value.trim();
    itemState.category = document.getElementById("filter-category").value.trim();
    itemState.rarity = document.getElementById("filter-rarity").value;
    itemState.usable = document.getElementById("filter-usable").value;
    itemState.sellable = document.getElementById("filter-sellable").value;
    itemState.tradable = document.getElementById("filter-tradable").value;
    itemState.size = Number(document.getElementById("filter-size").value);
    itemState.page = 0;

    loadItems();
}

function clearItemFilters() {
    itemState.page = 0;
    itemState.size = 20;
    itemState.search = "";
    itemState.category = "";
    itemState.rarity = "";
    itemState.usable = "";
    itemState.sellable = "";
    itemState.tradable = "";

    renderItemsPage();
}

function previousItemsPage() {
    if (itemState.page > 0) {
        itemState.page--;
        loadItems();
    }
}

function nextItemsPage() {
    if (itemState.lastResult?.hasNext) {
        itemState.page++;
        loadItems();
    }
}

function openItemEdit(id) {
    const item = (itemState.lastResult?.items || []).find(candidate => Number(candidate.id) === Number(id));
    if (!item) return;

    itemState.editing = item;
    renderItemEditModal(item);
}

function renderItemEditModal(item) {
    const modal = document.getElementById("item-modal");
    if (!modal) return;

    modal.innerHTML = `
      <div class="modal-overlay" onclick="closeItemEdit()">
        <div class="modal-content" onclick="event.stopPropagation()">
          <div class="flex items-center justify-between mb-6">
            <div>
              <h3 class="text-xl font-bold">Editar definição de item</h3>
              <p class="text-sm text-slate-400 mt-1">Atualize os atributos usados pelo inventário, loja, evolução e baús.</p>
            </div>
            <button class="text-slate-400 hover:text-white text-2xl" onclick="closeItemEdit()">&times;</button>
          </div>

          <form onsubmit="submitItemEdit(event, ${Number(item.id)})">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label class="text-sm text-slate-400">Código</label>
                <input class="input mt-1 font-mono" value="${itemEscapeAttr(item.code)}" readonly />
              </div>
              <div>
                <label class="text-sm text-slate-400">ID</label>
                <input class="input mt-1" value="${itemEscapeAttr(item.id)}" readonly />
              </div>

              <div class="md:col-span-2">
                <label class="text-sm text-slate-400">Nome exibido</label>
                <input id="item-edit-name" class="input mt-1" value="${itemEscapeAttr(item.name)}" required maxlength="120" />
              </div>

              <div class="md:col-span-2">
                <label class="text-sm text-slate-400">Descrição</label>
                <textarea id="item-edit-description" class="input mt-1 min-h-24" maxlength="2000">${itemEscapeHtml(item.description || "")}</textarea>
              </div>

              <div>
                <label class="text-sm text-slate-400">Categoria</label>
                <input id="item-edit-category" class="input mt-1" value="${itemEscapeAttr(item.category || "")}" required maxlength="40" />
              </div>
              <div>
                <label class="text-sm text-slate-400">Raridade</label>
                <select id="item-edit-rarity" class="input mt-1">
                  ${editRarityOptions(item.rarity)}
                </select>
              </div>

              <div>
                <label class="text-sm text-slate-400">Preço de compra (Bits)</label>
                <input id="item-edit-buy-price" class="input mt-1" type="number" min="0" value="${item.buyPrice ?? ""}" />
              </div>
              <div>
                <label class="text-sm text-slate-400">Preço de venda (Bits)</label>
                <input id="item-edit-sell-price" class="input mt-1" type="number" min="0" value="${item.sellPrice ?? ""}" />
              </div>

              <div class="md:col-span-2 flex items-center gap-3">
                <input id="item-edit-stackable" type="checkbox" class="accent-cyan-500" ${item.stackable ? "checked" : ""} onchange="toggleItemMaxStack()" />
                <label for="item-edit-stackable" class="text-sm text-slate-300">Permite acumular no inventário</label>
              </div>
              <div>
                <label class="text-sm text-slate-400">Stack máximo</label>
                <input id="item-edit-max-stack" class="input mt-1" type="number" min="1" value="${item.maxStack ?? ""}" ${item.stackable ? "" : "disabled"} />
              </div>
              <div>
                <label class="text-sm text-slate-400">Ícone</label>
                <input id="item-edit-icon" class="input mt-1" value="${itemEscapeAttr(item.icon || "")}" maxlength="120" placeholder="Ex.: chest_fragment_rookie" />
              </div>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 mt-5 p-3 rounded-lg border border-slate-800 bg-slate-950/40">
              ${itemFlagCheckbox("item-edit-usable", "Usável", item.usable)}
              ${itemFlagCheckbox("item-edit-sellable", "Vendável", item.sellable)}
              ${itemFlagCheckbox("item-edit-tradable", "Negociável", item.tradable)}
            </div>

            <div id="item-edit-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/30 border border-red-900 text-red-200 text-sm"></div>

            <div class="flex gap-3 mt-6">
              <button type="submit" class="btn-primary flex-1">Salvar alterações</button>
              <button type="button" class="btn-secondary flex-1" onclick="closeItemEdit()">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    `;
}

function itemFlagCheckbox(id, label, checked) {
    return `
      <label class="flex items-center gap-2 text-sm text-slate-300">
        <input id="${id}" type="checkbox" class="accent-cyan-500" ${checked ? "checked" : ""} />
        ${label}
      </label>
    `;
}

function toggleItemMaxStack() {
    const checkbox = document.getElementById("item-edit-stackable");
    const input = document.getElementById("item-edit-max-stack");
    if (!checkbox || !input) return;

    input.disabled = !checkbox.checked;
    if (checkbox.checked && !input.value) input.value = "999";
}

async function submitItemEdit(event, id) {
    event.preventDefault();

    const stackable = document.getElementById("item-edit-stackable").checked;
    const maxStackValue = document.getElementById("item-edit-max-stack").value;
    const maxStack = stackable ? Number(maxStackValue) : null;
    const errorDiv = document.getElementById("item-edit-error");

    if (stackable && (!Number.isInteger(maxStack) || maxStack < 1)) {
        showItemEditError(errorDiv, "Informe um stack máximo maior que zero para itens acumuláveis.");
        return;
    }

    const body = {
        name: document.getElementById("item-edit-name").value.trim(),
        description: document.getElementById("item-edit-description").value.trim() || null,
        category: document.getElementById("item-edit-category").value.trim().toUpperCase(),
        stackable,
        buyPrice: nullableNumber("item-edit-buy-price"),
        sellPrice: nullableNumber("item-edit-sell-price"),
        tradable: document.getElementById("item-edit-tradable").checked,
        sellable: document.getElementById("item-edit-sellable").checked,
        usable: document.getElementById("item-edit-usable").checked,
        maxStack,
        rarity: document.getElementById("item-edit-rarity").value,
        icon: document.getElementById("item-edit-icon").value.trim() || null
    };

    if (!body.name || !body.category) {
        showItemEditError(errorDiv, "Nome e categoria são obrigatórios.");
        return;
    }

    try {
        await apiPut(`/admin/items/${encodeURIComponent(id)}`, body);
        closeItemEdit();
        await loadItems();
        alert("Item atualizado com sucesso.");
    } catch (error) {
        showItemEditError(errorDiv, error.message);
    }
}

function nullableNumber(id) {
    const value = document.getElementById(id).value.trim();
    return value === "" ? null : Number(value);
}

function showItemEditError(errorDiv, message) {
    if (!errorDiv) return;
    errorDiv.textContent = message;
    errorDiv.classList.remove("hidden");
}

function closeItemEdit() {
    const modal = document.getElementById("item-modal");
    if (modal) modal.innerHTML = "";
    itemState.editing = null;
}

function booleanOptions(selectedValue) {
    const options = [
        { label: "Todos", value: "" },
        { label: "Sim", value: "true" },
        { label: "Não", value: "false" }
    ];

    return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
}

function sizeOptions(selectedValue) {
    const options = [10, 20, 50, 100];

    return options.map(size => `
      <option value="${size}" ${Number(selectedValue) === size ? "selected" : ""}>
        ${size}
      </option>
    `).join("");
}

function formatPrice(value) {
    if (value === null || value === undefined) {
        return "-";
    }

    return `${Number(value).toLocaleString("pt-BR")} Bits`;
}

function rarityOptions(selectedValue) {
    const options = [
        { label: "Todas", value: "" },
        { label: "Comum", value: "COMMON" },
        { label: "Rara", value: "RARE" },
        { label: "Épica", value: "EPIC" },
        { label: "Lendária", value: "LEGENDARY" }
    ];

    return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
}

function editRarityOptions(selectedValue) {
    const options = [
        { label: "Comum", value: "COMMON" },
        { label: "Rara", value: "RARE" },
        { label: "Épica", value: "EPIC" },
        { label: "Lendária", value: "LEGENDARY" }
    ];

    return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
}

function itemEscapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function itemEscapeAttr(value) {
    return itemEscapeHtml(value).replace(/\r?\n/g, "&#10;");
}
