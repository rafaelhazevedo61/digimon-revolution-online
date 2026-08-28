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

        <div class="mb-6 p-3 rounded-lg bg-slate-900/50 border border-slate-800">
          <p class="text-xs text-slate-400">Username atual</p>
          <p id="current-username-value" class="font-medium text-slate-200 mt-1">Carregando...</p>
          <p id="username-change-cost" class="text-xs text-amber-300 mt-2">Custo da próxima troca: carregando...</p>
          <p id="username-change-bits" class="text-xs text-slate-400 mt-1">Bits disponíveis: carregando...</p>
        </div>

        <form
          id="change-username-form"
          onsubmit="settingsChangeUsername(event)"
          class="mb-8"
        >
          <div class="mb-4">
            <label class="label" for="new-username">Novo username</label>
            <input
              id="new-username"
              type="text"
              class="input"
              placeholder="Seu novo username"
              autocomplete="nickname"
              minlength="3"
              maxlength="50"
              required
            />
          </div>

          <p class="text-xs text-slate-500 mb-4">
            A troca custa Bits do seu Digimon ativo. O valor aumenta a cada nova troca.
          </p>

          <button type="submit" class="btn-secondary w-full" id="change-username-btn">
            Alterar username
          </button>
        </form>

        <div class="mb-6 p-3 rounded-lg bg-slate-900/50 border border-slate-800">
          <p class="text-xs text-slate-400">E-mail atual</p>
          <p id="current-email-value" class="font-medium text-slate-200 mt-1">Carregando...</p>
        </div>

        <form
          id="change-email-form"
          onsubmit="settingsChangeEmail(event)"
          class="mb-8"
        >
          <div class="mb-4">
            <label class="label" for="new-email">Novo e-mail</label>
            <input
              id="new-email"
              type="email"
              class="input"
              placeholder="seu-email@exemplo.com"
              autocomplete="email"
              required
            />
          </div>

          <div class="mb-4">
            <label class="label" for="email-current-password">Senha atual</label>
            <input
              id="email-current-password"
              type="password"
              class="input"
              placeholder="••••••••"
              autocomplete="current-password"
              required
            />
          </div>

          <p class="text-xs text-slate-500 mb-4">
            Por segurança, confirme sua senha atual. As outras sessões serão encerradas e este dispositivo continuará conectado.
          </p>

          <button
            type="submit"
            class="btn-secondary w-full"
            id="change-email-btn"
          >
            Alterar e-mail
          </button>
        </form>

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

  const currentEmail = parseJwtPayload(getToken())?.email;
  const currentEmailValue = document.getElementById("current-email-value");
  if (currentEmailValue) {
    currentEmailValue.textContent = currentEmail || "Não disponível";
  }

  document
    .getElementById("logout-all-btn")
    ?.addEventListener("click", settingsLogoutAll);

  settingsLoadUsernameChangeInfo();
}

async function settingsLoadUsernameChangeInfo() {
  try {
    const info = await apiGet("/players/me/change-username");
    document.getElementById("current-username-value").textContent = info.username;
    document.getElementById("username-change-cost").textContent = `Custo da próxima troca: ${info.cost.toLocaleString("pt-BR")} Bits`;
    document.getElementById("username-change-bits").textContent = `Bits disponíveis: ${info.availableBits.toLocaleString("pt-BR")}`;
  } catch (err) {
    document.getElementById("current-username-value").textContent = "Não disponível";
    document.getElementById("username-change-cost").textContent = err.message || "Não foi possível carregar o custo.";
  }
}

async function settingsChangeUsername(event) {
  event.preventDefault();
  const errorDiv = document.getElementById("settings-error");
  const successDiv = document.getElementById("settings-success");
  const btn = document.getElementById("change-username-btn");
  const newUsername = document.getElementById("new-username").value.trim();
  errorDiv.classList.add("hidden");
  successDiv.classList.add("hidden");
  if (newUsername.length < 3 || newUsername.length > 50) {
    errorDiv.textContent = "O username deve ter entre 3 e 50 caracteres.";
    errorDiv.classList.remove("hidden");
    return;
  }
  btn.disabled = true;
  btn.textContent = "Alterando...";
  try {
    const result = await apiPost("/players/me/change-username", { newUsername });
    if (result?.token) setToken(result.token);
    document.getElementById("change-username-form").reset();
    successDiv.textContent = `Username alterado com sucesso. Foram cobrados ${result.cost.toLocaleString("pt-BR")} Bits.`;
    successDiv.classList.remove("hidden");
    await settingsLoadUsernameChangeInfo();
  } catch (err) {
    errorDiv.textContent = err.message || "Erro ao alterar username.";
    errorDiv.classList.remove("hidden");
  } finally {
    btn.disabled = false;
    btn.textContent = "Alterar username";
  }
}

async function settingsChangeEmail(event) {
  event.preventDefault();

  const errorDiv = document.getElementById("settings-error");
  const successDiv = document.getElementById("settings-success");
  const btn = document.getElementById("change-email-btn");
  const newEmail = document.getElementById("new-email").value.trim();
  const currentPassword = document.getElementById("email-current-password").value;

  errorDiv.classList.add("hidden");
  successDiv.classList.add("hidden");

  if (!newEmail) {
    errorDiv.textContent = "Informe o novo e-mail.";
    errorDiv.classList.remove("hidden");
    return;
  }

  btn.disabled = true;
  btn.textContent = "Alterando...";

  try {
    const result = await apiPost("/players/me/change-email", {
      currentPassword,
      newEmail
    });

    if (result?.token) {
      setToken(result.token);
    }

    const updatedEmail = result?.email || newEmail.toLowerCase();
    const currentEmailValue = document.getElementById("current-email-value");
    if (currentEmailValue) {
      currentEmailValue.textContent = updatedEmail;
    }

    document.getElementById("change-email-form").reset();
    successDiv.textContent = "E-mail alterado com sucesso.";
    successDiv.classList.remove("hidden");
  } catch (err) {
    errorDiv.textContent = err.message || "Erro ao alterar e-mail.";
    errorDiv.classList.remove("hidden");
  } finally {
    btn.disabled = false;
    btn.textContent = "Alterar e-mail";
  }
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