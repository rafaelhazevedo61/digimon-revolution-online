function renderLoginPage() {
  const app = document.getElementById("app");
  const nav = document.getElementById("bottom-nav");
  if (nav) nav.classList.add("hidden");

  app.innerHTML = `
    <div class="flex items-center justify-center min-h-screen p-4">
      <div class="w-full max-w-sm">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-cyan-400 mb-1">DRO</h1>
          <p class="text-slate-400 text-sm">Digimon Revolution Online</p>
        </div>

        <div class="card">
          <div class="flex gap-2 mb-6">
            <button id="tab-login" class="tab-btn active" onclick="authSwitchTab('login')">Entrar</button>
            <button id="tab-register" class="tab-btn" onclick="authSwitchTab('register')">Criar Conta</button>
          </div>

          <div id="auth-error" class="hidden mb-4 p-3 rounded-lg bg-red-950/50 border border-red-900 text-red-300 text-sm"></div>

          <!-- Login form -->
          <form id="login-form" onsubmit="authLogin(event)">
            <div class="mb-4">
              <label class="label">Email</label>
              <input id="login-email" type="email" class="input" placeholder="seu@email.com" required />
            </div>
            <div class="mb-6">
              <label class="label">Senha</label>
              <input id="login-password" type="password" class="input" placeholder="••••••••" required />
            </div>
            <button type="submit" class="btn-primary w-full" id="login-btn">Entrar</button>
          </form>

          <!-- Register form -->
          <form id="register-form" class="hidden" onsubmit="authRegister(event)">
            <div class="mb-4">
              <label class="label">Username</label>
              <input id="reg-username" type="text" class="input" placeholder="Tamer123" required minlength="3" />
            </div>
            <div class="mb-4">
              <label class="label">Email</label>
              <input id="reg-email" type="email" class="input" placeholder="seu@email.com" required />
            </div>
            <div class="mb-6">
              <label class="label">Senha</label>
              <input id="reg-password" type="password" class="input" placeholder="••••••••" required minlength="6" />
            </div>
            <button type="submit" class="btn-primary w-full" id="reg-btn">Criar Conta</button>
          </form>
        </div>
      </div>
    </div>
  `;
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
  } else {
    loginForm.classList.add("hidden");
    registerForm.classList.remove("hidden");
    tabLogin.classList.remove("active");
    tabRegister.classList.add("active");
  }
}

async function authLogin(e) {
  e.preventDefault();
  const btn = document.getElementById("login-btn");
  const errorDiv = document.getElementById("auth-error");
  errorDiv.classList.add("hidden");
  btn.disabled = true;
  btn.textContent = "Entrando...";

  try {
    const res = await apiPost("/auth/login", {
      email: document.getElementById("login-email").value,
      password: document.getElementById("login-password").value
    });

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
  errorDiv.classList.add("hidden");
  btn.disabled = true;
  btn.textContent = "Criando...";

  try {
    await apiPost("/auth/register", {
      username: document.getElementById("reg-username").value,
      email: document.getElementById("reg-email").value,
      password: document.getElementById("reg-password").value
    });

    // Auto-login after register
    const res = await apiPost("/auth/login", {
      email: document.getElementById("reg-email").value,
      password: document.getElementById("reg-password").value
    });

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
    const startup = await apiGet("/players/me/startup");
    if (startup.redirectTo === "DIGITAMA_SELECTION" || startup.redirectTo === "DIGITAMA_HATCHING" || startup.redirectTo === "DIGIMON_SELECTION") {
      navigateTo("starter");
    } else {
      navigateTo("dashboard");
    }
  } catch {
    navigateTo("dashboard");
  }
}

function authLogout() {
  clearAuth();
  navigateTo("login");
}
