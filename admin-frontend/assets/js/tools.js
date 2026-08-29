const toolsState = {
  players: [],
  playerPage: 0,
  playerPageSize: 10,
  playerSearch: "",
  playerSearchResult: null,
  digimons: [],
  itemDefinitions: [],
  equipmentTemplates: [],
  selectedItem: null,
  selectedEquipment: null,
  catalogPicker: null,
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
      <div class="mb-5 rounded-xl border border-cyan-900/60 bg-cyan-950/20 px-4 py-3 text-sm text-slate-300">
        <strong class="text-cyan-300">Ações do jogador selecionado.</strong>
        Recursos econômicos são concedidos ao jogador. O Digimon ativo é necessário somente para a operação de XP.
      </div>

      <section class="card mb-6 border-cyan-900/60">
        <div class="flex items-start justify-between gap-4 mb-5">
          <div>
            <h3 class="text-lg font-semibold text-emerald-400">Economia do jogador</h3>
            <p class="text-sm text-slate-400 mt-1">Itens e equipamentos entram no inventário global do tamer.</p>
          </div>
          <span class="badge badge-success">Player-owned</span>
        </div>
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-5">
          <div class="rounded-xl border border-slate-700 bg-slate-950/40 p-4">
            <h4 class="font-semibold text-green-300 mb-4">Grant Item</h4>
            <div class="space-y-3">
              <div>
                <label class="block text-sm text-slate-400">Item</label>
                <input id="tools-item-code" type="hidden" value="" />
                <div id="tools-item-selection" class="mt-1 rounded-lg border border-slate-700 bg-slate-900/60 px-3 py-2 text-sm text-slate-500">Nenhum item selecionado.</div>
                <button id="tools-item-picker-btn" type="button" class="btn-secondary w-full mt-2">Buscar item no catálogo</button>
              </div>
              <label class="block text-sm text-slate-400">Quantidade
                <input id="tools-item-qty" type="number" class="input mt-1" placeholder="1" min="1" value="1" />
              </label>
              <button id="tools-item-btn" class="btn-primary w-full py-2">Conceder item ao jogador</button>
              <div id="tools-item-result" class="text-sm mt-2 min-h-5"></div>
            </div>
          </div>

          <div class="rounded-xl border border-slate-700 bg-slate-950/40 p-4">
            <h4 class="font-semibold text-purple-300 mb-4">Grant Equipment</h4>
            <div class="space-y-3">
              <div>
                <label class="block text-sm text-slate-400">Template</label>
                <input id="tools-equip-template" type="hidden" value="" />
                <div id="tools-equipment-selection" class="mt-1 rounded-lg border border-slate-700 bg-slate-900/60 px-3 py-2 text-sm text-slate-500">Nenhum template selecionado.</div>
                <button id="tools-equipment-picker-btn" type="button" class="btn-secondary w-full mt-2">Buscar equipamento no catálogo</button>
              </div>
              <label class="block text-sm text-slate-400">Raridade (opcional)
                <select id="tools-equip-rarity" class="input mt-1">
                  <option value="">Roll automático</option><option value="COMMON">Comum</option><option value="RARE">Rara</option><option value="EPIC">Épica</option><option value="LEGENDARY">Lendária</option>
                </select>
              </label>
              <button id="tools-equip-btn" class="btn-primary w-full py-2">Conceder equipamento ao jogador</button>
              <div id="tools-equip-result" class="text-sm mt-2 min-h-5"></div>
            </div>
          </div>
        </div>
      </section>

      <section class="card border-yellow-900/60">
        <div class="flex items-start justify-between gap-4 mb-5">
          <div>
            <h3 class="text-lg font-semibold text-yellow-300">Progressão do Digimon ativo</h3>
            <p class="text-sm text-slate-400 mt-1">Esta operação altera somente a experiência do Digimon selecionado acima.</p>
          </div>
          <span class="badge badge-warning">Digimon-specific</span>
        </div>
        <div class="max-w-md rounded-xl border border-slate-700 bg-slate-950/40 p-4">
          <label class="block text-sm text-slate-400">Quantidade de XP
            <input id="tools-xp-amount" type="number" class="input mt-1" placeholder="1000" min="1" />
          </label>
          <button id="tools-xp-btn" class="btn-primary w-full py-2 mt-3">Conceder XP ao Digimon ativo</button>
          <div id="tools-xp-result" class="text-sm mt-2 min-h-5"></div>
        </div>
      </section>
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
  document.getElementById("tools-item-picker-btn").addEventListener("click", () => toolsOpenCatalogPicker("item"));
  document.getElementById("tools-equipment-picker-btn").addEventListener("click", () => toolsOpenCatalogPicker("equipment"));

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
  toolsState.selectedItem = null;
  toolsState.selectedEquipment = null;
  toolsRenderCatalogSelections();
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


function toolsRunWithConfirmation(buttonId, resultId, message, busyLabel, action) {
  const button = document.getElementById(buttonId);
  const result = document.getElementById(resultId);
  if (!button || typeof openConfirmModal !== "function") return;

  openConfirmModal({
    title: "Confirmar operação administrativa",
    message,
    confirmText: "Confirmar",
    cancelText: "Cancelar",
    onConfirm: async () => {
      const originalLabel = button.textContent;
      button.disabled = true;
      button.textContent = busyLabel;
      try {
        await action();
      } catch (error) {
        if (result) result.innerHTML = `<span class="text-red-400">${escapeHtml(error.message)}</span>`;
      } finally {
        button.disabled = false;
        button.textContent = originalLabel;
      }
    }
  });
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

  toolsRunWithConfirmation(
    "tools-xp-btn",
    "tools-xp-result",
    `Confirmar concessão de ${amount} XP ao Digimon ativo selecionado?`,
    "Concedendo XP...",
    async () => {
      await apiPostVoid(`/admin/digimon/add-xp?digimonId=${toolsState.selectedDigimonId}&amount=${amount}`);
      document.getElementById("tools-xp-result").innerHTML =
        `<span class="text-green-400">+${amount} XP concedido com sucesso!</span>`;
    }
  );
}

async function toolsGrantEquipment() {
  if (!toolsState.selectedPlayerId) {
    document.getElementById("tools-equip-result").innerHTML = `<span class="text-red-400">Selecione um jogador.</span>`;
    return;
  }
  const templateName = document.getElementById("tools-equip-template").value;
  if (!templateName || !toolsState.selectedEquipment) {
    document.getElementById("tools-equip-result").innerHTML = `<span class="text-red-400">Selecione um template.</span>`;
    return;
  }

  const rarity = document.getElementById("tools-equip-rarity").value || null;

  toolsRunWithConfirmation(
    "tools-equip-btn",
    "tools-equip-result",
    `Confirmar concessão do equipamento ${templateName} ao jogador selecionado?`,
    "Concedendo equipamento...",
    async () => {
      const body = { playerId: toolsState.selectedPlayerId, templateName };
      if (rarity) body.rarity = rarity;

      const result = await apiPost("/admin/equipment-templates/grant", body);
      document.getElementById("tools-equip-result").innerHTML =
        `<span class="text-green-400">${escapeHtml(result.message)} (ID: ${escapeHtml(result.equipmentId.substring(0, 8))}...)</span>`;
    }
  );
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

  const item = toolsState.selectedItem || toolsState.itemDefinitions.find(candidate => candidate.code === itemCode);
  toolsRunWithConfirmation(
    "tools-item-btn",
    "tools-item-result",
    `Confirmar concessão de ${quantity} unidade(s) de ${item?.name || itemCode} ao jogador selecionado?`,
    "Concedendo item...",
    async () => {
      const result = await apiPost("/admin/inventory/grant", {
        playerId: toolsState.selectedPlayerId,
        itemCode,
        quantity
      });
      document.getElementById("tools-item-result").innerHTML =
        `<span class="text-green-400">${escapeHtml(result.message)}</span>`;
    }
  );
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


function toolsRenderCatalogSelections() {
  const itemCode = document.getElementById("tools-item-code");
  const itemSelection = document.getElementById("tools-item-selection");
  const equipmentName = document.getElementById("tools-equip-template");
  const equipmentSelection = document.getElementById("tools-equipment-selection");

  if (itemCode && itemSelection) {
    itemCode.value = toolsState.selectedItem?.code || "";
    itemSelection.innerHTML = toolsState.selectedItem
      ? `<span class="block text-cyan-300 font-medium break-words">${escapeHtml(toolsState.selectedItem.name)}</span><span class="block text-xs text-slate-500 mt-1">${escapeHtml(toolsState.selectedItem.category || "Item")} · <span class="font-mono">${escapeHtml(toolsState.selectedItem.code)}</span></span>`
      : "Nenhum item selecionado.";
    itemSelection.classList.toggle("text-slate-500", !toolsState.selectedItem);
  }
  if (equipmentName && equipmentSelection) {
    equipmentName.value = toolsState.selectedEquipment?.name || "";
    equipmentSelection.innerHTML = toolsState.selectedEquipment
      ? `<span class="block text-purple-300 font-medium break-words">${escapeHtml(toolsState.selectedEquipment.name)}</span><span class="block text-xs text-slate-500 mt-1">${escapeHtml(toolsState.selectedEquipment.slot || "Equipment")} · ${escapeHtml(toolsState.selectedEquipment.rarity || "-")} · T${Number(toolsState.selectedEquipment.tier || 0)}</span>`
      : "Nenhum template selecionado.";
    equipmentSelection.classList.toggle("text-slate-500", !toolsState.selectedEquipment);
  }
}

function toolsOpenCatalogPicker(kind) {
  const isItem = kind === "item";
  toolsState.catalogPicker = { kind, search: "", page: 0, pageSize: 8, items: [], totalItems: 0, totalPages: 1, hasNext: false, hasPrevious: false, remote: isItem, loading: false, error: "" };
  const overlay = document.createElement("div");
  overlay.id = "tools-catalog-picker-modal";
  overlay.className = "modal-overlay";
  overlay.innerHTML = `
    <div class="modal-content modal-wide" role="dialog" aria-modal="true" aria-labelledby="tools-catalog-picker-title" onclick="event.stopPropagation()">
      <div class="flex items-start justify-between gap-4 mb-5">
        <div>
          <h3 id="tools-catalog-picker-title" class="text-xl font-bold">Selecionar ${isItem ? "item" : "template de equipamento"}</h3>
          <p class="text-sm text-slate-400 mt-1">Pesquise no catálogo oficial e selecione um registro para conceder ao jogador.</p>
        </div>
        <button type="button" class="text-slate-400 hover:text-white text-2xl" aria-label="Fechar" data-tools-picker-close>&times;</button>
      </div>
      <div class="flex flex-col sm:flex-row gap-2 mb-4">
        <input id="tools-catalog-picker-search" class="input flex-1" placeholder="Nome, código ou ${isItem ? "categoria" : "posição"}" autocomplete="off">
        <button type="button" class="btn-primary" data-tools-picker-search>Pesquisar</button>
      </div>
      <div id="tools-catalog-picker-results" class="space-y-2 min-h-48"></div>
    </div>
  `;
  document.body.appendChild(overlay);
  overlay.querySelector("[data-tools-picker-close]").addEventListener("click", toolsCloseCatalogPicker);
  overlay.querySelector("[data-tools-picker-search]").addEventListener("click", toolsSearchCatalogPicker);
  overlay.querySelector("#tools-catalog-picker-search").addEventListener("keydown", event => {
    if (event.key === "Enter") toolsSearchCatalogPicker();
  });
  overlay.addEventListener("click", event => { if (event.target === overlay) toolsCloseCatalogPicker(); });
  document.addEventListener("keydown", toolsCatalogPickerKeydown);
  toolsLoadCatalogPicker();
  overlay.querySelector("#tools-catalog-picker-search").focus();
}

function toolsCatalogPickerKeydown(event) {
  if (event.key === "Escape") toolsCloseCatalogPicker();
}

function toolsCloseCatalogPicker() {
  document.getElementById("tools-catalog-picker-modal")?.remove();
  document.removeEventListener("keydown", toolsCatalogPickerKeydown);
  toolsState.catalogPicker = null;
}

function toolsSearchCatalogPicker() {
  const picker = toolsState.catalogPicker;
  const input = document.getElementById("tools-catalog-picker-search");
  if (!picker || !input) return;
  picker.search = input.value.trim();
  picker.page = 0;
  toolsLoadCatalogPicker();
}

async function toolsLoadCatalogPicker() {
  const picker = toolsState.catalogPicker;
  const container = document.getElementById("tools-catalog-picker-results");
  if (!picker || !container) return;
  picker.loading = true;
  container.innerHTML = `<p class="text-slate-400">Carregando catálogo...</p>`;
  try {
    if (picker.kind === "item") {
      const result = await apiGet("/items", { search: picker.search, page: picker.page, size: picker.pageSize });
      picker.items = (result.items || []).map(item => ({ ...item, name: item.name || item.code }));
      picker.totalItems = Number(result.totalItems || 0);
      picker.totalPages = Math.max(1, Number(result.totalPages || 1));
      picker.hasNext = Boolean(result.hasNext);
      picker.hasPrevious = Boolean(result.hasPrevious);
    } else {
      if (toolsState.equipmentTemplates.length === 0) toolsState.equipmentTemplates = await apiGet("/admin/equipment-templates", { activeOnly: true });
      const term = picker.search.toLowerCase();
      const filtered = toolsState.equipmentTemplates.filter(template => [template.name, template.slot, template.rarity, template.set, `T${template.tier}`]
        .filter(Boolean).some(value => String(value).toLowerCase().includes(term)));
      picker.totalItems = filtered.length;
      picker.totalPages = Math.max(1, Math.ceil(filtered.length / picker.pageSize));
      picker.page = Math.min(picker.page, picker.totalPages - 1);
      picker.items = filtered.slice(picker.page * picker.pageSize, (picker.page + 1) * picker.pageSize);
      picker.hasPrevious = picker.page > 0;
      picker.hasNext = picker.page < picker.totalPages - 1;
    }
    picker.error = "";
  } catch (error) {
    picker.error = error.message || "Não foi possível carregar o catálogo.";
    picker.items = [];
    picker.totalItems = 0;
    picker.totalPages = 1;
    picker.hasNext = false;
    picker.hasPrevious = false;
  } finally {
    picker.loading = false;
    toolsRenderCatalogPicker();
  }
}

function toolsRenderCatalogPicker() {
  const picker = toolsState.catalogPicker;
  const container = document.getElementById("tools-catalog-picker-results");
  if (!picker || !container) return;
  const isItem = picker.kind === "item";
  const results = picker.items.length
    ? picker.items.map(entry => {
      const subtitle = isItem
        ? `${entry.category || "Item"} · ${entry.rarity || "-"} · ${entry.code}`
        : `${entry.slot || "-"} · ${entry.rarity || "-"} · T${entry.tier || "-"} · HP ${entry.bonusHp || 0} · ATK ${entry.bonusAttack || 0} · DEF ${entry.bonusDefense || 0}`;
      const value = isItem ? entry.code : entry.name;
      return `<button type="button" class="card-sm w-full text-left hover:border-cyan-600" data-tools-picker-value="${escapeAttr(value)}">
        <div class="flex items-center justify-between gap-3"><span class="min-w-0"><span class="block ${isItem ? "text-cyan-300" : "text-purple-300"} font-medium break-words">${escapeHtml(entry.name)}</span><span class="block text-xs text-slate-500">${escapeHtml(subtitle)}</span></span><span class="text-xs text-slate-400 shrink-0">Selecionar</span></div>
      </button>`;
    }).join("")
    : `<p class="text-slate-500">Nenhum registro encontrado.</p>`;
  const errorNotice = picker.error ? `<p class="text-xs text-red-300 mb-2">${escapeHtml(picker.error)}</p>` : "";
  container.innerHTML = `${errorNotice}
    <div class="flex items-center justify-between gap-3 mb-2"><p class="text-xs text-slate-500">${picker.totalItems} registro(s) encontrado(s)</p><div class="flex items-center gap-2"><button type="button" class="btn-secondary text-xs" data-tools-picker-previous ${!picker.hasPrevious ? "disabled" : ""}>Anterior</button><span class="text-xs text-slate-400 whitespace-nowrap">Página ${picker.page + 1} de ${picker.totalPages}</span><button type="button" class="btn-secondary text-xs" data-tools-picker-next ${!picker.hasNext ? "disabled" : ""}>Próxima</button></div></div>
    <div class="space-y-2">${results}</div>`;
  container.querySelector("[data-tools-picker-previous]")?.addEventListener("click", () => { if (picker.hasPrevious) { picker.page--; toolsLoadCatalogPicker(); } });
  container.querySelector("[data-tools-picker-next]")?.addEventListener("click", () => { if (picker.hasNext) { picker.page++; toolsLoadCatalogPicker(); } });
  container.querySelectorAll("[data-tools-picker-value]").forEach(button => button.addEventListener("click", () => {
    const selected = picker.items.find(entry => (isItem ? entry.code : entry.name) === button.dataset.toolsPickerValue);
    if (!selected) return;
    if (isItem) toolsState.selectedItem = selected; else toolsState.selectedEquipment = selected;
    toolsRenderCatalogSelections();
    toolsCloseCatalogPicker();
  }));
}

function toolsLoadSelects() {
  return Promise.resolve();
}
