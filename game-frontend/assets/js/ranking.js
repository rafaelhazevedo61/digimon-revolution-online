let rankTab = "power";
let rankPage = 0;
let rankEntries = [];
let rankLoading = false;
let rankHasMore = true;
let rankGeneration = 0;
let rankSearch = "";
let rankSearchTimeout = null;
const RANK_PAGE_SIZE = 10;

async function renderRankingPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  rankTab = "power";
  rankPage = 0;
  rankSearch = "";
  rankEntries = [];
  rankHasMore = true;

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">🏆 Ranking</h2>

      <div class="flex gap-2 mb-4" id="rank-tabs">
        <button class="tab-btn active" data-tab="power" onclick="rankSwitchTab('power')">Poder</button>
        <button class="tab-btn" data-tab="rebirth" onclick="rankSwitchTab('rebirth')">Rebirth</button>
      </div>

      <div class="mb-4" id="rank-search-wrap">
        <input
          id="rank-search"
          type="text"
          placeholder="Buscar jogador ou Digimon..."
          class="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-sm text-slate-100 placeholder-slate-500 focus:outline-none focus:border-cyan-500"
          value="${escapeHtml(rankSearch)}"
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
  rankGeneration++;
  rankLoading = false;

  document.querySelectorAll("#rank-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.tab === tab);
  });

  const searchInput = document.getElementById("rank-search");
  if (searchInput) {
    searchInput.value = "";
    searchInput.disabled = tab !== "power";
    searchInput.placeholder = tab === "power" ? "Buscar jogador ou Digimon..." : "Busca disponível apenas no ranking de Poder";
  }

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

  const content = document.getElementById("rank-content");
  if (rankPage === 0) {
    content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  const params = { page: rankPage, size: RANK_PAGE_SIZE };
  if (rankTab === "power" && rankSearch) {
    params.search = rankSearch;
  }

  try {
    const data = await apiGet(`/ranking/${rankTab}`, params);
    if (gen !== rankGeneration) return;
    rankEntries = data || [];
    rankHasMore = rankEntries.length === RANK_PAGE_SIZE;
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
