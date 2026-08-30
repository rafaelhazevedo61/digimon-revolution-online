let evoData = null;
let evoDigimonId = null;

async function renderEvolutionPage() {
  const app = document.getElementById("app");
  showBottomNav("digimons");

  app.innerHTML = `
    <div class="page-container">
      <header class="progression-page-header mb-4">
        <div>
          <p class="progression-eyebrow progression-eyebrow-cyan">Sistema de progressão</p>
          <h2 class="progression-page-title">Evolução</h2>
          <p class="progression-page-subtitle">Escolha o próximo estágio e prepare seu Digimon para o salto.</p>
        </div>
        <button class="progression-back-button" onclick="navigateTo('dashboard')"><span aria-hidden="true">←</span> Voltar</button>
      </header>
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
    <section class="progression-hero progression-hero-cyan mb-4">
      <div class="progression-hero-topline"><span class="progression-hero-kicker">Digimon selecionado</span><span class="progression-hero-status">Estágio atual</span></div>
      <div class="flex items-center gap-3">
        <div class="progression-hero-visual">${renderDigimonVisual(digimon.imageUrl, digimon.stage, "w-14 h-14", "text-4xl")}</div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <h3 class="font-bold text-lg truncate">${escapeHtml(digimon.name)}</h3>
            <span class="text-sm font-bold text-cyan-400">Lv.${digimon.level}</span>
          </div>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge badge-${digimon.stage.toLowerCase()}">${evoFormatStage(digimon.stage)}</span>
            <span class="badge badge-${digimon.rarity.toLowerCase()}">${escapeHtml(formatRarity(digimon.rarity))}</span>${renderRarityDieIndicator(digimon)}
            ${evoData.currentAttribute ? `<span class="badge badge-common">${escapeHtml(formatAttribute(evoData.currentAttribute))}</span>` : ""}
            ${evoData.currentElement ? `<span class="badge badge-common">${escapeHtml(formatElement(evoData.currentElement))}</span>` : ""}
          </div>
          ${renderRarityDieDetails(digimon)}
        </div>
      </div>
    </section>
  `;

  if (options.length === 0) {
    html += `
      <section class="evolution-empty-state">
        <div class="evolution-empty-icon" aria-hidden="true">🏆</div>
        <p class="evolution-empty-title">Estágio máximo alcançado</p>
        <p class="evolution-empty-copy">Seu Digimon já chegou ao fim desta linha evolutiva. Use Rebirth para abrir um novo ciclo.</p>
        <button class="progression-button progression-button-amber mt-3" onclick="navigateTo('rebirth')">↻ Ir para Rebirth</button>
      </section>
    `;
  } else {
    html += `<section class="evolution-options-section"><div class="dashboard-section-heading"><div><p class="progression-eyebrow progression-eyebrow-cyan">Próximo passo</p><h3 class="progression-panel-title">Opções de evolução</h3></div><span class="evolution-option-count">${options.length} ${options.length === 1 ? "rota" : "rotas"}</span></div><p class="dashboard-section-note">Compare atributos e requisitos antes de confirmar.</p><div class="evolution-options-list">${options.map(opt => evoRenderOption(opt, digimon)).join("")}</div></section>`;
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
    <div class="evolution-stat-grid">
      <div class="evolution-stat evolution-stat-hp"><span>HP</span><strong>${next.baseHp}</strong></div>
      <div class="evolution-stat evolution-stat-atk"><span>ATK</span><strong>${next.baseAtk}</strong></div>
      <div class="evolution-stat evolution-stat-def"><span>DEF</span><strong>${next.baseDef}</strong></div>
    </div>
  `;

  // Requirements list
  let reqHtml = `
    <div class="evolution-requirements">
      <div class="evolution-requirements-heading"><p>Requisitos para evoluir</p><span class="${canEvolve ? "evolution-requirements-ready" : ""}">${canEvolve ? "Pronto" : "Verifique"}</span></div>
      <div class="evolution-requirements-list">
        <div class="evolution-requirement-row"><span>Nível ${req.level}</span><span class="${levelMet ? 'evolution-check' : 'evolution-missing'}">${levelMet ? '✓' : `Lv.${digimon.level}/${req.level}`}</span></div>
  `;

  materials.forEach(m => {
    const met = m.playerHas >= m.quantity;
    reqHtml += `
      <div class="evolution-requirement-row"><span>${escapeHtml(m.description || m.materialCode)}</span><span class="${met ? 'evolution-check' : 'evolution-missing'}">${m.playerHas}/${m.quantity}</span></div>
    `;
  });

  reqHtml += `</div></div>`;

  // Reason text if can't evolve
  const reasonHtml = !canEvolve && opt.reason ? `
    <p class="text-xs text-red-400 mt-2">${escapeHtml(opt.reason)}</p>
  ` : "";

  return `
    <article class="evolution-option-card ${canEvolve ? 'evolution-option-card-ready' : ''}">
      <div class="evolution-option-heading"><div><p class="progression-eyebrow progression-eyebrow-cyan">${canEvolve ? 'Rota disponível' : 'Rota bloqueada'}</p><p class="evolution-option-title">Próximo estágio</p></div><span class="evolution-option-arrow" aria-hidden="true">→</span></div>
      <div class="flex items-center gap-3">
        ${renderDigimonVisual(next.imageUrl, next.stage, "w-12 h-12", "text-3xl")}
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
        <button class="progression-button progression-button-cyan w-full ${!canEvolve ? 'opacity-50 cursor-not-allowed' : ''}"
          ${canEvolve ? `onclick="evoEvolve(${opt.evolutionLineId})"` : 'disabled'}
          id="evo-btn-${opt.evolutionLineId}">
          ${canEvolve ? 'Evoluir!' : 'Requisitos não atendidos'}
        </button>
      </div>
    </article>
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
