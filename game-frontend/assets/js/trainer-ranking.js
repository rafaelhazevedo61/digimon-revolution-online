function trainerRankingContent(data) {
  if (!data) return '<p role="status">Carregando classificação…</p>';
  const number = value => Number(value || 0).toLocaleString("pt-BR");
  const myId = String(getPlayerId() || "");
  const row = (entry, personal = false) => {
    const mine = String(entry.playerId) === myId;
    return `<article class="trainer-ranking-row ${mine ? "is-mine" : ""} ${personal ? "is-personal" : ""}">
      <div class="trainer-rank-position">#${number(entry.position)}</div>
      <div class="trainer-rank-main"><div class="trainer-rank-heading"><strong>@${escapeHtml(entry.playerName)}</strong>${mine ? '<span class="trainer-rank-you">você</span>' : ""}</div>
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
      <p class="trainer-rank-updated">${updated ? "Última atualização: " + escapeHtml(updated) : "Aguardando a primeira atualização."} · ${number(data.totalPlayers)} jogadores · atualização periódica a cada 5 minutos por padrão</p>
      <details><summary>Como os pontos são calculados?</summary><p>10 pontos por registro de espécie + raridade, mais 50 por espécie com as quatro raridades. Conquistas concluídas somam 25, 50 ou 100 pontos, sem exigir resgate. Cada andar vencido da torre vale 20 pontos. Empates: torre, conquistas, coleção, nome e identificador da conta. Administradores, bots e contas de teste marcadas ficam fora.</p></details>
    </section><section class="trainer-rank-personal" aria-label="Sua classificação"><h3>Sua posição</h3>${personal}</section>
    <section aria-label="Classificação de jogadores">${data.entries.length ? data.entries.map(e => row(e)).join("") : '<p class="trainer-rank-notice">Nenhum jogador nesta página.</p>'}</section>`;
}
