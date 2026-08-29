let clanListPage = 0;
let clanListQuery = "";
let clanListEntries = [];
let clanListHasMore = true;
let clanListLoading = false;

let clanRankingPage = 0;
let clanRankingEntries = [];
let clanRankingHasMore = true;
let clanRankingLoading = false;

let currentClan = null;
let currentClanTab = "members";
let clanRaidCooldownTimer = null;
let clanStorageDepositItems = [];
let clanStorageDepositSearch = "";
let clanStorageDepositPage = 0;
let clanStorageDepositSelectedItemId = null;
const clanStorageDepositPageSize = 6;
let clanStorageHistory = [];
let clanStorageHistoryPage = 0;
const clanStorageHistoryPageSize = 8;
let clanStorageItems = [];
let clanStorageItemsSearch = "";
let clanStorageItemsPage = 0;
const clanStorageItemsPageSize = 8;

const clanActionHandlers = {
  promote: clanPromote,
  demote: clanDemote,
  kick: clanKick,
  transfer: clanTransfer
};

function safeContent(id) {
  return document.getElementById(id);
}

function setHtml(id, html) {
  const el = safeContent(id);
  if (el) el.innerHTML = html;
  return el;
}

async function renderClansPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div id="clan-content">
        <div class="card animate-pulse"><div class="h-40"></div></div>
      </div>
    </div>
  `;

  try {
    const myClan = await apiGet("/clans/me");
    if (myClan) {
      renderClanDetail(myClan);
      return;
    }
  } catch (err) {
    // not in a clan — continue to listing
  }

  renderClanList();
  await clanLoadList();
}

function renderClanList() {
  const el = setHtml("clan-content", `
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-lg font-bold px-1">Clãs</h2>
      <button class="btn-primary text-sm py-1.5 px-3" onclick="renderClanCreate()">Criar Clã</button>
    </div>

    <div class="flex gap-2 mb-4">
      <input type="text" id="clan-search" class="input flex-1" placeholder="Buscar por nome ou tag..." value="${escapeHtml(clanListQuery)}" onkeydown="if(event.key==='Enter')clanSearch()">
      <button class="btn-secondary" onclick="clanSearch()">Buscar</button>
    </div>

    <div id="clan-list">
      <div class="card animate-pulse"><div class="h-32"></div></div>
    </div>

    <button class="btn-primary w-full mt-4" onclick="renderClanRanking()">Ver Classificação de Clãs</button>
  `);
  return el;
}

async function clanLoadList() {
  if (clanListLoading) return;
  clanListLoading = true;

  const list = safeContent("clan-list");
  if (list && clanListPage === 0) {
    list.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  try {
    const data = await apiGet(`/clans?query=${encodeURIComponent(clanListQuery)}&page=${clanListPage}&size=10`);
    const newEntries = data.content || [];
    clanListEntries = clanListPage === 0 ? newEntries : [...clanListEntries, ...newEntries];
    clanListHasMore = !data.last;
    clanRenderList();
  } catch (err) {
    if (list) list.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  } finally {
    clanListLoading = false;
  }
}

function clanRenderList() {
  const list = safeContent("clan-list");
  if (!list) return;

  if (clanListEntries.length === 0) {
    list.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum clã encontrado.</p>`;
    return;
  }

  let html = clanListEntries.map(c => `
    <div class="card-sm mb-2 flex items-center gap-3 cursor-pointer" onclick="clanOpenDetail('${c.id}')">
      <div class="flex-1 min-w-0">
        <p class="font-bold text-sm truncate">${escapeHtml(c.name)} <span class="text-cyan-400">[${escapeHtml(c.tag)}]</span></p>
        <p class="text-xs text-slate-400 truncate">${escapeHtml(c.description || "Sem descrição")}</p>
        <p class="text-xs text-slate-500 mt-1">👥 ${c.memberCount}/${c.maxMembers} · Nível ${c.level}</p>
      </div>
      <div class="flex items-center gap-2">
        <button class="text-slate-500 text-lg" onclick="event.stopPropagation(); clanShowPreview('${c.id}')">👁️</button>
        <button class="btn-primary text-xs py-1 px-2" onclick="event.stopPropagation(); clanJoin('${c.id}')">Entrar</button>
      </div>
    </div>
  `).join("");

  if (clanListHasMore) {
    html += `<button class="btn-secondary w-full mt-2" id="clan-load-more" onclick="clanLoadMore()">Carregar mais</button>`;
  }

  list.innerHTML = html;
}

function clanSearch() {
  const input = document.getElementById("clan-search");
  clanListQuery = input ? input.value : "";
  clanListPage = 0;
  clanListEntries = [];
  clanListHasMore = true;
  clanLoadList();
}

async function clanLoadMore() {
  clanListPage++;
  await clanLoadList();
}

async function clanOpenDetail(id) {
  try {
    const clan = await apiGet(`/clans/${id}`);
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanShowPreview(id) {
  try {
    const clan = await apiGet(`/clans/${id}`);
    const overlay = document.createElement("div");
    overlay.id = "clan-preview-overlay";
    overlay.className = "fixed inset-0 z-50 flex items-end justify-center";
    overlay.style.background = "rgba(0,0,0,0.6)";
    overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

    overlay.innerHTML = `
      <div class="w-full max-w-md rounded-t-2xl p-4 pb-8" style="background:#0f172a;max-height:85vh;overflow-y:auto">
        <div class="flex justify-between items-center mb-3">
          <h3 class="font-bold text-lg">Pré-visualização do Clã</h3>
          <button class="text-slate-400 text-xl" onclick="document.getElementById('clan-preview-overlay').remove()">&times;</button>
        </div>
        <div id="clan-preview-body"></div>
      </div>
    `;

    document.body.appendChild(overlay);
    setHtml("clan-preview-body", renderClanDetailHtml(clan, { preview: true }));
  } catch (err) {
    showToast(err.message, "error");
  }
}

function renderClanDetail(clan) {
  currentClan = clan;
  currentClanTab = "members";
  setHtml("clan-content", renderClanDetailHtml(clan, { preview: false }));
  clanShowTab("members");
}

function clanShowTab(tab) {
  currentClanTab = tab;
  const container = safeContent("clan-tab-content");
  if (!container) return;

  document.querySelectorAll(".clan-tab-btn").forEach(b => {
    b.classList.remove("bg-cyan-600", "text-white");
    b.classList.add("bg-slate-800", "text-slate-300");
  });
  const activeBtn = safeContent(`clan-tab-${tab}`);
  if (activeBtn) {
    activeBtn.classList.remove("bg-slate-800", "text-slate-300");
    activeBtn.classList.add("bg-cyan-600", "text-white");
  }

  if (tab === "members") {
    clanRenderMembersTab();
  } else if (tab === "upgrades") {
    clanLoadUpgrades();
  } else if (tab === "storage") {
    clanLoadStorage();
  } else if (tab === "missions") {
    clanLoadMissions();
  } else if (tab === "raid") {
    clanLoadRaid();
  }
}

function renderClanDetailHtml(clan, opts = {}) {
  const preview = !!opts.preview;
  const isLeader = clan.myRole && clan.myRole.role === "LEADER";
  const isOfficer = clan.myRole && clan.myRole.role === "OFFICER";
  const canManage = (isLeader || isOfficer) && !preview;

  const xpPercent = clan.xpToNextLevel > 0
    ? Math.min(100, Math.round((clan.experience / (clan.experience + clan.xpToNextLevel)) * 100))
    : 100;

  const backButton = !clan.isMember && !preview
    ? `<div class="mb-4"><button class="text-sm text-slate-400" onclick="renderClansPage()">← Voltar</button></div>`
    : "";

  const managementButtons = !preview && clan.isMember
    ? `
      <div class="flex gap-2 mt-4">
        ${!isLeader ? `<button class="btn-secondary flex-1" onclick="clanLeave('${clan.id}')">Sair do Clã</button>` : ""}
        ${isLeader ? `<button class="btn-red flex-1" onclick="clanDissolve('${clan.id}')">Dissolver Clã</button>` : ""}
      </div>
    `
    : "";

  const showMemberTabs = clan.isMember && !preview;

  return `
    ${backButton}

    <div class="card mb-4">
      <div class="flex items-start justify-between">
        <div>
          <h2 class="text-lg font-bold">${escapeHtml(clan.name)} <span class="text-cyan-400">[${escapeHtml(clan.tag)}]</span></h2>
          <p class="text-sm text-slate-400">${escapeHtml(clan.description || "Sem descrição")}</p>
        </div>
        ${canManage ? `<button class="text-xs text-cyan-400" onclick="clanEdit('${clan.id}')">Editar</button>` : ""}
      </div>

      <div class="grid grid-cols-2 sm:grid-cols-3 gap-2 mt-4 text-center">
        <div class="card-sm"><p class="text-xs text-slate-500">Nível</p><p class="font-bold text-cyan-400">${clan.level}</p></div>
        <div class="card-sm"><p class="text-xs text-slate-500">Membros</p><p class="font-bold">${clan.memberCount}/${clan.maxMembers}</p></div>
        <div class="card-sm"><p class="text-xs text-slate-500">Vagas base</p><p class="font-bold text-green-400">${clan.baseMaxMembers}</p></div>
        <div class="card-sm"><p class="text-xs text-slate-500">Vagas extras</p><p class="font-bold text-amber-400">+${clan.memberCapacityUpgradeLevel}</p></div>
        <div class="card-sm col-span-2 sm:col-span-1"><p class="text-xs text-slate-500">Marcas de Honra</p><p class="font-bold text-purple-400">${clan.honorMarks}</p></div>
      </div>

      <div class="mt-4">
        <div class="flex justify-between text-xs mb-1">
          <span class="text-slate-400">Experiência do Clã</span>
          <span class="text-slate-400">${clan.xpToNextLevel > 0 ? clan.xpToNextLevel + " para o próximo" : "Máximo"}</span>
        </div>
        <div class="w-full bg-slate-800 rounded-full h-2"><div class="bg-cyan-500 h-2 rounded-full" style="width:${xpPercent}%"></div></div>
      </div>
    </div>

    <div class="clan-tabs ${showMemberTabs ? "clan-tabs--full" : "clan-tabs--compact"} mb-3">
      <button id="clan-tab-members" class="clan-tab-btn font-bold bg-cyan-600 text-white" onclick="clanShowTab('members')">Membros</button>
      <button id="clan-tab-upgrades" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('upgrades')">Melhorias</button>
      ${showMemberTabs ? `<button id="clan-tab-storage" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('storage')">Armazém</button>` : ""}
      ${showMemberTabs ? `<button id="clan-tab-missions" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('missions')">Missões</button>` : ""}
      ${showMemberTabs ? `<button id="clan-tab-raid" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('raid')">Incursão</button>` : ""}
    </div>

    <div id="clan-tab-content" class="mb-4"></div>

    ${managementButtons}

    ${!preview ? `<button class="btn-primary w-full mt-4" onclick="renderClanRanking()">Classificação de Clãs</button>` : ""}
  `;
}

function clanRenderMembersTab() {
  const clan = currentClan;
  if (!clan) return;
  const container = safeContent("clan-tab-content");
  if (!container) return;

  const isLeader = clan.myRole && clan.myRole.role === "LEADER";
  const isOfficer = clan.myRole && clan.myRole.role === "OFFICER";
  const canManage = isLeader || isOfficer;
  const currentPlayerId = getPlayerId() || "";

  const membersHtml = clan.members.map(m => {
    const roleLabel = { LEADER: "Líder", OFFICER: "Oficial", MEMBER: "Membro" }[m.role] || m.role;
    const isSelf = m.id === currentPlayerId;
    let actions = "";
    if (canManage && !isSelf && m.role !== "LEADER") {
      if (isLeader && m.role === "MEMBER") {
        actions += `<button class="text-xs text-cyan-400 ml-2" data-clan-action="promote" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Promover</button>`;
      }
      if (isLeader && m.role === "OFFICER") {
        actions += `<button class="text-xs text-amber-400 ml-2" data-clan-action="demote" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Rebaixar</button>`;
      }
      if ((isLeader && m.role !== "LEADER") || (isOfficer && m.role === "MEMBER")) {
        actions += `<button class="text-xs text-red-400 ml-2" data-clan-action="kick" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Expulsar</button>`;
      }
      if (isLeader) {
        actions += `<button class="text-xs text-amber-300 ml-2" data-clan-action="transfer" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Transferir</button>`;
      }
    }
    return `
      <div class="card-sm mb-2 flex items-center justify-between">
        <div>
          <p class="font-bold text-sm">${escapeHtml(m.username)} <span class="text-xs text-slate-400">(${escapeHtml(roleLabel)})</span></p>
          ${m.activeDigimonPower ? `<p class="text-xs text-slate-500">⚔️ Poder ${m.activeDigimonPower}</p>` : ""}
        </div>
        <div class="text-right">${actions}</div>
      </div>
    `;
  }).join("");

  const inviteHtml = canManage ? `
    <form class="card-sm mb-4" onsubmit="clanInvite(event, '${clan.id}')">
      <p class="font-bold text-sm mb-1">Convidar jogador</p>
      <p class="text-xs text-slate-400 mb-2">O jogador receberá um convite no Correio e poderá aceitar ou recusar.</p>
      <div class="flex gap-2">
        <input id="clan-invite-username" class="input flex-1 min-w-0" maxlength="30" required placeholder="Nome do jogador">
        <button class="btn-primary text-sm" type="submit">Convidar</button>
      </div>
    </form>
  ` : "";
  container.innerHTML = `${inviteHtml}<h3 class="font-bold mb-2">Membros</h3>${membersHtml}`;
  container.querySelectorAll("[data-clan-action]").forEach(button => {
    const action = button.dataset.clanAction;
    const id = button.dataset.clanId;
    const username = button.dataset.clanUsername;
    button.addEventListener("click", () => clanActionHandlers[action]?.(id, username));
  });
}

async function clanLoadUpgrades() {
  const clan = currentClan;
  const container = safeContent("clan-tab-content");
  if (!container || !clan) return;
  container.innerHTML = `<div class="card animate-pulse"><div class="h-24"></div></div>`;

  try {
    const upgrades = await apiGet(`/clans/${clan.id}/upgrades`);
    const isLeader = clan.myRole && clan.myRole.role === "LEADER";

    let html = `<h3 class="font-bold mb-2">Melhorias</h3>`;
    if (upgrades.length === 0) {
      html += `<p class="text-slate-400 text-sm">Nenhuma melhoria disponível.</p>`;
    } else {
      html += upgrades.map(u => {
        const locked = !u.unlocked;
        const maxed = u.maxed;
        const canBuy = isLeader && u.unlocked && !maxed && clan.honorMarks >= u.nextCostHonorMarks;
        return `
          <div class="card-sm mb-2 ${locked ? 'opacity-60' : ''}">
            <div class="flex justify-between items-start">
              <div>
                <p class="font-bold text-sm">${escapeHtml(u.name)} <span class="text-xs text-slate-400">Nv ${u.currentLevel}/${u.maxLevel}</span></p>
                <p class="text-xs text-slate-400">${escapeHtml(u.description || "")}</p>
                <p class="text-xs text-slate-500">Efeito total: <span class="text-cyan-400">${formatEffect(u)}</span></p>
              </div>
              <div class="text-right">
                ${locked ? `<span class="text-xs text-slate-500">Nv clã ${u.unlockedAtClanLevel}</span>` : ""}
                ${!locked && !maxed ? `<p class="text-xs ${canBuy ? 'text-green-400' : 'text-slate-400'}">${u.nextCostHonorMarks} HM</p>` : ""}
                ${maxed ? `<p class="text-xs text-amber-400">Max</p>` : ""}
              </div>
            </div>
            ${isLeader && !locked && !maxed ? `
              <button class="btn-primary w-full mt-2 text-sm py-1 ${canBuy ? '' : 'opacity-50 cursor-not-allowed'}" 
                ${canBuy ? `onclick="clanBuyUpgrade('${clan.id}', '${escapeHtml(u.code)}')"` : 'disabled'}>
                Melhorar (${u.nextCostHonorMarks} HM)
              </button>
            ` : ""}
          </div>
        `;
      }).join("");
    }
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

function formatEffect(u) {
  if (u.code === "MAX_ENERGY_BONUS" || u.code === "MEMBER_CAPACITY" || u.code === "CLAN_STORAGE_CAPACITY") {
    return `+${Math.round(u.totalEffect)}${u.code === "CLAN_STORAGE_CAPACITY" ? " slots" : ""}`;
  }
  return `+${(u.totalEffect * 100).toFixed(0)}%`;
}

async function clanBuyUpgrade(clanId, code) {
  try {
    await apiPost(`/clans/${clanId}/upgrades/${encodeURIComponent(code)}/buy`);
    showToast("Melhoria adquirida!", "success");
    const clan = await apiGet(`/clans/${clanId}`);
    renderClanDetail(clan);
    clanShowTab("upgrades");
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanLoadStorage() {
  const clan = currentClan;
  const container = safeContent("clan-tab-content");
  if (!container || !clan) return;
  container.innerHTML = `<div class="card animate-pulse"><div class="h-48"></div></div>`;

  try {
    const [storage, inventory] = await Promise.all([
      apiGet(`/clans/${clan.id}/storage`),
      apiGet("/inventory")
    ]);
    const isLeader = clan.myRole && clan.myRole.role === "LEADER";
    const isOfficer = clan.myRole && clan.myRole.role === "OFFICER";
    const canWithdraw = isLeader || isOfficer;
    const depositableItems = (inventory || []).filter(item => item.quantity > 0 && item.itemDefinition && item.itemDefinition.tradable && item.itemDefinition.id);
    clanStorageDepositItems = depositableItems;
    clanStorageDepositSelectedItemId = null;
    clanStorageHistory = Array.isArray(storage.history) ? storage.history : [];
    clanStorageHistoryPage = 0;
    clanStorageItems = Array.isArray(storage.items) ? storage.items : [];
    clanStorageItemsSearch = "";
    clanStorageItemsPage = 0;

    let html = `
      <div class="flex items-center justify-between mb-2">
        <h3 class="font-bold">Armazém do Clã</h3>
        <span class="text-xs text-slate-400">${storage.usedSlots}/${storage.capacity} slots</span>
      </div>
      <div class="card-sm mb-3">
        <div class="flex justify-between text-xs mb-1"><span class="text-slate-400">Capacidade utilizada</span><span class="text-cyan-400">${storage.availableSlots} livres</span></div>
        <div class="w-full bg-slate-800 rounded-full h-2"><div class="bg-cyan-500 h-2 rounded-full" style="width:${storage.capacity ? Math.min(100, Math.round(storage.usedSlots / storage.capacity * 100)) : 0}%"></div></div>
      </div>
    `;

    html += `
      <div class="card-sm mb-4">
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <p class="font-bold text-sm mb-1">Depositar item</p>
            <p class="text-xs text-slate-400">Qualquer membro pode depositar itens negociáveis do próprio inventário.</p>
          </div>
          <button type="button" class="btn-primary text-sm shrink-0" ${depositableItems.length ? "" : "disabled"} onclick="clanOpenStorageDepositModal()">Depositar</button>
        </div>
        ${depositableItems.length
          ? `<p class="text-xs text-slate-500 mt-3">Use a busca para localizar rapidamente o item que deseja enviar ao armazém.</p>`
          : `<p class="text-xs text-slate-500 mt-3">Nenhum item negociável disponível para depósito.</p>`}
      </div>
    `;

    html += `
      <div class="flex items-center justify-between gap-3 mb-2">
        <h4 class="font-bold text-sm">Itens armazenados</h4>
        <span class="text-xs text-slate-500">${clanStorageItems.length} ${clanStorageItems.length === 1 ? "item armazenado" : "itens armazenados"}</span>
      </div>
      <form id="clan-storage-items-search-form" class="flex flex-col sm:flex-row gap-2 mb-3" onsubmit="clanStorageSearchItems(event)">
        <input id="clan-storage-items-search" class="input flex-1 min-w-0" type="search" placeholder="Buscar item armazenado..." aria-label="Buscar item armazenado" autocomplete="off">
        <button type="submit" class="btn-primary w-full sm:w-auto shrink-0">Buscar</button>
      </form>
      <div id="clan-storage-items-list">${clanStorageRenderItemsHtml()}</div>
    `;

    html += `<details class="card-sm mt-4"><summary class="cursor-pointer font-bold text-sm">Histórico de movimentações</summary><div id="clan-storage-history-list" class="mt-3">${clanStorageRenderHistoryHtml()}</div></details>`;
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

function clanStorageFilteredItems() {
  if (!clanStorageItemsSearch) return clanStorageItems;
  return clanStorageItems.filter(item => {
    return [item.name, item.code, item.category]
      .filter(Boolean)
      .some(value => String(value).toLowerCase().includes(clanStorageItemsSearch));
  });
}

function clanStorageRenderItemsHtml() {
  const filteredItems = clanStorageFilteredItems();
  if (filteredItems.length === 0) {
    return clanStorageItems.length === 0
      ? `<p class="text-slate-400 text-sm mb-4">O armazém está vazio.</p>`
      : `<p class="text-slate-500 text-sm mb-4">Nenhum item encontrado para essa busca.</p>`;
  }

  const totalPages = Math.max(1, Math.ceil(filteredItems.length / clanStorageItemsPageSize));
  clanStorageItemsPage = Math.min(clanStorageItemsPage, totalPages - 1);
  const start = clanStorageItemsPage * clanStorageItemsPageSize;
  const pageItems = filteredItems.slice(start, start + clanStorageItemsPageSize);
  const end = start + pageItems.length;
  const itemLabel = filteredItems.length === 1 ? "item" : "itens";
  const canWithdraw = currentClan && currentClan.myRole && ["LEADER", "OFFICER"].includes(currentClan.myRole.role);
  const clanId = currentClan ? currentClan.id : "";
  const rows = pageItems.map(item => `
    <div class="card-sm mb-2 flex items-center gap-3 overflow-hidden">
      ${item.icon ? `<img src="${escapeAttr(item.icon)}" class="w-8 h-8 object-contain shrink-0" onerror="this.style.display='none'">` : ""}
      <div class="flex-1 min-w-0">
        <p class="font-bold text-sm truncate">${escapeHtml(item.name || item.code)}</p>
        <p class="text-xs text-slate-400 truncate">Quantidade: ${item.quantity}${item.maxStack ? `/${item.maxStack}` : ""}</p>
      </div>
      ${canWithdraw ? `<button class="btn-secondary text-xs py-1 px-2 shrink-0" onclick="clanStorageWithdraw('${clanId}', ${item.itemDefinitionId}, '${escapeAttr(item.name || item.code)}', ${item.quantity})">Retirar</button>` : ""}
    </div>
  `).join("");

  return `
    <div class="flex flex-wrap items-center justify-between gap-2 mb-2">
      <p class="text-xs text-slate-500">${start + 1}-${end} de ${filteredItems.length} ${itemLabel}</p>
      ${totalPages > 1 ? `
        <div class="flex items-center gap-2">
          <button type="button" class="btn-secondary text-xs" onclick="clanStorageChangeItemsPage(-1)" ${clanStorageItemsPage === 0 ? "disabled" : ""}>Anterior</button>
          <span class="text-xs text-slate-400 whitespace-nowrap">${clanStorageItemsPage + 1}/${totalPages}</span>
          <button type="button" class="btn-secondary text-xs" onclick="clanStorageChangeItemsPage(1)" ${clanStorageItemsPage >= totalPages - 1 ? "disabled" : ""}>Próxima</button>
        </div>
      ` : ""}
    </div>
    <div>${rows}</div>
  `;
}

function clanStorageSearchItems(event) {
  event.preventDefault();
  clanStorageItemsSearch = document.getElementById("clan-storage-items-search")?.value.trim().toLowerCase() || "";
  clanStorageItemsPage = 0;
  const container = document.getElementById("clan-storage-items-list");
  if (container) container.innerHTML = clanStorageRenderItemsHtml();
}

function clanStorageChangeItemsPage(delta) {
  const filteredItems = clanStorageFilteredItems();
  const totalPages = Math.max(1, Math.ceil(filteredItems.length / clanStorageItemsPageSize));
  clanStorageItemsPage = Math.max(0, Math.min(totalPages - 1, clanStorageItemsPage + delta));
  const container = document.getElementById("clan-storage-items-list");
  if (container) container.innerHTML = clanStorageRenderItemsHtml();
}

function clanStorageRenderHistoryHtml() {
  if (clanStorageHistory.length === 0) {
    return `<p class="text-slate-500 text-xs">Nenhuma movimentação registrada.</p>`;
  }

  const totalPages = Math.max(1, Math.ceil(clanStorageHistory.length / clanStorageHistoryPageSize));
  clanStorageHistoryPage = Math.min(clanStorageHistoryPage, totalPages - 1);
  const start = clanStorageHistoryPage * clanStorageHistoryPageSize;
  const pageEntries = clanStorageHistory.slice(start, start + clanStorageHistoryPageSize);
  const end = start + pageEntries.length;
  const rows = pageEntries.map(entry => `
    <div class="border-t border-slate-800 py-2 text-xs">
      <div class="flex justify-between gap-2"><span class="${entry.action === "DEPOSIT" ? "text-green-400" : "text-amber-400"}">${entry.action === "DEPOSIT" ? "Depósito" : "Retirada"}</span><span class="text-slate-500">${new Date(entry.createdAt).toLocaleString()}</span></div>
      <p class="text-slate-300">${escapeHtml(entry.actorUsername)} · ${escapeHtml(entry.itemName)} × ${entry.quantity}</p>
    </div>
  `).join("");

  return `
    <div class="flex items-center justify-between gap-3 mb-2">
      <p class="text-xs text-slate-500">${start + 1}-${end} de ${clanStorageHistory.length} movimentação(ões)</p>
      ${totalPages > 1 ? `
        <div class="flex items-center gap-2">
          <button type="button" class="btn-secondary text-xs" onclick="clanStorageChangeHistoryPage(-1)" ${clanStorageHistoryPage === 0 ? "disabled" : ""}>Anterior</button>
          <span class="text-xs text-slate-400 whitespace-nowrap">${clanStorageHistoryPage + 1}/${totalPages}</span>
          <button type="button" class="btn-secondary text-xs" onclick="clanStorageChangeHistoryPage(1)" ${clanStorageHistoryPage >= totalPages - 1 ? "disabled" : ""}>Próxima</button>
        </div>
      ` : ""}
    </div>
    <div>${rows}</div>
  `;
}

function clanStorageChangeHistoryPage(delta) {
  const totalPages = Math.max(1, Math.ceil(clanStorageHistory.length / clanStorageHistoryPageSize));
  clanStorageHistoryPage = Math.max(0, Math.min(totalPages - 1, clanStorageHistoryPage + delta));
  const container = document.getElementById("clan-storage-history-list");
  if (container) container.innerHTML = clanStorageRenderHistoryHtml();
}

async function clanStorageDeposit(event, clanId) {
  event.preventDefault();
  const itemDefinitionId = Number(document.getElementById("clan-storage-deposit-item")?.value);
  const quantity = Number(document.getElementById("clan-storage-deposit-quantity")?.value);
  const item = clanStorageDepositItems.find(entry => Number(entry.itemDefinition?.id) === itemDefinitionId);
  if (!item || !Number.isInteger(itemDefinitionId) || itemDefinitionId <= 0) {
    showToast("Selecione um item negociável para depositar.", "error");
    return;
  }
  if (!Number.isInteger(quantity) || quantity < 1 || quantity > item.quantity) {
    showToast("Quantidade inválida.", "error");
    return;
  }

  const button = document.getElementById("clan-storage-deposit-submit");
  if (button) {
    button.disabled = true;
    button.textContent = "Depositando...";
  }
  try {
    await apiPost(`/clans/${clanId}/storage/deposit`, { itemDefinitionId, quantity });
    showToast("Item depositado no armazém.", "success");
    clanCloseStorageDepositModal();
    await clanLoadStorage();
  } catch (err) {
    showToast(err.message, "error");
    if (button) {
      button.disabled = false;
      button.textContent = "Depositar item";
    }
  }
}

function clanOpenStorageDepositModal() {
  if (!clanStorageDepositItems.length || document.getElementById("clan-storage-deposit-modal")) return;
  clanStorageDepositSearch = "";
  clanStorageDepositPage = 0;
  clanStorageDepositSelectedItemId = null;

  const overlay = document.createElement("div");
  overlay.id = "clan-storage-deposit-modal";
  overlay.className = "shop-modal-overlay";
  overlay.innerHTML = `
    <div class="shop-modal auction-item-modal clan-storage-deposit-modal" role="dialog" aria-modal="true" aria-labelledby="clan-storage-deposit-modal-title">
      <div class="flex items-start justify-between gap-4 mb-5">
        <div>
          <h3 id="clan-storage-deposit-modal-title" class="text-xl font-bold">Depositar item</h3>
          <p class="text-sm text-slate-400 mt-1">Pesquise e selecione um item negociável do seu inventário.</p>
        </div>
        <button type="button" class="text-slate-400 hover:text-white text-2xl" aria-label="Fechar" data-clan-storage-deposit-close>&times;</button>
      </div>
      <label class="block text-xs text-slate-400 mb-2" for="clan-storage-deposit-search">Buscar item</label>
      <input id="clan-storage-deposit-search" class="input mb-4" type="search" placeholder="Nome ou código do item" autocomplete="off">
      <div id="clan-storage-deposit-modal-results" class="space-y-2 min-h-48"></div>
      <div id="clan-storage-deposit-selected" class="mt-4"></div>
      <form id="clan-storage-deposit-form" class="mt-4 border-t border-slate-800 pt-4">
        <label class="block text-xs text-slate-400" for="clan-storage-deposit-quantity">Quantidade</label>
        <input id="clan-storage-deposit-item" type="hidden">
        <input id="clan-storage-deposit-quantity" class="input w-full mt-1" type="number" min="1" value="1" required disabled>
        <button id="clan-storage-deposit-submit" class="btn-primary w-full mt-3" type="submit" disabled>Depositar item</button>
      </form>
    </div>
  `;
  document.body.appendChild(overlay);

  overlay.querySelector("[data-clan-storage-deposit-close]").addEventListener("click", clanCloseStorageDepositModal);
  overlay.querySelector("#clan-storage-deposit-search").addEventListener("input", event => {
    clanStorageDepositSearch = event.target.value.trim().toLowerCase();
    clanStorageDepositPage = 0;
    clanStorageRenderDepositModalResults();
  });
  overlay.querySelector("#clan-storage-deposit-form").addEventListener("submit", event => clanStorageDeposit(event, currentClan.id));
  overlay.addEventListener("click", event => { if (event.target === overlay) clanCloseStorageDepositModal(); });
  document.addEventListener("keydown", clanHandleStorageDepositModalKeydown);
  clanStorageRenderDepositModalResults();
  clanStorageRenderDepositSelection();
  overlay.querySelector("#clan-storage-deposit-search").focus();
}

function clanHandleStorageDepositModalKeydown(event) {
  if (event.key === "Escape") clanCloseStorageDepositModal();
}

function clanCloseStorageDepositModal() {
  document.getElementById("clan-storage-deposit-modal")?.remove();
  document.removeEventListener("keydown", clanHandleStorageDepositModalKeydown);
}

function clanStorageFilteredDepositItems() {
  if (!clanStorageDepositSearch) return clanStorageDepositItems;
  return clanStorageDepositItems.filter(item => {
    const definition = item.itemDefinition || {};
    return [definition.name, definition.code, definition.category]
      .filter(Boolean)
      .some(value => String(value).toLowerCase().includes(clanStorageDepositSearch));
  });
}

function clanStorageChangeDepositPage(delta) {
  const filteredItems = clanStorageFilteredDepositItems();
  const totalPages = Math.max(1, Math.ceil(filteredItems.length / clanStorageDepositPageSize));
  clanStorageDepositPage = Math.max(0, Math.min(totalPages - 1, clanStorageDepositPage + delta));
  clanStorageRenderDepositModalResults();
}

function clanStorageRenderDepositModalResults() {
  const container = document.getElementById("clan-storage-deposit-modal-results");
  if (!container) return;

  const filteredItems = clanStorageFilteredDepositItems();
  const totalPages = Math.max(1, Math.ceil(filteredItems.length / clanStorageDepositPageSize));
  clanStorageDepositPage = Math.min(clanStorageDepositPage, totalPages - 1);
  const start = clanStorageDepositPage * clanStorageDepositPageSize;
  const pageItems = filteredItems.slice(start, start + clanStorageDepositPageSize);
  const results = pageItems.length
    ? pageItems.map(item => {
      const definition = item.itemDefinition || {};
      const itemId = Number(definition.id);
      const selected = itemId === Number(clanStorageDepositSelectedItemId);
      return `
        <button type="button" class="card-sm w-full text-left clan-storage-deposit-option overflow-hidden ${selected ? "border-cyan-500 bg-cyan-950/30" : "hover:border-cyan-600"}" data-clan-storage-deposit-item-id="${escapeAttr(String(itemId))}">
          <div class="clan-storage-deposit-option-layout">
            <span class="w-9 h-9 shrink-0 rounded-lg bg-slate-800 flex items-center justify-center">${clanStorageDepositItemIcon(item)}</span>
            <span class="clan-storage-deposit-option-details">
              <span class="clan-storage-deposit-option-name text-cyan-300 font-medium">${escapeHtml(definition.name || definition.code || "Item")}</span>
              <span class="clan-storage-deposit-option-meta text-xs text-slate-500">${escapeHtml(clanStorageDepositCategoryLabel(definition.category))} · ${Number(item.quantity).toLocaleString("pt-BR")} disponível(is)</span>
            </span>
            <span class="clan-storage-deposit-option-action text-xs text-slate-400">${selected ? "Selecionado" : "Selecionar"}</span>
          </div>
        </button>
      `;
    }).join("")
    : `<p class="text-sm text-slate-500 py-6 text-center">Nenhum item negociável encontrado.</p>`;

  container.innerHTML = `
    <div class="flex items-center justify-between gap-3 mb-2">
      <p class="text-xs text-slate-500">${filteredItems.length} item(ns) encontrado(s)</p>
      ${totalPages > 1 ? `
        <div class="flex items-center gap-2">
          <button type="button" class="btn-secondary text-xs" data-clan-storage-deposit-page="-1" ${clanStorageDepositPage === 0 ? "disabled" : ""}>Anterior</button>
          <span class="text-xs text-slate-400 whitespace-nowrap">${clanStorageDepositPage + 1}/${totalPages}</span>
          <button type="button" class="btn-secondary text-xs" data-clan-storage-deposit-page="1" ${clanStorageDepositPage >= totalPages - 1 ? "disabled" : ""}>Próxima</button>
        </div>
      ` : ""}
    </div>
    <div class="space-y-2">${results}</div>
  `;

  container.querySelectorAll("[data-clan-storage-deposit-page]").forEach(button => {
    button.addEventListener("click", () => clanStorageChangeDepositPage(Number(button.dataset.clanStorageDepositPage)));
  });
  container.querySelectorAll("[data-clan-storage-deposit-item-id]").forEach(button => {
    button.addEventListener("click", () => clanStorageSelectDepositItem(button.dataset.clanStorageDepositItemId));
  });
}

function clanStorageDepositCategoryLabel(category) {
  return {
    CONSUMABLE: "Consumível",
    MATERIAL: "Material",
    EVOLUTION_MATERIAL: "Material de evolução",
    FRAGMENT: "Fragmento",
    DIGITAMA: "Digitama",
    INCUBATOR: "Incubadora",
    CHEST: "Baú"
  }[String(category || "").toUpperCase()] || "Item negociável";
}

function clanStorageDepositItemIcon(item) {
  const definition = item.itemDefinition || {};
  const icon = String(definition.icon || "").trim();
  const isImageUrl = icon.startsWith("http://")
    || icon.startsWith("https://")
    || icon.startsWith("/")
    || icon.startsWith("./")
    || icon.startsWith("../")
    || icon.startsWith("assets/")
    || /\.(png|jpe?g|gif|webp|svg)(\?.*)?$/i.test(icon);
  if (isImageUrl) {
    return `<img src="${escapeAttr(icon)}" alt="" class="w-7 h-7 object-contain" onerror="this.style.display='none'">`;
  }

  const emojiByIcon = {
    xp_disc_15: "💿",
    xp_disc_20: "💿",
    xp_disc_30: "💿",
    incubation_slot_unlock: "🔓",
    training_stone: "💎",
    potion_small: "🧪",
    data_core: "🔮",
    digitama_starter: "🥚",
    incubator_common: "📦",
    incubator_rare: "📦",
    incubator_epic: "📦"
  };
  const emojiByCategory = {
    CONSUMABLE: "🧪",
    MATERIAL: "🧱",
    EVOLUTION_MATERIAL: "🧬",
    FRAGMENT: "🧩",
    DIGITAMA: "🥚",
    INCUBATOR: "📦",
    CHEST: "🗝️"
  };
  const emoji = emojiByIcon[icon.toLowerCase()] || emojiByCategory[String(definition.category || "").toUpperCase()] || "📦";
  return `<span class="text-xl leading-none" aria-hidden="true">${emoji}</span>`;
}

function clanStorageSelectDepositItem(itemDefinitionId) {
  const item = clanStorageDepositItems.find(entry => Number(entry.itemDefinition?.id) === Number(itemDefinitionId));
  if (!item) return;
  clanStorageDepositSelectedItemId = Number(item.itemDefinition.id);
  const hiddenInput = document.getElementById("clan-storage-deposit-item");
  if (hiddenInput) hiddenInput.value = item.itemDefinition.id;
  clanStorageRenderDepositModalResults();
  clanStorageRenderDepositSelection();
}

function clanStorageRenderDepositSelection() {
  const container = document.getElementById("clan-storage-deposit-selected");
  const quantityInput = document.getElementById("clan-storage-deposit-quantity");
  const submitButton = document.getElementById("clan-storage-deposit-submit");
  if (!container || !quantityInput || !submitButton) return;

  const item = clanStorageDepositItems.find(entry => Number(entry.itemDefinition?.id) === Number(clanStorageDepositSelectedItemId));
  if (!item) {
    container.innerHTML = `<p class="text-xs text-slate-500">Selecione um item acima para informar a quantidade.</p>`;
    quantityInput.value = "1";
    quantityInput.removeAttribute("max");
    quantityInput.disabled = true;
    submitButton.disabled = true;
    return;
  }

  const definition = item.itemDefinition || {};
  container.innerHTML = `
    <div class="card-sm flex items-center gap-3 border-cyan-800 bg-cyan-950/30">
      <span class="w-9 h-9 shrink-0 rounded-lg bg-slate-800 flex items-center justify-center">${clanStorageDepositItemIcon(item)}</span>
      <div class="min-w-0">
        <p class="font-semibold text-cyan-200 truncate">${escapeHtml(definition.name || definition.code || "Item")}</p>
        <p class="text-xs text-slate-400 mt-1">${Number(item.quantity).toLocaleString("pt-BR")} disponível(is)</p>
      </div>
      <span class="text-xs text-cyan-300 shrink-0">Selecionado</span>
    </div>
  `;
  quantityInput.max = String(item.quantity);
  quantityInput.disabled = false;
  if (!quantityInput.value || Number(quantityInput.value) > item.quantity) quantityInput.value = "1";
  submitButton.disabled = false;
}

async function clanStorageWithdraw(clanId, itemDefinitionId, itemName, availableQuantity) {
  const value = window.prompt(`Quantidade de ${itemName} para retirar (máximo ${availableQuantity}):`, "1");
  if (value === null) return;
  const quantity = Number(value);
  if (!Number.isInteger(quantity) || quantity < 1 || quantity > availableQuantity) {
    showToast("Quantidade inválida.", "error");
    return;
  }
  try {
    await apiPost(`/clans/${clanId}/storage/withdraw`, { itemDefinitionId, quantity });
    showToast("Item retirado do armazém.", "success");
    await clanLoadStorage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanLoadMissions() {
  const clan = currentClan;
  const container = safeContent("clan-tab-content");
  if (!container || !clan) return;
  container.innerHTML = `<div class="card animate-pulse"><div class="h-24"></div></div>`;

  try {
    const [catalog, myMission, ranking] = await Promise.all([
      apiGet("/clan-missions"),
      apiGet("/clan-missions/me").catch(() => null),
      apiGet("/clan-missions/ranking").catch(() => [])
    ]);

    const hasActive = myMission && myMission.status !== "CLAIMED";

    let html = `<h3 class="font-bold mb-2">Missões de Clã</h3>`;

    if (hasActive) {
      html += renderPlayerMission(myMission);
    }

    html += catalog.map(m => {
      const active = myMission && myMission.missionId === m.id && myMission.status !== "CLAIMED";
      const doneToday = m.alreadyAccepted && !active;
      const canAccept = !hasActive && !doneToday && !active && m.minClanLevel <= clan.level;
      return `
        <div class="card-sm mb-2 ${active || doneToday ? 'border-cyan-800' : ''}">
          <div class="flex justify-between items-start">
            <div>
              <p class="font-bold text-sm">${escapeHtml(m.title)}</p>
              <p class="text-xs text-slate-400">${escapeHtml(m.description || "")}</p>
              <p class="text-xs text-slate-500">Objetivo: <span class="text-cyan-400">${formatObjective(m.objectiveType)} ${m.targetValue}</span></p>
              <p class="text-xs text-slate-500">Recompensa: ${m.minHonorMarksReward}-${m.maxHonorMarksReward} Marcas de Honra · ${m.clanXpReward} Experiência do Clã</p>
            </div>
            <div class="text-right">
              ${active ? `<span class="text-xs text-cyan-400">Ativa</span>` : ""}
              ${doneToday ? `<span class="text-xs text-slate-500">Disponível amanhã</span>` : ""}
              ${!active && !doneToday && m.minClanLevel > clan.level ? `<span class="text-xs text-slate-500">Nv ${m.minClanLevel}</span>` : ""}
            </div>
          </div>
          ${canAccept ? `<button class="btn-primary w-full mt-2 text-sm py-1" onclick="clanAcceptMission('${m.id}')">Aceitar</button>` : ""}
        </div>
      `;
    }).join("");

    html += renderHonorMarksRanking(ranking);

    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

function renderPlayerMission(m) {
  const complete = m.progress >= m.targetValue;
  const percent = Math.min(100, Math.round((m.progress / m.targetValue) * 100));
  return `
    <div class="card mb-3 border-cyan-800">
      <div class="flex justify-between items-start">
        <div>
          <p class="font-bold text-sm">${escapeHtml(m.title)}</p>
          <p class="text-xs text-slate-400">${formatObjective(m.objectiveType)} ${m.progress}/${m.targetValue}</p>
          <p class="text-xs text-slate-500">Recompensa: ${m.honorMarksReward} Marcas de Honra · ${m.clanXpReward} Experiência do Clã</p>
        </div>
        ${m.status === "COMPLETED" || complete ? `<button class="btn-primary text-sm py-1 px-2" onclick="clanClaimMission('${m.id}')">Resgatar</button>` : `<span class="text-xs text-slate-500">${formatMissionStatus(m.status)}</span>`}
      </div>
      <div class="w-full bg-slate-800 rounded-full h-1.5 mt-2"><div class="bg-cyan-500 h-1.5 rounded-full" style="width:${percent}%"></div></div>
    </div>
  `;
}

function formatMissionStatus(status) {
  const map = {
    IN_PROGRESS: "Em andamento",
    COMPLETED: "Concluída",
    CLAIMED: "Resgatada"
  };
  return map[status] || status;
}

function formatObjective(type) {
  const map = {
    MISSIONS_COMPLETED: "Missões concluídas:",
    BOSSES_DEFEATED: "Chefes derrotados:",
    ARENA_WINS: "Vitórias na arena:",
    ARENA_DUELS: "Duelos na arena:",
    REBIRTHS_DONE: "Rebirths realizados:"
  };
  return map[type] || type;
}

function renderHonorMarksRanking(ranking) {
  if (!ranking || ranking.length === 0) return "";

  const rankColor = (index) => {
    if (index === 0) return "text-yellow-400";
    if (index === 1) return "text-slate-300";
    if (index === 2) return "text-amber-600";
    return "text-slate-400";
  };

  const rows = ranking.map((entry, i) => `
    <div class="flex justify-between items-center py-1.5 border-b border-slate-800 last:border-0">
      <div class="flex items-center gap-2">
        <span class="font-bold w-5 ${rankColor(i)}">${i + 1}.</span>
        <span class="text-sm text-slate-200">${escapeHtml(entry.username)}</span>
      </div>
      <span class="text-xs text-cyan-400 font-mono">${entry.contribution} Marcas de Honra</span>
    </div>
  `).join("");

  return `
    <div class="card mt-4 border-cyan-900">
          <p class="font-bold mb-2 text-sm">Classificação de Contribuição</p>
      ${rows}
    </div>
  `;
}

async function clanAcceptMission(missionId) {
  try {
    await apiPost(`/clan-missions/${missionId}/accept`);
    showToast("Missão de clã aceita!", "success");
    clanLoadMissions();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanClaimMission(playerMissionId) {
  try {
    await apiPost(`/clan-missions/${playerMissionId}/claim`);
    showToast("Recompensa resgatada!", "success");
    const clan = await apiGet(`/clans/${currentClan.id}`);
    renderClanDetail(clan);
    clanShowTab("missions");
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanLoadRaid() {
  const container = safeContent("clan-tab-content");
  if (!container) return;
  clearClanRaidCooldownTimer();
  container.innerHTML = `<div class="card animate-pulse"><div class="h-24"></div></div>`;

  try {
    const raid = await apiGet("/clan-raids/me");
    const percent = raid.maxHp > 0
      ? Math.min(100, Math.round((raid.remainingHp / raid.maxHp) * 100))
      : 100;
    const defeated = raid.status === "DEFEATED" || raid.remainingHp <= 0;
    const cooldownMinutes = Number.isFinite(Number(raid.attackCooldownMinutes)) && Number(raid.attackCooldownMinutes) > 0
      ? Number(raid.attackCooldownMinutes)
      : 5;
    const cooldownEnabled = raid.cooldownEnabled !== false;
    const nextAttackAt = raid.nextAttackAvailableAt ? Date.parse(raid.nextAttackAvailableAt) : NaN;
    const cooldownActive = cooldownEnabled
      && !defeated
      && Number.isFinite(nextAttackAt)
      && nextAttackAt > Date.now();
    const cooldownInfoHtml = cooldownEnabled
      ? `<p class="text-xs text-slate-500 mb-2 text-center">Intervalo entre ataques: ${cooldownMinutes} minuto(s)</p>`
      : `<p class="text-xs text-green-400 mb-2 text-center">Intervalo desativado pelo administrador</p>`;

    let rankingHtml = "";
    if (raid.ranking && raid.ranking.length > 0) {
      const rankColor = (index) => {
        if (index === 0) return "text-yellow-400";
        if (index === 1) return "text-slate-300";
        if (index === 2) return "text-amber-600";
        return "text-slate-400";
      };
      rankingHtml = `
        <div class="card mt-4 border-cyan-900">
          <p class="font-bold mb-2 text-sm">Classificação de Dano</p>
          ${raid.ranking.map((entry, i) => `
            <div class="flex justify-between items-center py-1.5 border-b border-slate-800 last:border-0">
              <div class="flex items-center gap-2">
                <span class="font-bold w-5 ${rankColor(i)}">${entry.position}.</span>
                <span class="text-sm text-slate-200">${escapeHtml(entry.username)}</span>
              </div>
              <span class="text-xs text-cyan-400 font-mono">${entry.totalDamage.toLocaleString()}</span>
            </div>
          `).join("")}
        </div>
      `;
    }

    let attacksHtml = "";
    if (raid.recentAttacks && raid.recentAttacks.length > 0) {
      attacksHtml = `
        <div class="card mt-4 border-slate-800">
          <p class="font-bold mb-2 text-sm">Últimos ataques</p>
          ${raid.recentAttacks.map(a => `
            <div class="flex justify-between items-center py-1 border-b border-slate-800 last:border-0">
              <span class="text-sm text-slate-200">${escapeHtml(a.username)}</span>
              <span class="text-xs text-cyan-400 font-mono">${a.damage.toLocaleString()}</span>
            </div>
          `).join("")}
        </div>
      `;
    }

    container.innerHTML = `
      <h3 class="font-bold mb-2">Incursão de Clã</h3>

      <div class="card mb-3">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-16 h-16 rounded-lg flex items-center justify-center text-2xl shrink-0" style="background:#334155;color:#94a3b8">
            ${raid.bossImageUrl ? `<img src="${escapeHtml(raid.bossImageUrl)}" class="w-16 h-16 rounded-lg object-cover" alt="" onerror="this.style.display='none'">` : "👾"}
          </div>
          <div>
            <p class="font-bold">${escapeHtml(raid.bossName)}</p>
            <p class="text-xs ${defeated ? 'text-green-400' : 'text-slate-400'}">${defeated ? 'Derrotado' : 'Em batalha'}</p>
          </div>
        </div>

        <div class="flex justify-between text-xs mb-1">
          <span class="text-slate-400">HP</span>
          <span class="text-slate-400">${raid.remainingHp.toLocaleString()} / ${raid.maxHp.toLocaleString()}</span>
        </div>
        <div class="w-full bg-slate-800 rounded-full h-2.5 mb-4">
          <div class="${defeated ? 'bg-green-500' : 'bg-red-500'} h-2.5 rounded-full" style="width:${percent}%"></div>
        </div>

        <p class="text-xs text-slate-400 mb-3">Seu dano: <span class="text-cyan-400">${raid.myTotalDamage.toLocaleString()}</span></p>

        ${!defeated ? `${cooldownInfoHtml}<button id="clan-raid-attack-button" class="btn-primary w-full" onclick="clanAttackRaid()"${cooldownActive ? " disabled" : ""}>${cooldownActive ? "Próximo ataque em <span id=\"clan-raid-countdown\">--:--</span>" : "Atacar Incursão"}</button>` : ""}
        ${defeated ? `<p class="text-xs text-green-400 text-center">Incursão derrotada. O próximo renascimento ocorrerá uma hora após a derrota.</p>${raid.defeatSummary ? `<div class="mt-3 rounded-lg border border-green-800/70 bg-green-950/20 p-3 text-xs"><p class="font-bold text-green-300 mb-2">Resumo da derrota</p><div class="grid grid-cols-2 gap-2"><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Golpe final</p><p class="mt-1 font-semibold text-white">${escapeHtml(raid.defeatSummary.finalBlowUsername || "Desconhecido")}</p></div><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Maior dano</p><p class="mt-1 font-semibold text-white">${escapeHtml(raid.defeatSummary.topDamageUsername || "Desconhecido")}<br><span class="text-cyan-300">${Number(raid.defeatSummary.topDamage || 0).toLocaleString()} de dano</span></p></div><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Ataques totais</p><p class="mt-1 font-semibold text-white">${Number(raid.defeatSummary.totalAttacks || 0).toLocaleString()}</p></div><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Tempo vivo</p><p class="mt-1 font-semibold text-white">${formatBossAliveDuration(raid.defeatSummary.aliveDurationSeconds)}</p></div><div class="col-span-2 rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Próximo ciclo</p><p class="mt-1 font-semibold text-amber-300">${formatBossDateTime(raid.defeatSummary.nextCycleAt)}</p></div></div></div>` : ""}` : ""}
      </div>

      ${rankingHtml}
      ${attacksHtml}
    `;

    if (cooldownActive) {
      startClanRaidCooldownCountdown(nextAttackAt);
    }
  } catch (err) {
    container.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

async function clanAttackRaid() {
  const btn = document.getElementById("clan-raid-attack-button");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Atacando...";
  }
  try {
    const result = await apiPost("/clan-raids/attack");
    showRaidAttackModal(result);
    clanLoadRaid();
  } catch (err) {
    if (btn) {
      btn.disabled = false;
      btn.textContent = "Atacar Incursão";
    }
    showToast(err.message, "error");
  }
}

function clearClanRaidCooldownTimer() {
  if (clanRaidCooldownTimer !== null) {
    clearInterval(clanRaidCooldownTimer);
    clanRaidCooldownTimer = null;
  }
}

function formatClanRaidCountdown(totalSeconds) {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

function startClanRaidCooldownCountdown(nextAttackAt) {
  clearClanRaidCooldownTimer();
  const button = document.getElementById("clan-raid-attack-button");
  const countdown = document.getElementById("clan-raid-countdown");
  if (!button || !countdown || !Number.isFinite(nextAttackAt)) return;

  const tick = () => {
    const remainingSeconds = Math.max(0, Math.ceil((nextAttackAt - Date.now()) / 1000));
    if (remainingSeconds === 0) {
      clearClanRaidCooldownTimer();
      clanLoadRaid();
      return;
    }
    countdown.textContent = formatClanRaidCountdown(remainingSeconds);
  };

  tick();
  clanRaidCooldownTimer = setInterval(tick, 1000);
}

function formatBossAliveDuration(seconds) { const total = Math.max(0, Number(seconds) || 0); const days = Math.floor(total / 86400); const hours = Math.floor((total % 86400) / 3600); const minutes = Math.floor((total % 3600) / 60); const secs = total % 60; return `${days}d ${hours}h ${minutes}m ${secs}s`; }
function formatBossDateTime(value) { return value ? new Date(value).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "medium" }) : "Não informado"; }

function showRaidAttackModal(result) {
  const existing = document.getElementById("raid-attack-modal");
  if (existing) existing.remove();

  const defeatSummary = result.defeatSummary;
  const defeatSummaryHtml = result.defeated && defeatSummary ? `<div class="mt-3 rounded-lg border border-green-800/70 bg-green-950/20 p-3 text-xs"><p class="font-bold text-green-300 mb-2">Resumo da derrota</p><div class="grid grid-cols-2 gap-2"><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Golpe final</p><p class="mt-1 font-semibold text-white">${escapeHtml(defeatSummary.finalBlowUsername || "Desconhecido")}</p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Maior dano</p><p class="mt-1 font-semibold text-white">${escapeHtml(defeatSummary.topDamageUsername || "Desconhecido")}<br><span class="text-cyan-300">${Number(defeatSummary.topDamage || 0).toLocaleString()} de dano</span></p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Ataques totais</p><p class="mt-1 font-semibold text-white">${Number(defeatSummary.totalAttacks || 0).toLocaleString()}</p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Tempo vivo</p><p class="mt-1 font-semibold text-white">${formatBossAliveDuration(defeatSummary.aliveDurationSeconds)}</p></div><div class="col-span-2 rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Próximo ciclo</p><p class="mt-1 font-semibold text-amber-300">${formatBossDateTime(defeatSummary.nextCycleAt)}</p></div></div></div>` : "";
  const rewardRow = result.defeated
    ? `<p class="text-green-400 text-sm font-bold mb-1">Clã ganhou ${result.clanHonorMarksGained.toLocaleString()} Marcas de Honra e ${result.clanXpGained.toLocaleString()} Experiência do Clã!</p>`
    : "";

  const overlay = document.createElement("div");
  overlay.id = "raid-attack-modal";
  overlay.className = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70";
  overlay.innerHTML = `
    <div class="bg-slate-900 border border-cyan-900 rounded-xl max-w-sm w-full p-5 shadow-2xl">
      <div class="flex justify-between items-center mb-3">
        <h3 class="font-bold text-lg text-cyan-400">Resultado do Ataque</h3>
        <button class="text-slate-400 text-2xl" onclick="document.getElementById('raid-attack-modal').remove()">&times;</button>
      </div>
      <div class="text-center mb-4">
        <p class="text-3xl font-bold text-white mb-1">${result.damage.toLocaleString()}</p>
        <p class="text-xs text-slate-400">Dano causado</p>
      </div>
      <div class="grid grid-cols-2 gap-3 mb-4 text-sm">
        <div class="bg-slate-800 rounded-lg p-2 text-center">
          <p class="font-bold text-cyan-400">+${result.xpGained.toLocaleString()}</p>
          <p class="text-xs text-slate-400">XP</p>
        </div>
        <div class="bg-slate-800 rounded-lg p-2 text-center">
          <p class="font-bold text-yellow-400">+${result.bitsGained.toLocaleString()}</p>
          <p class="text-xs text-slate-400">Bits</p>
        </div>
      </div>
      ${rewardRow}
      ${defeatSummaryHtml}
      <button class="btn-primary w-full" onclick="document.getElementById('raid-attack-modal').remove()">Fechar</button>
    </div>
  `;
  document.body.appendChild(overlay);
}

function renderClanCreate() {
  setHtml("clan-content", `
    <div class="mb-4">
      <button class="text-sm text-slate-400" onclick="renderClansPage()">← Voltar</button>
      <h2 class="text-lg font-bold mt-2">Criar Clã</h2>
    </div>

    <div class="card">
      <label class="block text-sm text-slate-400 mb-1">Nome do Clã</label>
      <input type="text" id="clan-create-name" class="input w-full mb-3" maxlength="30" placeholder="Ex: DRO Heroes">

      <label class="block text-sm text-slate-400 mb-1">Tag (2-3 caracteres)</label>
      <input type="text" id="clan-create-tag" class="input w-full mb-3" maxlength="3" placeholder="Ex: DRO">

      <label class="block text-sm text-slate-400 mb-1">Descrição (opcional)</label>
      <textarea id="clan-create-desc" class="input w-full mb-4" maxlength="280" rows="3" placeholder="Descreva seu clã..."></textarea>

      <button class="btn-primary w-full" onclick="clanCreate()">Criar Clã</button>
    </div>
  `);
}

async function clanCreate() {
  const name = document.getElementById("clan-create-name").value.trim();
  const tag = document.getElementById("clan-create-tag").value.trim();
  const desc = document.getElementById("clan-create-desc").value.trim();

  if (!name || !tag) {
    showToast("Nome e tag são obrigatórios", "error");
    return;
  }

  try {
    const clan = await apiPost("/clans", { name, tag, description: desc || null });
    showToast("Clã criado!", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanInvite(event, clanId) {
  event.preventDefault();
  const input = document.getElementById("clan-invite-username");
  const username = input?.value.trim();
  if (!username) return;

  try {
    await apiPost(`/clans/${clanId}/invite`, { username });
    showToast("Convite enviado pelo Correio.", "success");
    if (input) input.value = "";
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanJoin(id) {
  try {
    const clan = await apiPost(`/clans/${id}/join`);
    showToast("Você entrou no clã!", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanLeave(id) {
  if (!(await showConfirm("Tem certeza que deseja sair do clã?", { title: "Sair do Clã", confirmText: "Sair", danger: true }))) return;
  try {
    await apiPost(`/clans/${id}/leave`);
    showToast("Você saiu do clã.", "success");
    renderClansPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanKick(id, username) {
  if (!(await showConfirm(`Expulsar ${username} do clã?`, { title: "Expulsar Membro", confirmText: "Expulsar", danger: true }))) return;
  try {
    const clan = await apiPost(`/clans/${id}/members/${encodeURIComponent(username)}/kick`);
    showToast("Membro expulso.", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanPromote(id, username) {
  try {
    const clan = await apiPost(`/clans/${id}/members/${encodeURIComponent(username)}/role`, { role: "OFFICER" });
    showToast("Membro promovido.", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanDemote(id, username) {
  try {
    const clan = await apiPost(`/clans/${id}/members/${encodeURIComponent(username)}/role`, { role: "MEMBER" });
    showToast("Oficial rebaixado.", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanTransfer(id, username) {
  if (!(await showConfirm(`Transferir a liderança para ${username}? Essa ação não pode ser desfeita.`, { title: "Transferir Liderança", confirmText: "Transferir", danger: true }))) return;
  try {
    const clan = await apiPost(`/clans/${id}/members/${encodeURIComponent(username)}/transfer`);
    showToast("Liderança transferida.", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanDissolve(id) {
  if (!(await showConfirm("Tem certeza que deseja DISSOLVER o clã? Todos os membros serão removidos.", { title: "Dissolver Clã", confirmText: "Dissolver", danger: true }))) return;
  try {
    await apiDelete(`/clans/${id}`);
    showToast("Clã dissolvido.", "success");
    renderClansPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function clanEdit(id) {
  setHtml("clan-content", `
    <div class="mb-4">
      <button class="text-sm text-slate-400" onclick="renderClansPage()">← Voltar</button>
      <h2 class="text-lg font-bold mt-2">Editar Clã</h2>
    </div>

    <div class="card">
      <label class="block text-sm text-slate-400 mb-1">Descrição</label>
      <textarea id="clan-edit-desc" class="input w-full mb-3" maxlength="280" rows="3"></textarea>

      <label class="block text-sm text-slate-400 mb-1">Emblema (código/icon curto)</label>
      <input type="text" id="clan-edit-emblem" class="input w-full mb-4" maxlength="50" placeholder="Ex: 🛡️">

      <button class="btn-primary w-full" onclick="clanUpdate('${id}')">Salvar</button>
    </div>
  `);
}

async function clanUpdate(id) {
  const description = document.getElementById("clan-edit-desc").value.trim();
  const emblem = document.getElementById("clan-edit-emblem").value.trim();
  try {
    const body = {};
    if (description) body.description = description;
    if (emblem) body.emblem = emblem;
    const clan = await apiPatch(`/clans/${id}`, body);
    showToast("Clã atualizado.", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function renderClanRanking() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold px-1">🏆 Classificação de Clãs</h2>
        <button class="text-sm text-slate-400" onclick="renderClansPage()">Voltar</button>
      </div>
      <div id="clan-ranking-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  clanRankingPage = 0;
  clanRankingEntries = [];
  clanRankingHasMore = true;
  await clanLoadRanking();
}

async function clanLoadRanking() {
  if (clanRankingLoading) return;
  clanRankingLoading = true;

  const content = safeContent("clan-ranking-content");
  if (content && clanRankingPage === 0) {
    content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  try {
    const data = await apiGet(`/clans/ranking?page=${clanRankingPage}&size=10`);
    const newEntries = data.content || [];
    clanRankingEntries = clanRankingPage === 0 ? newEntries : [...clanRankingEntries, ...newEntries];
    clanRankingHasMore = !data.last;
    clanRenderRanking();
  } catch (err) {
    if (content) content.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  } finally {
    clanRankingLoading = false;
  }
}

function clanRenderRanking() {
  const content = safeContent("clan-ranking-content");
  if (!content) return;

  if (clanRankingEntries.length === 0) {
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum clã no ranking.</p>`;
    return;
  }

  let html = clanRankingEntries.map(e => {
    const posIcon = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `<span class="text-slate-500 font-bold text-sm">#${e.position}</span>`;
    return `
      <div class="card-sm mb-2 flex items-center gap-3" onclick="clanShowPreview('${e.id}')">
        <div class="w-8 text-center text-lg">${posIcon}</div>
        <div class="flex-1">
          <p class="font-bold text-sm">${escapeHtml(e.name)} <span class="text-cyan-400">[${escapeHtml(e.tag)}]</span></p>
          <p class="text-xs text-slate-500">👥 ${e.memberCount} membros</p>
        </div>
        <div class="text-right">
          <p class="text-xs text-slate-500">Poder total</p>
          <p class="font-bold text-amber-400">${e.totalPower.toLocaleString()}</p>
        </div>
        <div class="text-slate-500 text-lg">👁️</div>
      </div>
    `;
  }).join("");

  if (clanRankingHasMore) {
    html += `<button class="btn-secondary w-full mt-2" id="clan-ranking-load-more" onclick="clanRankingLoadMore()">Carregar mais</button>`;
  }

  content.innerHTML = html;
}

async function clanRankingLoadMore() {
  clanRankingPage++;
  await clanLoadRanking();
}
