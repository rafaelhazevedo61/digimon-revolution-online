const ARENA_STAGE_LABELS = {
  BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega"
};

const ARENA_MAX_STACK_QUANTITY = 999;

let arenaShopModalUnitPrice = 0;
let arenaShopModalBalance = 0;

function arenaWinChanceColor(chance) {
  return chance >= 60 ? "text-green-400" : chance >= 40 ? "text-yellow-400" : "text-red-400";
}

const ARENA_TIER_STYLES = {
  Bronze: "bg-amber-900/60 text-amber-300 border border-amber-700",
  Prata: "bg-slate-500/40 text-slate-100 border border-slate-400",
  Ouro: "bg-yellow-700/50 text-yellow-300 border border-yellow-500",
  Platina: "bg-cyan-800/50 text-cyan-200 border border-cyan-500",
  Diamante: "bg-fuchsia-800/50 text-fuchsia-200 border border-fuchsia-400"
};

function arenaTierBadge(tier) {
  if (!tier) return "";
  const cls = ARENA_TIER_STYLES[tier] || "bg-slate-600 text-slate-200";
  return `<span class="text-[10px] font-bold px-1.5 py-0.5 rounded ${cls}">${escapeHtml(tier)}</span>`;
}

function arenaFormatCooldown(seconds) {
  if (seconds <= 0) return "";
  const min = Math.floor(seconds / 60);
  const sec = seconds % 60;
  return min > 0 ? `${min}min` : `${sec}s`;
}

async function renderArenaPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container arena-page-container">
      <header class="arena-page-header">
        <div class="arena-page-heading">
          <p class="arena-eyebrow">Competição · PvP</p>
          <h1 class="arena-page-title">Arena</h1>
          <p class="arena-page-subtitle">Escolha seu próximo confronto, proteja sua pontuação e avance de tier.</p>
        </div>
        <nav class="arena-page-nav" aria-label="Navegação da Arena">
          <button type="button" class="arena-nav-link is-active" aria-current="page">Lobby</button>
          <button type="button" class="arena-nav-link" onclick="navigateTo('arena-ranking')">Classificação</button>
          <button type="button" class="arena-nav-link" onclick="navigateTo('arena-history')">Histórico</button>
          <button type="button" class="arena-nav-link" onclick="navigateTo('arena-shop')">Loja</button>
        </nav>
      </header>
      <div id="arena-lobby">
        <div class="arena-profile-card arena-loading-card"><div class="arena-loading-bar"></div><div class="arena-loading-bar short"></div></div>
        <div class="arena-loading-card"><div class="arena-loading-bar"></div><div class="arena-loading-bar"></div></div>
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
  const total = Number(lobby.wins || 0) + Number(lobby.losses || 0);
  const winRate = total > 0 ? Math.round((Number(lobby.wins || 0) / total) * 100) : 0;
  const dailyLimitReached = lobby.dailyChallengeLimit > 0 && lobby.challengesRemaining <= 0;

  const myCard = `
    <section class="arena-profile-card">
      <div class="arena-profile-topline">
        <div class="arena-profile-identity">
          <div class="arena-profile-avatar" aria-hidden="true">⚔</div>
          <div class="arena-profile-copy">
            <p class="arena-eyebrow arena-eyebrow-cyan">Seu competidor</p>
            <div class="arena-profile-name-row"><h2>${escapeHtml(lobby.digimonName)}</h2>${arenaTierBadge(lobby.tier)}</div>
            <p class="arena-profile-caption">${lobby.nextTier ? `Faltam ${lobby.pointsToNextTier} pts para ${escapeHtml(lobby.nextTier)}` : "Tier máximo alcançado"}</p>
          </div>
        </div>
        <div class="arena-rating-block"><span>Rating atual</span><strong>${Number(lobby.rating || 0).toLocaleString("pt-BR")}</strong><small>pontos</small></div>
      </div>
      <div class="arena-profile-metrics">
        <div class="arena-profile-metric"><span>Vitórias</span><strong class="is-positive">${Number(lobby.wins || 0)}</strong><small>partidas ganhas</small></div>
        <div class="arena-profile-metric"><span>Derrotas</span><strong class="is-negative">${Number(lobby.losses || 0)}</strong><small>partidas perdidas</small></div>
        <div class="arena-profile-metric"><span>Aproveitamento</span><strong>${winRate}%</strong><small>taxa de vitória</small></div>
        <div class="arena-profile-metric"><span>Poder de combate</span><strong>${Number(lobby.power || 0).toLocaleString("pt-BR")}</strong><small>força total</small></div>
      </div>
    </section>
  `;

  const resourcesPanel = `
    <aside class="arena-lobby-sidebar">
      <section class="arena-resource-card arena-energy-card">
        <div class="arena-sidebar-heading"><div><p class="arena-eyebrow arena-eyebrow-cyan">Recurso de combate</p><h3>Energia</h3></div><span class="arena-sidebar-glyph">◈</span></div>
        <div class="arena-energy-value"><strong>${Number(lobby.energy || 0)}</strong><span>unidades</span></div>
        <p>Cada desafio consome ${Number(lobby.energyCost || 0)} de energia. A recarga acontece com o tempo.</p>
      </section>
      <section class="arena-resource-card">
        <div class="arena-sidebar-heading"><div><p class="arena-eyebrow">Recursos</p><h3>Temporada atual</h3></div><span class="arena-sidebar-glyph is-amber">✦</span></div>
        <div class="arena-resource-grid"><div><span>Moedas de Arena</span><strong>${Number(lobby.arenaCoins || 0).toLocaleString("pt-BR")}</strong></div><div><span>Desafios hoje</span><strong class="${dailyLimitReached ? "is-negative" : ""}">${Number(lobby.challengesUsedToday || 0)}/${Number(lobby.dailyChallengeLimit || 0)}</strong></div></div>
        <p>${dailyLimitReached ? "Limite diário atingido. Volte amanhã para novos desafios." : `${Number(lobby.challengesRemaining || 0)} desafios disponíveis neste ciclo.`}</p>
      </section>
      <section class="arena-resource-card arena-tip-card"><p class="arena-eyebrow">Como avançar</p><h3>Escolha confrontos equilibrados</h3><p>Oponentes próximos do seu stage e rating oferecem uma disputa mais justa e ajudam a proteger sua sequência.</p></section>
    </aside>
  `;

  if (!lobby.opponents || lobby.opponents.length === 0) {
    container.innerHTML = myCard + `<div class="arena-lobby-layout"><section class="arena-opponents-section"><div class="arena-section-heading"><div><p class="arena-eyebrow">Fila de confrontos</p><h2>Nenhum oponente disponível</h2></div></div><div class="arena-empty-state"><span>◎</span><p>Nenhum oponente foi encontrado no seu intervalo de stage e rating.</p></div></section>${resourcesPanel}</div>`;
    return;
  }

  const opponentsHtml = lobby.opponents.map(o => {
    const onCooldown = (o.cooldownSecondsRemaining || 0) > 0;
    const canFight = lobby.energy >= lobby.energyCost && !dailyLimitReached && !onCooldown;
    let btnLabel = "Desafiar";
    if (onCooldown) btnLabel = `Aguarde ${arenaFormatCooldown(o.cooldownSecondsRemaining)}`;
    else if (dailyLimitReached) btnLabel = "Limite diário";
    return `
      <article class="arena-opponent-card ${onCooldown ? "is-cooldown" : ""}">
        <div class="arena-opponent-heading">
          <div class="arena-opponent-identity"><div class="arena-opponent-avatar" aria-hidden="true">⚡</div><div class="min-w-0"><div class="arena-opponent-name-row"><h3>${escapeHtml(o.digimonName)}</h3>${o.bot ? `<span class="arena-opponent-badge is-bot">BOT</span>` : ""}${arenaTierBadge(o.tier)}</div><p>@${escapeHtml(o.playerName)}</p></div></div>
          <strong class="arena-opponent-rating">${Number(o.rating || 0).toLocaleString("pt-BR")}<small>pts</small></strong>
        </div>
        <div class="arena-opponent-compact-meta"><span>${escapeHtml(ARENA_STAGE_LABELS[o.stage] || o.stage)}</span><span>Lv. ${o.level}</span><span>Poder ${Number(o.power || 0).toLocaleString("pt-BR")}</span><strong class="${arenaWinChanceColor(o.winChance)}">Chance ${o.winChance}%</strong></div>
        <div class="arena-opponent-footer"><span class="arena-opponent-reward"><strong>+${Number(o.bitsReward || 0).toLocaleString("pt-BR")} Bits</strong></span>${onCooldown ? `<span class="arena-opponent-cooldown">Aguarde ${arenaFormatCooldown(o.cooldownSecondsRemaining)}</span>` : ""}<button type="button" class="arena-challenge-button ${canFight ? "" : "is-disabled"}" ${canFight ? `data-arena-opponent-id="${escapeAttr(o.digimonId)}" data-arena-opponent-name="${escapeAttr(o.digimonName)}"` : "disabled"}>${btnLabel}</button></div>
      </article>
    `;
  }).join("");

  container.innerHTML = `${myCard}<div class="arena-lobby-layout"><section class="arena-opponents-section"><div class="arena-section-heading"><div><p class="arena-eyebrow">Fila de confrontos</p><h2>Escolha seu próximo desafio</h2><p>Oponentes no mesmo stage ou adjacente e dentro de ±200 pts.</p></div><span class="arena-opponent-count">${lobby.opponents.length} opções</span></div>${dailyLimitReached ? `<div class="arena-inline-alert is-danger">Limite diário de desafios atingido. Volte amanhã para continuar.</div>` : lobby.energy < lobby.energyCost ? `<div class="arena-inline-alert is-danger">Energia insuficiente para iniciar um novo desafio.</div>` : ""}<div class="arena-opponents-grid">${opponentsHtml}</div></section>${resourcesPanel}</div>`;
  container.querySelectorAll("[data-arena-opponent-id]").forEach(button => {
    button.addEventListener("click", () => {
      startArenaChallenge(button.dataset.arenaOpponentId, button.dataset.arenaOpponentName);
    });
  });
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
          <span class="font-bold ${change >= 0 ? "text-green-400" : "text-red-400"}">${changeStr} → ${result.newRating} pts ${arenaTierBadge(result.tier)}</span>
        </div>
        ${result.bitsGained > 0 ? `<div class="flex items-center justify-between text-sm mt-1">
          <span class="text-slate-400 text-xs">Recompensa</span>
          <span class="text-amber-400 font-bold">+${result.bitsGained} Bits</span>
        </div>` : ""}
        ${result.arenaCoinsGained > 0 ? `<div class="flex items-center justify-between text-sm mt-1">
          <span class="text-slate-400 text-xs">Moedas de Arena</span>
          <span class="text-amber-300 font-bold">🪙 +${result.arenaCoinsGained} (${result.arenaCoinsBalance})</span>
        </div>` : ""}
        ${victory && result.rewardChestName ? `<div class="flex items-center justify-between text-sm mt-2 pt-2 border-t border-slate-700">
          <span class="text-slate-400 text-xs">Baú recebido</span>
          <span class="text-cyan-300 font-bold">${escapeHtml(result.rewardChestName)}</span>
        </div>` : ""}
      </div>

      <div class="flex gap-2 w-full">
        <button class="btn-primary flex-1" onclick="navigateTo('arena')">Voltar a Arena</button>
        <button class="flex-1 px-4 py-2.5 rounded-xl text-sm font-bold bg-slate-700 hover:bg-slate-600 transition-colors" onclick="navigateTo('arena-ranking')">
          Ver Classificação
        </button>
      </div>
    </div>
  `;
}

async function renderArenaRankingPage(mode = "current") {
  const seasonMode = mode === "season";
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container arena-page-container arena-subpage">
      <header class="arena-page-header">
        <div class="arena-page-heading"><p class="arena-eyebrow">Competição · PvP</p><h1 class="arena-page-title">Classificação</h1><p class="arena-page-subtitle">Compare sua evolução com os melhores competidores da Arena.</p></div>
        <nav class="arena-page-nav" aria-label="Navegação da Arena"><button type="button" class="arena-nav-link" onclick="navigateTo('arena')">Lobby</button><button type="button" class="arena-nav-link is-active" aria-current="page">Classificação</button><button type="button" class="arena-nav-link" onclick="navigateTo('arena-history')">Histórico</button><button type="button" class="arena-nav-link" onclick="navigateTo('arena-shop')">Loja</button></nav>
      </header>
      <section class="arena-subpage-panel">
        <div class="arena-subpage-heading"><div><p class="arena-eyebrow arena-eyebrow-cyan">Tabela de líderes</p><h2>${seasonMode ? "Ranking da temporada" : "Ranking atual"}</h2><p>${seasonMode ? "Temporada experimental: 01/08/2026 – 31/12/2026" : "Classificação pela pontuação atual de cada Digimon ativo ou armazenado."}</p></div><span class="arena-subpage-icon">✦</span></div>
        <div class="arena-mode-tabs" role="tablist" aria-label="Tipo de classificação"><button type="button" class="${seasonMode ? "" : "is-active"}" role="tab" aria-selected="${!seasonMode}" onclick="renderArenaRankingPage('current')">Atual</button><button type="button" class="${seasonMode ? "is-active" : ""}" role="tab" aria-selected="${seasonMode}" onclick="renderArenaRankingPage('season')">Temporada</button></div>
        <div id="arena-player-history" class="arena-ranking-summary ${seasonMode ? "hidden" : ""}"></div>
        <div id="arena-ranking-list"><div class="arena-ranking-row arena-loading-row"><div class="arena-loading-bar"></div><div class="arena-loading-bar short"></div></div></div>
      </section>
    </div>
  `;

  try {
    const [ranking, statistics] = await Promise.all([
      apiGet(seasonMode ? "/arena/season-ranking" : "/arena/ranking", { page: 0, size: 50 }),
      seasonMode ? Promise.resolve(null) : apiGet("/arena/statistics").catch(() => null)
    ]);
    const history = document.getElementById("arena-player-history");
    if (history && statistics) {
      history.classList.remove("hidden");
      history.innerHTML = `<div class="arena-summary-title">Seu histórico de arena</div><div class="arena-summary-grid"><div><span>Saldo</span><strong class="${statistics.netPoints >= 0 ? "is-positive" : "is-negative"}">${Number(statistics.netPoints || 0).toLocaleString("pt-BR")}</strong></div><div><span>Ganhos</span><strong>${Number(statistics.pointsWon || 0).toLocaleString("pt-BR")}</strong></div><div><span>Perdas</span><strong class="is-negative">${Number(statistics.pointsLost || 0).toLocaleString("pt-BR")}</strong></div></div>`;
    }
    const container = document.getElementById("arena-ranking-list");
    const myId = getPlayerId();
    if (!ranking || ranking.length === 0) { container.innerHTML = `<div class="arena-empty-state"><span>◎</span><p>Classificação vazia no momento.</p></div>`; return; }
    container.innerHTML = ranking.map(e => {
      const mine = myId && e.playerId === myId;
      const medal = e.position === 1 ? "🥇" : e.position === 2 ? "🥈" : e.position === 3 ? "🥉" : `#${e.position}`;
      if (seasonMode) return `<article class="arena-ranking-row ${mine ? "is-mine" : ""}"><span class="arena-ranking-position">${medal}</span><div class="arena-ranking-identity"><strong>@${escapeHtml(e.playerName)}</strong>${mine ? `<span class="arena-you-badge">você</span>` : ""}<p><span class="is-positive">${e.wins}V</span> / <span class="is-negative">${e.losses}D</span> · ganhos ${Number(e.pointsWon || 0).toLocaleString("pt-BR")} · perdas ${Number(e.pointsLost || 0).toLocaleString("pt-BR")}</p></div><strong class="arena-ranking-score">${Number(e.netPoints || 0).toLocaleString("pt-BR")}<small>pts</small></strong></article>`;
      return `<article class="arena-ranking-row ${mine ? "is-mine" : ""}"><span class="arena-ranking-position">${medal}</span><div class="arena-ranking-identity"><div class="arena-ranking-name-row"><strong>${escapeHtml(e.digimonName)}</strong>${arenaTierBadge(e.tier)}${mine ? `<span class="arena-you-badge">você</span>` : ""}</div><p>@${escapeHtml(e.playerName)} · ${escapeHtml(ARENA_STAGE_LABELS[e.stage] || e.stage)} Lv.${e.level} · <span class="is-positive">${e.wins}V</span> / <span class="is-negative">${e.losses}D</span></p></div><strong class="arena-ranking-score">${Number(e.rating || 0).toLocaleString("pt-BR")}<small>pts</small></strong></article>`;
    }).join("");
  } catch (err) {
    document.getElementById("arena-ranking-list").innerHTML = `<div class="card border-red-900"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>`;
  }
}

async function renderArenaHistoryPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container arena-page-container arena-subpage">
      <header class="arena-page-header">
        <div class="arena-page-heading"><p class="arena-eyebrow">Competição · PvP</p><h1 class="arena-page-title">Histórico</h1><p class="arena-page-subtitle">Acompanhe seus confrontos, variações de rating e recompensas.</p></div>
        <nav class="arena-page-nav" aria-label="Navegação da Arena"><button type="button" class="arena-nav-link" onclick="navigateTo('arena')">Lobby</button><button type="button" class="arena-nav-link" onclick="navigateTo('arena-ranking')">Classificação</button><button type="button" class="arena-nav-link is-active" aria-current="page">Histórico</button><button type="button" class="arena-nav-link" onclick="navigateTo('arena-shop')">Loja</button></nav>
      </header>
      <section class="arena-subpage-panel"><div class="arena-subpage-heading"><div><p class="arena-eyebrow arena-eyebrow-cyan">Registro de partidas</p><h2>Seus últimos confrontos</h2><p>Vitórias, derrotas e recompensas recebidas nas batalhas recentes.</p></div><span class="arena-subpage-icon">◷</span></div><div id="arena-history-list"><div class="arena-history-row arena-loading-row"><div class="arena-loading-bar"></div><div class="arena-loading-bar short"></div></div></div></section>
    </div>
  `;

  try {
    const history = await apiGet("/arena/history", { page: 0, size: 30 });
    const container = document.getElementById("arena-history-list");
    if (!history || history.length === 0) { container.innerHTML = `<div class="arena-empty-state"><span>◷</span><p>Nenhuma partida registrada.</p></div>`; return; }
    container.innerHTML = history.map(m => {
      const date = new Date(m.createdAt);
      const dateStr = date.toLocaleDateString("pt-BR") + " · " + date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
      const changeStr = m.ratingChange >= 0 ? `+${m.ratingChange}` : `${m.ratingChange}`;
      return `<article class="arena-history-row"><div class="arena-history-result ${m.won ? "is-win" : "is-loss"}">${m.won ? "V" : "D"}</div><div class="arena-history-opponent"><div><strong>${escapeHtml(m.opponentName)}</strong><span class="arena-history-outcome ${m.won ? "is-win" : "is-loss"}">${m.won ? "Vitória" : "Derrota"}</span></div><p>${m.attacker ? "Desafio iniciado por você" : "Você foi desafiado"}</p></div><div class="arena-history-power"><span>Poder</span><strong>${Number(m.myPower || 0).toLocaleString("pt-BR")} <small>vs</small> ${Number(m.opponentPower || 0).toLocaleString("pt-BR")}</strong></div><div class="arena-history-change ${m.ratingChange >= 0 ? "is-win" : "is-loss"}"><span>Rating</span><strong>${changeStr} pts</strong>${m.bitsGained > 0 ? `<small>+${Number(m.bitsGained).toLocaleString("pt-BR")} Bits</small>` : ""}</div><time>${dateStr}</time></article>`;
    }).join("");
  } catch (err) {
    document.getElementById("arena-history-list").innerHTML = `<div class="card border-red-900"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>`;
  }
}

async function renderArenaShopPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container arena-page-container arena-subpage">
      <header class="arena-page-header">
        <div class="arena-page-heading"><p class="arena-eyebrow">Competição · PvP</p><h1 class="arena-page-title">Loja</h1><p class="arena-page-subtitle">Troque suas moedas de Arena por itens especiais da temporada.</p></div>
        <nav class="arena-page-nav" aria-label="Navegação da Arena"><button type="button" class="arena-nav-link" onclick="navigateTo('arena')">Lobby</button><button type="button" class="arena-nav-link" onclick="navigateTo('arena-ranking')">Classificação</button><button type="button" class="arena-nav-link" onclick="navigateTo('arena-history')">Histórico</button><button type="button" class="arena-nav-link is-active" aria-current="page">Loja</button></nav>
      </header>
      <section class="arena-subpage-panel"><div class="arena-subpage-heading"><div><p class="arena-eyebrow arena-eyebrow-cyan">Recompensas da temporada</p><h2>Itens disponíveis</h2><p>Ganhe moedas lutando e use-as para resgatar itens.</p></div><span class="arena-subpage-icon">✦</span></div><div id="arena-shop-content"><div class="arena-shop-loading arena-loading-row"><div class="arena-loading-bar"></div><div class="arena-loading-bar short"></div></div></div></section>
    </div>
  `;

  try {
    const [shop, inventory] = await Promise.all([
      apiGet("/arena/shop"),
      apiGet("/inventory")
    ]);
    renderArenaShop(shop, inventory || []);
  } catch (err) {
    document.getElementById("arena-shop-content").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300 text-sm">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function renderArenaShop(shop, inventoryItems = []) {
  const container = document.getElementById("arena-shop-content");
  const coins = shop.arenaCoins || 0;

  if (!shop.products || shop.products.length === 0) {
    container.innerHTML = `<div class="arena-shop-balance"><span><span class="arena-eyebrow">Seu saldo</span><strong>🪙 ${Number(coins).toLocaleString("pt-BR")} <small>Moedas de Arena</small></strong></span></div><div class="arena-empty-state"><span>✦</span><p>Nenhum item disponível nesta temporada.</p></div>`;
    return;
  }

  const productsHtml = shop.products.map(p => {
    const inventoryQuantity = arenaShopInventoryQuantity(p.itemType, inventoryItems);
    const maxQty = arenaShopMaxPurchaseQuantity(p, coins, inventoryQuantity);
    const canBuy = maxQty >= 1;
    const unavailableLabel = coins < p.priceCoins ? "Sem saldo" : "Limite atingido";
    return `<article class="arena-shop-card"><div class="arena-shop-icon">✦</div><div class="arena-shop-copy"><h3>${escapeHtml(p.name)}${p.quantity > 1 ? `<small>×${p.quantity}</small>` : ""}</h3></div><div class="arena-shop-purchase"><strong>🪙 ${Number(p.priceCoins).toLocaleString("pt-BR")}</strong><button type="button" class="arena-shop-button ${canBuy ? "" : "is-disabled"}" ${canBuy ? `data-arena-shop-code="${escapeAttr(p.code)}" data-arena-shop-name="${escapeAttr(p.name)}" data-arena-shop-price="${p.priceCoins}" data-arena-shop-inventory="${inventoryQuantity}" data-arena-shop-quantity="${p.quantity}"` : "disabled"}>${canBuy ? "Comprar" : unavailableLabel}</button></div></article>`;
  }).join("");

  container.innerHTML = `<div class="arena-shop-balance"><span><span class="arena-eyebrow">Seu saldo</span><strong>🪙 ${Number(coins).toLocaleString("pt-BR")} <small>Moedas de Arena</small></strong></span><p>As moedas são obtidas em vitórias e derrotas na Arena.</p></div><div class="arena-shop-grid">${productsHtml}</div>`;
  container.querySelectorAll("[data-arena-shop-code]").forEach(button => {
    button.addEventListener("click", () => {
      openArenaShopPurchase(
        button.dataset.arenaShopCode,
        button.dataset.arenaShopName,
        Number(button.dataset.arenaShopPrice || 0),
        coins,
        Number(button.dataset.arenaShopInventory || 0),
        Number(button.dataset.arenaShopQuantity || 1)
      );
    });
  });
}

function arenaShopInventoryQuantity(itemType, inventoryItems = []) {
  const targetType = String(itemType || "");
  return inventoryItems
    .filter(item => {
      const definitionCode = item.itemDefinition && item.itemDefinition.code;
      return definitionCode === targetType || (!definitionCode && item.itemType === targetType);
    })
    .reduce((total, item) => total + Math.max(0, Number(item.quantity) || 0), 0);
}

function arenaShopMaxPurchaseQuantity(product, balance, inventoryQuantity) {
  const productQuantity = Math.max(1, Number(product.quantity) || 1);
  const remainingStackQuantity = Math.max(0, ARENA_MAX_STACK_QUANTITY - Math.max(0, Number(inventoryQuantity) || 0));
  const stackPurchaseQuantity = Math.floor(remainingStackQuantity / productQuantity);
  const affordableQuantity = product.priceCoins > 0
    ? Math.floor(Math.max(0, Number(balance) || 0) / product.priceCoins)
    : ARENA_MAX_STACK_QUANTITY;
  return Math.max(0, Math.min(ARENA_MAX_STACK_QUANTITY, stackPurchaseQuantity, affordableQuantity));
}

function openArenaShopPurchase(productCode, productName, priceCoins, balance, inventoryQuantity = 0, productQuantity = 1) {
  arenaShopModalUnitPrice = Number(priceCoins || 0);
  arenaShopModalBalance = Number(balance || 0);
  const product = { quantity: productQuantity, priceCoins: Number(priceCoins || 0) };
  const maxQty = arenaShopMaxPurchaseQuantity(product, balance, inventoryQuantity);
  const remainingStackQuantity = Math.max(0, ARENA_MAX_STACK_QUANTITY - Math.max(0, Number(inventoryQuantity) || 0));
  if (maxQty < 1) return;
  const overlay = document.createElement("div");
  overlay.id = "arena-shop-modal";
  overlay.className = "shop-modal-overlay";
  overlay.innerHTML = `<div class="shop-modal arena-shop-confirm-modal" role="dialog" aria-modal="true" aria-labelledby="arena-shop-modal-title" onclick="event.stopPropagation()"><div class="arena-shop-confirm-heading"><div><p class="arena-eyebrow arena-eyebrow-amber">Comprar item</p><h3 id="arena-shop-modal-title">${escapeHtml(productName)}</h3></div><button type="button" class="arena-shop-modal-close" aria-label="Fechar" onclick="closeArenaShopPurchase()">×</button></div><div class="arena-shop-quantity-row"><button type="button" class="arena-shop-quantity-button" onclick="arenaShopQtyChange(-1)" aria-label="Diminuir quantidade">−</button><input id="arena-shop-qty" class="arena-shop-quantity-input" type="number" value="1" min="1" max="${maxQty}" inputmode="numeric" oninput="arenaShopQtyUpdate()"><button type="button" class="arena-shop-quantity-button" onclick="arenaShopQtyChange(1)" aria-label="Aumentar quantidade">+</button><button type="button" class="arena-shop-max-button" onclick="arenaShopQtyMax()">MAX</button></div><p class="arena-shop-quantity-hint">No inventário: ${Number(inventoryQuantity).toLocaleString("pt-BR")} / ${ARENA_MAX_STACK_QUANTITY} · Restante: ${remainingStackQuantity.toLocaleString("pt-BR")}</p><div class="arena-shop-total-row"><span><small>Preço unitário</small><strong>🪙 ${Number(priceCoins).toLocaleString("pt-BR")}</strong></span><span><small>Total</small><strong id="arena-shop-total">🪙 ${Number(priceCoins).toLocaleString("pt-BR")}</strong></span><span><small>Saldo após</small><strong id="arena-shop-remaining">🪙 ${Number(balance - priceCoins).toLocaleString("pt-BR")}</strong></span></div><div class="arena-shop-modal-actions"><button type="button" class="arena-shop-modal-buy" onclick="confirmArenaShopQuantity('${escapeAttr(productCode)}','${escapeAttr(productName)}',${Number(priceCoins)})">Comprar</button><button type="button" class="arena-shop-modal-cancel" onclick="closeArenaShopPurchase()">Cancelar</button></div></div>`;
  overlay.addEventListener("click", event => { if (event.target === overlay) closeArenaShopPurchase(); });
  document.body.appendChild(overlay);
}

function arenaShopQtyChange(delta) {
  const input = document.getElementById("arena-shop-qty");
  if (!input) return;
  input.value = Math.max(1, Math.min(Number(input.max) || ARENA_MAX_STACK_QUANTITY, (Number(input.value) || 1) + delta));
  arenaShopQtyUpdate();
}

function arenaShopQtyMax() {
  const input = document.getElementById("arena-shop-qty");
  if (!input) return;
  input.value = input.max;
  arenaShopQtyUpdate();
}

function arenaShopQtyUpdate() {
  const input = document.getElementById("arena-shop-qty");
  if (!input) return;
  const qty = Math.max(1, Math.min(Number(input.max) || ARENA_MAX_STACK_QUANTITY, Number(input.value) || 1));
  input.value = qty;
  const total = qty * arenaShopModalUnitPrice;
  const totalEl = document.getElementById("arena-shop-total");
  const remainingEl = document.getElementById("arena-shop-remaining");
  if (totalEl) totalEl.textContent = `🪙 ${total.toLocaleString("pt-BR")}`;
  if (remainingEl) remainingEl.textContent = `🪙 ${Math.max(0, arenaShopModalBalance - total).toLocaleString("pt-BR")}`;
}

async function confirmArenaShopQuantity(productCode, productName, priceCoins) {
  const input = document.getElementById("arena-shop-qty");
  const max = Number(input?.max) || ARENA_MAX_STACK_QUANTITY;
  const quantity = Math.max(1, Math.min(max, Number(input?.value) || 1));
  const button = document.querySelector(".arena-shop-modal-buy");
  if (button) { button.disabled = true; button.textContent = "Comprando..."; }
  const purchased = await buyArenaShopItem(productCode, productName, quantity);
  if (purchased) closeArenaShopPurchase();
  else if (button) { button.disabled = false; button.textContent = "Comprar"; }
}

function closeArenaShopPurchase() {
  document.getElementById("arena-shop-modal")?.remove();
}

function arenaShopPurchaseErrorMessage(err) {
  const message = String(err?.message || "Não foi possível concluir a compra.");
  const maxStack = message.match(/Maximum stack:\s*(\d+)/i);
  if (/stack/i.test(message)) return maxStack ? `Quantidade excede o limite de stack deste item (${maxStack[1]} unidades).` : "Quantidade excede o limite de stack deste item.";
  return message;
}

async function buyArenaShopItem(productCode, productName, quantity = 1) {
  try {
    const result = await apiPost("/arena/shop/buy", { productCode, quantity });
    showToast(`Comprou ${result.quantity}x ${productName} (🪙 ${result.arenaCoinsBalance} restantes)`);
    const [shop, inventory] = await Promise.all([
      apiGet("/arena/shop"),
      apiGet("/inventory")
    ]);
    renderArenaShop(shop, inventory || []);
    return true;
  } catch (err) {
    showToast(arenaShopPurchaseErrorMessage(err), "error");
    return false;
  }
}
