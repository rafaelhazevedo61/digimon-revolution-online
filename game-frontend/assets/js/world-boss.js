async function renderWorldBossPage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <div class="flex items-center gap-2 mb-4">
        <button class="text-sm text-slate-400" onclick="navigateTo('more')">← Voltar</button>
      </div>
      <h2 class="text-lg font-bold mb-1">Boss Mundial</h2>
      <p class="text-xs text-slate-400 mb-4">Boss compartilhado por todo o servidor. Todos os jogadores contribuem para derrotá-lo.</p>

      <div id="world-boss-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
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
  const cooldownMinutes = Number.isFinite(Number(boss.attackCooldownMinutes)) && Number(boss.attackCooldownMinutes) > 0
    ? Number(boss.attackCooldownMinutes)
    : 5;
  const cooldownEnabled = boss.cooldownEnabled !== false;
  const nextAttackAt = boss.nextAttackAvailableAt ? Date.parse(boss.nextAttackAvailableAt) : NaN;
  const cooldownActive = cooldownEnabled
    && !defeated
    && boss.myDailyAttacksRemaining > 0
    && Number.isFinite(nextAttackAt)
    && nextAttackAt > Date.now();
  const cooldownInfoHtml = cooldownEnabled
    ? `<p class="text-xs text-slate-500 mb-2 text-center">Cooldown entre ataques: ${cooldownMinutes} minuto(s)</p>`
    : `<p class="text-xs text-green-400 mb-2 text-center">Cooldown desativado pelo administrador</p>`;
  const attackButtonHtml = !defeated && boss.myDailyAttacksRemaining > 0
    ? `${cooldownInfoHtml}<button id="world-boss-attack-button" class="btn-primary w-full" onclick="attackWorldBoss()"${cooldownActive ? " disabled" : ""}>${cooldownActive ? "Próximo ataque em <span id=\"world-boss-countdown\">--:--</span>" : "Atacar Boss Mundial"}</button>`
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
      <div class="card mt-4 border-cyan-900">
        <p class="font-bold mb-2 text-sm">Ranking de Dano Global</p>
        ${boss.ranking.map((entry, i) => `
          <div class="flex justify-between items-center py-1.5 border-b border-slate-800 last:border-0">
            <div class="flex items-center gap-2">
              <span class="font-bold w-5 ${rankColor(i)}">${entry.position}.</span>
              <span class="text-sm text-slate-200">${escapeHtml(entry.username)}</span>
            </div>
            <span class="text-xs text-cyan-400 font-mono">${entry.totalDamage.toLocaleString()}</span>
          </div>
        `).join("")}
      </div>
    `;
  } else {
    rankingHtml = `
      <div class="card mt-4 border-slate-800">
        <p class="font-bold mb-2 text-sm">Ranking de Dano Global</p>
        <p class="text-sm text-slate-500">Ainda nenhum jogador atacou o Boss Mundial hoje. Seja o primeiro!</p>
      </div>
    `;
  }

  let attacksHtml = "";
  if (boss.recentAttacks && boss.recentAttacks.length > 0) {
    attacksHtml = `
      <div class="card mt-4 border-slate-800">
        <p class="font-bold mb-2 text-sm">Últimos ataques</p>
        ${boss.recentAttacks.map(a => `
          <div class="flex justify-between items-center py-1 border-b border-slate-800 last:border-0">
            <span class="text-sm text-slate-200">${escapeHtml(a.username)}</span>
            <span class="text-xs text-cyan-400 font-mono">${a.damage.toLocaleString()}</span>
          </div>
        `).join("")}
      </div>
    `;
  }

  container.innerHTML = `
    <div class="card mb-3">
      <div class="flex items-center gap-3 mb-3">
        <div class="w-16 h-16 rounded-lg flex items-center justify-center text-2xl shrink-0" style="background:#334155;color:#94a3b8">
          ${boss.bossImageUrl ? `<img src="${escapeHtml(boss.bossImageUrl)}" class="w-16 h-16 rounded-lg object-cover" alt="" onerror="this.style.display='none'; this.parentElement.textContent='👾'">` : "👾"}
        </div>
        <div>
          <p class="font-bold">${escapeHtml(boss.bossName)}</p>
          <p class="text-xs ${defeated ? 'text-green-400' : 'text-slate-400'}">${defeated ? 'Derrotado hoje' : 'Em batalha'}</p>
        </div>
      </div>

      <div class="flex justify-between text-xs mb-1">
        <span class="text-slate-400">HP</span>
        <span class="text-slate-400">${boss.remainingHp.toLocaleString()} / ${boss.maxHp.toLocaleString()}</span>
      </div>
      <div class="w-full bg-slate-800 rounded-full h-2.5 mb-4">
        <div class="${defeated ? 'bg-green-500' : 'bg-red-500'} h-2.5 rounded-full" style="width:${percent}%"></div>
      </div>

      <p class="text-xs text-slate-400 mb-3">Seus ataques hoje: <span class="text-cyan-400">${boss.myDailyAttacksUsed}/${boss.myDailyAttacksUsed + boss.myDailyAttacksRemaining}</span> · Seu dano: <span class="text-cyan-400">${boss.myTotalDamage.toLocaleString()}</span></p>

      ${attackButtonHtml}
      ${!defeated && boss.myDailyAttacksRemaining === 0 ? `${cooldownInfoHtml}<p class="text-xs text-slate-500 text-center">Limite diário de ataques atingido. Volte amanhã.</p>` : ""}
      ${defeated ? `<p class="text-xs text-green-400 text-center">Boss Mundial derrotado hoje! Volte amanhã.</p>` : ""}
    </div>

    ${rankingHtml}
    ${attacksHtml}
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
      btn.textContent = "Atacar Boss Mundial";
    }
  }
}

function createWorldBossRequestId() {
  if (window.crypto && typeof window.crypto.randomUUID === "function") {
    return window.crypto.randomUUID();
  }
  return `world-boss-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

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
  const defeatBonusHtml = result.defeated ? `
    <div class="mt-4 rounded-lg border border-green-800/70 bg-green-950/30 p-3">
      <p class="text-sm font-bold text-green-300">Boss derrotado</p>
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
