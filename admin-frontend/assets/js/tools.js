const toolsState = {
  players: [],
  playerPage: 0,
  playerPageSize: 10,
  playerSearch: "",
  playerSearchResult: null,
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
          <input id="tools-player-search" class="input mt-1" placeholder="Digite o username..." value="${escapeAttr(toolsState.playerSearch)}" />
        </div>
        <button id="tools-player-search-btn" class="btn-primary px-4 py-2">Buscar</button>
      </div>
      <div id="tools-player-list" class="mt-4"></div>
    </div>

    <div id="tools-digimon-section" class="card mb-6 hidden">
      <h3 class="text-lg font-semibold text-cyan-400 mb-4">2. Selecionar Digimon para XP</h3>
      <div id="tools-digimon-list"></div>
    </div>

    <div id="tools-actions-section" class="hidden">
      <div class="mb-4 rounded-lg border border-cyan-900/60 bg-cyan-950/20 px-4 py-3 text-sm text-slate-300">
        <strong class="text-cyan-300">Ações do jogador selecionado.</strong>
        Grant Item e Grant Equipment usam diretamente o inventário global do jogador. A seleção de Digimon abaixo é necessária apenas para Add XP.
      </div>
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
      <h3 class="text-lg font-semibold text-red-400 mb-4">Comandos do Servidor</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-slate-900 rounded-lg p-4 border border-slate-800">
          <p class="text-sm text-slate-300 mb-2">Buff global de dano (teste) — 100x</p>
          <div id="admin-damage-buff-status" class="text-sm mb-3 text-slate-400">Carregando...</div>
          <button id="admin-damage-buff-btn" onclick="adminToggleDamageBuff()" class="btn-primary w-full py-2">Ligar/Desligar Buff</button>
          <div id="admin-damage-buff-result" class="text-sm mt-2"></div>
        </div>
        <div class="bg-slate-900 rounded-lg p-4 border border-slate-800">
          <p class="text-sm text-slate-300 mb-2">Evento de Double XP e Bits</p>
          <div id="admin-weekend-double-reward-status" class="text-sm mb-3 text-slate-400">Carregando...</div>
          <div class="flex gap-2">
            <button id="admin-weekend-double-reward-btn" onclick="adminToggleWeekendDoubleReward()" class="btn-primary flex-1 py-2">Ligar/Desligar</button>
            <button onclick="adminUseAutomaticWeekendDoubleReward()" class="btn-secondary flex-1 py-2">Automático</button>
          </div>
          <div id="admin-weekend-double-reward-result" class="text-sm mt-2"></div>
        </div>

        <div class="bg-slate-900 rounded-lg p-4 border border-slate-800">
          <p class="text-sm text-slate-300 mb-2">Resetar ataques diários da Arena (todos os jogadores)</p>
          <button onclick="adminResetArena()" class="btn-primary w-full py-2">Resetar Arena</button>
          <div id="admin-reset-arena-result" class="text-sm mt-2"></div>
        </div>
        <div class="bg-slate-900 rounded-lg p-4 border border-slate-800">
          <p class="text-sm text-slate-300 mb-2">Completar todas as missões de clã em andamento</p>
          <button onclick="adminCompleteClanMissions()" class="btn-primary w-full py-2">Completar Missões</button>
          <div id="admin-complete-missions-result" class="text-sm mt-2"></div>
        </div>
        <div class="bg-slate-900 rounded-lg p-4 border border-amber-900/60">
          <p class="text-sm text-slate-300 mb-1">Forçar novo ciclo do Boss Mundial</p>
          <p class="text-xs text-slate-500 mb-2">Disponível somente depois que o Boss atual for derrotado. O histórico anterior será preservado.</p>
          <button onclick="adminForceNewWorldBossCycle()" class="btn-primary w-full py-2">Abrir novo ciclo</button>
          <div id="admin-force-world-boss-result" class="text-sm mt-2"></div>
        </div>
      </div>
    </div>
  `;

  document.getElementById("tools-player-search-btn").addEventListener("click", toolsSearchPlayers);
  document.getElementById("tools-player-search").addEventListener("keydown", (e) => {
    if (e.key === "Enter") toolsSearchPlayers();
  });

  adminLoadDamageBuff();
  adminLoadWeekendDoubleReward();
}

async function toolsSearchPlayers(page = 0) {
  const input = document.getElementById("tools-player-search");
  const username = (input ? input.value : toolsState.playerSearch).trim();
  if (!username) return;

  if (page === 0) {
    toolsState.selectedPlayerId = null;
    toolsState.selectedDigimonId = null;
    toolsState.digimons = [];
    document.getElementById("tools-digimon-section")?.classList.add("hidden");
    document.getElementById("tools-actions-section")?.classList.add("hidden");
  }

  toolsState.playerPage = Math.max(0, Number(page) || 0);
  toolsState.playerSearch = username;
  const container = document.getElementById("tools-player-list");
  container.innerHTML = `<p class="text-slate-400">Buscando jogadores...</p>`;

  try {
    const data = await apiGet("/admin/players", {
      username: toolsState.playerSearch,
      page: toolsState.playerPage,
      size: toolsState.playerPageSize
    });
    toolsState.players = data.items || [];
    toolsState.playerSearchResult = data;
    toolsRenderPlayerList();
  } catch (err) {
    toolsState.playerSearchResult = null;
    container.innerHTML =
      `<p class="text-red-400">${escapeHtml(err.message)}</p>`;
  }
}

function toolsRenderPlayerList() {
  const container = document.getElementById("tools-player-list");
  if (!container) return;

  const result = toolsState.playerSearchResult || {};
  const currentPage = Number(result.page ?? toolsState.playerPage) || 0;
  const totalItems = Number(result.totalItems) || 0;
  const totalPages = Math.max(1, Number(result.totalPages) || 0);
  const hasPrevious = Boolean(result.hasPrevious);
  const hasNext = Boolean(result.hasNext);
  const playerRows = toolsState.players.length === 0
    ? `<p class="text-slate-500">Nenhum jogador encontrado.</p>`
    : toolsState.players.map(p => `
      <button class="w-full text-left px-3 py-2 rounded hover:bg-slate-800 transition-colors flex justify-between items-center
        ${toolsState.selectedPlayerId === p.id ? 'bg-slate-800 border border-cyan-500' : 'border border-slate-700'}"
        data-player-id="${escapeAttr(p.id)}">
        <span>
          <span class="text-cyan-300 font-medium">${escapeHtml(p.username)}</span>
          <span class="text-slate-500 text-sm ml-2">${escapeHtml(p.email)}</span>
        </span>
        <span class="text-slate-500 text-xs">${escapeHtml(p.id.substring(0, 8))}...</span>
      </button>
    `).join("");

  container.innerHTML = `
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-3">
      <p class="text-sm text-slate-400">
        ${totalItems} jogador${totalItems === 1 ? "" : "es"} encontrado${totalItems === 1 ? "" : "s"}
      </p>
      <div class="flex items-center gap-2">
        <button class="btn-secondary text-xs" data-tools-player-previous ${!hasPrevious ? "disabled" : ""}>Anterior</button>
        <span class="text-xs text-slate-400 whitespace-nowrap">Página ${currentPage + 1} de ${totalPages}</span>
        <button class="btn-secondary text-xs" data-tools-player-next ${!hasNext ? "disabled" : ""}>Próxima</button>
      </div>
    </div>
    <div class="space-y-1">${playerRows}</div>
  `;

  container.querySelector("[data-tools-player-previous]")?.addEventListener("click", () => {
    if (hasPrevious) toolsSearchPlayers(currentPage - 1);
  });
  container.querySelector("[data-tools-player-next]")?.addEventListener("click", () => {
    if (hasNext) toolsSearchPlayers(currentPage + 1);
  });
  container.querySelectorAll("[data-player-id]").forEach(button => {
    button.addEventListener("click", () => toolsSelectPlayer(button.dataset.playerId));
  });
}

async function toolsSelectPlayer(playerId) {
  toolsState.selectedPlayerId = playerId;
  toolsState.selectedDigimonId = null;
  toolsRenderPlayerList();

  const actionsSection = document.getElementById("tools-actions-section");
  actionsSection.classList.remove("hidden");
  await toolsLoadSelects();
  toolsBindActionButtons();

  const digimonSection = document.getElementById("tools-digimon-section");
  digimonSection.classList.remove("hidden");

  try {
    const digimons = await apiGet(`/admin/digimon/by-player/${playerId}`);
    toolsState.digimons = digimons;
    toolsRenderDigimonList();
  } catch (err) {
    document.getElementById("tools-digimon-list").innerHTML =
      `<p class="text-red-400">${escapeHtml(err.message)}</p>`;
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
      data-digimon-id="${escapeAttr(d.id)}">
      <span>
        <span class="text-cyan-300 font-medium">${escapeHtml(d.name)}</span>
        <span class="text-slate-500 text-sm ml-2">${escapeHtml(d.type)} | Lv.${d.level} | ${escapeHtml(d.stage)}</span>
      </span>
      <span class="text-xs ${d.status === 'ACTIVE' ? 'text-green-400' : 'text-slate-500'}">${escapeHtml(d.status)}</span>
    </button>
  `).join("");
  container.querySelectorAll("[data-digimon-id]").forEach(button => {
    button.addEventListener("click", () => toolsSelectDigimon(button.dataset.digimonId));
  });
}

async function toolsSelectDigimon(digimonId) {
  toolsState.selectedDigimonId = digimonId;
  toolsRenderDigimonList();
  toolsBindActionButtons();
}

function toolsBindActionButtons() {
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
      `<option value="${escapeAttr(t.name)}">${escapeHtml(t.name)} (${escapeHtml(t.slot)} | T${t.tier})</option>`
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
      `<option value="${escapeAttr(i.code)}">${escapeHtml(i.name)} (${escapeHtml(i.category)})</option>`
    ).join("");
  } catch (err) {
    console.error("Failed to load item definitions", err);
  }
}

async function toolsAddXp() {
  if (!toolsState.selectedDigimonId) {
    document.getElementById("tools-xp-result").innerHTML = `<span class="text-red-400">Selecione um Digimon para conceder XP.</span>`;
    return;
  }
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
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function toolsGrantEquipment() {
  if (!toolsState.selectedPlayerId) {
    document.getElementById("tools-equip-result").innerHTML = `<span class="text-red-400">Selecione um jogador.</span>`;
    return;
  }
  const templateName = document.getElementById("tools-equip-template").value;
  if (!templateName) {
    document.getElementById("tools-equip-result").innerHTML = `<span class="text-red-400">Selecione um template.</span>`;
    return;
  }

  const rarity = document.getElementById("tools-equip-rarity").value || null;

  try {
    const body = { playerId: toolsState.selectedPlayerId, templateName };
    if (rarity) body.rarity = rarity;

    const result = await apiPost("/admin/equipment-templates/grant", body);
    document.getElementById("tools-equip-result").innerHTML =
      `<span class="text-green-400">${escapeHtml(result.message)} (ID: ${escapeHtml(result.equipmentId.substring(0, 8))}...)</span>`;
  } catch (err) {
    document.getElementById("tools-equip-result").innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function toolsGrantItem() {
  if (!toolsState.selectedPlayerId) {
    document.getElementById("tools-item-result").innerHTML = `<span class="text-red-400">Selecione um jogador.</span>`;
    return;
  }
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
      playerId: toolsState.selectedPlayerId,
      itemCode,
      quantity
    });
    document.getElementById("tools-item-result").innerHTML =
      `<span class="text-green-400">${escapeHtml(result.message)}</span>`;
  } catch (err) {
    document.getElementById("tools-item-result").innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminLoadDamageBuff() {
  try {
    const result = await apiGet("/admin/server/damage-buff");
    const status = document.getElementById("admin-damage-buff-status");
    const btn = document.getElementById("admin-damage-buff-btn");
    if (status) {
      status.innerHTML = result.enabled
        ? `<span class="text-green-400 font-semibold">Buff ativo (${result.multiplier}x dano)</span>`
        : `<span class="text-slate-400">Buff desligado</span>`;
    }
    if (btn) {
      btn.textContent = result.enabled ? "Desligar Buff" : "Ligar Buff";
    }
  } catch (err) {
    const status = document.getElementById("admin-damage-buff-status");
    if (status) status.innerHTML = `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminLoadWeekendDoubleReward() {
  try {
    const result = await apiGet("/admin/server/weekend-double-reward");
    const status = document.getElementById("admin-weekend-double-reward-status");
    const btn = document.getElementById("admin-weekend-double-reward-btn");
    if (status) {
      const mode = result.manualOverride ? "controle manual" : "programação automática";
      status.innerHTML = result.active
        ? `<span class="text-green-400 font-semibold">Evento ativo (2x XP e 2x Bits — ${mode})</span>`
        : `<span class="text-slate-400">Evento desligado (${mode})</span>`;
    }
    if (btn) btn.textContent = result.active ? "Desligar evento" : "Ligar evento";
  } catch (err) {
    const status = document.getElementById("admin-weekend-double-reward-status");
    if (status) status.innerHTML = `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminToggleWeekendDoubleReward() {
  try {
    const result = await apiPost("/admin/server/weekend-double-reward/toggle");
    await adminLoadWeekendDoubleReward();
    document.getElementById("admin-weekend-double-reward-result").innerHTML =
      `<span class="text-green-400">Evento ${result.active ? "ligado" : "desligado"} manualmente.</span>`;
  } catch (err) {
    document.getElementById("admin-weekend-double-reward-result").innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminUseAutomaticWeekendDoubleReward() {
  try {
    const result = await apiPost("/admin/server/weekend-double-reward/automatic");
    await adminLoadWeekendDoubleReward();
    document.getElementById("admin-weekend-double-reward-result").innerHTML =
      `<span class="text-green-400">Programação automática restaurada. Evento ${result.active ? "ativo" : "desligado"} agora.</span>`;
  } catch (err) {
    document.getElementById("admin-weekend-double-reward-result").innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminToggleDamageBuff() {
  try {
    const result = await apiPost("/admin/server/damage-buff/toggle");
    await adminLoadDamageBuff();
    document.getElementById("admin-damage-buff-result").innerHTML =
      `<span class="text-green-400">Buff ${result.enabled ? "ligado" : "desligado"} (${result.multiplier}x)</span>`;
  } catch (err) {
    document.getElementById("admin-damage-buff-result").innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminResetArena() {
  try {
    const result = await apiPost("/admin/tools/reset-daily-arena-attacks");
    document.getElementById("admin-reset-arena-result").innerHTML =
      `<span class="text-green-400">${escapeHtml(result.message)} (${result.playersReset} jogadores)</span>`;
  } catch (err) {
    document.getElementById("admin-reset-arena-result").innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminForceNewWorldBossCycle() {
  const confirmed = window.confirm(
    "Abrir um novo ciclo do Boss Mundial agora? O ciclo atual precisa estar derrotado e o histórico será preservado."
  );
  if (!confirmed) return;

  const resultElement = document.getElementById("admin-force-world-boss-result");
  try {
    const result = await apiPost("/admin/tools/force-new-world-boss-cycle");
    resultElement.innerHTML =
      `<span class="text-green-400">${escapeHtml(result.message)} (ciclo ${result.cycleNumber})</span>`;
  } catch (err) {
    resultElement.innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}

async function adminCompleteClanMissions() {
  try {
    const result = await apiPost("/admin/tools/complete-clan-missions");
    document.getElementById("admin-complete-missions-result").innerHTML =
      `<span class="text-green-400">${escapeHtml(result.message)} (${result.completedCount} missões)</span>`;
  } catch (err) {
    document.getElementById("admin-complete-missions-result").innerHTML =
      `<span class="text-red-400">${escapeHtml(err.message)}</span>`;
  }
}
