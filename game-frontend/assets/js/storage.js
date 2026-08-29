let storageDigimons = [];
let storageSelectedDigimonIds = new Set();

let storageFilterState = {
  search: "",
  stage: "ALL",
  rarity: "ALL",
  sort: "level-desc",
  open: false
};

async function renderStoragePage() {
  storageSelectedDigimonIds.clear();
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

      <div id="storage-config-panel" class="card-sm mb-3 hidden">
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
      storageRenderList();
    });
  document
    .getElementById("storage-rarity-filter")
    ?.addEventListener("change", (event) => {
      storageFilterState.rarity = event.target.value;
      storageRenderList();
    });
  document
    .getElementById("storage-sort")
    ?.addEventListener("change", (event) => {
      storageFilterState.sort = event.target.value;
      storageRenderList();
    });
  storageSyncFilterControls();

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
  storageRenderList();
}

function storageClearSearch() {
  storageFilterState.search = "";
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
  const total = storageDigimons.length;
  const summary = document.getElementById("storage-filter-summary");
  if (summary) {
    summary.textContent = `Exibindo ${filtered.length} de ${total} Digimon${total === 1 ? "" : "s"}.`;
  }

  if (filtered.length === 0) {
    container.innerHTML = `<div class="card text-center text-slate-400 text-sm">${total === 0 ? "Armazém Digimon vazio" : "Nenhum Digimon corresponde aos filtros atuais."}
</div>`;
    return;
  }

  container.innerHTML = filtered.map(d => `
      <div class="card mb-2 flex items-center gap-3">
        <label class="shrink-0 flex items-center justify-center cursor-pointer" title="Selecionar Digimon para sacrifício">
          <input
            type="checkbox"
            class="storage-sacrifice-checkbox h-5 w-5 accent-cyan-500"
            data-digimon-id="${escapeAttr(d.id)}"
            ${storageSelectedDigimonIds.has(String(d.id)) ? "checked" : ""}
            onchange="storageToggleSelection('${escapeAttr(d.id)}', this.checked)"
            aria-label="Selecionar ${escapeAttr(d.name || "Digimon")} para sacrifício"
          />
        </label>
        ${renderDigimonVisual(d.imageUrl, d.stage, "w-16 h-16", "text-4xl")}
        <div class="flex-1 min-w-0">
          <p class="font-bold text-sm truncate">${escapeHtml(d.name)}</p>
          <p class="text-xs text-slate-400">Lv.${d.level} | ${escapeHtml(d.stage)} | ${formatRarity(d.rarity)} ${renderRarityDieIndicator(d)}</p>
          ${renderRarityDieDetails(d)}
          <p class="text-xs text-slate-500">HP ${d.hp} ATK ${d.attack} DEF ${d.defense}</p>
          <p class="text-xs text-cyan-300 mt-1">Sacrifício: +${calculateDigitalDataPreview(d)} Dados Digitais</p>
        </div>
        <div class="flex flex-col gap-1">
          <button class="btn-sm"
            style="background:#065f46;color:#6ee7b7"
            onclick="storageRetrieve('${escapeHtml(d.id)}')">
            Tornar ativo
          </button>
          <button class="btn-sm"
            style="background:#7f1d1d;color:#fecaca"
            onclick="storageSacrifice('${escapeHtml(d.id)}', '${encodeURIComponent(d.name || "Digimon")}')">
            Sacrificar
          </button>
        </div>
      </div>
    `).join("");

  storageUpdateBulkActions();
}

function storageUpdateBulkActions() {
  const container = document.getElementById("storage-bulk-actions");
  if (!container) return;

  const selected = storageDigimons.filter(d => storageSelectedDigimonIds.has(String(d.id)));
  const count = selected.length;
  const totalReward = selected.reduce((sum, digimon) => sum + calculateDigitalDataPreview(digimon), 0);

  container.innerHTML = count === 0 ? "" : `
    <div class="card-sm border-cyan-700 bg-cyan-950/30">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <p class="font-bold text-sm text-cyan-200">${count} Digimon${count === 1 ? " selecionado" : "s selecionados"}</p>
          <p class="text-xs text-cyan-300 mt-1">Total estimado: +${totalReward.toLocaleString()} Dados Digitais</p>
        </div>
        <div class="flex gap-2">
          <button type="button" class="btn-secondary" onclick="storageClearSelection()">Limpar</button>
          <button type="button" class="btn-sm" style="background:#7f1d1d;color:#fecaca" onclick="storageSacrificeSelected()">Sacrificar selecionados</button>
        </div>
      </div>
    </div>
  `;
}

function storageToggleSelection(digimonId, selected) {
  const id = String(digimonId);
  if (selected) storageSelectedDigimonIds.add(id);
  else storageSelectedDigimonIds.delete(id);
  storageUpdateBulkActions();
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
  const selected = storageDigimons.filter(d => storageSelectedDigimonIds.has(String(d.id)));
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
