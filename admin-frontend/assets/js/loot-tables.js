const lootTableState = {
  tables: [],
  catalog: [],
  activeOnly: false,
  editing: null,
  modal: null
};

const LOOT_TABLE_RARITIES = [
  { value: "COMMON", label: "Comum", className: "badge-common" },
  { value: "RARE", label: "Rara", className: "badge-rare" },
  { value: "EPIC", label: "Épica", className: "badge-epic" },
  { value: "LEGENDARY", label: "Lendária", className: "badge-legendary" }
];

function renderLootTablesPage() {
  setPageHeader("Loot Tables", "Configure pools nomeadas de recompensas e seus pesos");
  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="card mb-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-bold">Pools de Loot</h3>
          <p class="text-sm text-slate-400 mt-1">A configuração fica restrita ao painel administrativo e não é exibida aos jogadores.</p>
        </div>
        <div class="flex flex-wrap items-center gap-3">
          <label class="flex items-center gap-2 text-sm text-slate-400 cursor-pointer">
            <input type="checkbox" id="loot-active-only" ${lootTableState.activeOnly ? "checked" : ""}
              onchange="lootTableToggleActiveFilter()" class="accent-cyan-500" />
            Apenas ativas
          </label>
          <button class="btn-primary" onclick="lootTableShowCreateModal()">+ Nova Loot Table</button>
        </div>
      </div>
    </div>
    <div id="loot-tables-result"></div>
    <div id="loot-tables-modal"></div>
  `;
  lootTableLoadTables();
}

async function lootTableLoadTables() {
  const container = document.getElementById("loot-tables-result");
  if (!container) return;
  container.innerHTML = `<div class="card"><p class="text-slate-400">Carregando Loot Tables...</p></div>`;

  try {
    lootTableState.tables = await apiGet("/admin/loot-tables", {
      activeOnly: lootTableState.activeOnly ? "true" : ""
    });
    lootTableRenderTable();
  } catch (error) {
    container.innerHTML = lootTableErrorCard("Erro ao carregar Loot Tables", error.message);
  }
}

function lootTableRenderTable() {
  const container = document.getElementById("loot-tables-result");
  if (!container) return;
  const tables = lootTableState.tables || [];

  container.innerHTML = `
    <div class="flex items-center justify-between mb-4">
      <div>
        <h3 class="text-lg font-bold">Loot Tables nomeadas</h3>
        <p class="text-sm text-slate-400">${tables.length} configuração(ões) encontrada(s)</p>
      </div>
    </div>
    <div class="table-wrapper">
      <table class="table">
        <thead>
          <tr>
            <th>Código</th>
            <th>Nome</th>
            <th>Itens por abertura</th>
            <th>Entradas</th>
            <th>Status</th>
            <th>Atualizado</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>${tables.map(lootTableRenderRow).join("")}</tbody>
      </table>
    </div>
    ${tables.length === 0 ? '<div class="card mt-4"><p class="text-slate-400">Nenhuma Loot Table encontrada.</p></div>' : ""}
  `;
  document.querySelectorAll(".js-loot-edit").forEach(button => {
    button.addEventListener("click", () => lootTableShowEditModal(button.dataset.code));
  });
  document.querySelectorAll(".js-loot-toggle").forEach(button => {
    button.addEventListener("click", () => lootTableToggleActive(button.dataset.code));
  });
}

function lootTableRenderRow(table) {
  const statusClass = table.active ? "badge-success" : "badge-danger";
  const statusLabel = table.active ? "Ativa" : "Inativa";
  return `
    <tr>
      <td><span class="font-mono text-cyan-300">${escapeHtml(table.code)}</span></td>
      <td>
        <div class="font-semibold">${escapeHtml(table.name)}</div>
        <div class="text-xs text-slate-500 line-clamp-1">${escapeHtml(table.description || "Sem descrição")}</div>
      </td>
      <td>${table.minItems}–${table.maxItems}</td>
      <td>${(table.entries || []).length}</td>
      <td><span class="badge ${statusClass}">${statusLabel}</span></td>
      <td>
        <div class="text-xs text-slate-400">${lootTableFormatDate(table.updatedAt)}</div>
        <div class="text-xs text-slate-500">por ${escapeHtml(table.updatedBy || "-")}</div>
      </td>
      <td>
        <div class="flex flex-wrap gap-2">
          <button type="button" class="btn-sm btn-secondary js-loot-edit" data-code="${escapeAttr(table.code)}">Editar</button>
          <button type="button" class="btn-sm ${table.active ? "btn-warning" : "btn-success-outline"} js-loot-toggle" data-code="${escapeAttr(table.code)}">
            ${table.active ? "Desativar" : "Ativar"}
          </button>
        </div>
      </td>
    </tr>
  `;
}

function lootTableToggleActiveFilter() {
  lootTableState.activeOnly = document.getElementById("loot-active-only").checked;
  lootTableLoadTables();
}

async function lootTableLoadCatalog() {
  if (lootTableState.catalog.length > 0) return;
  lootTableState.catalog = await apiGet("/admin/loot-tables/catalog/items");
}

async function lootTableShowCreateModal() {
  lootTableState.editing = null;
  try {
    await lootTableLoadCatalog();
    lootTableRenderModal("Nova Loot Table", {
      code: "",
      name: "",
      description: "",
      active: true,
      minItems: 1,
      maxItems: 4,
      rarityWeights: LOOT_TABLE_RARITIES.map(rarity => ({ rarity: rarity.value, weight: 1 })),
      entries: []
    }, false);
  } catch (error) {
    lootTableShowInlineError(error.message);
  }
}

async function lootTableShowEditModal(code) {
  const table = lootTableState.tables.find(item => item.code === code);
  if (!table) return;
  try {
    await lootTableLoadCatalog();
    lootTableState.editing = code;
    lootTableRenderModal("Editar Loot Table", table, true);
  } catch (error) {
    lootTableShowInlineError(error.message);
  }
}

function lootTableRenderModal(title, data, isEdit) {
  const root = document.getElementById("loot-tables-modal");
  if (!root) return;
  lootTableState.modal = data;
  const weights = Object.fromEntries((data.rarityWeights || []).map(weight => [weight.rarity, weight.weight]));
  const entries = data.entries && data.entries.length > 0 ? data.entries : [lootTableEmptyEntry()];

  root.innerHTML = `
    <div class="modal-overlay" onclick="lootTableCloseModal()">
      <div class="modal-content modal-wide" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between gap-4 mb-6">
          <div>
            <h3 class="text-xl font-bold">${escapeHtml(title)}</h3>
            <p class="text-sm text-slate-400 mt-1">Os itens são selecionados exclusivamente do catálogo oficial.</p>
          </div>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="lootTableCloseModal()" aria-label="Fechar">&times;</button>
        </div>

        <form id="loot-table-form" onsubmit="lootTableSubmitForm(event)">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-slate-400">Código técnico</label>
              <input id="loot-code" class="input mt-1 font-mono" value="${escapeAttr(data.code || "")}" ${isEdit ? "disabled" : ""} required />
              <p class="text-xs text-slate-500 mt-1">Use apenas A-Z, números e underscore.</p>
            </div>
            <div>
              <label class="text-sm text-slate-400">Nome administrativo</label>
              <input id="loot-name" class="input mt-1" value="${escapeAttr(data.name || "")}" required />
            </div>
            <div class="md:col-span-2">
              <label class="text-sm text-slate-400">Descrição</label>
              <textarea id="loot-description" class="input mt-1" rows="2">${escapeHtml(data.description || "")}</textarea>
            </div>
            <div>
              <label class="text-sm text-slate-400">Mínimo de tipos por abertura</label>
              <input id="loot-min-items" class="input mt-1" type="number" min="1" max="4" value="${Number(data.minItems || 1)}" required />
            </div>
            <div>
              <label class="text-sm text-slate-400">Máximo de tipos por abertura</label>
              <input id="loot-max-items" class="input mt-1" type="number" min="1" max="4" value="${Number(data.maxItems || 4)}" required />
            </div>
          </div>

          <div class="mt-6">
            <div class="flex items-center justify-between mb-3">
              <div>
                <h4 class="font-bold">Pesos de raridade</h4>
                <p class="text-xs text-slate-500">Os pesos são relativos entre as quatro raridades oficiais.</p>
              </div>
            </div>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
              ${LOOT_TABLE_RARITIES.map(rarity => `
                <label class="card-sm">
                  <span class="badge ${rarity.className}">${rarity.label}</span>
                  <input id="loot-weight-${rarity.value}" class="input mt-2" type="number" min="1" value="${Number(weights[rarity.value] || 1)}" required />
                </label>
              `).join("")}
            </div>
          </div>

          <div class="mt-6">
            <div class="flex items-center justify-between gap-3 mb-3">
              <div>
                <h4 class="font-bold">Entradas da pool</h4>
                <p class="text-xs text-slate-500">Cada item pode ter peso e faixa de quantidade próprios.</p>
              </div>
              <button type="button" class="btn-secondary" onclick="lootTableAddEntryRow()">+ Adicionar item</button>
            </div>
            <div id="loot-entries" class="space-y-3">
              ${entries.map(lootTableRenderEntryRow).join("")}
            </div>
          </div>

          <label class="flex items-center gap-2 mt-5 text-sm text-slate-300">
            <input id="loot-active" type="checkbox" class="accent-cyan-500" ${data.active !== false ? "checked" : ""} />
            Loot Table ativa para uso do jogo
          </label>
          <div id="loot-form-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/30 border border-red-900 text-red-200 text-sm"></div>
          <div class="flex flex-col md:flex-row gap-3 mt-6">
            <button type="submit" class="btn-primary flex-1">Salvar configuração</button>
            <button type="button" class="btn-secondary flex-1" onclick="lootTableCloseModal()">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  `;
}

function lootTableRenderEntryRow(entry) {
  const selectedCode = entry.itemCode || entry.materialCode || entry.itemType || "";
  return `
    <div class="card-sm loot-entry-row" data-entry-id="${escapeAttr(entry.id || "new")}">
      <div class="grid grid-cols-1 md:grid-cols-12 gap-3 items-end">
        <div class="md:col-span-3">
          <label class="text-xs text-slate-500">Item catalogado</label>
          <select class="input mt-1 loot-entry-item" required>
            <option value="">Selecione um item</option>
            ${lootTableState.catalog.map(item => `
              <option value="${escapeAttr(item.code)}" ${item.code === selectedCode ? "selected" : ""}>
                ${escapeHtml(item.name)} — ${escapeHtml(item.code)}
              </option>
            `).join("")}
          </select>
        </div>
        <div class="md:col-span-2">
          <label class="text-xs text-slate-500">Raridade</label>
          <select class="input mt-1 loot-entry-rarity" required>
            ${lootTableRarityOptions(entry.rarity)}
          </select>
        </div>
        <div>
          <label class="text-xs text-slate-500">Peso</label>
          <input class="input mt-1 loot-entry-weight" type="number" min="1" value="${Number(entry.weight || 1)}" required />
        </div>
        <div>
          <label class="text-xs text-slate-500">Mín.</label>
          <input class="input mt-1 loot-entry-min" type="number" min="1" value="${Number(entry.minQuantity || 1)}" required />
        </div>
        <div>
          <label class="text-xs text-slate-500">Máx.</label>
          <input class="input mt-1 loot-entry-max" type="number" min="1" value="${Number(entry.maxQuantity || 1)}" required />
        </div>
        <label class="md:col-span-2 flex items-center gap-2 text-xs text-slate-400 pb-2">
          <input class="loot-entry-active accent-cyan-500" type="checkbox" ${entry.active !== false ? "checked" : ""} /> Ativo
        </label>
        <button type="button" class="btn-sm btn-danger md:col-span-2" onclick="lootTableRemoveEntryRow(this)">Remover</button>
      </div>
    </div>
  `;
}

function lootTableEmptyEntry() {
  return {
    rarity: "COMMON",
    itemType: "",
    materialCode: null,
    itemCode: "",
    weight: 1,
    minQuantity: 1,
    maxQuantity: 1,
    active: true
  };
}

function lootTableAddEntryRow() {
  const container = document.getElementById("loot-entries");
  if (!container) return;
  container.insertAdjacentHTML("beforeend", lootTableRenderEntryRow(lootTableEmptyEntry()));
}

function lootTableRemoveEntryRow(button) {
  const rows = document.querySelectorAll(".loot-entry-row");
  if (rows.length <= 1) {
    lootTableShowFormError("A Loot Table precisa ter pelo menos uma entrada.");
    return;
  }
  button.closest(".loot-entry-row")?.remove();
}

async function lootTableSubmitForm(event) {
  event.preventDefault();
  const formError = document.getElementById("loot-form-error");
  formError.classList.add("hidden");

  try {
    const request = lootTableCollectRequest();
    const code = request.code;
    const result = lootTableState.editing
      ? await apiPut(`/admin/loot-tables/${encodeURIComponent(lootTableState.editing)}`, request)
      : await apiPost("/admin/loot-tables", request);
    lootTableCloseModal();
    await lootTableLoadTables();
    lootTableShowInlineSuccess(`Loot Table ${result.code} salva com sucesso.`);
  } catch (error) {
    lootTableShowFormError(error.message);
  }
}

function lootTableCollectRequest() {
  const code = document.getElementById("loot-code").value.trim();
  const name = document.getElementById("loot-name").value.trim();
  const minItems = Number(document.getElementById("loot-min-items").value);
  const maxItems = Number(document.getElementById("loot-max-items").value);
  if (!code || !name) throw new Error("Código e nome são obrigatórios.");
  if (!/^[A-Z0-9_]+$/.test(code)) throw new Error("O código deve usar apenas A-Z, números e underscore.");
  if (minItems < 1 || maxItems < minItems || maxItems > 4) throw new Error("O intervalo de itens deve ficar entre 1 e 4.");

  const rarityWeights = LOOT_TABLE_RARITIES.map(rarity => ({
    rarity: rarity.value,
    weight: Number(document.getElementById(`loot-weight-${rarity.value}`).value)
  }));
  if (rarityWeights.some(weight => !Number.isInteger(weight.weight) || weight.weight < 1)) {
    throw new Error("Informe pesos positivos para todas as raridades.");
  }

  const rows = [...document.querySelectorAll(".loot-entry-row")];
  if (rows.length === 0) throw new Error("Adicione pelo menos uma entrada.");
  const entries = rows.map(row => {
    const itemCode = row.querySelector(".loot-entry-item").value;
    const catalogItem = lootTableState.catalog.find(item => item.code === itemCode);
    const itemType = lootTableItemType(catalogItem);
    const materialCode = itemType === "EVOLUTION_MATERIAL" || itemType === "LOOT_CHEST" ? itemCode : null;
    const minQuantity = Number(row.querySelector(".loot-entry-min").value);
    const maxQuantity = Number(row.querySelector(".loot-entry-max").value);
    if (!catalogItem) throw new Error("Selecione um item catalogado em todas as entradas.");
    if (!Number.isInteger(minQuantity) || !Number.isInteger(maxQuantity) || minQuantity < 1 || maxQuantity < minQuantity) {
      throw new Error(`Faixa de quantidade inválida para ${catalogItem.name}.`);
    }
    if (catalogItem.maxStack != null && maxQuantity > catalogItem.maxStack) {
      throw new Error(`A quantidade máxima de ${catalogItem.name} excede o limite do catálogo.`);
    }
    return {
      rarity: row.querySelector(".loot-entry-rarity").value,
      itemType,
      materialCode,
      weight: Number(row.querySelector(".loot-entry-weight").value),
      minQuantity,
      maxQuantity,
      active: row.querySelector(".loot-entry-active").checked
    };
  });
  if (entries.some(entry => !Number.isInteger(entry.weight) || entry.weight < 1)) {
    throw new Error("Todas as entradas precisam de peso positivo.");
  }

  return {
    code,
    name,
    description: document.getElementById("loot-description").value.trim() || null,
    minItems,
    maxItems,
    rarityWeights,
    entries,
    active: document.getElementById("loot-active").checked
  };
}

function lootTableItemType(item) {
  if (!item) return "";
  if (item.category === "EVOLUTION_MATERIAL") return "EVOLUTION_MATERIAL";
  if (item.category === "CHEST") return "LOOT_CHEST";
  return item.code;
}

function lootTableRarityOptions(selected) {
  return LOOT_TABLE_RARITIES.map(rarity => `
    <option value="${rarity.value}" ${selected === rarity.value ? "selected" : ""}>${rarity.label}</option>
  `).join("");
}

async function lootTableToggleActive(code) {
  try {
    await apiPatch(`/admin/loot-tables/${encodeURIComponent(code)}/toggle-active`);
    await lootTableLoadTables();
  } catch (error) {
    lootTableShowInlineError(error.message);
  }
}

function lootTableCloseModal() {
  const root = document.getElementById("loot-tables-modal");
  if (root) root.innerHTML = "";
  lootTableState.modal = null;
}

function lootTableShowInlineError(message) {
  const container = document.getElementById("loot-tables-result");
  if (!container) return;
  container.insertAdjacentHTML("afterbegin", lootTableErrorCard("Não foi possível concluir a operação", message));
}

function lootTableShowInlineSuccess(message) {
  const container = document.getElementById("loot-tables-result");
  if (!container) return;
  container.insertAdjacentHTML("afterbegin", `<div class="mb-4 rounded-lg border border-emerald-800 bg-emerald-950/30 px-4 py-3 text-sm text-emerald-200">${escapeHtml(message)}</div>`);
}

function lootTableShowFormError(message) {
  const error = document.getElementById("loot-form-error");
  if (!error) return;
  error.textContent = message;
  error.classList.remove("hidden");
}

function lootTableErrorCard(title, message) {
  return `<div class="card border-red-900 bg-red-950/30 mb-4"><h3 class="font-bold text-red-300 mb-2">${escapeHtml(title)}</h3><p class="text-red-200">${escapeHtml(message)}</p></div>`;
}

function lootTableFormatDate(value) {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("pt-BR");
}
