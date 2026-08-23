const areaChestState = {
  chests: [],
  lootTables: [],
  activeOnly: false,
  category: "",
  editing: null
};

function renderAreaChestsPage() {
  setPageHeader("Baús Temáticos", "Vincule cada baú do jogo à Loot Table que sorteia seu conteúdo");
  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="card mb-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-bold">Baús Temáticos</h3>
          <p class="text-sm text-slate-400 mt-1">Missões, Arena e Bosses entregam baús; a abertura usa a Loot Table vinculada.</p>
        </div>
          <label class="flex items-center gap-2 text-sm text-slate-400 cursor-pointer">
            <input type="checkbox" id="area-chest-active-only" ${areaChestState.activeOnly ? "checked" : ""}
              onchange="areaChestToggleActiveFilter()" class="accent-cyan-500" />
            Apenas ativos
          </label>
          <label class="flex items-center gap-2 text-sm text-slate-400">
            <span>Origem</span>
            <select id="area-chest-category" class="input py-1" onchange="areaChestSetCategory(this.value)">
              <option value="" ${areaChestState.category === "" ? "selected" : ""}>Todas</option>
              <option value="MISSION" ${areaChestState.category === "MISSION" ? "selected" : ""}>Área / Missão</option>
              <option value="ARENA" ${areaChestState.category === "ARENA" ? "selected" : ""}>Arena</option>
              <option value="BOSS" ${areaChestState.category === "BOSS" ? "selected" : ""}>Boss</option>
            </select>
          </label>
      </div>
    </div>
    <div id="area-chest-result"></div>
    <div id="area-chest-modal"></div>
  `;
  areaChestLoadData();
}

async function areaChestLoadData() {
  const container = document.getElementById("area-chest-result");
  if (!container) return;
  container.innerHTML = `<div class="card"><p class="text-slate-400">Carregando Baús da Área...</p></div>`;
  try {
    const [chests, lootTables] = await Promise.all([
      apiGet("/admin/chests", { activeOnly: areaChestState.activeOnly ? "true" : "" }),
      apiGet("/admin/loot-tables")
    ]);
    areaChestState.chests = chests;
    areaChestState.lootTables = lootTables;
    areaChestRenderTable();
  } catch (error) {
    container.innerHTML = areaChestErrorCard("Erro ao carregar Baús da Área", error.message);
  }
}

function areaChestRenderTable() {
  const container = document.getElementById("area-chest-result");
  if (!container) return;
  const allChests = areaChestState.chests || [];
  const chests = allChests.filter(areaChestMatchesCategory);
  container.innerHTML = `
    <div class="mb-4">
      <h3 class="text-lg font-bold">Catálogo de Baús</h3>
      <p class="text-sm text-slate-400">Exibindo ${chests.length} de ${allChests.length} baú(s)</p>
    </div>
    <div class="table-wrapper">
      <table class="table">
        <thead>
          <tr>
            <th>Baú</th>
            <th>Loot Table vinculada</th>
            <th>Inventário</th>
            <th>Status</th>
            <th>Atualizado</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>${chests.map(areaChestRenderRow).join("")}</tbody>
      </table>
    </div>
    ${chests.length === 0 ? '<div class="card mt-4"><p class="text-slate-400">Nenhum baú encontrado.</p></div>' : ""}
  `;
}

function areaChestRenderRow(chest) {
  const statusClass = chest.active ? "badge-success" : "badge-danger";
  const statusLabel = chest.active ? "Ativo" : "Inativo";
  const tableStatus = chest.lootTableActive ? "" : " (inativa)";
  return `
    <tr>
      <td>
        <div class="font-semibold">${escapeHtml(chest.name)}</div>
        <div class="text-xs text-slate-500 font-mono">${escapeHtml(chest.code)}</div>
      </td>
      <td>
        <div class="font-semibold">${escapeHtml(chest.lootTableName || "Sem Loot Table")}${tableStatus}</div>
        <div class="text-xs text-slate-500 font-mono">${escapeHtml(chest.lootTableCode || "-")}</div>
      </td>
      <td>
        <span class="badge ${chest.tradable ? "badge-success" : "badge-danger"}">${chest.tradable ? "Negociável" : "Não negociável"}</span>
      </td>
      <td><span class="badge ${statusClass}">${statusLabel}</span></td>
      <td>
        <div class="text-xs text-slate-400">${areaChestFormatDate(chest.updatedAt)}</div>
        <div class="text-xs text-slate-500">por ${escapeHtml(chest.updatedBy || "-")}</div>
      </td>
      <td>
        <div class="flex flex-wrap gap-2">
          <button type="button" class="btn-sm btn-secondary" onclick="areaChestShowEditModal('${escapeAttr(chest.code)}')">Editar</button>
          <button type="button" class="btn-sm ${chest.active ? "btn-warning" : "btn-success-outline"}" onclick="areaChestToggleActive('${escapeAttr(chest.code)}')">
            ${chest.active ? "Desativar" : "Ativar"}
          </button>
        </div>
      </td>
    </tr>
  `;
}

function areaChestToggleActiveFilter() {
  areaChestState.activeOnly = document.getElementById("area-chest-active-only").checked;
  areaChestLoadData();
}

function areaChestSetCategory(category) {
  areaChestState.category = category || "";
  areaChestRenderTable();
}

function areaChestMatchesCategory(chest) {
  if (!areaChestState.category) return true;
  const code = String(chest.code || "");
  if (areaChestState.category === "MISSION") return code.startsWith("CHEST_MISSION_");
  if (areaChestState.category === "ARENA") return code.startsWith("CHEST_ARENA_");
  if (areaChestState.category === "BOSS") return code.startsWith("CHEST_BOSS_");
  return true;
}

function areaChestShowEditModal(code) {
  const chest = areaChestState.chests.find(item => item.code === code);
  areaChestState.editing = code;
  if (!chest) return;
  const root = document.getElementById("area-chest-modal");
  const lootTables = areaChestState.lootTables || [];
  const activeTables = lootTables.filter(table => table.active);
  if (chest.lootTableCode && !activeTables.some(table => table.code === chest.lootTableCode)) {
    activeTables.unshift({ code: chest.lootTableCode, name: `${String(chest.lootTableName || chest.lootTableCode)} (inativa)`, active: false });
  }

  root.innerHTML = `
    <div class="modal-overlay" onclick="areaChestCloseModal()">
      <div class="modal-content modal-wide" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold">Editar Baú Temático</h3>
            <p class="text-sm text-slate-400 mt-1">O código e o item de inventário são somente leitura.</p>
          </div>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="areaChestCloseModal()">&times;</button>
        </div>
        <form id="area-chest-form" onsubmit="areaChestSubmitForm(event)">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="text-sm text-slate-400">Código do baú</label>
              <input class="input mt-1 font-mono" value="${escapeAttr(chest.code)}" disabled />
            </div>
            <div>
              <label class="text-sm text-slate-400">Item de inventário</label>
              <input class="input mt-1 font-mono" value="${escapeAttr(chest.itemCode || "-")}" disabled />
            </div>
            <div>
              <label class="text-sm text-slate-400">Nome</label>
              <input id="area-chest-name" class="input mt-1" value="${escapeAttr(chest.name)}" required />
            </div>
            <div>
              <label class="text-sm text-slate-400">Ícone</label>
              <input id="area-chest-icon" class="input mt-1" value="${escapeAttr(chest.icon || "")}" />
            </div>
            <div class="md:col-span-2">
              <label class="text-sm text-slate-400">Descrição</label>
              <textarea id="area-chest-description" class="input mt-1" rows="2">${escapeHtml(chest.description || "")}</textarea>
            </div>
            <div class="md:col-span-2">
              <label class="text-sm text-slate-400">Loot Table ativa</label>
              <select id="area-chest-loot-table" class="input mt-1" required>
                ${activeTables.map(table => `<option value="${escapeAttr(table.code)}" ${table.code === chest.lootTableCode ? "selected" : ""}>${escapeHtml(table.name)} — ${escapeHtml(table.code)}</option>`).join("")}
              </select>
              <p class="text-xs text-slate-500 mt-1">Somente Loot Tables ativas podem ser vinculadas a um baú ativo.</p>
            </div>
          </div>
          <div class="flex flex-wrap gap-5 mt-5 text-sm text-slate-300">
            <label class="flex items-center gap-2"><input id="area-chest-tradable" type="checkbox" class="accent-cyan-500" ${chest.tradable ? "checked" : ""} /> Negociável</label>
            <label class="flex items-center gap-2"><input id="area-chest-active" type="checkbox" class="accent-cyan-500" ${chest.active ? "checked" : ""} /> Ativo</label>
          </div>
          <div id="area-chest-form-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/30 border border-red-900 text-red-200 text-sm"></div>
          <div class="flex flex-col md:flex-row gap-3 mt-6">
            <button type="submit" class="btn-primary flex-1">Salvar configuração</button>
            <button type="button" class="btn-secondary flex-1" onclick="areaChestCloseModal()">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  `;
}

async function areaChestSubmitForm(event) {
  event.preventDefault();
  const error = document.getElementById("area-chest-form-error");
  error.classList.add("hidden");
  const payload = {
    name: document.getElementById("area-chest-name").value.trim(),
    description: document.getElementById("area-chest-description").value.trim() || null,
    icon: document.getElementById("area-chest-icon").value.trim() || null,
    lootTableCode: document.getElementById("area-chest-loot-table").value,
    tradable: document.getElementById("area-chest-tradable").checked,
    active: document.getElementById("area-chest-active").checked
  };
  try {
    await apiPut(`/admin/chests/${encodeURIComponent(areaChestState.editing)}`, payload);
    areaChestCloseModal();
    await areaChestLoadData();
  } catch (requestError) {
    error.textContent = requestError.message;
    error.classList.remove("hidden");
  }
}

async function areaChestToggleActive(code) {
  try {
    await apiPatch(`/admin/chests/${encodeURIComponent(code)}/toggle-active`);
    await areaChestLoadData();
  } catch (error) {
    areaChestShowInlineError(error.message);
  }
}

function areaChestCloseModal() {
  const root = document.getElementById("area-chest-modal");
  if (root) root.innerHTML = "";
  areaChestState.editing = null;
}

function areaChestShowInlineError(message) {
  const container = document.getElementById("area-chest-result");
  if (!container) return;
  container.insertAdjacentHTML("afterbegin", areaChestErrorCard("Não foi possível concluir a operação", message));
}

function areaChestErrorCard(title, message) {
  return `<div class="card border-red-900 bg-red-950/30 mb-4"><h3 class="font-bold text-red-300 mb-2">${escapeHtml(title)}</h3><p class="text-red-200">${escapeHtml(message)}</p></div>`;
}

function areaChestFormatDate(value) {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString("pt-BR");
}
