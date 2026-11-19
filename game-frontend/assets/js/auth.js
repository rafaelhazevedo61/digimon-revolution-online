let registrationInviteRequired = true;

function renderLoginPage() {
  const app = document.getElementById("app");
  const nav = document.getElementById("bottom-nav");

  if (nav) {
    nav.classList.add("hidden");
  }

  app.innerHTML = `
    <div class="flex items-center justify-center min-h-screen p-4">
      <div class="w-full max-w-sm">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-cyan-400 mb-1">DRO</h1>
          <p class="text-slate-400 text-sm">Digimon Revolution Online</p>
        </div>

        <div class="card">
          <div class="flex gap-2 mb-6">
            <button
              id="tab-login"
              class="tab-btn active"
              onclick="authSwitchTab('login')"
            >
              Entrar
            </button>

            <button
              id="tab-register"
              class="tab-btn"
              onclick="authSwitchTab('register')"
            >
              Criar Conta
            </button>
          </div>

          <div
            id="auth-error"
            class="hidden mb-4 p-3 rounded-lg bg-red-950/50 border border-red-900 text-red-300 text-sm"
          ></div>

          <!-- Login form -->
          <form id="login-form" onsubmit="authLogin(event)">
            <div class="mb-4">
              <label class="label" for="login-identifier">E-mail ou nome de usuário</label>

              <input
                id="login-identifier"
                type="text"
                autocomplete="username"
                class="input"
                placeholder="Seu e-mail ou nome de usuário"
                required
              />
            </div>

            <div class="mb-6">
              <label class="label" for="login-password">Senha</label>

              <div class="relative">
                <input style="padding-inline-end: 3rem;"
                  id="login-password"
                  type="password" autocomplete="current-password"
                  class="input"
                  placeholder="••••••••"
                  required
                />
                <button type="button" class="absolute inset-y-0 right-0 flex w-11 items-center justify-center text-slate-400 hover:text-cyan-300 focus-visible:text-cyan-300" aria-controls="login-password" aria-label="Mostrar senha"
                  onclick="const field = document.getElementById(this.getAttribute('aria-controls')); const show = field.type === 'password'; field.type = show ? 'text' : 'password'; this.setAttribute('aria-label', show ? 'Ocultar senha' : 'Mostrar senha'); this.children[0].classList.toggle('hidden', show); this.children[1].classList.toggle('hidden', !show);">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                  <svg class="hidden" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m3 3 18 18M10.6 10.6a2 2 0 0 0 2.8 2.8M9.2 5.4A10 10 0 0 1 12 5c6.4 0 10 7 10 7a17 17 0 0 1-3 3.7M6.1 6.1A18 18 0 0 0 2 12s3.6 7 10 7a11 11 0 0 0 4.2-.9"/></svg>
                </button>
              </div>
            </div>

            <button
              type="submit"
              class="btn-primary w-full"
              id="login-btn"
            >
              Entrar
            </button>
          </form>

          <!-- Register form -->
          <form
            id="register-form"
            class="hidden"
            onsubmit="authRegister(event)"
          >
            <div class="mb-4">
              <label class="label">Username</label>

              <input
                id="reg-username"
                type="text"
                class="input"
                placeholder="Tamer123"
                required
                minlength="3"
              />
            </div>

            <div class="mb-4">
              <label class="label">Email</label>

              <input
                id="reg-email"
                type="email"
                class="input"
                placeholder="seu@email.com"
                required
              />
            </div>

            <div class="mb-4">
              <label class="label">Senha</label>

              <input
                id="reg-password"
                type="password"
                class="input"
                placeholder="••••••••"
                required
                minlength="8"
                maxlength="60"
              />

              <p class="text-xs text-slate-500 mt-1">
                A senha deve ter entre 8 e 60 caracteres.
              </p>
            </div>

            <div
              id="invite-code-container"
              class="hidden mb-6"
            >
              <label class="label">
                Código de convite da Alpha
              </label>

              <input
                id="reg-invite-code"
                type="text"
                class="input uppercase"
                placeholder="DRO-ALPHA-XXXX-XXXX-XXXX-XXXX"
                autocomplete="off"
              />

              <p class="text-xs text-slate-500 mt-1">
                O acesso à Alpha fechada requer um convite válido.
              </p>
            </div>

            <button
              type="submit"
              class="btn-primary w-full"
              id="reg-btn"
            >
              Criar Conta
            </button>
          </form>
        </div>

        <p id="app-version" class="text-center text-xs text-slate-600 mt-4"></p>
      </div>
    </div>
  `;

  loadPublicConfig();
}

async function loadPublicConfig() {
  try {
    const response = await fetch(
      `${CONFIG.API_BASE_URL}/public/config`
    );

    if (!response.ok) {
      throw new Error(
        "Não foi possível carregar as configurações públicas."
      );
    }

    const config = await response.json();

    registrationInviteRequired =
      config.registrationInviteRequired === true;

    updateInviteCodeVisibility();
    renderAppVersion(config.appVersion);

  } catch (error) {
    console.error(
      "Erro ao carregar configuração pública:",
      error
    );

    /*
     * Fail closed:
     * se não conseguirmos consultar a configuração,
     * consideramos o convite obrigatório.
     */
    registrationInviteRequired = true;

    updateInviteCodeVisibility();
  }
}

function renderAppVersion(version) {
  const el = document.getElementById("app-version");

  if (!el || !version) {
    return;
  }

  el.textContent = `v${version}`;
}

function updateInviteCodeVisibility() {
  const container = document.getElementById(
    "invite-code-container"
  );

  const input = document.getElementById(
    "reg-invite-code"
  );

  if (!container || !input) {
    return;
  }

  if (registrationInviteRequired) {
    container.classList.remove("hidden");
    input.required = true;
    return;
  }

  container.classList.add("hidden");
  input.required = false;
  input.value = "";
}

function authSwitchTab(tab) {
  const loginForm = document.getElementById("login-form");
  const registerForm = document.getElementById("register-form");

  const tabLogin = document.getElementById("tab-login");
  const tabRegister = document.getElementById("tab-register");

  const errorDiv = document.getElementById("auth-error");

  errorDiv.classList.add("hidden");

  if (tab === "login") {
    loginForm.classList.remove("hidden");
    registerForm.classList.add("hidden");

    tabLogin.classList.add("active");
    tabRegister.classList.remove("active");

    return;
  }

  loginForm.classList.add("hidden");
  registerForm.classList.remove("hidden");

  tabLogin.classList.remove("active");
  tabRegister.classList.add("active");
}

async function authLogin(e) {
  e.preventDefault();

  const btn = document.getElementById("login-btn");
  const errorDiv = document.getElementById("auth-error");

  errorDiv.classList.add("hidden");

  btn.disabled = true;
  btn.textContent = "Entrando...";

  try {
    const res = await apiPost(
      "/auth/login",
      {
        identifier: document
          .getElementById("login-identifier")
          .value.trim(),

        password: document
          .getElementById("login-password")
          .value
      }
    );

    setToken(res.token);
    setPlayerId(res.playerId);

    await authCheckStartup();

  } catch (err) {
    errorDiv.textContent = err.message;
    errorDiv.classList.remove("hidden");

    btn.disabled = false;
    btn.textContent = "Entrar";
  }
}

async function authRegister(e) {
  e.preventDefault();

  const btn = document.getElementById("reg-btn");
  const errorDiv = document.getElementById("auth-error");

  const username = document
    .getElementById("reg-username")
    .value
    .trim();

  const email = document
    .getElementById("reg-email")
    .value
    .trim();

  const password = document
    .getElementById("reg-password")
    .value;

  errorDiv.classList.add("hidden");

  if (password.length < 8 || password.length > 60) {
    errorDiv.textContent =
      "A senha deve ter entre 8 e 60 caracteres.";

    errorDiv.classList.remove("hidden");

    return;
  }

  let inviteCode = null;

  if (registrationInviteRequired) {
    const inviteInput = document.getElementById(
      "reg-invite-code"
    );

    inviteCode = inviteInput
      ? inviteInput.value.trim()
      : "";

    if (!inviteCode) {
      errorDiv.textContent =
        "Informe o código de convite da Alpha.";

      errorDiv.classList.remove("hidden");

      return;
    }
  }

  btn.disabled = true;
  btn.textContent = "Criando...";

  try {
    const registerPayload = {
      username,
      email,
      password
    };

    if (registrationInviteRequired) {
      registerPayload.inviteCode = inviteCode;
    }

    await apiPost(
      "/auth/register",
      registerPayload
    );

    const res = await apiPost(
      "/auth/login",
      {
        email,
        password
      }
    );

    setToken(res.token);
    setPlayerId(res.playerId);

    await authCheckStartup();

  } catch (err) {
    errorDiv.textContent = err.message;
    errorDiv.classList.remove("hidden");

    btn.disabled = false;
    btn.textContent = "Criar Conta";
  }
}

async function authCheckStartup() {
  try {
    const startup = await apiGet(
      "/players/me/startup"
    );

    if (
      startup.redirectTo === "DIGITAMA_SELECTION"
      || startup.redirectTo === "DIGITAMA_HATCHING"
    ) {
      navigateTo("starter");
      return;
    }

    if (
      startup.redirectTo === "DIGIMON_SELECTION"
    ) {
      navigateTo("digimon-select");
      return;
    }

    navigateTo("dashboard");

  } catch {
    navigateTo("dashboard");
  }
}

function authLogout() {
  clearAuth();
  navigateTo("login");
}