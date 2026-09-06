renderCollectionPage = async function() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container collection-auto-page">
      <div class="collection-auto-header flex items-center justify-between gap-3 mb-4">
        <div>
          <p class="text-xs uppercase tracking-wider text-cyan-400 font-bold">Descobertas do jogador</p>
          <h2 class="text-lg font-bold mt-1">Coleção</h2>
          <p class="text-xs text-slate-400 mt-1">Digimons são registrados automaticamente ao nascer, evoluir ou renascer.</p>
        </div>
        <div class="flex items-center gap-2 shrink-0">
          <button class="btn-sm btn-primary" onclick="collectionOpenAlbum()">Álbum completo</button>
          <button class="btn-sm" onclick="navigateTo('more')">Voltar</button>
        </div>
      </div>

      <div class="card mb-3 border-cyan-900/60 bg-cyan-950/20 collection-auto-notice">
        <div class="flex items-start gap-3">
          <span class="text-2xl" aria-hidden="true">✦</span>
          <div>
            <p class="font-bold text-sm text-cyan-200">Registro automático ativo</p>
            <p class="text-xs text-slate-400 mt-1">Você não precisa consumir Digimons nem usar Digivices. Cada combinação de espécie e raridade é registrada apenas uma vez.</p>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-3 gap-2 mb-3 collection-auto-stats">
        <div class="card text-center p-3 collection-stat-card">
          <p class="text-2xl font-black text-cyan-400" id="collection-points">—</p>
          <p class="text-[11px] text-slate-400 mt-1">Pontos</p>
        </div>
        <div class="card text-center p-3 collection-stat-card">
          <p class="text-2xl font-black text-fuchsia-300" id="collection-added">— / —</p>
          <p class="text-[11px] text-slate-400 mt-1">Espécies registradas</p>
        </div>
        <div class="card text-center p-3 collection-stat-card">
          <p class="text-2xl font-black text-amber-300" id="collection-completed">—</p>
          <p class="text-[11px] text-slate-400 mt-1">Completos</p>
          <p class="text-[9px] text-slate-500">4 raridades</p>
        </div>
      </div>

      <div class="collection-auto-content-grid">
        <div id="collection-milestones" class="card mb-3 collection-milestones-card"></div>
        <div id="collection-entries" class="card collection-entries-card"></div>
      </div>
    </div>
  `;

  try {
    const [summary, evolutionLines] = await Promise.all([
      apiGet("/collection"),
      apiGet("/evolution-lines/available")
    ]);
    collectionSummary = summary;
    collectionEvolutionLines = evolutionLines || [];
    collectionRenderAutomaticSummary(summary);
  } catch (err) {
    app.querySelector(".collection-auto-page").insertAdjacentHTML(
      "beforeend",
      `<div class="card border-red-900 mt-3"><p class="text-sm text-red-300">${escapeHtml(err.message || "Não foi possível carregar a coleção.")}</p></div>`
    );
  }
};

function collectionRenderAutomaticSummary(summary) {
  document.getElementById("collection-points").textContent = Number(summary.points || 0).toLocaleString("pt-BR");
  document.getElementById("collection-added").textContent = `${summary.addedDigimons || 0} / ${summary.totalDigimons || 0}`;
  document.getElementById("collection-completed").textContent = summary.completedDigimons || 0;

  const milestones = summary.milestones || [];
  const reached = milestones.filter(milestone => milestone.reached).length;
  document.getElementById("collection-milestones").innerHTML = `
    <div class="flex items-center justify-between gap-3 collection-milestones-summary">
      <div>
        <h3 class="font-bold">Marcos de coleção</h3>
        <p class="text-xs text-slate-400 mt-1">${reached} de ${milestones.length} marcos alcançados</p>
      </div>
      <button class="btn-sm btn-primary shrink-0" type="button" onclick="collectionOpenMilestones()">Visualizar</button>
    </div>
  `;

  const entries = (summary.entries || []).slice(0, 6).map(entry => `
    <li class="flex items-center justify-between gap-3 rounded-lg border border-slate-800 bg-slate-900/50 px-3 py-2 text-sm collection-recent-entry">
      <span class="min-w-0">
        <strong class="break-words">${escapeHtml(entry.speciesName || "Digimon")}</strong>
        <span class="ml-2 badge badge-${String(entry.rarity || "COMMON").toLowerCase()}">${escapeHtml(entry.rarity || "COMMON")}</span>
      </span>
      <span class="text-xs text-emerald-300 shrink-0">✓ Registrado</span>
    </li>
  `).join("");

  document.getElementById("collection-entries").innerHTML = `
    <div class="flex items-center justify-between gap-3 mb-3 collection-entries-header">
      <div>
        <h3 class="font-bold">Últimos registros</h3>
        <p class="text-xs text-slate-400 mt-1">Histórico recente de espécies e raridades descobertas.</p>
      </div>
      <button class="text-xs text-cyan-400 hover:text-cyan-300 shrink-0" type="button" onclick="collectionOpenAlbum()">Ver álbum</button>
    </div>
    <ul class="grid grid-cols-1 gap-2 collection-recent-grid">${entries || '<li class="text-xs text-slate-400">Nenhum Digimon registrado.</li>'}</ul>
  `;
}
