let storageDigimons = [];
let storageSelectedDigimonIds = new Set();
const STORAGE_PAGE_SIZE = 5;
let storageCurrentPage = 1;

let storageFilterState = {
  search: "",
  stage: "ALL",
  rarity: "ALL",
  sort: "level-desc",
  open: false
};

async function renderStoragePage() {
  storageSelectedDigimonIds.clear();
  storageCurrentPage = 1;
  const app = document.getElementById("app");
  showBottomNav("digimons");
  storageFilterState.open = false;

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between gap-2 mb-4 px-1">
        <div class="flex items-center gap-2 min-w-0">
          <button class="btn-sm" style="background:#334155;color:#94a3b8" onclick="navigateTo('dashboard')">← Voltar</button>
          <h2 class="text-lg font-bold truncate">Armazém Digimon</h2>
        </div>
        <button
          id="storage-config-btn"
          type="button"
          class="btn-sm whitespace-nowrap"
          style="background:#334155;color:#cbd5e1"
          aria-expanded="false"
        >
          ⚙ Configurar
        </button>
      </div>

      <form id="storage-search-form" class="flex flex-col sm:flex-row gap-2 mb-3">
        <input
          id="storage-search"
          class="input flex-1"
          type="search"
          value="${escapeHtml(storageFilterState.search)}"
          placeholder="Pesquisar Digimon por nome..."
          aria-label="Pesquisar Digimon no Armazém Digimon"
        />
        <div class="flex gap-2">
          <button type="submit" class="btn-primary flex-1 sm:flex-none">Buscar</button>
          <button id="storage-clear-search" type="button" class="btn-secondary flex-1 sm:flex-none">Limpar</button>
        </div>
      </form>

      <div id="storage-config-panel" class="card-sm mb-3 hidden">
        <div class="mb-3 rounded-lg border border-slate-700 bg-slate-900/50 px-3 py-2">
          <label class="flex items-center gap-2 text-sm text-slate-300 cursor-pointer">
            <input id="storage-pagination-enabled" type="checkbox" class="accent-cyan-500" checked>
            Usar paginação no armazém
          </label>
          <p class="text-xs text-slate-500 mt-1">Desative para exibir todos os Digimons de uma vez.</p>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <label class="text-xs text-slate-400 flex flex-col min-w-0">
            <span class="min-h-8 leading-4 flex items-start">Estágio</span>
            <select id="storage-stage-filter" class="input mt-1" aria-label="Filtrar por estágio">
              <option value="ALL">Todos os estágios</option>
              <option value="MEGA">Mega</option>
              <option value="ULTIMATE">Ultimate</option>
              <option value="CHAMPION">Champion</option>
              <option value="ROOKIE">Rookie</option>
              <option value="BABY_II">Baby II</option>
              <option value="BABY">Baby</option>
            </select>
          </label>
          <label class="text-xs text-slate-400 flex flex-col min-w-0">
            <span class="min-h-8 leading-4 flex items-start">Raridade</span>
            <select id="storage-rarity-filter" class="input mt-1" aria-label="Filtrar por raridade">
              <option value="ALL">Todas as raridades</option>
              <option value="LEGENDARY">${formatRarity("LEGENDARY")}</option>
              <option value="EPIC">${formatRarity("EPIC")}</option>
              <option value="RARE">${formatRarity("RARE")}</option>
              <option value="COMMON">${formatRarity("COMMON")}</option>
            </select>
          </label>
          <label class="text-xs text-slate-400 flex flex-col min-w-0">
            <span class="min-h-8 leading-4 flex items-start">Ordenar por</span>
            <select id="storage-sort" class="input mt-1" aria-label="Ordenar Armazém Digimon">
              <option value="level-desc">Nível: maior para menor</option>
              <option value="level-asc">Nível: menor para maior</option>
              <option value="stage-desc">Estágio: maior para menor</option>
              <option value="stage-asc">Estágio: menor para maior</option>
              <option value="rarity-desc">Raridade: maior para menor</option>
              <option value="rarity-asc">Raridade: menor para maior</option>
              <option value="name-asc">Nome: A–Z</option>
              <option value="name-desc">Nome: Z–A</option>
            </select>
          </label>
        </div>
        <p id="storage-filter-summary" class="text-xs text-slate-500 mt-3"></p>
      </div>

      <div id="storage-info" class="mb-3"></div>
      <div id="storage-bulk-actions" class="mb-3"></div>
      <div id="storage-list">
        <div class="card animate-pulse"><div class="h-20"></div></div>
      </div>
      <div id="storage-pagination" class="mt-4"></div>
    </div>
  `;

  document
    .getElementById("storage-config-btn")
    ?.addEventListener("click", storageToggleConfig);
  document
    .getElementById("storage-search-form")
    ?.addEventListener("submit", storageSubmitSearch);
  document
    .getElementById("storage-clear-search")
    ?.addEventListener("click", storageClearSearch);
  document
    .getElementById("storage-stage-filter")
    ?.addEventListener("change", (event) => {
      storageFilterState.stage = event.target.value;
      storageCurrentPage = 1;
      storageRenderList();
    });
  document
    .getElementById("storage-rarity-filter")
    ?.addEventListener("change", (event) => {
      storageFilterState.rarity = event.target.value;
      storageCurrentPage = 1;
      storageRenderList();
    });
  document
    .getElementById("storage-sort")
    ?.addEventListener("change", (event) => {
      storageFilterState.sort = event.target.value;
      storageCurrentPage = 1;
      storageRenderList();
    });
  storageSyncFilterControls();
  await loadPlayerPaginationPreference();
  const paginationToggle = document.getElementById("storage-pagination-enabled");
  if (paginationToggle) {
    paginationToggle.checked = playerPaginationEnabled;
    paginationToggle.addEventListener("change", async () => {
      paginationToggle.disabled = true;
      try {
        await savePlayerPaginationPreference(paginationToggle.checked);
        storageCurrentPage = 1;
        storageRenderList();
      } finally {
        paginationToggle.disabled = false;
      }
    });
  }

  try {
    const [stored, dashboard] = await Promise.all([
      apiGet("/digimon/storage"),
      apiGet("/players/me/dashboard")
    ]);

    storageDigimons = Array.isArray(stored) ? stored : [];

    const slotInfo = dashboard.slotInfo;
    const infoEl = document.getElementById("storage-info");
    infoEl.innerHTML = `
      <div class="grid grid-cols-2 gap-2">
        <div class="card-sm text-center">
          <p class="text-xs text-slate-400">Digimons armazenados</p>
          <p class="font-bold text-sm ${slotInfo.storedDigimons >= slotInfo.maxStorageSlots ? 'text-red-400' : 'text-cyan-400'}">${slotInfo.storedDigimons}/${slotInfo.maxStorageSlots}</p>
        </div>
        <div class="card-sm text-center">
          <p class="text-xs text-slate-400">Dados Digitais</p>
          <p class="font-bold text-sm text-cyan-400">${Number(dashboard.digitalData || 0).toLocaleString()}</p>
        </div>
      </div>
    `;

    storageRenderList();
    storageUpdateBulkActions();
  } catch (err) {
    document.getElementById("storage-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function storageToggleConfig() {
  const panel = document.getElementById("storage-config-panel");
  const button = document.getElementById("storage-config-btn");
  if (!panel || !button) return;

  storageFilterState.open = panel.classList.contains("hidden");
  panel.classList.toggle("hidden", !storageFilterState.open);
  button.setAttribute("aria-expanded", String(storageFilterState.open));
}

function storageSubmitSearch(event) {
  event.preventDefault();
  storageFilterState.search = document.getElementById("storage-search")?.value.trim() || "";
  storageCurrentPage = 1;
  storageRenderList();
}

function storageClearSearch() {
  storageFilterState.search = "";
  storageCurrentPage = 1;
  const input = document.getElementById("storage-search");
  if (input) input.value = "";
  storageRenderList();
}

function storageSyncFilterControls() {
  const stage = document.getElementById("storage-stage-filter");
  const rarity = document.getElementById("storage-rarity-filter");
  const sort = document.getElementById("storage-sort");
  if (stage) stage.value = storageFilterState.stage;
  if (rarity) rarity.value = storageFilterState.rarity;
  if (sort) sort.value = storageFilterState.sort;
}

function storageRenderList() {
  const container = document.getElementById("storage-list");
  if (!container) return;

  const filtered = storageGetFilteredDigimons();
  const totalPages = playerPaginationEnabled ? Math.max(1, Math.ceil(filtered.length / STORAGE_PAGE_SIZE)) : 1;
  storageCurrentPage = Math.min(Math.max(1, storageCurrentPage), totalPages);
  const pageStart = playerPaginationEnabled ? (storageCurrentPage - 1) * STORAGE_PAGE_SIZE : 0;
  const pageItems = playerPaginationEnabled ? filtered.slice(pageStart, pageStart + STORAGE_PAGE_SIZE) : filtered;
  const summary = document.getElementById("storage-filter-summary");
  if (summary) {
    summary.textContent = filtered.length === 0
      ? "Nenhum Digimon encontrado."
      : playerPaginationEnabled
        ? `Exibindo ${pageStart + 1}-${Math.min(pageStart + STORAGE_PAGE_SIZE, filtered.length)} de ${filtered.length} Digimon${filtered.length === 1 ? "" : "s"}.`
        : `Exibindo todos os ${filtered.length} Digimon${filtered.length === 1 ? "" : "s"}.`;
  }

  if (filtered.length === 0) {
    container.innerHTML = `<div class="card text-center text-slate-400 text-sm">${storageDigimons.length === 0 ? "Armazém Digimon vazio" : "Nenhum Digimon corresponde aos filtros atuais."}
</div>`;
    storageRenderPagination(0, 1);
    storageUpdateBulkActions();
    return;
  }

  container.innerHTML = pageItems.map(d => {
    const locked = d.locked === true;
    const selected = storageSelectedDigimonIds.has(String(d.id));
    return `
      <div class="card mb-2 flex items-center gap-3 ${locked ? "border-amber-700/70 bg-amber-950/10" : ""}">
        <label class="shrink-0 flex items-center justify-center ${locked ? "cursor-not-allowed" : "cursor-pointer"}" title="${locked ? "Digimon bloqueado contra sacrifício" : "Selecionar Digimon para sacrifício"}">
          <input
            type="checkbox"
            class="storage-sacrifice-checkbox h-5 w-5 accent-cyan-500"
            data-digimon-id="${escapeAttr(d.id)}"
            ${selected ? "checked" : ""}
            ${locked ? "disabled" : ""}
            onchange="storageToggleSelection('${escapeAttr(d.id)}', this.checked)"
            aria-label="Selecionar ${escapeAttr(d.name || "Digimon")} para sacrifício"
          />
        </label>
        ${renderDigimonVisual(d.imageUrl, d.stage, "w-16 h-16", "text-4xl")}
        <div class="flex-1 min-w-0">
          <div class="min-w-0">
            <p class="font-bold text-sm break-words" style="overflow-wrap:anywhere">${escapeHtml(d.name)}</p>
            ${locked ? '<span class="block text-xs text-amber-300 mt-1" title="Protegido contra sacrifício">🔒 Bloqueado</span>' : ""}
          </div>
          <p class="text-xs text-slate-400">Lv.${d.level} | ${escapeHtml(d.stage)} | ${formatRarity(d.rarity)} ${renderRarityDieIndicator(d)}</p>
          ${renderRarityDieDetails(d)}
          <p class="text-xs text-slate-500">HP ${d.hp} ATK ${d.attack} DEF ${d.defense}</p>
          <p class="text-xs ${locked ? "text-amber-300" : "text-cyan-300"} mt-1">${locked ? "Protegido contra sacrifício" : `Sacrifício: +${calculateDigitalDataPreview(d)} Dados Digitais`}</p>
        </div>
        <div class="flex flex-col gap-1">
          <button class="btn-sm"
            style="background:${locked ? "#78350f;color:#fde68a" : "#475569;color:#e2e8f0"}"
            onclick="storageToggleLock('${escapeAttr(d.id)}')"
            aria-label="${locked ? "Desbloquear" : "Bloquear"} ${escapeAttr(d.name || "Digimon")}">
            ${locked ? "🔓 Desbloquear" : "🔒 Bloquear"}
          </button>
          <button class="btn-sm"
            style="background:#065f46;color:#6ee7b7"
            onclick="storageRetrieve('${escapeHtml(d.id)}')">
            Tornar ativo
          </button>
          <button class="btn-sm"
            style="background:#7f1d1d;color:#fecaca${locked ? ";opacity:.55;cursor:not-allowed" : ""}"
            onclick="storageSacrifice('${escapeHtml(d.id)}', '${encodeURIComponent(d.name || "Digimon")}')"
            ${locked ? "disabled" : ""}>
            ${locked ? "Bloqueado" : "Sacrificar"}
          </button>
        </div>
      </div>
    `;
  }).join("");

  storageRenderPagination(filtered.length, totalPages);
  storageUpdateBulkActions();
}

function storageRenderPagination(totalItems, totalPages) {
  const container = document.getElementById("storage-pagination");
  if (!container) return;
  if (!playerPaginationEnabled || totalItems <= STORAGE_PAGE_SIZE) {
    container.innerHTML = "";
    return;
  }

  container.innerHTML = `
    <div class="flex items-center justify-center gap-3">
      <button type="button" class="btn-secondary" onclick="storageGoToPage(${storageCurrentPage - 1})" ${storageCurrentPage === 1 ? "disabled" : ""}>Anterior</button>
      <span class="text-sm text-slate-400 whitespace-nowrap">Página ${storageCurrentPage} de ${totalPages}</span>
      <button type="button" class="btn-secondary" onclick="storageGoToPage(${storageCurrentPage + 1})" ${storageCurrentPage === totalPages ? "disabled" : ""}>Próxima</button>
    </div>
  `;
}

function storageGoToPage(page) {
  if (!playerPaginationEnabled) return;
  const totalPages = Math.max(1, Math.ceil(storageGetFilteredDigimons().length / STORAGE_PAGE_SIZE));
  const nextPage = Math.min(Math.max(1, Number(page) || 1), totalPages);
  if (nextPage === storageCurrentPage) return;
  storageCurrentPage = nextPage;
  storageRenderList();
}

function storageUpdateBulkActions() {
  const container = document.getElementById("storage-bulk-actions");
  if (!container) return;

  storageDigimons.forEach(digimon => {
    if (digimon.locked === true) storageSelectedDigimonIds.delete(String(digimon.id));
  });

  const eligible = storageDigimons.filter(d => d.locked !== true);
  const selected = eligible.filter(d => storageSelectedDigimonIds.has(String(d.id)));
  const count = selected.length;
  const totalReward = selected.reduce((sum, digimon) => sum + calculateDigitalDataPreview(digimon), 0);
  const selectionActions = count === 0 ? "" : `
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mt-3 pt-3 border-t border-cyan-800/60">
          <div>
            <p class="font-bold text-sm text-cyan-200">${count} Digimon${count === 1 ? " selecionado" : "s selecionados"}</p>
            <p class="text-xs text-cyan-300 mt-1">Total estimado: +${totalReward.toLocaleString()} Dados Digitais</p>
          </div>
          <div class="flex gap-2">
            <button type="button" class="btn-secondary" onclick="storageClearSelection()">Limpar</button>
            <button type="button" class="btn-sm" style="background:#7f1d1d;color:#fecaca" onclick="storageSacrificeSelected()">Sacrificar selecionados</button>
          </div>
        </div>
  `;

  container.innerHTML = `
    <div class="card-sm border-slate-700 bg-slate-900/50">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <p class="font-bold text-sm text-slate-200">Seleção em massa</p>
          <p class="text-xs text-slate-400 mt-1">${eligible.length} Digimon${eligible.length === 1 ? " desbloqueado" : "s desbloqueados"} disponível${eligible.length === 1 ? "" : "eis"}. Digimons com cadeado ficam de fora.</p>
        </div>
        <button type="button" class="btn-primary whitespace-nowrap" onclick="storageSelectAllUnlocked()" ${eligible.length === 0 ? "disabled" : ""}>
          Selecionar todos
        </button>
      </div>
      ${selectionActions}
    </div>
  `;
}

function storageSelectAllUnlocked() {
  storageDigimons
    .filter(digimon => digimon.locked !== true)
    .forEach(digimon => storageSelectedDigimonIds.add(String(digimon.id)));
  storageRenderList();
}

function storageToggleSelection(digimonId, selected) {
  const id = String(digimonId);
  const digimon = storageDigimons.find(item => String(item.id) === id);
  if (digimon?.locked === true) {
    storageSelectedDigimonIds.delete(id);
    storageUpdateBulkActions();
    return;
  }
  if (selected) storageSelectedDigimonIds.add(id);
  else storageSelectedDigimonIds.delete(id);
  storageUpdateBulkActions();
}

async function storageToggleLock(digimonId) {
  const id = String(digimonId);
  const digimon = storageDigimons.find(item => String(item.id) === id);
  if (!digimon) return;

  try {
    const updated = await apiPatch(`/digimon/${encodeURIComponent(id)}/lock`, null);
    const locked = updated?.locked === true;
    digimon.locked = locked;
    if (locked) storageSelectedDigimonIds.delete(id);
    showToast(locked ? "Digimon bloqueado contra sacrifício." : "Digimon desbloqueado.");
    storageRenderList();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function storageClearSelection() {
  storageSelectedDigimonIds.clear();
  document.querySelectorAll(".storage-sacrifice-checkbox").forEach(input => { input.checked = false; });
  storageUpdateBulkActions();
}

function storageGetFilteredDigimons() {
  const search = storageNormalize(storageFilterState.search);
  const stage = storageFilterState.stage;
  const rarity = storageFilterState.rarity;

  return [...storageDigimons]
    .filter(digimon => {
      const matchesSearch = !search || [digimon.name, digimon.stage, digimon.rarity]
        .some(value => storageNormalize(value).includes(search));
      const matchesStage = stage === "ALL" || String(digimon.stage || "").toUpperCase() === stage;
      const matchesRarity = rarity === "ALL" || String(digimon.rarity || "").toUpperCase() === rarity;
      return matchesSearch && matchesStage && matchesRarity;
    })
    .sort(storageCompareDigimons);
}

function storageCompareDigimons(a, b) {
  const sort = storageFilterState.sort;
  const aName = String(a.name || "");
  const bName = String(b.name || "");
  let comparison = 0;

  if (sort === "name-asc" || sort === "name-desc") {
    comparison = aName.localeCompare(bName, "pt-BR", { sensitivity: "base" });
    return sort === "name-desc" ? -comparison : comparison;
  }

  if (sort === "level-asc" || sort === "level-desc") {
    comparison = (Number(a.level) || 0) - (Number(b.level) || 0);
    if (sort === "level-desc") comparison = -comparison;
  } else if (sort === "stage-asc" || sort === "stage-desc") {
    comparison = storageStageRank(a.stage) - storageStageRank(b.stage);
    if (sort === "stage-desc") comparison = -comparison;
  } else if (sort === "rarity-asc" || sort === "rarity-desc") {
    comparison = storageRarityRank(a.rarity) - storageRarityRank(b.rarity);
    if (sort === "rarity-desc") comparison = -comparison;
  }

  return comparison || aName.localeCompare(bName, "pt-BR", { sensitivity: "base" });
}

function storageStageRank(stage) {
  return {
    BABY: 1,
    BABY_II: 2,
    ROOKIE: 3,
    CHAMPION: 4,
    ULTIMATE: 5,
    MEGA: 6
  }[String(stage || "").toUpperCase()] || 0;
}

function storageRarityRank(rarity) {
  return {
    COMMON: 1,
    RARE: 2,
    EPIC: 3,
    LEGENDARY: 4
  }[String(rarity || "").toUpperCase()] || 0;
}

function storageNormalize(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

function calculateDigitalDataPreview(digimon) {
  const stageBase = {
    BABY: 1,
    BABY_II: 2,
    ROOKIE: 5,
    CHAMPION: 12,
    ULTIMATE: 30,
    MEGA: 60
  }[digimon.stage] || 1;
  const level = Math.min(Math.max(Number(digimon.level) || 1, 1), 100);
  const ivHp = Math.min(Math.max(Number(digimon.ivHp) || 0, 0), 100);
  const ivAttack = Math.min(Math.max(Number(digimon.ivAttack) || 0, 0), 100);
  const ivDefense = Math.min(Math.max(Number(digimon.ivDefense) || 0, 0), 100);
  const averageIv = Math.floor((ivHp + ivAttack + ivDefense) / 3);
  const levelFactor = 25 + Math.floor((75 * level) / 100);
  const ivFactor = 50 + Math.floor(averageIv / 2);
  return Math.max(1, Math.floor((stageBase * levelFactor * ivFactor) / 10000));
}

async function storageSacrifice(digimonId, encodedDigimonName) {
  const digimon = storageDigimons.find(item => String(item.id) === String(digimonId));
  if (digimon?.locked === true) {
    showToast("Este Digimon está bloqueado e não pode ser sacrificado.", "error");
    return;
  }
  const digimonName = decodeURIComponent(encodedDigimonName || "Digimon");
  const confirmed = await showConfirm(
    `Sacrificar ${digimonName}? Esta ação é permanente e não pode ser desfeita.`,
    {
      title: "Sacrificar Digimon",
      confirmText: "Sacrificar",
      cancelText: "Cancelar",
      danger: true
    }
  );
  if (!confirmed) return;
  try {
    const result = await apiPost(`/digimon/${digimonId}/sacrifice`, {});
    showToast(`Digimon sacrificado. +${result.digitalDataReceived} Dados Digitais.`);
    renderStoragePage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function storageSacrificeSelected() {
  const selected = storageDigimons.filter(d => d.locked !== true && storageSelectedDigimonIds.has(String(d.id)));
  if (selected.length === 0) return;

  const totalReward = selected.reduce((sum, digimon) => sum + calculateDigitalDataPreview(digimon), 0);
  const confirmed = await showConfirm(
    `Sacrificar ${selected.length} Digimons? Esta ação é permanente e não pode ser desfeita.\n\nTotal estimado: +${totalReward.toLocaleString()} Dados Digitais.`,
    {
      title: "Sacrificar Digimons em lote",
      confirmText: "Sacrificar selecionados",
      cancelText: "Cancelar",
      danger: true
    }
  );
  if (!confirmed) return;

  try {
    const result = await apiPost("/digimon/sacrifice/bulk", {
      digimonIds: selected.map(digimon => digimon.id)
    });
    storageSelectedDigimonIds.clear();
    showToast(`${result.sacrificedCount} Digimon${result.sacrificedCount === 1 ? " sacrificado" : "sacrificados"}. +${result.digitalDataReceived} Dados Digitais.`);
    await renderStoragePage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function storageRetrieve(digimonId) {
  try {
    await apiPost(`/digimon/${digimonId}/retrieve`, {});
    showToast("Digimon agora é o parceiro ativo!");
    renderStoragePage();
  } catch (err) {
    showToast(err.message, "error");
  }
}
