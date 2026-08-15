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

    <button class="btn-primary w-full mt-4" onclick="renderClanRanking()">Ver Ranking de Clãs</button>
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
  } else if (tab === "missions") {
    clanLoadMissions();
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

  const showMissionTab = clan.isMember && !preview;

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
        <div class="card-sm col-span-2 sm:col-span-1"><p class="text-xs text-slate-500">Honor Marks</p><p class="font-bold text-purple-400">${clan.honorMarks}</p></div>
      </div>

      <div class="mt-4">
        <div class="flex justify-between text-xs mb-1">
          <span class="text-slate-400">XP do Clã</span>
          <span class="text-slate-400">${clan.xpToNextLevel > 0 ? clan.xpToNextLevel + " para o próximo" : "Máximo"}</span>
        </div>
        <div class="w-full bg-slate-800 rounded-full h-2"><div class="bg-cyan-500 h-2 rounded-full" style="width:${xpPercent}%"></div></div>
      </div>
    </div>

    <div class="flex gap-2 mb-3 overflow-x-auto">
      <button id="clan-tab-members" class="clan-tab-btn flex-1 py-2 px-3 rounded-lg text-sm font-bold bg-cyan-600 text-white" onclick="clanShowTab('members')">Membros</button>
      <button id="clan-tab-upgrades" class="clan-tab-btn flex-1 py-2 px-3 rounded-lg text-sm font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('upgrades')">Melhorias</button>
      ${showMissionTab ? `<button id="clan-tab-missions" class="clan-tab-btn flex-1 py-2 px-3 rounded-lg text-sm font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('missions')">Missões</button>` : ""}
    </div>

    <div id="clan-tab-content" class="mb-4"></div>

    ${managementButtons}

    ${!preview ? `<button class="btn-primary w-full mt-4" onclick="renderClanRanking()">Ranking de Clãs</button>` : ""}
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
        actions += `<button class="text-xs text-cyan-400 ml-2" onclick="clanPromote('${clan.id}', '${escapeHtml(m.username)}')">Promover</button>`;
      }
      if (isLeader && m.role === "OFFICER") {
        actions += `<button class="text-xs text-amber-400 ml-2" onclick="clanDemote('${clan.id}', '${escapeHtml(m.username)}')">Rebaixar</button>`;
      }
      if ((isLeader && m.role !== "LEADER") || (isOfficer && m.role === "MEMBER")) {
        actions += `<button class="text-xs text-red-400 ml-2" onclick="clanKick('${clan.id}', '${escapeHtml(m.username)}')">Expulsar</button>`;
      }
      if (isLeader) {
        actions += `<button class="text-xs text-amber-300 ml-2" onclick="clanTransfer('${clan.id}', '${escapeHtml(m.username)}')">Transferir</button>`;
      }
    }
    return `
      <div class="card-sm mb-2 flex items-center justify-between">
        <div>
          <p class="font-bold text-sm">${escapeHtml(m.username)} <span class="text-xs text-slate-400">(${roleLabel})</span></p>
          ${m.activeDigimonPower ? `<p class="text-xs text-slate-500">⚔️ Poder ${m.activeDigimonPower}</p>` : ""}
        </div>
        <div class="text-right">${actions}</div>
      </div>
    `;
  }).join("");

  container.innerHTML = `<h3 class="font-bold mb-2">Membros</h3>${membersHtml}`;
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
  if (u.code === "MAX_ENERGY_BONUS" || u.code === "MEMBER_CAPACITY") {
    return `+${Math.round(u.totalEffect)}`;
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

async function clanLoadMissions() {
  const clan = currentClan;
  const container = safeContent("clan-tab-content");
  if (!container || !clan) return;
  container.innerHTML = `<div class="card animate-pulse"><div class="h-24"></div></div>`;

  try {
    const [catalog, myMission] = await Promise.all([
      apiGet("/clan-missions"),
      apiGet("/clan-missions/me").catch(() => null)
    ]);

    const hasActive = myMission && myMission.status !== "CLAIMED";

    let html = `<h3 class="font-bold mb-2">Missões de Clã</h3>`;

    if (hasActive) {
      html += renderPlayerMission(myMission);
    }

    html += catalog.map(m => {
      const accepted = m.alreadyAccepted || (myMission && myMission.missionId === m.id && myMission.status !== "CLAIMED");
      const canAccept = !hasActive && !accepted && m.minClanLevel <= clan.level;
      return `
        <div class="card-sm mb-2 ${accepted ? 'border-cyan-800' : ''}">
          <div class="flex justify-between items-start">
            <div>
              <p class="font-bold text-sm">${escapeHtml(m.title)}</p>
              <p class="text-xs text-slate-400">${escapeHtml(m.description || "")}</p>
              <p class="text-xs text-slate-500">Objetivo: <span class="text-cyan-400">${formatObjective(m.objectiveType)} ${m.targetValue}</span></p>
              <p class="text-xs text-slate-500">Recompensa: ${m.minHonorMarksReward}-${m.maxHonorMarksReward} HM · ${m.clanXpReward} XP</p>
            </div>
            <div class="text-right">
              ${accepted ? `<span class="text-xs text-cyan-400">Ativa</span>` : ""}
              ${!accepted && m.minClanLevel > clan.level ? `<span class="text-xs text-slate-500">Nv ${m.minClanLevel}</span>` : ""}
            </div>
          </div>
          ${canAccept ? `<button class="btn-primary w-full mt-2 text-sm py-1" onclick="clanAcceptMission('${m.id}')">Aceitar</button>` : ""}
        </div>
      `;
    }).join("");

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
          <p class="text-xs text-slate-500">Recompensa: ${m.honorMarksReward} HM · ${m.clanXpReward} XP</p>
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
    BOSSES_DEFEATED: "Bosses derrotados:",
    ARENA_WINS: "Vitórias na arena:",
    ARENA_DUELS: "Duelos na arena:",
    REBIRTHS_DONE: "Rebirths realizados:"
  };
  return map[type] || type;
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
    clanLoadMissions();
  } catch (err) {
    showToast(err.message, "error");
  }
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

      <label class="block text-sm text-slate-400 mb-1">Tag (2-5 caracteres)</label>
      <input type="text" id="clan-create-tag" class="input w-full mb-3" maxlength="5" placeholder="Ex: DRO">

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
  if (!confirm("Tem certeza que deseja sair do clã?")) return;
  try {
    await apiPost(`/clans/${id}/leave`);
    showToast("Você saiu do clã.", "success");
    renderClansPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanKick(id, username) {
  if (!confirm(`Expulsar ${username} do clã?`)) return;
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
  if (!confirm(`Transferir a liderança para ${username}? Essa ação não pode ser desfeita.`)) return;
  try {
    const clan = await apiPost(`/clans/${id}/members/${encodeURIComponent(username)}/transfer`);
    showToast("Liderança transferida.", "success");
    renderClanDetail(clan);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function clanDissolve(id) {
  if (!confirm("Tem certeza que deseja DISSOLVER o clã? Todos os membros serão removidos.")) return;
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
        <h2 class="text-lg font-bold px-1">🏆 Ranking de Clãs</h2>
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
