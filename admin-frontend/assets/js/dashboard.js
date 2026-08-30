let dashboardRefreshTimer;

function renderDashboard() {
  setPageHeader(
    "Dashboard",
    "Visão operacional do Digimon Revolution Online"
  );

  const app = document.getElementById("app");
  app.innerHTML = `
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between mb-6">
      <div>
        <h3 class="text-xl font-bold text-slate-100">Resumo operacional</h3>
        <p id="dashboard-generated-at" class="text-sm text-slate-400 mt-1">Carregando métricas...</p>
      </div>
      <button id="dashboard-refresh" class="btn-secondary px-4 py-2">Atualizar dados</button>
    </div>

    <div id="dashboard-system-status" class="card mb-6 border-cyan-900/60">
      <div class="flex flex-wrap items-center gap-3 text-sm text-slate-400">
        <span class="h-2.5 w-2.5 rounded-full bg-amber-400 animate-pulse"></span>
        Carregando status do sistema...
      </div>
    </div>

    <section>
      <div class="flex items-center justify-between mb-3">
        <h3 class="text-lg font-semibold text-cyan-300">Visão geral</h3>
        <span class="text-xs text-slate-500">Dados atuais</span>
      </div>
      <div id="dashboard-metrics" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        ${dashboardMetricSkeleton(7)}
      </div>
    </section>

    <section class="card mt-8 border-cyan-900/60">
      <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between mb-5">
        <div>
          <h3 class="text-lg font-semibold text-cyan-300">Atividade dos jogadores</h3>
          <p class="text-sm text-slate-400 mt-1">Pontos de atividade registrados por mês nos últimos 12 meses.</p>
        </div>
        <span id="dashboard-activity-period" class="text-xs text-slate-500">Carregando...</span>
      </div>
      <div id="dashboard-activity-chart" class="min-h-64 flex items-center justify-center text-sm text-slate-500">Carregando atividade...</div>
    </section>

    <section class="mt-8">
      <div class="flex items-center justify-between mb-3">
        <h3 class="text-lg font-semibold text-amber-300">Alertas de integridade</h3>
        <span id="dashboard-alert-count" class="text-xs text-slate-500">Verificando...</span>
      </div>
      <div id="dashboard-alerts" class="space-y-3">
        <div class="card text-slate-400">Verificando consistência dos dados...</div>
      </div>
    </section>

    <section class="card mt-8 border-slate-700">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <div>
          <h3 class="text-lg font-semibold text-slate-100">Atalhos administrativos</h3>
          <p class="text-sm text-slate-400 mt-1">Acesse rapidamente os catálogos e ferramentas de operação.</p>
        </div>
        <div class="flex flex-wrap gap-2">
          <button class="btn-secondary" onclick="navigateTo('players')">Jogadores</button>
          <button class="btn-secondary" onclick="navigateTo('items')">Catálogo de itens</button>
          <button class="btn-secondary" onclick="navigateTo('tools')">Grant / XP</button>
          <button class="btn-secondary" onclick="navigateTo('loot-tables')">Loot Tables</button>
        </div>
      </div>
    </section>

    <details class="card mt-8 border-red-900/60 bg-red-950/10">
      <summary class="cursor-pointer list-none flex items-center justify-between gap-4">
        <span>
          <strong class="text-red-300">Zona de Perigo</strong>
          <span class="block text-sm text-slate-400 mt-1">Operações destrutivas e irreversíveis sobre dados de jogadores.</span>
        </span>
        <span class="text-red-400 text-sm">Abrir</span>
      </summary>
      <div class="mt-5 border-t border-red-900/50 pt-4">
        <p class="text-sm text-slate-300 leading-relaxed mb-4">
          O wipe remove jogadores, Digimons, inventários, equipamentos, missões, incubações e tentativas de boss. Conteúdos do jogo são preservados.
        </p>
        <button id="btn-wipe" class="px-4 py-2 rounded font-semibold text-white bg-red-600 hover:bg-red-700 transition-colors">
          Executar wipe de jogadores
        </button>
      </div>
    </details>
  `;

  document.getElementById("dashboard-refresh").addEventListener("click", () => loadDashboardSummary(true));
  document.getElementById("btn-wipe").addEventListener("click", dashboardConfirmWipe);
  loadDashboardSummary();
}

function dashboardMetricSkeleton(count) {
  return Array.from({ length: count }, () => `
    <div class="card animate-pulse">
      <div class="h-3 w-24 bg-slate-700 rounded"></div>
      <div class="h-8 w-20 bg-slate-700 rounded mt-3"></div>
      <div class="h-3 w-32 bg-slate-800 rounded mt-3"></div>
    </div>
  `).join("");
}

async function loadDashboardSummary(showLoading = false) {
  const refreshButton = document.getElementById("dashboard-refresh");
  if (refreshButton) {
    refreshButton.disabled = true;
    refreshButton.textContent = "Atualizando...";
  }
  if (showLoading) {
    document.getElementById("dashboard-metrics").innerHTML = dashboardMetricSkeleton(4);
  }

  try {
    const [summary, activity] = await Promise.all([
      apiGet("/admin/dashboard/summary"),
      apiGet("/admin/dashboard/activity/monthly")
    ]);
    dashboardRenderSummary(summary);
    dashboardRenderActivity(activity);
  } catch (err) {
    document.getElementById("dashboard-system-status").innerHTML =
      `<span class="text-red-400">Não foi possível carregar o dashboard: ${escapeHtml(err.message)}</span>`;
  } finally {
    if (refreshButton) {
      refreshButton.disabled = false;
      refreshButton.textContent = "Atualizar dados";
    }
  }
}

function dashboardRenderSummary(summary) {
  const metrics = [
    { title: "Jogadores", value: summary.players.total, detail: `${summary.players.secondary} sem Digimon ativo`, tone: "cyan" },
    { title: "Digimons", value: summary.digimons.total, detail: `${summary.digimons.secondary} ativos`, tone: "violet" },
    { title: "Inventário", value: summary.inventory.total, detail: `${summary.inventory.secondary} unidades em stacks`, tone: "emerald" },
    { title: "Equipamentos", value: summary.equipment.total, detail: `${summary.equipment.secondary} equipados`, tone: "purple" },
    { title: "Itens cadastrados", value: summary.items.total, detail: "Itens disponíveis no catálogo", tone: "blue" },
    { title: "Templates de equipamento", value: summary.equipmentTemplates.total, detail: "Modelos disponíveis no catálogo", tone: "fuchsia" },
    { title: "Loot Tables", value: summary.lootTables.total, detail: `${summary.lootTables.secondary} ativas`, tone: "amber" }
  ];
  document.getElementById("dashboard-metrics").innerHTML = metrics.map(dashboardMetricCard).join("");

  const generatedAt = summary.system?.generatedAt ? new Date(summary.system.generatedAt).toLocaleString("pt-BR") : "agora";
  document.getElementById("dashboard-generated-at").textContent = `Atualizado em ${generatedAt}`;
  document.getElementById("dashboard-system-status").innerHTML = `
    <div class="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm">
      <span class="inline-flex items-center gap-2 text-emerald-300"><span class="h-2.5 w-2.5 rounded-full bg-emerald-400"></span>Sistema operacional</span>
      <span class="text-slate-500">Endpoint: /admin/dashboard/summary</span>
      <span class="text-slate-500">${summary.alerts.length} alerta(s) ativo(s)</span>
    </div>`;

  document.getElementById("dashboard-alert-count").textContent = summary.alerts.length === 0 ? "Tudo normal" : `${summary.alerts.length} alerta(s)`;
  document.getElementById("dashboard-alerts").innerHTML = summary.alerts.length === 0
    ? `<div class="card border-emerald-900/50 bg-emerald-950/10"><span class="text-emerald-300">Nenhuma inconsistência detectada.</span></div>`
    : summary.alerts.map(dashboardAlertCard).join("");
}

function dashboardRenderActivity(data) {
  const chart = document.getElementById("dashboard-activity-chart");
  const period = document.getElementById("dashboard-activity-period");
  if (!chart || !data) return;
  period.textContent = `${dashboardFormatMonth(data.from)} – ${dashboardFormatMonth(data.to)}`;
  const byMonth = new Map((data.months || []).map(month => [month.yearMonth, month]));
  const first = dashboardParseMonth(data.from);
  const rows = Array.from({ length: 12 }, (_, index) => {
    const date = new Date(first.getFullYear(), first.getMonth() + index, 1);
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
    return { key, label: date.toLocaleDateString("pt-BR", { month: "short" }).replace(".", ""), ...(byMonth.get(key) || { points: 0, activePlayers: 0, events: 0 }) };
  });
  const maxPoints = Math.max(1, ...rows.map(row => Number(row.points) || 0));
  chart.innerHTML = `<div class="w-full overflow-x-auto pb-2"><div class="min-w-[720px]">
    <div class="h-52 flex items-end gap-2 border-b border-l border-slate-700 px-3">
      ${rows.map(row => {
        const points = Number(row.points) || 0;
        const height = points === 0 ? 2 : Math.max(5, Math.round((points / maxPoints) * 100));
        return `<div class="h-full flex-1 flex flex-col justify-end items-center gap-2 group" title="${row.key}: ${points.toLocaleString("pt-BR")} pontos | ${Number(row.activePlayers).toLocaleString("pt-BR")} jogadores ativos">
          <span class="text-[10px] text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity">${points.toLocaleString("pt-BR")}</span>
          <div class="w-full max-w-10 rounded-t bg-cyan-500/80 hover:bg-cyan-400 transition-colors" style="height:${height}%"></div>
        </div>`;
      }).join("")}
    </div>
    <div class="flex gap-2 px-3 mt-2">${rows.map(row => `<span class="flex-1 text-center text-[10px] uppercase text-slate-500">${row.label}</span>`).join("")}</div>
    <div class="flex flex-wrap gap-x-5 gap-y-1 mt-4 text-xs text-slate-400"><span><strong class="text-cyan-300">${rows.reduce((sum, row) => sum + Number(row.points || 0), 0).toLocaleString("pt-BR")}</strong> pontos no período</span><span><strong class="text-emerald-300">${Math.max(...rows.map(row => Number(row.activePlayers || 0))).toLocaleString("pt-BR")}</strong> jogadores ativos no pico mensal</span><span><strong class="text-violet-300">${rows.reduce((sum, row) => sum + Number(row.events || 0), 0).toLocaleString("pt-BR")}</strong> eventos</span></div>
  </div></div>`;
}

function dashboardParseMonth(yearMonth) {
  const [year, month] = String(yearMonth || "").split("-").map(Number);
  return new Date(year || new Date().getFullYear(), (month || 1) - 1, 1);
}

function dashboardFormatMonth(yearMonth) {
  return dashboardParseMonth(yearMonth).toLocaleDateString("pt-BR", { month: "short", year: "numeric" }).replace(".", "");
}

function dashboardMetricCard(metric) {
  const tone = { cyan: "text-cyan-300", violet: "text-violet-300", emerald: "text-emerald-300", purple: "text-purple-300", blue: "text-blue-300", fuchsia: "text-fuchsia-300", amber: "text-amber-300" }[metric.tone] || "text-slate-100";
  return `<div class="card border-slate-700"><p class="text-sm text-slate-400">${metric.title}</p><h4 class="text-3xl font-bold mt-2 ${tone}">${Number(metric.value).toLocaleString("pt-BR")}</h4><p class="text-xs text-slate-500 mt-2">${metric.detail}</p></div>`;
}

function dashboardAlertCard(alert) {
  const critical = alert.severity === "ERROR";
  return `<div class="card ${critical ? "border-red-900/70 bg-red-950/10" : "border-amber-900/70 bg-amber-950/10"}">
    <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
      <div><h4 class="font-semibold ${critical ? "text-red-300" : "text-amber-300"}">${escapeHtml(alert.title)}</h4><p class="text-sm text-slate-400 mt-1">${escapeHtml(alert.message)}</p></div>
      <span class="text-2xl font-bold ${critical ? "text-red-300" : "text-amber-300"}">${Number(alert.count).toLocaleString("pt-BR")}</span>
    </div>
  </div>`;
}

function dashboardConfirmWipe() {
  openConfirmModal({
    title: "Executar wipe de jogadores",
    message: "Esta operação apagará os dados de jogadores, Digimons, inventários, equipamentos, missões, incubações e tentativas de boss. Deseja continuar?",
    confirmText: "Continuar",
    cancelText: "Cancelar",
    onConfirm: () => new Promise(resolve => {
      openConfirmModal({
        title: "Confirmação final do wipe",
        message: "Esta ação é irreversível. Confirme novamente para apagar todos os dados de jogadores.",
        confirmText: "Executar wipe",
        cancelText: "Cancelar",
        onConfirm: async () => {
          try {
            await apiPostVoid("/admin/players/wipe", { confirmation: "WIPE" });
            await loadDashboardSummary(true);
            resolve();
          } catch (err) {
            alert("Erro ao executar wipe: " + err.message);
            resolve();
          }
        }
      });
    })
  });
}
