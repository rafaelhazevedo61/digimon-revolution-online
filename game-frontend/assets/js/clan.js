let clanListPage = 0;
let clanListQuery = "";
let clanListEntries = [];
let clanListHasMore = true;
let clanListLoading = false;

let clanRankingPage = 0;
let clanRankingEntries = [];
let clanRankingHasMore = true;
let clanRankingLoading = false;

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
  clanLoadList();
}

function renderClanList() {
  const content = document.getElementById("clan-content");
  content.innerHTML = `
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
  `;
}

async function clanLoadList() {
  if (clanListLoading) return;
  clanListLoading = true;

  const list = document.getElementById("clan-list");
  if (clanListPage === 0) {
    list.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  try {
    const data = await apiGet(`/clans?query=${encodeURIComponent(clanListQuery)}&page=${clanListPage}&size=10`);
    const newEntries = data.content || [];
    clanListEntries = clanListPage === 0 ? newEntries : [...clanListEntries, ...newEntries];
    clanListHasMore = !data.last;
    clanRenderList();
  } catch (err) {
    list.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  } finally {
    clanListLoading = false;
  }
}

function clanRenderList() {
  const list = document.getElementById("clan-list");
  if (clanListEntries.length === 0) {
    list.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum clã encontrado.</p>`;
    return;
  }

  let html = clanListEntries.map(c => `
    <div class="card-sm mb-2 cursor-pointer" onclick="clanOpenDetail('${c.id}')">
      <div class="flex items-center justify-between">
        <div>
          <p class="font-bold text-sm">${escapeHtml(c.name)} <span class="text-cyan-400">[${escapeHtml(c.tag)}]</span></p>
          <p class="text-xs text-slate-400">${escapeHtml(c.description || "Sem descrição")}</p>
          <p class="text-xs text-slate-500 mt-1">👥 ${c.memberCount}/${c.maxMembers} · Nível ${c.level}</p>
        </div>
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

function renderClanDetail(clan) {
  const content = document.getElementById("clan-content");
  const isLeader = clan.myRole && clan.myRole.role === "LEADER";
  const isOfficer = clan.myRole && clan.myRole.role === "OFFICER";
  const canManage = isLeader || isOfficer;

  const xpPercent = clan.xpToNextLevel > 0
    ? Math.min(100, Math.round(((clan.experience - (clan.experience + clan.xpToNextLevel - clan.xpToNextLevel)) / (clan.experience + clan.xpToNextLevel)) * 100))
    : 100;

  const membersHtml = clan.members.map(m => {
    const roleLabel = { LEADER: "Líder", OFFICER: "Oficial", MEMBER: "Membro" }[m.role] || m.role;
    const isSelf = m.username === getPlayerIdFromLocal();
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

  content.innerHTML = `
    <div class="mb-4">
      <button class="text-sm text-slate-400" onclick="renderClansPage()">← Voltar</button>
    </div>

    <div class="card mb-4">
      <div class="flex items-start justify-between">
        <div>
          <h2 class="text-lg font-bold">${escapeHtml(clan.name)} <span class="text-cyan-400">[${escapeHtml(clan.tag)}]</span></h2>
          <p class="text-sm text-slate-400">${escapeHtml(clan.description || "Sem descrição")}</p>
        </div>
        ${canManage ? `<button class="text-xs text-cyan-400" onclick="clanEdit('${clan.id}')">Editar</button>` : ""}
      </div>

      <div class="grid grid-cols-4 gap-2 mt-4 text-center">
        <div class="card-sm"><p class="text-xs text-slate-500">Nível</p><p class="font-bold text-cyan-400">${clan.level}</p></div>
        <div class="card-sm"><p class="text-xs text-slate-500">Membros</p><p class="font-bold">${clan.memberCount}/${clan.maxMembers}</p></div>
        <div class="card-sm"><p class="text-xs text-slate-500">Vagas do nível</p><p class="font-bold text-green-400">${clan.baseMaxMembers}</p></div>
        <div class="card-sm"><p class="text-xs text-slate-500">Vagas compradas</p><p class="font-bold text-amber-400">${clan.boughtSlots}</p></div>
      </div>

      <div class="mt-4">
        <div class="flex justify-between text-xs mb-1">
          <span class="text-slate-400">XP do Clã</span>
          <span class="text-slate-400">${clan.xpToNextLevel > 0 ? clan.xpToNextLevel + " para o próximo" : "Máximo"}</span>
        </div>
        <div class="w-full bg-slate-800 rounded-full h-2"><div class="bg-cyan-500 h-2 rounded-full" style="width:${xpPercent}%"></div></div>
      </div>

      ${isLeader ? `
        ${clan.nextSlotCost > 0 ? `
          <button class="btn-primary w-full mt-3" onclick="clanBuySlot('${clan.id}')">
            Comprar vaga extra (${clan.nextSlotCost} bits)
          </button>
          <p class="text-xs text-slate-500 text-center mt-1">Limite de vagas extras: ${clan.boughtSlots}/${clan.maxBoughtSlots}</p>
        ` : `<p class="text-xs text-amber-400 text-center mt-3">Limite de vagas extras atingido (${clan.maxBoughtSlots}/${clan.maxBoughtSlots})</p>`}
        <p class="text-xs text-slate-500 text-center mt-1">As vagas também aumentam automaticamente ao subir de nível do clã.</p>
      ` : `<p class="text-xs text-slate-500 text-center mt-3">As vagas aumentam automaticamente ao subir de nível do clã.</p>`}
    </div>

    <h3 class="font-bold mb-2">Membros</h3>
    ${membersHtml}

    <div class="flex gap-2 mt-4">
      ${clan.isMember ? `<button class="btn-secondary flex-1" onclick="clanLeave('${clan.id}')">Sair do Clã</button>` : ""}
      ${isLeader ? `<button class="btn-red flex-1" onclick="clanDissolve('${clan.id}')">Dissolver Clã</button>` : ""}
    </div>

    <button class="btn-primary w-full mt-4" onclick="renderClanRanking()">Ranking de Clãs</button>
  `;
}

function getPlayerIdFromLocal() {
  return getPlayerId() || "";
}

function renderClanCreate() {
  const content = document.getElementById("clan-content");
  content.innerHTML = `
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
  `;
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

async function clanBuySlot(id) {
  if (!confirm("Deseja comprar uma vaga extra para o clã?")) return;
  try {
    const result = await apiPost(`/clans/${id}/upgrade/buy-slot`);
    showToast(`Vaga comprada! Máximo de membros: ${result.maxMembers}`, "success");
    const clan = await apiGet(`/clans/${id}`);
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
  const content = document.getElementById("clan-content");
  content.innerHTML = `
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
  `;
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

  const content = document.getElementById("clan-ranking-content");
  if (clanRankingPage === 0) {
    content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  try {
    const data = await apiGet(`/clans/ranking?page=${clanRankingPage}&size=10`);
    const newEntries = data.content || [];
    clanRankingEntries = clanRankingPage === 0 ? newEntries : [...clanRankingEntries, ...newEntries];
    clanRankingHasMore = !data.last;
    clanRenderRanking();
  } catch (err) {
    content.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  } finally {
    clanRankingLoading = false;
  }
}

function clanRenderRanking() {
  const content = document.getElementById("clan-ranking-content");
  if (clanRankingEntries.length === 0) {
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum clã no ranking.</p>`;
    return;
  }

  let html = clanRankingEntries.map(e => {
    const posIcon = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `<span class="text-slate-500 font-bold text-sm">#${e.position}</span>`;
    return `
      <div class="card-sm mb-2 flex items-center gap-3" onclick="clanOpenDetail('${e.id}')">
        <div class="w-8 text-center text-lg">${posIcon}</div>
        <div class="flex-1">
          <p class="font-bold text-sm">${escapeHtml(e.name)} <span class="text-cyan-400">[${escapeHtml(e.tag)}]</span></p>
          <p class="text-xs text-slate-500">👥 ${e.memberCount} membros</p>
        </div>
        <div class="text-right">
          <p class="text-xs text-slate-500">Poder total</p>
          <p class="font-bold text-amber-400">${e.totalPower.toLocaleString()}</p>
        </div>
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
