let adminAnnouncementSubmitting = false;

function renderMailAnnouncementsPage() {
  setPageHeader(
    "Comunicados do Correio",
    "Envie uma mensagem informativa para todos os jogadores"
  );

  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="max-w-4xl space-y-6">
      <div class="card">
        <div class="flex items-start justify-between gap-4 mb-2">
          <div>
            <h3 class="text-xl font-bold">Novo comunicado oficial</h3>
            <p class="text-sm text-slate-400 mt-1">
              A mensagem será enviada para todos os jogadores através do Correio.
            </p>
          </div>
          <span class="badge badge-purple">ADMIN</span>
        </div>

        <div id="admin-announcement-result" class="hidden mb-4 rounded-lg px-3 py-2 text-sm"></div>

        <form id="admin-announcement-form" class="space-y-4" onsubmit="adminSubmitAnnouncement(event)">
          <label class="block">
            <span class="text-sm text-slate-300">Assunto</span>
            <input
              id="admin-announcement-subject"
              class="input w-full mt-1"
              maxlength="80"
              required
              placeholder="Ex.: Manutenção programada"
              oninput="adminUpdateAnnouncementCounters()"
            >
            <span class="text-xs text-slate-500 mt-1 block text-right"><span id="admin-announcement-subject-count">0</span>/80</span>
          </label>

          <label class="block">
            <span class="text-sm text-slate-300">Comunicado</span>
            <textarea
              id="admin-announcement-body"
              class="input w-full mt-1 min-h-40"
              maxlength="1000"
              rows="8"
              required
              placeholder="Escreva o comunicado oficial..."
              oninput="adminUpdateAnnouncementCounters()"
            ></textarea>
            <span class="text-xs text-slate-500 mt-1 block text-right"><span id="admin-announcement-body-count">0</span>/1.000</span>
          </label>

          <div class="rounded-lg border border-amber-900/70 bg-amber-950/20 p-3 text-sm text-amber-200">
            <p class="font-semibold">Atenção</p>
            <p class="text-xs text-amber-100/70 mt-1">
              O envio é global e não pode ser desfeito. O comunicado não possui anexos, ações ou envio de itens.
            </p>
          </div>

          <div class="flex justify-end">
            <button id="admin-announcement-submit" class="btn-primary" type="submit">
              Enviar para todos os jogadores
            </button>
          </div>
        </form>
      </div>

      <div class="card">
        <h3 class="text-lg font-bold mb-2">Prévia para o jogador</h3>
        <div class="rounded-lg border border-slate-700 bg-slate-950 p-4">
          <p class="text-xs uppercase tracking-wider text-cyan-300">Comunicado oficial</p>
          <p id="admin-announcement-preview-subject" class="font-bold mt-2">Assunto do comunicado</p>
          <p id="admin-announcement-preview-body" class="text-sm text-slate-300 mt-2 whitespace-pre-wrap">O texto do comunicado aparecerá aqui.</p>
        </div>
      </div>
    </div>
  `;

  adminUpdateAnnouncementCounters();
}

function adminUpdateAnnouncementCounters() {
  const subject = document.getElementById("admin-announcement-subject");
  const body = document.getElementById("admin-announcement-body");
  const subjectCount = document.getElementById("admin-announcement-subject-count");
  const bodyCount = document.getElementById("admin-announcement-body-count");
  const previewSubject = document.getElementById("admin-announcement-preview-subject");
  const previewBody = document.getElementById("admin-announcement-preview-body");

  if (subjectCount) subjectCount.textContent = subject?.value.length || 0;
  if (bodyCount) bodyCount.textContent = body?.value.length || 0;
  if (previewSubject) previewSubject.textContent = subject?.value.trim() || "Assunto do comunicado";
  if (previewBody) previewBody.textContent = body?.value.trim() || "O texto do comunicado aparecerá aqui.";
}

function adminShowAnnouncementResult(message, success) {
  const result = document.getElementById("admin-announcement-result");
  if (!result) return;
  result.textContent = message;
  result.className = success
    ? "mb-4 rounded-lg px-3 py-2 text-sm border border-emerald-800 bg-emerald-950/40 text-emerald-300"
    : "mb-4 rounded-lg px-3 py-2 text-sm border border-red-800 bg-red-950/40 text-red-300";
}

async function adminSubmitAnnouncement(event) {
  event.preventDefault();
  if (adminAnnouncementSubmitting) return;

  const subject = document.getElementById("admin-announcement-subject")?.value.trim();
  const body = document.getElementById("admin-announcement-body")?.value.trim();
  if (!subject || !body) {
    adminShowAnnouncementResult("Preencha o assunto e o comunicado antes de enviar.", false);
    return;
  }

  const confirmed = confirm(
    "Este comunicado será enviado para TODOS os jogadores e não poderá ser desfeito.\n\nDeseja continuar?"
  );
  if (!confirmed) return;

  const button = document.getElementById("admin-announcement-submit");
  adminAnnouncementSubmitting = true;
  if (button) {
    button.disabled = true;
    button.textContent = "Enviando...";
  }

  try {
    const result = await apiPost("/admin/mail/announcements", { subject, body });
    adminShowAnnouncementResult(
      `Comunicado enviado para ${Number(result.delivered || 0).toLocaleString("pt-BR")} jogadores.`,
      true
    );
    document.getElementById("admin-announcement-form")?.reset();
    adminUpdateAnnouncementCounters();
  } catch (error) {
    adminShowAnnouncementResult(error.message || "Não foi possível enviar o comunicado.", false);
  } finally {
    adminAnnouncementSubmitting = false;
    if (button) {
      button.disabled = false;
      button.textContent = "Enviar para todos os jogadores";
    }
  }
}
