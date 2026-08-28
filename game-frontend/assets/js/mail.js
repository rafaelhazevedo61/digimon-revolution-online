let mailFolder = "inbox";
let mailPage = 0;
const mailPageSize = 10;

function renderMailPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between gap-3 mb-4 px-1">
        <div>
          <button class="text-sm text-cyan-400 mb-2" onclick="navigateTo('more')">← Voltar</button>
          <h2 class="text-lg font-bold">Correio</h2>
          <p class="text-xs text-slate-400">Mensagens e comunicados do Mundo Digital</p>
        </div>
        <button class="btn-primary text-sm" onclick="mailOpenCompose()">+ Nova mensagem</button>
      </div>

      <div class="card-sm mb-3 flex items-center justify-between gap-3">
        <div>
          <p class="text-xs text-slate-400">Mensagens não lidas</p>
          <p class="text-xl font-bold text-cyan-300" id="mail-unread-count">--</p>
        </div>
        <div class="flex items-center gap-2">
          <button id="mail-mark-all-read" class="btn-sm text-xs" onclick="mailMarkAllRead()">
            Marcar todas como lidas
          </button>
          <span class="text-3xl" aria-hidden="true">✉️</span>
        </div>
      </div>

      <div class="flex gap-2 mb-4" id="mail-folder-tabs">
        <button class="tab-btn active flex-1" data-folder="inbox" onclick="mailSetFolder('inbox')">Entrada</button>
        <button class="tab-btn flex-1" data-folder="sent" onclick="mailSetFolder('sent')">Enviadas</button>
      </div>

      <div id="mail-list">
        <div class="card animate-pulse"><div class="h-40"></div></div>
      </div>
      <div id="mail-pagination" class="flex items-center justify-between gap-3 mt-3"></div>
      <div id="mail-modal-root"></div>
    </div>
  `;

  mailPage = 0;
  mailUpdateMarkAllReadButton();
  mailLoadFolder();
  mailRefreshUnreadCount();
}

function mailUpdateMarkAllReadButton() {
  const button = document.getElementById("mail-mark-all-read");
  if (button) button.classList.toggle("hidden", mailFolder !== "inbox");
}

async function mailMarkAllRead() {
  const button = document.getElementById("mail-mark-all-read");
  if (!button || mailFolder !== "inbox") return;
  button.disabled = true;
  button.textContent = "Marcando...";
  try {
    const result = await apiPost("/mail/read-all", {});
    const markedCount = Number(result.markedCount || 0);
    showToast(
      markedCount > 0
        ? `${markedCount} mensagem(ns) marcada(s) como lida(s).`
        : "Nenhuma mensagem elegível foi encontrada. Recompensas com loot pendente foram preservadas.",
      "success"
    );
    await mailLoadFolder();
    await mailRefreshUnreadCount();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    if (button) {
      button.disabled = false;
      button.textContent = "Marcar todas como lidas";
    }
  }
}

async function mailRefreshUnreadCount() {
  const countEl = document.getElementById("mail-unread-count");
  try {
    const result = await apiGet("/mail/unread-count");
    const count = Number(result.count || 0);
    if (countEl) countEl.textContent = count;
    const shortcutCount = document.getElementById("mail-more-unread");
    if (shortcutCount) {
      shortcutCount.textContent = count > 99 ? "99+" : String(count);
      shortcutCount.classList.toggle("hidden", count === 0);
    }
    return count;
  } catch (err) {
    if (countEl) countEl.textContent = "0";
    return 0;
  }
}

function mailSetFolder(folder) {
  mailFolder = folder === "sent" ? "sent" : "inbox";
  mailPage = 0;
  mailUpdateMarkAllReadButton();
  document.querySelectorAll("#mail-folder-tabs .tab-btn").forEach(button => {
    button.classList.toggle("active", button.dataset.folder === mailFolder);
  });
  mailLoadFolder();
}

async function mailLoadFolder() {
  const list = document.getElementById("mail-list");
  if (!list) return;
  list.innerHTML = `<div class="card animate-pulse"><div class="h-40"></div></div>`;

  try {
    const result = await apiGet(`/mail/${mailFolder}?page=${mailPage}&size=${mailPageSize}`);
    mailRenderList(result);
  } catch (err) {
    list.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
    const pagination = document.getElementById("mail-pagination");
    if (pagination) pagination.innerHTML = "";
  }
}

function mailRenderList(result) {
  const list = document.getElementById("mail-list");
  if (!list) return;
  const messages = result.content || [];

  if (messages.length === 0) {
    list.innerHTML = `
      <div class="card text-center py-8">
        <p class="text-3xl mb-2" aria-hidden="true">📭</p>
        <p class="text-slate-300">${mailFolder === "inbox" ? "Sua caixa de entrada está vazia." : "Você ainda não enviou mensagens."}</p>
        <p class="text-xs text-slate-500 mt-1">As mensagens aparecerão aqui quando houver novidades.</p>
      </div>
    `;
  } else {
    list.innerHTML = messages.map(mailRenderSummary).join("");
  }

  mailRenderPagination(result);
}

function mailRenderSummary(message) {
  const otherPlayer = mailFolder === "inbox" ? message.senderUsername : message.recipientUsername;
  const unreadClass = mailFolder === "inbox" && !message.read ? "border-cyan-600 bg-cyan-950/20" : "";
  const unreadLabel = mailFolder === "inbox" && !message.read
    ? `<span class="badge text-cyan-300">Nova</span>`
    : "";
  const actionLabel = message.actionType === "CLAN_INVITE" || message.actionType === "EVENT_REWARD_CLAIM"
    ? `<span class="badge text-amber-300">Ação pendente</span>`
    : "";

  return `
    <button class="card-sm w-full text-left mb-2 ${unreadClass}" onclick="mailOpen('${message.id}')">
      <div class="flex items-start gap-3">
        <span class="text-2xl mt-1" aria-hidden="true">${message.read ? "✉️" : "📨"}</span>
        <div class="min-w-0 flex-1">
          <div class="flex items-start justify-between gap-2">
            <p class="font-bold text-sm truncate">${escapeHtml(message.subject)}</p>
            <div class="flex gap-1 flex-wrap justify-end">${unreadLabel}${actionLabel}</div>
          </div>
          <p class="text-xs text-slate-400 mt-1">${mailFolder === "inbox" ? "De" : "Para"}: ${escapeHtml(otherPlayer || "Sistema")}</p>
          <p class="text-xs text-slate-500 mt-1">${mailFormatDate(message.createdAt)}</p>
        </div>
      </div>
    </button>
  `;
}

function mailRenderPagination(result) {
  const pagination = document.getElementById("mail-pagination");
  if (!pagination) return;
  const totalPages = Number(result.totalPages || 0);
  if (totalPages <= 1) {
    pagination.innerHTML = "";
    return;
  }

  pagination.innerHTML = `
    <button class="btn-sm ${mailPage <= 0 ? "opacity-40 pointer-events-none" : ""}" onclick="mailChangePage(-1)">Anterior</button>
    <span class="text-xs text-slate-400">Página ${mailPage + 1} de ${totalPages}</span>
    <button class="btn-sm ${mailPage >= totalPages - 1 ? "opacity-40 pointer-events-none" : ""}" onclick="mailChangePage(1)">Próxima</button>
  `;
}

function mailChangePage(delta) {
  mailPage = Math.max(0, mailPage + delta);
  mailLoadFolder();
}

async function mailOpen(messageId) {
  try {
    let message = await apiGet(`/mail/${messageId}`);
    if (mailFolder === "inbox" && !message.read) {
      message = await apiPost(`/mail/${messageId}/read`, {});
      mailRefreshUnreadCount();
    }
    mailShowMessageModal(message);
    mailLoadFolder();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function mailShowMessageModal(message) {
  const root = document.getElementById("mail-modal-root");
  if (!root) return;
  const actionMarkup = message.actionType === "CLAN_INVITE" ? `
    <div class="card-sm mt-4 border-cyan-800 bg-cyan-950/20">
      <p class="text-xs text-slate-400 mb-2">Este convite ainda precisa de uma decisão.</p>
      <div class="grid grid-cols-2 gap-2">
        <button class="btn-secondary w-full" onclick="mailProcessAction('${message.id}', 'DECLINE')">Recusar</button>
        <button class="btn-primary w-full" onclick="mailProcessAction('${message.id}', 'ACCEPT')">Aceitar</button>
      </div>
    </div>
  ` : message.actionType === "EVENT_REWARD_CLAIM" ? `
    <div class="card-sm mt-4 border-amber-800 bg-amber-950/20">
      <p class="text-xs text-slate-400 mb-2">Esta premiação está disponível para resgate.</p>
      <button class="btn-primary w-full" onclick="mailProcessAction('${message.id}', 'CLAIM')">Resgatar prêmio</button>
    </div>
  ` : "";
  root.innerHTML = `
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70" id="mail-message-modal">
      <div class="card w-full max-w-lg max-h-[85vh] overflow-y-auto" role="dialog" aria-modal="true" aria-labelledby="mail-message-title">
        <div class="flex items-start justify-between gap-3 mb-4">
          <div>
            <p class="text-xs uppercase tracking-wider text-cyan-300">Correio</p>
            <h3 class="text-lg font-bold" id="mail-message-title">${escapeHtml(message.subject)}</h3>
          </div>
          <button class="text-slate-400 text-xl" aria-label="Fechar" onclick="mailCloseModal()">×</button>
        </div>
        <div class="grid grid-cols-2 gap-2 text-xs text-slate-400 mb-4">
          <p>De: <strong class="text-slate-200">${escapeHtml(message.senderUsername || "Sistema")}</strong></p>
          <p>Para: <strong class="text-slate-200">${escapeHtml(message.recipientUsername || "Você")}</strong></p>
          <p class="col-span-2">${mailFormatDate(message.createdAt)}</p>
        </div>
        <div class="rounded-lg bg-slate-900/70 border border-slate-700 p-4 text-sm text-slate-200 whitespace-pre-wrap break-words">${escapeHtml(message.body)}</div>
        <p class="text-xs text-slate-500 mt-3">O MVP ainda não possui respostas diretas. Para escrever novamente, use “Nova mensagem”.</p>
        ${actionMarkup}
        <div class="grid grid-cols-2 gap-2 mt-4">
          <button class="btn-sm w-full" onclick="mailCloseModal()">Fechar</button>
          <button class="btn-sm w-full text-red-300" onclick="mailAskDelete('${message.id}')">Excluir</button>
        </div>
      </div>
    </div>
  `;
  document.getElementById("mail-message-modal").addEventListener("click", event => {
    if (event.target.id === "mail-message-modal") mailCloseModal();
  });
}

function mailOpenCompose() {
  const root = document.getElementById("mail-modal-root");
  if (!root) return;
  root.innerHTML = `
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70" id="mail-compose-modal">
      <div class="card w-full max-w-lg" role="dialog" aria-modal="true" aria-labelledby="mail-compose-title">
        <div class="flex items-start justify-between gap-3 mb-4">
          <div>
            <p class="text-xs uppercase tracking-wider text-cyan-300">Correio</p>
            <h3 class="text-lg font-bold" id="mail-compose-title">Nova mensagem</h3>
          </div>
          <button class="text-slate-400 text-xl" aria-label="Fechar" onclick="mailCloseModal()">×</button>
        </div>
        <form onsubmit="mailSubmitCompose(event)" class="space-y-3">
          <label class="block text-sm">
            <span class="text-slate-300">Destinatário</span>
            <input id="mail-recipient" class="w-full mt-1 px-3 py-2 rounded-lg text-sm text-white" style="background:#1e293b;border:1px solid #334155" maxlength="30" required placeholder="Nome do jogador" oninput="mailClearRecipientError()">
            <p id="mail-recipient-error" class="hidden text-xs text-red-300 mt-1" role="alert"></p>
          </label>
          <label class="block text-sm">
            <span class="text-slate-300">Assunto</span>
            <input id="mail-subject" class="w-full mt-1 px-3 py-2 rounded-lg text-sm text-white" style="background:#1e293b;border:1px solid #334155" maxlength="80" required placeholder="Assunto da mensagem">
          </label>
          <label class="block text-sm">
            <span class="text-slate-300">Mensagem</span>
            <textarea id="mail-body" class="w-full mt-1 px-3 py-2 rounded-lg text-sm text-white min-h-32" style="background:#1e293b;border:1px solid #334155" maxlength="1000" required placeholder="Escreva sua mensagem"></textarea>
          </label>
          <div class="grid grid-cols-2 gap-2 pt-2">
            <button type="button" class="btn-sm w-full" onclick="mailCloseModal()">Cancelar</button>
            <button type="submit" class="btn-primary w-full" id="mail-send-button">Enviar</button>
          </div>
        </form>
      </div>
    </div>
  `;
  document.getElementById("mail-compose-modal").addEventListener("click", event => {
    if (event.target.id === "mail-compose-modal") mailCloseModal();
  });
}

async function mailSubmitCompose(event) {
  event.preventDefault();
  const button = document.getElementById("mail-send-button");
  const payload = {
    recipientUsername: document.getElementById("mail-recipient").value,
    subject: document.getElementById("mail-subject").value,
    body: document.getElementById("mail-body").value
  };
  if (button) {
    button.disabled = true;
    button.textContent = "Enviando...";
  }

  try {
    await apiPost("/mail", payload);
    mailCloseModal();
    showToast("Mensagem enviada!");
    if (mailFolder === "sent") mailLoadFolder();
  } catch (err) {
    const message = mailTranslateError(err);
    const recipientInput = document.getElementById("mail-recipient");
    if (recipientInput && message.code === "recipient-not-found") {
      mailShowRecipientError(message.text);
      recipientInput.focus();
    }
    showToast(message.text, "error");
    if (button) {
      button.disabled = false;
      button.textContent = "Enviar";
    }
  }
}

async function mailProcessAction(messageId, action) {
  try {
    const result = await apiPost(`/mail/${messageId}/action`, { action });
    mailCloseModal();
    showToast(result.message || "Ação processada.", result.completed ? "success" : "error");
    await mailLoadFolder();
    await mailRefreshUnreadCount();
    if (result.completed && action === "CLAIM") {
      await mailOpen(messageId);
    }
  } catch (err) {
    showToast(err.message, "error");
  }
}

function mailAskDelete(messageId) {
  const root = document.getElementById("mail-modal-root");
  if (!root) return;
  root.insertAdjacentHTML("beforeend", `
    <div class="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/70" id="mail-delete-modal">
      <div class="card w-full max-w-sm" role="dialog" aria-modal="true" aria-labelledby="mail-delete-title">
        <p class="text-xs uppercase tracking-wider text-amber-300">Confirmação</p>
        <h3 class="text-lg font-bold mt-1" id="mail-delete-title">Excluir mensagem?</h3>
        <p class="text-sm text-slate-400 mt-2">A mensagem será removida da sua caixa. Essa ação não pode ser desfeita.</p>
        <div class="grid grid-cols-2 gap-2 mt-4">
          <button class="btn-sm w-full" onclick="mailCloseDeleteModal()">Cancelar</button>
          <button class="btn-sm w-full text-red-300" onclick="mailDelete('${messageId}')">Excluir</button>
        </div>
      </div>
    </div>
  `);
}

function mailCloseDeleteModal() {
  const modal = document.getElementById("mail-delete-modal");
  if (modal) modal.remove();
}

async function mailDelete(messageId) {
  try {
    await apiDelete(`/mail/${messageId}`);
    mailCloseModal();
    showToast("Mensagem excluída.");
    mailLoadFolder();
    mailRefreshUnreadCount();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function mailShowRecipientError(message) {
  const recipientInput = document.getElementById("mail-recipient");
  const errorEl = document.getElementById("mail-recipient-error");
  if (recipientInput) recipientInput.style.borderColor = "#ef4444";
  if (errorEl) {
    errorEl.textContent = message;
    errorEl.classList.remove("hidden");
  }
}

function mailClearRecipientError() {
  const recipientInput = document.getElementById("mail-recipient");
  const errorEl = document.getElementById("mail-recipient-error");
  if (recipientInput) recipientInput.style.borderColor = "#334155";
  if (errorEl) {
    errorEl.textContent = "";
    errorEl.classList.add("hidden");
  }
}

function mailTranslateError(error) {
  const rawMessage = String(error?.message || "");
  if (/recipient player not found|jogador destinatário não encontrado/i.test(rawMessage)) {
    return {
      code: "recipient-not-found",
      text: "Não encontramos esse jogador. Confira o nome exatamente como aparece no jogo e tente novamente."
    };
  }
  if (/you cannot send a message to yourself|você não pode enviar uma mensagem para si mesmo/i.test(rawMessage)) {
    return {
      code: "self-recipient",
      text: "Você não pode enviar uma mensagem para si mesmo."
    };
  }
  return { code: "generic", text: rawMessage || "Não foi possível enviar a mensagem." };
}

function mailCloseModal() {
  const root = document.getElementById("mail-modal-root");
  if (root) root.innerHTML = "";
}

function mailFormatDate(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
}
