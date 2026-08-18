let evoData = null;
let evoDigimonId = null;

async function renderEvolutionPage() {
  const app = document.getElementById("app");
  showBottomNav("digimons");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center gap-2 mb-4 px-1">
        <button class="btn-sm" style="background:#334155;color:#94a3b8" onclick="navigateTo('digimon-select')">← Voltar</button>
        <h2 class="text-lg font-bold">Evolução</h2>
      </div>
      <div id="evo-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  try {
    const dashboard = await apiGet("/players/me/dashboard");
    const d = dashboard.activeDigimon;
    if (!d) {
      document.getElementById("evo-content").innerHTML = `
        <div class="card"><p class="text-slate-400 text-center">Nenhum Digimon ativo.</p></div>
      `;
      return;
    }

    evoDigimonId = d.id;
    evoData = await apiGet(`/digimon/${d.id}/evolution-options`);
    evoRender(d);
  } catch (err) {
    document.getElementById("evo-content").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function evoRender(digimon) {
  const content = document.getElementById("evo-content");
  const options = evoData.options || [];

  // Current digimon summary
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
            <span class="badge badge-${digimon.stage.toLowerCase()}">${evoFormatStage(digimon.stage)}</span>
            <span class="badge badge-${digimon.rarity.toLowerCase()}">${escapeHtml(formatRarity(digimon.rarity))}</span>
            ${evoData.currentAttribute ? `<span class="badge badge-common">${escapeHtml(formatAttribute(evoData.currentAttribute))}</span>` : ""}
            ${evoData.currentElement ? `<span class="badge badge-common">${escapeHtml(formatElement(evoData.currentElement))}</span>` : ""}
          </div>
        </div>
      </div>
    </div>
  `;

  if (options.length === 0) {
    html += `
      <div class="card text-center">
        <p class="text-2xl mb-2">🏆</p>
        <p class="text-slate-400">Este Digimon já atingiu o estágio máximo!</p>
        <p class="text-xs text-slate-500 mt-1">Considere fazer Rebirth para continuar evoluindo.</p>
        <button class="btn-primary mt-3" style="background:#854d0e;color:#fbbf24" onclick="navigateTo('rebirth')">🔄 Ir para Rebirth</button>
      </div>
    `;
  } else {
    html += `<h3 class="text-sm font-bold text-slate-300 mb-3 px-1">Opções de Evolução</h3>`;
    html += options.map(opt => evoRenderOption(opt, digimon)).join("");
  }

  content.innerHTML = html;
}

function evoRenderOption(opt, digimon) {
  const next = opt.nextStep;
  const req = opt.requirements;
  const canEvolve = opt.canEvolve;

  const stageBadge = next.stage ? `badge-${next.stage.toLowerCase()}` : "badge-common";

  // Requirements check
  const levelMet = digimon.level >= req.level;
  const materials = req.materials || [];
  const allMaterialsMet = materials.every(m => m.playerHas >= m.quantity);

  // Stats comparison
  const statsHtml = `
    <div class="grid grid-cols-3 gap-2 text-center text-xs mt-2">
      <div>
        <p class="text-slate-500">HP</p>
        <p class="font-bold text-red-400">${next.baseHp}</p>
      </div>
      <div>
        <p class="text-slate-500">ATK</p>
        <p class="font-bold text-orange-400">${next.baseAtk}</p>
      </div>
      <div>
        <p class="text-slate-500">DEF</p>
        <p class="font-bold text-blue-400">${next.baseDef}</p>
      </div>
    </div>
  `;

  // Requirements list
  let reqHtml = `
    <div class="mt-3 pt-3" style="border-top:1px solid #1e293b">
      <p class="text-xs font-bold text-slate-400 mb-2">Requisitos:</p>
      <div class="flex flex-col gap-1">
        <div class="flex justify-between text-xs">
          <span>Nível ${req.level}</span>
          <span class="${levelMet ? 'text-green-400' : 'text-red-400'}">${levelMet ? '✓' : `Lv.${digimon.level}/${req.level}`}</span>
        </div>
  `;

  materials.forEach(m => {
    const met = m.playerHas >= m.quantity;
    reqHtml += `
      <div class="flex justify-between text-xs">
        <span>${escapeHtml(m.description || m.materialCode)}</span>
        <span class="${met ? 'text-green-400' : 'text-red-400'}">${m.playerHas}/${m.quantity}</span>
      </div>
    `;
  });

  reqHtml += `</div></div>`;

  // Reason text if can't evolve
  const reasonHtml = !canEvolve && opt.reason ? `
    <p class="text-xs text-red-400 mt-2">${escapeHtml(opt.reason)}</p>
  ` : "";

  return `
    <div class="card mb-3 ${canEvolve ? 'border-cyan-800' : ''}">
      <div class="flex items-center gap-3">
        <div class="text-3xl">🐉</div>
        <div class="flex-1 min-w-0">
          <p class="font-bold truncate">${escapeHtml(next.name)}</p>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge ${stageBadge}">${evoFormatStage(next.stage)}</span>
            ${next.attribute ? `<span class="badge badge-common">${escapeHtml(formatAttribute(next.attribute))}</span>` : ""}
            ${next.element ? `<span class="badge badge-common">${escapeHtml(formatElement(next.element))}</span>` : ""}
          </div>
          ${statsHtml}
        </div>
      </div>
      ${reqHtml}
      ${reasonHtml}
      <div class="mt-3">
        <button class="btn-primary w-full ${!canEvolve ? 'opacity-50 cursor-not-allowed' : ''}"
          ${canEvolve ? `onclick="evoEvolve(${opt.evolutionLineId})"` : 'disabled'}
          id="evo-btn-${opt.evolutionLineId}">
          ${canEvolve ? 'Evoluir!' : 'Requisitos não atendidos'}
        </button>
      </div>
    </div>
  `;
}

async function evoEvolve(evolutionLineId) {
  const btn = document.getElementById(`evo-btn-${evolutionLineId}`);
  if (btn) { btn.disabled = true; btn.textContent = "Evoluindo..."; }

  try {
    await apiPost("/digimon/evolve", { evolutionLineId: evolutionLineId });
    showToast("Digimon evoluiu com sucesso!");
    // Reload to show new state
    renderEvolutionPage();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; btn.textContent = "Evoluir!"; }
  }
}

function evoFormatStage(stage) {
  const map = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  return map[stage] || stage;
}
