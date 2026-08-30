let collectionSummary = null;
let collectionDigimons = [];
let collectionInventory = [];

async function renderCollectionPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `<div class="page-container"><div class="flex items-center justify-between mb-4"><div><h2 class="text-lg font-bold">Coleção</h2><p class="text-xs text-slate-400">Registre Digimons usando um Digivice.</p></div><button class="btn-sm" onclick="navigateTo('more')">Voltar</button></div><div class="card grid grid-cols-2 divide-x divide-slate-700 p-0 overflow-hidden"><div class="flex min-h-24 flex-col items-center justify-center px-4 py-4 text-center"><p class="text-3xl font-black leading-none text-cyan-400" id="collection-points">—</p><p class="mt-2 text-xs text-slate-400">Pontos de coleção</p></div><div class="flex min-h-24 flex-col items-center justify-center px-4 py-4 text-center"><p class="text-3xl font-black leading-none text-fuchsia-300" id="collection-digivices">—</p><p class="mt-2 text-xs text-slate-400">Digivices de Registro</p></div></div><div id="collection-milestones" class="card mt-3"></div><div class="card mt-3"><div class="flex items-center justify-between gap-3 mb-2"><h3 class="font-bold">Digimons disponíveis para registro</h3><span id="collection-storage-count" class="text-xs text-slate-400"></span></div><p class="text-xs text-slate-400 mb-3">Somente Digimons que estão no seu Armazém Digimon aparecem nesta lista.</p><div class="flex w-full items-center gap-2 mb-3"><input id="collection-search" class="input flex-1 min-w-0" type="search" placeholder="Buscar por nome" autocomplete="off" onkeydown="if (event.key === 'Enter') collectionApplySearch()" /><button type="button" class="btn-primary shrink-0" onclick="collectionApplySearch()">Buscar</button></div><p class="text-xs text-amber-200 mb-3">O Digivice e o Digimon serão consumidos permanentemente. Duplicatas não geram pontos.</p><div id="collection-digimons"><p class="text-sm text-slate-400">Carregando o Storage...</p></div></div><div id="collection-entries" class="card mt-3"></div></div>`;
  try {
    const [summary, digimons, inventory] = await Promise.all([apiGet("/collection"), apiGet("/digimon/storage"), apiGet("/inventory")]);
    collectionSummary = summary;
    collectionDigimons = digimons || [];
    collectionInventory = inventory || [];
    collectionRenderSummary(summary);
    collectionRenderDigimons();
  } catch (err) {
    document.getElementById("collection-digimons").innerHTML = `<p class="text-sm text-red-300">${escapeHtml(err.message || "Não foi possível carregar a coleção.")}</p>`;
  }
}

function collectionDigiviceCount() {
  return collectionInventory.filter(item => item.itemType === "COLLECTION_DIGIVICE" || item.itemDefinition?.code === "COLLECTION_DIGIVICE").reduce((total, item) => total + Number(item.quantity || 0), 0);
}

function collectionRenderSummary(summary) {
  document.getElementById("collection-points").textContent = summary.points;
  document.getElementById("collection-digivices").textContent = collectionDigiviceCount();
  const milestones = (summary.availableMilestones || []).map(value => `<span class="badge badge-epic mr-1 mb-1 inline-block">${value} pontos alcançados · Disco XP +20%</span>`).join("");
  document.getElementById("collection-milestones").innerHTML = `<h3 class="font-bold mb-2">Marcos alcançados</h3>${milestones || '<p class="text-xs text-slate-400">Continue registrando Digimons para alcançar seu primeiro marco.</p>'}`;
  const entries = (summary.entries || []).map(entry => `<li class="flex items-center justify-between gap-3 rounded-lg border border-slate-800 bg-slate-900/50 px-3 py-2 text-sm"><span><strong>${escapeHtml(entry.speciesName || "Digimon")}</strong><span class="ml-2 badge badge-${String(entry.rarity).toLowerCase()}">${escapeHtml(entry.rarity)}</span></span><span class="text-xs text-slate-500">Registrado</span></li>`).join("");
  document.getElementById("collection-entries").innerHTML = `<div class="flex items-center justify-between mb-2"><h3 class="font-bold">Digimons registrados</h3><span class="text-xs text-slate-400">${(summary.entries || []).length} entrada(s)</span></div><ul class="space-y-2">${entries || '<li class="text-xs text-slate-400">Nenhum Digimon registrado.</li>'}</ul>`;
}

function collectionRenderDigimons() {
  const entries = new Set((collectionSummary?.entries || []).map(e => `${e.digimonInfoId}:${e.rarity}`));
  const search = String(document.getElementById("collection-search")?.value || "").trim().toLowerCase();
  const filtered = collectionDigimons.filter(d => !search || String(d.name || "").toLowerCase().includes(search));
  const target = document.getElementById("collection-digimons");
  if (!target) return;
  const countLabel = search ? `${filtered.length} encontrado(s)` : `${filtered.length} no Storage`;
  const countElement = document.getElementById("collection-storage-count");
  if (countElement) countElement.textContent = countLabel;
  target.innerHTML = filtered.length ? filtered.map(d => { const registered = entries.has(`${d.digimonInfoId}:${d.rarity}`); return `<div class="flex items-center justify-between gap-2 rounded-lg border ${registered ? "border-emerald-900/70 bg-emerald-950/20" : "border-slate-800 bg-slate-900/20"} px-3 py-2 mb-2"><div><p class="font-bold text-sm">${escapeHtml(d.name || "Digimon")}</p><p class="text-xs text-slate-400">${escapeHtml(d.rarity)} · ${escapeHtml(d.stage)}</p></div>${registered ? '<span class="text-xs font-semibold text-emerald-300">✓ Já registrado</span>' : `<button class="btn-sm btn-primary" onclick="collectionOpenRegisterModal('${d.id}')">Registrar</button>`}</div>`; }).join("") : `<div class="rounded-lg border border-slate-800 bg-slate-900/50 p-4 text-center"><p class="text-sm text-slate-400">${search ? "Nenhum Digimon do Storage corresponde à busca." : "Nenhum Digimon encontrado no Storage."}</p>${search ? '<button class="btn-secondary mt-3" type="button" onclick="document.getElementById(\'collection-search\').value=\'\'; collectionApplySearch()">Limpar busca</button>' : ""}</div>`;
}

function collectionApplySearch() {
  collectionRenderDigimons();
}

function collectionOpenRegisterModal(digimonId) {
  const digimon = collectionDigimons.find(item => item.id === digimonId);
  if (!digimon) return;
  document.getElementById("collection-register-overlay")?.remove();
  const overlay = document.createElement("div");
  overlay.id = "collection-register-overlay";
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(2,6,23,.82);z-index:70;display:flex;align-items:center;justify-content:center;padding:1rem;";
  overlay.innerHTML = `<div class="card w-full max-w-md" role="dialog" aria-modal="true"><div class="flex justify-between items-start gap-4 mb-5"><div><p class="text-xs uppercase tracking-wider text-fuchsia-400 font-bold">Coleção</p><h3 class="text-xl font-bold mt-1">Confirmar registro</h3></div><button class="text-slate-400 hover:text-white text-2xl" aria-label="Fechar" onclick="collectionCloseRegisterModal()">&times;</button></div><div class="rounded-lg border border-amber-700/60 bg-amber-950/30 p-4 mb-5"><p class="font-bold">${escapeHtml(digimon.name || "Digimon")} — ${escapeHtml(digimon.rarity)}</p><p class="text-sm text-amber-100 mt-2">Este Digimon será consumido permanentemente. Você gastará 1 Digivice de Registro. Esta ação não pode ser desfeita.</p></div><div class="grid grid-cols-1 gap-2"><button class="btn-primary" onclick="collectionRegister('${digimon.id}')">Confirmar registro</button><button class="btn-secondary" onclick="collectionCloseRegisterModal()">Voltar</button></div></div>`;
  overlay.onclick = event => { if (event.target === overlay) collectionCloseRegisterModal(); };
  document.body.appendChild(overlay);
}

function collectionCloseRegisterModal() {
  document.getElementById("collection-register-overlay")?.remove();
}

async function collectionRegister(digimonId) {
  collectionCloseRegisterModal();
  try {
    const result = await apiPost("/collection/register", { digimonId });
    showToast(result.message || "Digimon registrado na coleção!");
    await renderCollectionPage();
  } catch (err) {
    showToast(err.message || "Não foi possível registrar o Digimon.", "error");
  }
}
