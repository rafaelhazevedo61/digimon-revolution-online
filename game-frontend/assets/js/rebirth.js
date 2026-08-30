let rebirthDigimonId = null;
let rebirthDigimon = null;
let rebirthPreview = null;
let rebirthCodeAllocation = { hp: 0, attack: 0, defense: 0 };
let rebirthEquippedEquipmentCount = 0;
let rebirthPreserveRarity = false;

async function renderRebirthPage() {
  const app = document.getElementById("app");
  showBottomNav("dashboard");
  rebirthCodeAllocation = { hp: 0, attack: 0, defense: 0 };
  rebirthEquippedEquipmentCount = 0;
  rebirthPreserveRarity = false;

  app.innerHTML = `
    <div class="page-container">
      <header class="progression-page-header mb-4">
        <div>
          <p class="progression-eyebrow progression-eyebrow-amber">Sistema de progressão</p>
          <h2 class="progression-page-title">Renascimento</h2>
          <p class="progression-page-subtitle">Recomece com mais potencial e preserve o legado do seu Digimon.</p>
        </div>
        <button class="progression-back-button" onclick="navigateTo('dashboard')"><span aria-hidden="true">←</span> Voltar</button>
      </header>
      <div id="rebirth-content">
        <div class="card animate-pulse"><div class="h-32"></div></div>
      </div>
    </div>
  `;

  try {
    const dashboard = await apiGet("/players/me/dashboard");
    const d = dashboard.activeDigimon;
    if (!d) {
      document.getElementById("rebirth-content").innerHTML = `
        <div class="card"><p class="text-slate-400 text-center">Nenhum Digimon ativo.</p></div>
      `;
      return;
    }

    rebirthDigimonId = d.id;
    rebirthDigimon = d;
    rebirthPreview = await apiGet(`/digimon/${d.id}/rebirth-preview`, { preserveRarity: false });
    rebirthEquippedEquipmentCount = Number(rebirthPreview.equippedEquipmentCount || 0);
    rebirthRender(d, rebirthEquippedEquipmentCount);
  } catch (err) {
    document.getElementById("rebirth-content").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

async function rebirthTogglePreserveRarity(checked) {
  if (!rebirthDigimonId || !rebirthDigimon || !rebirthPreview) return;

  const previousValue = rebirthPreserveRarity;
  rebirthPreserveRarity = checked;
  const checkbox = document.getElementById("rebirth-preserve-rarity");
  if (checkbox) checkbox.disabled = true;

  try {
    rebirthPreview = await apiGet(
      `/digimon/${rebirthDigimonId}/rebirth-preview`,
      { preserveRarity: checked }
    );
    rebirthEquippedEquipmentCount = Number(rebirthPreview.equippedEquipmentCount || 0);
    rebirthRender(rebirthDigimon, rebirthEquippedEquipmentCount);
  } catch (err) {
    rebirthPreserveRarity = previousValue;
    if (checkbox) {
      checkbox.checked = previousValue;
      checkbox.disabled = false;
    }
    showToast(err.message, "error");
  }
}

function rebirthRender(digimon, equippedEquipmentCount = 0) {
  const content = document.getElementById("rebirth-content");
  const p = rebirthPreview;
  const availableCodeInfinite = Math.max(0, Number(p.currentCodeInfinite ?? 0));
  const rarityPreservationQuantity = Math.max(0, Number(p.rarityPreservationItemQuantity ?? 0));
  const pendingCompletedMissionCount = Math.max(0, Number(p.pendingCompletedMissionCount ?? 0));
  const codeInfiniteDisabled = availableCodeInfinite <= 0 ? "disabled" : "";
  const codeInfiniteMax = Math.min(100, availableCodeInfinite);

  const stageMap = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  const formatStg = s => stageMap[s] || s;
  const rarityMap = { COMMON: "Comum", RARE: "Rara", EPIC: "Épica", LEGENDARY: "Lendária" };
  const currentRarity = p.currentRarity || digimon.rarity;
  const currentRarityLabel = rarityMap[currentRarity] || currentRarity || "Não informada";

  let html = `
    <section class="progression-hero progression-hero-amber mb-4">
      <div class="progression-hero-topline"><span class="progression-hero-kicker">Digimon selecionado</span><span class="progression-hero-status">Ciclo atual</span></div>
      <div class="flex items-center gap-3">
        <div class="progression-hero-visual">${renderDigimonVisual(digimon.imageUrl, digimon.stage, "w-14 h-14", "text-4xl")}</div>
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <h3 class="font-bold text-lg truncate">${escapeHtml(digimon.name)}</h3>
            <span class="text-sm font-bold text-cyan-400">Lv.${digimon.level}</span>
          </div>
          <div class="flex gap-2 mt-1 flex-wrap">
            <span class="badge badge-${digimon.stage.toLowerCase()}">${formatStg(digimon.stage)}</span>
            ${digimon.attribute ? `<span class="badge badge-common">${escapeHtml(digimon.attribute)}</span>` : ""}
            ${digimon.element ? `<span class="badge badge-common">${escapeHtml(digimon.element)}</span>` : ""}
          </div>
          ${p.currentRebirthCount > 0 ? `<p class="text-xs text-amber-400 mt-1">🔄 Renascimento x${p.currentRebirthCount}</p>` : ""}
        </div>
      </div>
    </section>

    <section class="rebirth-panel mb-4">
      <div class="rebirth-panel-heading"><div><p class="progression-eyebrow progression-eyebrow-amber">Ritual de renascimento</p><h3 class="progression-panel-title">Renascimento #${p.newRebirthCount}</h3></div><span class="rebirth-level-badge">+${Math.round((p.statMultiplier - 1) * 100)}% stats</span></div>
      <p class="text-xs text-slate-400 mb-4">O Digimon renasce como um novo ovo, mantendo bônus de IV e stats acumulados.</p>

      <div class="mb-4">
        <div class="rebirth-block-heading"><p class="rebirth-block-title">Custo do ritual</p><span class="rebirth-block-note">Recursos necessários</span></div>
        <div class="grid grid-cols-3 gap-2">
          <div class="rebirth-cost-card">
            <p class="text-xs text-slate-500">Bits</p>
            <p class="font-bold ${p.currentBits >= p.costBits ? 'text-yellow-400' : 'text-red-400'}">${p.costBits.toLocaleString()}</p>
            <p class="text-xs ${p.currentBits >= p.costBits ? 'text-slate-500' : 'text-red-400'}">Você tem: ${p.currentBits.toLocaleString()}</p>
          </div>
          <div class="rebirth-cost-card">
            <p class="text-xs text-slate-500">Data Core</p>
            <p class="font-bold ${p.currentDataCore >= p.costDataCore ? 'text-purple-400' : 'text-red-400'}">${p.costDataCore}</p>
            <p class="text-xs ${p.currentDataCore >= p.costDataCore ? 'text-slate-500' : 'text-red-400'}">Você tem: ${p.currentDataCore}</p>
          </div>
          <div class="rebirth-cost-card">
            <p class="text-xs text-slate-500">Dados Digitais</p>
            <p class="font-bold ${p.currentDigitalData >= p.costDigitalData ? 'text-cyan-400' : 'text-red-400'}">${p.costDigitalData}</p>
            <p class="text-xs text-slate-500">Você tem: ${p.currentDigitalData}</p>
          </div>
        </div>
      </div>

      <div class="mb-4">
        <div class="rebirth-block-heading"><p class="rebirth-block-title">Bônus de atributos</p><span class="rebirth-block-note">Impacto permanente</span></div>
        <div class="rebirth-bonus-card text-center">
          <p class="text-xs text-slate-500">Multiplicador</p>
          <p class="font-bold text-green-400">x${p.statMultiplier.toFixed(2)}</p>
          <p class="text-xs text-slate-500">+${Math.round((p.statMultiplier - 1) * 100)}% em HP, ATK e DEF</p>
        </div>
      </div>

      <div class="rebirth-code-section mb-4">
        <div class="rebirth-block-heading">
          <div><p class="rebirth-block-title">Refinar IV</p><p class="rebirth-block-note mt-1">Invista Código Infinito nos atributos</p></div>
          <span class="rebirth-code-available">${availableCodeInfinite} disponíveis</span>
        </div>
        <div class="flex justify-between items-center mb-2 hidden">
          <p class="text-xs font-bold text-slate-300">Refinar IV com Código Infinito</p>
          <span class="text-xs font-bold text-violet-300">Disponíveis: ${availableCodeInfinite}</span>
        </div>
        <p class="text-xs text-slate-500 mb-3">A cada 10 códigos investidos, o IV mínimo do atributo sobe 1 ponto. Limite: 100 por atributo.</p>

        <div class="grid grid-cols-2 gap-2 mb-3">
          <button class="btn-secondary text-xs" ${codeInfiniteDisabled} onclick="rebirthDistributeCodes('balanced')">Equilibrar</button>
          <button class="btn-secondary text-xs" ${codeInfiniteDisabled} onclick="rebirthDistributeCodes('hp')">Focar HP</button>
          <button class="btn-secondary text-xs" ${codeInfiniteDisabled} onclick="rebirthDistributeCodes('attack')">Focar ATK</button>
          <button class="btn-secondary text-xs" ${codeInfiniteDisabled} onclick="rebirthDistributeCodes('defense')">Focar DEF</button>
        </div>

        <div class="grid grid-cols-1 gap-2">
          ${rebirthCodeAttributeCard("hp", "HP", "red", p.hpIvRange.min, codeInfiniteDisabled, codeInfiniteMax)}
          ${rebirthCodeAttributeCard("attack", "ATK", "orange", p.attackIvRange.min, codeInfiniteDisabled, codeInfiniteMax)}
          ${rebirthCodeAttributeCard("defense", "DEF", "blue", p.defenseIvRange.min, codeInfiniteDisabled, codeInfiniteMax)}
        </div>

        <div class="rebirth-code-summary mt-3 text-center">
          <p id="code-allocation-summary" class="text-xs text-slate-400">Nenhum Código Infinito será usado</p>
          <p class="text-xs text-slate-500 mt-1">Restantes: <span id="code-infinite-remaining" class="font-bold text-violet-300">${availableCodeInfinite}</span></p>
        </div>
        ${availableCodeInfinite === 0 ? '<p class="text-xs text-amber-400 mt-2">Você não possui Códigos Infinitos disponíveis para investir.</p>' : ''}
      </div>

      <div class="mb-4">
        <div class="rebirth-block-heading"><p class="rebirth-block-title">IVs após renascer</p><span class="rebirth-block-note">mínimo — máximo</span></div>
        <div class="grid grid-cols-3 gap-2">
          <div class="rebirth-result-card text-center">
            <p class="text-xs text-slate-500">HP</p>
            <p class="text-sm font-bold text-red-400"><span id="rebirth-hp-min">${p.hpIvRange.min}</span> — ${p.hpIvRange.max}</p>
            <p class="text-xs text-slate-600">Atual: ${digimon.ivHp}</p>
          </div>
          <div class="rebirth-result-card text-center">
            <p class="text-xs text-slate-500">ATK</p>
            <p class="text-sm font-bold text-orange-400"><span id="rebirth-attack-min">${p.attackIvRange.min}</span> — ${p.attackIvRange.max}</p>
            <p class="text-xs text-slate-600">Atual: ${digimon.ivAttack}</p>
          </div>
          <div class="rebirth-result-card text-center">
            <p class="text-xs text-slate-500">DEF</p>
            <p class="text-sm font-bold text-blue-400"><span id="rebirth-defense-min">${p.defenseIvRange.min}</span> — ${p.defenseIvRange.max}</p>
            <p class="text-xs text-slate-600">Atual: ${digimon.ivDefense}</p>
          </div>
        </div>
      </div>

      ${p.eligible ? `
      <div class="mb-4">
        <div class="flex justify-between text-xs text-slate-400">
          <span>Bits restantes após Renascimento:</span>
          <span class="text-yellow-400 font-bold">${p.remainingBitsAfterRebirth.toLocaleString()}</span>
        </div>
      </div>
      ` : ""}
    </div>

    <div class="mb-4">
      <label class="card-sm rebirth-preserve-card flex items-start gap-3 mb-3 cursor-pointer ${rarityPreservationQuantity === 0 ? "opacity-60" : ""}">
        <input id="rebirth-preserve-rarity" type="checkbox" class="mt-1 h-4 w-4 accent-cyan-500" ${rebirthPreserveRarity ? "checked" : ""} ${rarityPreservationQuantity === 0 ? "disabled" : ""} onchange="rebirthTogglePreserveRarity(this.checked)">
        <span class="flex-1">
          <span class="block text-sm font-bold text-cyan-300">Preservar raridade</span>
          <span class="inline-flex items-center rounded-md border border-cyan-700 bg-cyan-950/60 px-2 py-1 mt-2 text-xs font-bold text-cyan-200">Raridade atual: ${currentRarityLabel}</span>
          <span class="block text-xs text-slate-400 mt-2">Usa 1 Cristal de Preservação e garante que o Digimon renascido mantenha esta raridade.</span>
          <span class="block text-xs ${rarityPreservationQuantity > 0 ? "text-slate-500" : "text-amber-400"} mt-1">Disponíveis: ${rarityPreservationQuantity}</span>
        </span>
      </label>
      ${equippedEquipmentCount > 0 ? `
        <div class="rebirth-block-alert">
          <p class="font-semibold">Renascimento bloqueado</p>
          <p class="text-xs text-red-200/80 mt-1">Este Digimon possui ${equippedEquipmentCount} equipamento(s) equipado(s). Desequipe todos antes de realizar o Renascimento para não sacrificá-los junto com o Digimon.</p>
        </div>
      ` : ""}
      ${pendingCompletedMissionCount > 0 ? `
        <div class="rebirth-block-alert">
          <p class="font-semibold">Renascimento bloqueado</p>
          <p class="text-xs text-red-200/80 mt-1">Este Digimon possui ${pendingCompletedMissionCount} missão${pendingCompletedMissionCount === 1 ? "" : "ões"} concluída${pendingCompletedMissionCount === 1 ? "" : "s"} com recompensa pendente. Resgate ${pendingCompletedMissionCount === 1 ? "a recompensa" : "as recompensas"} antes de realizar o Renascimento.</p>
        </div>
      ` : ""}
      ${equippedEquipmentCount > 0 ? `
        <button class="btn-primary w-full opacity-50 cursor-not-allowed py-3" disabled>
          Desequipe os equipamentos para continuar
        </button>
      ` : pendingCompletedMissionCount > 0 ? `
        <button class="btn-primary w-full opacity-50 cursor-not-allowed py-3" disabled>
          Resgate as missões antes de continuar
        </button>
      ` : p.eligible ? `
        <button class="btn-primary w-full text-lg py-3" id="rebirth-btn" onclick="rebirthExecute()">
          Renascer Digimon
        </button>
      ` : `
        <button class="btn-primary w-full opacity-50 cursor-not-allowed py-3" disabled>
          Requisitos não atendidos
        </button>
        <p class="text-xs text-red-400 text-center mt-2">${escapeHtml(p.reason)}</p>
      `}
    </div>
  `;

  content.innerHTML = html;
  rebirthUpdateCodeUI();
}

function rebirthCodeAttributeCard(attribute, label, color, baseMinimum, disabled, maxCodes) {
  return `
    <div class="card-sm">
      <div class="flex justify-between items-center mb-2">
        <span class="font-bold text-${color}-300">${label}</span>
        <span class="text-xs text-slate-400"><span id="code-alloc-${attribute}">0</span> códigos</span>
      </div>
      <div class="flex items-center justify-between gap-2">
        <span class="text-xs text-slate-500">IV mínimo: <span id="code-iv-${attribute}" class="font-bold text-${color}-300">${baseMinimum}</span></span>
        <div class="flex gap-1">
          <button class="btn-sm px-2" ${disabled} onclick="rebirthAdjustCode('${attribute}', -10)">−10</button>
          <button class="btn-sm px-2" ${disabled} onclick="rebirthAdjustCode('${attribute}', -1)">−1</button>
          <button class="btn-sm px-2" ${disabled} onclick="rebirthAdjustCode('${attribute}', 1)">+1</button>
          <button class="btn-sm px-2" ${disabled} onclick="rebirthAdjustCode('${attribute}', 10)">+10</button>
          <button class="btn-sm px-2" ${disabled} onclick="rebirthFocusCode('${attribute}')">Tudo</button>
        </div>
      </div>
      <div class="mt-2 h-1.5 rounded bg-slate-800 overflow-hidden">
        <div id="code-progress-${attribute}" class="h-full bg-${color}-400" style="width:0%"></div>
      </div>
    </div>
  `;
}

function rebirthAdjustCode(attribute, amount) {
  const available = Math.max(0, Number(rebirthPreview?.currentCodeInfinite ?? 0));
  if (available <= 0 || !Object.prototype.hasOwnProperty.call(rebirthCodeAllocation, attribute)) return;

  const attributeMaximum = 100;
  const currentTotal = rebirthCodeAllocation.hp + rebirthCodeAllocation.attack + rebirthCodeAllocation.defense;
  const currentValue = rebirthCodeAllocation[attribute];
  const remainingBalance = Math.max(0, available - currentTotal);
  const nextValue = amount > 0
    ? Math.min(attributeMaximum, currentValue + Math.min(amount, remainingBalance))
    : Math.max(0, currentValue + amount);
  rebirthCodeAllocation[attribute] = nextValue;
  rebirthUpdateCodeUI();
}

function rebirthFocusCode(attribute) {
  const available = Math.max(0, Number(rebirthPreview?.currentCodeInfinite ?? 0));
  if (available <= 0 || !Object.prototype.hasOwnProperty.call(rebirthCodeAllocation, attribute)) return;
  const otherAttributesTotal = rebirthCodeAllocation.hp + rebirthCodeAllocation.attack + rebirthCodeAllocation.defense - rebirthCodeAllocation[attribute];
  const maximum = Math.min(100, Math.max(0, available - otherAttributesTotal));
  rebirthCodeAllocation[attribute] = maximum;
  rebirthUpdateCodeUI();
}

function rebirthDistributeCodes(mode) {
  const available = Math.max(0, Number(rebirthPreview?.currentCodeInfinite ?? 0));
  if (available <= 0) return;

  const maximum = Math.min(available, 300);
  if (mode === "balanced") {
    const each = Math.min(100, Math.floor(maximum / 3));
    let remainder = maximum - (each * 3);
    const allocation = { hp: each, attack: each, defense: each };
    ["hp", "attack", "defense"].forEach(attribute => {
      if (remainder > 0 && allocation[attribute] < 100) {
        allocation[attribute] += 1;
        remainder -= 1;
      }
    });
    rebirthCodeAllocation = allocation;
  } else if (Object.prototype.hasOwnProperty.call(rebirthCodeAllocation, mode)) {
    rebirthCodeAllocation = { hp: 0, attack: 0, defense: 0 };
    rebirthCodeAllocation[mode] = maximum;
  }
  rebirthUpdateCodeUI();
}

function rebirthUpdateCodeUI() {
  const p = rebirthPreview;
  if (!p) return;
  const available = Math.max(0, Number(p.currentCodeInfinite ?? 0));
  const used = rebirthCodeAllocation.hp + rebirthCodeAllocation.attack + rebirthCodeAllocation.defense;
  const remaining = Math.max(0, available - used);
  const attributes = [
    ["hp", p.hpIvRange.min],
    ["attack", p.attackIvRange.min],
    ["defense", p.defenseIvRange.min]
  ];

  attributes.forEach(([attribute, baseMinimum]) => {
    const amount = rebirthCodeAllocation[attribute];
    const ivMinimum = Math.min(100, baseMinimum + Math.floor(amount / 10));
    const allocationElement = document.getElementById(`code-alloc-${attribute}`);
    const ivElement = document.getElementById(`code-iv-${attribute}`);
    const rangeElement = document.getElementById(`rebirth-${attribute}-min`);
    const progressElement = document.getElementById(`code-progress-${attribute}`);
    if (allocationElement) allocationElement.textContent = amount;
    if (ivElement) ivElement.textContent = ivMinimum;
    if (rangeElement) rangeElement.textContent = ivMinimum;
    if (progressElement) progressElement.style.width = `${Math.min(100, (amount / 100) * 100)}%`;
  });

  const summary = document.getElementById("code-allocation-summary");
  const remainingElement = document.getElementById("code-infinite-remaining");
  if (summary) summary.textContent = used > 0 ? `Serão usados ${used} Código${used === 1 ? "" : "s"} Infinito${used === 1 ? "" : "s"}` : "Nenhum Código Infinito será usado";
  if (remainingElement) remainingElement.textContent = remaining;

  const button = document.getElementById("rebirth-btn");
  if (button) button.textContent = "Renascer Digimon";
}

async function rebirthExecute() {
  const btn = document.getElementById("rebirth-btn");
  if (btn) { btn.disabled = true; btn.textContent = "Renascendo..."; }

  try {
    await apiPost("/digimon/rebirth", {
      digimonId: rebirthDigimonId,
      codeInfiniteHp: rebirthCodeAllocation.hp,
      codeInfiniteAttack: rebirthCodeAllocation.attack,
      codeInfiniteDefense: rebirthCodeAllocation.defense,
      preserveRarity: rebirthPreserveRarity
    });
    showToast("Renascimento realizado com sucesso! Seu Digimon renasceu.");
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
    if (btn) { btn.disabled = false; rebirthUpdateCodeUI(); }
  }
}
