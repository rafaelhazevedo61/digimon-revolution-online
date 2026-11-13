function showAdminLogin() {
  const app = document.getElementById("app");
  const sidebar = document.querySelector("aside");
  const header = document.querySelector("header");

  if (sidebar) sidebar.classList.add("hidden");
  if (header) header.classList.add("hidden");

  app.innerHTML = `
    <div class="flex items-center justify-center min-h-screen p-4">
      <div class="w-full max-w-sm">
        <div class="text-center mb-8">
          <h1 class="text-3xl font-bold text-cyan-400 mb-1">DRO Admin</h1>
          <p class="text-slate-400 text-sm">Painel Administrativo</p>
        </div>

        <div class="card">
          <h2 class="text-lg font-semibold mb-4 text-center">Login</h2>

          <div id="admin-login-error" class="hidden mb-4 p-3 rounded-lg bg-red-950/50 border border-red-900 text-red-300 text-sm"></div>

          <form id="admin-login-form" onsubmit="adminLogin(event)">
            <div class="mb-4">
              <label class="block text-sm text-slate-400 mb-1" for="admin-identifier">E-mail ou nome de usuário</label>
              <input id="admin-identifier" type="text" autocomplete="username" class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 focus:outline-none focus:border-cyan-500" placeholder="Seu e-mail ou nome de usuário" required />
            </div>
            <div class="mb-6">
              <label class="block text-sm text-slate-400 mb-1" for="admin-password">Senha</label>
              <div class="relative">
                <input style="padding-inline-end: 3rem;" id="admin-password" type="password" autocomplete="current-password" class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 focus:outline-none focus:border-cyan-500" placeholder="••••••••" required />
                <button type="button" class="absolute inset-y-0 right-0 flex w-11 items-center justify-center text-slate-400 hover:text-cyan-300 focus-visible:text-cyan-300" aria-controls="admin-password" aria-label="Mostrar senha"
                  onclick="const field = document.getElementById(this.getAttribute('aria-controls')); const show = field.type === 'password'; field.type = show ? 'text' : 'password'; this.setAttribute('aria-label', show ? 'Ocultar senha' : 'Mostrar senha'); this.children[0].classList.toggle('hidden', show); this.children[1].classList.toggle('hidden', !show);">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
                  <svg class="hidden" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m3 3 18 18M10.6 10.6a2 2 0 0 0 2.8 2.8M9.2 5.4A10 10 0 0 1 12 5c6.4 0 10 7 10 7a17 17 0 0 1-3 3.7M6.1 6.1A18 18 0 0 0 2 12s3.6 7 10 7a11 11 0 0 0 4.2-.9"/></svg>
                </button>
              </div>
            </div>
            <button type="submit" id="admin-login-btn" class="w-full px-4 py-2 rounded-lg font-semibold text-white bg-cyan-600 hover:bg-cyan-500 transition-colors">
              Entrar
            </button>
          </form>

          <p class="text-xs text-slate-500 mt-4 text-center">
            Apenas usuarios com tipo ADMIN podem acessar.
          </p>
        </div>
      </div>
    </div>
  `;
}

async function adminLogin(e) {
  e.preventDefault();
  const btn = document.getElementById("admin-login-btn");
  const errorDiv = document.getElementById("admin-login-error");
  errorDiv.classList.add("hidden");
  btn.disabled = true;
  btn.textContent = "Entrando...";

  try {
    const res = await apiPostNoAuth("/auth/login", {
      identifier: document.getElementById("admin-identifier").value.trim(),
      password: document.getElementById("admin-password").value
    });

    const token = res.token;
    const payload = parseJwtPayload(token);

    if (!payload || payload.userType !== "ADMIN") {
      throw new Error("Acesso negado: usuario nao e ADMIN");
    }

    setAdminToken(token);
    showAdminPanel();
  } catch (err) {
    errorDiv.textContent = err.message;
    errorDiv.classList.remove("hidden");
    btn.disabled = false;
    btn.textContent = "Entrar";
  }
}

function adminLogout() {
  clearAdminAuth();
  showAdminLogin();
}

function showAdminPanel() {
  const sidebar = document.querySelector("aside");
  const header = document.querySelector("header");

  if (sidebar) {
    sidebar.classList.remove("hidden");
    sidebar.classList.add("md:flex", "md:flex-col");
  }
  if (header) header.classList.remove("hidden");

  setupRouter();
}
