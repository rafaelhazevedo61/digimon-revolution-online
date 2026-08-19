let adminEventRewardSubmitting = false;
let adminEventRewardSelectedPlayers = [];
let adminEventRewardClanOptions = [];
let adminEventRewardClanMembers = [];
let adminEventRewardPlayerSearchTimer = null;

const ADMIN_EVENT_ITEM_OPTIONS = [
  ["", "Nenhum item"],
  ["POTION_SMALL", "Poção pequena"],
  ["TRAINING_STONE", "Pedra de treinamento"],
  ["DATA_CORE", "Núcleo de dados"],
  ["DIGITAMA_STARTER", "Digitama inicial"],
  ["DIGITAMA_FIRE", "Digitama de fogo"],
  ["DIGITAMA_WATER", "Digitama de água"],
  ["DIGITAMA_NATURE", "Digitama da natureza"],
  ["INCUBATOR_COMMON", "Incubadora comum"],
  ["INCUBATOR_RARE", "Incubadora rara"],
  ["INCUBATOR_EPIC", "Incubadora épica"],
  ["FRAGMENT_ROOKIE", "Fragmento Rookie"],
  ["FRAGMENT_CHAMPION", "Fragmento Champion"],
  ["FRAGMENT_ULTIMATE", "Fragmento Ultimate"],
  ["FRAGMENT_MEGA", "Fragmento Mega"],
  ["EVOLUTION_MATERIAL", "Material de evolução"],
  ["REFINEMENT_STONE", "Pedra de refinamento"]
];

function renderEventRewardsPage() {
  setPageHeader("Premiações de Eventos", "Envie uma recompensa resgatável pelo Correio");
  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="max-w-5xl space-y-6">
      <div class="card">
        <div class="flex items-start justify-between gap-4 mb-2">
          <div>
            <h3 class="text-xl font-bold">Nova premiação</h3>
            <p class="text-sm text-slate-400 mt-1">Escolha um jogador, um clã inteiro ou uma lista de jogadores.</p>
          </div>
          <span class="badge badge-area">EVENT</span>
        </div>
        <div id="admin-event-reward-result" class="hidden mb-4 rounded-lg px-3 py-2 text-sm"></div>
        <form id="admin-event-reward-form" class="space-y-4" onsubmit="adminSubmitEventReward(event)">
          <div class="grid gap-4 md:grid-cols-2">
            <label class="block">
              <span class="text-sm text-slate-300">Destinatários</span>
              <select id="admin-event-reward-recipient-type" class="input w-full mt-1" onchange="adminChangeEventRewardRecipientType()">
                <option value="PLAYER">Um jogador</option>
                <option value="CLAN">Todos os membros de um clã</option>
                <option value="PLAYERS">Lista de jogadores</option>
              </select>
            </label>
            <label class="block">
              <span class="text-sm text-slate-300">Validade</span>
              <select id="admin-event-reward-validity" class="input w-full mt-1" required onchange="adminUpdateEventRewardPreview()">
                <option value="1">1 dia</option>
                <option value="3">3 dias</option>
                <option value="7" selected>7 dias</option>
                <option value="14">14 dias</option>
                <option value="30">30 dias</option>
              </select>
            </label>
          </div>
          <div id="admin-event-reward-recipient-panel"></div>

          <div class="grid gap-4 md:grid-cols-2">
            <label class="block">
              <span class="text-sm text-slate-300">Tipo da origem</span>
              <input id="admin-event-reward-source-type" class="input w-full mt-1" maxlength="64" value="EVENT" required oninput="adminUpdateEventRewardPreview()">
            </label>
            <label class="block">
              <span class="text-sm text-slate-300">Identificador da origem</span>
              <input id="admin-event-reward-source-id" class="input w-full mt-1" maxlength="128" required placeholder="ex.: evento-agosto-2026-001" oninput="adminUpdateEventRewardPreview()">
              <span class="text-xs text-slate-500 mt-1 block">A mesma origem não gera uma segunda premiação para o mesmo jogador.</span>
            </label>
          </div>

          <label class="block">
            <span class="text-sm text-slate-300">Assunto</span>
            <input id="admin-event-reward-subject" class="input w-full mt-1" maxlength="80" required placeholder="Ex.: Premiação do evento" oninput="adminUpdateEventRewardCounters()">
            <span class="text-xs text-slate-500 mt-1 block text-right"><span id="admin-event-reward-subject-count">0</span>/80</span>
          </label>
          <label class="block">
            <span class="text-sm text-slate-300">Mensagem</span>
            <textarea id="admin-event-reward-body" class="input w-full mt-1 min-h-32" maxlength="1000" rows="7" required placeholder="Explique ao jogador a origem da premiação." oninput="adminUpdateEventRewardCounters()"></textarea>
            <span class="text-xs text-slate-500 mt-1 block text-right"><span id="admin-event-reward-body-count">0</span>/1.000</span>
          </label>

          <div class="grid gap-4 md:grid-cols-2">
            <label class="block">
              <span class="text-sm text-slate-300">Quantidade de Bits</span>
              <input id="admin-event-reward-bits" class="input w-full mt-1" type="number" min="0" max="2147483647" value="0" required oninput="adminUpdateEventRewardPreview()">
              <span class="text-xs text-slate-500 mt-1 block">Será entregue ao Digimon ativo de cada destinatário.</span>
            </label>
            <label class="block">
              <span class="text-sm text-slate-300">Item</span>
              <select id="admin-event-reward-item" class="input w-full mt-1" onchange="adminUpdateEventRewardPreview()">
                ${ADMIN_EVENT_ITEM_OPTIONS.map(([value, label]) => `<option value="${value}">${label}</option>`).join("")}
              </select>
            </label>
          </div>
          <label class="block max-w-md">
            <span class="text-sm text-slate-300">Quantidade do item</span>
            <input id="admin-event-reward-item-quantity" class="input w-full mt-1" type="number" min="0" max="2147483647" value="0" required oninput="adminUpdateEventRewardPreview()">
          </label>

          <div class="rounded-lg border border-amber-900/70 bg-amber-950/20 p-3 text-sm text-amber-200">
            <p class="font-semibold">Como o resgate funciona</p>
            <p class="text-xs text-amber-100/70 mt-1">Cada destinatário recebe sua própria mensagem e precisa ter um Digimon ativo. A mesma origem não pode entregar o prêmio duas vezes ao mesmo jogador.</p>
          </div>
          <div class="flex justify-end">
            <button id="admin-event-reward-submit" class="btn-primary" type="submit">Criar premiação</button>
          </div>
        </form>
      </div>

      <div class="card">
        <h3 class="text-lg font-bold mb-2">Prévia para o jogador</h3>
        <div class="rounded-lg border border-slate-700 bg-slate-950 p-4">
          <p class="text-xs uppercase tracking-wider text-amber-300">Premiação de evento</p>
          <p id="admin-event-reward-preview-recipient" class="text-xs text-slate-400 mt-2">Destinatários não selecionados</p>
          <p id="admin-event-reward-preview-subject" class="font-bold mt-2">Assunto da premiação</p>
          <p id="admin-event-reward-preview-body" class="text-sm text-slate-300 mt-2 whitespace-pre-wrap">O texto da premiação aparecerá aqui.</p>
          <div class="flex flex-wrap gap-2 mt-4 text-xs">
            <span id="admin-event-reward-preview-bits" class="badge">0 Bits</span>
            <span id="admin-event-reward-preview-item" class="badge">Sem item</span>
            <span id="admin-event-reward-preview-validity" class="badge">Válido por 7 dias</span>
          </div>
        </div>
      </div>
    </div>
  `;
  adminChangeEventRewardRecipientType();
  adminUpdateEventRewardCounters();
  adminUpdateEventRewardPreview();
}

function adminEventRewardValue(id) {
  return document.getElementById(id)?.value?.trim() || "";
}

function adminEventRewardEscape(value) {
  return String(value || "").replace(/[&<>"']/g, character => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
  }[character]));
}

function adminChangeEventRewardRecipientType() {
  const type = document.getElementById("admin-event-reward-recipient-type")?.value || "PLAYER";
  const panel = document.getElementById("admin-event-reward-recipient-panel");
  if (!panel) return;

  if (type === "CLAN") {
    panel.innerHTML = `
      <label class="block">
        <span class="text-sm text-slate-300">Clã destinatário</span>
        <select id="admin-event-reward-clan" class="input w-full mt-1" onchange="adminLoadEventRewardClanMembers()">
          <option value="">Carregando clãs...</option>
        </select>
        <span id="admin-event-reward-clan-members" class="text-xs text-slate-500 mt-1 block">Todos os membros receberão uma mensagem individual.</span>
      </label>
    `;
    adminLoadEventRewardClans();
  } else if (type === "PLAYERS") {
    panel.innerHTML = `
      <div>
        <label class="block">
          <span class="text-sm text-slate-300">Adicionar jogadores</span>
          <input id="admin-event-reward-player-search" class="input w-full mt-1" maxlength="30" placeholder="Digite parte do nome para pesquisar" oninput="adminSearchEventRewardPlayers()">
        </label>
        <div id="admin-event-reward-player-results" class="mt-2 space-y-1"></div>
        <div class="mt-3">
          <p class="text-xs text-slate-500 mb-2">Selecionados: <strong id="admin-event-reward-selected-count">0</strong>/100</p>
          <div id="admin-event-reward-selected-players" class="flex flex-wrap gap-2"></div>
        </div>
      </div>
    `;
    adminRenderSelectedEventRewardPlayers();
  } else {
    panel.innerHTML = `
      <label class="block">
        <span class="text-sm text-slate-300">Jogador destinatário</span>
        <input id="admin-event-reward-player" class="input w-full mt-1" maxlength="30" required placeholder="Nome exato do jogador" oninput="adminUpdateEventRewardPreview()">
        <span class="text-xs text-slate-500 mt-1 block">A busca não diferencia letras maiúsculas e minúsculas.</span>
      </label>
    `;
  }
  adminUpdateEventRewardPreview();
}

async function adminLoadEventRewardClans() {
  try {
    adminEventRewardClanOptions = await apiGet("/admin/mail/recipients/clans");
    const select = document.getElementById("admin-event-reward-clan");
    if (!select) return;
    select.innerHTML = `<option value="">Selecione um clã</option>${adminEventRewardClanOptions.map(clan => `<option value="${clan.id}">[${adminEventRewardEscape(clan.tag)}] ${adminEventRewardEscape(clan.name)} (${clan.memberCount} membros)</option>`).join("")}`;
    adminUpdateEventRewardPreview();
  } catch (error) {
    adminShowEventRewardResult(error.message || "Não foi possível carregar os clãs.", false);
  }
}

async function adminLoadEventRewardClanMembers() {
  const clanId = adminEventRewardValue("admin-event-reward-clan");
  const status = document.getElementById("admin-event-reward-clan-members");
  if (!clanId) {
    adminEventRewardClanMembers = [];
    if (status) status.textContent = "Todos os membros receberão uma mensagem individual.";
    adminUpdateEventRewardPreview();
    return;
  }
  try {
    adminEventRewardClanMembers = await apiGet(`/admin/mail/recipients/clans/${clanId}/members`);
    if (status) status.textContent = `${adminEventRewardClanMembers.length} membro(s) receberão uma mensagem individual.`;
    adminUpdateEventRewardPreview();
  } catch (error) {
    adminEventRewardClanMembers = [];
    if (status) status.textContent = error.message || "Não foi possível carregar os membros.";
  }
}

function adminSearchEventRewardPlayers() {
  clearTimeout(adminEventRewardPlayerSearchTimer);
  const query = adminEventRewardValue("admin-event-reward-player-search");
  if (!query) {
    const results = document.getElementById("admin-event-reward-player-results");
    if (results) results.innerHTML = "";
    return;
  }
  adminEventRewardPlayerSearchTimer = setTimeout(async () => {
    try {
      const players = await apiGet(`/admin/mail/recipients/players?query=${encodeURIComponent(query)}`);
      const results = document.getElementById("admin-event-reward-player-results");
      if (!results) return;
      results.innerHTML = players.length
        ? players.map(player => `<button type="button" class="card-sm w-full text-left text-sm hover:border-cyan-600" onclick="adminSelectEventRewardPlayer('${player.id}', '${adminEventRewardEscape(player.username)}')">${adminEventRewardEscape(player.username)}</button>`).join("")
        : `<p class="text-xs text-slate-500">Nenhum jogador encontrado.</p>`;
    } catch (error) {
      adminShowEventRewardResult(error.message || "Não foi possível pesquisar jogadores.", false);
    }
  }, 250);
}

function adminSelectEventRewardPlayer(id, username) {
  if (adminEventRewardSelectedPlayers.length >= 100) {
    adminShowEventRewardResult("A lista pode conter no máximo 100 jogadores.", false);
    return;
  }
  if (!adminEventRewardSelectedPlayers.some(player => player.id === id)) {
    adminEventRewardSelectedPlayers.push({ id, username });
  }
  adminRenderSelectedEventRewardPlayers();
  adminUpdateEventRewardPreview();
}

function adminRemoveEventRewardPlayer(id) {
  adminEventRewardSelectedPlayers = adminEventRewardSelectedPlayers.filter(player => player.id !== id);
  adminRenderSelectedEventRewardPlayers();
  adminUpdateEventRewardPreview();
}

function adminRenderSelectedEventRewardPlayers() {
  const count = document.getElementById("admin-event-reward-selected-count");
  const selected = document.getElementById("admin-event-reward-selected-players");
  if (count) count.textContent = adminEventRewardSelectedPlayers.length;
  if (selected) {
    selected.innerHTML = adminEventRewardSelectedPlayers.length
      ? adminEventRewardSelectedPlayers.map(player => `<span class="badge badge-area">${adminEventRewardEscape(player.username)} <button type="button" class="ml-1 text-amber-200" aria-label="Remover ${adminEventRewardEscape(player.username)}" onclick="adminRemoveEventRewardPlayer('${player.id}')">×</button></span>`).join("")
      : `<span class="text-xs text-slate-500">Nenhum jogador selecionado.</span>`;
  }
}

function adminGetEventRewardRecipientLabel() {
  const type = document.getElementById("admin-event-reward-recipient-type")?.value || "PLAYER";
  if (type === "PLAYER") {
    const username = adminEventRewardValue("admin-event-reward-player");
    return username ? `1 jogador: ${username}` : "Nenhum jogador selecionado";
  }
  if (type === "CLAN") {
    const clan = adminEventRewardClanOptions.find(option => String(option.id) === adminEventRewardValue("admin-event-reward-clan"));
    return clan ? `Clã [${clan.tag}] ${clan.name}: ${clan.memberCount} membros` : "Nenhum clã selecionado";
  }
  return adminEventRewardSelectedPlayers.length
    ? `${adminEventRewardSelectedPlayers.length} jogador(es) selecionado(s)`
    : "Nenhum jogador selecionado";
}

function adminGetEventRewardRecipientCount() {
  const type = document.getElementById("admin-event-reward-recipient-type")?.value || "PLAYER";
  if (type === "PLAYER") return adminEventRewardValue("admin-event-reward-player") ? 1 : 0;
  if (type === "CLAN") return adminEventRewardClanMembers.length;
  return adminEventRewardSelectedPlayers.length;
}

function adminUpdateEventRewardCounters() {
  const subject = adminEventRewardValue("admin-event-reward-subject");
  const body = adminEventRewardValue("admin-event-reward-body");
  const subjectCount = document.getElementById("admin-event-reward-subject-count");
  const bodyCount = document.getElementById("admin-event-reward-body-count");
  if (subjectCount) subjectCount.textContent = subject.length;
  if (bodyCount) bodyCount.textContent = body.length;
  adminUpdateEventRewardPreview();
}

function adminUpdateEventRewardPreview() {
  const subject = adminEventRewardValue("admin-event-reward-subject");
  const body = adminEventRewardValue("admin-event-reward-body");
  const bits = Number(document.getElementById("admin-event-reward-bits")?.value || 0);
  const itemSelect = document.getElementById("admin-event-reward-item");
  const itemQuantity = Number(document.getElementById("admin-event-reward-item-quantity")?.value || 0);
  const itemLabel = itemSelect?.selectedOptions?.[0]?.textContent || "Sem item";
  const validity = adminEventRewardValue("admin-event-reward-validity") || "7";
  const previewRecipient = document.getElementById("admin-event-reward-preview-recipient");
  const previewSubject = document.getElementById("admin-event-reward-preview-subject");
  const previewBody = document.getElementById("admin-event-reward-preview-body");
  const previewBits = document.getElementById("admin-event-reward-preview-bits");
  const previewItem = document.getElementById("admin-event-reward-preview-item");
  const previewValidity = document.getElementById("admin-event-reward-preview-validity");
  if (previewRecipient) previewRecipient.textContent = adminGetEventRewardRecipientLabel();
  if (previewSubject) previewSubject.textContent = subject || "Assunto da premiação";
  if (previewBody) previewBody.textContent = body || "O texto da premiação aparecerá aqui.";
  if (previewBits) previewBits.textContent = `${bits.toLocaleString("pt-BR")} Bits por jogador`;
  if (previewItem) previewItem.textContent = itemQuantity > 0 && itemSelect?.value ? `${itemQuantity.toLocaleString("pt-BR")} × ${itemLabel} por jogador` : "Sem item";
  if (previewValidity) previewValidity.textContent = `Válido por ${validity} ${validity === "1" ? "dia" : "dias"}`;
}

function adminShowEventRewardResult(message, success) {
  const result = document.getElementById("admin-event-reward-result");
  if (!result) return;
  result.textContent = message;
  result.className = success
    ? "mb-4 rounded-lg px-3 py-2 text-sm border border-emerald-800 bg-emerald-950/40 text-emerald-300"
    : "mb-4 rounded-lg px-3 py-2 text-sm border border-red-800 bg-red-950/40 text-red-300";
}

function adminConfirmEventReward(payload) {
  return new Promise(resolve => {
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.id = "admin-event-reward-confirm-modal";
    overlay.innerHTML = `
      <div class="modal-content" role="dialog" aria-modal="true" aria-labelledby="admin-event-reward-confirm-title">
        <div class="flex items-start gap-3">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-500/15 text-xl text-amber-300" aria-hidden="true">!</div>
          <div>
            <h3 id="admin-event-reward-confirm-title" class="text-xl font-bold">Confirmar premiação</h3>
            <p class="text-sm text-slate-400 mt-1">Cada destinatário receberá uma mensagem individual.</p>
          </div>
        </div>
        <div class="card mt-5 bg-slate-950/70 space-y-2">
          <p><span class="text-slate-500">Destinatários:</span> <strong id="admin-event-reward-confirm-recipient"></strong></p>
          <p><span class="text-slate-500">Assunto:</span> <strong id="admin-event-reward-confirm-subject"></strong></p>
          <p><span class="text-slate-500">Prêmio por jogador:</span> <strong id="admin-event-reward-confirm-reward"></strong></p>
          <p><span class="text-slate-500">Validade:</span> <strong id="admin-event-reward-confirm-validity"></strong></p>
        </div>
        <div class="mt-4 rounded-lg border border-amber-900/70 bg-amber-950/20 p-3">
          <p class="text-sm font-semibold text-amber-200">Atenção</p>
          <p class="text-xs text-amber-100/70 mt-1">Depois de criada, a premiação ficará disponível e não poderá ser alterada por este formulário.</p>
        </div>
        <div class="grid grid-cols-2 gap-3 mt-6">
          <button id="admin-event-reward-confirm-cancel" class="btn-secondary w-full" type="button">Cancelar</button>
          <button id="admin-event-reward-confirm-submit" class="btn-primary w-full" type="button">Criar premiação</button>
        </div>
      </div>
    `;
    document.body.appendChild(overlay);
    document.getElementById("admin-event-reward-confirm-recipient").textContent = `${payload.recipientLabel} (${payload.recipientCount} mensagem(ns))`;
    document.getElementById("admin-event-reward-confirm-subject").textContent = payload.subject;
    const itemText = payload.itemType && payload.itemQuantity > 0 ? `${payload.itemQuantity.toLocaleString("pt-BR")} × ${payload.itemType}` : "Sem item";
    document.getElementById("admin-event-reward-confirm-reward").textContent = `${payload.bitsAmount.toLocaleString("pt-BR")} Bits · ${itemText}`;
    document.getElementById("admin-event-reward-confirm-validity").textContent = `${payload.validityDays} ${payload.validityDays === 1 ? "dia" : "dias"}`;
    let settled = false;
    const finish = confirmed => {
      if (settled) return;
      settled = true;
      overlay.remove();
      document.removeEventListener("keydown", onKeydown);
      resolve(confirmed);
    };
    const onKeydown = event => { if (event.key === "Escape") finish(false); };
    document.getElementById("admin-event-reward-confirm-cancel").addEventListener("click", () => finish(false));
    document.getElementById("admin-event-reward-confirm-submit").addEventListener("click", () => finish(true));
    overlay.addEventListener("click", event => { if (event.target === overlay) finish(false); });
    document.addEventListener("keydown", onKeydown);
    document.getElementById("admin-event-reward-confirm-cancel").focus();
  });
}

async function adminSubmitEventReward(event) {
  event.preventDefault();
  if (adminEventRewardSubmitting) return;
  const recipientType = document.getElementById("admin-event-reward-recipient-type")?.value || "PLAYER";
  const recipientCount = adminGetEventRewardRecipientCount();
  const payload = {
    recipientType,
    playerUsername: recipientType === "PLAYER" ? adminEventRewardValue("admin-event-reward-player") : null,
    clanId: recipientType === "CLAN" ? adminEventRewardValue("admin-event-reward-clan") : null,
    playerUsernames: recipientType === "PLAYERS" ? adminEventRewardSelectedPlayers.map(player => player.username) : [],
    sourceType: adminEventRewardValue("admin-event-reward-source-type"),
    sourceId: adminEventRewardValue("admin-event-reward-source-id"),
    subject: adminEventRewardValue("admin-event-reward-subject"),
    body: adminEventRewardValue("admin-event-reward-body"),
    bitsAmount: Number(document.getElementById("admin-event-reward-bits")?.value || 0),
    itemType: document.getElementById("admin-event-reward-item")?.value || null,
    itemQuantity: Number(document.getElementById("admin-event-reward-item-quantity")?.value || 0),
    validityDays: Number(document.getElementById("admin-event-reward-validity")?.value || 0),
    recipientLabel: adminGetEventRewardRecipientLabel(),
    recipientCount
  };
  if (!payload.sourceType || !payload.sourceId || !payload.subject || !payload.body) {
    adminShowEventRewardResult("Preencha os campos obrigatórios antes de criar a premiação.", false);
    return;
  }
  if (recipientCount <= 0) {
    adminShowEventRewardResult("Selecione pelo menos um destinatário válido.", false);
    return;
  }
  if (payload.bitsAmount <= 0 && payload.itemQuantity <= 0) {
    adminShowEventRewardResult("Informe Bits ou uma quantidade de item maior que zero.", false);
    return;
  }
  if (payload.itemQuantity > 0 && !payload.itemType) {
    adminShowEventRewardResult("Selecione o item correspondente à quantidade informada.", false);
    return;
  }
  if (payload.itemQuantity === 0) payload.itemType = null;
  if (!await adminConfirmEventReward(payload)) return;

  const button = document.getElementById("admin-event-reward-submit");
  adminEventRewardSubmitting = true;
  if (button) { button.disabled = true; button.textContent = "Criando..."; }
  try {
    const result = await apiPost("/admin/mail/event-rewards", payload);
    const skipped = Number(result.skippedCount || 0);
    const suffix = skipped > 0 ? ` ${skipped} já existia(m) e foi(ram) ignorado(s).` : "";
    adminShowEventRewardResult(`${result.createdCount || 0} premiação(ões) criada(s) para ${payload.recipientLabel}.${suffix}`, true);
    document.getElementById("admin-event-reward-form")?.reset();
    adminEventRewardSelectedPlayers = [];
    adminChangeEventRewardRecipientType();
    adminUpdateEventRewardCounters();
    adminUpdateEventRewardPreview();
  } catch (error) {
    adminShowEventRewardResult(error.message || "Não foi possível criar a premiação.", false);
  } finally {
    adminEventRewardSubmitting = false;
    if (button) { button.disabled = false; button.textContent = "Criar premiação"; }
  }
}
