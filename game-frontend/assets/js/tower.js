let towerPageGeneration = 0;
let towerCooldownTimer = null;

function towerRewardText(reward) {
  if (!reward || !reward.bits) return "Nenhuma recompensa nesta tentativa.";
  return `${Number(reward.bits).toLocaleString("pt-BR")} Bits + ${reward.quantity} × ${reward.itemName}`;
}

async function renderTowerPage() {
  const generation = ++towerPageGeneration;
  clearInterval(towerCooldownTimer);
  showBottomNav("more");
  const app = document.getElementById("app");
  app.innerHTML = '<div class="page-container tower-page" aria-busy="true">Carregando a torre…</div>';
  const root = app.firstElementChild;
  const live = () => generation === towerPageGeneration && app.contains(root);
  let state, teams, members, selected = "", busy = false, pending = null, clockOffset = 0;
  const number = value => Number(value).toLocaleString("pt-BR", { maximumFractionDigits: 0 });
  try {
    [state, teams, members] = await Promise.all([apiGet("/players/me/tower"), apiGet("/mission-teams"), apiGet("/digimon/me")]);
    if (!live()) return;
    selected = teams[0]?.id || "";
    draw();
  } catch (error) {
    if (!live()) return;
    root.removeAttribute("aria-busy");
    root.innerHTML = '<h1>Torre Infinita</h1><p role="alert"></p><button class="btn-primary" type="button">Tentar novamente</button>';
    root.querySelector("p").textContent = error.message;
    root.querySelector("button").onclick = renderTowerPage;
  }

  function draw() {
    clockOffset = Date.parse(state.serverTime) - Date.now();
    root.removeAttribute("aria-busy");
    const floor = state.nextFloor;
    const result = state.lastResult;
    root.innerHTML = `
      <header class="tower-header"><div><p class="tower-eyebrow">Desafio permanente · em equipe</p><h1>Torre Infinita</h1><p>Um andar de cada vez. Até onde seu time consegue chegar?</p></div><a href="#more" class="btn-secondary">Voltar</a></header>
      <div class="tower-record"><div><span>Seu recorde</span><strong>${number(state.highestFloor)} <small>andares vencidos</small></strong></div><div><span>Tentativas</span><strong>${number(state.attempts)}</strong></div><div><span>Próximo marco</span><strong>Andar ${number((Math.floor(floor.number / 10) + (floor.number % 10 ? 1 : 0)) * 10)}</strong></div></div>
      <div class="tower-layout"><section class="tower-encounter" aria-labelledby="tower-floor-title">
        <div class="tower-floor-line"><span>${floor.kind === "MARCO" ? "✦ Guardião do marco" : floor.kind === "CHEFE" ? "♜ Chefe" : "⚔ Encontro comum"}</span><span>${escapeHtml(floor.profile)}</span></div>
        <div class="tower-emblem" aria-hidden="true">${floor.kind === "COMUM" ? "◈" : "♜"}</div>
        <p class="tower-eyebrow">Próximo desafio</p><h2 id="tower-floor-title">Andar ${number(floor.number)}</h2><h3>${escapeHtml(floor.enemyName)}</h3>
        <div class="tower-stats"><div><span>HP</span><strong>${number(floor.stats.hp)}</strong></div><div><span>ATK</span><strong>${number(floor.stats.attack)}</strong></div><div><span>DEF</span><strong>${number(floor.stats.defense)}</strong></div></div>
        <div class="tower-reward"><span>Recompensa por primeira vitória</span><strong>${escapeHtml(towerRewardText(floor.reward))}</strong></div>
      </section><section class="tower-team" aria-labelledby="tower-team-title"><p class="tower-eyebrow">Sua formação</p><h2 id="tower-team-title">Escolha seu time</h2>
        ${teams.length ? `<label for="tower-team-select">Time de 1 a 3 Digimons</label><select id="tower-team-select">${teams.map(t => `<option value="${escapeAttr(t.id)}" ${String(t.id) === String(selected) ? "selected" : ""}>${escapeHtml(t.name)}</option>`).join("")}</select><div id="tower-members"></div><button id="tower-challenge" class="btn-primary" type="button">Desafiar andar</button>` : '<p>Monte seu primeiro time para começar a subir a torre.</p>'}
        <p id="tower-status" role="status" aria-live="polite"></p><p id="tower-error" role="alert"></p><a class="btn-secondary tower-manage" href="#mission-teams">${teams.length ? "Gerenciar times" : "Criar meu primeiro time"}</a>
        <p class="tower-note">Sem custo de energia e sem limite diário. Aguarde 30 segundos entre combates. Membros em missão precisam retornar antes de participar.</p></section></div>
      ${result ? `<section class="tower-result ${result.won ? "tower-victory" : "tower-defeat"}" aria-live="polite"><p class="tower-eyebrow">Último combate · andar ${number(result.floor)}</p><h2>${result.won ? "Vitória! Próximo andar liberado." : "Seu time foi derrotado. Prepare uma nova estratégia."}</h2><p>${escapeHtml(result.teamName)} × ${escapeHtml(result.enemyName)} · ${result.rounds} rodada(s)</p><p>${escapeHtml(towerRewardText(result.reward))}</p>${!result.won ? '<small>Seu progresso permanece salvo. Você pode trocar o time e tentar novamente.</small>' : '<small>Recompensa adicionada automaticamente à sua conta.</small>'}</section>` : ""}
      <footer class="tower-rules"><h2>Como funciona</h2><p>Vitórias avançam um andar. Derrotas mantêm seu progresso. Um chefe espera a cada 5 andares e um guardião especial a cada 10. A dificuldade de cada andar é igual para todos.</p><p>Os combates são automáticos, com até 60 rodadas. Equipamentos e bônus de clã contam. O time ataca primeiro e o guardião alterna entre os membros vivos. Não há perda de HP fora da torre.</p></footer>`;
    const select = root.querySelector("select");
    if (select) {
      select.onchange = () => { selected = select.value; pending = null; drawMembers(); };
      root.querySelector("#tower-challenge").onclick = challenge;
      drawMembers();
    }
    clearInterval(towerCooldownTimer);
    tick();
    towerCooldownTimer = setInterval(tick, 250);
  }
  function drawMembers() {
    const team = teams.find(t => String(t.id) === String(selected));
    root.querySelector("#tower-members").innerHTML = (team?.digimonIds || []).map(id => {
      const d = members.find(m => String(m.id) === String(id));
      return `<div class="tower-member"><span aria-hidden="true">◈</span><div><strong>${escapeHtml(d?.name || "Digimon indisponível")}</strong><small>${d ? `Nível ${number(d.level)} · ${escapeHtml(formatStage(d.stage))}` : "Revise a formação em Meus Times"}</small></div></div>`;
    }).join("");
  }
  function tick() {
    if (!live()) { clearInterval(towerCooldownTimer); return; }
    const remaining = Math.max(0, Math.ceil((Date.parse(state.availableAt || state.serverTime) - Date.now() - clockOffset) / 1000));
    const button = root.querySelector("#tower-challenge");
    if (button) {
      button.disabled = busy || remaining > 0;
      button.textContent = busy ? "Combatendo…" : remaining ? `Aguarde ${remaining}s` : "Desafiar andar";
    }
    root.querySelector("#tower-status").textContent = remaining ? `Novo combate disponível em ${remaining} segundos.` : "";
  }
  async function challenge() {
    if (busy) return;
    busy = true;
    root.querySelector("select").disabled = true;
    root.querySelector("#tower-error").textContent = "";
    // Mantém o mesmo pedido em uma repetição após falha de conexão.
    pending ||= { teamId: selected, requestId: crypto.randomUUID(), expectedAttempts: state.attempts };
    tick();
    try {
      const updated = await apiPost("/players/me/tower/challenge", pending);
      if (!live()) return;
      state = updated; pending = null;
    } catch (error) {
      if (!live()) return;
      // Reconciliar com o servidor também cobre resposta perdida após uma vitória.
      try { state = await apiGet("/players/me/tower"); if (state.attempts !== pending.expectedAttempts) pending = null; } catch (_) { /* próxima tentativa reutiliza o requestId */ }
      if (!live()) return;
      busy = false;
      draw();
      root.querySelector("#tower-error").textContent = error.message;
      return;
    } finally { busy = false; }
    if (live()) draw();
  }
}
