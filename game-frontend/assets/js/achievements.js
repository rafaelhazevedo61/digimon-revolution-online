const ACHIEVEMENT_CATEGORIES = {
  PRIMEIROS_PASSOS: { name: "Primeiros passos", icon: "✧" },
  CRIACAO: { name: "Criação", icon: "🥚" },
  EVOLUCAO: { name: "Evolução", icon: "ϟ" },
  RENASCIMENTO: { name: "Renascimento", icon: "↻" },
  COLECAO: { name: "Coleção", icon: "◈" },
  MISSOES: { name: "Missões", icon: "🧭" },
  COMBATE: { name: "Combate", icon: "⚔" },
  CLA: { name: "Clã", icon: "⚑" },
  EQUIPAMENTOS: { name: "Equipamentos", icon: "⚒" },
};

function achievementRewardIcon(code) {
  if (code === "VIP_DAY_PASS") return "✦";
  if (code === "VIP_ENERGY_CHARGE") return "⚡";
  if (code === "ENERGY_CELL") return "🔋";
  if (code.startsWith("STORAGE_SLOT_")) return "▣";
  if (code.startsWith("XP_DISC_")) return "💿";
  if (code.startsWith("INCUBATOR_")) return "🥚";
  if (code === "RARITY_REROLL") return "🎲";
  if (code === "RARITY_PRESERVATION") return "💎";
  return "◈";
}

function achievementCard(entry) {
  const category = ACHIEVEMENT_CATEGORIES[entry.category] || { name: "Conquista", icon: "✧" };
  const available = entry.status === "AVAILABLE";
  const claimed = entry.status === "CLAIMED";
  const progress = Math.max(0, Math.min(entry.target, entry.progress));
  const percentage = Math.min(100, Math.floor(progress / Math.max(1, entry.target) * 100));
  const number = value => Number(value).toLocaleString("pt-BR");
  const code = escapeHtml(entry.code);
  return `<article class="achievement-card ${available ? "achievement-ready" : ""} ${claimed ? "achievement-claimed" : ""}">
    <div class="achievement-card-top">
      <span class="achievement-category-icon" aria-hidden="true">${category.icon}</span>
      <span class="achievement-category-label">${category.name}</span>
      <span class="achievement-status ${available ? "is-ready" : ""}">${claimed ? "✓ Resgatada" : available ? "Pronta para resgate" : "Em andamento"}</span>
    </div>
    <h3>${escapeHtml(entry.name)}</h3>
    <p class="achievement-description">${escapeHtml(entry.description)}</p>
    <div class="achievement-progress-label"><span>${claimed ? "Jornada registrada" : available ? "Meta concluída" : "Seu progresso"}</span><strong>${number(progress)} / ${number(entry.target)}</strong></div>
    <progress class="achievement-progress" max="${Number(entry.target)}" value="${progress}" aria-label="Progresso de ${escapeHtml(entry.name)}"></progress>
    <div class="achievement-rewards" aria-label="Recompensas">
      <span class="achievement-bits"><span aria-hidden="true">◉</span> ${number(entry.reward.bits)} <small>Bits</small></span>
      <span class="achievement-item"><span aria-hidden="true">${achievementRewardIcon(entry.reward.itemCode)}</span> ${number(entry.reward.quantity)} × ${escapeHtml(entry.reward.itemName)}</span>
    </div>
    <button type="button" class="achievement-claim" data-claim="${code}" ${available ? "" : "disabled"}>
      ${claimed ? "✓ Recompensa recebida" : available ? "Resgatar recompensa →" : `Em progresso · ${percentage}%`}
    </button>
  </article>`;
}

async function renderAchievementsPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `<div class="page-container achievements-page">
    <header class="achievements-header"><div><p class="achievements-eyebrow">Sua história no Mundo Digital</p><h2>Conquistas</h2>
      <p>Cada passo da jornada merece uma recompensa.</p></div><button type="button" class="achievement-back" data-back>← Voltar</button></header>
    <section class="achievements-hero" aria-label="Resumo das conquistas">
      <div class="achievements-emblem" aria-hidden="true">🏆</div><div class="achievements-hero-copy"><span>Marcos da sua jornada</span><h3>Explore. Evolua. Conquiste.</h3>
      <p>Recompensas permanentes, uma vez por conta. Os itens ficam no inventário para você usar quando quiser.</p></div></section>
    <div data-achievements-body aria-live="polite"><p class="achievements-message">Consultando suas conquistas…</p></div>
  </div>`;
  const page = app.querySelector(".achievements-page");
  page.querySelector("[data-back]").onclick = () => navigateTo("more");
  const body = page.querySelector("[data-achievements-body]");
  let data;
  let category = "ALL", status = "ALL", search = "", claiming = false;

  function render() {
    const summary = data.summary;
    body.innerHTML = `<div class="achievements-stats">
      <div><span>Concluídas</span><strong>${summary.completed}<small> / ${summary.total}</small></strong></div>
      <div class="achievements-stat-ready"><span>Para resgatar</span><strong>${summary.available}</strong></div>
      <div><span>Já resgatadas</span><strong>${summary.claimed}</strong></div>
    </div>
    <nav class="achievements-categories" aria-label="Categorias de conquistas">
      <button type="button" data-category="ALL">Todas</button>
      ${Object.entries(ACHIEVEMENT_CATEGORIES).filter(([key]) => data.achievements.some(e => e.category === key))
        .map(([key, value]) => `<button type="button" data-category="${key}">${value.icon} ${value.name}</button>`).join("")}
    </nav>
    <div class="achievements-toolbar"><label class="achievement-search"><span aria-hidden="true">⌕</span><input type="search" placeholder="Buscar conquista…" aria-label="Buscar conquista" data-search></label>
      <label class="achievement-filter"><span>Status</span><select data-status><option value="ALL">Todos os status</option><option value="AVAILABLE">Para resgatar</option><option value="IN_PROGRESS">Em andamento</option><option value="CLAIMED">Resgatadas</option></select></label>
      <button type="button" class="achievement-refresh" data-refresh>↻ Atualizar</button></div>
    <p class="achievements-result-count" data-count></p><div class="achievements-grid" data-grid></div>
    <p class="achievements-footnote">O progresso é atualizado ao abrir ou atualizar esta página. Feitos anteriores são reconhecidos quando constam no histórico do jogo. As conquistas não expiram.</p>`;
    body.querySelector("[data-status]").value = status;
    body.querySelector("[data-search]").value = search;
    body.querySelectorAll("[data-category]").forEach(button => {
      button.onclick = () => { category = button.dataset.category; renderGrid(); };
    });
    body.querySelector("[data-search]").oninput = event => { search = event.target.value; renderGrid(); };
    body.querySelector("[data-status]").onchange = event => { status = event.target.value; renderGrid(); };
    body.querySelector("[data-refresh]").onclick = refresh;
    renderGrid();
  }

  function renderGrid() {
    const order = { AVAILABLE: 0, IN_PROGRESS: 1, CLAIMED: 2 };
    const entries = data.achievements.filter(e => (category === "ALL" || e.category === category)
      && (status === "ALL" || e.status === status)
      && `${e.name} ${e.description} ${e.reward.itemName}`.toLocaleLowerCase("pt-BR").includes(search.toLocaleLowerCase("pt-BR")))
      .sort((a, b) => order[a.status] - order[b.status]);
    body.querySelectorAll("[data-category]").forEach(button => {
      button.setAttribute("aria-pressed", String(button.dataset.category === category));
    });
    body.querySelector("[data-count]").textContent = `${entries.length} ${entries.length === 1 ? "conquista encontrada" : "conquistas encontradas"} · Recompensas disponíveis primeiro`;
    body.querySelector("[data-grid]").innerHTML = entries.length ? entries.map(achievementCard).join("")
      : '<p class="achievements-message">Nenhuma conquista para estes filtros.</p>';
    if (claiming) body.querySelectorAll("[data-claim]").forEach(button => { button.disabled = true; });
  }

  async function refresh() {
    const button = body.querySelector("[data-refresh]");
    if (button) { button.disabled = true; button.textContent = "Atualizando…"; }
    try {
      const result = await apiGet("/players/me/achievements");
      if (!page.isConnected) return;
      data = result;
      render();
    } catch (error) {
      if (!page.isConnected) return;
      if (data) {
        showToast(error.message, "error");
        if (button) { button.disabled = false; button.textContent = "↻ Atualizar"; }
      } else {
        body.innerHTML = '<div class="achievements-message"><p>Não foi possível carregar suas conquistas.</p><button type="button" class="achievement-refresh" data-retry>Tentar novamente</button></div>';
        body.querySelector("[data-retry]").onclick = refresh;
      }
    }
  }

  body.addEventListener("click", async event => {
    const button = event.target.closest("[data-claim]");
    if (!button || button.disabled || claiming) return;
    claiming = true;
    const code = button.dataset.claim;
    body.querySelectorAll("[data-claim]").forEach(entry => { entry.disabled = true; });
    button.textContent = "Resgatando…";
    try {
      const result = await apiPost(`/players/me/achievements/${encodeURIComponent(code)}/claim`);
      if (!page.isConnected) return;
      // Atualiza localmente antes de recarregar: falha de rede posterior não oferece novo resgate.
      const entry = data.achievements.find(item => item.code === code);
      if (entry && entry.status !== "CLAIMED") {
        entry.status = "CLAIMED";
        data.summary.available--;
        data.summary.claimed++;
      }
      showToast(result.message);
      claiming = false;
      render();
      await refresh();
    } catch (error) {
      if (page.isConnected) { showToast(error.message, "error"); claiming = false; renderGrid(); }
    } finally { claiming = false; }
  });

  await refresh();
}
