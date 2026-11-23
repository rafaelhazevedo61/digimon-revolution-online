function trainerRankingContent(data) {
  if (!data) return '<p role="status">Carregando classificação…</p>';
  const number = value => Number(value || 0).toLocaleString("pt-BR");
  const myId = String(getPlayerId() || "");
  const row = (entry, personal = false) => {
    const mine = String(entry.playerId) === myId;
    return `<article class="trainer-ranking-row ${mine ? "is-mine" : ""} ${personal ? "is-personal" : ""}">
      <div class="trainer-rank-position">#${number(entry.position)}</div>
      <div class="trainer-rank-main"><div class="trainer-rank-heading"><button type="button" class="trainer-profile-link" data-trainer-id="${escapeAttr(entry.playerId)}">@${escapeHtml(entry.playerName)}</button>${mine ? '<span class="trainer-rank-you">você</span>' : ""}</div>
      <div class="trainer-rank-breakdown">
        <span title="Registros e maestrias">Coleção <strong>${number(entry.collectionScore)}</strong></span>
        <span>Conquistas <strong>${number(entry.achievementScore)}</strong></span>
        <span>Torre <strong>${number(entry.towerScore)}</strong></span>
      </div>
      <details class="trainer-rank-details"><summary>Ver progresso</summary><p>${number(entry.collectionEntries)} registros de espécie/raridade · ${number(entry.masteredSpecies)} maestrias · ${number(entry.completedAchievements)} conquistas concluídas · andar ${number(entry.highestFloor)} da torre</p></details>
      </div><div class="trainer-rank-total">${number(entry.totalScore)}<small>pontos de treinador</small></div>
    </article>`;
  };
  const updated = data.updatedAt ? new Date(data.updatedAt).toLocaleString("pt-BR") : null;
  const personal = data.myStatus === "EXCLUDED"
    ? '<p class="trainer-rank-notice">Sua conta não participa deste ranking competitivo.</p>'
    : data.me ? row(data.me, true) : '<p class="trainer-rank-notice">Sua posição será exibida após a próxima atualização.</p>';
  return `<section class="trainer-ranking-intro"><h3>Ranking de Treinadores</h3>
      <p>O progresso da sua conta em coleção, conquistas e Torre Infinita.</p>
      <p class="trainer-rank-updated">${updated ? "Última atualização: " + escapeHtml(updated) : "Aguardando a primeira atualização."} · ${number(data.totalPlayers)} jogadores${data.matchingPlayers != null && data.matchingPlayers !== data.totalPlayers ? " · " + number(data.matchingPlayers) + " encontrados" : ""} · atualização periódica a cada 5 minutos por padrão</p>
      <details><summary>Como os pontos são calculados?</summary><p>10 pontos por registro de espécie + raridade, mais 50 por espécie com as quatro raridades. Conquistas concluídas somam 25, 50 ou 100 pontos, sem exigir resgate. Cada andar vencido da torre vale 20 pontos. Empates: torre, conquistas, coleção, nome e identificador da conta. Administradores, bots e contas de teste marcadas ficam fora.</p></details>
    </section><section class="trainer-rank-personal" aria-label="Sua classificação"><h3>Sua posição</h3>${personal}</section>
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
  return `<div class="trainer-profile-identity"><strong>@${escapeHtml(entry.playerName)}</strong><span>#${number(entry.position)} na classificação global</span></div>
    <p class="trainer-profile-clan">Clã: ${escapeHtml(clan)}</p>
    <div class="trainer-profile-score">${number(entry.totalScore)}<small>pontos de treinador</small></div>
    <div class="trainer-profile-stats">
      <section><h4>Coleção</h4><strong>${number(entry.collectionScore)} pontos</strong><p>${number(entry.collectionEntries)} registros de espécie + raridade</p><p>${number(entry.masteredSpecies)} espécies com maestria</p></section>
      <section><h4>Conquistas</h4><strong>${number(entry.achievementScore)} pontos</strong><p>${number(entry.completedAchievements)} conquistas concluídas</p></section>
      <section><h4>Torre Infinita</h4><strong>${number(entry.towerScore)} pontos</strong><p>Maior andar vencido: ${number(entry.highestFloor)}</p></section>
    </div><p class="trainer-rank-updated">Pontuação atualizada em ${escapeHtml(new Date(profile.updatedAt).toLocaleString("pt-BR"))}. O clã representa a participação atual.</p>`;
}
