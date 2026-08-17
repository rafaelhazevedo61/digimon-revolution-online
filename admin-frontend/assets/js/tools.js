const toolsState = {
  players: [],
  digimons: [],
  itemDefinitions: [],
  equipmentTemplates: [],
  selectedPlayerId: null,
  selectedDigimonId: null
};

async function renderToolsPage() {
  setPageHeader("Ferramentas", "Grant Equipment, Grant Item, Add XP");

  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="card mb-6">
      <h3 class="text-lg font-semibold text-cyan-400 mb-4">1. Selecionar Jogador</h3>
      <div class="flex gap-4 items-end">
        <div class="flex-1">
          <label class="text-sm text-slate-400">Buscar por username</label>
          <input id="tools-player-search" class="input mt-1" placeholder="Digite o username..." />
        </div>
        <button id="tools-player-search-btn" class="btn-primary px-4 py-2">Buscar</button>
      </div>
      <div id="tools-player-list" class="mt-4"></div>
    </div>

    <div id="tools-digimon-section" class="card mb-6 hidden">
      <h3 class="text-lg font-semibold text-cyan-400 mb-4">2. Selecionar Digimon</h3>
      <div id="tools-digimon-list"></div>
    </div>

    <div id="tools-actions-section" class="hidden">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">

        <div class="card">
          <h3 class="text-lg font-semibold text-yellow-400 mb-4">Add XP</h3>
          <div class="space-y-3">
            <div>
              <label class="text-sm text-slate-400">Quantidade de XP</label>
              <input id="tools-xp-amount" type="number" class="input mt-1" placeholder="1000" min="1" />
            </div>
            <button id="tools-xp-btn" class="btn-primary w-full py-2">Conceder XP</button>
            <div id="tools-xp-result" class="text-sm mt-2"></div>
          </div>
        </div>

        <div class="card">
          <h3 class="text-lg font-semibold text-purple-400 mb-4">Grant Equipment</h3>
          <div class="space-y-3">
            <div>
              <label class="text-sm text-slate-400">Template</label>
              <select id="tools-equip-template" class="input mt-1">
                <option value="">Carregando...</option>
              </select>
            </div>
            <div>
              <label class="text-sm text-slate-400">Raridade (opcional)</label>
              <select id="tools-equip-rarity" class="input mt-1">
                <option value="">Roll automatico</option>
                <option value="COMMON">Common</option>
                <option value="RARE">Rare</option>
                <option value="EPIC">Epic</option>
                <option value="LEGENDARY">Legendary</option>
              </select>
            </div>
            <button id="tools-equip-btn" class="btn-primary w-full py-2">Conceder Equipamento</button>
            <div id="tools-equip-result" class="text-sm mt-2"></div>
          </div>
        </div>

        <div class="card">
          <h3 class="text-lg font-semibold text-green-400 mb-4">Grant Item</h3>
          <div class="space-y-3">
            <div>
              <label class="text-sm text-slate-400">Item</label>
              <select id="tools-item-code" class="input mt-1">
                <option value="">Carregando...</option>
              </select>
            </div>
            <div>
              <label class="text-sm text-slate-400">Quantidade</label>
              <input id="tools-item-qty" type="number" class="input mt-1" placeholder="1" min="1" value="1" />
            </div>
            <button id="tools-item-btn" class="btn-primary w-full py-2">Conceder Item</button>
            <div id="tools-item-result" class="text-sm mt-2"></div>
          </div>
        </div>

      </div>
    </div>

    <div class="card border-red-900/50 mb-6">
      <h3 class="text-lg font-semibold text-red-400 mb-4">Comandos de Reset</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-slate-900 rounded-lg p-4 border border-slate-800">
          <p class="text-sm text-slate-300 mb-2">Resetar ataques diários da Arena (todos os jogadores)</p>
          <button onclick="adminResetArena()" class="btn-primary w-full py-2">Resetar Arena</button>
          <div id="admin-reset-arena-result" class="text-sm mt-2"></div>
        </div>
        <div class="bg-slate-900 rounded-lg p-4 border border-slate-800">
          <p class="text-sm text-slate-300 mb-2">Resetar ataques da Raid de Clã (todos os clãs)</p>
          <button onclick="adminResetClanRaid()" class="btn-primary w-full py-2">Resetar Raid</button>
          <div id="admin-reset-raid-result" class="text-sm mt-2"></div>
        </div>
        <div class="bg-slate-900 rounded-lg p-4 border border-slate-800">
          <p class="text-sm text-slate-300 mb-2">Completar todas as missões de clã em andamento</p>
          <button onclick="adminCompleteClanMissions()" class="btn-primary w-full py-2">Completar Missões</button>
          <div id="admin-complete-missions-result" class="text-sm mt-2"></div>
        </div>
      </div>
    </div>
  `;

  document.getElementById("tools-player-search-btn").addEventListener("click", toolsSearchPlayers);
  document.getElementById("tools-player-search").addEventListener("keydown", (e) => {
    if (e.key === "Enter") toolsSearchPlayers();
  });
}

async function toolsSearchPlayers() {
  const username = document.getElementById("tools-player-search").value.trim();
  if (!username) return;

  try {
    const data = await apiGet("/admin/players", { username, size: 10 });
    toolsState.players = data.items || [];
    toolsRenderPlayerList();
  } catch (err) {
    document.getElementById("tools-player-list").innerHTML =
      `<p class="text-red-400">${err.message}</p>`;
  }
}

function toolsRenderPlayerList() {
  const container = document.getElementById("tools-player-list");
  if (toolsState.players.length === 0) {
    container.innerHTML = `<p class="text-slate-500">Nenhum jogador encontrado.</p>`;
    return;
  }

  container.innerHTML = toolsState.players.map(p => `
    <button class="w-full text-left px-3 py-2 rounded hover:bg-slate-800 transition-colors flex justify-between items-center
      ${toolsState.selectedPlayerId === p.id ? 'bg-slate-800 border border-cyan-500' : 'border border-slate-700'}"
      onclick="toolsSelectPlayer('${p.id}', '${p.username}')">
      <span>
        <span class="text-cyan-300 font-medium">${p.username}</span>
        <span class="text-slate-500 text-sm ml-2">${p.email}</span>
      </span>
      <span class="text-slate-500 text-xs">${p.id.substring(0, 8)}...</span>
    </button>
  `).join("");
}

async function toolsSelectPlayer(playerId, username) {
  toolsState.selectedPlayerId = playerId;
  toolsRenderPlayerList();

  const digimonSection = document.getElementById("tools-digimon-section");
  digimonSection.classList.remove("hidden");

  try {
    const digimons = await apiGet(`/admin/digimon/by-player/${playerId}`);
    toolsState.digimons = digimons;
    toolsRenderDigimonList();
  } catch (err) {
    document.getElementById("tools-digimon-list").innerHTML =
      `<p class="text-red-400">${err.message}</p>`;
  }
}

function toolsRenderDigimonList() {
  const container = document.getElementById("tools-digimon-list");
  if (toolsState.digimons.length === 0) {
    container.innerHTML = `<p class="text-slate-500">Nenhum digimon encontrado.</p>`;
    return;
  }

  container.innerHTML = toolsState.digimons.map(d => `
    <button class="w-full text-left px-3 py-2 rounded hover:bg-slate-800 transition-colors flex justify-between items-center mb-1
      ${toolsState.selectedDigimonId === d.id ? 'bg-slate-800 border border-cyan-500' : 'border border-slate-700'}"
      onclick="toolsSelectDigimon('${d.id}')">
      <span>
        <span class="text-cyan-300 font-medium">${d.name}</span>
        <span class="text-slate-500 text-sm ml-2">${d.type} | Lv.${d.level} | ${d.stage}</span>
      </span>
      <span class="text-xs ${d.status === 'ACTIVE' ? 'text-green-400' : 'text-slate-500'}">${d.status}</span>
    </button>
  `).join("");
}

async function toolsSelectDigimon(digimonId) {
  toolsState.selectedDigimonId = digimonId;
  toolsRenderDigimonList();

  const actionsSection = document.getElementById("tools-actions-section");
  actionsSection.classList.remove("hidden");

  await toolsLoadSelects();

  document.getElementById("tools-xp-btn").onclick = toolsAddXp;
  document.getElementById("tools-equip-btn").onclick = toolsGrantEquipment;
  document.getElementById("tools-item-btn").onclick = toolsGrantItem;
}

async function toolsLoadSelects() {
  try {
    if (toolsState.equipmentTemplates.length === 0) {
      const templates = await apiGet("/admin/equipment-templates", { activeOnly: true });
      toolsState.equipmentTemplates = templates;
    }
    const templateSelect = document.getElementById("tools-equip-template");
    templateSelect.innerHTML = toolsState.equipmentTemplates.map(t =>
      `<option value="${t.name}">${t.name} (${t.slot} | T${t.tier})</option>`
    ).join("");
  } catch (err) {
    console.error("Failed to load equipment templates", err);
  }

  try {
    if (toolsState.itemDefinitions.length === 0) {
      const items = await apiGet("/admin/inventory/item-definitions");
      toolsState.itemDefinitions = items;
    }
    const itemSelect = document.getElementById("tools-item-code");
    itemSelect.innerHTML = toolsState.itemDefinitions.map(i =>
      `<option value="${i.code}">${i.name} (${i.category})</option>`
    ).join("");
  } catch (err) {
    console.error("Failed to load item definitions", err);
  }
}

async function toolsAddXp() {
  const amount = parseInt(document.getElementById("tools-xp-amount").value);
  if (!amount || amount <= 0) {
    document.getElementById("tools-xp-result").innerHTML = `<span class="text-red-400">Informe um valor valido.</span>`;
    return;
  }

  try {
    await apiPostVoid(`/admin/digimon/add-xp?digimonId=${toolsState.selectedDigimonId}&amount=${amount}`);
    document.getElementById("tools-xp-result").innerHTML =
      `<span class="text-green-400">+${amount} XP concedido com sucesso!</span>`;
  } catch (err) {
    document.getElementById("tools-xp-result").innerHTML =
      `<span class="text-red-400">${err.message}</span>`;
  }
}

async function toolsGrantEquipment() {
  const templateName = document.getElementById("tools-equip-template").value;
  if (!templateName) {
    document.getElementById("tools-equip-result").innerHTML = `<span class="text-red-400">Selecione um template.</span>`;
    return;
  }

  const rarity = document.getElementById("tools-equip-rarity").value || null;

  try {
    const body = { digimonId: toolsState.selectedDigimonId, templateName };
    if (rarity) body.rarity = rarity;

    const result = await apiPost("/admin/equipment-templates/grant", body);
    document.getElementById("tools-equip-result").innerHTML =
      `<span class="text-green-400">${result.message} (ID: ${result.equipmentId.substring(0, 8)}...)</span>`;
  } catch (err) {
    document.getElementById("tools-equip-result").innerHTML =
      `<span class="text-red-400">${err.message}</span>`;
  }
}

async function toolsGrantItem() {
  const itemCode = document.getElementById("tools-item-code").value;
  const quantity = parseInt(document.getElementById("tools-item-qty").value);

  if (!itemCode) {
    document.getElementById("tools-item-result").innerHTML = `<span class="text-red-400">Selecione um item.</span>`;
    return;
  }
  if (!quantity || quantity <= 0) {
    document.getElementById("tools-item-result").innerHTML = `<span class="text-red-400">Informe quantidade valida.</span>`;
    return;
  }

  try {
    const result = await apiPost("/admin/inventory/grant", {
      digimonId: toolsState.selectedDigimonId,
      itemCode,
      quantity
    });
    document.getElementById("tools-item-result").innerHTML =
      `<span class="text-green-400">${result.message}</span>`;
  } catch (err) {
    document.getElementById("tools-item-result").innerHTML =
      `<span class="text-red-400">${err.message}</span>`;
  }
}

async function adminResetArena() {
  try {
    const result = await apiPost("/admin/tools/reset-daily-arena-attacks");
    document.getElementById("admin-reset-arena-result").innerHTML =
      `<span class="text-green-400">${result.message} (${result.playersReset} jogadores)</span>`;
  } catch (err) {
    document.getElementById("admin-reset-arena-result").innerHTML =
      `<span class="text-red-400">${err.message}</span>`;
  }
}

async function adminResetClanRaid() {
  try {
    const result = await apiPost("/admin/tools/reset-clan-raid-daily");
    document.getElementById("admin-reset-raid-result").innerHTML =
      `<span class="text-green-400">${result.message} (${result.raidsReset} raids)</span>`;
  } catch (err) {
    document.getElementById("admin-reset-raid-result").innerHTML =
      `<span class="text-red-400">${err.message}</span>`;
  }
}

async function adminCompleteClanMissions() {
  try {
    const result = await apiPost("/admin/tools/complete-clan-missions");
    document.getElementById("admin-complete-missions-result").innerHTML =
      `<span class="text-green-400">${result.message} (${result.completedCount} missões)</span>`;
  } catch (err) {
    document.getElementById("admin-complete-missions-result").innerHTML =
      `<span class="text-red-400">${err.message}</span>`;
  }
}
