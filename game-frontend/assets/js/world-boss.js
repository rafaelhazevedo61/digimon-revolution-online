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

function renderWorldBossContent(boss) {
  const container = document.getElementById("world-boss-content");
  if (!container) return;

  const percent = boss.maxHp > 0
    ? Math.min(100, Math.round((boss.remainingHp / boss.maxHp) * 100))
    : 100;
  const defeated = boss.status === "DEFEATED" || boss.remainingHp <= 0;

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

      ${!defeated && boss.myDailyAttacksRemaining > 0 ? `<button class="btn-primary w-full" onclick="attackWorldBoss()">Atacar Boss Mundial</button>` : ""}
      ${!defeated && boss.myDailyAttacksRemaining === 0 ? `<p class="text-xs text-slate-500 text-center">Limite diário de ataques atingido. Volte amanhã.</p>` : ""}
      ${defeated ? `<p class="text-xs text-green-400 text-center">Boss Mundial derrotado hoje! Volte amanhã.</p>` : ""}
    </div>

    ${rankingHtml}
    ${attacksHtml}
  `;
}

async function attackWorldBoss() {
  const btn = document.querySelector('#world-boss-content button');
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

  const rewardLabels = {
    ATTEMPT: "Baú por tentativa",
    TOP_DAMAGE: "Baú de maior dano acumulado",
    FINAL_BLOW: "Baú do golpe final"
  };
  const rewardRow = `
    ${result.defeated ? `<p class="text-green-400 text-sm font-bold mb-2">Boss derrotado! Você ganhou um bônus final de ${result.defeatedRewardXp.toLocaleString()} XP e ${result.defeatedRewardBits.toLocaleString()} Bits.</p>` : ""}
    ${result.rewards && result.rewards.length > 0 ? `<div class="border-t border-slate-700 pt-2 mt-2">
      <p class="text-amber-300 text-sm font-bold mb-1">Baús recebidos</p>
      ${result.rewards.map(reward => `<p class="text-xs text-slate-300">${escapeHtml(rewardLabels[reward.rewardType] || "Baú")}: <span class="text-amber-200 font-bold">${escapeHtml(reward.chestName)}</span></p>`).join("")}
    </div>` : ""}
  `;

  const overlay = document.createElement("div");
  overlay.id = "world-boss-attack-modal";
  overlay.className = "fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70";
  overlay.innerHTML = `
    <div class="bg-slate-900 border border-cyan-900 rounded-xl max-w-sm w-full p-5 shadow-2xl">
      <div class="flex justify-between items-center mb-3">
        <h3 class="font-bold text-lg text-cyan-400">Resultado do Ataque</h3>
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
      ${rewardRow}
      <p class="text-xs text-slate-500 mb-3">Chance de vitória: ${result.winChance}% · HP restante: ${result.remainingHp.toLocaleString()} / ${result.maxHp.toLocaleString()}</p>
      <button class="btn-primary w-full" onclick="document.getElementById('world-boss-attack-modal').remove()">Fechar</button>
    </div>
  `;
  document.body.appendChild(overlay);
}
