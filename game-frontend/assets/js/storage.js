async function renderStoragePage() {
  const app = document.getElementById("app");
  showBottomNav("digimons");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center gap-2 mb-4 px-1">
        <button class="btn-sm" style="background:#334155;color:#94a3b8" onclick="navigateTo('dashboard')">← Voltar</button>
        <h2 class="text-lg font-bold">Storage</h2>
      </div>
      <div id="storage-info" class="mb-3"></div>
      <div id="storage-list">
        <div class="card animate-pulse"><div class="h-20"></div></div>
      </div>
    </div>
  `;

  try {
    const [stored, dashboard] = await Promise.all([
      apiGet("/digimon/storage"),
      apiGet("/players/me/dashboard")
    ]);

    const slotInfo = dashboard.slotInfo;
    const infoEl = document.getElementById("storage-info");
    infoEl.innerHTML = `
      <div class="card-sm flex justify-between items-center">
        <div>
          <p class="text-xs text-slate-400">Digimons armazenados</p>
          <p class="font-bold text-sm ${slotInfo.storedDigimons >= slotInfo.maxStorageSlots ? 'text-red-400' : 'text-cyan-400'}">${slotInfo.storedDigimons}/${slotInfo.maxStorageSlots}</p>
        </div>
      </div>
    `;

    const container = document.getElementById("storage-list");
    if (!stored || stored.length === 0) {
      container.innerHTML = `<div class="card text-center text-slate-400 text-sm">Storage vazio</div>`;
      return;
    }

    container.innerHTML = stored.map(d => `
        <div class="card mb-2 flex items-center gap-3">
          ${renderDigimonVisual(d.imageUrl, d.stage, "w-16 h-16", "text-4xl")}
          <div class="flex-1 min-w-0">
            <p class="font-bold text-sm truncate">${escapeHtml(d.name)}</p>
            <p class="text-xs text-slate-400">Lv.${d.level} | ${escapeHtml(d.stage)} | ${formatRarity(d.rarity)}</p>
            <p class="text-xs text-slate-500">HP ${d.hp} ATK ${d.attack} DEF ${d.defense}</p>
          </div>
          <div class="flex flex-col gap-1">
            <button class="btn-sm"
              style="background:#065f46;color:#6ee7b7"
              onclick="storageRetrieve('${d.id}')">
              Tornar ativo
            </button>
            <button class="btn-sm"
              style="background:#7f1d1d;color:#fecaca"
              onclick="storageSacrifice('${d.id}', '${escapeHtml(d.name)}')">
              Sacrificar
            </button>
          </div>
        </div>
      `).join("");
  } catch (err) {
    document.getElementById("storage-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

async function storageSacrifice(digimonId, digimonName) {
  if (!window.confirm(`Sacrificar ${digimonName}? Esta ação é permanente e não pode ser desfeita.`)) return;
  try {
    const result = await apiPost(`/digimon/${digimonId}/sacrifice`, {});
    showToast(`Digimon sacrificado. +${result.digitalDataReceived} Dados Digitais.`);
    renderStoragePage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function storageRetrieve(digimonId) {
  try {
    await apiPost(`/digimon/${digimonId}/retrieve`, {});
    showToast("Digimon agora é o parceiro ativo!");
    renderStoragePage();
  } catch (err) {
    showToast(err.message, "error");
  }
}
