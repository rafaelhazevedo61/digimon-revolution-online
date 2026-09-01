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
    <div class="page-container clan-page-container">
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
    <header class="clan-page-header">
      <div class="clan-page-header-copy">
        <p class="clan-eyebrow">Comunidade · Conexões</p>
        <h1 class="clan-page-title">Clãs</h1>
        <p class="clan-page-subtitle">Encontre uma equipe para evoluir, competir e conquistar o mundo digital.</p>
      </div>
      <button class="btn-primary clan-header-action" onclick="renderClanCreate()">
        <span class="clan-header-action-icon" aria-hidden="true">+</span>
        <span><strong>Criar Clã</strong><small>Comece sua própria jornada</small></span>
      </button>
    </header>

    <div class="clan-list-layout">
      <main class="clan-list-main">
        <section class="clan-surface clan-discovery-panel">
          <div class="clan-section-heading">
            <div>
              <p class="clan-eyebrow clan-eyebrow-cyan">Explorar</p>
              <h2 class="clan-section-title">Encontre seu próximo grupo</h2>
            </div>
            <span class="clan-section-mark" aria-hidden="true">⌕</span>
          </div>
          <form class="clan-search-row" onsubmit="event.preventDefault(); clanSearch()">
            <label class="sr-only" for="clan-search">Buscar clã</label>
            <span class="clan-search-input-wrap"><span class="clan-search-icon" aria-hidden="true">⌕</span><input type="search" id="clan-search" class="input" placeholder="Buscar por nome ou tag..." value="${escapeHtml(clanListQuery)}" autocomplete="off"></span>
            <button type="submit" class="btn-secondary">Buscar</button>
          </form>
        </section>

        <section class="clan-list-surface">
          <div class="clan-section-heading clan-list-heading">
            <div>
              <p class="clan-eyebrow">Diretório aberto</p>
              <h2 class="clan-section-title">Clãs disponíveis</h2>
            </div>
            <span class="clan-section-caption">Toque em um card para ver detalhes</span>
          </div>
          <div id="clan-list" class="clan-list-grid">
            <div class="card animate-pulse"><div class="h-32"></div></div>
          </div>
        </section>
      </main>

      <aside class="clan-list-aside">
        <section class="clan-aside-card clan-ranking-promo">
          <div class="clan-aside-icon" aria-hidden="true">✦</div>
          <p class="clan-eyebrow clan-eyebrow-amber">Competição</p>
          <h2 class="clan-aside-title">Ranking de Clãs</h2>
          <p class="clan-aside-copy">Compare o poder total das equipes e descubra quem lidera a temporada.</p>
          <button class="btn-primary w-full" onclick="renderClanRanking()">Ver classificação</button>
        </section>
        <section class="clan-aside-card clan-aside-note">
          <span class="clan-aside-note-mark" aria-hidden="true">i</span>
          <div>
            <p class="font-bold text-sm">Uma boa equipe faz diferença</p>
            <p class="text-xs text-slate-400 mt-1">Participe de missões, incursões e melhorias para fortalecer seu clã.</p>
          </div>
        </section>
      </aside>
    </div>
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
    <article class="clan-list-card" onclick="clanOpenDetail('${c.id}')" role="button" tabindex="0" onkeydown="if(event.key==='Enter'||event.key===' ') { event.preventDefault(); clanOpenDetail('${c.id}'); }">
      <div class="clan-card-emblem" aria-hidden="true">${c.emblem ? escapeHtml(c.emblem) : "✦"}</div>
      <div class="clan-card-body">
        <div class="clan-card-title-row">
          <p class="clan-card-title">${escapeHtml(c.name)}</p>
          <span class="clan-card-tag">[${escapeHtml(c.tag)}]</span>
        </div>
        <p class="clan-card-description">${escapeHtml(c.description || "Sem descrição")}</p>
        <div class="clan-card-meta">
          <span>${c.memberCount}/${c.maxMembers} membros</span>
          <span>Nível ${c.level}</span>
        </div>
      </div>
      <div class="clan-card-actions">
        <button class="clan-icon-button" aria-label="Ver prévia de ${escapeAttr(c.name)}" title="Ver prévia" onclick="event.stopPropagation(); clanShowPreview('${c.id}')">◉</button>
        <button class="btn-primary clan-card-join" onclick="event.stopPropagation(); clanJoin('${c.id}')">Entrar</button>
      </div>
    </article>
  `).join("");

  if (clanListHasMore) {
    html += `<button class="btn-secondary clan-list-load-more" id="clan-load-more" onclick="clanLoadMore()">Carregar mais</button>`;
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
    const previewClan = {
      ...clan,
      isMember: false,
      myRole: null,
      members: Array.isArray(clan.members) ? clan.members : []
    };
    const previousClan = currentClan;
    const previousTab = currentClanTab;
    const overlay = document.createElement("div");
    overlay.id = "clan-preview-overlay";
    overlay.className = "clan-preview-overlay fixed inset-0 z-50 flex items-end justify-center";
    overlay.style.background = "rgba(0,0,0,0.6)";

    const closePreview = () => {
      overlay.remove();
      currentClan = previousClan;
      currentClanTab = previousTab;
    };
    overlay.onclick = (e) => { if (e.target === overlay) closePreview(); };

    overlay.innerHTML = `
      <div class="clan-preview-modal w-full max-w-md rounded-t-2xl p-4 pb-8" style="background:#0f172a;max-height:85vh;overflow-y:auto">
        <div class="flex justify-between items-center mb-3">
          <h3 class="font-bold text-lg">Pré-visualização do Clã</h3>
          <button type="button" class="text-slate-400 text-xl" data-clan-preview-close aria-label="Fechar pré-visualização">&times;</button>
        </div>
        <div id="clan-preview-body"></div>
      </div>
    `;

    document.body.appendChild(overlay);
    overlay.querySelector("[data-clan-preview-close]")?.addEventListener("click", closePreview);
    currentClan = previewClan;
    currentClanTab = "members";
    setHtml("clan-preview-body", renderClanDetailHtml(previewClan, { preview: true }));
    clanShowTab("members", "clan-preview-");
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

function clanShowTab(tab, idPrefix = "") {
  currentClanTab = tab;
  const container = safeContent(`${idPrefix}clan-tab-content`);
  if (!container) return;

  const activeBtn = safeContent(`${idPrefix}clan-tab-${tab}`);
  const tabNav = activeBtn?.parentElement;
  (tabNav ? tabNav.querySelectorAll(".clan-tab-btn") : document.querySelectorAll(".clan-tab-btn")).forEach(b => {
    b.classList.remove("bg-cyan-600", "text-white", "is-active");
    b.classList.add("bg-slate-800", "text-slate-300");
  });
  if (activeBtn) {
    activeBtn.classList.remove("bg-slate-800", "text-slate-300");
    activeBtn.classList.add("bg-cyan-600", "text-white", "is-active");
  }

  if (tab === "members") {
    clanRenderMembersTab(idPrefix);
  } else if (tab === "upgrades") {
    clanLoadUpgrades(idPrefix);
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
      <div class="clan-management-actions">
        <p class="clan-rail-label">Gestão do clã</p>
        ${!isLeader ? `<button class="btn-secondary w-full" onclick="clanLeave('${clan.id}')">Sair do Clã</button>` : ""}
        ${isLeader ? `<button class="btn-red w-full" onclick="clanDissolve('${clan.id}')">Dissolver Clã</button>` : ""}
      </div>
    `
    : "";

  const showMemberTabs = clan.isMember && !preview;

  return `
    ${backButton}

    <section class="clan-detail-hero">
      <div class="clan-detail-identity">
        <div class="clan-detail-emblem" aria-hidden="true">${clan.emblem ? escapeHtml(clan.emblem) : "✦"}</div>
        <div class="clan-detail-identity-copy">
          <p class="clan-eyebrow ${clan.isMember ? "clan-eyebrow-cyan" : ""}">${clan.isMember ? "Seu clã" : "Perfil público"}</p>
          <div class="clan-detail-title-row">
            <h1 class="clan-detail-title">${escapeHtml(clan.name)}</h1>
            <span class="clan-detail-tag">[${escapeHtml(clan.tag)}]</span>
          </div>
          <p class="clan-detail-description">${escapeHtml(clan.description || "Sem descrição")}</p>
        </div>
        ${canManage ? `<button class="clan-edit-action" onclick="clanEdit('${clan.id}')"><span aria-hidden="true">✎</span> Editar</button>` : ""}
      </div>

      <div class="clan-stat-grid">
        <div class="clan-stat-card clan-stat-cyan"><span>Nível</span><strong>${clan.level}</strong><small>progressão atual</small></div>
        <div class="clan-stat-card"><span>Membros</span><strong>${clan.memberCount}/${clan.maxMembers}</strong><small>participantes</small></div>
        <div class="clan-stat-card clan-stat-green"><span>Vagas base</span><strong>${clan.baseMaxMembers}</strong><small>capacidade inicial</small></div>
        <div class="clan-stat-card clan-stat-amber"><span>Vagas extras</span><strong>+${clan.memberCapacityUpgradeLevel}</strong><small>melhorias ativas</small></div>
        <div class="clan-stat-card clan-stat-purple"><span>Marcas de Honra</span><strong>${Number(clan.honorMarks || 0).toLocaleString("pt-BR")}</strong><small>saldo disponível</small></div>
      </div>

      <div class="clan-xp-block">
        <div class="clan-xp-heading"><span>Experiência do Clã</span><strong>${xpPercent}%</strong></div>
        <div class="clan-xp-track" role="progressbar" aria-valuenow="${xpPercent}" aria-valuemin="0" aria-valuemax="100" aria-label="${xpPercent}% de experiência do clã"><span style="width:${xpPercent}%"></span></div>
        <div class="clan-xp-meta"><span>${Number(clan.experience || 0).toLocaleString("pt-BR")} XP acumulada</span><span>${clan.xpToNextLevel > 0 ? Number(clan.xpToNextLevel).toLocaleString("pt-BR") + " para o próximo nível" : "Nível máximo alcançado"}</span></div>
      </div>
    </section>

    <nav class="clan-tabs ${showMemberTabs ? "clan-tabs--full" : "clan-tabs--compact"}" aria-label="Seções do clã">
      <button id="${preview ? "clan-preview-" : ""}clan-tab-members" class="clan-tab-btn font-bold bg-cyan-600 text-white is-active" onclick="clanShowTab('members', '${preview ? "clan-preview-" : ""}')"><span aria-hidden="true">◉</span> Membros</button>
      <button id="${preview ? "clan-preview-" : ""}clan-tab-upgrades" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('upgrades', '${preview ? "clan-preview-" : ""}')"><span aria-hidden="true">◇</span> Melhorias</button>
      ${showMemberTabs ? `<button id="${preview ? "clan-preview-" : ""}clan-tab-storage" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('storage', '${preview ? "clan-preview-" : ""}')"><span aria-hidden="true">▣</span> Armazém</button>` : ""}
      ${showMemberTabs ? `<button id="${preview ? "clan-preview-" : ""}clan-tab-missions" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('missions', '${preview ? "clan-preview-" : ""}')"><span aria-hidden="true">✦</span> Missões</button>` : ""}
      ${showMemberTabs ? `<button id="${preview ? "clan-preview-" : ""}clan-tab-raid" class="clan-tab-btn font-bold bg-slate-800 text-slate-300" onclick="clanShowTab('raid', '${preview ? "clan-preview-" : ""}')"><span aria-hidden="true">ϟ</span> Incursão</button>` : ""}
    </nav>

    <div class="clan-detail-layout ${preview ? "clan-detail-layout--preview" : ""}">
      <main id="${preview ? "clan-preview-" : ""}clan-tab-content" class="clan-tab-content"></main>
      ${!preview ? `
        <aside class="clan-detail-rail">
          ${managementButtons}
          <div class="clan-rail-card">
            <p class="clan-eyebrow clan-eyebrow-amber">Competição</p>
            <h2 class="clan-rail-title">Ranking de Clãs</h2>
            <p class="text-xs text-slate-400 mt-1">Veja o poder das equipes mais fortes.</p>
            <button class="btn-secondary w-full mt-3" onclick="renderClanRanking()">Ver classificação</button>
          </div>
        </aside>
      ` : ""}
    </div>
  `;
}

function clanRenderMembersTab(idPrefix = "") {
  const clan = currentClan;
  if (!clan) return;
  const container = safeContent(`${idPrefix}clan-tab-content`);
  if (!container) return;

  const isLeader = clan.myRole && clan.myRole.role === "LEADER";
  const isOfficer = clan.myRole && clan.myRole.role === "OFFICER";
  const canManage = isLeader || isOfficer;
  const currentPlayerId = getPlayerId() || "";

  const membersHtml = clan.members.map(m => {
    const roleLabel = { LEADER: "Líder", OFFICER: "Oficial", MEMBER: "Membro" }[m.role] || m.role;
    const roleKey = String(m.role || "MEMBER").toLowerCase();
    const isSelf = m.id === currentPlayerId;
    const avatar = String(m.username || "?").trim().slice(0, 1).toUpperCase();
    let actions = "";
    if (canManage && !isSelf && m.role !== "LEADER") {
      if (isLeader && m.role === "MEMBER") {
        actions += `<button class="clan-action-link clan-action-cyan" data-clan-action="promote" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Promover</button>`;
      }
      if (isLeader && m.role === "OFFICER") {
        actions += `<button class="clan-action-link clan-action-amber" data-clan-action="demote" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Rebaixar</button>`;
      }
      if ((isLeader && m.role !== "LEADER") || (isOfficer && m.role === "MEMBER")) {
        actions += `<button class="clan-action-link clan-action-red" data-clan-action="kick" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Expulsar</button>`;
      }
      if (isLeader) {
        actions += `<button class="clan-action-link clan-action-amber" data-clan-action="transfer" data-clan-id="${escapeAttr(clan.id)}" data-clan-username="${escapeAttr(m.username)}">Transferir</button>`;
      }
    }
    return `
      <article class="clan-member-card">
        <div class="clan-member-avatar" aria-hidden="true">${escapeHtml(avatar)}</div>
        <div class="clan-member-identity">
          <div class="clan-member-name-row">
            <p class="clan-member-name">${escapeHtml(m.username)}</p>
            ${isSelf ? `<span class="clan-member-self">Você</span>` : ""}
          </div>
          <div class="clan-member-meta">
            <span class="clan-member-role clan-role-${roleKey}">${escapeHtml(roleLabel)}</span>
            ${m.activeDigimonPower ? `<span>⚔ Poder ${Number(m.activeDigimonPower).toLocaleString("pt-BR")}</span>` : `<span>Poder não informado</span>`}
          </div>
        </div>
        <div class="clan-member-actions">${actions || `<span class="clan-member-muted">—</span>`}</div>
      </article>
    `;
  }).join("");

  const inviteHtml = canManage ? `
    <form class="clan-invite-card" onsubmit="clanInvite(event, '${clan.id}')">
      <div class="clan-section-heading">
        <div><p class="clan-eyebrow clan-eyebrow-cyan">Recrutamento</p><p class="clan-section-title clan-section-title-sm">Convidar jogador</p></div>
        <span class="clan-section-mark" aria-hidden="true">+</span>
      </div>
      <p class="text-xs text-slate-400 mt-2 mb-3">O jogador receberá um convite no Correio e poderá aceitar ou recusar.</p>
      <div class="clan-inline-form">
        <input id="clan-invite-username" class="input" maxlength="30" required placeholder="Nome do jogador">
        <button class="btn-primary" type="submit">Convidar</button>
      </div>
    </form>
  ` : "";
  container.innerHTML = `${inviteHtml}<div class="clan-tab-heading"><div><p class="clan-eyebrow">Equipe atual</p><h2 class="clan-section-title">Membros</h2></div><span class="clan-section-count">${clan.members.length}</span></div><div class="clan-members-grid">${membersHtml}</div>`;
  container.querySelectorAll("[data-clan-action]").forEach(button => {
    const action = button.dataset.clanAction;
    const id = button.dataset.clanId;
    const username = button.dataset.clanUsername;
    button.addEventListener("click", () => clanActionHandlers[action]?.(id, username));
  });
}

async function clanLoadUpgrades(idPrefix = "") {
  const clan = currentClan;
  const container = safeContent(`${idPrefix}clan-tab-content`);
  if (!container || !clan) return;
  container.innerHTML = `<div class="card animate-pulse"><div class="h-24"></div></div>`;

  try {
    const upgrades = await apiGet(`/clans/${clan.id}/upgrades`);
    const isPublicReadOnly = !clan.isMember || !clan.id || !currentClan || String(clan.id) !== String(currentClan.id);
    const isLeader = !isPublicReadOnly && clan.myRole && clan.myRole.role === "LEADER";
    const upgradeCaption = isPublicReadOnly ? "Visualização pública · somente leitura" : "Use Marcas de Honra para evoluir";

    let html = `<div class="clan-tab-heading"><div><p class="clan-eyebrow">Progressão coletiva</p><h2 class="clan-section-title">Melhorias</h2></div><span class="clan-section-caption">${upgradeCaption}</span></div>`;
    if (upgrades.length === 0) {
      html += `<p class="text-slate-400 text-sm">Nenhuma melhoria disponível.</p>`;
    } else if (isPublicReadOnly) {
      html += `<div class="clan-upgrades-public-list">${upgrades.map(u => `
        <article class="clan-upgrade-public-row">
          <span class="clan-upgrade-public-icon" aria-hidden="true">${u.maxed ? "✓" : "◇"}</span>
          <p class="clan-upgrade-public-name">${escapeHtml(u.name)}</p>
          <strong class="clan-upgrade-public-level">Nível ${Number(u.currentLevel || 0)}</strong>
        </article>
      `).join("")}</div>`;
    } else {
      html += `<div class="clan-upgrades-grid">${upgrades.map(u => {
        const locked = !u.unlocked;
        const maxed = u.maxed;
        const canBuy = isLeader && u.unlocked && !maxed && clan.honorMarks >= u.nextCostHonorMarks;
        return `
          <article class="clan-upgrade-card ${locked ? 'is-locked' : ''}">
            <div class="clan-upgrade-heading">
              <div class="clan-upgrade-icon" aria-hidden="true">${locked ? "◇" : maxed ? "✓" : "↗"}</div>
              <div class="min-w-0"><p class="clan-upgrade-name">${escapeHtml(u.name)}</p><p class="clan-upgrade-level">Nível ${u.currentLevel}/${u.maxLevel}</p></div>
              <div class="clan-upgrade-status">
                ${locked ? `<span>Clã nível ${u.unlockedAtClanLevel}</span>` : ""}
                ${!locked && !maxed ? `<strong class="${canBuy ? 'is-affordable' : ''}">${Number(u.nextCostHonorMarks).toLocaleString("pt-BR")} HM</strong>` : ""}
                ${maxed ? `<strong class="is-maxed">Máx.</strong>` : ""}
              </div>
            </div>
            <p class="clan-upgrade-description">${escapeHtml(u.description || "")}</p>
            <div class="clan-upgrade-effect"><span>Efeito total</span><strong>${formatEffect(u)}</strong></div>
            ${isPublicReadOnly ? `<div class="clan-upgrade-readonly" role="note"><span aria-hidden="true">◉</span><span>Informação pública · apenas visualização</span></div>` : isLeader && !locked && !maxed ? `
              <button class="btn-primary w-full clan-upgrade-action ${canBuy ? '' : 'opacity-50 cursor-not-allowed'}"
                ${canBuy ? `onclick="clanBuyUpgrade('${clan.id}', '${escapeHtml(u.code)}')"` : 'disabled'}>
                Melhorar · ${Number(u.nextCostHonorMarks).toLocaleString("pt-BR")} HM
              </button>
            ` : ""}
          </article>
        `;
      }).join("")}</div>`;
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
  const canManageCurrentClan = currentClan
    && currentClan.isMember
    && String(currentClan.id) === String(clanId)
    && currentClan.myRole
    && currentClan.myRole.role === "LEADER";
  if (!canManageCurrentClan) {
    showToast("Apenas o líder pode melhorar o próprio clã.", "error");
    return;
  }

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
      <div class="clan-tab-heading clan-storage-heading">
        <div><p class="clan-eyebrow">Logística compartilhada</p><h2 class="clan-section-title">Armazém do Clã</h2></div>
        <span class="clan-storage-capacity-label">${storage.usedSlots}/${storage.capacity} slots</span>
      </div>
      <div class="clan-storage-overview-grid">
        <section class="clan-storage-capacity-card">
          <div class="clan-storage-card-heading"><div><p class="clan-eyebrow clan-eyebrow-cyan">Capacidade</p><p class="clan-section-title clan-section-title-sm">Espaço utilizado</p></div><strong>${storage.availableSlots} livres</strong></div>
          <div class="clan-xp-track clan-storage-progress"><span style="width:${storage.capacity ? Math.min(100, Math.round(storage.usedSlots / storage.capacity * 100)) : 0}%"></span></div>
          <p class="clan-storage-progress-meta">${storage.usedSlots} ocupados de ${storage.capacity} slots</p>
        </section>
        <section class="clan-storage-deposit-card">
          <div class="clan-storage-card-heading"><div><p class="clan-eyebrow clan-eyebrow-amber">Colaboração</p><p class="clan-section-title clan-section-title-sm">Depositar item</p></div><span class="clan-section-mark" aria-hidden="true">↗</span></div>
          <p class="text-xs text-slate-400 mt-2">Qualquer membro pode depositar itens negociáveis do próprio inventário.</p>
          <div class="clan-storage-deposit-footer"><span class="text-xs text-slate-500">${depositableItems.length ? "Itens prontos para envio" : "Nenhum item negociável"}</span><button type="button" class="btn-primary text-sm" ${depositableItems.length ? "" : "disabled"} onclick="clanOpenStorageDepositModal()">Depositar</button></div>
        </section>
      </div>
    `;

    html += `
      <section class="clan-storage-items-panel">
        <div class="clan-tab-heading clan-storage-items-heading"><div><p class="clan-eyebrow">Inventário compartilhado</p><h3 class="clan-section-title clan-section-title-sm">Itens armazenados</h3></div><span class="clan-section-caption">${clanStorageItems.length} ${clanStorageItems.length === 1 ? "item armazenado" : "itens armazenados"}</span></div>
        <form id="clan-storage-items-search-form" class="clan-search-row clan-storage-search-row" onsubmit="clanStorageSearchItems(event)">
          <label class="sr-only" for="clan-storage-items-search">Buscar item armazenado</label>
          <span class="clan-search-input-wrap"><span class="clan-search-icon" aria-hidden="true">⌕</span><input id="clan-storage-items-search" class="input" type="search" placeholder="Buscar item armazenado..." aria-label="Buscar item armazenado" autocomplete="off"></span>
          <button type="submit" class="btn-primary">Buscar</button>
        </form>
        <div id="clan-storage-items-list">${clanStorageRenderItemsHtml()}</div>
      </section>
    `;

    html += `<details class="clan-storage-history"><summary class="cursor-pointer font-bold text-sm">Histórico de movimentações</summary><div id="clan-storage-history-list" class="mt-3">${clanStorageRenderHistoryHtml()}</div></details>`;
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
    <article class="clan-storage-item-card">
      ${item.icon ? `<img src="${escapeAttr(item.icon)}" class="clan-storage-item-icon" alt="" onerror="this.style.display='none'">` : `<span class="clan-storage-item-icon clan-storage-item-icon-fallback" aria-hidden="true">▣</span>`}
      <div class="clan-storage-item-body">
        <p class="clan-storage-item-name">${escapeHtml(item.name || item.code)}</p>
        <p class="clan-storage-item-meta">Quantidade: ${Number(item.quantity).toLocaleString("pt-BR")}${item.maxStack ? `/${Number(item.maxStack).toLocaleString("pt-BR")}` : ""}</p>
      </div>
      ${canWithdraw ? `<button class="btn-secondary clan-storage-withdraw" onclick="clanStorageWithdraw('${clanId}', ${item.itemDefinitionId}, '${escapeAttr(item.name || item.code)}', ${item.quantity})">Retirar</button>` : ""}
    </article>
  `).join("");

  return `
    <div class="clan-storage-pagination-toolbar clan-storage-items-pagination flex flex-wrap items-center justify-between gap-2">
      <p class="text-xs text-slate-500">${start + 1}-${end} de ${filteredItems.length} ${itemLabel}</p>
      ${totalPages > 1 ? `
        <div class="clan-storage-pagination-controls flex items-center gap-2">
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
    <article class="clan-history-row">
      <div class="clan-history-action ${entry.action === "DEPOSIT" ? "is-deposit" : "is-withdraw"}"><span aria-hidden="true">${entry.action === "DEPOSIT" ? "↓" : "↑"}</span>${entry.action === "DEPOSIT" ? "Depósito" : "Retirada"}</div>
      <div class="clan-history-copy"><strong>${escapeHtml(entry.itemName)}</strong><span>${escapeHtml(entry.actorUsername)} · ${entry.quantity} unidade(s)</span></div>
      <time class="clan-history-date">${new Date(entry.createdAt).toLocaleString("pt-BR")}</time>
    </article>
  `).join("");

  return `
    <div class="clan-storage-pagination-toolbar clan-storage-history-pagination flex items-center justify-between gap-3">
      <p class="text-xs text-slate-500">${start + 1}-${end} de ${clanStorageHistory.length} movimentação(ões)</p>
      ${totalPages > 1 ? `
        <div class="clan-storage-pagination-controls flex items-center gap-2">
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
    <div class="clan-storage-pagination-toolbar clan-storage-deposit-pagination flex items-center justify-between gap-3">
      <p class="text-xs text-slate-500">${filteredItems.length} item(ns) encontrado(s)</p>
      ${totalPages > 1 ? `
        <div class="clan-storage-pagination-controls flex items-center gap-2">
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
    CHEST: renderChestIcon("w-6 h-6")
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

    let html = `
      <div class="clan-missions-section">
        <div class="clan-tab-heading clan-missions-heading">
          <div>
            <p class="clan-eyebrow clan-eyebrow-cyan">Objetivos coletivos</p>
            <h2 class="clan-section-title">Missões de Clã</h2>
          </div>
          <span class="clan-section-count">${catalog.length}</span>
        </div>
    `;

    if (hasActive) {
      html += renderPlayerMission(myMission);
    }

    html += `<div class="clan-missions-grid">`;
    html += catalog.map(m => {
      const active = myMission && myMission.missionId === m.id && myMission.status !== "CLAIMED";
      const doneToday = m.alreadyAccepted && !active;
      const locked = !active && !doneToday && m.minClanLevel > clan.level;
      const canAccept = !hasActive && !doneToday && !active && !locked;
      const missionStateClass = active ? "is-active" : doneToday ? "is-done" : locked ? "is-locked" : "";
      const stateHtml = active
        ? `<span class="clan-mission-status clan-status-cyan"><span class="clan-status-dot"></span>Ativa agora</span>`
        : doneToday
          ? `<span class="clan-mission-status clan-status-muted">Disponível amanhã</span>`
          : locked
            ? `<span class="clan-mission-status clan-status-amber">Requer nível ${m.minClanLevel}</span>`
            : `<span class="clan-mission-status clan-status-green"><span class="clan-status-dot"></span>Disponível</span>`;
      return `
        <article class="clan-mission-card ${missionStateClass}">
          <div class="clan-mission-card-topline">
            <span class="clan-mission-type">${escapeHtml(formatObjective(m.objectiveType).replace(/:$/, ""))}</span>
            ${stateHtml}
          </div>
          <div class="clan-mission-card-body">
            <h3 class="clan-mission-title">${escapeHtml(m.title)}</h3>
            <p class="clan-mission-description">${escapeHtml(m.description || "Sem descrição disponível.")}</p>
          </div>
          <div class="clan-mission-specs">
            <div class="clan-mission-spec"><span>Objetivo</span><strong>${formatObjective(m.objectiveType)} ${Number(m.targetValue).toLocaleString("pt-BR")}</strong></div>
            <div class="clan-mission-spec"><span>Recompensa</span><strong>${Number(m.minHonorMarksReward).toLocaleString("pt-BR")}-${Number(m.maxHonorMarksReward).toLocaleString("pt-BR")} HM</strong><small>+${Number(m.clanXpReward).toLocaleString("pt-BR")} XP de clã</small></div>
          </div>
          <div class="clan-mission-card-footer">
            <span class="clan-mission-availability">${locked ? `Nível atual: ${clan.level}` : active ? "Uma missão por vez" : "Contribua com sua equipe"}</span>
            ${canAccept ? `<button class="btn-primary clan-mission-action" onclick="clanAcceptMission('${m.id}')">Aceitar missão</button>` : ""}
          </div>
        </article>
      `;
    }).join("");
    html += `</div></div>`;

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
    <article class="clan-current-mission-card">
      <div class="clan-current-mission-heading"><div><p class="clan-eyebrow clan-eyebrow-cyan">Sua missão atual</p><h3 class="clan-current-mission-title">${escapeHtml(m.title)}</h3></div><span class="clan-current-mission-progress">${percent}%</span></div>
      <div class="clan-current-mission-meta"><span>${formatObjective(m.objectiveType)} ${Number(m.progress).toLocaleString("pt-BR")}/${Number(m.targetValue).toLocaleString("pt-BR")}</span><span>${Number(m.honorMarksReward).toLocaleString("pt-BR")} HM · ${Number(m.clanXpReward).toLocaleString("pt-BR")} XP</span></div>
      <div class="clan-current-mission-track"><span style="width:${percent}%"></span></div>
      <div class="clan-current-mission-footer">${m.status === "COMPLETED" || complete ? `<button class="btn-primary clan-mission-action" onclick="clanClaimMission('${m.id}')">Resgatar recompensa</button>` : `<span class="clan-mission-status clan-status-cyan">${formatMissionStatus(m.status)}</span>`}<span class="clan-mission-availability">Progresso compartilhado</span></div>
    </article>
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
    <div class="clan-mission-ranking-row flex justify-between items-center py-1.5 border-b border-slate-800 last:border-0">
      <div class="flex items-center gap-2">
        <span class="font-bold w-5 ${rankColor(i)}">${i + 1}.</span>
        <span class="text-sm text-slate-200">${escapeHtml(entry.username)}</span>
      </div>
      <span class="text-xs text-cyan-400 font-mono">${Number(entry.contribution).toLocaleString("pt-BR")} Marcas de Honra</span>
    </div>
  `).join("");

  return `
    <section class="clan-mission-ranking card mt-4 border-cyan-900">
      <div class="clan-tab-heading"><div><p class="clan-eyebrow clan-eyebrow-amber">Contribuição da equipe</p><h3 class="clan-section-title clan-section-title-sm">Classificação de Contribuição</h3></div><span class="clan-section-mark">✦</span></div>
      <div class="clan-mission-ranking-list">${rows}</div>
    </section>
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
    const formatNumber = (value) => Number(value || 0).toLocaleString("pt-BR");
    const cooldownCopy = cooldownEnabled
      ? `Intervalo de ${cooldownMinutes} minuto(s) entre ataques.`
      : "Intervalo desativado pelo administrador.";

    let rankingHtml = "";
    if (raid.ranking && raid.ranking.length > 0) {
      rankingHtml = `
        <section class="clan-raid-panel clan-raid-ranking-panel">
          <div class="clan-raid-panel-heading"><div><p class="clan-eyebrow clan-eyebrow-cyan">Impacto da equipe</p><h3 class="clan-section-title clan-section-title-sm">Classificação de dano</h3></div><span class="clan-section-mark">✦</span></div>
          <div class="clan-raid-ranking-list">
            ${raid.ranking.map((entry, i) => `
              <div class="clan-raid-ranking-row">
                <span class="clan-raid-rank ${i < 3 ? `clan-raid-rank--${i + 1}` : ""}">${entry.position || i + 1}</span>
                <div class="clan-raid-ranking-identity"><strong>${escapeHtml(entry.username)}</strong><span>Contribuição individual</span></div>
                <strong class="clan-raid-ranking-damage">${formatNumber(entry.totalDamage)}</strong>
              </div>
            `).join("")}
          </div>
        </section>
      `;
    }

    let attacksHtml = "";
    if (raid.recentAttacks && raid.recentAttacks.length > 0) {
      attacksHtml = `
        <section class="clan-raid-panel clan-raid-attacks-panel">
          <div class="clan-raid-panel-heading"><div><p class="clan-eyebrow">Registro de combate</p><h3 class="clan-section-title clan-section-title-sm">Últimos ataques</h3></div><span class="clan-section-count">${raid.recentAttacks.length}</span></div>
          <div class="clan-raid-attacks-list">
            ${raid.recentAttacks.map(a => `
              <div class="clan-raid-attack-row"><span>${escapeHtml(a.username)}</span><strong>+${formatNumber(a.damage)}</strong></div>
            `).join("")}
          </div>
        </section>
      `;
    }

    const defeatSummary = raid.defeatSummary;
    const defeatSummaryHtml = defeated && defeatSummary ? `
      <div class="clan-raid-defeat-summary">
        <div class="clan-raid-summary-heading"><span class="clan-raid-summary-icon">✓</span><div><p>Vitória confirmada</p><span>Resumo da última incursão</span></div></div>
        <div class="clan-raid-summary-grid">
          <div><span>Golpe final</span><strong>${escapeHtml(defeatSummary.finalBlowUsername || "Desconhecido")}</strong></div>
          <div><span>Maior dano</span><strong>${escapeHtml(defeatSummary.topDamageUsername || "Desconhecido")}</strong><small>${formatNumber(defeatSummary.topDamage)} de dano</small></div>
          <div><span>Ataques totais</span><strong>${formatNumber(defeatSummary.totalAttacks)}</strong></div>
          <div><span>Tempo vivo</span><strong>${formatBossAliveDuration(defeatSummary.aliveDurationSeconds)}</strong></div>
        </div>
        <div class="clan-raid-next-cycle"><span>Próximo ciclo</span><strong>${formatBossDateTime(defeatSummary.nextCycleAt)}</strong></div>
      </div>
    ` : "";

    container.innerHTML = `
      <div class="clan-raid-shell">
        <header class="clan-raid-heading">
          <div><p class="clan-eyebrow clan-eyebrow-red">Confronto cooperativo</p><h2 class="clan-section-title">Incursão de Clã</h2><p class="clan-raid-subtitle">Concentre o dano da equipe no mesmo alvo e acompanhe a evolução da batalha.</p></div>
          <span class="clan-raid-status ${defeated ? "clan-raid-status--success" : "clan-raid-status--battle"}"><span class="clan-status-dot"></span>${defeated ? "Incursão concluída" : "Batalha em andamento"}</span>
        </header>

        <div class="clan-raid-stage">
          <section class="clan-raid-boss-card">
            <div class="clan-raid-boss-intro">
              <div class="clan-raid-boss-portrait">${raid.bossImageUrl ? `<img src="${escapeHtml(raid.bossImageUrl)}" alt="${escapeHtml(raid.bossName || "Chefe da incursão")}" onerror="this.style.display='none'">` : "👾"}</div>
              <div class="clan-raid-boss-copy"><p class="clan-eyebrow">Alvo da incursão</p><h3>${escapeHtml(raid.bossName)}</h3><span>${defeated ? "Ameaça neutralizada pela equipe" : "Chefe ativo · Ataques coordenados"}</span></div>
            </div>
            <div class="clan-raid-health-block">
              <div class="clan-raid-health-heading"><span>Vitalidade do chefe</span><strong>${formatNumber(raid.remainingHp)} <small>/ ${formatNumber(raid.maxHp)} HP</small></strong></div>
              <div class="clan-raid-health-track" role="progressbar" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100" aria-label="${percent}% de vida restante"><span class="${defeated ? "clan-raid-health-fill--success" : ""}" style="width:${percent}%"></span></div>
              <div class="clan-raid-health-meta"><span>${percent}% restante</span><span>${defeated ? "Recompensas processadas" : "Não deixe a barra estabilizar"}</span></div>
            </div>
            <div class="clan-raid-contribution-grid"><div><span>Sua contribuição</span><strong>${formatNumber(raid.myTotalDamage)}</strong><small>dano total causado</small></div><div><span>Registro recente</span><strong>${formatNumber(raid.recentAttacks?.length || 0)}</strong><small>ataques no histórico</small></div></div>
          </section>

          <section class="clan-raid-command-card">
            <div class="clan-raid-command-heading"><div><p class="clan-eyebrow clan-eyebrow-cyan">Centro de comando</p><h3>${defeated ? "Incursão finalizada" : "Pronto para atacar?"}</h3></div><span class="clan-raid-command-icon">ϟ</span></div>
            <p class="clan-raid-command-copy">${defeated ? "A equipe venceu este confronto. Reúna seus aliados para o próximo ciclo." : "Cada ataque reduz a vitalidade do chefe e registra sua contribuição no ranking."}</p>
            ${!defeated ? `<div class="clan-raid-cooldown-block"><div><span class="clan-raid-cooldown-label">Janela de ataque</span><p>${cooldownCopy}</p></div><strong class="${cooldownActive ? "is-waiting" : "is-ready"}">${cooldownActive ? `Aguardar <span id="clan-raid-countdown">--:--</span>` : "Disponível agora"}</strong></div><button id="clan-raid-attack-button" class="btn-primary clan-raid-attack-btn" onclick="clanAttackRaid()"${cooldownActive ? " disabled" : ""}>${cooldownActive ? "Aguardar janela" : "Atacar chefe"}</button>` : `<div class="clan-raid-complete-block"><span class="clan-raid-complete-icon">✓</span><p>Incursão derrotada.<br><small>O próximo renascimento ocorrerá uma hora após a derrota.</small></p></div>${defeatSummaryHtml}`}
          </section>
        </div>

        ${rankingHtml || attacksHtml ? `<div class="clan-raid-support-grid">${rankingHtml}${attacksHtml}</div>` : ""}
      </div>
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
function formatBossDateTime(value) { if (!value) return "Não informado"; const date = new Date(value); return `${date.toLocaleDateString("pt-BR")} - ${date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}`; }

function showRaidAttackModal(result) {
  const existing = document.getElementById("raid-attack-modal");
  if (existing) existing.remove();

  const defeatSummary = result.defeatSummary;
  const defeatSummaryHtml = result.defeated && defeatSummary ? `<div class="mt-3 rounded-lg border border-green-800/70 bg-green-950/20 p-3 text-xs"><p class="font-bold text-green-300 mb-2">Resumo da derrota</p><div class="grid grid-cols-2 gap-2"><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Golpe final</p><p class="mt-1 font-semibold text-white">${escapeHtml(defeatSummary.finalBlowUsername || "Desconhecido")}</p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Maior dano</p><p class="mt-1 font-semibold text-white">${escapeHtml(defeatSummary.topDamageUsername || "Desconhecido")}<br><span class="text-cyan-300">${Number(defeatSummary.topDamage || 0).toLocaleString("pt-BR")} de dano</span></p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Ataques totais</p><p class="mt-1 font-semibold text-white">${Number(defeatSummary.totalAttacks || 0).toLocaleString("pt-BR")}</p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Tempo vivo</p><p class="mt-1 font-semibold text-white">${formatBossAliveDuration(defeatSummary.aliveDurationSeconds)}</p></div><div class="col-span-2 rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Próximo ciclo</p><p class="mt-1 font-semibold text-amber-300">${formatBossDateTime(defeatSummary.nextCycleAt)}</p></div></div></div>` : "";
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
    <div class="clan-form-page">
      <button class="clan-back-action" onclick="renderClansPage()">← Voltar</button>
      <header class="clan-form-header"><p class="clan-eyebrow clan-eyebrow-cyan">Novo grupo</p><h1 class="clan-page-title">Criar Clã</h1><p class="clan-page-subtitle">Reúna sua equipe e construa uma comunidade forte.</p></header>
      <div class="clan-form-card">
        <div class="clan-section-heading"><div><p class="clan-eyebrow">Identidade</p><h2 class="clan-section-title">Apresente seu clã</h2></div><span class="clan-section-mark" aria-hidden="true">✦</span></div>
      <label class="block text-sm text-slate-400 mb-1">Nome do Clã</label>
      <input type="text" id="clan-create-name" class="input w-full mb-3" maxlength="30" placeholder="Ex: DRO Heroes">

      <label class="block text-sm text-slate-400 mb-1">Tag (2-3 caracteres)</label>
      <input type="text" id="clan-create-tag" class="input w-full mb-3" maxlength="3" placeholder="Ex: DRO">

      <label class="block text-sm text-slate-400 mb-1">Descrição (opcional)</label>
      <textarea id="clan-create-desc" class="input w-full mb-4" maxlength="280" rows="3" placeholder="Descreva seu clã..."></textarea>

        <button class="btn-primary w-full" onclick="clanCreate()">Criar Clã</button>
      </div>
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
    <div class="clan-form-page">
      <button class="clan-back-action" onclick="renderClansPage()">← Voltar</button>
      <header class="clan-form-header"><p class="clan-eyebrow clan-eyebrow-cyan">Configurações</p><h1 class="clan-page-title">Editar Clã</h1><p class="clan-page-subtitle">Mantenha a identidade da sua equipe atualizada.</p></header>
      <div class="clan-form-card">
        <div class="clan-section-heading"><div><p class="clan-eyebrow">Identidade</p><h2 class="clan-section-title">Informações públicas</h2></div><span class="clan-section-mark" aria-hidden="true">✎</span></div>
      <label class="block text-sm text-slate-400 mb-1">Descrição</label>
      <textarea id="clan-edit-desc" class="input w-full mb-3" maxlength="280" rows="3"></textarea>

      <label class="block text-sm text-slate-400 mb-1">Emblema (código/icon curto)</label>
      <input type="text" id="clan-edit-emblem" class="input w-full mb-4" maxlength="50" placeholder="Ex: 🛡️">

        <button class="btn-primary w-full" onclick="clanUpdate('${id}')">Salvar</button>
      </div>
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
    <div class="page-container clan-page-container clan-ranking-page">
      <header class="clan-page-header clan-ranking-header">
        <div class="clan-page-header-copy">
          <p class="clan-eyebrow clan-eyebrow-amber">Competição · Temporada</p>
          <h1 class="clan-page-title">Classificação de Clãs</h1>
          <p class="clan-page-subtitle">Acompanhe as equipes que estão no topo pelo poder total.</p>
        </div>
        <button class="clan-back-action" onclick="renderClansPage()">Voltar</button>
      </header>
      <section class="clan-ranking-surface">
        <div class="clan-section-heading"><div><p class="clan-eyebrow">Panorama competitivo</p><h2 class="clan-section-title">Melhores equipes</h2></div><span class="clan-section-mark" aria-hidden="true">✦</span></div>
        <div id="clan-ranking-content">
          <div class="card animate-pulse"><div class="h-32"></div></div>
        </div>
      </section>
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
      <article class="clan-ranking-row" onclick="clanShowPreview('${e.id}')" role="button" tabindex="0" onkeydown="if(event.key==='Enter'||event.key===' ') { event.preventDefault(); clanShowPreview('${e.id}'); }">
        <div class="clan-ranking-position">${posIcon}</div>
        <div class="clan-ranking-identity"><p class="clan-ranking-name">${escapeHtml(e.name)} <span>${escapeHtml(e.tag)}</span></p><p class="clan-ranking-members">${e.memberCount} membros</p></div>
        <div class="clan-ranking-power"><span>Poder total</span><strong>${Number(e.totalPower || 0).toLocaleString("pt-BR")}</strong></div>
        <div class="clan-icon-button" aria-hidden="true">◉</div>
      </article>
    `;
  }).join("");

  if (clanRankingHasMore) {
    html += `<button class="btn-secondary clan-list-load-more" id="clan-ranking-load-more" onclick="clanRankingLoadMore()">Carregar mais</button>`;
  }

  content.innerHTML = html;
}

async function clanRankingLoadMore() {
  clanRankingPage++;
  await clanLoadRanking();
}
