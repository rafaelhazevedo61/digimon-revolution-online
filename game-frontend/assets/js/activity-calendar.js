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
    return `<div class="card-sm text-center activity-day activity-day-${status}"><p class="text-xs text-slate-400">Dia ${day.dayOfMonth}</p><p class="text-xl font-bold mt-1">${day.points}<span class="text-xs text-slate-500">/${data.dailyGoal}</span></p><p class="text-[10px] uppercase text-slate-500 mt-1">${day.rewardClaimed ? "Resgatado" : day.goalReached ? "Disponível" : "Em progresso"}</p>${action}</div>`;
  }).join("");
  app.innerHTML = `<div class="page-container"><div class="flex items-center justify-between mb-4"><div><h2 class="text-lg font-bold">Calendário de Atividades</h2><p class="text-xs text-slate-400">${escapeHtml(data.yearMonth)} · ${data.dailyGoal} pontos por dia</p></div><button class="btn-sm" onclick="navigateTo('more')">Voltar</button></div><div class="card mb-4"><div class="flex justify-between text-sm"><span>Conclusão mensal</span><strong class="text-cyan-400">${completed}/${total}</strong></div><div class="w-full h-2 bg-slate-800 rounded-full mt-3"><div class="h-2 bg-cyan-500 rounded-full" style="width:${percent}%"></div></div><p class="text-xs text-slate-400 mt-3">${data.monthlyRewardClaimed ? "Baú mensal resgatado." : data.monthlyCompletionEligible ? "Você completou todos os dias." : "Resgate todos os dias para liberar o baú exclusivo."}</p>${data.monthlyCompletionEligible && !data.monthlyRewardClaimed ? `<button class="btn-primary w-full mt-3" onclick="claimActivityMonthly('${data.yearMonth}')">Resgatar baú de conclusão mensal</button>` : ""}</div><div class="grid grid-cols-4 gap-2">${days}</div></div>`;
}

async function claimActivityDay(date) {
  try { activityCalendarState = await apiPost(`/activity-calendar/days/${date}/claim`); renderActivityCalendar(activityCalendarState); showToast("Recompensa diária resgatada!", "success"); }
  catch (err) { showToast(err.message, "error"); }
}
async function claimActivityMonthly(yearMonth) {
  try { activityCalendarState = await apiPost(`/activity-calendar/months/${yearMonth}/claim-completion`); renderActivityCalendar(activityCalendarState); showToast("Baú de conclusão mensal resgatado!", "success"); }
  catch (err) { showToast(err.message, "error"); }
}
