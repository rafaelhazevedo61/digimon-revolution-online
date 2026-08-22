// Tutorial inicial — card "Primeiros Passos" no dashboard.
// As etapas são concluídas automaticamente pelos fluxos do jogo, mas suas
// recompensas ficam pendentes até o jogador resgatá-las manualmente.

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

  if (!data || data.finished) {
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
  const rows = data.steps
    .map(s => renderTutorialStep(s, nextStep && s.step === nextStep.step))
    .join("");

  const finishSection = data.canFinish
    ? `
      <div class="mt-3 pt-3 border-t border-slate-700">
        <p class="text-xs text-green-300 mb-2">Todas as etapas foram concluídas e as recompensas foram resgatadas.</p>
        <button class="btn-primary w-full" onclick="tutorialFinish()">Finalizar tutorial</button>
      </div>
    `
    : data.allCompleted && data.pendingRewards > 0
      ? `<p class="text-xs text-yellow-400 mt-3 pt-3 border-t border-slate-700">Resgate todas as recompensas pendentes para finalizar o tutorial.</p>`
      : "";

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
      ${finishSection}
    </div>
  `;
}

function renderTutorialStep(step, isNext) {
  const route = TUTORIAL_STEP_ROUTE[step.step] || "dashboard";
  const icon = TUTORIAL_STEP_ICON[step.step] || "•";
  const hasReward = tutorialStepHasReward(step);
  const reward = tutorialRewardLabel(step);
  const completed = !!step.completed;
  const claimed = !hasReward || !!step.rewardClaimed;

  const stateIcon = !completed
    ? `<span class="text-slate-600 text-lg" aria-label="Pendente">○</span>`
    : claimed
      ? `<span class="text-green-400 text-lg" aria-label="Concluída e resgatada">✓</span>`
      : `<span class="text-yellow-400 text-lg" aria-label="Recompensa pendente">🎁</span>`;

  const clickable = completed ? "" : `onclick="navigateTo('${route}')" style="cursor:pointer"`;
  const highlight = isNext
    ? "border border-cyan-600 bg-slate-800"
    : completed && !claimed
      ? "border border-yellow-700/60 bg-slate-800/70"
      : "bg-slate-900/40";
  const titleClass = claimed
    ? "text-slate-500 line-through"
    : completed
      ? "text-yellow-100"
      : "text-slate-200";

  const rewardAction = !completed
    ? ""
    : !claimed
      ? `<button class="btn-sm btn-primary shrink-0" onclick="tutorialClaimReward(event, '${escapeHtml(step.step)}')">Resgatar</button>`
      : hasReward
        ? `<span class="text-xs text-green-400 shrink-0">Resgatada</span>`
        : "";

  return `
    <div class="flex items-start gap-3 px-2 py-2 rounded-lg ${highlight}" ${clickable}>
      <span class="shrink-0 mt-0.5">${stateIcon}</span>
      <div class="text-xl shrink-0">${icon}</div>
      <div class="flex-1 min-w-0">
        <p class="text-sm font-bold leading-snug break-words ${titleClass}">${escapeHtml(step.title)}</p>
        ${!completed || !claimed ? `<p class="text-xs text-slate-400 leading-snug whitespace-normal break-words">${escapeHtml(step.description)}</p>` : ""}
        ${!completed && reward ? `<p class="text-xs text-yellow-500 mt-0.5 leading-snug whitespace-normal break-words">Recompensa: ${reward}</p>` : ""}
        ${completed && !claimed && reward ? `<p class="text-xs text-yellow-400 mt-0.5 leading-snug whitespace-normal break-words">Recompensa pendente: ${reward}</p>` : ""}
      </div>
      ${rewardAction}
      ${!completed && isNext ? `<span class="text-cyan-400 text-lg shrink-0 mt-0.5">›</span>` : ""}
    </div>
  `;
}

function tutorialStepHasReward(step) {
  return Number(step.rewardBits || 0) > 0
    || (!!step.rewardItem && Number(step.rewardItemQuantity || 0) > 0);
}

async function tutorialClaimReward(event, stepName) {
  if (event) event.stopPropagation();
  const button = event && event.currentTarget;
  if (button) {
    button.disabled = true;
    button.textContent = "Resgatando...";
  }

  try {
    const data = await apiPost(`/tutorial/steps/${encodeURIComponent(stepName)}/claim`);
    showToast("Recompensa do tutorial resgatada!");
    const container = document.getElementById("tutorial-card");
    if (container) container.innerHTML = data && !data.finished ? renderTutorialCard(data) : "";
  } catch (err) {
    showToast(err.message, "error");
    if (button) {
      button.disabled = false;
      button.textContent = "Resgatar";
    }
  }
}

async function tutorialFinish() {
  if (!window.confirm("Finalizar o tutorial? Esta ação encerra o card de Primeiros Passos.")) {
    return;
  }

  try {
    await apiPost("/tutorial/finish");
    showToast("Tutorial finalizado!");
    await loadTutorialCard();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function tutorialRewardLabel(step) {
  const parts = [];
  if (step.rewardBits > 0) parts.push(`${step.rewardBits} bits`);
  if (step.rewardItem && step.rewardItemQuantity > 0) {
    parts.push(`${step.rewardItemQuantity}× ${formatItemType(step.rewardItem)}`);
  }
  return parts.join(" + ");
}
