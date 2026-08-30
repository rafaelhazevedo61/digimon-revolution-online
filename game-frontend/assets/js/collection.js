let collectionSummary = null;

async function renderCollectionPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `<div class="page-container"><div class="flex items-center justify-between mb-4"><div><h2 class="text-lg font-bold">Coleção</h2><p class="text-xs text-slate-400">Registre Digimons usando um Digivice.</p></div><button class="btn-sm" onclick="navigateTo('more')">Voltar</button></div><div class="card text-center"><p class="text-3xl font-black text-cyan-400" id="collection-points">—</p><p class="text-xs text-slate-400">pontos de coleção</p></div><div id="collection-milestones" class="card mt-3"></div><div class="card mt-3"><h3 class="font-bold mb-2">Digimons disponíveis para registro</h3><p class="text-xs text-amber-200 mb-3">O Digivice e o Digimon serão consumidos permanentemente. Duplicatas não geram pontos.</p><div id="collection-digimons"><p class="text-sm text-slate-400">Carregando...</p></div></div><div id="collection-entries" class="card mt-3"></div></div>`;
  try {
    const [summary, digimons] = await Promise.all([apiGet("/collection"), apiGet("/digimon/storage")]);
    collectionSummary = summary;
    collectionRenderSummary(summary);
    collectionRenderDigimons(digimons || []);
  } catch (err) {
    document.getElementById("collection-digimons").innerHTML = `<p class="text-sm text-red-300">${escapeHtml(err.message || "Não foi possível carregar a coleção.")}</p>`;
  }
}

function collectionRenderSummary(summary) {
  document.getElementById("collection-points").textContent = summary.points;
  const milestones = (summary.availableMilestones || []).map(value => `<span class="badge badge-epic mr-1">${value} pontos alcançados · Disco XP +20%</span>`).join("");
  document.getElementById("collection-milestones").innerHTML = `<h3 class="font-bold mb-2">Marcos alcançados</h3>${milestones || '<p class="text-xs text-slate-400">Continue registrando Digimons para alcançar seu primeiro marco.</p>'}`;
  const entries = (summary.entries || []).slice(0, 20).map(entry => `<li class="flex justify-between text-sm py-1"><span>${escapeHtml(entry.rarity)}</span><span class="text-slate-500">#${entry.digimonInfoId}</span></li>`).join("");
  document.getElementById("collection-entries").innerHTML = `<h3 class="font-bold mb-2">Últimos registros</h3><ul>${entries || '<li class="text-xs text-slate-400">Nenhum Digimon registrado.</li>'}</ul>`;
}

function collectionRenderDigimons(digimons) {
  const entries = new Set((collectionSummary.entries || []).map(e => `${e.digimonInfoId}:${e.rarity}`));
  const eligible = digimons.filter(d => !entries.has(`${d.digimonInfoId}:${d.rarity}`));
  document.getElementById("collection-digimons").innerHTML = eligible.length ? eligible.map(d => `<div class="flex items-center justify-between gap-2 border-b border-slate-800 py-2"><div><p class="font-bold text-sm">${escapeHtml(d.name || "Digimon")}</p><p class="text-xs text-slate-400">${escapeHtml(d.rarity)} · ${escapeHtml(d.stage)}</p></div><button class="btn-sm btn-primary" onclick="collectionRegister('${d.id}', '${escapeHtml(d.name || "Digimon")}')">Registrar</button></div>`).join("") : '<p class="text-sm text-slate-400">Nenhum Digimon elegível encontrado no Storage.</p>';
}

async function collectionRegister(digimonId, name) {
  if (!confirm(`Registrar ${name}? O Digimon e 1 Digivice serão consumidos permanentemente.`)) return;
  try {
    const result = await apiPost("/collection/register", { digimonId });
    showToast(result.message || "Digimon registrado na coleção!");
    await renderCollectionPage();
  } catch (err) {
    showToast(err.message || "Não foi possível registrar o Digimon.");
  }
}
