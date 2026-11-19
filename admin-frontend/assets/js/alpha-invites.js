function renderAlphaInvitesPage() {
  setPageHeader("Convites Alpha", "Gerencie o acesso à Alpha fechada");

  document.getElementById("app").innerHTML = `
    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6">
      <div class="card xl:col-span-1">
        <h3 class="text-lg font-semibold text-cyan-400 mb-4">
          Gerar convite
        </h3>

        <form id="alpha-invite-form" class="space-y-4">
          <div>
            <label class="text-sm text-slate-400">
              Nome do tester
            </label>

            <input
              id="alpha-invite-name"
              class="input mt-1"
              maxlength="100"
              required
            />
          </div>

          <div>
            <label class="text-sm text-slate-400">
              E-mail do tester
            </label>

            <input
              id="alpha-invite-email"
              type="email"
              class="input mt-1"
              maxlength="100"
              required
            />
          </div>

          <div>
            <label class="text-sm text-slate-400">
              Validade
            </label>

            <input
              id="alpha-invite-expires"
              type="datetime-local"
              class="input mt-1"
              required
            />
          </div>

          <button
            id="alpha-invite-submit"
            class="btn-primary w-full py-2"
            type="submit"
          >
            Gerar convite
          </button>
        </form>

        <div
          id="alpha-invite-created"
          class="hidden mt-5 p-4 rounded-lg border border-amber-700/60 bg-amber-950/20"
        >
          <p class="text-xs uppercase tracking-wide text-amber-400 mb-2">
            Código exibido apenas agora
          </p>

          <div class="flex gap-2 items-center">
            <code
              id="alpha-invite-code"
              class="text-sm text-amber-200 break-all flex-1"
            ></code>

            <button
              id="alpha-invite-copy"
              class="px-3 py-2 rounded bg-slate-800 hover:bg-slate-700 text-sm"
            >
              Copiar
            </button>
          </div>

          <p class="text-xs text-slate-500 mt-2">
            Depois de sair desta tela, o código completo não poderá
            ser recuperado pelo Admin.
          </p>
        </div>

        <div
          id="alpha-invite-error"
          class="hidden text-sm text-red-400 mt-3"
        ></div>
      </div>

      <div class="card xl:col-span-2">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-lg font-semibold">
              Convites recentes
            </h3>

            <p class="text-xs text-slate-500">
              Até 100 registros, sem exposição do código completo.
            </p>
          </div>

          <button
            onclick="loadAlphaInvites()"
            class="px-3 py-2 rounded bg-slate-800 hover:bg-slate-700 text-sm"
          >
            Atualizar
          </button>
        </div>

        <div
          id="alpha-invite-list"
          class="space-y-3"
        >
          <p class="text-slate-500">
            Carregando...
          </p>
        </div>
      </div>
    </div>
  `;

  const expires = document.getElementById("alpha-invite-expires");

  const defaultExpiration = new Date(
    Date.now() + 7 * 24 * 60 * 60 * 1000
  );

  defaultExpiration.setMinutes(
    defaultExpiration.getMinutes()
      - defaultExpiration.getTimezoneOffset()
  );

  expires.value = defaultExpiration
    .toISOString()
    .slice(0, 16);

  document
    .getElementById("alpha-invite-form")
    .addEventListener(
      "submit",
      createAlphaInvite
    );

  loadAlphaInvites();
}

async function createAlphaInvite(event) {
  event.preventDefault();

  const button = document.getElementById(
    "alpha-invite-submit"
  );

  const error = document.getElementById(
    "alpha-invite-error"
  );

  const created = document.getElementById(
    "alpha-invite-created"
  );

  button.disabled = true;
  button.textContent = "Gerando...";

  error.classList.add("hidden");
  created.classList.add("hidden");

  try {
    const response = await apiPost(
      "/admin/alpha-invites",
      {
        testerName: document
          .getElementById("alpha-invite-name")
          .value
          .trim(),

        testerEmail: document
          .getElementById("alpha-invite-email")
          .value
          .trim(),

        expiresAt: document
          .getElementById("alpha-invite-expires")
          .value
      }
    );

    document.getElementById(
      "alpha-invite-code"
    ).textContent = response.inviteCode;

    document.getElementById(
      "alpha-invite-copy"
    ).onclick = async () => {
      await navigator.clipboard.writeText(
        response.inviteCode
      );

      document.getElementById(
        "alpha-invite-copy"
      ).textContent = "Copiado";
    };

    created.classList.remove("hidden");

    document
      .getElementById("alpha-invite-form")
      .reset();

    resetAlphaInviteExpiration();

    await loadAlphaInvites();

  } catch (err) {
    error.textContent = err.message;
    error.classList.remove("hidden");

  } finally {
    button.disabled = false;
    button.textContent = "Gerar convite";
  }
}

async function loadAlphaInvites() {
  const container = document.getElementById(
    "alpha-invite-list"
  );

  if (!container) {
    return;
  }

  try {
    const invites = await apiGet(
      "/admin/alpha-invites"
    );

    if (!invites || invites.length === 0) {
      container.innerHTML = `
        <p class="text-slate-500">
          Nenhum convite criado.
        </p>
      `;

      return;
    }

    container.innerHTML = invites
      .map(invite => renderAlphaInviteCard(invite))
      .join("");

  } catch (err) {
    container.innerHTML = `
      <p class="text-red-400">
        ${escapeHtml(err.message)}
      </p>
    `;
  }
}

function renderAlphaInviteCard(invite) {
  const statusText = getAlphaInviteStatusText(
    invite.status
  );

  const statusClass = getAlphaInviteStatusClass(
    invite.status
  );

  const canDelete =
    invite.status === "AVAILABLE"
    || invite.status === "EXPIRED";

  const deletedClass =
    invite.status === "DELETED"
      ? "opacity-60"
      : "";

  return `
    <div
      class="border rounded-lg p-4 ${statusClass} ${deletedClass}"
    >
      <div
        class="flex flex-col md:flex-row md:items-start md:justify-between gap-2"
      >
        <div>
          <p class="font-medium text-slate-200">
            ${escapeHtml(invite.testerName)}
          </p>

          <p class="text-sm text-slate-400">
            ${escapeHtml(invite.testerEmail)}
          </p>

          <p class="text-xs text-slate-500 mt-2">
            Código final:
            ...${escapeHtml(invite.codeHint)}
          </p>
        </div>

        <span class="text-xs font-semibold uppercase">
          ${statusText}
        </span>
      </div>

      <div
        class="grid grid-cols-1 md:grid-cols-2 gap-2 mt-3 text-xs text-slate-500"
      >
        <span>
          Criado:
          ${formatAlphaInviteDate(invite.createdAt)}
        </span>

        <span>
          Expira:
          ${formatAlphaInviteDate(invite.expiresAt)}
        </span>

        ${
          invite.usedAt
            ? `
              <span>
                Usado:
                ${formatAlphaInviteDate(invite.usedAt)}
              </span>
            `
            : ""
        }

        ${
          invite.usedByPlayerId
            ? `
              <span>
                Player:
                ${escapeHtml(invite.usedByPlayerId)}
              </span>
            `
            : ""
        }

        ${
          invite.deletedAt
            ? `
              <span>
                Excluído:
                ${formatAlphaInviteDate(invite.deletedAt)}
              </span>
            `
            : ""
        }

        ${
          invite.deletedByAdminId
            ? `
              <span>
                Excluído pelo Admin:
                ${escapeHtml(invite.deletedByAdminId)}
              </span>
            `
            : ""
        }
      </div>

      ${
        canDelete
          ? `
            <div class="mt-4 pt-3 border-t border-slate-800">
              <button
                type="button"
                onclick="deleteAlphaInvite(
                  '${invite.id}',
                  '${escapeForJs(invite.testerName)}'
                )"
                class="
                  px-3
                  py-2
                  rounded
                  bg-red-950/40
                  border
                  border-red-900/60
                  text-red-400
                  hover:bg-red-900/40
                  transition-colors
                  text-sm
                "
              >
                Excluir convite
              </button>
            </div>
          `
          : ""
      }
    </div>
  `;
}

function deleteAlphaInvite(
  inviteId,
  testerName
) {
  openConfirmModal({
    title: "Excluir convite Alpha",

    message:
      `Deseja realmente excluir o convite de ${testerName}? `
      + "O convite será removido logicamente e permanecerá disponível apenas no histórico administrativo.",

    confirmText: "Excluir convite",

    cancelText: "Cancelar",

    onConfirm: async () => {
      await apiDelete(
        `/admin/alpha-invites/${inviteId}`
      );

      await loadAlphaInvites();
    }
  });
}

function getAlphaInviteStatusText(status) {
  const statuses = {
    AVAILABLE: "Disponível",
    USED: "Utilizado",
    EXPIRED: "Expirado",
    DELETED: "Excluído"
  };

  return statuses[status] || status;
}

function getAlphaInviteStatusClass(status) {
  if (status === "AVAILABLE") {
    return "text-green-400 border-green-900/60";
  }

  if (status === "USED") {
    return "text-cyan-400 border-cyan-900/60";
  }

  if (status === "DELETED") {
    return "text-red-400 border-red-900/60 bg-red-950/10";
  }

  return "text-slate-400 border-slate-700";
}

function formatAlphaInviteDate(value) {
  if (!value) {
    return "-";
  }

  return new Date(value).toLocaleString("pt-BR");
}

function resetAlphaInviteExpiration() {
  const expires = document.getElementById(
    "alpha-invite-expires"
  );

  if (!expires) {
    return;
  }

  const defaultExpiration = new Date(
    Date.now() + 7 * 24 * 60 * 60 * 1000
  );

  defaultExpiration.setMinutes(
    defaultExpiration.getMinutes()
      - defaultExpiration.getTimezoneOffset()
  );

  expires.value = defaultExpiration
    .toISOString()
    .slice(0, 16);
}

function escapeForJs(value) {
  if (!value) {
    return "";
  }

  return String(value)
    .replace(/\\/g, "\\\\")
    .replace(/'/g, "\\'")
    .replace(/\r/g, "")
    .replace(/\n/g, " ");
}