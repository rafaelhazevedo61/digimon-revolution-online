const ARENA_STAGE_LABELS = {
  BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega"
};

function arenaWinChanceColor(chance) {
  return chance >= 60 ? "text-green-400" : chance >= 40 ? "text-yellow-400" : "text-red-400";
}

async function renderArenaPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold px-1">Arena</h2>
        <div class="flex gap-3">
          <button class="text-xs text-cyan-400 hover:text-cyan-300" onclick="navigateTo('arena-ranking')">Ranking</button>
          <button class="text-xs text-cyan-400 hover:text-cyan-300" onclick="navigateTo('arena-history')">Historico</button>
        </div>
      </div>
      <div id="arena-lobby">
        <div class="card animate-pulse mb-3"><div class="h-20"></div></div>
        <div class="card animate-pulse mb-3"><div class="h-24"></div></div>
      </div>
    </div>
  `;

  try {
    const lobby = await apiGet("/arena/lobby");
    renderArenaLobby(lobby);
  } catch (err) {
    document.getElementById("arena-lobby").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function renderArenaLobby(lobby) {
  const container = document.getElementById("arena-lobby");
  const total = lobby.wins + lobby.losses;
  const winRate = total > 0 ? Math.round((lobby.wins / total) * 100) : 0;

  const myCard = `
    <div class="card mb-4">
      <div class="flex items-center justify-between mb-2">
        <span class="font-bold text-sm">${escapeHtml(lobby.digimonName)}</span>
        <span class="text-lg font-bold text-cyan-400">${lobby.rating} <span class="text-xs text-slate-400">pts</span></span>
      </div>
      <div class="flex items-center gap-4 text-xs text-slate-300">
        <span class="text-green-400">${lobby.wins}V</span>
        <span class="text-red-400">${lobby.losses}D</span>
        <span class="text-slate-400">${winRate}% vitorias</span>
        <span class="text-purple-300">Poder ${lobby.power}</span>
      </div>
      <div class="mt-2 text-xs text-slate-400">Energia: ${lobby.energy} (custo ${lobby.energyCost} por desafio)</div>
    </div>
  `;

  if (!lobby.opponents || lobby.opponents.length === 0) {
    container.innerHTML = myCard + `
      <div class="card text-center text-slate-400 text-sm">Nenhum oponente disponivel no momento</div>
    `;
    return;
  }

  const opponentsHtml = lobby.opponents.map(o => {
    const canFight = lobby.energy >= lobby.energyCost;
    return `
      <div class="card-sm mb-2 flex items-center gap-3">
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <span class="font-bold text-sm truncate">${escapeHtml(o.digimonName)}</span>
            <span class="text-[10px] text-slate-500">${escapeHtml(ARENA_STAGE_LABELS[o.stage] || o.stage)} Lv.${o.level}</span>
          </div>
          <div class="text-xs text-slate-400 mt-0.5">
            <span class="text-slate-300">@${escapeHtml(o.playerName)}</span>
            <span class="ml-2 text-cyan-400">${o.rating} pts</span>
            <span class="ml-2 text-purple-300">Poder ${o.power}</span>
          </div>
          <div class="text-xs mt-0.5">
            <span class="${arenaWinChanceColor(o.winChance)}">Chance: ${o.winChance}%</span>
          </div>
        </div>
        <button
          class="px-3 py-2 rounded-lg text-xs font-bold ${canFight ? "btn-primary" : "bg-slate-700 text-slate-400 cursor-not-allowed"}"
          ${canFight ? `onclick="startArenaChallenge('${o.digimonId}', '${escapeHtml(o.digimonName).replace(/'/g, "\\'")}')"` : "disabled"}>
          Desafiar
        </button>
      </div>
    `;
  }).join("");

  container.innerHTML = myCard + `
    <p class="text-xs text-slate-400 mb-2 px-1">Oponentes proximos do seu rating</p>
    ${opponentsHtml}
    ${lobby.energy < lobby.energyCost ? `<p class="text-xs text-red-400 mt-2 px-1">Energia insuficiente para desafiar (recarrega com o tempo)</p>` : ""}
  `;
}

async function startArenaChallenge(opponentDigimonId, opponentName) {
  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="page-container flex flex-col items-center justify-center min-h-[60vh]">
      <div class="text-4xl mb-4 animate-bounce">⚔️</div>
      <p class="text-lg font-bold mb-2">Duelo em andamento...</p>
      <p class="text-sm text-slate-400">vs ${escapeHtml(opponentName || "")}</p>
    </div>
  `;

  try {
    const result = await apiPost("/arena/challenge", { opponentDigimonId });
    renderArenaResult(result);
  } catch (err) {
    app.innerHTML = `
      <div class="page-container">
        <div class="card border-red-900 mb-4"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>
        <button class="btn-primary w-full" onclick="renderArenaPage()">Voltar</button>
      </div>
    `;
  }
}

function renderArenaResult(result) {
  const app = document.getElementById("app");
  const victory = result.victory;
  const change = result.ratingChange;
  const changeStr = change >= 0 ? `+${change}` : `${change}`;

  app.innerHTML = `
    <div class="page-container flex flex-col items-center">
      <div class="text-5xl mb-3 mt-6">${victory ? "🏆" : "💀"}</div>
      <h2 class="text-xl font-bold mb-1 ${victory ? "text-yellow-400" : "text-red-400"}">
        ${victory ? "Vitoria!" : "Derrota"}
      </h2>
      <p class="text-sm text-slate-400 mb-4">vs ${escapeHtml(result.opponentName)}</p>

      <div class="card-sm w-full mb-3">
        <div class="grid grid-cols-2 gap-3 text-center text-sm">
          <div>
            <span class="text-slate-400 text-xs">Chance</span><br>
            <span class="font-bold ${arenaWinChanceColor(result.winChance)}">${result.winChance}%</span>
          </div>
          <div>
            <span class="text-slate-400 text-xs">Poder</span><br>
            <span class="font-bold text-cyan-400">${Math.round(result.attackerPower)}</span>
            <span class="text-slate-500 text-xs">vs</span>
            <span class="font-bold text-red-400">${Math.round(result.defenderPower)}</span>
          </div>
        </div>
      </div>

      <div class="card-sm w-full mb-3">
        <div class="flex items-center justify-between text-sm">
          <span class="text-slate-400 text-xs">Rating</span>
          <span class="font-bold ${change >= 0 ? "text-green-400" : "text-red-400"}">${changeStr} → ${result.newRating} pts</span>
        </div>
        ${result.bitsGained > 0 ? `<div class="flex items-center justify-between text-sm mt-1">
          <span class="text-slate-400 text-xs">Recompensa</span>
          <span class="text-amber-400 font-bold">+${result.bitsGained} Bits</span>
        </div>` : ""}
      </div>

      <div class="flex gap-2 w-full">
        <button class="btn-primary flex-1" onclick="navigateTo('arena')">Voltar a Arena</button>
        <button class="flex-1 px-4 py-2.5 rounded-xl text-sm font-bold bg-slate-700 hover:bg-slate-600 transition-colors" onclick="navigateTo('arena-ranking')">
          Ver Ranking
        </button>
      </div>
    </div>
  `;
}

async function renderArenaRankingPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold px-1">Ranking da Arena</h2>
        <button class="text-sm text-cyan-400" onclick="navigateTo('arena')">Voltar</button>
      </div>
      <div id="arena-ranking-list">
        <div class="card animate-pulse mb-3"><div class="h-16"></div></div>
      </div>
    </div>
  `;

  try {
    const ranking = await apiGet("/arena/ranking", { page: 0, size: 50 });
    const container = document.getElementById("arena-ranking-list");
    const myId = getPlayerId();

    if (!ranking || ranking.length === 0) {
      container.innerHTML = `<div class="card text-center text-slate-400 text-sm">Ranking vazio</div>`;
      return;
    }

    container.innerHTML = ranking.map(e => {
      const mine = myId && e.playerId === myId;
      const medal = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `#${e.position}`;
      return `
        <div class="card-sm mb-2 flex items-center gap-3 ${mine ? "border-cyan-500" : ""}">
          <span class="w-8 text-center text-sm font-bold">${medal}</span>
          <div class="flex-1 min-w-0">
            <span class="font-bold text-sm">${escapeHtml(e.digimonName)}</span>
            ${mine ? `<span class="ml-1 text-[10px] text-cyan-400">(voce)</span>` : ""}
            <div class="text-xs text-slate-400 mt-0.5">
              @${escapeHtml(e.playerName)} · ${escapeHtml(ARENA_STAGE_LABELS[e.stage] || e.stage)} Lv.${e.level}
              · <span class="text-green-400">${e.wins}V</span>/<span class="text-red-400">${e.losses}D</span>
            </div>
          </div>
          <span class="text-cyan-400 font-bold text-sm">${e.rating}</span>
        </div>
      `;
    }).join("");
  } catch (err) {
    document.getElementById("arena-ranking-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>
    `;
  }
}

async function renderArenaHistoryPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-bold px-1">Historico da Arena</h2>
        <button class="text-sm text-cyan-400" onclick="navigateTo('arena')">Voltar</button>
      </div>
      <div id="arena-history-list">
        <div class="card animate-pulse mb-3"><div class="h-16"></div></div>
      </div>
    </div>
  `;

  try {
    const history = await apiGet("/arena/history", { page: 0, size: 30 });
    const container = document.getElementById("arena-history-list");

    if (!history || history.length === 0) {
      container.innerHTML = `<div class="card text-center text-slate-400 text-sm">Nenhuma partida registrada</div>`;
      return;
    }

    container.innerHTML = history.map(m => {
      const date = new Date(m.createdAt);
      const dateStr = date.toLocaleDateString("pt-BR") + " " + date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
      const changeStr = m.ratingChange >= 0 ? `+${m.ratingChange}` : `${m.ratingChange}`;
      return `
        <div class="card-sm mb-2 flex items-center gap-3">
          <span class="text-xl">${m.won ? "🏆" : "💀"}</span>
          <div class="flex-1">
            <span class="font-bold text-sm">${m.attacker ? "" : "🛡️ "}${escapeHtml(m.opponentName)}</span>
            <span class="ml-2 text-xs ${m.won ? "text-green-400" : "text-red-400"}">${m.won ? "Vitoria" : "Derrota"}</span>
            <div class="text-xs text-slate-400 mt-0.5">
              Poder ${m.myPower} vs ${m.opponentPower}
              · <span class="${m.ratingChange >= 0 ? "text-green-400" : "text-red-400"}">${changeStr} pts</span>
              ${m.bitsGained > 0 ? `· <span class="text-amber-400">+${m.bitsGained} Bits</span>` : ""}
            </div>
          </div>
          <span class="text-[10px] text-slate-500">${dateStr}</span>
        </div>
      `;
    }).join("");
  } catch (err) {
    document.getElementById("arena-history-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>
    `;
  }
}
