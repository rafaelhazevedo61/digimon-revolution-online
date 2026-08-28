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
  const completed = Number(data.claimedDays || 0);
  const total = Number(data.daysInMonth || 0);
  const percent = total ? Math.round(completed * 100 / total) : 0;
  const days = (data.days || []).map(day => {
    const status = day.rewardClaimed ? "resgatado" : day.goalReached ? "disponivel" : day.points > 0 ? "progresso" : "bloqueado";
    const action = day.goalReached && !day.rewardClaimed ? `<button class="btn-sm btn-primary mt-2" onclick="claimActivityDay('${day.date}')">Resgatar</button>` : "";
    const isToday = day.date === data.currentDate;
    const todayClass = isToday ? "ring-2 ring-cyan-400 bg-cyan-950/50 shadow-lg shadow-cyan-900/30" : "";
    const todayLabel = isToday ? `<span class="inline-block rounded-full bg-cyan-400 px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wide text-slate-950">Hoje</span>` : "";
    return `<div class="card-sm text-center activity-day activity-day-${status} ${todayClass}">${todayLabel}<p class="text-xs ${isToday ? "font-bold text-cyan-200" : "text-slate-400"}">Dia ${day.dayOfMonth}</p><p class="text-xl font-bold mt-1 ${isToday ? "text-cyan-100" : ""}">${day.points}<span class="text-xs text-slate-500">/${data.dailyGoal}</span></p><p class="text-[10px] uppercase text-slate-500 mt-1">${day.rewardClaimed ? "Resgatado" : day.goalReached ? "Disponível" : "Em progresso"}</p>${action}</div>`;
  }).join("");
  app.innerHTML = `<div class="page-container"><div class="flex items-center justify-between mb-4"><div><h2 class="text-lg font-bold">Calendário de Atividades</h2><p class="text-xs text-slate-400">${escapeHtml(data.yearMonth)} · ${data.dailyGoal} pontos por dia</p></div><button class="btn-sm" onclick="navigateTo('more')">Voltar</button></div><div class="card mb-4"><div class="flex justify-between text-sm"><span>Conclusão mensal</span><strong class="text-cyan-400">${completed}/${total}</strong></div><div class="w-full h-2 bg-slate-800 rounded-full mt-3"><div class="h-2 bg-cyan-500 rounded-full" style="width:${percent}%"></div></div><p class="text-xs text-slate-400 mt-3">${data.monthlyRewardClaimed ? "Baú mensal resgatado." : data.monthlyCompletionEligible ? "Você completou todos os dias." : "Resgate todos os dias para liberar o baú exclusivo."}</p>${data.monthlyCompletionEligible && !data.monthlyRewardClaimed ? `<button class="btn-primary w-full mt-3" onclick="claimActivityMonthly('${data.yearMonth}')">Resgatar baú de conclusão mensal</button>` : ""}</div><div class="grid grid-cols-4 gap-2">${days}</div></div>`;
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
  overlay.innerHTML = `<div class="card w-full max-w-lg" onclick="event.stopPropagation()"><div class="flex items-start justify-between gap-4 mb-5"><div><p class="text-xs uppercase tracking-wider text-cyan-400 font-bold">${eyebrow}</p><h3 id="activity-calendar-reward-title" class="text-xl font-bold mt-1">${title}</h3></div><button class="text-slate-400 hover:text-white text-2xl leading-none" aria-label="Fechar" onclick="document.getElementById('activity-calendar-reward-modal')?.remove()">&times;</button></div><div class="flex items-center gap-3 rounded-lg border border-cyan-700 bg-cyan-950/40 px-3 py-4"><span class="text-3xl" aria-hidden="true">🎁</span><div class="min-w-0 flex-1"><p class="font-semibold text-cyan-200">${chestLabel}</p><p class="text-xs text-cyan-400 mt-1">Abra agora ou encontre o baú no Inventário.</p></div><div>${openButton}</div></div><button class="btn-primary w-full mt-5" onclick="document.getElementById('activity-calendar-reward-modal')?.remove()">Continuar</button></div>`;
  overlay.addEventListener("click", event => { if (event.target === overlay) overlay.remove(); });
  document.body.appendChild(overlay);
}
