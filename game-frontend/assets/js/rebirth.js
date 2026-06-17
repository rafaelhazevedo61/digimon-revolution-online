let rebirthDigimonId = null;
let rebirthPreview = null;

async function renderRebirthPage() {
  const app = document.getElementById("app");
  showBottomNav("dashboard");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center gap-2 mb-4">
        <button class="btn-sm" style="background:#1e293b;color:#94a3b8" onclick="navigateTo('dashboard')">&larr; Voltar</button>
        <h2 class="text-lg font-bold">Rebirth</h2>
      </div>
      <div id="rebirth-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  try {
    const dashboard = await apiGet("/players/me/dashboard");
    const d = dashboard.activeDigimon;
    if (!d) {
      document.getElementById("rebirth-content").innerHTML = `
        <div class="card"><p class="text-slate-400 text-center">Nenhum Digimon ativo.</p></div>
      `;
      return;
    }

    rebirthDigimonId = d.id;
    rebirthPreview = await apiGet(`/digimon/${d.id}/rebirth-preview`);
    rebirthRender(d);
  } catch (err) {
    document.getElementById("rebirth-content").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function rebirthRender(digimon) {
  const content = document.getElementById("rebirth-content");
  const p = rebirthPreview;

  const stageMap = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  const formatStg = s => stageMap[s] || s;

  let html = `
    <div class="card mb-4">
      <div class="flex items-center gap-3">
        <div class="text-4xl">🐉</div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <h3 class="font-bold text-lg truncate">${escapeHtml(digimon.name)}</h3>
            <span class="text-sm font-bold text-cyan-400">Lv.${digimon.level}</span>
          </div>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge badge-${digimon.stage.toLowerCase()}">${formatStg(digimon.stage)}</span>
            ${digimon.attribute ? `<span class="badge badge-common">${escapeHtml(digimon.attribute)}</span>` : ""}
            ${digimon.element ? `<span class="badge badge-common">${escapeHtml(digimon.element)}</span>` : ""}
          </div>
          ${p.currentRebirthCount > 0 ? `<p class="text-xs text-amber-400 mt-1">🔄 Rebirth x${p.currentRebirthCount}</p>` : ""}
        </div>
      </div>
    </div>

    <!-- Rebirth info -->
    <div class="card mb-4" style="border-color:#854d0e">
      <h3 class="font-bold text-amber-400 mb-3">🔄 Rebirth #${p.newRebirthCount}</h3>
      <p class="text-xs text-slate-400 mb-4">O Digimon renasce como um novo ovo, mantendo bônus de IV e stats acumulados.</p>

      <!-- Costs -->
      <div class="mb-4">
        <p class="text-xs font-bold text-slate-300 mb-2">Custo:</p>
        <div class="grid grid-cols-2 gap-2">
          <div class="card-sm text-center">
            <p class="text-xs text-slate-500">Bits</p>
            <p class="font-bold ${p.currentBits >= p.costBits ? 'text-yellow-400' : 'text-red-400'}">${p.costBits.toLocaleString()}</p>
            <p class="text-xs ${p.currentBits >= p.costBits ? 'text-slate-500' : 'text-red-400'}">Você tem: ${p.currentBits.toLocaleString()}</p>
          </div>
          <div class="card-sm text-center">
            <p class="text-xs text-slate-500">Data Core</p>
            <p class="font-bold text-purple-400">${p.costDataCore}</p>
          </div>
        </div>
      </div>

      <!-- Stat Multiplier -->
      <div class="mb-4">
        <p class="text-xs font-bold text-slate-300 mb-2">Bônus de Stats:</p>
        <div class="card-sm text-center">
          <p class="text-xs text-slate-500">Multiplicador</p>
          <p class="font-bold text-green-400">x${p.statMultiplier.toFixed(2)}</p>
          <p class="text-xs text-slate-500">+${Math.round((p.statMultiplier - 1) * 100)}% em HP, ATK e DEF</p>
        </div>
      </div>

      <!-- IV Ranges -->
      <div class="mb-4">
        <p class="text-xs font-bold text-slate-300 mb-2">IVs após Rebirth (mín — máx):</p>
        <div class="grid grid-cols-3 gap-2">
          <div class="card-sm text-center">
            <p class="text-xs text-slate-500">HP</p>
            <p class="text-sm font-bold text-red-400">${p.hpIvRange.min} — ${p.hpIvRange.max}</p>
            <p class="text-xs text-slate-600">Atual: ${digimon.ivHp}</p>
          </div>
          <div class="card-sm text-center">
            <p class="text-xs text-slate-500">ATK</p>
            <p class="text-sm font-bold text-orange-400">${p.attackIvRange.min} — ${p.attackIvRange.max}</p>
            <p class="text-xs text-slate-600">Atual: ${digimon.ivAttack}</p>
          </div>
          <div class="card-sm text-center">
            <p class="text-xs text-slate-500">DEF</p>
            <p class="text-sm font-bold text-blue-400">${p.defenseIvRange.min} — ${p.defenseIvRange.max}</p>
            <p class="text-xs text-slate-600">Atual: ${digimon.ivDefense}</p>
          </div>
        </div>
      </div>

      <!-- Bits remaining -->
      ${p.eligible ? `
      <div class="mb-4">
        <div class="flex justify-between text-xs text-slate-400">
          <span>Bits restantes após Rebirth:</span>
          <span class="text-yellow-400 font-bold">${p.remainingBitsAfterRebirth.toLocaleString()}</span>
        </div>
      </div>
      ` : ""}
    </div>

    <!-- Action -->
    <div class="mb-4">
      ${p.eligible ? `
        <button class="btn-primary w-full text-lg py-3" id="rebirth-btn" onclick="rebirthExecute()">
          🔄 Renascer!
        </button>
      ` : `
        <button class="btn-primary w-full opacity-50 cursor-not-allowed py-3" disabled>
          Requisitos não atendidos
        </button>
        <p class="text-xs text-red-400 text-center mt-2">${escapeHtml(p.reason)}</p>
      `}
    </div>
  `;

  content.innerHTML = html;
}

async function rebirthExecute() {
  const btn = document.getElementById("rebirth-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Renascendo..."; }

  try {
    await apiPost("/digimon/rebirth", { digimonId: rebirthDigimonId });
    showToast("Rebirth realizado com sucesso! Seu Digimon renasceu.");
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "🔄 Renascer!"; }
  }
}
