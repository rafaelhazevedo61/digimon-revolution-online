// Tutorial inicial — card "Primeiros Passos" no dashboard.
// Busca o progresso em GET /tutorial e some quando todos os steps estao concluidos.

const TUTORIAL_STEP_ROUTE = {
  SELECT_DIGITAMA: "starter",
  HATCH_DIGIMON: "incubation",
  COMPLETE_MISSION: "missions",
  BUY_SHOP: "shop",
  EQUIP_ITEM: "inventory",
  EVOLVE_DIGIMON: "evolution"
};

const TUTORIAL_STEP_ICON = {
  SELECT_DIGITAMA: "🥚",
  HATCH_DIGIMON: "🐣",
  COMPLETE_MISSION: "🗺️",
  BUY_SHOP: "🛒",
  EQUIP_ITEM: "⚔️",
  EVOLVE_DIGIMON: "⚡"
};

async function loadTutorialCard() {
  const container = document.getElementById("tutorial-card");
  if (!container) return;

  let data;
  try {
    data = await apiGet("/tutorial");
  } catch (err) {
    return;
  }

  if (!data || data.allCompleted) {
    container.innerHTML = "";
    return;
  }

  container.innerHTML = renderTutorialCard(data);
}

function renderTutorialCard(data) {
  const percent = data.totalSteps > 0
    ? Math.round((data.completedSteps / data.totalSteps) * 100)
    : 0;

  const nextStep = data.steps.find(s => !s.completed);

  const rows = data.steps.map(s => renderTutorialStep(s, nextStep && s.step === nextStep.step)).join("");

  return `
    <div class="card mb-4 border-cyan-800">
      <div class="flex items-center justify-between mb-2">
        <h3 class="text-sm font-bold text-cyan-300">🎓 Primeiros Passos</h3>
        <span class="text-xs text-slate-400">${data.completedSteps}/${data.totalSteps}</span>
      </div>
      <div class="xp-bar mb-3">
        <div class="xp-bar-fill" style="width:${percent}%"></div>
      </div>
      <div class="flex flex-col gap-2">
        ${rows}
      </div>
    </div>
  `;
}

function renderTutorialStep(step, isNext) {
  const route = TUTORIAL_STEP_ROUTE[step.step] || "dashboard";
  const icon = TUTORIAL_STEP_ICON[step.step] || "•";

  const reward = tutorialRewardLabel(step);

  const stateIcon = step.completed
    ? `<span class="text-green-400 text-lg">✓</span>`
    : `<span class="text-slate-600 text-lg">○</span>`;

  const clickable = step.completed ? "" : `onclick="navigateTo('${route}')" style="cursor:pointer"`;

  const highlight = isNext ? "border border-cyan-600 bg-slate-800" : "bg-slate-900/40";
  const titleClass = step.completed ? "text-slate-500 line-through" : "text-slate-200";

  return `
    <div class="flex items-center gap-3 px-2 py-2 rounded-lg ${highlight}" ${clickable}>
      ${stateIcon}
      <div class="text-xl">${icon}</div>
      <div class="flex-1 min-w-0">
        <p class="text-sm font-bold ${titleClass} truncate">${escapeHtml(step.title)}</p>
        ${step.completed ? "" : `<p class="text-xs text-slate-400 truncate">${escapeHtml(step.description)}</p>`}
        ${step.completed || !reward ? "" : `<p class="text-xs text-yellow-500 mt-0.5">Recompensa: ${reward}</p>`}
      </div>
      ${step.completed || !isNext ? "" : `<span class="text-cyan-400 text-lg">›</span>`}
    </div>
  `;
}

function tutorialRewardLabel(step) {
  const parts = [];
  if (step.rewardBits > 0) parts.push(`${step.rewardBits} bits`);
  if (step.rewardItem && step.rewardItemQuantity > 0) {
    parts.push(`${step.rewardItemQuantity}× ${formatItemType(step.rewardItem)}`);
  }
  return parts.join(" + ");
}
