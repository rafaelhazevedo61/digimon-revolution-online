let dexPage = 0;
let dexEntries = [];
let dexLoading = false;
let dexHasMore = true;
let dexFilters = { name: "", stage: "", attribute: "", element: "" };

async function renderPokedexPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  dexPage = 0;
  dexEntries = [];
  dexHasMore = true;
  dexFilters = { name: "", stage: "", attribute: "", element: "" };

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">📖 Digimon Info</h2>

      <div class="card mb-4" id="dex-filters">
        <div class="mb-2">
          <input type="text" id="dex-search" class="w-full px-3 py-2 rounded-lg text-sm text-white" style="background:#1e293b;border:1px solid #334155" placeholder="🔍 Buscar por nome..." oninput="dexOnSearch(this.value)">
        </div>
        <div class="flex flex-col gap-2">
          <select id="dex-stage" class="w-full px-3 py-2 rounded text-sm text-white" style="background:#1e293b;border:1px solid #334155" onchange="dexOnFilter()">
            <option value="">Todos os estágios</option>
            <option value="BABY">Baby</option>
            <option value="BABY_II">Baby II</option>
            <option value="ROOKIE">Rookie</option>
            <option value="CHAMPION">Champion</option>
            <option value="ULTIMATE">Ultimate</option>
            <option value="MEGA">Mega</option>
          </select>
          <select id="dex-attr" class="w-full px-3 py-2 rounded text-sm text-white" style="background:#1e293b;border:1px solid #334155" onchange="dexOnFilter()">
            <option value="">Todos os atributos</option>
            <option value="DATA">Data</option>
            <option value="VACCINE">Vaccine</option>
            <option value="VIRUS">Virus</option>
            <option value="NONE">None</option>
          </select>
          <select id="dex-elem" class="w-full px-3 py-2 rounded text-sm text-white" style="background:#1e293b;border:1px solid #334155" onchange="dexOnFilter()">
            <option value="">Todos os elementos</option>
            <option value="FIRE">Fogo</option>
            <option value="WATER">Água</option>
            <option value="ICE">Gelo</option>
            <option value="WIND">Vento</option>
            <option value="EARTH">Terra</option>
            <option value="THUNDER">Trovão</option>
            <option value="LIGHT">Luz</option>
            <option value="DARK">Sombrio</option>
            <option value="PITCH_BLACK">Negro</option>
            <option value="STEEL">Metal</option>
            <option value="WOOD">Madeira</option>
            <option value="NEUTRAL">Neutro</option>
          </select>
        </div>
      </div>

      <div id="dex-count" class="text-xs text-slate-500 mb-2 px-1"></div>

      <div id="dex-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  await dexLoadPage();
}

let dexSearchTimeout = null;
function dexOnSearch(val) {
  clearTimeout(dexSearchTimeout);
  dexSearchTimeout = setTimeout(() => {
    dexFilters.name = val.trim();
    dexReset();
  }, 300);
}

function dexOnFilter() {
  dexFilters.stage = document.getElementById("dex-stage").value;
  dexFilters.attribute = document.getElementById("dex-attr").value;
  dexFilters.element = document.getElementById("dex-elem").value;
  dexReset();
}

function dexReset() {
  dexPage = 0;
  dexEntries = [];
  dexHasMore = true;
  dexLoading = false;
  dexLoadPage();
}

async function dexLoadPage() {
  if (dexLoading || !dexHasMore) return;
  dexLoading = true;

  const content = document.getElementById("dex-content");
  if (dexPage === 0) {
    content.innerHTML = `<div class="card animate-pulse"><div class="h-32"></div></div>`;
  }

  try {
    let url = `/digimon-infos?page=${dexPage}&size=20`;
    if (dexFilters.name) url += `&name=${encodeURIComponent(dexFilters.name)}`;
    if (dexFilters.stage) url += `&stage=${encodeURIComponent(dexFilters.stage)}`;
    if (dexFilters.attribute) url += `&attribute=${encodeURIComponent(dexFilters.attribute)}`;
    if (dexFilters.element) url += `&element=${encodeURIComponent(dexFilters.element)}`;

    const data = await apiGet(url);
    const items = data.items || [];

    dexEntries = dexEntries.concat(items);
    dexHasMore = data.hasNext;

    const countEl = document.getElementById("dex-count");
    if (countEl) countEl.textContent = `${dexEntries.length} de ${data.totalItems} Digimons`;

    dexRender();
  } catch (err) {
    content.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }

  dexLoading = false;
}

function dexRender() {
  const content = document.getElementById("dex-content");

  if (dexEntries.length === 0) {
    content.innerHTML = `
      <div class="card text-center">
        <p class="text-2xl mb-2">🔍</p>
        <p class="text-slate-400">Nenhum Digimon encontrado.</p>
        <p class="text-xs text-slate-500 mt-1">Tente outros filtros.</p>
      </div>
    `;
    return;
  }

  let html = `
    <div class="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 gap-2" aria-label="Digimons disponíveis">
      ${dexEntries.map(d => `
        <button type="button" class="card-sm group text-left p-1.5 cursor-pointer transition-all hover:border-cyan-500 hover:bg-slate-800/80 focus:outline-none focus:ring-2 focus:ring-cyan-400" onclick="dexShowDetail(${d.id})" aria-label="Ver detalhes de ${escapeAttr(d.name)}">
          <div class="w-full aspect-square rounded-lg overflow-hidden bg-slate-900/70 flex items-center justify-center group-hover:bg-slate-900 transition-colors">
            ${renderDigimonVisual(d.imageUrl, d.stage, "w-full h-full", "text-5xl")}
          </div>
          <p class="font-bold text-[11px] sm:text-xs truncate mt-1.5" title="${escapeAttr(d.name)}">${escapeHtml(d.name)}</p>
        </button>
      `).join("")}
    </div>
  `;

  if (dexHasMore) {
    html += `
      <button class="btn-primary w-full mt-3" id="dex-load-more" onclick="dexLoadMore()">
        Carregar mais
      </button>
    `;
  }

  content.innerHTML = html;
}

async function dexLoadMore() {
  const btn = document.getElementById("dex-load-more");
  if (btn) { btn.disabled = true; btn.textContent = "Carregando..."; }
  dexPage++;
  await dexLoadPage();
}

async function dexShowDetail(infoId) {
  const d = dexEntries.find(e => e.id === infoId);
  if (!d) return;

  const overlay = document.createElement("div");
  overlay.id = "dex-modal-overlay";
  overlay.className = "fixed inset-0 z-50 flex items-end justify-center";
  overlay.style.background = "rgba(0,0,0,0.6)";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  const stage = dexStageName(d.stage);
  const total = d.baseHp + d.baseAtk + d.baseDef;

  const maxStat = Math.max(d.baseHp, d.baseAtk, d.baseDef, 1);
  const hpPct = Math.round((d.baseHp / maxStat) * 100);
  const atkPct = Math.round((d.baseAtk / maxStat) * 100);
  const defPct = Math.round((d.baseDef / maxStat) * 100);

  overlay.innerHTML = `
    <div class="w-full max-w-md rounded-t-2xl p-4 pb-8" style="background:#0f172a;max-height:80vh;overflow-y:auto">
      <div class="flex justify-between items-center mb-3">
        <h3 class="font-bold text-lg">Detalhes</h3>
        <button class="text-slate-400 text-xl" onclick="document.getElementById('dex-modal-overlay').remove()">&times;</button>
      </div>

      <div class="flex items-center gap-3 mb-4">
        ${renderDigimonVisual(d.imageUrl, d.stage, "w-20 h-20", "text-5xl")}
        <div class="flex-1 min-w-0">
          <h3 class="font-bold text-xl">${escapeHtml(d.name)}</h3>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge badge-${d.stage.toLowerCase()}">${stage}</span>
            ${d.rarity ? `<span class="badge badge-${d.rarity.toLowerCase()}">${escapeHtml(formatRarity(d.rarity))}</span>` : ""}
            <span class="badge badge-common">${escapeHtml(formatAttribute(d.attribute))}</span>
            <span class="badge badge-common">${dexElementLabel(d.element)}</span>
            <span class="badge badge-common">${escapeHtml(d.specie)}</span>
          </div>
        </div>
      </div>

      <div class="card mb-3">
        <h4 class="text-xs text-slate-500 font-bold mb-2">BASE STATS</h4>
        <div class="flex flex-col gap-2">
          <div class="flex items-center gap-2">
            <span class="text-xs text-red-400 w-8 text-right font-bold">HP</span>
            <div class="flex-1 bg-slate-800 rounded-full h-3">
              <div class="h-3 rounded-full" style="background:#f87171;width:${hpPct}%"></div>
            </div>
            <span class="text-sm font-bold text-red-400 w-10 text-right">${d.baseHp}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs text-orange-400 w-8 text-right font-bold">ATK</span>
            <div class="flex-1 bg-slate-800 rounded-full h-3">
              <div class="h-3 rounded-full" style="background:#fb923c;width:${atkPct}%"></div>
            </div>
            <span class="text-sm font-bold text-orange-400 w-10 text-right">${d.baseAtk}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs text-blue-400 w-8 text-right font-bold">DEF</span>
            <div class="flex-1 bg-slate-800 rounded-full h-3">
              <div class="h-3 rounded-full" style="background:#60a5fa;width:${defPct}%"></div>
            </div>
            <span class="text-sm font-bold text-blue-400 w-10 text-right">${d.baseDef}</span>
          </div>
        </div>
        <p class="text-xs text-slate-500 mt-2 text-right">Total: <span class="font-bold text-slate-300">${total}</span></p>
      </div>
    </div>
  `;

  document.body.appendChild(overlay);
}

// ==================== HELPERS ====================

function dexStageName(stage) {
  const map = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  return map[stage] || stage;
}

function dexStageEmoji(stage) {
  const map = { BABY: "🥒", BABY_II: "🐣", ROOKIE: "🐉", CHAMPION: "⚔️", ULTIMATE: "🔥", MEGA: "👑" };
  return map[stage] || "🐉";
}

function dexElementLabel(element) {
  const emoji = {
    FIRE: "🔥", WATER: "💧", ICE: "❄️", WIND: "🌪️",
    EARTH: "🌍", THUNDER: "⚡", LIGHT: "✨", DARK: "🌑",
    PITCH_BLACK: "🖤", STEEL: "⚙️", WOOD: "🌿", NEUTRAL: "⚪"
  }[element] || "";
  return `${formatElement(element)} ${emoji}`.trim();
}
