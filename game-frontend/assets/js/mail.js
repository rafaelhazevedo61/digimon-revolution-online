let mailFolder = "inbox";
let mailPage = 0;
const mailPageSize = 10;

function renderMailPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container mail-page-container">
      <header class="mail-page-header">
        <div class="mail-header-copy">
          <button class="mail-back-link" onclick="navigateTo('more')"><span aria-hidden="true">←</span> Voltar ao menu</button>
          <p class="mail-eyebrow">Comunicação · Mundo Digital</p>
          <h1 class="mail-page-title">Correio</h1>
          <p class="mail-page-subtitle">Centralize comunicados, convites e mensagens importantes da sua jornada.</p>
        </div>
        <div class="mail-header-actions">
          <button class="mail-compose-button" type="button" onclick="mailOpenCompose()"><span aria-hidden="true">＋</span> Nova mensagem</button>
        </div>
      </header>

      <div class="mail-layout">
        <aside class="mail-sidebar">
          <section class="mail-side-card">
            <div class="mail-side-heading"><p class="mail-section-eyebrow">Atalhos</p><span class="mail-side-mark" aria-hidden="true">⌁</span></div>
            <button class="mail-side-link" type="button" onclick="mailOpenCompose()"><span>＋</span> Escrever mensagem <b>→</b></button>
          </section>
          <section class="mail-side-card mail-side-highlight">
            <div class="mail-side-heading"><p class="mail-section-eyebrow mail-section-eyebrow-cyan">Visão geral</p><span class="mail-side-mark" aria-hidden="true">✦</span></div>
            <div class="mail-side-stat"><strong id="mail-unread-side">--</strong><span>mensagens não lidas</span></div>
            <p>Abra uma mensagem para marcar como lida e manter sua central sempre organizada.</p>
          </section>
        </aside>

        <main class="mail-main-column">
          <section class="mail-inbox-surface">
            <div class="mail-toolbar">
              <div class="mail-folder-tabs" id="mail-folder-tabs" role="tablist" aria-label="Pastas do correio">
                <button class="mail-folder-tab ${mailFolder === "inbox" ? "is-active" : ""}" data-folder="inbox" role="tab" aria-selected="${mailFolder === "inbox"}" onclick="mailSetFolder('inbox')"><span class="mail-folder-icon" aria-hidden="true">⌑</span><span>Entrada</span></button>
                <button class="mail-folder-tab ${mailFolder === "sent" ? "is-active" : ""}" data-folder="sent" role="tab" aria-selected="${mailFolder === "sent"}" onclick="mailSetFolder('sent')"><span class="mail-folder-icon" aria-hidden="true">↗</span><span>Enviadas</span></button>
              </div>
              <div class="mail-bulk-actions" id="mail-bulk-actions">
                <button id="mail-mark-all-read" class="mail-action-button" type="button" onclick="mailMarkAllRead()"><span aria-hidden="true">✓</span> Marcar como lidas</button>
                <button id="mail-delete-all" class="mail-action-button mail-action-danger" type="button" onclick="mailAskDeleteAll()"><span aria-hidden="true">⌫</span> Apagar todas</button>
              </div>
            </div>
            <div class="mail-list-heading">
              <div><p class="mail-section-eyebrow">${mailFolder === "inbox" ? "Recebidas" : "Histórico de envio"}</p><h2 id="mail-list-title">${mailFolder === "inbox" ? "Sua caixa de entrada" : "Mensagens enviadas"}</h2></div>
              <span class="mail-list-count" id="mail-list-count">Carregando...</span>
            </div>
            <div id="mail-list" class="mail-list">
              <div class="mail-loading-state"><span class="mail-loading-orb"></span><span>Carregando mensagens...</span></div>
            </div>
          </section>
          <div id="mail-pagination" class="mail-pagination"></div>
        </main>
      </div>
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
  const deleteButton = document.getElementById("mail-delete-all");
  if (button) button.classList.toggle("hidden", mailFolder !== "inbox");
  if (deleteButton) deleteButton.classList.remove("hidden");
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
  const countEl = document.getElementById("mail-unread-side");
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
  document.querySelectorAll("#mail-folder-tabs [data-folder]").forEach(button => {
    const active = button.dataset.folder === mailFolder;
    button.classList.toggle("is-active", active);
    button.setAttribute("aria-selected", String(active));
  });
  const heading = document.getElementById("mail-list-title");
  const eyebrow = heading?.previousElementSibling;
  if (heading) heading.textContent = mailFolder === "inbox" ? "Sua caixa de entrada" : "Mensagens enviadas";
  if (eyebrow) eyebrow.textContent = mailFolder === "inbox" ? "Recebidas" : "Histórico de envio";
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
  const totalMessages = Number(result.totalElements ?? messages.length);
  const countEl = document.getElementById("mail-list-count");
  if (countEl) countEl.textContent = `${totalMessages} ${totalMessages === 1 ? "mensagem" : "mensagens"}`;

  if (messages.length === 0) {
    list.innerHTML = `
      <div class="mail-empty-state">
        <span class="mail-empty-icon" aria-hidden="true">⌁</span>
        <strong>${mailFolder === "inbox" ? "Sua caixa de entrada está vazia." : "Você ainda não enviou mensagens."}</strong>
        <p>${mailFolder === "inbox" ? "Novos comunicados, convites e recompensas aparecerão aqui." : "As mensagens que você enviar ficarão disponíveis neste histórico."}</p>
        ${mailFolder === "sent" ? `<button class="mail-empty-action" type="button" onclick="mailOpenCompose()">＋ Escrever mensagem</button>` : ""}
      </div>
    `;
  } else {
    list.innerHTML = messages.map(mailRenderSummary).join("");
  }

  mailRenderPagination(result);
}

function mailRenderSummary(message) {
  const isUnread = mailFolder === "inbox" && !message.read;
  const otherPlayer = mailFolder === "inbox" ? message.senderUsername : message.recipientUsername;
  const messageTypeLabels = { PLAYER: "Jogador", SYSTEM: "Sistema", AUCTION: "Leilão", CLAN: "Clã", EVENT: "Evento", ADMIN: "Administração" };
  const originLabel = messageTypeLabels[message.messageType] || "Mensagem";
  const actionLabel = message.actionType === "CLAN_INVITE" || message.actionType === "EVENT_REWARD_CLAIM"
    ? `<span class="mail-message-action">Ação pendente</span>`
    : "";
  const icon = isUnread ? "✉" : message.actionType ? "!" : message.messageType === "PLAYER" ? "↗" : "✦";
  const subject = message.subject || "(Sem assunto)";

  return `
    <button class="mail-message-card ${isUnread ? "is-unread" : ""}" type="button" onclick="mailOpen('${message.id}')" aria-label="Abrir mensagem: ${escapeHtml(subject)}">
      <span class="mail-message-icon ${isUnread ? "is-unread" : ""}" aria-hidden="true">${icon}</span>
      <span class="mail-message-content">
        <span class="mail-message-topline"><span class="mail-message-origin">${originLabel}</span><time>${mailFormatDate(message.createdAt)}</time></span>
        <span class="mail-message-title-row"><strong class="mail-message-subject">${escapeHtml(subject)}</strong>${isUnread ? `<span class="mail-message-unread">Nova</span>` : ""}</span>
        <span class="mail-message-participant"><span>${mailFolder === "inbox" ? "De" : "Para"}</span> ${escapeHtml(otherPlayer || "Sistema")}</span>
        <span class="mail-message-footer">${actionLabel}<span class="mail-message-open">Abrir mensagem <b aria-hidden="true">→</b></span></span>
      </span>
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
    <button class="mail-pagination-button ${mailPage <= 0 ? "is-disabled" : ""}" type="button" onclick="mailChangePage(-1)" ${mailPage <= 0 ? "disabled" : ""}>← Anterior</button>
    <span class="mail-pagination-label">Página ${mailPage + 1} de ${totalPages}</span>
    <button class="mail-pagination-button ${mailPage >= totalPages - 1 ? "is-disabled" : ""}" type="button" onclick="mailChangePage(1)" ${mailPage >= totalPages - 1 ? "disabled" : ""}>Próxima →</button>
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

function mailAuctionBodyMarkup(message) {
  const isPurchase = message.actionType === "PURCHASE_COMPLETED_BUYER";
  const isSale = message.actionType === "PURCHASE_COMPLETED_SELLER";
  if (!isPurchase && !isSale) return escapeHtml(message.body).replace(/\n/g, "<br>");

  return String(message.body || "").split("\n").map(line => {
    const escaped = escapeHtml(line);
    if (!line.trim()) return "<br>";
    if (line.startsWith("A compra")) return `<p class="font-bold text-cyan-300">${escaped}</p>`;
    if (line.startsWith("A venda")) return `<p class="font-bold text-emerald-300">${escaped}</p>`;
    if (line.startsWith("Item: ")) return `<p><span class="text-slate-400">Item:</span> <strong class="text-violet-200">${escapeHtml(line.slice(6))}</strong></p>`;
    if (line.startsWith("Quantidade: ")) return `<p><span class="text-slate-400">Quantidade:</span> <strong class="text-slate-200">${escapeHtml(line.slice(12))}</strong></p>`;
    if (line.startsWith("Comprador: ")) return `<p><span class="text-slate-400">Comprador:</span> <strong class="text-amber-200">${escapeHtml(line.slice(11))}</strong></p>`;
    if (line.startsWith("Valor total: ")) return `<p><span class="text-slate-400">Valor total:</span> <strong class="text-red-300">${escapeHtml(line.slice(12))}</strong></p>`;
    if (line.startsWith("Valor bruto: ")) return `<p><span class="text-slate-400">Valor bruto:</span> <strong class="text-emerald-300">${escapeHtml(line.slice(12))}</strong></p>`;
    if (line.startsWith("Comissão: ")) return `<p><span class="text-slate-400">Comissão:</span> <strong class="text-red-300">${escapeHtml(line.slice(9))}</strong></p>`;
    if (line.startsWith("Valor líquido recebido: ")) return `<p><span class="text-slate-400">Valor líquido recebido:</span> <strong class="text-emerald-300">${escapeHtml(line.slice(24))}</strong></p>`;
    if (line.startsWith("O item já foi")) return `<p class="text-cyan-200">${escaped}</p>`;
    return `<p>${escaped}</p>`;
  }).join("");
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
        <div class="rounded-lg bg-slate-900/70 border border-slate-700 p-4 text-sm text-slate-200 break-words space-y-1">${mailAuctionBodyMarkup(message)}</div>
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

async function mailAskDeleteAll() {
  const confirmed = await showConfirm("Todas as mensagens visíveis da Entrada e de Enviadas serão apagadas. Mensagens da Entrada com recompensas pendentes serão preservadas.", { title: "Apagar todas as mensagens?", confirmText: "Apagar todas", danger: true });
  if (!confirmed) return;
  const button = document.getElementById("mail-delete-all");
  if (button) { button.disabled = true; button.textContent = "Apagando..."; }
  try {
    const result = await apiDelete("/mail/all");
    const preserved = Number(result.preservedCount || 0);
    showToast(preserved > 0 ? `${result.deletedCount || 0} mensagem(ns) apagada(s). ${preserved} mensagem(ns) com recompensa pendente foram preservadas.` : `${result.deletedCount || 0} mensagem(ns) apagada(s).`);
    await mailLoadFolder();
    await mailRefreshUnreadCount();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    if (button) { button.disabled = false; button.textContent = "Apagar todas"; }
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
