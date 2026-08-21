const missionState = {
  missions: [],
  activeOnly: false,
  editing: null,
  filterArea: "",
  filterStage: "",
  filterChest: "",
  chestOptions: []
};

const AREAS = ["NATIVE_FOREST", "GEAR_SAVANNA", "FACTORIAL_TOWN", "FREEZELAND", "SERVER_DESERT", "INFINITY_MOUNTAIN"];
const AREA_LABELS = {
  NATIVE_FOREST: "Floresta Nativa",
  GEAR_SAVANNA: "Savana Gear",
  FACTORIAL_TOWN: "Cidade Fatorial",
  FREEZELAND: "Terra Congelada",
  SERVER_DESERT: "Deserto Server",
  INFINITY_MOUNTAIN: "Montanha Infinita"
};
const STAGES = ["BABY", "BABY_II", "ROOKIE", "CHAMPION", "ULTIMATE", "MEGA"];
const STAGE_LABELS = {
  BABY: "Baby",
  BABY_II: "Baby II",
  ROOKIE: "Rookie",
  CHAMPION: "Champion",
  ULTIMATE: "Ultimate",
  MEGA: "Mega"
};

function renderMissionsPage() {
  setPageHeader("Missions", "Gerencie as missões do jogo");

  const app = document.getElementById("app");

  app.innerHTML = `
    <div class="card mb-6">
      <div class="flex flex-col gap-4">
        <div class="flex flex-wrap items-center gap-4">
          <div>
            <label class="text-xs text-slate-500 block mb-1">Área</label>
            <select id="mission-filter-area" class="input input-sm" onchange="missionApplyFilters()">
              <option value="">Todas</option>
              ${AREAS.map(a => `<option value="${a}" ${missionState.filterArea === a ? "selected" : ""}>${AREA_LABELS[a]}</option>`).join("")}
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500 block mb-1">Stage</label>
            <select id="mission-filter-stage" class="input input-sm" onchange="missionApplyFilters()">
              <option value="">Todos</option>
              ${STAGES.map(s => `<option value="${s}" ${missionState.filterStage === s ? "selected" : ""}>${STAGE_LABELS[s]}</option>`).join("")}
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500 block mb-1">Baú da Área</label>
            <select id="mission-filter-chest" class="input input-sm" onchange="missionApplyFilters()">
              <option value="">Todos</option>
              ${missionState.chestOptions.map(chest => `<option value="${missionEscapeAttr(chest.code)}" ${missionState.filterChest === chest.code ? "selected" : ""}>${missionEscapeHtml(chest.name)}</option>`).join("")}
            </select>
          </div>
          <div class="flex items-end h-full">
            <label class="flex items-center gap-2 text-sm text-slate-400 cursor-pointer mt-5">
              <input type="checkbox" id="mission-active-only" ${missionState.activeOnly ? "checked" : ""}
                onchange="missionApplyFilters()" class="accent-cyan-500" />
              Apenas ativas
            </label>
          </div>
        </div>
        <div class="flex justify-end">
          <button class="btn-primary" onclick="missionShowCreateModal()">
            + Nova Missão
          </button>
        </div>
      </div>
    </div>

    <div id="mission-result"></div>
    <div id="mission-modal"></div>
  `;

  loadMissionOptions().then(loadMissions);
}

async function loadMissionOptions() {
  if (missionState.chestOptions.length > 0) return;
  try {
    missionState.chestOptions = await apiGet("/admin/missions/chest-options");
  } catch (error) {
    missionState.chestOptions = [];
    console.error("Não foi possível carregar os Baús da Área", error);
  }
}

async function loadMissions() {
  const container = document.getElementById("mission-result");
  container.innerHTML = `<div class="card"><p class="text-slate-400">Carregando missões...</p></div>`;

  try {
    const params = {};
    if (missionState.activeOnly) params.activeOnly = "true";
    if (missionState.filterArea) params.area = missionState.filterArea;
    if (missionState.filterStage) params.stage = missionState.filterStage;
    if (missionState.filterChest) params.chestCode = missionState.filterChest;

    const missions = await apiGet("/admin/missions", params);
    missionState.missions = missions;
    renderMissionsTable(missions);
  } catch (error) {
    container.innerHTML = `
      <div class="card border-red-900 bg-red-950/30">
        <h3 class="font-bold text-red-300 mb-2">Erro ao carregar missões</h3>
        <p class="text-red-200">${error.message}</p>
      </div>
    `;
  }
}

function renderMissionsTable(missions) {
  const container = document.getElementById("mission-result");

  container.innerHTML = `
    <div class="mb-4">
      <h3 class="text-lg font-bold">Definições de Missão</h3>
      <p class="text-sm text-slate-400">Total: ${missions.length}</p>
    </div>

    <div class="table-wrapper">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nome</th>
            <th>Área</th>
            <th>Stage</th>
            <th>Nível</th>
            <th>XP</th>
            <th>Bits</th>
            <th>Energia</th>
            <th>Duração</th>
            <th>Baú da Área</th>
            <th>Loot Table</th>
            <th>Status</th>
            <th>Atualizado</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          ${missions.map(renderMissionRow).join("")}
        </tbody>
      </table>
    </div>

    ${missions.length === 0 ? '<div class="card mt-4"><p class="text-slate-400">Nenhuma missão encontrada.</p></div>' : ""}
  `;
}

function renderMissionRow(m) {
  const statusClass = m.active ? "badge-success" : "badge-danger";
  const statusText = m.active ? "Ativa" : "Inativa";
  const areaLabel = AREA_LABELS[m.area] || m.area;
  const chestLabel = m.chestName || "Sem Baú da Área";

  return `
    <tr>
      <td class="text-xs font-mono text-slate-400">${m.id}</td>
      <td class="font-semibold">${m.name}</td>
      <td><span class="badge badge-area">${areaLabel}</span></td>
      <td><span class="badge">${m.requiredStage}</span></td>
      <td>${m.requiredLevel}</td>
      <td>${m.baseXp}</td>
      <td>${m.baseBits ?? 0}</td>
      <td>${m.energyCost}</td>
      <td>${m.durationSeconds}s</td>
      <td>
        <div class="font-semibold">${missionEscapeHtml(chestLabel)}</div>
        <div class="text-xs text-slate-500 font-mono">${missionEscapeHtml(m.chestCode || "-")}</div>
      </td>
      <td>
        <div class="font-semibold">${missionEscapeHtml(m.chestLootTableName || "-")}</div>
        <div class="text-xs text-slate-500 font-mono">${missionEscapeHtml(m.chestLootTableCode || "-")}</div>
      </td>
      <td><span class="badge ${statusClass}">${statusText}</span></td>
      <td>
        <div class="text-xs text-slate-400">${missionFormatDate(m.updatedAt)}</div>
        <div class="text-xs text-slate-500">por ${m.updatedBy || "-"}</div>
      </td>
      <td>
        <div class="flex gap-2">
          <button class="btn-sm btn-secondary" onclick="missionShowEditModal('${m.id}')">
            Editar
          </button>
          <button class="btn-sm ${m.active ? 'btn-warning' : 'btn-success-outline'}"
            onclick="missionToggleActive('${m.id}')">
            ${m.active ? "Desativar" : "Ativar"}
          </button>
        </div>
      </td>
    </tr>
  `;
}

function missionApplyFilters() {
  missionState.activeOnly = document.getElementById("mission-active-only").checked;
  missionState.filterArea = document.getElementById("mission-filter-area").value;
  missionState.filterStage = document.getElementById("mission-filter-stage").value;
  missionState.filterChest = document.getElementById("mission-filter-chest").value;
  loadMissions();
}

async function missionShowCreateModal() {
  await loadMissionOptions();
  missionState.editing = null;
  missionRenderModal("Nova Missão", {
    id: "",
    name: "",
    description: "",
    area: "NATIVE_FOREST",
    requiredStage: "ROOKIE",
    requiredLevel: 1,
    baseXp: 100,
    baseBits: 50,
    energyCost: 5,
    durationSeconds: 60,
    chestCode: missionState.chestOptions[0]?.code || "",
    chestName: missionState.chestOptions[0]?.name || ""
  }, false);
}

async function missionShowEditModal(id) {
  const mission = missionState.missions.find(m => m.id === id);
  if (!mission) return;
  await loadMissionOptions();
  missionState.editing = id;
  missionRenderModal("Editar Missão", mission, true);
}

function missionRenderModal(title, data, isEdit) {
  const modal = document.getElementById("mission-modal");

  modal.innerHTML = `
    <div class="modal-overlay" onclick="missionCloseModal()">
      <div class="modal-content modal-wide" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold">${title}</h3>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="missionCloseModal()">&times;</button>
        </div>

        <form id="mission-form" onsubmit="missionSubmitForm(event)">
          <!-- Basic fields -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div>
              <label class="text-sm text-slate-400">ID</label>
              <input id="mission-id" class="input mt-1" value="${data.id}"
                ${isEdit ? "disabled" : ""} required placeholder="EX: MISSION_8" />
            </div>
            <div class="md:col-span-2">
              <label class="text-sm text-slate-400">Nome</label>
              <input id="mission-name" class="input mt-1" value="${data.name}" required />
            </div>
            <div class="md:col-span-3">
              <label class="text-sm text-slate-400">Descrição</label>
              <textarea id="mission-desc" class="input mt-1" rows="2">${data.description || ""}</textarea>
            </div>
          </div>

          <div class="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
            <div>
              <label class="text-sm text-slate-400">Área</label>
              <select id="mission-area" class="input mt-1">
                ${missionSelectOptions(AREAS, data.area, AREA_LABELS)}
              </select>
            </div>
            <div>
              <label class="text-sm text-slate-400">Stage Mínimo</label>
              <select id="mission-stage" class="input mt-1">
                ${missionSelectOptions(STAGES, data.requiredStage, STAGE_LABELS)}
              </select>
            </div>
            <div>
              <label class="text-sm text-slate-400">Nível Mínimo</label>
              <input id="mission-level" type="number" min="1" class="input mt-1" value="${data.requiredLevel}" required />
            </div>
            <div>
              <label class="text-sm text-slate-400">XP Base</label>
              <input id="mission-xp" type="number" min="0" class="input mt-1" value="${data.baseXp}" required />
            </div>
            <div>
              <label class="text-sm text-slate-400">Bits Base</label>
              <input id="mission-bits" type="number" min="0" class="input mt-1" value="${data.baseBits ?? 0}" required />
            </div>
            <div>
              <label class="text-sm text-slate-400">Custo de Energia</label>
              <input id="mission-energy" type="number" min="1" class="input mt-1" value="${data.energyCost}" required />
            </div>
            <div>
              <label class="text-sm text-slate-400">Duração (segundos)</label>
              <input id="mission-duration" type="number" min="1" class="input mt-1" value="${data.durationSeconds}" required />
            </div>
          </div>

          <div class="card-sm mb-6 border-cyan-800">
            <div class="flex items-start justify-between gap-4">
              <div>
                <h4 class="font-bold text-sm text-cyan-300">Baú da Área</h4>
                <p class="text-xs text-slate-400 mt-1">A missão entrega o baú; o loot é sorteado pela Loot Table vinculada durante a abertura.</p>
              </div>
              <span class="badge badge-area">Novo fluxo</span>
            </div>
            <label class="text-sm text-slate-400 block mt-4">Baú vinculado</label>
            <select id="mission-chest-code" class="input mt-1" required onchange="missionUpdateChestPreview()">
              ${missionRenderChestOptions(data.chestCode, data.chestName)}
            </select>
            <div id="mission-chest-preview" class="mt-3 text-xs text-slate-400"></div>
            <p class="text-xs text-slate-500 mt-3">Para editar pesos e itens da pool, use o menu <strong>Loot Tables</strong>.</p>
          </div>

          <div id="mission-form-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/30 border border-red-900 text-red-200 text-sm"></div>

          <div class="flex gap-3 mt-6">
            <button type="submit" class="btn-primary flex-1">${isEdit ? "Salvar" : "Criar"}</button>
            <button type="button" class="btn-secondary flex-1" onclick="missionCloseModal()">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  `;
  missionUpdateChestPreview();
}

function missionRenderChestOptions(selectedCode, selectedName) {
  const options = missionState.chestOptions.map(chest => `
    <option value="${missionEscapeAttr(chest.code)}" ${chest.code === selectedCode ? "selected" : ""}>
      ${missionEscapeHtml(chest.name)} — ${missionEscapeHtml(chest.lootTableName || "Loot Table não informada")}
    </option>
  `);
  if (selectedCode && !missionState.chestOptions.some(chest => chest.code === selectedCode)) {
    options.unshift(`<option value="${missionEscapeAttr(selectedCode)}" selected>${missionEscapeHtml(selectedName || selectedCode)} — Baú atualmente vinculado</option>`);
  }
  return options.length > 0 ? options.join("") : '<option value="">Nenhum Baú da Área ativo disponível</option>';
}

function missionUpdateChestPreview() {
  const select = document.getElementById("mission-chest-code");
  const preview = document.getElementById("mission-chest-preview");
  if (!select || !preview) return;
  const chest = missionState.chestOptions.find(option => option.code === select.value);
  preview.textContent = chest
    ? `${chest.description || "Baú da Área"} Loot Table: ${chest.lootTableName || chest.lootTableCode || "não informada"}.`
    : "Selecione um Baú da Área ativo.";
}

async function missionSubmitForm(event) {
  event.preventDefault();

  const errorDiv = document.getElementById("mission-form-error");
  errorDiv.classList.add("hidden");
  const chestCode = document.getElementById("mission-chest-code").value;
  if (!chestCode) {
    errorDiv.textContent = "Selecione um Baú da Área ativo.";
    errorDiv.classList.remove("hidden");
    return;
  }

  const body = {
    name: document.getElementById("mission-name").value.trim(),
    description: document.getElementById("mission-desc").value.trim(),
    area: document.getElementById("mission-area").value,
    requiredStage: document.getElementById("mission-stage").value,
    requiredLevel: Number(document.getElementById("mission-level").value),
    baseXp: Number(document.getElementById("mission-xp").value),
    baseBits: Number(document.getElementById("mission-bits").value),
    energyCost: Number(document.getElementById("mission-energy").value),
    durationSeconds: Number(document.getElementById("mission-duration").value),
    chestCode,
    rewards: [],
    lootChances: [],
    lootItems: []
  };

  try {
    if (missionState.editing) {
      await apiPut(`/admin/missions/${encodeURIComponent(missionState.editing)}`, body);
    } else {
      body.id = document.getElementById("mission-id").value.trim();
      if (!body.id) throw new Error("ID é obrigatório");
      await apiPost("/admin/missions", body);
    }

    missionCloseModal();
    loadMissions();
  } catch (error) {
    errorDiv.textContent = error.message;
    errorDiv.classList.remove("hidden");
  }
}

async function missionToggleActive(id) {
  try {
    await apiPatch(`/admin/missions/${encodeURIComponent(id)}/toggle-active`);
    loadMissions();
  } catch (error) {
    alert("Erro ao alterar status: " + error.message);
  }
}

function missionCloseModal() {
  document.getElementById("mission-modal").innerHTML = "";
}

function missionSelectOptions(options, selected, labels = {}) {
  return options.map(o => `<option value="${missionEscapeAttr(o)}" ${o === selected ? "selected" : ""}>${missionEscapeHtml(labels[o] || o)}</option>`).join("");
}

function missionEscapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function missionEscapeAttr(value) {
  return missionEscapeHtml(value);
}

function missionFormatDate(dateStr) {
  if (!dateStr) return "-";
  const d = new Date(dateStr);
  return d.toLocaleDateString("pt-BR") + " " + d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}
