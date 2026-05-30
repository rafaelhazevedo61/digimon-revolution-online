const eqtState = {
  templates: [],
  activeOnly: false,
  editing: null
};

function renderEquipmentTemplatesPage() {
  setPageHeader(
    "Equipment Templates",
    "Gerencie os templates de equipamento do jogo"
  );

  const app = document.getElementById("app");

  app.innerHTML = `
    <div class="card mb-6">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <label class="flex items-center gap-2 text-sm text-slate-400 cursor-pointer">
            <input type="checkbox" id="eqt-active-only" ${eqtState.activeOnly ? "checked" : ""} 
              onchange="eqtToggleActiveFilter()" class="accent-cyan-500" />
            Apenas ativos
          </label>
        </div>

        <button class="btn-primary" onclick="eqtShowCreateModal()">
          + Novo Template
        </button>
      </div>
    </div>

    <div id="eqt-result"></div>
    <div id="eqt-modal"></div>
  `;

  loadEquipmentTemplates();
}

async function loadEquipmentTemplates() {
  const container = document.getElementById("eqt-result");
  container.innerHTML = `<div class="card"><p class="text-slate-400">Carregando templates...</p></div>`;

  try {
    const params = {};
    if (eqtState.activeOnly) params.activeOnly = "true";

    const templates = await apiGet("/admin/equipment-templates", params);
    eqtState.templates = templates;
    renderEquipmentTemplatesTable(templates);
  } catch (error) {
    container.innerHTML = `
      <div class="card border-red-900 bg-red-950/30">
        <h3 class="font-bold text-red-300 mb-2">Erro ao carregar templates</h3>
        <p class="text-red-200">${error.message}</p>
      </div>
    `;
  }
}

function renderEquipmentTemplatesTable(templates) {
  const container = document.getElementById("eqt-result");

  container.innerHTML = `
    <div class="mb-4">
      <h3 class="text-lg font-bold">Templates de Equipamento</h3>
      <p class="text-sm text-slate-400">Total: ${templates.length}</p>
    </div>

    <div class="table-wrapper">
      <table class="table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Slot</th>
            <th>Raridade</th>
            <th>HP</th>
            <th>ATK</th>
            <th>DEF</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          ${templates.map(renderEqtRow).join("")}
        </tbody>
      </table>
    </div>

    ${templates.length === 0 ? '<div class="card mt-4"><p class="text-slate-400">Nenhum template encontrado.</p></div>' : ""}
  `;
}

function renderEqtRow(t) {
  const statusClass = t.active ? "badge-success" : "badge-danger";
  const statusText = t.active ? "Ativo" : "Inativo";

  return `
    <tr>
      <td class="font-semibold">${t.name}</td>
      <td><span class="badge">${t.slot}</span></td>
      <td><span class="badge badge-${t.rarity.toLowerCase()}">${t.rarity}</span></td>
      <td>${t.bonusHp > 0 ? `+${t.bonusHp}` : "-"}</td>
      <td>${t.bonusAttack > 0 ? `+${t.bonusAttack}` : "-"}</td>
      <td>${t.bonusDefense > 0 ? `+${t.bonusDefense}` : "-"}</td>
      <td><span class="badge ${statusClass}">${statusText}</span></td>
      <td>
        <div class="flex gap-2">
          <button class="btn-sm btn-secondary" onclick="eqtShowEditModal('${t.name.replace(/'/g, "\\'")}')">
            Editar
          </button>
          <button class="btn-sm ${t.active ? 'btn-warning' : 'btn-success-outline'}" 
            onclick="eqtToggleActive('${t.name.replace(/'/g, "\\'")}')">
            ${t.active ? "Desativar" : "Ativar"}
          </button>
        </div>
      </td>
    </tr>
  `;
}

function eqtToggleActiveFilter() {
  eqtState.activeOnly = document.getElementById("eqt-active-only").checked;
  loadEquipmentTemplates();
}

function eqtShowCreateModal() {
  eqtState.editing = null;
  eqtRenderModal("Novo Template", {
    name: "",
    slot: "WEAPON",
    rarity: "COMMON",
    bonusHp: 0,
    bonusAttack: 0,
    bonusDefense: 0
  }, false);
}

function eqtShowEditModal(name) {
  const template = eqtState.templates.find(t => t.name === name);
  if (!template) return;
  eqtState.editing = name;
  eqtRenderModal("Editar Template", template, true);
}

function eqtRenderModal(title, data, isEdit) {
  const modal = document.getElementById("eqt-modal");

  modal.innerHTML = `
    <div class="modal-overlay" onclick="eqtCloseModal()">
      <div class="modal-content" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold">${title}</h3>
          <button class="text-slate-400 hover:text-white text-2xl" onclick="eqtCloseModal()">&times;</button>
        </div>

        <form id="eqt-form" onsubmit="eqtSubmitForm(event)">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="md:col-span-2">
              <label class="text-sm text-slate-400">Nome</label>
              <input id="eqt-name" class="input mt-1" value="${data.name}" 
                ${isEdit ? "disabled" : ""} required />
            </div>

            <div>
              <label class="text-sm text-slate-400">Slot</label>
              <select id="eqt-slot" class="input mt-1">
                ${eqtSlotOptions(data.slot)}
              </select>
            </div>

            <div>
              <label class="text-sm text-slate-400">Raridade</label>
              <select id="eqt-rarity" class="input mt-1">
                ${eqtRarityOptions(data.rarity)}
              </select>
            </div>

            <div>
              <label class="text-sm text-slate-400">Bonus HP</label>
              <input id="eqt-hp" type="number" min="0" class="input mt-1" value="${data.bonusHp}" required />
            </div>

            <div>
              <label class="text-sm text-slate-400">Bonus ATK</label>
              <input id="eqt-atk" type="number" min="0" class="input mt-1" value="${data.bonusAttack}" required />
            </div>

            <div>
              <label class="text-sm text-slate-400">Bonus DEF</label>
              <input id="eqt-def" type="number" min="0" class="input mt-1" value="${data.bonusDefense}" required />
            </div>
          </div>

          <div id="eqt-form-error" class="hidden mt-4 p-3 rounded-lg bg-red-950/30 border border-red-900 text-red-200 text-sm"></div>

          <div class="flex gap-3 mt-6">
            <button type="submit" class="btn-primary flex-1">${isEdit ? "Salvar" : "Criar"}</button>
            <button type="button" class="btn-secondary flex-1" onclick="eqtCloseModal()">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  `;
}

async function eqtSubmitForm(event) {
  event.preventDefault();

  const errorDiv = document.getElementById("eqt-form-error");
  errorDiv.classList.add("hidden");

  const body = {
    slot: document.getElementById("eqt-slot").value,
    rarity: document.getElementById("eqt-rarity").value,
    bonusHp: Number(document.getElementById("eqt-hp").value),
    bonusAttack: Number(document.getElementById("eqt-atk").value),
    bonusDefense: Number(document.getElementById("eqt-def").value)
  };

  try {
    if (eqtState.editing) {
      await apiPut(`/admin/equipment-templates/${encodeURIComponent(eqtState.editing)}`, body);
    } else {
      body.name = document.getElementById("eqt-name").value.trim();
      if (!body.name) throw new Error("Nome é obrigatório");
      await apiPost("/admin/equipment-templates", body);
    }

    eqtCloseModal();
    loadEquipmentTemplates();
  } catch (error) {
    errorDiv.textContent = error.message;
    errorDiv.classList.remove("hidden");
  }
}

async function eqtToggleActive(name) {
  try {
    await apiPatch(`/admin/equipment-templates/${encodeURIComponent(name)}/toggle-active`);
    loadEquipmentTemplates();
  } catch (error) {
    alert("Erro ao alterar status: " + error.message);
  }
}

function eqtCloseModal() {
  document.getElementById("eqt-modal").innerHTML = "";
}

function eqtSlotOptions(selected) {
  const slots = ["WEAPON", "ARMOR", "ACCESSORY"];
  return slots.map(s => `<option value="${s}" ${s === selected ? "selected" : ""}>${s}</option>`).join("");
}

function eqtRarityOptions(selected) {
  const rarities = ["COMMON", "RARE", "EPIC", "LEGENDARY"];
  return rarities.map(r => `<option value="${r}" ${r === selected ? "selected" : ""}>${r}</option>`).join("");
}
