let collectionSummary = null;
let collectionDigimons = [];
let collectionInventory = [];

async function renderCollectionPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `<div class="page-container"><div class="flex items-center justify-between mb-4"><div><h2 class="text-lg font-bold">Coleção</h2><p class="text-xs text-slate-400">Registre Digimons usando um Digivice.</p></div><button class="btn-sm" onclick="navigateTo('more')">Voltar</button></div><div class="card flex items-center justify-between"><div><p class="text-3xl font-black text-cyan-400" id="collection-points">—</p><p class="text-xs text-slate-400">pontos de coleção</p></div><div class="text-right"><p class="text-2xl font-black text-fuchsia-300" id="collection-digivices">—</p><p class="text-xs text-slate-400">Digivices de Registro</p></div></div><div id="collection-milestones" class="card mt-3"></div><div class="card mt-3"><div class="flex items-center justify-between gap-3 mb-3"><h3 class="font-bold">Digimons disponíveis para registro</h3><input id="collection-search" class="input w-40" type="search" placeholder="Buscar por nome" autocomplete="off" oninput="collectionRenderDigimons()" /></div><p class="text-xs text-amber-200 mb-3">O Digivice e o Digimon serão consumidos permanentemente. Duplicatas não geram pontos.</p><div id="collection-digimons"><p class="text-sm text-slate-400">Carregando...</p></div></div><div id="collection-entries" class="card mt-3"></div></div>`;
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
  const entries = (summary.entries || []).slice(0, 20).map(entry => `<li class="flex justify-between text-sm py-1"><span>${escapeHtml(entry.rarity)}</span><span class="text-slate-500">#${entry.digimonInfoId}</span></li>`).join("");
  document.getElementById("collection-entries").innerHTML = `<h3 class="font-bold mb-2">Últimos registros</h3><ul>${entries || '<li class="text-xs text-slate-400">Nenhum Digimon registrado.</li>'}</ul>`;
}

function collectionRenderDigimons() {
  const entries = new Set((collectionSummary?.entries || []).map(e => `${e.digimonInfoId}:${e.rarity}`));
  const search = String(document.getElementById("collection-search")?.value || "").trim().toLowerCase();
  const eligible = collectionDigimons.filter(d => !entries.has(`${d.digimonInfoId}:${d.rarity}`)).filter(d => !search || String(d.name || "").toLowerCase().includes(search));
  const target = document.getElementById("collection-digimons");
  if (!target) return;
  target.innerHTML = eligible.length ? eligible.map(d => `<div class="flex items-center justify-between gap-2 border-b border-slate-800 py-2"><div><p class="font-bold text-sm">${escapeHtml(d.name || "Digimon")}</p><p class="text-xs text-slate-400">${escapeHtml(d.rarity)} · ${escapeHtml(d.stage)}</p></div><button class="btn-sm btn-primary" onclick="collectionOpenRegisterModal('${d.id}')">Registrar</button></div>`).join("") : '<p class="text-sm text-slate-400">Nenhum Digimon encontrado.</p>';
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
