const playerState = {
  page: 0,
  size: 20,
  username: "",
  email: "",
  selectedDigitama: "",
  starterSelected: "",
  lastResult: null
};

function renderPlayersPage() {
  setPageHeader(
    "Players",
    "Consulte os jogadores cadastrados e dados principais da conta"
  );

  const app = document.getElementById("app");

  app.innerHTML = `
      <div class="card mb-6">
        <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
  
          <div>
            <label class="text-sm text-slate-400">Username</label>
            <input id="player-filter-username" class="input mt-1" placeholder="Ex: rafael" value="${playerState.username}" />
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Email</label>
            <input id="player-filter-email" class="input mt-1" placeholder="Ex: gmail" value="${playerState.email}" />
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Digitama</label>
            <select id="player-filter-selected-digitama" class="input mt-1">
              ${digitamaOptions(playerState.selectedDigitama)}
            </select>
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Starter selecionado</label>
            <select id="player-filter-starter-selected" class="input mt-1">
              ${booleanOptions(playerState.starterSelected)}
            </select>
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Itens por página</label>
            <select id="player-filter-size" class="input mt-1">
              ${sizeOptions(playerState.size)}
            </select>
          </div>
  
        </div>
  
        <div class="flex flex-col md:flex-row gap-3 mt-6">
          <button class="btn-primary" onclick="applyPlayerFilters()">
            Buscar
          </button>
  
          <button class="btn-secondary" onclick="clearPlayerFilters()">
            Limpar filtros
          </button>
        </div>
      </div>
  
      <div id="players-result"></div>
    `;

  loadPlayers();
}

async function loadPlayers() {
  const container = document.getElementById("players-result");

  container.innerHTML = `
      <div class="card">
        <p class="text-slate-400">Carregando players...</p>
      </div>
    `;

  try {
    const result = await apiGet("/admin/players", {
      page: playerState.page,
      size: playerState.size,
      username: playerState.username,
      email: playerState.email,
      selectedDigitama: playerState.selectedDigitama,
      starterSelected: playerState.starterSelected
    });

    playerState.lastResult = result;
    renderPlayersResult(result);
  } catch (error) {
    container.innerHTML = `
        <div class="card border-red-900 bg-red-950/30">
          <h3 class="font-bold text-red-300 mb-2">Erro ao carregar players</h3>
          <p class="text-red-200">${error.message}</p>
          <p class="text-sm text-slate-400 mt-4">
            Verifique se o backend está rodando e se o endpoint GET /admin/players existe.
          </p>
        </div>
      `;
  }
}

function renderPlayersResult(result) {
  const container = document.getElementById("players-result");
  const items = result.items || [];

  container.innerHTML = `
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-4">
        <div>
          <h3 class="text-lg font-bold">Players encontrados</h3>
          <p class="text-sm text-slate-400">
            Total: ${result.totalItems ?? 0} | Página ${result.page + 1} de ${result.totalPages || 1}
          </p>
        </div>
  
        <div class="flex gap-2">
          <button class="btn-secondary" ${!result.hasPrevious ? "disabled" : ""} onclick="previousPlayerPage()">
            Anterior
          </button>
  
          <button class="btn-secondary" ${!result.hasNext ? "disabled" : ""} onclick="nextPlayerPage()">
            Próxima
          </button>
        </div>
      </div>
  
      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>Player</th>
              <th>Email</th>
              <th>Digitama</th>
              <th>Starter Selected</th>
              <th>Digimon Ativo</th>
              <th>Última Missão</th>
              <th>Criado em</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            ${items.map(renderPlayerRow).join("")}
          </tbody>
        </table>
      </div>
  
      ${items.length === 0 ? renderEmptyPlayers() : ""}
    `;
}

function renderPlayerRow(player) {
  return `
      <tr>
        <td>
          <div class="font-semibold text-cyan-300">${player.username}</div>
          <div class="text-xs text-slate-500 font-mono">${player.id}</div>
        </td>
  
        <td>
          <div class="text-slate-200">${player.email}</div>
        </td>
  
        <td>
          ${player.selectedDigitama ? `<span class="badge">${player.selectedDigitama}</span>` : `<span class="text-slate-500">-</span>`}
        </td>
  
        <td>
          ${player.starterSelected ? `<span class="badge">Sim</span>` : `<span class="badge">Não</span>`}
        </td>
  
        <td>
          ${player.activeDigimonId
      ? `<div class="font-mono text-xs text-slate-300">${player.activeDigimonId}</div>`
      : `<span class="text-slate-500">-</span>`
    }
        </td>
  
        <td>
          ${formatDateTime(player.lastMissionAt)}
        </td>
  
        <td>
          ${formatDateTime(player.createdAt)}
        </td>

        <td>
          <button class="btn-secondary text-xs" onclick='adminResetPassword(${JSON.stringify(player).replace(/'/g, "&#39;")})'>
            Resetar senha
          </button>
        </td>
      </tr>
    `;
}

function renderEmptyPlayers() {
  return `
      <div class="card mt-4">
        <p class="text-slate-400">Nenhum player encontrado com os filtros atuais.</p>
      </div>
    `;
}

function applyPlayerFilters() {
  playerState.username = document.getElementById("player-filter-username").value;
  playerState.email = document.getElementById("player-filter-email").value;
  playerState.selectedDigitama = document.getElementById("player-filter-selected-digitama").value;
  playerState.starterSelected = document.getElementById("player-filter-starter-selected").value;
  playerState.size = Number(document.getElementById("player-filter-size").value);
  playerState.page = 0;

  loadPlayers();
}

function clearPlayerFilters() {
  playerState.page = 0;
  playerState.size = 20;
  playerState.username = "";
  playerState.email = "";
  playerState.selectedDigitama = "";
  playerState.starterSelected = "";

  renderPlayersPage();
}

function previousPlayerPage() {
  if (playerState.page > 0) {
    playerState.page--;
    loadPlayers();
  }
}

function nextPlayerPage() {
  if (playerState.lastResult?.hasNext) {
    playerState.page++;
    loadPlayers();
  }
}

function digitamaOptions(selectedValue) {
  const options = [
    { label: "Todos", value: "" },
    { label: "Starter", value: "STARTER" },
    { label: "Fire", value: "FIRE" },
    { label: "Water", value: "WATER" },
    { label: "Nature", value: "NATURE" }
  ];

  return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
}

function formatDateTime(value) {
  if (!value) {
    return `<span class="text-slate-500">-</span>`;
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return `<span class="text-slate-400">${value}</span>`;
  }

  return `
      <span class="text-slate-300">
        ${date.toLocaleString("pt-BR")}
      </span>
    `;
}

async function adminResetPassword(player) {
  const generateRandom = window.confirm(
    `Gerar senha aleatória para ${player.username}?\n\nClique OK para gerar automática.\nClique Cancelar para digitar uma senha manual.`
  );

  let body = { generateRandom };

  if (!generateRandom) {
    const newPassword = window.prompt(`Nova senha para ${player.username}:`);
    if (newPassword === null) return;
    if (newPassword.length < 8 || newPassword.length > 60) {
      window.alert("A senha deve ter entre 8 e 60 caracteres.");
      return;
    }
    body.newPassword = newPassword;
  }

  try {
    const result = await apiPost(`/admin/players/${player.id}/reset-password`, body);
    showResetPasswordModal(result.username, result.newPassword);
  } catch (error) {
    window.alert(`Erro ao resetar senha: ${error.message}`);
  }
}

function showResetPasswordModal(username, password) {
  const existing = document.getElementById("reset-password-modal");
  if (existing) existing.remove();

  const modal = document.createElement("div");
  modal.id = "reset-password-modal";
  modal.className = "fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4";
  modal.innerHTML = `
      <div class="card w-full max-w-md">
        <h3 class="text-lg font-bold mb-2">Senha redefinida</h3>
        <p class="text-sm text-slate-400 mb-4">Player: <span id="reset-password-username" class="text-cyan-300"></span></p>

        <label class="text-sm text-slate-400">Nova senha</label>
        <div class="flex gap-2 mt-1 mb-4">
          <input id="reset-password-value" type="text" readonly class="input font-mono text-sm flex-1" />
          <button id="reset-password-copy" class="btn-primary whitespace-nowrap">Copiar</button>
        </div>

        <div class="flex justify-end gap-2">
          <button id="reset-password-close" class="btn-secondary">Fechar</button>
        </div>
      </div>
    `;

  document.body.appendChild(modal);

  modal.querySelector("#reset-password-username").textContent = username;
  const input = modal.querySelector("#reset-password-value");
  input.value = password;
  input.focus();
  input.select();

  modal.querySelector("#reset-password-copy").addEventListener("click", async () => {
    try {
      await navigator.clipboard.writeText(input.value);
      window.alert("Senha copiada para a área de transferência.");
    } catch {
      input.select();
      document.execCommand("copy");
      window.alert("Senha selecionada. Use Ctrl+C para copiar.");
    }
  });

  modal.querySelector("#reset-password-close").addEventListener("click", () => modal.remove());

  modal.addEventListener("click", (e) => {
    if (e.target === modal) modal.remove();
  });
}