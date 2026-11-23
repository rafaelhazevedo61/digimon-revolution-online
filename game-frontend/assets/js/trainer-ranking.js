function trainerMovement(entry) {
  const delta = Number(entry.positionChange || 0);
  const labels = { UP: `↑ ${Math.abs(delta)}`, DOWN: `↓ ${Math.abs(delta)}`, SAME: "—", NEW: "Nova entrada", UNAVAILABLE: "Sem base de ontem" };
  const status = Object.hasOwn(labels, entry.movement) ? entry.movement : "UNAVAILABLE";
  return `<span class="trainer-movement is-${status.toLowerCase()}" title="Variação da posição geral em relação ao registro de ontem">${labels[status]}</span>`;
}

function trainerRankingContent(data) {
  if (!data) return '<p role="status">Carregando classificação…</p>';
  const number = value => Number(value || 0).toLocaleString("pt-BR");
  const stamp = value => value ? new Date(value).toLocaleString("pt-BR", { timeZone: "America/Sao_Paulo" }) : "aguardando coleta";
  const date = value => value ? String(value).split("-").reverse().join("/") : "—";
  const weekly = data.mode === "weekly";
  const myId = String(getPlayerId() || "");
  const row = (entry, personal = false) => {
    const mine = String(entry.playerId) === myId;
    const position = weekly ? entry.weeklyPosition : entry.position;
    return `<article class="trainer-ranking-row ${mine ? "is-mine" : ""} ${personal ? "is-personal" : ""}">
      <div class="trainer-rank-position">${position == null ? "—" : "#" + number(position)}</div>
      <div class="trainer-rank-main"><div class="trainer-rank-heading"><button type="button" class="trainer-profile-link" data-trainer-id="${escapeAttr(entry.playerId)}">@${escapeHtml(entry.playerName)}</button>${mine ? '<span class="trainer-rank-you">você</span>' : ""}${weekly ? `<span class="trainer-rank-you">#${number(entry.position)} no geral</span>` : trainerMovement(entry)}</div>
      ${weekly ? '<p class="trainer-rank-updated">Pontuação acumulada da conta:</p>' : ""}
      <div class="trainer-rank-breakdown"><span>Coleção <strong>${number(entry.collectionScore)}</strong></span><span>Conquistas <strong>${number(entry.achievementScore)}</strong></span><span>Torre <strong>${number(entry.towerScore)}</strong></span></div>
      <details class="trainer-rank-details"><summary>Ver progresso</summary><p>${number(entry.collectionEntries)} registros de espécie/raridade · ${number(entry.masteredSpecies)} maestrias · ${number(entry.completedAchievements)} conquistas concluídas · andar ${number(entry.highestFloor)} da torre</p>
      ${weekly ? `<p>Base da conta em ${escapeHtml(stamp(entry.weeklyBaselineAt))} (${entry.weeklyBaselineKind === "NEW_ENTRY" ? "nova participação, base zero" : "pontuação observada"}). Total acumulado: ${number(entry.totalScore)}.</p>` : ""}</details>
      </div><div class="trainer-rank-total">${number(weekly ? entry.weeklyGain : entry.totalScore)}<small>${weekly ? "pontos na semana" : "pontos de treinador"}</small></div>
    </article>`;
  };
  const personal = data.myStatus === "EXCLUDED" ? '<p class="trainer-rank-notice">Sua conta não participa deste ranking competitivo.</p>'
    : data.me ? row(data.me, true) : '<p class="trainer-rank-notice">Sua posição será exibida após a próxima atualização.</p>';
  const period = weekly
    ? `<p class="trainer-week-context">${data.weekStart ? `Semana iniciada em ${date(data.weekStart)}, às 00h de São Paulo. Base de coleta em ${escapeHtml(stamp(data.weekBaselineAt))}.` : "Aguardando a primeira base semanal."} Cada regra usa sua própria base; mudanças de peso não viram progresso.</p>`
    : `<p class="trainer-week-context">${data.comparisonAvailable ? "Variação em relação ao registro de " + date(data.comparisonDate) + "." : "Variação diária indisponível: ainda não há registro válido de ontem sob a mesma regra."}</p>`;
  return `<section class="trainer-ranking-intro"><h3>${weekly ? "Progresso semanal dos treinadores" : "Ranking de Treinadores"}</h3><p>O progresso da sua conta em coleção, conquistas e Torre Infinita.</p>${period}
    <p class="trainer-rank-updated">${data.updatedAt ? "Última atualização: " + escapeHtml(stamp(data.updatedAt)) : "Aguardando a primeira atualização."} · ${number(data.totalPlayers)} jogadores${data.matchingPlayers != null && data.matchingPlayers !== data.totalPlayers ? " · " + number(data.matchingPlayers) + " encontrados" : ""} · atualização periódica a cada 5 minutos por padrão</p>
    <details><summary>Como os pontos são calculados?</summary><p>10 pontos por registro de espécie + raridade, mais 50 por espécie com as quatro raridades. Conquistas concluídas somam 25, 50 ou 100 pontos. Cada andar vencido da torre vale 20 pontos. No geral, empates usam torre, conquistas, coleção, nome e identificador. Na semana, vale o aumento do total desde a base; empate usa a posição geral. Administradores, bots e contas de teste marcadas ficam fora.</p></details>
    </section><section class="trainer-rank-personal" aria-label="Sua classificação"><h3>Sua posição${weekly ? " semanal" : ""}</h3>${personal}</section>
    <section aria-label="Classificação de jogadores">${data.entries.length ? data.entries.map(e => row(e)).join("") : '<p class="trainer-rank-notice">Nenhum jogador encontrado.</p>'}</section>`;
}

function bindTrainerProfileLinks(root) {
  root.querySelectorAll("[data-trainer-id]").forEach(button => {
    button.onclick = () => openTrainerProfile(button.dataset.trainerId);
  });
}

async function openTrainerProfile(playerId) {
  document.getElementById("trainer-profile-overlay")?.remove();
  const previousFocus = document.activeElement;
  const overlay = document.createElement("div");
  overlay.id = "trainer-profile-overlay";
  overlay.className = "trainer-profile-overlay";
  overlay.innerHTML = `<section class="trainer-profile-dialog" role="dialog" aria-modal="true" aria-labelledby="trainer-profile-title">
    <header><h3 id="trainer-profile-title">Perfil público do treinador</h3><button type="button" aria-label="Fechar perfil">×</button></header>
    <div class="trainer-profile-body" aria-busy="true">Carregando perfil…</div></section>`;
  const closeButton = overlay.querySelector("button");
  const close = () => { overlay.remove(); if (previousFocus?.isConnected) previousFocus.focus(); };
  closeButton.onclick = close;
  overlay.onclick = event => { if (event.target === overlay) close(); };
  overlay.onkeydown = event => {
    if (event.key === "Escape") { event.preventDefault(); close(); }
    if (event.key === "Tab") { event.preventDefault(); closeButton.focus(); }
  };
  document.body.appendChild(overlay);
  closeButton.focus();
  const body = overlay.querySelector(".trainer-profile-body");
  try {
    const profile = await apiGet(`/ranking/players/${encodeURIComponent(playerId)}`);
    if (!overlay.isConnected) return;
    body.innerHTML = trainerProfileContent(profile);
  } catch (error) {
    if (overlay.isConnected) body.innerHTML = `<p role="alert">${escapeHtml(error.message || "Não foi possível carregar o perfil.")}</p>`;
  } finally { if (overlay.isConnected) body.removeAttribute("aria-busy"); }
}

function trainerProfileContent(profile) {
  const entry = profile.ranking;
  const number = value => Number(value || 0).toLocaleString("pt-BR");
  const clan = profile.clanName ? `${profile.clanName}${profile.clanTag ? " [" + profile.clanTag + "]" : ""}` : "Sem clã";
  return `<div class="trainer-profile-identity"><strong>@${escapeHtml(entry.playerName)}</strong><span>#${number(entry.position)} na classificação global</span>${trainerMovement(entry)}</div>
    <p class="trainer-profile-clan">Clã: ${escapeHtml(clan)}</p>
    <div class="trainer-profile-score">${number(entry.totalScore)}<small>pontos de treinador</small></div>
    ${entry.weeklyPosition != null ? `<p class="trainer-week-context">Progresso semanal: ${number(entry.weeklyGain)} pontos · posição #${number(entry.weeklyPosition)}</p>` : ""}
    <div class="trainer-profile-stats">
      <section><h4>Coleção</h4><strong>${number(entry.collectionScore)} pontos</strong><p>${number(entry.collectionEntries)} registros de espécie + raridade</p><p>${number(entry.masteredSpecies)} espécies com maestria</p></section>
      <section><h4>Conquistas</h4><strong>${number(entry.achievementScore)} pontos</strong><p>${number(entry.completedAchievements)} conquistas concluídas</p></section>
      <section><h4>Torre Infinita</h4><strong>${number(entry.towerScore)} pontos</strong><p>Maior andar vencido: ${number(entry.highestFloor)}</p></section>
    </div><p class="trainer-rank-updated">Pontuação atualizada em ${escapeHtml(new Date(profile.updatedAt).toLocaleString("pt-BR"))}. O clã representa a participação atual.</p>`;
}
