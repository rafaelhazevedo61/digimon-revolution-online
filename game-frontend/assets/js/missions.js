const AREA_INFO = {
  NATIVE_FOREST: { name: "Floresta Nativa", emoji: "🌲", color: "from-green-900 to-green-950", border: "border-green-700" },
  GEAR_SAVANNA: { name: "Savana Gear", emoji: "⚙️", color: "from-amber-900 to-amber-950", border: "border-amber-700" },
  FACTORIAL_TOWN: { name: "Cidade Fatorial", emoji: "🏭", color: "from-slate-800 to-slate-900", border: "border-slate-600" },
  FREEZELAND: { name: "Terra Congelada", emoji: "❄️", color: "from-blue-900 to-blue-950", border: "border-blue-700" },
  SERVER_DESERT: { name: "Deserto Server", emoji: "🏜️", color: "from-orange-900 to-orange-950", border: "border-orange-700" },
  INFINITY_MOUNTAIN: { name: "Montanha Infinita", emoji: "🏔️", color: "from-purple-900 to-purple-950", border: "border-purple-700" }
};

const STAGE_ORDER = ["BABY", "BABY_II", "ROOKIE", "CHAMPION", "ULTIMATE", "MEGA"];
const AREA_ORDER = ["NATIVE_FOREST", "GEAR_SAVANNA", "FACTORIAL_TOWN", "FREEZELAND", "SERVER_DESERT", "INFINITY_MOUNTAIN"];

function areaProgressionRank(area) {
  return AREA_ORDER.indexOf(String(area || ""));
}

function stageProgressionRank(stage) {
  return STAGE_ORDER.indexOf(String(stage || ""));
}

function missionNumber(mission) {
  const id = String(mission && (mission.id || mission.missionId) || "");
  const match = id.match(/_(\d+)$/);
  return match ? Number(match[1]) : -1;
}

function missionIdentity(mission) {
  return String(mission && (mission.id || mission.missionId) || "");
}

function compareMissionsByDescendingProgression(a, b) {
  const requiredLevelA = Number(a.requiredLevel) || 0;
  const requiredLevelB = Number(b.requiredLevel) || 0;
  if (requiredLevelA !== requiredLevelB) return requiredLevelB - requiredLevelA;

  const missionNumberA = missionNumber(a);
  const missionNumberB = missionNumber(b);
  if (missionNumberA !== missionNumberB) return missionNumberB - missionNumberA;

  return missionIdentity(b).localeCompare(missionIdentity(a));
}

function compareMissionsByAscendingProgression(a, b) {
  const requiredLevelA = Number(a.requiredLevel) || 0;
  const requiredLevelB = Number(b.requiredLevel) || 0;
  if (requiredLevelA !== requiredLevelB) return requiredLevelA - requiredLevelB;

  const missionNumberA = missionNumber(a);
  const missionNumberB = missionNumber(b);
  if (missionNumberA !== missionNumberB) return missionNumberA - missionNumberB;

  return missionIdentity(a).localeCompare(missionIdentity(b));
}

async function renderMissionsPage() {
  const app = document.getElementById("app");
  showBottomNav("missions");

  app.innerHTML = `
    <div class="page-container missions-page-container">
      <header class="missions-page-header mb-4">
        <div>
          <p class="progression-eyebrow progression-eyebrow-cyan">Central de operações</p>
          <h2 class="progression-page-title">Mapa de missões</h2>
          <p class="progression-page-subtitle">Envie formações completas para explorar áreas e conquistar recompensas.</p>
        </div>
        <div class="flex items-center gap-2"><button type="button" class="btn-secondary text-xs" onclick="navigateTo('mission-teams')">Meus times</button><div class="missions-hero-emblem" aria-hidden="true">✦</div></div>
      </header>
      <div class="missions-page-layout">
        <div id="active-missions" class="missions-page-active-column"></div>
        <section class="missions-map-section">
          <div class="dashboard-section-heading">
            <div><p class="progression-eyebrow progression-eyebrow-cyan">Exploração</p><h3 class="progression-panel-title">Áreas disponíveis</h3></div>
            <span class="missions-map-key"><span class="missions-map-key-dot"></span> acessível</span>
          </div>
          <p class="dashboard-section-note">Avance pelas áreas para desbloquear missões mais desafiadoras e envie seus times completos.</p>
          <div id="areas-list">
            <div class="mission-area-skeleton"></div>
            <div class="mission-area-skeleton"></div>
          </div>
        </section>
      </div>
    </div>
  `;

  loadActiveMissions();

  try {
    const areas = await apiGet("/areas");
    renderAreaCards(areas);
  } catch (err) {
    document.getElementById("areas-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

async function loadActiveMissions() {
  const container = document.getElementById("active-missions");
  if (!container) return;

  let slotResponse;
  try {
    slotResponse = await apiGet("/missions/slots");
  } catch (err) {
    return;
  }

  const active = Array.isArray(slotResponse) ? slotResponse : (slotResponse?.activeMissions || []);
  const totalSlots = Math.max(1, Number(slotResponse?.totalSlots) || 3);
  const unlockedSlots = Math.max(1, Math.min(totalSlots, Number(slotResponse?.unlockedSlots) || 1));
  const missionBySlot = new Map(active.map(mission => [Number(mission.slotNumber), mission]));
  const slotCards = Array.from({ length: totalSlots }, (_, index) => {
    const slotNumber = index + 1;
    const mission = missionBySlot.get(slotNumber);
    if (slotNumber > unlockedSlots) {
      return `
        <article class="missions-slot-card missions-slot-card-locked" data-mp-slot="${slotNumber}">
          <div class="missions-slot-card-header">
            <div class="missions-slot-card-info"><span class="missions-slot-card-icon" aria-hidden="true">🔒</span><div class="missions-slot-card-copy"><p class="missions-active-label">Slot ${slotNumber}</p><p class="missions-slot-card-title">Slot bloqueado</p></div></div>
            <span class="missions-active-badge">Bloqueado</span>
          </div>
          <p class="missions-slot-card-description">Use um Expansor de Slot de Missão no inventário para liberar este espaço.</p>
        </article>
      `;
    }
    if (!mission) {
      return `
        <article class="missions-slot-card missions-slot-card-empty" data-mp-slot="${slotNumber}">
          <div class="missions-slot-card-header">
            <div class="missions-slot-card-info"><span class="missions-slot-card-icon" aria-hidden="true">✦</span><div class="missions-slot-card-copy"><p class="missions-active-label">Slot ${slotNumber}</p><p class="missions-slot-card-title">Slot livre</p></div></div>
            <span class="missions-active-badge">Disponível</span>
          </div>
          <p class="missions-slot-card-description">Escolha uma missão e envie um time para ocupar este slot.</p>
        </article>
      `;
    }
    return renderActiveMissionCard(mission);
  }).join("");

  container.innerHTML = `
    <section class="missions-active-section mb-4">
      <div class="dashboard-section-heading">
        <div><p class="progression-eyebrow progression-eyebrow-blue">Atividade em campo</p><h3 class="progression-panel-title">Slots de missão</h3></div>
        <span class="dashboard-section-count dashboard-section-count-blue">${active.length}/${unlockedSlots}</span>
      </div>
      <p class="dashboard-section-note">Você possui ${unlockedSlots} de ${totalSlots} slots desbloqueados. Acompanhe os retornos e resgate cada recompensa assim que estiver disponível.</p>
      <div class="missions-active-list grid grid-cols-1 gap-3">${slotCards}</div>
    </section>
  `;

  startMissionsPageTimers();
}

function formatMissionName(mission) {
  const id = mission.id || mission.missionId;
  const names = {
    MISSION_ADMIN: "Missão de Teste",
    MISSION_1: "Patrulha na Floresta Nativa",
    MISSION_2: "Caçada na Savana Gear",
    MISSION_3: "Investigação na Cidade Fatorial",
    MISSION_4: "Expedição na Terra Congelada",
    MISSION_5: "Travessia do Deserto Server",
    MISSION_6: "Ascensão à Montanha Infinita"
  };
  if (names[id]) return names[id];

  const friendlyName = mission.name || mission.missionName;
  if (!friendlyName) return "Missão";

  return String(friendlyName)
    .replace(/Gear Savanna/g, "Savana Gear")
    .replace(/Factorial Town/g, "Cidade Fatorial")
    .replace(/Freezeland/g, "Terra Congelada")
    .replace(/Server Desert/g, "Deserto Server")
    .replace(/Infinity Mountain/g, "Montanha Infinita");
}

function renderActiveMissionCard(m) {
  const now = Date.now();
  const endsAt = new Date(m.endsAt).getTime();
  const remaining = Math.max(0, Math.floor((endsAt - now) / 1000));
  const done = m.status === "COMPLETED" || remaining <= 0;
  const autoRepeatControl = m.teamId
    ? `<button class="btn-sm ${m.autoRepeatEnabled ? "btn-primary" : "btn-secondary"}" onclick="toggleMissionAutoRepeat('${m.missionInstanceId}', ${!m.autoRepeatEnabled})">${m.autoRepeatEnabled ? "Auto: ativa" : "Ativar auto"}</button>`
    : "";

  return `
    <article class="missions-active-card ${done ? "missions-active-card-ready" : ""}" data-mp-instance="${m.missionInstanceId}" data-mp-ends-at="${m.endsAt}">
      <div class="missions-active-icon" aria-hidden="true">✦</div>
      <div class="missions-active-main">
        <p class="missions-active-label">Objetivo em campo${m.slotNumber ? ` · Slot ${m.slotNumber}` : ""}</p>
        <p class="missions-active-name">${escapeHtml(formatMissionName(m))}</p>
        <p class="missions-active-team">${escapeHtml(m.teamName || (m.teamId ? "Time de missão" : "Missão legada"))}${m.teamId ? " · 3 Digimons" : ""}</p>
        <div class="missions-active-state"><span class="missions-active-dot ${done ? "missions-active-dot-ready" : ""}"></span><span class="mp-timer">${done ? "Concluída!" : `Retorno em ${formatTime(remaining)}`}</span></div>
      </div>
      <div class="missions-active-action">
        ${autoRepeatControl}
        ${done ? `<button class="btn-sm btn-primary" onclick="claimMissionFromList('${m.missionInstanceId}')">Resgatar</button>` : `<span class="missions-active-badge">Em andamento</span>`}
      </div>
    </article>
  `;
}

async function toggleMissionAutoRepeat(instanceId, enabled) {
  try {
    await apiPatch(`/missions/${instanceId}/auto-repeat`, { enabled });
    showToast(enabled ? "Auto-missão ativada para este slot." : "Auto-missão pausada.", "success");
    await loadActiveMissions();
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function startMissionAutoRepeat(result) {
  if (!result || !result.autoRepeatEnabled || !result.missionId || !result.teamId) return false;
  try {
    await apiPost("/missions/start", {
      missionId: result.missionId,
      teamId: result.teamId,
      autoRepeat: true
    });
    const repeatButton = document.getElementById("mission-repeat-button");
    if (repeatButton) {
      repeatButton.disabled = true;
      repeatButton.textContent = "Auto-missão ativa";
      repeatButton.classList.add("opacity-70", "cursor-not-allowed");
    }
    showToast("Auto-missão: o mesmo time foi reenviado.", "success");
    return true;
  } catch (err) {
    showToast(`Auto-missão pausada: ${err.message}`, "error");
    return false;
  }
}

async function claimMissionFromList(instanceId) {
  try {
    const result = await apiPost(`/missions/${instanceId}/claim`);
    showMissionClaimModal(result);
    await startMissionAutoRepeat(result);
    await loadActiveMissions();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function missionRewardLabel(reward) {
  if (reward && reward.itemName) return reward.itemName;
  if (reward && String(reward.itemCode || "").startsWith("CHEST_")) return "Baú";
  if (reward && reward.itemCode) return reward.itemCode;

  const labels = {
    POTION_SMALL: "Poção Pequena",
    TRAINING_STONE: "Pedra de Treino",
    DATA_CORE: "Núcleo de Dados",
    DIGITAMA_STARTER: "Digitama Inicial",
    DIGITAMA_FIRE: "Digitama de Fogo",
    DIGITAMA_WATER: "Digitama de Água",
    DIGITAMA_NATURE: "Digitama de Planta",
    DIGITAMA_EARTH: "Digitama de Terra",
    DIGITAMA_WIND: "Digitama de Vento",
    DIGITAMA_LIGHT: "Digitama de Luz",
    DIGITAMA_DARK: "Digitama de Trevas",
    DIGITAMA_THUNDER: "Digitama de Trovão",
    DIGITAMA_NEUTRAL: "Digitama Neutro",
    DIGITAMA_ICE: "Digitama de Gelo",
    DIGITAMA_STEEL: "Digitama de Metal",
    INCUBATOR_COMMON: "Incubadora Comum",
    INCUBATOR_RARE: "Incubadora Rara",
    INCUBATOR_EPIC: "Incubadora Épica",
    INCUBATOR_LEGENDARY: "Incubadora Lendária",
    LOOT_CHEST: "Baú"
  };
  return labels[reward && reward.item] || (reward && reward.item) || "Recompensa";
}

function missionRewardChestCode(reward) {
  const code = String(reward && reward.itemCode || "").trim();
  if (reward && reward.item === "LOOT_CHEST" && code) return code;
  return code.startsWith("CHEST_") ? code : null;
}

function missionRewardIcon(reward) {
  if (reward && (reward.item === "LOOT_CHEST" || missionRewardChestCode(reward))) {
    return renderChestIcon("w-10 h-10");
  }
  if (reward && String(reward.item || "").startsWith("DIGITAMA")) return "🥚";
  if (reward && String(reward.item || "").startsWith("INCUBATOR")) return "📦";
  if (reward && String(reward.item || "").includes("FRAGMENT")) return "🧩";
  return "✨";
}

function missionFriendlyCode(code) {
  return String(code || "")
    .toLowerCase()
    .split("_")
    .filter(Boolean)
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function missionLootItemLabel(item) {
  const itemType = String(item && item.itemType || "");
  const itemCode = String(item && (item.itemCode || item.materialCode) || itemType);
  if (item && item.itemName && ![itemType, itemCode].includes(String(item.itemName))) return item.itemName;
  if (typeof invItemName === "function" && itemType) {
    const inventoryName = invItemName(itemType);
    if (inventoryName && inventoryName !== itemType) return inventoryName;
  }

  if (itemCode.startsWith("FRAGMENT_")) {
    return `Fragmento ${missionFriendlyCode(itemCode.slice("FRAGMENT_".length))}`;
  }
  if (itemCode === "EVOLUTION_MATERIAL") return "Material de Evolução";
  if (itemCode === "LOOT_CHEST") return "Baú";
  return missionFriendlyCode(itemCode) || "Recompensa";
}

function missionLootItemIcon(item) {
  const itemType = String(item && item.itemType || item && item.itemCode || "");
  if (typeof invItemEmoji === "function") return invItemEmoji(itemType);

  if (itemType.startsWith("DIGITAMA")) return "🥚";
  if (itemType.startsWith("INCUBATOR")) return "📦";
  if (itemType.startsWith("FRAGMENT") || itemType === "EVOLUTION_MATERIAL") return "🧩";
  if (itemType === "LOOT_CHEST") return renderChestIcon("w-10 h-10");
  return "✨";
}

function missionLootRarityLabel(rarity) {
  return typeof formatRarity === "function" ? formatRarity(rarity) : missionFriendlyCode(rarity);
}

function missionLootRarityClass(rarity) {
  const normalized = String(rarity || "common").toLowerCase();
  return `badge-${normalized}`;
}

function missionLootQuantityLabel(item) {
  const min = Number(item && (item.minQuantity ?? item.quantity)) || 0;
  const max = Number(item && (item.maxQuantity ?? item.quantity)) || min;
  return min === max ? `x${min}` : `x${min}–${max}`;
}

function missionLootItemMarkup(item, includeWeight = false) {
  const weightMarkup = includeWeight && Number(item && item.weight) > 0
    ? `<span class="text-xs text-slate-500">Peso ${Number(item.weight)}</span>`
    : "";
  return `
    <div class="flex items-center gap-3 rounded-lg border border-slate-700 bg-slate-900/60 px-3 py-3">
      <span class="text-2xl" aria-hidden="true">${missionLootItemIcon(item)}</span>
      <div class="min-w-0 flex-1">
        <p class="font-semibold text-slate-100 truncate">${escapeHtml(missionLootItemLabel(item))}</p>
        <div class="flex items-center gap-2 mt-1 flex-wrap">
          <span class="badge ${missionLootRarityClass(item && item.rarity)}">${escapeHtml(missionLootRarityLabel(item && item.rarity))}</span>
          ${weightMarkup}
        </div>
      </div>
      <span class="font-bold text-cyan-300 whitespace-nowrap">${missionLootQuantityLabel(item)}</span>
    </div>
  `;
}

function missionRaritySort(a, b) {
  const order = ["COMMON", "RARE", "EPIC", "LEGENDARY"];
  return order.indexOf(String(a && a.rarity || "").toUpperCase()) - order.indexOf(String(b && b.rarity || "").toUpperCase());
}

function missionRenderLootPreview(preview) {
  const fixedRewards = Array.isArray(preview && preview.fixedRewards) ? preview.fixedRewards : [];
  const chest = preview && preview.chest;
  const legacyChances = Array.isArray(preview && preview.lootChances) ? preview.lootChances : [];
  const legacyItems = Array.isArray(preview && preview.lootItems) ? [...preview.lootItems].sort(missionRaritySort) : [];
  const chestItems = chest && Array.isArray(chest.items) ? [...chest.items].sort(missionRaritySort) : [];
  const hasLegacyLoot = !chest && (legacyChances.length > 0 || legacyItems.length > 0);

  const fixedMarkup = fixedRewards.length > 0
    ? `
      <section class="mb-5">
        <h4 class="text-sm font-bold text-slate-300 mb-2">Itens garantidos</h4>
        <div class="space-y-2">
          ${fixedRewards.map(item => missionLootItemMarkup({
            ...item,
            quantity: item.quantity,
            rarity: "COMMON"
          })).join("")}
        </div>
      </section>
    `
    : "";

  const chestMarkup = chest
    ? `
      <section class="mb-5">
        <div class="rounded-lg border border-cyan-700 bg-cyan-950/30 px-3 py-3 mb-3">
          <div class="flex items-start gap-3">
            ${renderChestIcon("w-14 h-14")}
            <div class="min-w-0">
              <h4 class="font-bold text-cyan-200">${escapeHtml(chest.name || "Baú da missão")}</h4>
              ${chest.description ? `<p class="text-xs text-cyan-100/70 mt-1">${escapeHtml(chest.description)}</p>` : ""}
              <p class="text-xs text-cyan-300 mt-2">Ao concluir, você recebe 1 baú. Cada abertura entrega entre ${Number(chest.minItems) || 0} e ${Number(chest.maxItems) || 0} itens.</p>
            </div>
          </div>
        </div>
        <h4 class="text-sm font-bold text-slate-300 mb-2">Possíveis itens do baú</h4>
        ${chestItems.length > 0
          ? `<div class="space-y-2">${chestItems.map(item => missionLootItemMarkup(item, true)).join("")}</div>`
          : `<p class="text-sm text-slate-400">Nenhum item ativo foi configurado neste baú.</p>`}
        ${Array.isArray(chest.rarityWeights) && chest.rarityWeights.length > 0 ? `
          <div class="mt-3 rounded-lg border border-slate-700 bg-slate-900/50 px-3 py-3">
            <p class="text-xs font-bold text-slate-300 mb-2">Pesos das raridades</p>
            <div class="flex gap-2 flex-wrap">
              ${chest.rarityWeights.map(weight => `<span class="badge ${missionLootRarityClass(weight.rarity)}">${escapeHtml(missionLootRarityLabel(weight.rarity))}: ${Number(weight.weight) || 0}</span>`).join("")}
            </div>
            <p class="text-xs text-slate-500 mt-2">Os valores são pesos relativos do sorteio, não percentuais fixos por abertura.</p>
          </div>
        ` : ""}
      </section>
    `
    : "";

  const legacyMarkup = hasLegacyLoot
    ? `
      <section class="mb-5">
        <h4 class="text-sm font-bold text-slate-300 mb-2">Loot aleatório da missão</h4>
        ${legacyChances.length > 0 ? `
          <p class="text-xs text-slate-400 mb-2">Chance por raridade</p>
          <div class="flex gap-2 flex-wrap mb-3">
            ${legacyChances.map(chance => `<span class="badge ${missionLootRarityClass(chance.rarity)}">${escapeHtml(missionLootRarityLabel(chance.rarity))}: ${Number(chance.chance) || 0}%</span>`).join("")}
          </div>
        ` : ""}
        ${legacyItems.length > 0
          ? `<div class="space-y-2">${legacyItems.map(item => missionLootItemMarkup(item)).join("")}</div>`
          : `<p class="text-sm text-slate-400">Nenhum item aleatório foi configurado.</p>`}
      </section>
    `
    : "";

  const hasAnyReward = fixedRewards.length > 0 || chest || hasLegacyLoot;
  const overlay = document.createElement("div");
  overlay.id = "mission-loot-modal";
  overlay.className = "fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/75";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.setAttribute("aria-labelledby", "mission-loot-title");
  overlay.innerHTML = `
    <div class="card w-full max-w-lg max-h-[88vh] overflow-y-auto" onclick="event.stopPropagation()">
      <div class="flex items-start justify-between gap-4 mb-5">
        <div>
          <p class="text-xs uppercase tracking-wider text-cyan-400 font-bold">Recompensas possíveis</p>
          <h3 id="mission-loot-title" class="text-xl font-bold mt-1">${escapeHtml(preview && preview.missionName || "Missão")}</h3>
        </div>
        <button type="button" class="text-slate-400 hover:text-white text-2xl leading-none" aria-label="Fechar" onclick="document.getElementById('mission-loot-modal')?.remove()">&times;</button>
      </div>

      <div class="grid grid-cols-2 gap-3 mb-5">
        <div class="rounded-lg border border-purple-800 bg-purple-950/40 px-3 py-3 text-center">
          <p class="text-xs text-purple-300">XP base</p>
          <p class="text-xl font-bold text-purple-200 mt-1">+${Number(preview && preview.xpReward) || 0}</p>
        </div>
        <div class="rounded-lg border border-yellow-800 bg-yellow-950/40 px-3 py-3 text-center">
          <p class="text-xs text-yellow-300">Bits base</p>
          <p class="text-xl font-bold text-yellow-200 mt-1">+${Number(preview && preview.bitsReward) || 0}</p>
        </div>
      </div>

      ${fixedMarkup}
      ${chestMarkup}
      ${legacyMarkup}
      ${!hasAnyReward ? `<p class="text-sm text-slate-400">Nenhuma recompensa de item foi configurada para esta missão.</p>` : ""}

      <button type="button" class="btn-secondary w-full mt-2" onclick="document.getElementById('mission-loot-modal')?.remove()">Fechar</button>
    </div>
  `;
  overlay.addEventListener("click", event => {
    if (event.target === overlay) overlay.remove();
  });
  document.body.appendChild(overlay);
}

async function missionShowLootPreview(missionId, button) {
  if (!missionId || (button && button.disabled)) return;

  const existing = document.getElementById("mission-loot-modal");
  if (existing) existing.remove();

  const originalText = button ? button.textContent : "Ver recompensas";
  if (button) {
    button.disabled = true;
    button.textContent = "Carregando...";
  }

  try {
    const preview = await apiGet(`/missions/${encodeURIComponent(missionId)}/loot`);
    missionRenderLootPreview(preview);
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    if (button) {
      button.disabled = false;
      button.textContent = originalText || "Ver recompensas";
    }
  }
}

async function missionOpenRewardChest(chestCode, quantity = 1, button) {
  if (!chestCode || typeof invOpenChest !== "function") {
    showToast("Abertura de baú indisponível.", "error");
    return;
  }
  if (button && button.disabled) return;

  const requestedQuantity = Number.parseInt(quantity, 10) || 1;
  if (button) {
    button.disabled = true;
    button.textContent = "Abrindo...";
  }

  const result = await invOpenChest(chestCode, requestedQuantity);
  if (result && button) {
    button.textContent = requestedQuantity > 1 ? "Baús abertos" : "Baú aberto";
  } else if (button) {
    button.disabled = false;
    button.textContent = requestedQuantity > 1 ? `Abrir ${requestedQuantity} baús` : "Abrir baú";
  }
}

async function repeatMissionFromReward(missionId, teamId = null) {
  const button = document.getElementById("mission-repeat-button");
  if (!missionId || !button) return;
  button.disabled = true;
  button.textContent = "Enviando...";
  try {
    if (teamId) {
      await apiPost("/missions/start", { missionId, teamId });
      document.getElementById("mission-claim-modal")?.remove();
      missionTeamContextPromise = null;
      showToast("Time reenviado para a missão!");
      navigateTo("missions");
      return;
    }

    if (!window._missionDefinitions || !window._missionDefinitions[missionId]) {
      const missions = await apiGet("/missions");
      window._missionDefinitions = Object.fromEntries((missions || []).map(mission => [missionIdentity(mission), mission]));
    }
    document.getElementById("mission-claim-modal")?.remove();
    openMissionTeamSelectionModal(missionId);
  } catch (err) {
    showToast(err.message, "error");
    button.disabled = false;
    button.textContent = "Repetir missão";
  }
}

function missionMultiplierLabel(value) {
  const number = Number(value);
  if (!Number.isFinite(number)) return "1x";
  return `${number.toFixed(2).replace(/\.?0+$/, "")}x`;
}

function missionRewardBreakdownMarkup(title, breakdown, unit, digimonLabel) {
  if (!breakdown) return "";
  const base = Number(breakdown.baseAmount) || 0;
  const progress = Number(breakdown.missionProgressMultiplier) || 1;
  const clan = Number(breakdown.clanMultiplier) || 1;
  const event = Number(breakdown.eventMultiplier) || 1;
  const digimon = Number(breakdown.digimonMultiplier) || 1;
  const combined = Number(breakdown.combinedMultiplier) || 1;
  const effective = Number(breakdown.effectiveMultiplier) || 0;
  const beforeDigimon = Number(breakdown.amountBeforeDigimonMultiplier) || 0;
  const total = Number(breakdown.finalAmount) || 0;
  const row = "flex items-center justify-between gap-3 rounded-md border border-slate-700/80 bg-slate-900/40 px-2.5 py-2";
  const label = "text-xs text-slate-400";
  const value = "text-xs font-semibold text-slate-200 whitespace-nowrap";
  return `
    <details class="mt-3 rounded-lg border border-slate-700 bg-slate-900/40 px-2.5 py-2">
      <summary class="cursor-pointer select-none text-xs font-semibold text-slate-300">Detalhes de ${title}</summary>
      <div class="mt-3 space-y-3">
        <section>
          <h5 class="mb-1.5 border-b border-slate-700/80 pb-1 text-[10px] font-bold uppercase tracking-wider text-slate-500">Composição</h5>
          <div class="grid grid-cols-1 gap-1.5">
            <div class="${row}"><span class="${label}">Valor-base</span><strong class="${value}">${base} ${unit}</strong></div>
            <div class="${row}"><span class="${label}">Progressão da missão</span><strong class="${value}">${missionMultiplierLabel(progress)}</strong></div>
            <div class="${row}"><span class="${label}">Bônus de clã</span><strong class="${value}">${missionMultiplierLabel(clan)}</strong></div>
            <div class="${row}"><span class="${label}">Evento de recompensa</span><strong class="${value}">${missionMultiplierLabel(event)}</strong></div>
            <div class="${row}"><span class="${label}">${escapeHtml(digimonLabel)}</span><strong class="${value}">${missionMultiplierLabel(digimon)}</strong></div>
          </div>
        </section>

        <section class="border-t border-slate-700/80 pt-3">
          <h5 class="mb-1.5 border-b border-slate-700/80 pb-1 text-[10px] font-bold uppercase tracking-wider text-slate-500">Resultado</h5>
          <div class="grid grid-cols-1 gap-1.5">
            <div class="${row}"><span class="${label}">Após missão, clã e evento</span><strong class="${value}">${beforeDigimon} ${unit}</strong></div>
            <div class="${row}"><span class="${label}">Multiplicador consolidado</span><strong class="${value} text-cyan-200">${missionMultiplierLabel(combined)}</strong></div>
            <div class="${row}"><span class="${label}">Multiplicador efetivo</span><strong class="${value} text-cyan-200">${missionMultiplierLabel(effective)}</strong></div>
          </div>
        </section>

        <section class="border-t border-slate-700/80 pt-3">
          <h5 class="mb-1.5 border-b border-slate-700/80 pb-1 text-[10px] font-bold uppercase tracking-wider text-slate-500">Conferência</h5>
          <div class="rounded-md border border-purple-700/80 bg-purple-950/40 px-2.5 py-2 text-center">
            <p class="text-xs font-semibold text-purple-100">${base} × ${missionMultiplierLabel(effective)} ≈ ${total} ${unit}</p>
            <p class="mt-0.5 text-[10px] text-purple-200/70">Valor-base × multiplicador efetivo = total aplicado</p>
          </div>
        </section>
      </div>
    </details>
  `;
}

function missionDigimonExperienceMarkup(result) {
  const members = Array.isArray(result && result.digimonExperience) ? result.digimonExperience : [];
  if (members.length === 0) return "";

  const cards = members.map(member => {
    const percent = Math.max(0, Math.min(100, Number(member.experiencePercent) || 0));
    const percentLabel = `${percent.toFixed(1).replace(/\.0$/, "")}%`;
    const levelsGained = Math.max(0, Number(member.levelsGained) || 0);
    const levelUpMarkup = levelsGained > 0
      ? `<span class="whitespace-nowrap rounded-full border border-emerald-600/70 bg-emerald-950/50 px-2 py-1 text-[0.62rem] font-bold uppercase tracking-wide text-emerald-300">+${levelsGained} ${levelsGained === 1 ? "Nível" : "Níveis"}</span>`
      : "";
    const isMaxLevel = Number(member.experienceToNextLevel) <= 0;
    const xpLabel = isMaxLevel
      ? "Nível máximo"
      : `${Number(member.experience) || 0} / ${Number(member.experienceToNextLevel) || 0} XP`;
    return `
      <article class="rounded-xl border border-purple-800/70 bg-purple-950/20 p-3">
        <div class="flex items-center gap-3">
          <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-xl border border-purple-700/70 bg-slate-950/60">
            ${renderDigimonVisual(member.imageUrl, member.stage, "h-12 w-12", "text-3xl")}
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-center justify-between gap-2">
              <p class="truncate font-bold text-slate-100">${escapeHtml(member.name || "Digimon")}</p>
              <div class="flex items-center gap-2">
                ${levelUpMarkup}
                <span class="whitespace-nowrap text-xs font-bold text-purple-200">Nv. ${Number(member.level) || 0}</span>
              </div>
            </div>
            <div class="mt-2 flex items-center justify-between gap-2 text-[0.68rem]">
              <span class="text-slate-400">${xpLabel}</span>
              <strong class="text-purple-200">${percentLabel}</strong>
            </div>
            <div class="mt-1.5 h-2 overflow-hidden rounded-full bg-slate-800" role="progressbar" aria-label="XP de ${escapeAttr(member.name || "Digimon")}" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100">
              <div class="h-full rounded-full bg-gradient-to-r from-purple-500 to-cyan-400 transition-all" style="width:${percent}%"></div>
            </div>
          </div>
        </div>
      </article>
    `;
  }).join("");

  return `
    <section class="mb-5 rounded-xl border border-purple-800/70 bg-purple-950/10 p-3">
      <div class="mb-3 flex items-start justify-between gap-3">
        <div>
          <p class="text-xs font-bold uppercase tracking-wider text-purple-300">Formação recompensada</p>
          <h4 class="mt-1 text-sm font-bold text-slate-200">XP de cada Digimon</h4>
        </div>
        <span class="text-xs text-slate-500">Após o resgate</span>
      </div>
      <div class="space-y-2">${cards}</div>
    </section>
  `;
}

function showMissionClaimModal(result) {
  const existing = document.getElementById("mission-claim-modal");
  if (existing) existing.remove();

  const rewards = Array.isArray(result && result.rewards) ? result.rewards : [];
  const missionName = formatMissionName({ id: result && result.missionId });
  const hasFriendlyMissionName = missionName !== "Missão";
  const rewardMarkup = rewards.length > 0
      ? rewards.map(reward => {
        const chestCode = missionRewardChestCode(reward);
        const chestQuantity = Math.max(1, Number(reward.quantity) || 1);
        const isChest = reward.item === "LOOT_CHEST" || !!chestCode;
        return `
          <div class="flex items-center gap-3 rounded-lg border ${isChest ? "border-cyan-700 bg-cyan-950/40" : "border-slate-700 bg-slate-900/60"} px-3 py-3">
            <span class="text-2xl" aria-hidden="true">${missionRewardIcon(reward)}</span>
            <div class="min-w-0 flex-1">
              <p class="font-semibold ${isChest ? "text-cyan-200" : "text-slate-100"}">${escapeHtml(missionRewardLabel(reward))}</p>
              ${isChest ? "<p class=\"text-xs text-cyan-400 mt-1\">Abra aqui ou pelo Inventário para revelar o loot</p>" : ""}
            </div>
            <div class="flex flex-col items-end gap-2">
              <span class="font-bold text-cyan-300">x${Number(reward.quantity) || 0}</span>
              ${chestCode ? `<button type="button" class="btn-sm btn-secondary whitespace-nowrap" onclick="missionOpenRewardChest('${escapeAttr(chestCode)}', ${chestQuantity}, this)">${chestQuantity > 1 ? `Abrir ${chestQuantity} baús` : "Abrir baú"}</button>` : ""}
            </div>
          </div>
        `;
      }).join("")
    : `<p class="text-sm text-slate-400">Nenhuma recompensa de item foi registrada.</p>`;

  const overlay = document.createElement("div");
  overlay.id = "mission-claim-modal";
  overlay.className = "fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/75";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.setAttribute("aria-labelledby", "mission-claim-title");
  overlay.innerHTML = `
    <div class="card w-full max-w-lg max-h-[88vh] overflow-y-auto" onclick="event.stopPropagation()">
      <div class="flex items-start justify-between gap-4 mb-5">
        <div>
          <p class="text-xs uppercase tracking-wider text-cyan-400 font-bold">Recompensa recebida</p>
          <h3 id="mission-claim-title" class="text-xl font-bold mt-1">Missão concluída!</h3>
          ${hasFriendlyMissionName ? `<p class="text-sm text-slate-400 mt-1">${escapeHtml(missionName)}</p>` : ""}
        </div>
        <button class="text-slate-400 hover:text-white text-2xl leading-none" aria-label="Fechar" onclick="document.getElementById('mission-claim-modal')?.remove()">&times;</button>
      </div>

      <div class="grid grid-cols-2 gap-3 mb-5">
        <div class="rounded-lg border border-purple-800 bg-purple-950/40 px-3 py-3 text-center">
          <p class="text-xs text-purple-300">Experiência</p>
          <p class="text-xl font-bold text-purple-200 mt-1">+${Number(result && result.xpGained) || 0} XP</p>
        </div>
        <div class="rounded-lg border border-yellow-800 bg-yellow-950/40 px-3 py-3 text-center">
          <p class="text-xs text-yellow-300">Bits</p>
          <p class="text-xl font-bold text-yellow-200 mt-1">+${Number(result && result.bitsGained) || 0}</p>
        </div>
      </div>

      ${missionDigimonExperienceMarkup(result)}

      ${result && result.missionId ? `
        <div class="flex flex-col sm:flex-row gap-2 mt-5 mb-5">
          <button id="mission-repeat-button" class="btn-primary flex-1" onclick="repeatMissionFromReward('${escapeAttr(result.missionId)}', '${escapeAttr(result.teamId || "")}')">Repetir missão</button>
          <button id="mission-claim-continue" class="btn-secondary flex-1">Continuar</button>
        </div>
      ` : `
        <button id="mission-claim-continue" class="btn-primary w-full mt-5 mb-5">Continuar</button>
      `}

      <div class="mb-4">
        <h4 class="text-sm font-bold text-slate-300 mb-2">Itens recebidos</h4>
        <div class="space-y-2">${rewardMarkup}</div>
      </div>

      ${missionRewardBreakdownMarkup("Experiência", result && result.experienceBreakdown, "XP", "Multiplicadores do Digimon")}
      ${missionRewardBreakdownMarkup("Bits", result && result.bitsBreakdown, "bits", "Multiplicador do Digimon")}

    </div>
  `;
  overlay.addEventListener("click", event => {
    if (event.target === overlay) overlay.remove();
  });
  document.body.appendChild(overlay);
  const continueButton = overlay.querySelector("#mission-claim-continue");
  if (continueButton) {
    continueButton.addEventListener("click", () => {
      overlay.remove();
      if (typeof showNewlyUnlockedContent === "function") {
        showNewlyUnlockedContent(result.newlyUnlockedContent);
      }
    });
  }
}

let missionsPageTimerInterval = null;

function startMissionsPageTimers() {
  if (missionsPageTimerInterval) clearInterval(missionsPageTimerInterval);
  missionsPageTimerInterval = setInterval(() => {
    const cards = document.querySelectorAll("[data-mp-instance]");
    if (cards.length === 0) { clearInterval(missionsPageTimerInterval); return; }

    cards.forEach(el => {
      const endsAt = new Date(el.dataset.mpEndsAt).getTime();
      const remaining = Math.max(0, Math.floor((endsAt - Date.now()) / 1000));
      const timerEl = el.querySelector(".mp-timer");
      if (!timerEl) return;

      if (remaining <= 0) {
        timerEl.textContent = "Concluída!";
        el.classList.add("missions-active-card-ready");
        const dot = el.querySelector(".missions-active-dot");
        if (dot) dot.classList.add("missions-active-dot-ready");
        const badge = el.querySelector(".missions-active-badge");
        if (badge) {
          badge.outerHTML = `<button class="btn-sm btn-primary" onclick="claimMissionFromList('${el.dataset.mpInstance}')">Resgatar</button>`;
        }
      } else {
        timerEl.textContent = `Retorno em ${formatTime(remaining)}`;
      }
    });
  }, 1000);
}

function toggleMissionAreaGroup(groupId, button) {
  const content = document.getElementById(groupId);
  if (!content || !button) return;
  const expanded = button.getAttribute("aria-expanded") === "true";
  content.hidden = expanded;
  button.setAttribute("aria-expanded", String(!expanded));
  const icon = button.querySelector(".mission-area-group-toggle-icon");
  if (icon) icon.textContent = expanded ? "+" : "−";
}

function renderMissionAreaGroup({ id, title, description, areas, expanded }) {
  if (areas.length === 0) return "";
  return `
    <section class="mission-area-group">
      <button type="button" class="mission-area-group-header" aria-expanded="${expanded}" aria-controls="${id}" onclick="toggleMissionAreaGroup('${id}', this)">
        <span class="mission-area-group-heading"><span class="mission-area-group-dot"></span><span><span class="mission-area-group-title">${title}</span><span class="mission-area-group-description">${description}</span></span></span>
        <span class="mission-area-group-toggle-icon" aria-hidden="true">${expanded ? "−" : "+"}</span>
      </button>
      <div id="${id}" class="mission-area-group-content" ${expanded ? "" : "hidden"}>${areas.map(renderMissionAreaCard).join("")}</div>
    </section>
  `;
}

function renderMissionAreaCard(a, index) {
  const info = AREA_INFO[a.area] || { name: a.area, emoji: "📍" };
  const locked = !a.unlocked;
  const progressionNumber = areaProgressionRank(a.area) >= 0 ? areaProgressionRank(a.area) + 1 : index + 1;
  const areaNumber = String(progressionNumber).padStart(2, "0");

  return `
    <article class="mission-area-card ${locked ? "mission-area-card-locked" : "mission-area-card-open"}" ${locked ? "" : `onclick="navigateTo('mission-area', { area: '${a.area}' })" role="button" tabindex="0" onkeydown="if(event.key === 'Enter' || event.key === ' ') { event.preventDefault(); navigateTo('mission-area', { area: '${a.area}' }); }"`}>
      <div class="mission-area-icon" aria-hidden="true">${info.emoji}</div>
      <div class="mission-area-main">
        <p class="mission-area-label">Área ${areaNumber}</p>
        <h3 class="mission-area-name">${info.name}</h3>
        <p class="mission-area-requirement">Estágio mínimo · ${formatStage(a.requiredStage)}</p>
      </div>
      <div class="mission-area-status">${locked ? `<span class="mission-area-lock" aria-hidden="true">🔒</span><span>Bloqueada</span>` : `<span class="mission-area-access-dot"></span><span>Acessível</span>`}<span class="mission-area-arrow" aria-hidden="true">›</span></div>
    </article>
  `;
}

function renderAreaCards(areas) {
  const container = document.getElementById("areas-list");
  const sorted = [...areas].sort((a, b) => {
    const areaDifference = areaProgressionRank(b.area) - areaProgressionRank(a.area);
    if (areaDifference !== 0) return areaDifference;
    return stageProgressionRank(b.requiredStage) - stageProgressionRank(a.requiredStage);
  });
  const available = sorted.filter(area => area.unlocked);
  const locked = sorted.filter(area => !area.unlocked);

  container.innerHTML = [
    renderMissionAreaGroup({
      id: "mission-areas-available",
      title: "Áreas disponíveis",
      description: "Acessíveis pelas formações do jogador",
      areas: available,
      expanded: true
    }),
    renderMissionAreaGroup({
      id: "mission-areas-locked",
      title: "Áreas bloqueadas",
      description: "Desbloqueie com um estágio mais avançado",
      areas: locked,
      expanded: available.length === 0
    })
  ].join("");
}


// Mission list for a specific area
async function renderMissionAreaPage(params) {
  const area = (params && params.area) || (window._routeParams && window._routeParams.area);
  if (!area) return navigateTo("missions");

  const app = document.getElementById("app");
  const info = AREA_INFO[area] || { name: area, emoji: "📍" };
  showBottomNav("missions");

  app.innerHTML = `
    <div class="page-container mission-area-page-container">
      <header class="mission-area-page-header mb-4">
        <button class="progression-back-button mb-3" onclick="navigateTo('missions')"><span aria-hidden="true">←</span> Voltar às áreas</button>
        <div class="mission-area-title-row">
          <div class="mission-area-title-lockup">
            <div class="mission-area-header-emoji" aria-hidden="true">${info.emoji}</div>
            <div>
              <p class="progression-eyebrow progression-eyebrow-cyan">Área de exploração</p>
              <h2 class="progression-page-title">${info.name}</h2>
              <p class="progression-page-subtitle">Selecione uma missão para escolher o time que será enviado.</p>
            </div>
          </div>
          <div class="mission-area-terminal-mark" aria-hidden="true"><span></span><span></span><span></span></div>
        </div>
      </header>
      <section class="mission-area-missions-strip">
        <div><p class="mission-area-strip-label">Protocolo de campo</p><p class="mission-area-strip-title">Operações disponíveis</p></div>
        <div class="mission-area-strip-status"><span class="mission-area-strip-dot"></span><span id="mission-area-count">—</span> missões</div>
      </section>

      <div id="mission-list">
        <div class="mission-detail-skeleton"></div>
        <div class="mission-detail-skeleton"></div>
      </div>
    </div>
  `;

  try {
    const missions = await apiGet("/missions");
    const areaMissions = missions.filter(m => {
      const missionArea = getMissionArea(m);
      return missionArea === area;
    });
    renderMissionCards(areaMissions, area);
  } catch (err) {
    document.getElementById("mission-list").innerHTML = `
      <div class="card border-red-900"><p class="text-red-300">${escapeHtml(err.message)}</p></div>
    `;
  }
}

function getMissionArea(mission) {
  return mission.area || null;
}

function renderMissionCards(missions, area) {
  const container = document.getElementById("mission-list");
  window._missionDefinitions = Object.fromEntries(missions.map(mission => [missionIdentity(mission), mission]));

  const countElement = document.getElementById("mission-area-count");
  if (countElement) countElement.textContent = String(missions.length);

  if (missions.length === 0) {
    container.innerHTML = `
      <div class="mission-area-empty-state">
        <div class="mission-area-empty-icon" aria-hidden="true">⌁</div>
        <p class="mission-area-empty-title">Nenhuma missão disponível</p>
        <p class="mission-area-empty-note">Nenhuma missão foi configurada para esta área.</p>
      </div>
    `;
    return;
  }

    const sortedMissions = [...missions].sort(compareMissionsByDescendingProgression);
  const progressionNumbers = new Map(
    [...missions]
      .sort(compareMissionsByAscendingProgression)
      .map((mission, index) => [missionIdentity(mission), index + 1])
  );
  container.innerHTML = sortedMissions.map((m, index) => `
    <article class="mission-detail-card">
      <div class="mission-detail-topline"><p class="progression-eyebrow progression-eyebrow-cyan">Missão ${String(progressionNumbers.get(missionIdentity(m)) || index + 1).padStart(2, "0")}</p><span class="mission-detail-code">${escapeHtml(missionFriendlyCode(m.id || "OPS"))}</span></div>
      <div class="mission-detail-heading"><div><h3 class="mission-detail-title">${escapeHtml(formatMissionName(m))}</h3><span class="mission-detail-status"><span></span> Operação disponível</span></div><span class="mission-detail-arrow" aria-hidden="true">✦</span></div>
      <p class="mission-detail-description">${escapeHtml(m.description) || "Sem descrição disponível."}</p>
      <div class="mission-detail-reward-row"><span class="mission-detail-reward-label">Retorno previsto</span><span class="mission-detail-reward-xp">+${m.xpReward} XP base</span>${m.bitsReward > 0 ? `<span class="mission-detail-reward-bits">+${m.bitsReward} bits base</span>` : ""}</div>
      <div class="mission-detail-meta">
        <span><small>REQUISITO</small>⚡ Nível ${m.requiredLevel}</span>
        <span><small>CUSTO</small>🔋 ${m.energyCost} energia</span>
        <span><small>DURAÇÃO</small>⏱️ ${formatTime(m.durationSeconds)}</span>
      </div>
      <div class="mission-detail-actions">
        <button type="button" class="btn-secondary flex-1 text-sm" onclick="missionShowLootPreview('${escapeAttr(m.id)}', this)">Ver recompensas</button>
        <button type="button" class="btn-primary flex-1 text-sm" onclick="startMission('${escapeAttr(m.id)}', '${escapeAttr(area)}')">Iniciar missão</button>
      </div>
    </article>
  `).join("");
}

async function startMission(missionId, area) {
  openMissionTeamSelectionModal(missionId);
}
