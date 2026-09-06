let rankTab = "power";
let rankPage = 0;
let rankEntries = [];
let rankLoading = false;
let rankHasMore = true;
let rankGeneration = 0;
let rankSearch = "";
let rankSearchTimeout = null;
let rankArenaMode = "current";
let rankArenaStatistics = null;
const RANK_PAGE_SIZE = 10;

async function renderRankingPage(params = {}) {
  const app = document.getElementById("app");
  showBottomNav("more");

  const validTabs = ["power", "rebirth", "arena", "clans"];
  rankTab = validTabs.includes(params.tab) ? params.tab : "power";
  rankPage = 0;
  rankSearch = "";
  rankEntries = [];
  rankHasMore = true;
  rankArenaMode = "current";
  rankArenaStatistics = null;

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">🏆 Ranking</h2>

      <div class="flex flex-wrap gap-2 mb-4" id="rank-tabs">
        <button class="tab-btn ${rankTab === "power" ? "active" : ""}" data-tab="power" onclick="rankSwitchTab('power')">Poder</button>
        <button class="tab-btn ${rankTab === "rebirth" ? "active" : ""}" data-tab="rebirth" onclick="rankSwitchTab('rebirth')">Rebirth</button>
        <button class="tab-btn ${rankTab === "arena" ? "active" : ""}" data-tab="arena" onclick="rankSwitchTab('arena')">Arena</button>
        <button class="tab-btn ${rankTab === "clans" ? "active" : ""}" data-tab="clans" onclick="rankSwitchTab('clans')">Clãs</button>
      </div>

      <div id="rank-arena-toggle" class="${rankTab === "arena" ? "flex" : "hidden"} gap-2 mb-4">
        <button class="tab-btn text-xs ${rankArenaMode === "current" ? "active" : ""}" data-arena-mode="current" onclick="rankSwitchArenaMode('current')">Atual</button>
        <button class="tab-btn text-xs ${rankArenaMode === "season" ? "active" : ""}" data-arena-mode="season" onclick="rankSwitchArenaMode('season')">Temporada</button>
      </div>

      <div class="mb-4" id="rank-search-wrap">
        <input
          id="rank-search"
          type="text"
          placeholder="${rankTab === "power" ? "Buscar jogador ou Digimon..." : "Busca disponível apenas no ranking de Poder"}"
          class="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          value="${escapeHtml(rankSearch)}"
          ${rankTab === "power" ? "" : "disabled"}
          oninput="rankOnSearchInput(this.value)"
        />
      </div>

      <div id="rank-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>

      <div id="rank-pagination" class="mt-4"></div>
    </div>
  `;

  await rankLoadPage();
}

function rankSwitchTab(tab) {
  rankTab = tab;
  rankPage = 0;
  rankSearch = "";
  rankEntries = [];
  rankHasMore = true;
  rankArenaMode = "current";
  rankArenaStatistics = null;
  rankGeneration++;
  rankLoading = false;

  document.querySelectorAll("#rank-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.tab === tab);
  });

  const arenaToggle = document.getElementById("rank-arena-toggle");
  if (arenaToggle) {
    arenaToggle.classList.toggle("hidden", tab !== "arena");
    arenaToggle.classList.toggle("flex", tab === "arena");
  }
  document.querySelectorAll("#rank-arena-toggle .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.arenaMode === rankArenaMode);
  });

  const searchInput = document.getElementById("rank-search");
  if (searchInput) {
    searchInput.value = "";
    searchInput.disabled = tab !== "power";
    searchInput.placeholder = tab === "power" ? "Buscar jogador ou Digimon..." : "Busca disponível apenas no ranking de Poder";
  }

  rankLoadPage();
}

function rankSwitchArenaMode(mode) {
  if (!["current", "season"].includes(mode) || rankTab !== "arena") return;
  rankArenaMode = mode;
  rankPage = 0;
  rankEntries = [];
  rankHasMore = true;
  rankArenaStatistics = null;
  rankGeneration++;
  rankLoading = false;

  document.querySelectorAll("#rank-arena-toggle .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.arenaMode === mode);
  });

  rankLoadPage();
}

function rankOnSearchInput(value) {
  clearTimeout(rankSearchTimeout);
  if (rankTab !== "power") return;
  rankSearchTimeout = setTimeout(() => {
    rankSearch = value.trim();
    rankPage = 0;
    rankLoadPage();
  }, 300);
}

function rankChangePage(delta) {
  if (rankLoading) return;
  rankPage = Math.max(0, rankPage + delta);
  rankLoadPage();
}

async function rankLoadPage() {
  if (rankLoading) return;
  rankLoading = true;
  const gen = ++rankGeneration;
  const tab = rankTab;
  const arenaMode = rankArenaMode;

  const content = document.getElementById("rank-content");
  if (rankPage === 0) {
    content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  const params = { page: rankPage, size: RANK_PAGE_SIZE };
  if (rankTab === "power" && rankSearch) {
    params.search = rankSearch;
  }

  try {
    let data;
    if (tab === "arena") {
      const rankingRequest = apiGet(
        arenaMode === "season" ? "/arena/season-ranking" : "/arena/ranking",
        params
      );
      const statisticsRequest = arenaMode === "current"
        ? apiGet("/arena/statistics").catch(() => null)
        : Promise.resolve(null);
      const [ranking, statistics] = await Promise.all([rankingRequest, statisticsRequest]);
      data = ranking || [];
      if (gen === rankGeneration) rankArenaStatistics = statistics;
    } else if (tab === "clans") {
      data = await apiGet("/clans/ranking", params);
    } else {
      data = await apiGet(`/ranking/${tab}`, params);
    }
    if (gen !== rankGeneration) return;
    rankEntries = tab === "clans" ? (data.content || []) : (data || []);
    rankHasMore = tab === "clans" ? !data.last : rankEntries.length === RANK_PAGE_SIZE;
    rankRender();
  } catch (err) {
    if (gen !== rankGeneration) return;
    content.innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  } finally {
    rankLoading = false;
  }
}

function rankRender() {
  const content = document.getElementById("rank-content");
  const pagination = document.getElementById("rank-pagination");

  if (rankTab === "arena") {
    const summary = rankArenaMode === "current" && rankArenaStatistics
      ? `<div class="arena-ranking-summary"><div class="arena-summary-title">Seu histórico de arena</div><div class="arena-summary-grid"><div><span>Saldo</span><strong class="${rankArenaStatistics.netPoints >= 0 ? "is-positive" : "is-negative"}">${Number(rankArenaStatistics.netPoints || 0).toLocaleString("pt-BR")}</strong></div><div><span>Ganhos</span><strong>${Number(rankArenaStatistics.pointsWon || 0).toLocaleString("pt-BR")}</strong></div><div><span>Perdas</span><strong class="is-negative">${Number(rankArenaStatistics.pointsLost || 0).toLocaleString("pt-BR")}</strong></div></div></div>`
      : "";

    if (rankEntries.length === 0) {
      content.innerHTML = `${summary}<p class="text-slate-400 text-sm text-center py-8">Classificação da arena vazia no momento.</p>`;
      if (pagination) pagination.innerHTML = "";
      return;
    }

    const myPlayerId = getPlayerId();
    const html = rankEntries.map(e => {
      const posIcon = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `#${e.position}`;
      const isMine = myPlayerId && e.playerId === myPlayerId;
      if (rankArenaMode === "season") {
        return `<article class="arena-ranking-row ${isMine ? "is-mine" : ""}"><span class="arena-ranking-position">${posIcon}</span><div class="arena-ranking-identity"><strong>@${escapeHtml(e.playerName)}</strong>${isMine ? `<span class="arena-you-badge">você</span>` : ""}<p><span class="is-positive">${e.wins}V</span> / <span class="is-negative">${e.losses}D</span> · ganhos ${Number(e.pointsWon || 0).toLocaleString("pt-BR")} · perdas ${Number(e.pointsLost || 0).toLocaleString("pt-BR")}</p></div><strong class="arena-ranking-score">${Number(e.netPoints || 0).toLocaleString("pt-BR")}<small>pts</small></strong></article>`;
      }
      return `<article class="arena-ranking-row ${isMine ? "is-mine" : ""}"><span class="arena-ranking-position">${posIcon}</span><div class="arena-ranking-identity"><div class="arena-ranking-name-row"><strong>${escapeHtml(e.digimonName)}</strong>${arenaTierBadge(e.tier)}${isMine ? `<span class="arena-you-badge">você</span>` : ""}</div><p>@${escapeHtml(e.playerName)} · ${escapeHtml(ARENA_STAGE_LABELS[e.stage] || e.stage)} Lv.${e.level} · <span class="is-positive">${e.wins}V</span> / <span class="is-negative">${e.losses}D</span></p></div><strong class="arena-ranking-score">${Number(e.rating || 0).toLocaleString("pt-BR")}<small>pts</small></strong></article>`;
    }).join("");
    content.innerHTML = `${summary}${html}`;
  } else if (rankTab === "clans") {
    if (rankEntries.length === 0) {
      content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">Nenhum clã no ranking.</p>`;
      if (pagination) pagination.innerHTML = "";
      return;
    }

    content.innerHTML = rankEntries.map(e => {
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
  }

  if (rankTab === "arena" || rankTab === "clans") {
    if (pagination) {
      pagination.innerHTML = `
        <div class="flex justify-between items-center gap-2">
          <button class="btn-secondary text-xs" ${rankPage <= 0 ? "disabled" : ""} onclick="rankChangePage(-1)">← Anterior</button>
          <span class="text-sm text-slate-400">Página ${rankPage + 1}</span>
          <button class="btn-secondary text-xs" ${!rankHasMore ? "disabled" : ""} onclick="rankChangePage(1)">Próxima →</button>
        </div>
      `;
    }
    return;
  }

  if (rankEntries.length === 0) {
    const emptyMsg = rankTab === "rebirth"
      ? "Nenhum Digimon fez Rebirth ainda."
      : rankSearch
        ? "Nenhum Digimon encontrado para a busca."
        : "Nenhum Digimon no ranking.";
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">${emptyMsg}</p>`;
    if (pagination) pagination.innerHTML = "";
    return;
  }

  const stageMap = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  const myPlayerId = getPlayerId();

  let html = rankEntries.map(e => {
    const posIcon = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `<span class="text-slate-500 font-bold text-sm">#${e.position}</span>`;
    const stage = stageMap[e.digimonStage] || e.digimonStage;
    const gradeBadge = rankGradeBadge(e.grade);
    const isOwn = e.playerId === myPlayerId;
    const power = rankTab === "power" ? e.power : null;

    const detail = rankTab === "power"
      ? `<span class="text-amber-400 font-bold">⚔ ${Number(e.power || 0).toLocaleString("pt-BR")}</span>`
      : `<span class="text-amber-400 font-bold">🔄 x${e.rebirthCount}</span>`;

    return `
      <div class="card-sm mb-2 flex items-center gap-3 cursor-pointer ${isOwn ? "border-cyan-500 bg-cyan-950/30" : ""}" onclick="rankShowPreview('${e.digimonId}', ${power ?? 'null'})">
        <div class="w-8 text-center text-lg">${posIcon}</div>
        ${renderDigimonVisual(e.imageUrl, e.digimonStage, "w-12 h-12", "text-3xl")}
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <p class="font-bold text-sm truncate">${escapeHtml(e.digimonName)}</p>
            ${detail}
          </div>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge badge-${e.digimonStage.toLowerCase()}">${stage}</span>
            <span class="badge ${gradeBadge}">${escapeHtml(e.grade)}</span>
            ${rankTab === "power" ? `<span class="text-cyan-400 text-xs font-bold">Lv.${e.level}</span>` : ""}
            ${e.rebirthCount > 0 && rankTab === "power" ? `<span class="text-xs text-amber-400">🔄x${e.rebirthCount}</span>` : ""}
          </div>
          <p class="text-xs ${isOwn ? "text-cyan-400" : "text-slate-500"} mt-1">👤 ${escapeHtml(e.playerName)}${isOwn ? ' <span class="text-cyan-300 font-bold">(Voce)</span>' : ''}</p>
        </div>
        <div class="text-slate-500 text-lg">👁️</div>
      </div>
    `;
  }).join("");

  content.innerHTML = html;

  if (pagination) {
    pagination.innerHTML = `
      <div class="flex justify-between items-center gap-2">
        <button class="btn-secondary text-xs" ${rankPage <= 0 ? "disabled" : ""} onclick="rankChangePage(-1)">← Anterior</button>
        <span class="text-sm text-slate-400">Página ${rankPage + 1}</span>
        <button class="btn-secondary text-xs" ${!rankHasMore ? "disabled" : ""} onclick="rankChangePage(1)">Próxima →</button>
      </div>
    `;
  }
}

function rankGradeBadge(grade) {
  const map = {
    SSS: "badge-legendary", SS: "badge-legendary", S: "badge-epic",
    A: "badge-rare", B: "badge-champion", C: "badge-common",
    D: "badge-common", E: "badge-common"
  };
  return map[grade] || "badge-common";
}

async function rankShowPreview(digimonId, power) {
  const overlay = document.createElement("div");
  overlay.id = "rank-modal-overlay";
  overlay.className = "fixed inset-0 z-50 flex items-end justify-center";
  overlay.style.background = "rgba(0,0,0,0.6)";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  overlay.innerHTML = `
    <div class="w-full max-w-md rounded-t-2xl p-4 pb-8" style="background:#0f172a;max-height:80vh;overflow-y:auto">
      <div class="flex justify-between items-center mb-3">
        <h3 class="font-bold text-lg">Detalhes do Digimon</h3>
        <button class="text-slate-400 text-xl" onclick="document.getElementById('rank-modal-overlay').remove()">&times;</button>
      </div>
      <div id="rank-modal-body">
        <div class="card animate-pulse"><div class="h-24"></div></div>
      </div>
    </div>
  `;

  document.body.appendChild(overlay);

  try {
    const d = await apiGet(`/digimon/${digimonId}`);
    const entry = rankEntries.find(e => e.digimonId === digimonId);
    const playerName = entry ? entry.playerName : "";
    rankRenderModal(d, playerName, power);
  } catch (err) {
    document.getElementById("rank-modal-body").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function rankRenderModal(d, playerName, power) {
  const body = document.getElementById("rank-modal-body");
  const stageMap = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  const stage = stageMap[d.stage] || d.stage;
  const gradeBadge = rankGradeBadge(d.grade);

  const totalHp = d.hp + d.equipBonusHp + (d.clanBonusHp || 0);
  const totalAtk = d.attack + d.equipBonusAttack + (d.clanBonusAttack || 0);
  const totalDef = d.defense + d.equipBonusDefense + (d.clanBonusDefense || 0);
  const modalPower = power != null ? power : Math.round(totalHp * 0.3 + totalAtk * 1.5 + totalDef * 1);

  body.innerHTML = `
    <div class="flex items-center gap-3 mb-4">
      ${renderDigimonVisual(d.imageUrl, d.stage, "w-16 h-16", "text-4xl")}
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2">
          <h3 class="font-bold text-lg truncate">${escapeHtml(d.name)}</h3>
          <span class="text-cyan-400 font-bold">Lv.${d.level}</span>
        </div>
        <div class="flex gap-2 mt-1 flex-wrap">
          <span class="badge badge-${d.stage.toLowerCase()}">${stage}</span>
          <span class="badge ${gradeBadge}">${escapeHtml(d.grade)}</span>
          <span class="badge badge-${(d.rarity || 'COMMON').toLowerCase()}">${escapeHtml(formatRarity(d.rarity))}</span>${renderRarityDieIndicator(d)}
        </div>
        <div class="flex gap-2 mt-1 flex-wrap">
          ${d.attribute ? `<span class="badge badge-common">${escapeHtml(d.attribute)}</span>` : ""}
          ${d.element ? `<span class="badge badge-common">${escapeHtml(d.element)}</span>` : ""}
          ${d.personality ? `<span class="badge badge-common">${escapeHtml(d.personality)}</span>` : ""}
        </div>
        ${d.rebirthCount > 0 ? `<p class="text-xs text-amber-400 mt-1">🔄 Rebirth x${d.rebirthCount}</p>` : ""}
        ${playerName ? `<p class="text-xs text-slate-500 mt-1">👤 ${escapeHtml(playerName)}</p>` : ""}
        ${renderRarityDieDetails(d)}
      </div>
    </div>

    <div class="card-sm mb-2 flex justify-between items-center">
      <span class="text-xs text-slate-500">Poder</span>
      <span class="font-bold text-amber-400 text-lg">⚔ ${Number(modalPower).toLocaleString("pt-BR")}</span>
    </div>

    <div class="grid grid-cols-3 gap-2 mb-4">
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">HP</p>
        <p class="font-bold text-red-400">${totalHp}</p>
        ${d.equipBonusHp > 0 ? `<p class="text-xs text-green-400">+${d.equipBonusHp}</p>` : ""}
      </div>
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">ATK</p>
        <p class="font-bold text-orange-400">${totalAtk}</p>
        ${d.equipBonusAttack > 0 ? `<p class="text-xs text-green-400">+${d.equipBonusAttack}</p>` : ""}
      </div>
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">DEF</p>
        <p class="font-bold text-blue-400">${totalDef}</p>
        ${d.equipBonusDefense > 0 ? `<p class="text-xs text-green-400">+${d.equipBonusDefense}</p>` : ""}
      </div>
    </div>

    <div class="grid grid-cols-3 gap-2 mb-4">
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">IV HP</p>
        <p class="font-bold text-sm text-red-300">${d.ivHp}</p>
      </div>
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">IV ATK</p>
        <p class="font-bold text-sm text-orange-300">${d.ivAttack}</p>
      </div>
      <div class="card-sm text-center">
        <p class="text-xs text-slate-500">IV DEF</p>
        <p class="font-bold text-sm text-blue-300">${d.ivDefense}</p>
      </div>
    </div>

    ${d.trait ? `
    <div class="card-sm mb-2">
      <p class="text-xs text-slate-500">Trait</p>
      <p class="font-bold text-sm">${escapeHtml(d.trait)}</p>
    </div>
    ` : ""}
  `;
}
