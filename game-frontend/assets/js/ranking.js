let rankTab = "level";
let rankPage = 0;
let rankEntries = [];
let rankLoading = false;
let rankHasMore = true;

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

  document.querySelectorAll("#rank-tabs .tab-btn").forEach(btn => {
    btn.classList.toggle("active", btn.dataset.tab === tab);
  });

  rankLoadPage();
}

async function rankLoadPage() {
  if (rankLoading) return;
  rankLoading = true;

  const content = document.getElementById("rank-content");
  if (rankPage === 0) {
    content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  try {
    const data = await apiGet(`/ranking/${rankTab}?page=${rankPage}&size=20`);
    rankEntries = rankPage === 0 ? data : [...rankEntries, ...data];
    rankHasMore = data.length === 20;
    rankRender();
  } catch (err) {
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

  let html = rankEntries.map(e => {
    const posIcon = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `<span class="text-slate-500 font-bold text-sm">#${e.position}</span>`;
    const stage = stageMap[e.digimonStage] || e.digimonStage;
    const gradeBadge = rankGradeBadge(e.grade);

    let detail = "";
    if (rankTab === "level") {
      detail = `<span class="text-cyan-400 font-bold">Lv.${e.level}</span>`;
    } else if (rankTab === "grade") {
      detail = `<span class="badge ${gradeBadge}">${escapeHtml(e.grade)}</span>`;
    } else {
      detail = `<span class="text-amber-400 font-bold">🔄 x${e.rebirthCount}</span>`;
    }

    return `
      <div class="card-sm mb-2 flex items-center gap-3">
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
          <p class="text-xs text-slate-500 mt-1">👤 ${escapeHtml(e.playerName)}</p>
        </div>
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
