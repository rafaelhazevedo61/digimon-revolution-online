async function renderSettingsPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">Configurações</h2>

      <!-- Segurança -->
      <div class="card mb-4">
        <div class="flex items-center gap-2 mb-4">
          <span class="text-xl">🔐</span>
          <h3 class="font-bold">Segurança</h3>
        </div>

        <div
          id="settings-error"
          class="hidden mb-4 p-3 rounded-lg bg-red-950/50 border border-red-900 text-red-300 text-sm"
        ></div>

        <div
          id="settings-success"
          class="hidden mb-4 p-3 rounded-lg bg-green-950/50 border border-green-900 text-green-300 text-sm"
        ></div>

        <form
          id="change-password-form"
          onsubmit="settingsChangePassword(event)"
        >
          <div class="mb-4">
            <label class="label">Senha atual</label>

            <input
              id="current-password"
              type="password"
              class="input"
              placeholder="••••••••"
              autocomplete="current-password"
              required
            />
          </div>

          <div class="mb-4">
            <label class="label">Nova senha</label>

            <input
              id="new-password"
              type="password"
              class="input"
              placeholder="••••••••"
              autocomplete="new-password"
              required
              minlength="8"
              maxlength="60"
            />

            <p class="text-xs text-slate-500 mt-1">
              Entre 8 e 60 caracteres.
            </p>
          </div>

          <div class="mb-6">
            <label class="label">Confirmar nova senha</label>

            <input
              id="confirm-new-password"
              type="password"
              class="input"
              placeholder="••••••••"
              autocomplete="new-password"
              required
              minlength="8"
              maxlength="60"
            />
          </div>

          <button
            type="submit"
            class="btn-primary w-full"
            id="change-password-btn"
          >
            Alterar senha
          </button>
        </form>
      </div>

      <!-- Sessão -->
      <div class="card mb-4">
        <div class="flex items-center gap-2 mb-4">
          <span class="text-xl">🚪</span>
          <h3 class="font-bold">Sessão</h3>
        </div>

        <button
          type="button"
          class="btn-secondary w-full"
          onclick="authLogout()"
        >
          Sair
        </button>

        <button
          type="button"
          class="btn-secondary w-full mt-3"
          id="logout-all-btn"
        >
          Encerrar sessões em todos os dispositivos
        </button>

        <p class="text-xs text-slate-500 mt-3">
          Esta opção desconecta sua conta de todos os dispositivos onde ela estiver autenticada.
        </p>
      </div>
    </div>
  `;

  document
    .getElementById("logout-all-btn")
    ?.addEventListener("click", settingsLogoutAll);
}

async function settingsChangePassword(event) {
  event.preventDefault();

  const errorDiv = document.getElementById("settings-error");
  const successDiv = document.getElementById("settings-success");
  const btn = document.getElementById("change-password-btn");

  const currentPassword =
    document.getElementById("current-password").value;

  const newPassword =
    document.getElementById("new-password").value;

  const confirmPassword =
    document.getElementById("confirm-new-password").value;

  errorDiv.classList.add("hidden");
  successDiv.classList.add("hidden");

  if (newPassword !== confirmPassword) {
    errorDiv.textContent =
      "A nova senha e a confirmação não coincidem.";

    errorDiv.classList.remove("hidden");
    return;
  }

  if (newPassword.length < 8 || newPassword.length > 60) {
    errorDiv.textContent =
      "A nova senha deve ter entre 8 e 60 caracteres.";

    errorDiv.classList.remove("hidden");
    return;
  }

  btn.disabled = true;
  btn.textContent = "Alterando...";

  try {
    const result = await apiPost(
      "/players/me/change-password",
      {
        currentPassword,
        newPassword
      }
    );

    if (result?.token) {
      setToken(result.token);
    }

    document
      .getElementById("change-password-form")
      .reset();

    successDiv.textContent =
      "Senha alterada com sucesso.";

    successDiv.classList.remove("hidden");
  } catch (err) {
    errorDiv.textContent =
      err.message || "Erro ao alterar senha.";

    errorDiv.classList.remove("hidden");
  } finally {
    btn.disabled = false;
    btn.textContent = "Alterar senha";
  }
}

async function settingsLogoutAll() {
  const errorDiv =
    document.getElementById("settings-error");

  const btn =
    document.getElementById("logout-all-btn");

  errorDiv.classList.add("hidden");

  btn.disabled = true;
  btn.textContent = "Encerrando sessões...";

  try {
    await apiPost("/players/me/logout-all");

    clearAuth();
    navigateTo("login");
  } catch (err) {
    errorDiv.textContent =
      err.message || "Erro ao encerrar sessões.";

    errorDiv.classList.remove("hidden");

    btn.disabled = false;
    btn.textContent =
      "Encerrar sessões em todos os dispositivos";
  }
}