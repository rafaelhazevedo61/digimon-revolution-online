let trainerGuideGeneration = 0;

async function renderTrainerGuidePage() {
  const generation = ++trainerGuideGeneration;
  showBottomNav("more");
  const app = document.getElementById("app");
  app.innerHTML = '<div class="page-container trainer-guide-page" aria-busy="true">Carregando seu guia…</div>';
  const root = app.firstElementChild;
  const live = () => generation === trainerGuideGeneration && app.contains(root);
  let data, busy = false;
  try {
    data = await apiGet("/tutorial/guide");
    if (live()) draw();
  } catch (error) { if (live()) showError(error); }

  function showError(error) {
    root.removeAttribute("aria-busy");
    root.innerHTML = '<h1>Guia do Treinador</h1><p role="alert"></p><button type="button" class="btn-primary">Tentar novamente</button>';
    root.querySelector("p").textContent = error.message;
    root.querySelector("button").onclick = renderTrainerGuidePage;
  }
  function topicCard(topic, recommended = false) {
    return `<article class="guide-topic ${recommended ? "is-recommended" : ""} ${topic.read ? "is-read" : ""}">
      <div class="guide-topic-heading"><h3>${escapeHtml(topic.title)}</h3><span>${topic.read ? "✓ Lido" : topic.available ? "Disponível" : "Mais adiante"}</span></div>
      <p>${escapeHtml(topic.description)}</p>
      ${!topic.available ? `<small>${escapeHtml(topic.unlockHint)}. Esta orientação não substitui os requisitos da atividade.</small>` : `<div class="guide-actions"><button type="button" class="btn-primary" data-guide-route="${escapeAttr(topic.route)}">Conhecer</button>${!topic.read ? `<button type="button" class="btn-secondary" data-guide-read="${escapeAttr(topic.code)}">Entendi</button>` : ""}</div>`}
    </article>`;
  }
  function draw() {
    root.removeAttribute("aria-busy");
    const next = data.topics.find(topic => topic.available && !topic.read);
    const groups = [...new Set(data.topics.map(topic => topic.group))];
    root.innerHTML = `<header class="guide-header"><div><p class="guide-eyebrow">Aprenda no seu ritmo</p><h1>Guia do Treinador</h1><p>Primeiros passos e novas possibilidades para sua jornada.</p></div><button type="button" class="btn-secondary" data-guide-back>Voltar</button></header>
      <section id="tutorial-card">${data.initial.finished ? '<div class="guide-finished"><strong>✓ Primeiros passos concluídos</strong><p>Seu progresso e suas recompensas foram preservados. Os conteúdos abaixo são opcionais.</p></div>' : renderTutorialCard(data.initial)}</section>
      <section class="guide-discovery-header"><h2>Descubra o jogo</h2><p>${data.readTopics}/${data.totalTopics} conteúdos lidos. “Entendi” salva apenas sua leitura; não conclui ações do jogo nem concede recompensas.</p><progress value="${data.readTopics}" max="${data.totalTopics}" aria-label="Conteúdos lidos"></progress><p>As orientações aparecem após os primeiros marcos. Cada atividade mantém seus próprios requisitos.</p></section>
      ${next ? `<section aria-label="Próxima descoberta sugerida"><p class="guide-eyebrow">Próxima descoberta sugerida</p>${topicCard(next, true)}</section>` : `<p class="guide-finished">${data.readTopics === data.totalTopics ? "Você leu todos os conteúdos. Volte sempre que precisar consultar." : "Conclua os primeiros passos para liberar novas orientações."}</p>`}
      <div id="guide-read-error" role="alert"></div>
      ${groups.map(group => { const topics = data.topics.filter(topic => topic.group === group); return `<details class="guide-group"><summary>${escapeHtml(group)} <span>${topics.filter(topic => topic.read).length}/${topics.length} lidos</span></summary><div class="guide-grid">${topics.map(topic => topicCard(topic)).join("")}</div></details>`; }).join("")}`;
    root.querySelector("[data-guide-back]").onclick = () => navigateTo("dashboard");
    root.querySelectorAll("[data-guide-route]").forEach(button => button.onclick = () => {
      const route = button.dataset.guideRoute;
      navigateTo(route, route === "ranking" ? { tab: "players" } : {});
    });
    root.querySelectorAll("[data-guide-read]").forEach(button => button.onclick = async () => {
      if (busy) return;
      busy = true;
      root.querySelectorAll("[data-guide-read]").forEach(b => b.disabled = true);
      try {
        const updated = await apiPost(`/tutorial/guide/${encodeURIComponent(button.dataset.guideRead)}/acknowledge`);
        if (live()) { data = updated; draw(); }
      } catch (error) {
        if (live()) {
          root.querySelector("#guide-read-error").textContent = error.message;
          root.querySelectorAll("[data-guide-read]").forEach(b => b.disabled = false);
        }
      } finally { busy = false; }
    });
  }
}
