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

function compareMissionsByProgression(a, b) {
  const levelDifference = (Number(b.requiredLevel) || 0) - (Number(a.requiredLevel) || 0);
  if (levelDifference !== 0) return levelDifference;

  const numberDifference = missionNumber(b) - missionNumber(a);
  if (numberDifference !== 0) return numberDifference;

  return String(b.id || b.missionId || "").localeCompare(String(a.id || a.missionId || ""));
}

async function renderMissionsPage() {
  const app = document.getElementById("app");
  showBottomNav("missions");

  app.innerHTML = `
    <div class="page-container">
      <div id="active-missions"></div>
      <h2 class="text-lg font-bold mb-4 px-1">Mapa de Áreas</h2>
      <div id="areas-list">
        <div class="card animate-pulse mb-3"><div class="h-20"></div></div>
        <div class="card animate-pulse mb-3"><div class="h-20"></div></div>
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

  let active;
  try {
    active = await apiGet("/missions/active");
  } catch (err) {
    return;
  }

  if (!active || active.length === 0) {
    container.innerHTML = "";
    return;
  }

  container.innerHTML = `
    <div class="mb-4">
      <h3 class="text-sm font-bold text-slate-300 mb-2 px-1">Missões em Andamento</h3>
      ${active.map(renderActiveMissionCard).join("")}
    </div>
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

  return `
    <div class="card-sm mb-2 flex items-center justify-between" data-mp-instance="${m.missionInstanceId}" data-mp-ends-at="${m.endsAt}">
      <div class="min-w-0">
        <p class="font-bold text-sm truncate">${escapeHtml(formatMissionName(m))}</p>
        <p class="text-xs mp-timer ${done ? "text-green-400 font-bold" : "text-slate-500"}">${done ? "Concluída!" : formatTime(remaining)}</p>
      </div>
      ${done ? `
        <button class="btn-sm btn-primary" onclick="claimMissionFromList('${m.missionInstanceId}')">Resgatar</button>
      ` : `
        <span class="badge">Em andamento</span>
      `}
    </div>
  `;
}

async function claimMissionFromList(instanceId) {
  try {
    const result = await apiPost(`/missions/${instanceId}/claim`);
    showMissionClaimModal(result);
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
    return "🎁";
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
  if (itemType === "LOOT_CHEST") return "🎁";
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
            <span class="text-3xl" aria-hidden="true">🎁</span>
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

async function repeatMissionFromReward(missionId) {
  const button = document.getElementById("mission-repeat-button");
  if (!missionId || !button) return;

  button.disabled = true;
  button.textContent = "Iniciando...";

  try {
    await apiPost("/missions/start", { missionId });
    document.getElementById("mission-claim-modal")?.remove();
    showToast("Missão repetida!");
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
    button.disabled = false;
    button.textContent = "Repetir missão";
  }
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

      ${result && result.levelUp ? `
        <div class="rounded-lg border border-emerald-700 bg-emerald-950/40 px-3 py-3 mb-5 text-center">
          <p class="font-bold text-emerald-300">Level Up!</p>
          <p class="text-xs text-emerald-200 mt-1">Seu Digimon subiu de nível.</p>
        </div>
      ` : ""}

      <div>
        <h4 class="text-sm font-bold text-slate-300 mb-2">Itens recebidos</h4>
        <div class="space-y-2">${rewardMarkup}</div>
      </div>

      ${result && result.missionId ? `
        <div class="flex flex-col sm:flex-row gap-2 mt-5">
          <button id="mission-repeat-button" class="btn-primary flex-1" onclick="repeatMissionFromReward('${escapeAttr(result.missionId)}')">Repetir missão</button>
          <button class="btn-secondary flex-1" onclick="document.getElementById('mission-claim-modal')?.remove()">Continuar</button>
        </div>
      ` : `
        <button class="btn-primary w-full mt-5" onclick="document.getElementById('mission-claim-modal')?.remove()">Continuar</button>
      `}
    </div>
  `;
  overlay.addEventListener("click", event => {
    if (event.target === overlay) overlay.remove();
  });
  document.body.appendChild(overlay);
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
        timerEl.className = "text-xs mp-timer text-green-400 font-bold";
        const badge = el.querySelector(".badge");
        if (badge) {
          badge.outerHTML = `<button class="btn-sm btn-primary" onclick="claimMissionFromList('${el.dataset.mpInstance}')">Resgatar</button>`;
        }
      } else {
        timerEl.textContent = formatTime(remaining);
      }
    });
  }, 1000);
}

function renderAreaCards(areas) {
  const container = document.getElementById("areas-list");

  const sorted = [...areas].sort((a, b) => {
    const areaDifference = areaProgressionRank(b.area) - areaProgressionRank(a.area);
    if (areaDifference !== 0) return areaDifference;
    return stageProgressionRank(b.requiredStage) - stageProgressionRank(a.requiredStage);
  });

  container.innerHTML = sorted.map(a => {
    const info = AREA_INFO[a.area] || { name: a.area, emoji: "📍", color: "from-slate-800 to-slate-900", border: "border-slate-700" };
    const locked = !a.unlocked;

    return `
      <div class="card mb-3 bg-gradient-to-r ${info.color} ${info.border} ${locked ? "opacity-40" : "cursor-pointer hover:scale-[1.02] transition-transform"}"
        ${locked ? "" : `onclick="navigateTo('mission-area', { area: '${a.area}' })"`}>
        <div class="flex items-center gap-4">
          <span class="text-3xl">${info.emoji}</span>
          <div class="flex-1">
            <h3 class="font-bold">${info.name}</h3>
            <p class="text-xs text-slate-400">Stage mínimo: ${formatStage(a.requiredStage)}</p>
          </div>
          ${locked ? `<span class="text-xl">🔒</span>` : `<span class="text-slate-400">›</span>`}
        </div>
      </div>
    `;
  }).join("");
}


// Mission list for a specific area
async function renderMissionAreaPage(params) {
  const area = (params && params.area) || (window._routeParams && window._routeParams.area);
  if (!area) return navigateTo("missions");

  const app = document.getElementById("app");
  const info = AREA_INFO[area] || { name: area, emoji: "📍" };
  showBottomNav("missions");

  app.innerHTML = `
    <div class="page-container">
      <button class="text-sm text-cyan-400 mb-3 flex items-center gap-1" onclick="navigateTo('missions')">
        ← Voltar ao Mapa
      </button>

      <div class="flex items-center gap-3 mb-4">
        <span class="text-3xl">${info.emoji}</span>
        <div>
          <h2 class="text-lg font-bold">${info.name}</h2>
          <p class="text-xs text-slate-400">Missões disponíveis</p>
        </div>
      </div>

      <div id="mission-list">
        <div class="card animate-pulse mb-3"><div class="h-24"></div></div>
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

  if (missions.length === 0) {
    container.innerHTML = `
      <div class="card text-center">
        <p class="text-slate-400">Nenhuma missão disponível nesta área.</p>
        <p class="text-xs text-slate-500 mt-1">Seu Digimon pode não atender os requisitos.</p>
      </div>
    `;
    return;
  }

  const sortedMissions = [...missions].sort(compareMissionsByProgression);

  container.innerHTML = sortedMissions.map(m => `
    <div class="card mb-3">
      <div class="flex justify-between items-start mb-2">
        <div class="flex-1">
          <h3 class="font-bold text-sm">${escapeHtml(formatMissionName(m))}</h3>
          <p class="text-xs text-slate-400 mt-1">${escapeHtml(m.description) || ""}</p>
        </div>
      </div>

      <div class="flex gap-3 text-xs text-slate-400 mb-3 flex-wrap">
        <span>⚡ Nível ${m.requiredLevel}</span>
        <span>✨ ${m.xpReward} XP</span>
        ${m.bitsReward > 0 ? `<span class="text-yellow-500">💰 ${m.bitsReward} bits</span>` : ""}
        <span>🔋 ${m.energyCost} energia</span>
        <span>⏱️ ${formatTime(m.durationSeconds)}</span>
      </div>

      <div class="flex flex-col sm:flex-row gap-2">
        <button type="button" class="btn-secondary flex-1 text-sm" onclick="missionShowLootPreview('${escapeAttr(m.id)}', this)">
          Ver recompensas
        </button>
        <button type="button" class="btn-primary flex-1 text-sm" onclick="startMission('${escapeAttr(m.id)}', '${escapeAttr(area)}')">
          Iniciar Missão
        </button>
      </div>
    </div>
  `).join("");
}

async function startMission(missionId, area) {
  try {
    await apiPost("/missions/start", { missionId: missionId });
    showToast("Missão iniciada!");
    navigateTo("dashboard");
  } catch (err) {
    showToast(err.message, "error");
  }
}
