let collectionSummary = null;
let collectionDigimons = [];
let collectionInventory = [];

async function renderCollectionPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `<div class="page-container"><div class="flex items-center justify-between mb-4"><div><h2 class="text-lg font-bold">Coleção</h2><p class="text-xs text-slate-400">Registre Digimons usando um Digivice.</p></div><div class="flex items-center gap-2"><button class="btn-sm btn-primary" onclick="collectionOpenAlbum()">Álbum completo</button><button class="btn-sm" onclick="navigateTo('more')">Voltar</button></div></div><div class="card grid grid-cols-2 divide-x divide-slate-700 p-0 overflow-hidden"><div class="flex min-h-24 flex-col items-center justify-center px-4 py-4 text-center"><p class="text-3xl font-black leading-none text-cyan-400" id="collection-points">—</p><p class="mt-2 text-xs text-slate-400">Pontos de coleção</p></div><div class="flex min-h-24 flex-col items-center justify-center px-4 py-4 text-center"><p class="text-3xl font-black leading-none text-fuchsia-300" id="collection-digivices">—</p><p class="mt-2 text-xs text-slate-400">Digivices de Registro</p></div></div><div class="card mt-3 grid grid-cols-2 divide-x divide-slate-700 p-0 overflow-hidden"><div class="flex flex-col items-center justify-center px-4 py-3 text-center"><p class="text-xl font-black text-cyan-300" id="collection-added">— / —</p><p class="text-xs text-slate-400">Digimons adicionados</p></div><div class="flex flex-col items-center justify-center px-4 py-3 text-center"><p class="text-xl font-black text-amber-300" id="collection-completed">—</p><p class="text-xs text-slate-400">Digimons completos</p><p class="text-[10px] text-slate-500">4 raridades registradas</p></div></div><div id="collection-milestones" class="card mt-3"></div><div class="card mt-3"><div class="flex items-center justify-between gap-3 mb-2"><h3 class="font-bold">Digimons disponíveis para registro</h3><span id="collection-storage-count" class="text-xs text-slate-400"></span></div><p class="text-xs text-slate-400 mb-3">Somente Digimons que estão no seu Armazém Digimon aparecem nesta lista.</p><div class="flex w-full items-center gap-2 mb-3"><input id="collection-search" class="input flex-1 min-w-0" type="search" placeholder="Buscar por nome" autocomplete="off" onkeydown="if (event.key === 'Enter') collectionApplySearch()" /><button type="button" class="btn-primary shrink-0" onclick="collectionApplySearch()">Buscar</button></div><select id="collection-filter" class="input w-full mb-3" onchange="collectionApplySearch()"><option value="ALL">Todos os Digimons do Storage</option><option value="ELIGIBLE">Apenas elegíveis para registro</option><option value="REGISTERED">Já registrados</option><option value="LOCKED">Trancados</option></select><p class="text-xs text-amber-200 mb-3">O Digivice e o Digimon serão consumidos permanentemente. Duplicatas não geram pontos.</p><div id="collection-digimons"><p class="text-sm text-slate-400">Carregando o Storage...</p></div></div><div id="collection-entries" class="card mt-3"></div></div>`;
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
  document.getElementById("collection-added").textContent = `${summary.addedDigimons} / ${summary.totalDigimons}`;
  document.getElementById("collection-completed").textContent = summary.completedDigimons;
  const reached = (summary.milestones || []).filter(milestone => milestone.reached).length;
  document.getElementById("collection-milestones").innerHTML = `<div class="flex items-center justify-between gap-3"><div><h3 class="font-bold">Marcos alcançados</h3><p class="text-xs text-slate-400 mt-1">${reached} de ${(summary.milestones || []).length} marcos alcançados</p></div><button class="btn-sm btn-primary shrink-0" type="button" onclick="collectionOpenMilestones()">Visualizar</button></div>`;
  const entries = (summary.entries || []).slice(0, 3).map(entry => `<li class="flex items-center justify-between gap-3 rounded-lg border border-slate-800 bg-slate-900/50 px-3 py-2 text-sm"><span><strong>${escapeHtml(entry.speciesName || "Digimon")}</strong><span class="ml-2 badge badge-${String(entry.rarity).toLowerCase()}">${escapeHtml(entry.rarity)}</span></span><span class="text-xs text-slate-500">Registrado</span></li>`).join("");
  document.getElementById("collection-entries").innerHTML = `<div class="flex items-center justify-between mb-2"><h3 class="font-bold">Últimos registros</h3><button class="text-xs text-cyan-400 hover:text-cyan-300" type="button" onclick="collectionOpenAlbum()">Ver coleção completa</button></div><ul class="space-y-2">${entries || '<li class="text-xs text-slate-400">Nenhum Digimon registrado.</li>'}</ul>`;
}

function collectionRenderDigimons() {
  const entries = new Set((collectionSummary?.entries || []).map(e => `${e.digimonInfoId}:${e.rarity}`));
  const search = String(document.getElementById("collection-search")?.value || "").trim().toLowerCase();
  const filter = document.getElementById("collection-filter")?.value || "ALL";
  const filtered = collectionDigimons.filter(d => !search || String(d.name || "").toLowerCase().includes(search)).filter(d => { const registered = entries.has(`${d.digimonInfoId}:${d.rarity}`); const locked = Boolean(d.locked); return filter === "ELIGIBLE" ? !locked && !registered : filter === "REGISTERED" ? registered : filter === "LOCKED" ? locked : true; });
  const target = document.getElementById("collection-digimons");
  if (!target) return;
  const countLabel = search || filter !== "ALL" ? `${filtered.length} encontrado(s)` : `${filtered.length} no Storage`;
  const countElement = document.getElementById("collection-storage-count");
  if (countElement) countElement.textContent = countLabel;
  target.innerHTML = filtered.length ? filtered.map(d => { const registered = entries.has(`${d.digimonInfoId}:${d.rarity}`); const locked = Boolean(d.locked); const stateClass = locked ? "border-amber-900/70 bg-amber-950/20" : registered ? "border-emerald-900/70 bg-emerald-950/20" : "border-slate-800 bg-slate-900/20"; const stateLabel = locked ? '<span class="text-xs font-semibold text-amber-300">🔒 Trancado</span>' : registered ? '<span class="text-xs font-semibold text-emerald-300">✓ Já registrado</span>' : `<button class="btn-sm btn-primary" onclick="collectionOpenRegisterModal('${d.id}')">Registrar</button>`; return `<div class="flex items-center justify-between gap-2 rounded-lg border ${stateClass} px-3 py-2 mb-2"><div><p class="font-bold text-sm">${escapeHtml(d.name || "Digimon")}</p><p class="text-xs text-slate-400">${escapeHtml(d.rarity)} · ${escapeHtml(d.stage)}</p></div>${stateLabel}</div>`; }).join("") : `<div class="rounded-lg border border-slate-800 bg-slate-900/50 p-4 text-center"><p class="text-sm text-slate-400">${filter === "ELIGIBLE" ? "Nenhum Digimon elegível para registro." : search ? "Nenhum Digimon do Storage corresponde à busca." : filter === "REGISTERED" ? "Nenhum Digimon já registrado no Storage." : filter === "LOCKED" ? "Nenhum Digimon trancado no Storage." : "Nenhum Digimon encontrado no Storage."}</p>${search ? '<button class="btn-secondary mt-3" type="button" onclick="document.getElementById(\'collection-search\').value=\'\'; collectionApplySearch()">Limpar busca</button>' : ""}</div>`;
}

function collectionApplySearch() {
  collectionRenderDigimons();
}

function collectionOpenMilestones() {
  document.getElementById("collection-milestones-overlay")?.remove();
  const milestones = collectionSummary?.milestones || [];
  const overlay = document.createElement("div");
  overlay.id = "collection-milestones-overlay";
  overlay.className = "fixed inset-0 z-[60] flex items-end justify-center";
  overlay.style.background = "rgba(0,0,0,0.72)";
  overlay.innerHTML = `<div class="w-full max-w-lg rounded-t-2xl p-4 pb-8" style="background:#0f172a;max-height:88vh;overflow-y:auto" onclick="event.stopPropagation()"><div class="flex items-center justify-between gap-3 mb-4"><div><p class="text-xs uppercase tracking-wider text-fuchsia-400 font-bold">Coleção</p><h3 class="font-bold text-lg">Marcos de coleção</h3><p class="text-xs text-slate-400">Alcance os pontos necessários para receber cada recompensa.</p></div><button class="text-slate-400 text-2xl" aria-label="Fechar" onclick="collectionCloseMilestones()">&times;</button></div><div class="flex flex-col gap-2">${milestones.map(milestone => `<div class="flex items-center justify-between gap-3 rounded-lg border ${milestone.reached ? "border-emerald-900/70 bg-emerald-950/20" : "border-slate-700 bg-slate-900/60"} px-3 py-3"><div><p class="font-bold text-sm">${milestone.pointsRequired} pontos</p><p class="text-xs text-slate-400">${escapeHtml(milestone.reward)}</p></div><span class="text-xs font-semibold ${milestone.reached ? "text-emerald-300" : "text-slate-500"}">${milestone.reached ? "✓ Alcançado" : "Bloqueado"}</span></div>`).join("")}</div></div>`;
  overlay.onclick = event => { if (event.target === overlay) collectionCloseMilestones(); };
  document.body.appendChild(overlay);
}

function collectionCloseMilestones() {
  document.getElementById("collection-milestones-overlay")?.remove();
}

async function collectionOpenAlbum() {
  document.getElementById("collection-album-overlay")?.remove();
  const overlay = document.createElement("div");
  overlay.id = "collection-album-overlay";
  overlay.className = "fixed inset-0 z-[60] flex items-end justify-center";
  overlay.style.background = "rgba(0,0,0,0.72)";
  overlay.innerHTML = `<div class="w-full max-w-4xl rounded-t-2xl p-4 pb-8" style="background:#0f172a;max-height:92vh;overflow-y:auto" onclick="event.stopPropagation()"><div class="flex items-center justify-between gap-3 mb-3"><div><p class="text-xs uppercase tracking-wider text-fuchsia-400 font-bold">Coleção</p><h3 class="font-bold text-lg">Álbum completo</h3><p class="text-xs text-slate-400">Cada Digimon possui uma entrada para cada raridade.</p></div><button class="text-slate-400 text-2xl" aria-label="Fechar" onclick="collectionCloseAlbum()">&times;</button></div><div class="flex w-full items-center gap-2 mb-4"><input id="collection-album-search" class="input flex-1 min-w-0" type="search" placeholder="Buscar Digimon por nome" oninput="collectionRenderAlbum()" /><span id="collection-album-count" class="text-xs text-slate-400 whitespace-nowrap"></span></div><div id="collection-album-content"><p class="text-sm text-slate-400 text-center py-8">Carregando catálogo...</p></div></div>`;
  overlay.onclick = event => { if (event.target === overlay) collectionCloseAlbum(); };
  document.body.appendChild(overlay);
  try {
    let page = 0;
    window.collectionAlbumEntries = [];
    let hasNext = true;
    while (hasNext) {
      const result = await apiGet(`/digimon-infos?page=${page}&size=100`);
      window.collectionAlbumEntries.push(...(result.items || []));
      hasNext = Boolean(result.hasNext);
      page++;
    }
    collectionRenderAlbum();
  } catch (err) {
    document.getElementById("collection-album-content").innerHTML = `<p class="text-sm text-red-300 text-center py-8">${escapeHtml(err.message || "Não foi possível carregar o álbum.")}</p>`;
  }
}

function collectionRenderAlbum() {
  const target = document.getElementById("collection-album-content");
  if (!target) return;
  const search = String(document.getElementById("collection-album-search")?.value || "").trim().toLowerCase();
  const entries = new Set((collectionSummary?.entries || []).map(e => `${e.digimonInfoId}:${e.rarity}`));
  const items = (window.collectionAlbumEntries || []).filter(item => !search || String(item.name || "").toLowerCase().includes(search));
  const count = document.getElementById("collection-album-count");
  if (count) count.textContent = `${items.length} Digimon(s)`;
  const rarities = [["COMMON", "Comum", "rarity-box-common"], ["RARE", "Rara", "rarity-box-rare"], ["EPIC", "Épica", "rarity-box-epic"], ["LEGENDARY", "Lendária", "rarity-box-legendary"]];
  target.innerHTML = items.length ? `<div class="grid grid-cols-1 sm:grid-cols-2 gap-3">${items.map(item => { const masteryComplete = rarities.every(([code]) => entries.has(`${item.id}:${code}`)); return `<div class="rounded-xl border ${masteryComplete ? "border-amber-500/70 bg-amber-950/20" : "border-slate-700 bg-slate-900/60"} p-3"><div class="flex items-center gap-2 mb-3"><div class="w-10 h-10 rounded-lg bg-slate-800 flex items-center justify-center">${renderDigimonVisual(item.imageUrl, item.stage, "w-full h-full", "text-2xl")}</div><div class="min-w-0 flex-1"><div class="flex items-center justify-between gap-2"><p class="font-bold truncate">${escapeHtml(item.name)}</p>${masteryComplete ? '<span class="badge badge-legendary whitespace-nowrap">Maestria adquirida</span>' : ""}</div><p class="text-xs text-slate-500">${escapeHtml(item.stage)}</p></div></div><div class="grid grid-cols-4 gap-1.5">${rarities.map(([code, label, rarityClass]) => { const registered = entries.has(`${item.id}:${code}`); return `<div class="${rarityClass} rounded-lg border p-1.5 text-center ${registered ? "opacity-100" : "opacity-50 grayscale"}" title="${registered ? "Registrado" : "Ainda não registrado"}"><div class="aspect-square rounded bg-slate-950/60 flex items-center justify-center overflow-hidden">${renderDigimonVisual(item.imageUrl, item.stage, "w-full h-full", "text-2xl")}</div><p class="text-[9px] font-semibold mt-1 truncate">${label}</p><p class="text-[10px]">${registered ? "✓" : "—"}</p></div>`; }).join("")}</div></div>`; }).join("")}</div>` : '<p class="text-sm text-slate-400 text-center py-8">Nenhum Digimon corresponde à busca.</p>';
}

function collectionCloseAlbum() {
  document.getElementById("collection-album-overlay")?.remove();
  window.collectionAlbumEntries = [];
}

function collectionOpenRegisterModal(digimonId) {
  const digimon = collectionDigimons.find(item => item.id === digimonId);
  if (!digimon || digimon.locked) {
    showToast("Digimons trancados não podem ser registrados na coleção.", "error");
    return;
  }
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
