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
              <label class="block text-sm text-slate-400 mb-1">Email</label>
              <input id="admin-email" type="email" class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 focus:outline-none focus:border-cyan-500" placeholder="admin@email.com" required />
            </div>
            <div class="mb-6">
              <label class="block text-sm text-slate-400 mb-1">Senha</label>
              <input id="admin-password" type="password" class="w-full px-3 py-2 bg-slate-800 border border-slate-700 rounded-lg text-slate-100 focus:outline-none focus:border-cyan-500" placeholder="••••••••" required />
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
      email: document.getElementById("admin-email").value,
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
