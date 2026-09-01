let missionTeamContextPromise = null;
let missionTeamEditingId = null;
let missionTeamEditorSelectedIds = [];
let missionTeamEditorCaptainId = null;
let missionTeamPickerDigimons = [];
let missionTeamPickerQuery = "";

async function loadMissionTeamContext(force = false) {
  if (!force && missionTeamContextPromise) return missionTeamContextPromise;
  missionTeamContextPromise = Promise.all([
    apiGet("/mission-teams"),
    apiGet("/digimon/me"),
    apiGet("/missions/active")
  ]).then(([teams, digimons, activeMissions]) => ({
    teams: Array.isArray(teams) ? teams : [],
    digimons: Array.isArray(digimons) ? digimons : [],
    activeMissions: Array.isArray(activeMissions) ? activeMissions : []
  })).catch(error => {
    missionTeamContextPromise = null;
    throw error;
  });
  return missionTeamContextPromise;
}

function missionTeamUsableDigimons(digimons) {
  return digimons.filter(digimon => !["REBORN", "SACRIFICED", "COLLECTION_CONSUMED"].includes(String(digimon.status || "")));
}

function missionTeamDigimonMap(digimons) {
  return new Map(digimons.map(digimon => [String(digimon.id), digimon]));
}

function missionTeamDigimonName(digimon) {
  if (!digimon) return "Digimon indisponível";
  return `${digimon.name || "Digimon"} · Nível ${Number(digimon.level) || 0}`;
}

function missionTeamStatusLabel(digimon) {
  const labels = { ACTIVE: "Ativo", HATCHED: "Disponível", STORED: "Armazenado" };
  return labels[String(digimon && digimon.status || "")] || "Disponível";
}

function renderMissionTeamMember(digimon, captainId) {
  const isCaptain = String(digimon && digimon.id) === String(captainId);
  return `
    <div class="flex items-center gap-2 rounded-lg border ${isCaptain ? "border-amber-700 bg-amber-950/30" : "border-slate-700 bg-slate-900/50"} px-2 py-2">
      ${digimon ? renderDigimonVisual(digimon.imageUrl, digimon.stage, "w-10 h-10", "text-3xl") : `<span class="flex w-10 h-10 items-center justify-center rounded-lg bg-slate-800 text-xl">?</span>`}
      <div class="min-w-0 flex-1">
        <p class="truncate text-xs font-bold text-slate-100">${escapeHtml(digimon ? digimon.name : "Digimon indisponível")}</p>
        <p class="text-[0.62rem] text-slate-400">${digimon ? `Nível ${Number(digimon.level) || 0} · ${escapeHtml(formatStage(digimon.stage))}` : "Remova este membro"}</p>
      </div>
      ${isCaptain ? `<span class="rounded-full border border-amber-700/70 bg-amber-900/30 px-2 py-1 text-[0.55rem] font-bold uppercase tracking-wider text-amber-300">Capitão</span>` : ""}
    </div>
  `;
}

function renderMissionTeamCard(team, digimonById, activeMissionDigimonIds = new Set()) {
  const members = (team.digimonIds || []).map(id => digimonById.get(String(id))).filter(Boolean);
  const unavailable = members.some(digimon => activeMissionDigimonIds.has(String(digimon.id)));
  return `
    <article class="card border-slate-700 bg-slate-900/40">
      <div class="flex items-start justify-between gap-3">
        <div class="min-w-0">
          <p class="text-xs font-bold uppercase tracking-wider text-cyan-400">Formação de missão</p>
          <h3 class="mt-1 truncate text-lg font-bold text-slate-100">${escapeHtml(team.name)}</h3>
          <p class="mt-1 text-xs ${unavailable ? "text-amber-300" : "text-slate-400"}">${unavailable ? "Um membro está em missão" : `${members.length}/3 membros · disponível para envio`}</p>
        </div>
        <span class="rounded-full border ${unavailable ? "border-amber-800 bg-amber-950/30 text-amber-300" : "border-emerald-800 bg-emerald-950/30 text-emerald-300"} px-2 py-1 text-[0.58rem] font-bold uppercase tracking-wider">${unavailable ? "Ocupado" : "Pronto"}</span>
      </div>
      <div class="mt-4 space-y-2">
        ${[0, 1, 2].map(index => renderMissionTeamMember(members[index], team.captainDigimonId)).join("")}
      </div>
      <div class="mt-4 flex gap-2">
        <button type="button" class="btn-secondary flex-1 text-xs" onclick="openMissionTeamEditor('${escapeAttr(team.id)}')">Editar</button>
        <button type="button" class="btn-sm btn-secondary text-xs" onclick="deleteMissionTeam('${escapeAttr(team.id)}')">Excluir</button>
      </div>
    </article>
  `;
}

async function renderMissionTeamsPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `
    <div class="page-container">
      <header class="mb-4 flex items-start justify-between gap-3 border-b border-slate-800 pb-4">
        <div>
          <button class="progression-back-button mb-3" onclick="navigateTo('missions')"><span aria-hidden="true">←</span> Voltar às missões</button>
          <p class="progression-eyebrow progression-eyebrow-cyan">Preparação de campo</p>
          <h2 class="progression-page-title">Meus times</h2>
          <p class="progression-page-subtitle">Monte formações de até três Digimons para enviar juntos às missões.</p>
        </div>
        <button type="button" class="btn-primary shrink-0 text-xs" onclick="openMissionTeamEditor()">Novo time</button>
      </header>
      <section class="mb-4 rounded-xl border border-cyan-800/70 bg-cyan-950/20 p-3">
        <p class="text-sm font-bold text-cyan-200">Como funciona</p>
        <p class="mt-1 text-xs leading-relaxed text-slate-400">Cada missão envia o time inteiro e ocupa um dos três slots paralelos. O Digimon ativo do dashboard é independente dos times.</p>
      </section>
      <div id="mission-teams-list" class="grid grid-cols-1 gap-3 md:grid-cols-2">
        <div class="card h-48 animate-pulse"></div>
        <div class="card h-48 animate-pulse"></div>
      </div>
    </div>
  `;

  try {
    const context = await loadMissionTeamContext();
    const container = document.getElementById("mission-teams-list");
    if (!container) return;
    const digimonById = missionTeamDigimonMap(context.digimons);
    const activeMissionDigimonIds = new Set(context.activeMissions.flatMap(mission => mission.digimonIds || [] ).map(String));
    if (context.teams.length === 0) {
      container.innerHTML = `
        <div class="card border-dashed border-cyan-800 bg-cyan-950/15 text-center md:col-span-2">
          <p class="text-3xl">◈</p>
          <p class="mt-2 font-bold text-slate-100">Você ainda não criou um time</p>
          <p class="mt-1 text-sm text-slate-400">Monte sua primeira formação com até três Digimons para começar a enviar missões.</p>
          <button type="button" class="btn-primary mt-4" onclick="openMissionTeamEditor()">Criar primeiro time</button>
        </div>
      `;
      return;
    }
    container.innerHTML = context.teams.map(team => renderMissionTeamCard(team, digimonById, activeMissionDigimonIds)).join("");
  } catch (error) {
    const container = document.getElementById("mission-teams-list");
    if (container) container.innerHTML = `<div class="card border-red-900 md:col-span-2"><p class="text-red-300">${escapeHtml(error.message)}</p></div>`;
  }
}

function missionTeamPickerFilteredDigimons() {
  const query = missionTeamPickerQuery.trim().toLowerCase();
  const stage = document.getElementById("mission-team-picker-stage")?.value || "";
  return missionTeamPickerDigimons.filter(digimon => {
    const searchable = [digimon.name, digimon.stage, digimon.type, digimon.rarity, digimon.personality]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();
    return (!query || searchable.includes(query)) && (!stage || String(digimon.stage) === stage);
  });
}

function renderMissionTeamPickerList() {
  const container = document.getElementById("mission-team-picker-results");
  const count = document.getElementById("mission-team-picker-count");
  if (!container) return;
  const filtered = missionTeamPickerFilteredDigimons();
  const visible = missionTeamPickerQuery.trim() || document.getElementById("mission-team-picker-stage")?.value
    ? filtered.slice(0, 60)
    : filtered.slice(0, 30);
  if (count) count.textContent = `${filtered.length} encontrado${filtered.length === 1 ? "" : "s"}`;
  if (!visible.length) {
    container.innerHTML = `<div class="rounded-xl border border-dashed border-slate-700 p-5 text-center text-sm text-slate-400">Nenhum Digimon corresponde à busca.</div>`;
    return;
  }
  container.innerHTML = visible.map(digimon => {
    const id = String(digimon.id);
    const selected = missionTeamEditorSelectedIds.includes(id);
    const blocked = !selected && missionTeamEditorSelectedIds.length >= 3;
    return `
      <button type="button" class="flex items-center gap-3 rounded-xl border ${selected ? "border-cyan-500 bg-cyan-950/40" : "border-slate-700 bg-slate-900/60 hover:border-slate-500"} ${blocked ? "cursor-not-allowed opacity-45" : ""} p-2 text-left transition" ${blocked ? "disabled" : ""} onclick="toggleMissionTeamDigimon('${escapeAttr(id)}')">
        ${renderDigimonVisual(digimon.imageUrl, digimon.stage, "h-12 w-12", "text-3xl")}
        <span class="min-w-0 flex-1"><span class="block truncate text-xs font-bold text-slate-100">${escapeHtml(digimon.name || "Digimon")}</span><span class="mt-1 block truncate text-[0.62rem] text-slate-400">Nível ${Number(digimon.level) || 0} · ${escapeHtml(formatStage(digimon.stage))}</span><span class="mt-1 block truncate text-[0.58rem] text-slate-500">${escapeHtml(missionTeamStatusLabel(digimon))}</span></span>
        <span class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full border ${selected ? "border-cyan-400 bg-cyan-500 text-slate-950" : "border-slate-600 text-slate-500"} text-xs font-bold">${selected ? "✓" : "+"}</span>
      </button>
    `;
  }).join("");
  if (filtered.length > visible.length) {
    container.insertAdjacentHTML("beforeend", `<p class="px-2 pt-1 text-center text-[0.65rem] text-slate-500">Mostrando ${visible.length} de ${filtered.length}. Refine a busca para encontrar outros Digimons.</p>`);
  }
}

function renderMissionTeamEditorSelection() {
  const container = document.getElementById("mission-team-selected-list");
  const count = document.getElementById("mission-team-selected-count");
  if (count) count.textContent = `${missionTeamEditorSelectedIds.length}/3 selecionados`;
  if (container) {
    const byId = missionTeamDigimonMap(missionTeamPickerDigimons);
    container.innerHTML = [0, 1, 2].map(index => {
      const digimon = byId.get(String(missionTeamEditorSelectedIds[index]));
      return digimon
        ? `<div class="flex items-center gap-2 rounded-lg border border-cyan-800/70 bg-cyan-950/20 px-2 py-2"><span class="text-xs font-bold text-cyan-400">${index + 1}</span>${renderDigimonVisual(digimon.imageUrl, digimon.stage, "h-9 w-9", "text-2xl")}<span class="min-w-0 flex-1 truncate text-xs font-bold text-slate-100">${escapeHtml(digimon.name)}</span><button type="button" class="text-slate-500 hover:text-red-300" aria-label="Remover ${escapeAttr(digimon.name)}" onclick="toggleMissionTeamDigimon('${escapeAttr(digimon.id)}')">&times;</button></div>`
        : `<div class="flex items-center gap-2 rounded-lg border border-dashed border-slate-700 px-2 py-2 text-xs text-slate-500"><span class="font-bold">${index + 1}</span> Escolha um Digimon</div>`;
    }).join("");
  }
  const captain = document.getElementById("mission-team-captain");
  if (captain) {
    if (!missionTeamEditorSelectedIds.includes(String(missionTeamEditorCaptainId))) {
      missionTeamEditorCaptainId = missionTeamEditorSelectedIds[0] || null;
    }
    const byId = missionTeamDigimonMap(missionTeamPickerDigimons);
    captain.innerHTML = missionTeamEditorSelectedIds.map(id => {
      const digimon = byId.get(String(id));
      return digimon ? `<option value="${escapeAttr(id)}" ${String(id) === String(missionTeamEditorCaptainId) ? "selected" : ""}>${escapeHtml(missionTeamDigimonName(digimon))}</option>` : "";
    }).join("");
    captain.disabled = missionTeamEditorSelectedIds.length === 0;
    captain.onchange = event => { missionTeamEditorCaptainId = event.target.value || null; };
  }
}

function toggleMissionTeamDigimon(digimonId) {
  const id = String(digimonId);
  if (missionTeamEditorSelectedIds.includes(id)) {
    missionTeamEditorSelectedIds = missionTeamEditorSelectedIds.filter(selectedId => selectedId !== id);
  } else if (missionTeamEditorSelectedIds.length < 3) {
    missionTeamEditorSelectedIds = [...missionTeamEditorSelectedIds, id];
  } else {
    showToast("Um time pode ter no máximo três Digimons.", "error");
    return;
  }
  renderMissionTeamEditorSelection();
  renderMissionTeamPickerList();
}

function openMissionTeamPicker() {
  const existing = document.getElementById("mission-team-picker-modal");
  if (existing) existing.remove();
  missionTeamPickerQuery = "";
  const overlay = document.createElement("div");
  overlay.id = "mission-team-picker-modal";
  overlay.className = "fixed inset-0 z-[80] flex items-center justify-center bg-black/75 p-4";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.innerHTML = `
    <div class="card max-h-[92vh] w-full max-w-2xl overflow-y-auto" onclick="event.stopPropagation()">
      <div class="mb-4 flex items-start justify-between gap-3"><div><p class="text-xs font-bold uppercase tracking-wider text-cyan-400">Armazém de Digimons</p><h3 class="mt-1 text-xl font-bold text-slate-100">Selecionar membros</h3><p class="mt-1 text-sm text-slate-400">Busque pelo nome ou refine por estágio. Selecione até três.</p></div><button type="button" class="text-2xl leading-none text-slate-400 hover:text-white" aria-label="Fechar" onclick="document.getElementById('mission-team-picker-modal')?.remove()">&times;</button></div>
      <div class="mb-3 flex flex-col gap-2 sm:flex-row"><input id="mission-team-picker-search" class="min-w-0 flex-1 rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none focus:border-cyan-500" autocomplete="off" placeholder="Buscar por nome, atributo ou raridade..." aria-label="Buscar Digimon" /><select id="mission-team-picker-stage" class="rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none focus:border-cyan-500" aria-label="Filtrar por estágio"><option value="">Todos os estágios</option>${["BABY", "BABY_II", "ROOKIE", "CHAMPION", "ULTIMATE", "MEGA"].map(stage => `<option value="${stage}">${escapeHtml(formatStage(stage))}</option>`).join("")}</select></div>
      <div class="mb-3 flex items-center justify-between gap-2"><span id="mission-team-picker-count" class="text-xs font-bold text-slate-400"></span><span class="rounded-full border border-cyan-800 bg-cyan-950/30 px-2 py-1 text-xs font-bold text-cyan-300">${missionTeamEditorSelectedIds.length}/3 selecionados</span></div>
      <div id="mission-team-picker-results" class="grid max-h-[55vh] grid-cols-1 gap-2 overflow-y-auto pr-1 sm:grid-cols-2"></div>
      <button type="button" class="btn-primary mt-4 w-full" onclick="document.getElementById('mission-team-picker-modal')?.remove()">Concluir seleção</button>
    </div>
  `;
  overlay.addEventListener("click", event => { if (event.target === overlay) overlay.remove(); });
  document.body.appendChild(overlay);
  const search = document.getElementById("mission-team-picker-search");
  search?.addEventListener("input", event => { missionTeamPickerQuery = event.target.value; renderMissionTeamPickerList(); });
  document.getElementById("mission-team-picker-stage")?.addEventListener("change", renderMissionTeamPickerList);
  renderMissionTeamPickerList();
  search?.focus();
}

function openMissionTeamEditor(teamId = null) {
  loadMissionTeamContext().then(context => {
    const team = teamId ? context.teams.find(item => String(item.id) === String(teamId)) : null;
    missionTeamEditingId = team ? team.id : null;
    missionTeamPickerDigimons = missionTeamUsableDigimons(context.digimons);
    missionTeamEditorSelectedIds = team && Array.isArray(team.digimonIds) ? team.digimonIds.map(String) : [];
    missionTeamEditorCaptainId = team ? String(team.captainDigimonId) : null;
    const overlay = document.createElement("div");
    overlay.id = "mission-team-editor-modal";
    overlay.className = "fixed inset-0 z-[70] flex items-center justify-center bg-black/75 p-4";
    overlay.setAttribute("role", "dialog");
    overlay.setAttribute("aria-modal", "true");
    overlay.innerHTML = `
      <div class="card max-h-[90vh] w-full max-w-lg overflow-y-auto" onclick="event.stopPropagation()">
        <div class="mb-5 flex items-start justify-between gap-3"><div><p class="text-xs font-bold uppercase tracking-wider text-cyan-400">Configuração de formação</p><h3 class="mt-1 text-xl font-bold text-slate-100">${team ? "Editar time" : "Novo time"}</h3></div><button type="button" class="text-2xl leading-none text-slate-400 hover:text-white" aria-label="Fechar" onclick="document.getElementById('mission-team-editor-modal')?.remove()">&times;</button></div>
        <label class="mb-4 block text-xs font-bold uppercase tracking-wider text-slate-400">Nome do time<input id="mission-team-name" class="mt-2 w-full rounded-lg border border-slate-700 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none focus:border-cyan-500" maxlength="40" value="${escapeAttr(team ? team.name : "Novo time")}" placeholder="Ex.: Exploradores" /></label>
        <div class="rounded-xl border border-slate-700 bg-slate-950/40 p-3"><div class="flex items-center justify-between gap-2"><div><p class="text-xs font-bold uppercase tracking-wider text-slate-400">Membros do time</p><p id="mission-team-selected-count" class="mt-1 text-xs text-cyan-300"></p></div><button type="button" class="btn-secondary text-xs" onclick="openMissionTeamPicker()">Buscar Digimons</button></div><div id="mission-team-selected-list" class="mt-3 space-y-2"></div></div>
        <label class="mt-4 block text-xs font-bold uppercase tracking-wider text-amber-300">Capitão do time<select id="mission-team-captain" class="mt-2 w-full rounded-lg border border-amber-800 bg-slate-900 px-3 py-2 text-sm text-slate-100 outline-none focus:border-amber-500"></select></label>
        ${missionTeamPickerDigimons.length < 1 ? `<p class="mt-4 rounded-lg border border-amber-800 bg-amber-950/30 px-3 py-2 text-xs text-amber-200">Você precisa ter pelo menos um Digimon disponível para salvar um time.</p>` : `<p class="mt-4 text-xs text-slate-500">Você pode buscar no armazém e adicionar até três membros antes de salvar.</p>`}
        <div class="mt-5 flex gap-2"><button type="button" class="btn-secondary flex-1" onclick="document.getElementById('mission-team-editor-modal')?.remove()">Cancelar</button><button id="mission-team-save-button" type="button" class="btn-primary flex-1" ${missionTeamPickerDigimons.length < 1 ? "disabled" : ""} onclick="saveMissionTeamFromEditor()">Salvar time</button></div>
      </div>
    `;
    overlay.addEventListener("click", event => { if (event.target === overlay) overlay.remove(); });
    document.body.appendChild(overlay);
    renderMissionTeamEditorSelection();
  }).catch(error => showToast(error.message, "error"));
}

async function saveMissionTeamFromEditor() {
  const name = document.getElementById("mission-team-name")?.value?.trim();
  const digimonIds = missionTeamEditorSelectedIds.map(String);
  const captainDigimonId = document.getElementById("mission-team-captain")?.value;
  if (!name || digimonIds.length < 1 || digimonIds.length > 3 || new Set(digimonIds).size !== digimonIds.length || !captainDigimonId) {
    showToast("Selecione de um a três Digimons diferentes e escolha um capitão.", "error");
    return;
  }
  const button = document.getElementById("mission-team-save-button");
  if (button) { button.disabled = true; button.textContent = "Salvando..."; }
  const editing = Boolean(missionTeamEditingId);
  try {
    const body = { name, digimonIds, captainDigimonId };
    if (editing) await apiPut(`/mission-teams/${missionTeamEditingId}`, body);
    else await apiPost("/mission-teams", body);
    document.getElementById("mission-team-editor-modal")?.remove();
    missionTeamContextPromise = null;
    showToast(editing ? "Time atualizado!" : "Time criado!");
    await renderMissionTeamsPage();
  } catch (error) {
    showToast(error.message, "error");
    if (button) { button.disabled = false; button.textContent = "Salvar time"; }
  }
}

function openMissionTeamEditorForCurrent(teamId) {
  openMissionTeamEditor(teamId);
}

async function deleteMissionTeam(teamId) {
  if (!(await showConfirm("Excluir esta formação?", { title: "Excluir time", confirmText: "Excluir", danger: true }))) return;
  try {
    await apiDelete(`/mission-teams/${teamId}`);
    missionTeamContextPromise = null;
    showToast("Time excluído!");
    await renderMissionTeamsPage();
  } catch (error) {
    showToast(error.message, "error");
  }
}

function openMissionTeamSelectionModal(missionId) {
  const mission = window._missionDefinitions && window._missionDefinitions[missionId];
  if (!mission) {
    showToast("Missão não encontrada. Atualize a página e tente novamente.", "error");
    return;
  }
  loadMissionTeamContext().then(context => {
    const activeIds = new Set(context.activeMissions.flatMap(item => item.digimonIds || []).map(String));
    const occupiedSlots = context.activeMissions.length;
    const digimonById = missionTeamDigimonMap(context.digimons);
    const overlay = document.createElement("div");
    overlay.id = "mission-team-selection-modal";
    overlay.className = "fixed inset-0 z-[70] flex items-center justify-center bg-black/75 p-4";
    overlay.setAttribute("role", "dialog");
    overlay.setAttribute("aria-modal", "true");
    const cards = context.teams.map(team => {
      const members = (team.digimonIds || []).map(id => digimonById.get(String(id)));
      const missing = members.some(member => !member);
      const lowLevel = members.some(member => member && (Number(member.level) < Number(mission.requiredLevel) || (typeof stageProgressionRank === "function" && stageProgressionRank(member.stage) < stageProgressionRank(mission.requiredStage))));
      const busy = members.some(member => member && activeIds.has(String(member.id)));
      const valid = !missing && !lowLevel && !busy && occupiedSlots < 3;
      const reason = occupiedSlots >= 3 ? "Todos os slots estão ocupados" : missing ? "A formação possui Digimon indisponível" : busy ? "Um membro já está em missão" : lowLevel ? `Requer nível ${Number(mission.requiredLevel) || 0} e estágio ${escapeHtml(formatStage(mission.requiredStage))}` : "Pronto para envio";
      return `
        <article class="rounded-xl border ${valid ? "border-cyan-700 bg-cyan-950/20" : "border-slate-700 bg-slate-900/50 opacity-75"} p-3">
          <div class="flex items-start justify-between gap-2">
            <div><p class="font-bold text-slate-100">${escapeHtml(team.name)}</p><p class="mt-1 text-xs ${valid ? "text-emerald-300" : "text-amber-300"}">${reason}</p></div>
            <span class="text-[0.65rem] font-bold text-slate-500">${members.filter(Boolean).length}/3</span>
          </div>
          <div class="mt-3 grid grid-cols-3 gap-2">${members.map(member => member ? `<div class="rounded-lg border border-slate-700 bg-slate-900/60 p-2 text-center">${renderDigimonVisual(member.imageUrl, member.stage, "mx-auto h-10 w-10", "text-3xl")}<p class="mt-1 truncate text-[0.6rem] font-bold text-slate-200">${escapeHtml(member.name)}</p><p class="text-[0.55rem] text-slate-500">Nv. ${Number(member.level) || 0}</p></div>` : `<div class="rounded-lg border border-red-900 bg-red-950/20 p-2 text-center text-xs text-red-300">?</div>`).join("")}</div>
          <button type="button" class="btn-primary mt-3 w-full text-xs" ${valid ? "" : "disabled"} onclick="confirmMissionTeam('${escapeAttr(team.id)}', '${escapeAttr(missionId)}')">Enviar este time</button>
        </article>
      `;
    }).join("");
    overlay.innerHTML = `
      <div class="card max-h-[90vh] w-full max-w-2xl overflow-y-auto" onclick="event.stopPropagation()">
        <div class="mb-5 flex items-start justify-between gap-3">
          <div><p class="text-xs font-bold uppercase tracking-wider text-cyan-400">Seleção de expedição</p><h3 class="mt-1 text-xl font-bold text-slate-100">Escolha o time para esta missão</h3><p class="mt-1 text-sm text-slate-400">${escapeHtml(formatMissionName(mission))} · Slot ${Math.min(occupiedSlots + 1, 3)} de 3</p></div>
          <button type="button" class="text-2xl leading-none text-slate-400 hover:text-white" aria-label="Fechar" onclick="document.getElementById('mission-team-selection-modal')?.remove()">&times;</button>
        </div>
        ${context.teams.length ? `<div class="space-y-3">${cards}</div>` : `<div class="rounded-xl border border-dashed border-cyan-800 bg-cyan-950/20 p-5 text-center"><p class="font-bold text-slate-100">Nenhum time disponível</p><p class="mt-1 text-sm text-slate-400">Crie uma formação com até três Digimons antes de iniciar esta missão.</p><button type="button" class="btn-primary mt-4" onclick="document.getElementById('mission-team-selection-modal')?.remove(); navigateTo('mission-teams')">Gerenciar times</button></div>`}
        <button type="button" class="btn-secondary mt-4 w-full" onclick="document.getElementById('mission-team-selection-modal')?.remove()">Cancelar</button>
      </div>
    `;
    overlay.addEventListener("click", event => { if (event.target === overlay) overlay.remove(); });
    document.body.appendChild(overlay);
  }).catch(error => showToast(error.message, "error"));
}

async function confirmMissionTeam(teamId, missionId) {
  const buttons = document.querySelectorAll("#mission-team-selection-modal button");
  buttons.forEach(button => { button.disabled = true; });
  try {
    await apiPost("/missions/start", { missionId, teamId });
    document.getElementById("mission-team-selection-modal")?.remove();
    missionTeamContextPromise = null;
    showToast("Time enviado para a missão!");
    navigateTo("missions");
  } catch (error) {
    showToast(error.message, "error");
    buttons.forEach(button => { button.disabled = false; });
  }
}
