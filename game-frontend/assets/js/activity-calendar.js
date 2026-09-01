let activityCalendarState = null;

async function renderActivityCalendarPage() {
  const app = document.getElementById("app");
  showBottomNav("activity-calendar");
  app.innerHTML = `<div class="page-container"><div class="card animate-pulse h-64"></div></div>`;
  try {
    activityCalendarState = await apiGet("/activity-calendar/current");
    renderActivityCalendar(activityCalendarState);
  } catch (err) {
    app.innerHTML = `<div class="page-container"><div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div></div>`;
  }
}

function renderActivityCalendar(data) {
  const app = document.getElementById("app");
  const dayEntries = Array.isArray(data.days) ? data.days : [];
  const completed = Number(data.claimedDays || 0);
  const total = Number(data.daysInMonth || dayEntries.length || 0);
  const percent = total ? Math.round(completed * 100 / total) : 0;
  const currentDay = dayEntries.find(day => day.date === data.currentDate);
  const statusLabels = {
    resgatado: "Resgatado",
    disponivel: "Disponível",
    progresso: "Em progresso",
    bloqueado: "Sem atividade"
  };
  const getStatus = (day) => day.rewardClaimed ? "resgatado" : day.goalReached ? "disponivel" : day.points > 0 ? "progresso" : "bloqueado";
  const currentStatus = currentDay ? getStatus(currentDay) : "bloqueado";
  const currentStatusLabel = currentDay ? statusLabels[currentStatus] : "Aguardando atividade";
  const monthlyMessage = data.monthlyRewardClaimed
    ? "Baú mensal resgatado."
    : data.monthlyCompletionEligible
      ? "Você completou todos os dias e já pode abrir o baú mensal."
      : "Resgate todos os dias para liberar o baú exclusivo.";
  const monthlyAction = data.monthlyCompletionEligible && !data.monthlyRewardClaimed
    ? `<button type="button" class="btn-primary activity-calendar-monthly-action" onclick="claimActivityMonthly('${data.yearMonth}')">Resgatar baú mensal</button>`
    : "";

  const days = dayEntries.map(day => {
    const status = getStatus(day);
    const action = day.goalReached && !day.rewardClaimed
      ? `<button type="button" class="btn-sm btn-primary activity-day-action" onclick="claimActivityDay('${day.date}')">Resgatar</button>`
      : "";
    const isToday = day.date === data.currentDate;
    const todayLabel = isToday ? `<span class="activity-day-today-label">Hoje</span>` : "";
    return `<article class="activity-day activity-day-${status}${isToday ? " is-today" : ""}">
      <div class="activity-day-topline">${todayLabel}<span class="activity-day-number">Dia ${day.dayOfMonth}</span></div>
      <div class="activity-day-points"><strong>${Number(day.points || 0)}</strong><span>/${Number(data.dailyGoal || 0)}</span></div>
      <div class="activity-day-state"><span class="activity-day-dot" aria-hidden="true"></span>${statusLabels[status]}</div>
      ${action}
    </article>`;
  }).join("");

  app.innerHTML = `<div class="page-container activity-calendar-page-container">
    <header class="activity-calendar-header">
      <div class="activity-calendar-header-copy">
        <p class="activity-calendar-eyebrow">Rotina · Progresso</p>
        <h1 class="activity-calendar-title">Calendário de Atividades</h1>
        <p class="activity-calendar-subtitle">Construa sua sequência diária e acompanhe o caminho até a recompensa mensal.</p>
      </div>
      <div class="activity-calendar-header-actions">
        <div class="activity-calendar-target"><span>Meta diária</span><strong>${Number(data.dailyGoal || 0)} pts</strong></div>
        <button type="button" class="activity-calendar-back" onclick="navigateTo('more')">Voltar</button>
      </div>
    </header>

    <div class="activity-calendar-layout">
      <main class="activity-calendar-main">
        <section class="activity-calendar-progress-card">
          <div class="activity-calendar-card-heading">
            <div><p class="activity-calendar-eyebrow activity-calendar-eyebrow-cyan">Resumo do mês</p><h2 class="activity-calendar-section-title">Conclusão mensal</h2></div>
            <strong class="activity-calendar-progress-value">${completed}/${total}</strong>
          </div>
          <div class="activity-calendar-progress-track" role="progressbar" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100" aria-label="${percent}% de conclusão mensal"><span style="width:${percent}%"></span></div>
          <div class="activity-calendar-progress-meta"><span>${percent}% concluído</span><span>${Math.max(total - completed, 0)} dias restantes</span></div>
          <p class="activity-calendar-progress-copy">${monthlyMessage}</p>
          ${monthlyAction}
        </section>

        <section class="activity-calendar-surface">
          <div class="activity-calendar-section-heading">
            <div><p class="activity-calendar-eyebrow">Calendário do mês</p><h2 class="activity-calendar-section-title">Sua atividade diária</h2></div>
            <span class="activity-calendar-section-caption">${dayEntries.length} dias · ${Number(data.dailyGoal || 0)} pts/dia</span>
          </div>
          <div class="activity-calendar-legend" aria-label="Legenda dos estados">
            <span><i class="activity-calendar-legend-dot is-resgatado"></i>Resgatado</span>
            <span><i class="activity-calendar-legend-dot is-disponivel"></i>Disponível</span>
            <span><i class="activity-calendar-legend-dot is-progresso"></i>Em progresso</span>
            <span><i class="activity-calendar-legend-dot is-bloqueado"></i>Sem atividade</span>
          </div>
          <div class="activity-calendar-grid">${days}</div>
        </section>
      </main>

      <aside class="activity-calendar-sidebar">
        <section class="activity-calendar-side-card activity-calendar-focus-card">
          <div class="activity-calendar-side-heading"><p class="activity-calendar-eyebrow activity-calendar-eyebrow-cyan">Foco de hoje</p><span class="activity-calendar-side-mark">◎</span></div>
          <div class="activity-calendar-focus-day"><strong>${currentDay ? `Dia ${currentDay.dayOfMonth}` : "—"}</strong><span>${currentStatusLabel}</span></div>
          <div class="activity-calendar-focus-points"><strong>${Number(currentDay?.points || 0)}</strong><span>/${Number(data.dailyGoal || 0)} pontos acumulados</span></div>
          <p>${currentDay?.goalReached ? "A meta de hoje foi alcançada." : "Continue ativo para alcançar a meta diária."}</p>
        </section>
        <section class="activity-calendar-side-card">
          <div class="activity-calendar-side-heading"><p class="activity-calendar-eyebrow">Ritmo do mês</p><span class="activity-calendar-side-mark">✦</span></div>
          <div class="activity-calendar-mini-stats"><div><strong>${completed}</strong><span>dias resgatados</span></div><div><strong>${Number(data.dailyGoal || 0)}</strong><span>meta diária</span></div></div>
          <p class="activity-calendar-side-copy">Mantenha uma rotina constante para completar o calendário e liberar a recompensa final.</p>
        </section>
        <section class="activity-calendar-side-card activity-calendar-reward-card">
          <p class="activity-calendar-eyebrow activity-calendar-eyebrow-amber">Recompensa mensal</p>
          <h3>Baú de conclusão</h3>
          <p>${data.monthlyRewardClaimed ? "Recompensa já resgatada neste ciclo." : data.monthlyCompletionEligible ? "O baú está liberado para resgate." : "Complete todos os dias do mês para liberar."}</p>
          <span class="activity-calendar-reward-status ${data.monthlyRewardClaimed ? "is-claimed" : data.monthlyCompletionEligible ? "is-ready" : "is-progress"}">${data.monthlyRewardClaimed ? "Resgatado" : data.monthlyCompletionEligible ? "Disponível" : "Em andamento"}</span>
        </section>
      </aside>
    </div>
  </div>`;
}

async function claimActivityDay(date) {
  try {
    const chestCode = activityCalendarState && activityCalendarState.rewardChestCode;
    activityCalendarState = await apiPost(`/activity-calendar/days/${date}/claim`);
    renderActivityCalendar(activityCalendarState);
    showActivityCalendarRewardModal(chestCode, "Recompensa diária recebida", "Dia concluído!");
  } catch (err) { showToast(err.message, "error"); }
}
async function claimActivityMonthly(yearMonth) {
  try {
    const chestCode = activityCalendarState && activityCalendarState.monthlyCompletionChestCode;
    activityCalendarState = await apiPost(`/activity-calendar/months/${yearMonth}/claim-completion`);
    renderActivityCalendar(activityCalendarState);
    showActivityCalendarRewardModal(chestCode, "Baú exclusivo recebido", "Mês concluído!");
  } catch (err) { showToast(err.message, "error"); }
}

function showActivityCalendarRewardModal(chestCode, eyebrow, title) {
  const existing = document.getElementById("activity-calendar-reward-modal");
  if (existing) existing.remove();
  const code = String(chestCode || "").trim();
  const chestLabel = code === "CHEST_ACTIVITY_CALENDAR_MONTHLY" ? "Baú de Conclusão Mensal" : "Baú do Calendário de Atividades";
  const openButton = code && typeof missionOpenRewardChest === "function"
    ? `<button type="button" class="btn-sm btn-secondary" onclick="document.getElementById('activity-calendar-reward-modal')?.remove(); missionOpenRewardChest('${escapeAttr(code)}', 1, this)">Abrir baú</button>`
    : "";
  const overlay = document.createElement("div");
  overlay.id = "activity-calendar-reward-modal";
  overlay.className = "fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/75";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.setAttribute("aria-labelledby", "activity-calendar-reward-title");
  overlay.innerHTML = `<div class="card w-full max-w-lg" onclick="event.stopPropagation()"><div class="flex items-start justify-between gap-4 mb-5"><div><p class="text-xs uppercase tracking-wider text-cyan-400 font-bold">${eyebrow}</p><h3 id="activity-calendar-reward-title" class="text-xl font-bold mt-1">${title}</h3></div><button class="text-slate-400 hover:text-white text-2xl leading-none" aria-label="Fechar" onclick="document.getElementById('activity-calendar-reward-modal')?.remove()">&times;</button></div><div class="flex items-center gap-3 rounded-lg border border-cyan-700 bg-cyan-950/40 px-3 py-4">${renderChestIcon("w-14 h-14")}<div class="min-w-0 flex-1"><p class="font-semibold text-cyan-200">${chestLabel}</p><p class="text-xs text-cyan-400 mt-1">Abra agora ou encontre o baú no Inventário.</p></div><div>${openButton}</div></div><button class="btn-primary w-full mt-5" onclick="document.getElementById('activity-calendar-reward-modal')?.remove()">Continuar</button></div>`;
  overlay.addEventListener("click", event => { if (event.target === overlay) overlay.remove(); });
  document.body.appendChild(overlay);
}
