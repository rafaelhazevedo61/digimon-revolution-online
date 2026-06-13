const missionState = {
  missions: [],
  activeOnly: false,
  editing: null,
  filterArea: "",
  filterStage: "",
  filterLoot: ""
};

const AREAS = ["NATIVE_FOREST", "GEAR_SAVANNA", "FACTORIAL_TOWN", "FREEZELAND", "SERVER_DESERT", "INFINITY_MOUNTAIN"];
const STAGES = ["BABY", "BABY_II", "ROOKIE", "CHAMPION", "ULTIMATE", "MEGA"];
const LOOT_RARITIES = ["COMMON", "RARE", "EPIC", "LEGENDARY"];
const MISSION_ITEM_TYPES = [
  "POTION_SMALL", "TRAINING_STONE", "DATA_CORE",
  "DIGITAMA_STARTER", "DIGITAMA_FIRE", "DIGITAMA_WATER", "DIGITAMA_NATURE",
  "INCUBATOR_COMMON", "INCUBATOR_RARE", "INCUBATOR_EPIC",
  "FRAGMENT_ROOKIE", "FRAGMENT_CHAMPION", "FRAGMENT_ULTIMATE", "FRAGMENT_MEGA",
  "EVOLUTION_MATERIAL"
];

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
              ${AREAS.map(a => `<option value="${a}" ${missionState.filterArea === a ? "selected" : ""}>${a.replace(/_/g, " ")}</option>`).join("")}
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500 block mb-1">Stage</label>
            <select id="mission-filter-stage" class="input input-sm" onchange="missionApplyFilters()">
              <option value="">Todos</option>
              ${STAGES.map(s => `<option value="${s}" ${missionState.filterStage === s ? "selected" : ""}>${s}</option>`).join("")}
            </select>
          </div>
          <div>
            <label class="text-xs text-slate-500 block mb-1">Loot / Reward</label>
            <select id="mission-filter-loot" class="input input-sm" onchange="missionApplyFilters()">
              <option value="">Todos</option>
              ${MISSION_ITEM_TYPES.map(t => `<option value="${t}" ${missionState.filterLoot === t ? "selected" : ""}>${t}</option>`).join("")}
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

  loadMissions();
}

async function loadMissions() {
  const container = document.getElementById("mission-result");
  container.innerHTML = `<div class="card"><p class="text-slate-400">Carregando missões...</p></div>`;

  try {
    const params = {};
    if (missionState.activeOnly) params.activeOnly = "true";
    if (missionState.filterArea) params.area = missionState.filterArea;
    if (missionState.filterStage) params.stage = missionState.filterStage;
    if (missionState.filterLoot) params.lootItemType = missionState.filterLoot;

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
            <th>Energia</th>
            <th>Duração</th>
            <th>Rewards</th>
            <th>Loot</th>
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
  const areaLabel = m.area.replace(/_/g, " ");

  return `
    <tr>
      <td class="text-xs font-mono text-slate-400">${m.id}</td>
      <td class="font-semibold">${m.name}</td>
      <td><span class="badge badge-area">${areaLabel}</span></td>
      <td><span class="badge">${m.requiredStage}</span></td>
      <td>${m.requiredLevel}</td>
      <td>${m.baseXp}</td>
      <td>${m.energyCost}</td>
      <td>${m.durationSeconds}s</td>
      <td><span class="badge">${m.rewards ? m.rewards.length : 0}</span></td>
      <td><span class="badge">${m.lootChances ? m.lootChances.length : 0} chances</span></td>
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
  missionState.filterLoot = document.getElementById("mission-filter-loot").value;
  loadMissions();
}

function missionShowCreateModal() {
  missionState.editing = null;
  missionRenderModal("Nova Missão", {
    id: "",
    name: "",
    description: "",
    area: "NATIVE_FOREST",
    requiredStage: "ROOKIE",
    requiredLevel: 1,
    baseXp: 100,
    energyCost: 5,
    durationSeconds: 60,
    rewards: [{ itemType: "TRAINING_STONE", baseQuantity: 1 }],
    lootChances: [],
    lootItems: []
  }, false);
}

function missionShowEditModal(id) {
  const mission = missionState.missions.find(m => m.id === id);
  if (!mission) return;
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
                ${missionSelectOptions(AREAS, data.area)}
              </select>
            </div>
            <div>
              <label class="text-sm text-slate-400">Stage Mínimo</label>
              <select id="mission-stage" class="input mt-1">
                ${missionSelectOptions(STAGES, data.requiredStage)}
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
              <label class="text-sm text-slate-400">Custo de Energia</label>
              <input id="mission-energy" type="number" min="1" class="input mt-1" value="${data.energyCost}" required />
            </div>
            <div>
              <label class="text-sm text-slate-400">Duração (segundos)</label>
              <input id="mission-duration" type="number" min="1" class="input mt-1" value="${data.durationSeconds}" required />
            </div>
          </div>

          <!-- Rewards section -->
          <div class="mb-6">
            <div class="flex items-center justify-between mb-2">
              <h4 class="font-bold text-sm text-cyan-400">Recompensas Fixas</h4>
              <button type="button" class="btn-sm btn-primary" onclick="missionAddReward()">+ Recompensa</button>
            </div>
            <div id="mission-rewards-list">
              ${(data.rewards || []).map((r, i) => missionRenderRewardRow(r, i)).join("")}
            </div>
          </div>

          <!-- Loot Chances section -->
          <div class="mb-6">
            <div class="flex items-center justify-between mb-2">
              <h4 class="font-bold text-sm text-cyan-400">Chances de Loot (por Raridade)</h4>
              <button type="button" class="btn-sm btn-primary" onclick="missionAddLootChance()">+ Chance</button>
            </div>
            <div id="mission-loot-chances-list">
              ${(data.lootChances || []).map((c, i) => missionRenderLootChanceRow(c, i)).join("")}
            </div>
          </div>

          <!-- Loot Items section -->
          <div class="mb-6">
            <div class="flex items-center justify-between mb-2">
              <h4 class="font-bold text-sm text-cyan-400">Itens de Loot (por Raridade)</h4>
              <button type="button" class="btn-sm btn-primary" onclick="missionAddLootItem()">+ Item</button>
            </div>
            <div id="mission-loot-items-list">
              ${(data.lootItems || []).map((item, i) => missionRenderLootItemRow(item, i)).join("")}
            </div>
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
}

function missionRenderRewardRow(reward, index) {
  return `
    <div class="flex gap-2 items-center mb-2" data-reward-row="${index}">
      <select class="input flex-1 reward-item-type">
        ${missionSelectOptions(MISSION_ITEM_TYPES, reward.itemType)}
      </select>
      <input type="number" min="1" class="input w-24 reward-quantity" value="${reward.baseQuantity}" placeholder="Qtd" />
      <button type="button" class="btn-sm btn-danger" onclick="missionRemoveRow(this)">&times;</button>
    </div>
  `;
}

function missionRenderLootChanceRow(chance, index) {
  return `
    <div class="flex gap-2 items-center mb-2" data-loot-chance-row="${index}">
      <select class="input flex-1 loot-chance-rarity">
        ${missionSelectOptions(LOOT_RARITIES, chance.rarity)}
      </select>
      <input type="number" min="1" class="input w-24 loot-chance-value" value="${chance.chance}" placeholder="Peso" />
      <button type="button" class="btn-sm btn-danger" onclick="missionRemoveRow(this)">&times;</button>
    </div>
  `;
}

function missionRenderLootItemRow(item, index) {
  return `
    <div class="flex gap-2 items-center mb-2" data-loot-item-row="${index}">
      <select class="input flex-1 loot-item-rarity">
        ${missionSelectOptions(LOOT_RARITIES, item.rarity)}
      </select>
      <select class="input flex-1 loot-item-type">
        ${missionSelectOptions(MISSION_ITEM_TYPES, item.itemType)}
      </select>
      <input type="number" min="1" class="input w-20 loot-item-quantity" value="${item.quantity}" placeholder="Qtd" />
      <button type="button" class="btn-sm btn-danger" onclick="missionRemoveRow(this)">&times;</button>
    </div>
  `;
}

function missionAddReward() {
  const list = document.getElementById("mission-rewards-list");
  const index = list.children.length;
  list.insertAdjacentHTML("beforeend", missionRenderRewardRow({ itemType: "TRAINING_STONE", baseQuantity: 1 }, index));
}

function missionAddLootChance() {
  const list = document.getElementById("mission-loot-chances-list");
  const index = list.children.length;
  list.insertAdjacentHTML("beforeend", missionRenderLootChanceRow({ rarity: "COMMON", chance: 50 }, index));
}

function missionAddLootItem() {
  const list = document.getElementById("mission-loot-items-list");
  const index = list.children.length;
  list.insertAdjacentHTML("beforeend", missionRenderLootItemRow({ rarity: "COMMON", itemType: "TRAINING_STONE", quantity: 1 }, index));
}

function missionRemoveRow(btn) {
  btn.closest("[data-reward-row], [data-loot-chance-row], [data-loot-item-row]").remove();
}

async function missionSubmitForm(event) {
  event.preventDefault();

  const errorDiv = document.getElementById("mission-form-error");
  errorDiv.classList.add("hidden");

  const rewards = [];
  document.querySelectorAll("[data-reward-row]").forEach(row => {
    rewards.push({
      itemType: row.querySelector(".reward-item-type").value,
      baseQuantity: Number(row.querySelector(".reward-quantity").value)
    });
  });

  const lootChances = [];
  document.querySelectorAll("[data-loot-chance-row]").forEach(row => {
    lootChances.push({
      rarity: row.querySelector(".loot-chance-rarity").value,
      chance: Number(row.querySelector(".loot-chance-value").value)
    });
  });

  const lootItems = [];
  document.querySelectorAll("[data-loot-item-row]").forEach(row => {
    lootItems.push({
      rarity: row.querySelector(".loot-item-rarity").value,
      itemType: row.querySelector(".loot-item-type").value,
      quantity: Number(row.querySelector(".loot-item-quantity").value)
    });
  });

  const body = {
    name: document.getElementById("mission-name").value.trim(),
    description: document.getElementById("mission-desc").value.trim(),
    area: document.getElementById("mission-area").value,
    requiredStage: document.getElementById("mission-stage").value,
    requiredLevel: Number(document.getElementById("mission-level").value),
    baseXp: Number(document.getElementById("mission-xp").value),
    energyCost: Number(document.getElementById("mission-energy").value),
    durationSeconds: Number(document.getElementById("mission-duration").value),
    rewards,
    lootChances,
    lootItems
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

function missionSelectOptions(options, selected) {
  return options.map(o => `<option value="${o}" ${o === selected ? "selected" : ""}>${o}</option>`).join("");
}

function missionFormatDate(dateStr) {
  if (!dateStr) return "-";
  const d = new Date(dateStr);
  return d.toLocaleDateString("pt-BR") + " " + d.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}
