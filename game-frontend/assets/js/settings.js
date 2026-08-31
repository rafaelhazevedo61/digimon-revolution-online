async function renderSettingsPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container settings-menu-page">
      <header class="settings-menu-header">
        <div class="settings-menu-title-wrap">
          <div class="settings-menu-icon">⚙</div>
          <div>
            <p class="settings-eyebrow">Conta e preferências</p>
            <h2 class="settings-menu-title">Configurações</h2>
          </div>
        </div>
        <span class="settings-menu-caption">Minha conta</span>
      </header>

      <section class="settings-menu-card">
        <div class="settings-menu-intro">
          <p class="settings-eyebrow">Gerencie sua experiência</p>
          <p class="settings-menu-description">Escolha uma opção para atualizar seus dados, proteger sua conta ou configurar o jogo.</p>
        </div>
        <nav class="settings-menu-list" aria-label="Opções de configurações">
          ${settingsMenuItem("username", "👤", "Alterar username", "Atualize o nome exibido no jogo", "Conta")}
          ${settingsMenuItem("email", "✉", "Alterar e-mail", "Atualize o endereço associado à conta", "Conta")}
          ${settingsMenuItem("password", "🔐", "Alterar senha", "Mantenha sua conta protegida", "Segurança")}
          ${settingsMenuItem("sessions", "🚪", "Sessões e acesso", "Sair ou encerrar sessões em outros dispositivos", "Segurança")}
          ${settingsMenuItem("shortcuts", "⌨", "Barra de atalhos", "Configure seus atalhos de jogo", "Personalização")}
        </nav>
      </section>

      <p class="settings-menu-footer">Suas alterações são aplicadas imediatamente e protegidas pela sua sessão atual.</p>
    </div>
  `;
}

function settingsMenuItem(type, icon, title, description, category, comingSoon = false) {
  return `
    <button type="button" class="settings-menu-item ${comingSoon ? "is-coming-soon" : ""}" onclick="${comingSoon ? "settingsShowShortcuts()" : `settingsOpenPanel('${type}')`}">
      <span class="settings-menu-item-icon" aria-hidden="true">${icon}</span>
      <span class="settings-menu-item-content">
        <span class="settings-menu-item-category">${category}</span>
        <span class="settings-menu-item-title">${title}</span>
        <span class="settings-menu-item-description">${description}</span>
      </span>
      <span class="settings-menu-item-arrow" aria-hidden="true">›</span>
      ${comingSoon ? `<span class="settings-coming-soon">Em breve</span>` : ""}
    </button>
  `;
}

function settingsOpenPanel(type) {
  const currentEmail = parseJwtPayload(getToken())?.email || "Não disponível";
  const content = {
    username: {
      icon: "👤",
      category: "Conta",
      title: "Alterar username",
      description: "Escolha um novo nome para sua conta."
    },
    email: {
      icon: "✉",
      category: "Conta",
      title: "Alterar e-mail",
      description: "Atualize o e-mail usado para acessar sua conta."
    },
    password: {
      icon: "🔐",
      category: "Segurança",
      title: "Alterar senha",
      description: "Use uma senha forte para manter sua conta protegida."
    },
    sessions: {
      icon: "🚪",
      category: "Segurança",
      title: "Sessões e acesso",
      description: "Gerencie o acesso à sua conta neste e em outros dispositivos."
    }
  }[type];
  if (!content) return;

  document.getElementById("settings-panel-modal")?.remove();
  const overlay = document.createElement("div");
  overlay.id = "settings-panel-modal";
  overlay.className = "settings-panel-overlay fixed inset-0 z-50 flex items-center justify-center p-4";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.onclick = event => { if (event.target === overlay) settingsClosePanel(); };
  overlay.innerHTML = `
    <div class="settings-panel-modal card" role="document">
      <button type="button" class="settings-panel-close" onclick="settingsClosePanel()" aria-label="Fechar">×</button>
      <div class="settings-panel-heading">
        <span class="settings-panel-icon">${content.icon}</span>
        <div>
          <p class="settings-eyebrow">${content.category}</p>
          <h3 class="settings-panel-title">${content.title}</h3>
          <p class="settings-panel-description">${content.description}</p>
        </div>
      </div>

      <div id="settings-error" class="hidden settings-feedback settings-feedback-error"></div>
      <div id="settings-success" class="hidden settings-feedback settings-feedback-success"></div>

      ${type === "username" ? `
        <div class="settings-current-value">
          <span>Username atual</span>
          <strong id="current-username-value">Carregando...</strong>
          <small id="username-change-cost">Custo da próxima troca: carregando...</small>
          <small id="username-change-bits">Bits disponíveis: carregando...</small>
        </div>
        <form id="change-username-form" onsubmit="settingsChangeUsername(event)" class="settings-panel-form">
          <label class="label" for="new-username">Novo username</label>
          <input id="new-username" type="text" class="input" placeholder="Seu novo username" autocomplete="nickname" minlength="3" maxlength="50" required />
          <p class="settings-form-help">A troca custa Bits do seu Digimon ativo.</p>
          <button type="submit" class="btn-primary w-full" id="change-username-btn">Alterar username</button>
        </form>
      ` : ""}

      ${type === "email" ? `
        <div class="settings-current-value">
          <span>E-mail atual</span>
          <strong id="current-email-value">${escapeHtml(currentEmail)}</strong>
        </div>
        <form id="change-email-form" onsubmit="settingsChangeEmail(event)" class="settings-panel-form">
          <label class="label" for="new-email">Novo e-mail</label>
          <input id="new-email" type="email" class="input" placeholder="seu-email@exemplo.com" autocomplete="email" required />
          <label class="label" for="email-current-password">Senha atual</label>
          <input id="email-current-password" type="password" class="input" placeholder="••••••••" autocomplete="current-password" required />
          <p class="settings-form-help">Por segurança, confirme sua senha atual.</p>
          <button type="submit" class="btn-primary w-full" id="change-email-btn">Alterar e-mail</button>
        </form>
      ` : ""}

      ${type === "password" ? `
        <form id="change-password-form" onsubmit="settingsChangePassword(event)" class="settings-panel-form">
          <label class="label" for="current-password">Senha atual</label>
          <input id="current-password" type="password" class="input" placeholder="••••••••" autocomplete="current-password" required />
          <label class="label" for="new-password">Nova senha</label>
          <input id="new-password" type="password" class="input" placeholder="••••••••" autocomplete="new-password" minlength="8" maxlength="60" required />
          <p class="settings-form-help">A nova senha deve ter entre 8 e 60 caracteres.</p>
          <label class="label" for="confirm-new-password">Confirmar nova senha</label>
          <input id="confirm-new-password" type="password" class="input" placeholder="••••••••" autocomplete="new-password" minlength="8" maxlength="60" required />
          <button type="submit" class="btn-primary w-full" id="change-password-btn">Alterar senha</button>
        </form>
      ` : ""}

      ${type === "sessions" ? `
        <div class="settings-session-actions">
          <button type="button" class="btn-secondary w-full" onclick="authLogout()">Sair da conta</button>
          <button type="button" class="btn-secondary w-full" id="logout-all-btn" onclick="settingsLogoutAll()">Encerrar sessões em todos os dispositivos</button>
        </div>
        <p class="settings-form-help">A segunda opção desconecta sua conta de todos os dispositivos autenticados, mantendo apenas esta sessão até a conclusão.</p>
      ` : ""}
    </div>
  `;
  document.body.appendChild(overlay);
  if (type === "username") settingsLoadUsernameChangeInfo();
}

function settingsClosePanel() {
  document.getElementById("settings-panel-modal")?.remove();
}

let settingsShortcutDraft = [];

async function settingsShowShortcuts() {
  document.getElementById("settings-shortcuts-modal")?.remove();
  try {
    settingsShortcutDraft = [...await loadPlayerShortcutPreference()];
  } catch (_) {
    settingsShortcutDraft = [];
  }
  settingsRenderShortcutsModal();
}

function settingsRenderShortcutsModal() {
  const catalog = getPlayerShortcutCatalog();
  const selected = new Set(settingsShortcutDraft);
  const rows = catalog.map(item => {
    const isSelected = selected.has(item.route);
    const selectedIndex = settingsShortcutDraft.indexOf(item.route);
    const icon = item.image
      ? `<img src="${escapeAttr(item.image)}" alt="" class="settings-shortcut-option-image" />`
      : `<span class="settings-shortcut-option-emoji">${item.icon || "•"}</span>`;
    return `
      <div class="settings-shortcut-option ${isSelected ? "is-selected" : ""}">
        <button type="button" class="settings-shortcut-toggle" onclick="settingsToggleShortcut('${escapeAttr(item.route)}')" aria-pressed="${isSelected}">
          <span class="settings-shortcut-option-icon">${icon}</span>
          <span class="settings-shortcut-option-label">${escapeHtml(item.label)}</span>
          <span class="settings-shortcut-option-check">${isSelected ? "✓" : ""}</span>
        </button>
        ${isSelected ? `<span class="settings-shortcut-order"><button type="button" onclick="settingsMoveShortcut('${escapeAttr(item.route)}', -1)" aria-label="Mover ${escapeAttr(item.label)} para cima" ${selectedIndex === 0 ? "disabled" : ""}>↑</button><button type="button" onclick="settingsMoveShortcut('${escapeAttr(item.route)}', 1)" aria-label="Mover ${escapeAttr(item.label)} para baixo" ${selectedIndex === settingsShortcutDraft.length - 1 ? "disabled" : ""}>↓</button></span>` : ""}
      </div>
    `;
  }).join("");
  const overlay = document.createElement("div");
  overlay.id = "settings-shortcuts-modal";
  overlay.className = "settings-panel-overlay fixed inset-0 z-50 flex items-center justify-center p-4";
  overlay.onclick = event => { if (event.target === overlay) overlay.remove(); };
  overlay.innerHTML = `
    <div class="settings-panel-modal settings-shortcuts-modal card">
      <button type="button" class="settings-panel-close" onclick="document.getElementById('settings-shortcuts-modal')?.remove()" aria-label="Fechar">×</button>
      <div class="settings-panel-heading">
        <span class="settings-panel-icon">⌨</span>
        <div>
          <p class="settings-eyebrow">Personalização</p>
          <h3 class="settings-panel-title">Barra de atalhos</h3>
          <p class="settings-panel-description">Escolha e ordene os atalhos que deseja acessar rapidamente.</p>
        </div>
      </div>
      <div class="settings-shortcuts-limit"><span>Mobile</span><strong>Até 4 atalhos + Mais</strong><span>Desktop</span><strong>Até 8 atalhos + Mais</strong></div>
      <div class="settings-shortcuts-list">${rows}</div>
      <div id="settings-shortcuts-feedback" class="hidden settings-feedback"></div>
      <button type="button" class="btn-primary w-full mt-4" id="settings-shortcuts-save" onclick="settingsSaveShortcuts()">Salvar atalhos</button>
    </div>
  `;
  document.body.appendChild(overlay);
}

function settingsToggleShortcut(route) {
  const index = settingsShortcutDraft.indexOf(route);
  if (index >= 0) {
    settingsShortcutDraft.splice(index, 1);
  } else if (settingsShortcutDraft.length < 8) {
    settingsShortcutDraft.push(route);
  } else {
    showToast("Você pode escolher até 8 atalhos no desktop.", "info");
    return;
  }
  settingsRenderShortcutsModal();
}

function settingsMoveShortcut(route, direction) {
  const index = settingsShortcutDraft.indexOf(route);
  const target = index + direction;
  if (index < 0 || target < 0 || target >= settingsShortcutDraft.length) return;
  [settingsShortcutDraft[index], settingsShortcutDraft[target]] = [settingsShortcutDraft[target], settingsShortcutDraft[index]];
  settingsRenderShortcutsModal();
}

async function settingsSaveShortcuts() {
  const button = document.getElementById("settings-shortcuts-save");
  const feedback = document.getElementById("settings-shortcuts-feedback");
  if (!button || !feedback) return;
  button.disabled = true;
  button.textContent = "Salvando...";
  feedback.className = "hidden settings-feedback";
  try {
    await savePlayerShortcutPreference(settingsShortcutDraft);
    feedback.textContent = "Atalhos salvos com sucesso.";
    feedback.className = "settings-feedback settings-feedback-success";
    setTimeout(() => document.getElementById("settings-shortcuts-modal")?.remove(), 450);
  } catch (err) {
    feedback.textContent = err.message || "Não foi possível salvar os atalhos.";
    feedback.className = "settings-feedback settings-feedback-error";
    button.disabled = false;
    button.textContent = "Salvar atalhos";
  }
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