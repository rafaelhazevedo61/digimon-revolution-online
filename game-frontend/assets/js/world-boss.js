async function renderWorldBossPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container world-boss-page">
      <header class="world-boss-page-header">
        <button class="world-boss-back" onclick="navigateTo('more')">← <span>Voltar para Mais</span></button>
        <div class="world-boss-kicker"><span class="world-boss-kicker-dot"></span> Evento global do servidor</div>
        <div class="world-boss-heading-row">
          <div>
            <h2 class="world-boss-page-title">Chefe Mundial</h2>
            <p class="world-boss-page-subtitle">Una-se aos jogadores para derrubar um inimigo que pertence a todo o servidor.</p>
          </div>
          <div class="world-boss-header-mark" aria-hidden="true">⚔</div>
        </div>
      </header>

      <div id="world-boss-content">
        <div class="world-boss-loading" aria-label="Carregando Chefe Mundial">
          <div class="world-boss-loading-hero"></div>
          <div class="world-boss-loading-line world-boss-loading-line-wide"></div>
          <div class="world-boss-loading-line"></div>
        </div>
      </div>
    </div>
  `;

  await loadWorldBoss();
}

async function loadWorldBoss() {
  const container = document.getElementById("world-boss-content");
  if (!container) return;

  try {
    const boss = await apiGet("/world-boss/me");
    renderWorldBossContent(boss);
  } catch (err) {
    container.innerHTML = `<div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>`;
  }
}

let worldBossCooldownTimer = null;

function renderWorldBossContent(boss) {
  const container = document.getElementById("world-boss-content");
  if (!container) return;

  clearWorldBossCooldownTimer();
  const percent = boss.maxHp > 0
    ? Math.min(100, Math.round((boss.remainingHp / boss.maxHp) * 100))
    : 100;
  const defeated = boss.status === "DEFEATED" || boss.remainingHp <= 0;
  const summary = boss.defeatSummary;
  const formatAliveDuration = (seconds) => { const total = Math.max(0, Number(seconds) || 0); const days = Math.floor(total / 86400); const hours = Math.floor((total % 86400) / 3600); const minutes = Math.floor((total % 3600) / 60); const secs = total % 60; return `${days}d ${hours}h ${minutes}m ${secs}s`; };
  const formatBossDateTime = (value) => { if (!value) return "Não informado"; const date = new Date(value); return `${date.toLocaleDateString("pt-BR")} - ${date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}`; };
  const defeatSummaryHtml = defeated && summary ? `<div class="mt-3 rounded-lg border border-green-800/70 bg-green-950/20 p-3 text-xs"><p class="font-bold text-green-300 mb-2">Resumo da derrota</p><div class="grid grid-cols-2 gap-2"><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Golpe final</p><p class="mt-1 font-semibold text-white">${escapeHtml(summary.finalBlowUsername || "Desconhecido")}</p></div><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Maior dano</p><p class="mt-1 font-semibold text-white">${escapeHtml(summary.topDamageUsername || "Desconhecido")}<br><span class="text-cyan-300">${Number(summary.topDamage || 0).toLocaleString("pt-BR")} de dano</span></p></div><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Ataques totais</p><p class="mt-1 font-semibold text-white">${Number(summary.totalAttacks || 0).toLocaleString("pt-BR")}</p></div><div class="rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Tempo vivo</p><p class="mt-1 font-semibold text-white">${formatAliveDuration(summary.aliveDurationSeconds)}</p></div><div class="col-span-2 rounded-md bg-slate-900/60 p-2"><p class="text-[10px] uppercase text-slate-500">Próximo ciclo</p><p class="mt-1 font-semibold text-amber-300">${formatBossDateTime(summary.nextCycleAt)}</p></div></div></div>` : "";
  const cooldownMinutes = Number.isFinite(Number(boss.attackCooldownMinutes)) && Number(boss.attackCooldownMinutes) > 0
    ? Number(boss.attackCooldownMinutes)
    : 5;
  const cooldownEnabled = boss.cooldownEnabled !== false;
  const nextAttackAt = boss.nextAttackAvailableAt ? Date.parse(boss.nextAttackAvailableAt) : NaN;
  const cooldownActive = cooldownEnabled
    && !defeated
    && Number.isFinite(nextAttackAt)
    && nextAttackAt > Date.now();
  const cooldownInfoHtml = cooldownEnabled
    ? `<p class="world-boss-cooldown-info">Intervalo entre ataques: ${cooldownMinutes} minuto(s)</p>`
    : `<p class="world-boss-cooldown-info world-boss-cooldown-info-safe">Intervalo desativado pelo administrador</p>`;
  const attackButtonHtml = !defeated
    ? `${cooldownInfoHtml}<button id="world-boss-attack-button" class="btn-primary world-boss-attack-button" onclick="attackWorldBoss()"${cooldownActive ? " disabled" : ""}>${cooldownActive ? "Próximo ataque em <span id=\"world-boss-countdown\">--:--</span>" : "Atacar Chefe Mundial"}</button>`
    : "";

  let rankingHtml = "";
  if (boss.ranking && boss.ranking.length > 0) {
    const rankColor = (index) => {
      if (index === 0) return "text-yellow-400";
      if (index === 1) return "text-slate-300";
      if (index === 2) return "text-amber-600";
      return "text-slate-400";
    };
    rankingHtml = `
      <section class="world-boss-panel world-boss-ranking-panel">
        <div class="world-boss-panel-heading"><div><p class="world-boss-panel-kicker">Contribuição global</p><h3 class="world-boss-panel-title">Ranking de Dano</h3></div><span class="world-boss-panel-mark">#</span></div>
        <div class="world-boss-ranking-list">
          ${boss.ranking.map((entry, i) => `
            <div class="world-boss-ranking-row">
              <div class="world-boss-ranking-player"><span class="world-boss-ranking-position ${rankColor(i)}">${entry.position}.</span><span>${escapeHtml(entry.username)}</span></div>
              <span class="world-boss-ranking-damage">${Number(entry.totalDamage || 0).toLocaleString("pt-BR")}</span>
            </div>
          `).join("")}
        </div>
      </section>
    `;
  } else {
    rankingHtml = `
      <section class="world-boss-panel world-boss-ranking-panel world-boss-empty-panel">
        <div class="world-boss-panel-heading"><div><p class="world-boss-panel-kicker">Contribuição global</p><h3 class="world-boss-panel-title">Ranking de Dano</h3></div><span class="world-boss-panel-mark">#</span></div>
        <p class="world-boss-empty-copy">Ainda nenhum jogador atacou o Chefe Mundial neste ciclo. Seja o primeiro!</p>
      </section>
    `;
  }

  let attacksHtml = "";
  if (boss.recentAttacks && boss.recentAttacks.length > 0) {
    attacksHtml = `
      <section class="world-boss-panel world-boss-attacks-panel">
        <div class="world-boss-panel-heading"><div><p class="world-boss-panel-kicker">Atividade recente</p><h3 class="world-boss-panel-title">Últimos ataques</h3></div><span class="world-boss-panel-mark">↯</span></div>
        <div class="world-boss-attacks-list">
          ${boss.recentAttacks.map(a => `
            <div class="world-boss-attack-row"><span>${escapeHtml(a.username)}</span><span class="world-boss-attack-damage">${Number(a.damage || 0).toLocaleString("pt-BR")}</span></div>
          `).join("")}
        </div>
      </section>
    `;
  }

  const remainingHp = Number(boss.remainingHp || 0).toLocaleString("pt-BR");
  const maxHp = Number(boss.maxHp || 0).toLocaleString("pt-BR");
  const myDamage = Number(boss.myTotalDamage || 0).toLocaleString("pt-BR");
  const statusLabel = defeated ? "Derrotado hoje" : "Em batalha";
  const statusClass = defeated ? "world-boss-status-defeated" : "world-boss-status-active";

  container.innerHTML = `
    <section class="world-boss-combat-card ${defeated ? "is-defeated" : ""}">
      <div class="world-boss-combat-topline">
        <span class="world-boss-live-badge ${statusClass}"><span class="world-boss-live-dot"></span>${statusLabel}</span>
        <span class="world-boss-cycle-label">Ciclo atual</span>
      </div>
      <div class="world-boss-identity">
        <div class="world-boss-portrait">
          <div class="world-boss-portrait-ring"></div>
          ${boss.bossImageUrl ? `<img src="${escapeHtml(boss.bossImageUrl)}" class="world-boss-portrait-image" alt="${escapeHtml(boss.bossName || "Chefe Mundial")}" onerror="this.style.display='none'; this.parentElement.classList.add('has-fallback')">` : ""}
          <span class="world-boss-portrait-fallback" aria-hidden="true">👾</span>
        </div>
        <div class="world-boss-identity-copy">
          <p class="world-boss-identity-eyebrow">Alvo compartilhado</p>
          <h3 class="world-boss-name">${escapeHtml(boss.bossName)}</h3>
          <p class="world-boss-identity-description">Cada ataque contribui para o progresso global da batalha.</p>
        </div>
      </div>

      <div class="world-boss-hp-block">
        <div class="world-boss-hp-heading"><span>Vitalidade do chefe</span><strong>${remainingHp} <small>/ ${maxHp} HP</small></strong></div>
        <div class="world-boss-hp-track" role="progressbar" aria-label="Vitalidade restante" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100">
          <div class="world-boss-hp-fill ${defeated ? "is-defeated" : ""}" style="width:${percent}%"></div>
        </div>
        <div class="world-boss-hp-foot"><span>${percent}% de vitalidade restante</span><span>${defeated ? "Próximo ciclo em breve" : "Ataque coletivo em andamento"}</span></div>
      </div>

      <div class="world-boss-stat-grid">
        <div class="world-boss-stat-card"><span class="world-boss-stat-icon">✦</span><div><span class="world-boss-stat-label">Seu dano total</span><strong>${myDamage}</strong></div></div>
        <div class="world-boss-stat-card"><span class="world-boss-stat-icon world-boss-stat-icon-muted">◎</span><div><span class="world-boss-stat-label">Participação</span><strong>${boss.ranking && boss.ranking.length ? "No ranking" : "Primeiro ataque"}</strong></div></div>
      </div>

      ${attackButtonHtml}
      ${defeated ? `<p class="world-boss-defeated-message">Chefe Mundial derrotado. O próximo renascimento ocorrerá uma hora após a derrota.</p>${defeatSummaryHtml}` : ""}
    </section>

    <div class="world-boss-secondary-grid">
      ${rankingHtml}
      ${attacksHtml}
    </div>
  `;

  if (cooldownActive) {
    startWorldBossCooldownCountdown(nextAttackAt);
  }
}

function clearWorldBossCooldownTimer() {
  if (worldBossCooldownTimer !== null) {
    clearInterval(worldBossCooldownTimer);
    worldBossCooldownTimer = null;
  }
}

function formatWorldBossCountdown(totalSeconds) {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  if (hours > 0) {
    return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
  }
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
}

function startWorldBossCooldownCountdown(nextAttackAt) {
  clearWorldBossCooldownTimer();
  const button = document.getElementById("world-boss-attack-button");
  const countdown = document.getElementById("world-boss-countdown");
  if (!button || !countdown || !Number.isFinite(nextAttackAt)) return;

  const tick = () => {
    const remainingSeconds = Math.max(0, Math.ceil((nextAttackAt - Date.now()) / 1000));
    if (remainingSeconds === 0) {
      clearWorldBossCooldownTimer();
      loadWorldBoss();
      return;
    }
    countdown.textContent = formatWorldBossCountdown(remainingSeconds);
  };

  tick();
  worldBossCooldownTimer = setInterval(tick, 1000);
}

async function attackWorldBoss() {
  const btn = document.querySelector('#world-boss-attack-button');
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Atacando...";
  }

  try {
    const requestId = createWorldBossRequestId();
    const result = await apiPost("/world-boss/attack", null, {
      "Idempotency-Key": requestId
    });
    showWorldBossAttackModal(result);
    await loadWorldBoss();
  } catch (err) {
    showToast(err.message, "error");
    if (btn) {
      btn.disabled = false;
      btn.textContent = "Atacar Chefe Mundial";
    }
  }
}

function createWorldBossRequestId() {
  if (window.crypto && typeof window.crypto.randomUUID === "function") {
    return window.crypto.randomUUID();
  }
  return `world-boss-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function formatBossDateTime(value) { if (!value) return "Não informado"; const date = new Date(value); return `${date.toLocaleDateString("pt-BR")} - ${date.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit", second: "2-digit" })}`; }

function showWorldBossAttackModal(result) {
  const existing = document.getElementById("world-boss-attack-modal");
  if (existing) existing.remove();

  const rewardMeta = {
    ATTEMPT: { label: "Baú por tentativa", tone: "text-cyan-300" },
    TOP_DAMAGE: { label: "Baú de maior dano acumulado", tone: "text-amber-300" },
    FINAL_BLOW: { label: "Baú do golpe final", tone: "text-rose-300" }
  };
  const rewards = Array.isArray(result.rewards) ? result.rewards : [];
  const rewardRows = rewards.map((reward, index) => {
    const meta = rewardMeta[reward.rewardType] || { label: "Baú recebido", tone: "text-amber-300" };
    return `
      <div class="flex items-start gap-3 rounded-lg border border-slate-700 bg-slate-800/70 p-3">
        <div class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-slate-700 text-xs font-bold text-slate-300">${index + 1}</div>
        <div class="min-w-0 flex-1">
          <p class="text-[11px] font-semibold uppercase tracking-wide ${meta.tone}">${meta.label}</p>
          <p class="mt-1 break-words text-sm font-bold leading-snug text-white">${escapeHtml(reward.chestName || "Baú")}</p>
        </div>
      </div>
    `;
  }).join("");
  const rewardSectionHtml = rewards.length > 0 ? `
    <section class="mt-4 border-t border-slate-700 pt-4" aria-label="Baús recebidos">
      <div class="mb-2 flex items-center justify-between gap-2">
        <p class="text-sm font-bold text-amber-300">Baús recebidos</p>
        <span class="text-[11px] text-slate-500">${rewards.length} ${rewards.length === 1 ? "recompensa" : "recompensas"}</span>
      </div>
      <div class="space-y-2">${rewardRows}</div>
    </section>
  ` : "";
  const defeatSummary = result.defeatSummary;
  const defeatSummaryHtml = result.defeated && defeatSummary ? `<div class="mt-3 rounded-lg border border-green-800/70 bg-green-950/20 p-3 text-xs"><p class="font-bold text-green-300 mb-2">Resumo da derrota</p><div class="grid grid-cols-2 gap-2"><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Golpe final</p><p class="mt-1 font-semibold text-white">${escapeHtml(defeatSummary.finalBlowUsername || "Desconhecido")}</p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Maior dano</p><p class="mt-1 font-semibold text-white">${escapeHtml(defeatSummary.topDamageUsername || "Desconhecido")}<br><span class="text-cyan-300">${Number(defeatSummary.topDamage || 0).toLocaleString("pt-BR")} de dano</span></p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Ataques totais</p><p class="mt-1 font-semibold text-white">${Number(defeatSummary.totalAttacks || 0).toLocaleString("pt-BR")}</p></div><div class="rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Tempo vivo</p><p class="mt-1 font-semibold text-white">${formatAliveDuration(defeatSummary.aliveDurationSeconds)}</p></div><div class="col-span-2 rounded-md bg-slate-800/70 p-2"><p class="text-[10px] uppercase text-slate-500">Próximo ciclo</p><p class="mt-1 font-semibold text-amber-300">${formatBossDateTime(defeatSummary.nextCycleAt)}</p></div></div></div>` : "";
  const defeatBonusHtml = result.defeated ? `
    <div class="mt-4 rounded-lg border border-green-800/70 bg-green-950/30 p-3">
      <p class="text-sm font-bold text-green-300">Chefe derrotado</p>
      <p class="mt-1 text-xs leading-relaxed text-green-200">Bônus final: <strong>${result.defeatedRewardXp.toLocaleString()} XP</strong> e <strong>${result.defeatedRewardBits.toLocaleString()} Bits</strong>.</p>
    </div>
  ` : "";

  const overlay = document.createElement("div");
  overlay.id = "world-boss-attack-modal";
  overlay.className = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70";
  overlay.innerHTML = `
    <div role="dialog" aria-modal="true" aria-labelledby="world-boss-result-title" class="bg-slate-900 border border-cyan-900 rounded-xl max-w-md max-h-[90vh] w-full overflow-y-auto p-5 shadow-2xl">
      <div class="flex justify-between items-center mb-3">
        <h3 id="world-boss-result-title" class="font-bold text-lg text-cyan-400">Resultado do Ataque</h3>
        <button class="text-slate-400 text-2xl" onclick="document.getElementById('world-boss-attack-modal').remove()">&times;</button>
      </div>
      <div class="text-center mb-4">
        <p class="text-3xl font-bold text-white mb-1">${result.damage.toLocaleString()}</p>
        <p class="text-xs text-slate-400">Dano causado</p>
      </div>
      <div class="grid grid-cols-2 gap-3 mb-4 text-sm">
        <div class="bg-slate-800 rounded-lg p-2 text-center">
          <p class="font-bold text-cyan-400">+${result.xpGained.toLocaleString()}</p>
          <p class="text-xs text-slate-400">XP</p>
        </div>
        <div class="bg-slate-800 rounded-lg p-2 text-center">
          <p class="font-bold text-yellow-400">+${result.bitsGained.toLocaleString()}</p>
          <p class="text-xs text-slate-400">Bits</p>
        </div>
      </div>
      ${defeatBonusHtml}
      ${defeatSummaryHtml}
      ${rewardSectionHtml}
      <section class="mt-4 grid grid-cols-2 gap-2 border-t border-slate-800 pt-3" aria-label="Detalhes do ataque">
        <div class="rounded-lg bg-slate-800/60 p-2 text-center">
          <p class="text-[10px] uppercase tracking-wide text-slate-500">Chance de vitória</p>
          <p class="mt-1 text-sm font-bold text-slate-200">${result.winChance}%</p>
        </div>
        <div class="rounded-lg bg-slate-800/60 p-2 text-center">
          <p class="text-[10px] uppercase tracking-wide text-slate-500">HP restante</p>
          <p class="mt-1 break-words text-sm font-bold text-slate-200">${result.remainingHp.toLocaleString()} / ${result.maxHp.toLocaleString()}</p>
        </div>
      </section>
      <button class="btn-primary w-full" onclick="document.getElementById('world-boss-attack-modal').remove()">Fechar</button>
    </div>
  `;
  document.body.appendChild(overlay);
}
