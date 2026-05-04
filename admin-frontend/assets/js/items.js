const itemState = {
    page: 0,
    size: 20,
    category: "",
    rarity: "",
    usable: "",
    sellable: "",
    tradable: "",
    lastResult: null
  };
  
  function renderItemsPage() {
    setPageHeader(
      "Catálogo de Itens",
      "Consulte os itens cadastrados em item_definitions"
    );
  
    const app = document.getElementById("app");
  
    app.innerHTML = `
      <div class="card mb-6">
        <div class="grid grid-cols-1 md:grid-cols-6 gap-4">
  
          <div>
            <label class="text-sm text-slate-400">Categoria</label>
            <input id="filter-category" class="input mt-1" placeholder="Ex: MATERIAL" value="${itemState.category}" />
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Raridade</label>
            <input id="filter-rarity" class="input mt-1" placeholder="Ex: RARE" value="${itemState.rarity}" />
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
          <button class="btn-primary" onclick="applyItemFilters()">
            Buscar
          </button>
  
          <button class="btn-secondary" onclick="clearItemFilters()">
            Limpar filtros
          </button>
        </div>
      </div>
  
      <div id="items-result"></div>
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
          <p class="text-red-200">${error.message}</p>
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
            Total: ${result.totalItems ?? 0} | Página ${result.page + 1} de ${result.totalPages || 1}
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
          <div class="font-mono text-cyan-300">${item.code}</div>
          <div class="text-xs text-slate-500">ID ${item.id}</div>
        </td>
  
        <td>
          <div class="font-semibold">${item.name}</div>
          <div class="text-xs text-slate-500 line-clamp-1">${item.description || "Sem descrição"}</div>
        </td>
  
        <td>
          <span class="badge">${item.category || "-"}</span>
        </td>
  
        <td>
          <span class="badge">${item.rarity || "-"}</span>
        </td>
  
        <td>${formatPrice(item.buyPrice)}</td>
        <td>${formatPrice(item.sellPrice)}</td>
  
        <td>
          ${item.stackable ? `Máx. ${item.maxStack ?? "-"}` : "Não stacka"}
        </td>
  
        <td>
          <div class="flex flex-wrap gap-1">
            ${item.usable ? `<span class="badge">Usável</span>` : ""}
            ${item.sellable ? `<span class="badge">Vendável</span>` : ""}
            ${item.tradable ? `<span class="badge">Trade</span>` : ""}
          </div>
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
    itemState.category = document.getElementById("filter-category").value;
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
  
    return `${value} bits`;
  }