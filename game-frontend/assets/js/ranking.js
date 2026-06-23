let rankTab = "level";
let rankPage = 0;
let rankEntries = [];
let rankLoading = false;
let rankHasMore = true;
let rankGeneration = 0;

async function renderRankingPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  rankTab = "level";
  rankPage = 0;
  rankEntries = [];
  rankHasMore = true;

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">🏆 Ranking</h2>

      <div class="flex gap-2 mb-4" id="rank-tabs">
        <button class="tab-btn active" data-tab="level" onclick="rankSwitchTab('level')">Nível</button>
        <button class="tab-btn" data-tab="grade" onclick="rankSwitchTab('grade')">Grade</button>
        <button class="tab-btn" data-tab="rebirth" onclick="rankSwitchTab('rebirth')">Rebirth</button>
      </div>

      <div id="rank-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  await rankLoadPage();
}

function rankSwitchTab(tab) {
  rankTab = tab;
  rankPage = 0;
  rankEntries = [];
  rankHasMore = true;
  rankGeneration++;
  rankLoading = false;

  document.querySelectorAll("#rank-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.tab === tab);
  });

  rankLoadPage();
}

async function rankLoadPage() {
  if (rankLoading) return;
  rankLoading = true;
  const gen = rankGeneration;

  const content = document.getElementById("rank-content");
  if (rankPage === 0) {
    content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  try {
    const data = await apiGet(`/ranking/${rankTab}?page=${rankPage}&size=20`);
    if (gen !== rankGeneration) return;
    rankEntries = rankPage === 0 ? data : [...rankEntries, ...data];
    rankHasMore = data.length === 20;
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

  if (rankEntries.length === 0) {
    const emptyMsg = rankTab === "rebirth"
      ? "Nenhum Digimon fez Rebirth ainda."
      : "Nenhum Digimon no ranking.";
    content.innerHTML = `<p class="text-slate-400 text-sm text-center py-8">${emptyMsg}</p>`;
    return;
  }

  const stageMap = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };

  const myPlayerId = getPlayerId();

  let html = rankEntries.map(e => {
    const posIcon = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `<span class="text-slate-500 font-bold text-sm">#${e.position}</span>`;
    const stage = stageMap[e.digimonStage] || e.digimonStage;
    const gradeBadge = rankGradeBadge(e.grade);
    const isOwn = e.playerId === myPlayerId;

    let detail = "";
    if (rankTab === "level") {
      detail = `<span class="text-cyan-400 font-bold">Lv.${e.level}</span>`;
    } else if (rankTab === "grade") {
      detail = `<span class="badge ${gradeBadge}">${escapeHtml(e.grade)}</span>`;
    } else {
      detail = `<span class="text-amber-400 font-bold">🔄 x${e.rebirthCount}</span>`;
    }

    return `
      <div class="card-sm mb-2 flex items-center gap-3 cursor-pointer ${isOwn ? "border-cyan-500 bg-cyan-950/30" : ""}" onclick="rankShowPreview('${e.digimonId}')">
        <div class="w-8 text-center text-lg">${posIcon}</div>
        <div class="text-2xl">🐉</div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <p class="font-bold text-sm truncate">${escapeHtml(e.digimonName)}</p>
            ${detail}
          </div>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge badge-${e.digimonStage.toLowerCase()}">${stage}</span>
            ${rankTab !== "grade" ? `<span class="badge ${gradeBadge}">${escapeHtml(e.grade)}</span>` : `<span class="text-cyan-400 text-xs font-bold">Lv.${e.level}</span>`}
            ${e.rebirthCount > 0 && rankTab !== "rebirth" ? `<span class="text-xs text-amber-400">🔄x${e.rebirthCount}</span>` : ""}
          </div>
          <p class="text-xs ${isOwn ? "text-cyan-400" : "text-slate-500"} mt-1">👤 ${escapeHtml(e.playerName)}${isOwn ? ' <span class="text-cyan-300 font-bold">(Voce)</span>' : ''}</p>
        </div>
        <div class="text-slate-500 text-lg">👁️</div>
      </div>
    `;
  }).join("");

  if (rankHasMore) {
    html += `
      <button class="btn-primary w-full mt-3" id="rank-load-more" onclick="rankLoadMore()">
        Carregar mais
      </button>
    `;
  }

  content.innerHTML = html;
}

function rankGradeBadge(grade) {
  const map = {
    SSS: "badge-legendary", SS: "badge-legendary", S: "badge-epic",
    A: "badge-rare", B: "badge-champion", C: "badge-common",
    D: "badge-common", E: "badge-common"
  };
  return map[grade] || "badge-common";
}

async function rankLoadMore() {
  const btn = document.getElementById("rank-load-more");
  if (btn) { btn.disabled = true; btn.textContent = "Carregando..."; }
  rankPage++;
  await rankLoadPage();
}

async function rankShowPreview(digimonId) {
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
    rankRenderModal(d, playerName);
  } catch (err) {
    document.getElementById("rank-modal-body").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function rankRenderModal(d, playerName) {
  const body = document.getElementById("rank-modal-body");
  const stageMap = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  const stage = stageMap[d.stage] || d.stage;
  const gradeBadge = rankGradeBadge(d.grade);

  const totalHp = d.hp + d.equipBonusHp;
  const totalAtk = d.attack + d.equipBonusAttack;
  const totalDef = d.defense + d.equipBonusDefense;

  body.innerHTML = `
    <div class="flex items-center gap-3 mb-4">
      <div class="text-4xl">🐉</div>
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2">
          <h3 class="font-bold text-lg truncate">${escapeHtml(d.name)}</h3>
          <span class="text-cyan-400 font-bold">Lv.${d.level}</span>
        </div>
        <div class="flex gap-2 mt-1 flex-wrap">
          <span class="badge badge-${d.stage.toLowerCase()}">${stage}</span>
          <span class="badge ${gradeBadge}">${escapeHtml(d.grade)}</span>
          <span class="badge badge-${(d.rarity || 'COMMON').toLowerCase()}">${escapeHtml(d.rarity)}</span>
        </div>
        <div class="flex gap-2 mt-1 flex-wrap">
          ${d.attribute ? `<span class="badge badge-common">${escapeHtml(d.attribute)}</span>` : ""}
          ${d.element ? `<span class="badge badge-common">${escapeHtml(d.element)}</span>` : ""}
          ${d.personality ? `<span class="badge badge-common">${escapeHtml(d.personality)}</span>` : ""}
        </div>
        ${d.rebirthCount > 0 ? `<p class="text-xs text-amber-400 mt-1">🔄 Rebirth x${d.rebirthCount}</p>` : ""}
        ${playerName ? `<p class="text-xs text-slate-500 mt-1">👤 ${escapeHtml(playerName)}</p>` : ""}
      </div>
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
