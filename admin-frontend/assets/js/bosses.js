let adminBosses = [];
let adminBossEditId = null;
let adminBossChestOptions = [];

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
  return escapeHtml(value);
}

async function renderBossesAdminPage() {
  const app = document.getElementById("app");
  document.getElementById("page-title").textContent = "Bosses";
  document.getElementById("page-subtitle").textContent = "Gerenciar definicoes de bosses e drops";

  app.innerHTML = `
    <div class="mb-4 flex justify-between items-center">
      <h3 class="text-lg font-bold">Boss Definitions</h3>
      <button class="px-4 py-2 bg-cyan-700 hover:bg-cyan-600 rounded-lg text-sm font-bold" onclick="openBossForm()">+ Novo Boss</button>
    </div>
    <div id="bosses-table-container">
      <p class="text-slate-400">Carregando...</p>
    </div>
    <div id="boss-form-container"></div>
    <div id="boss-drops-container"></div>
  `;

  await loadBosses();
}

async function loadBosses() {
  try {
    const [bosses, chests] = await Promise.all([
      apiGet("/admin/bosses"),
      apiGet("/admin/bosses/chest-options")
    ]);
    adminBosses = bosses;
    adminBossChestOptions = chests;
    renderBossesTable();
  } catch (err) {
    document.getElementById("bosses-table-container").innerHTML = `<p class="text-red-400">${err.message}</p>`;
  }
}

function renderBossesTable() {
  const container = document.getElementById("bosses-table-container");

  if (adminBosses.length === 0) {
    container.innerHTML = `<p class="text-slate-400">Nenhum boss cadastrado.</p>`;
    return;
  }

  const typeColors = {
    NORMAL: "bg-slate-700 text-slate-200",
    DAILY: "bg-blue-700 text-blue-100",
    WEEKLY: "bg-purple-700 text-purple-100",
    MONTHLY: "bg-yellow-700 text-yellow-100"
  };

  container.innerHTML = `
    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-slate-700 text-left text-slate-400">
            <th class="py-2 px-2">ID</th>
            <th class="py-2 px-2">Nome</th>
            <th class="py-2 px-2">Tipo</th>
            <th class="py-2 px-2">Stage</th>
            <th class="py-2 px-2">Lv</th>
            <th class="py-2 px-2">HP/ATK/DEF</th>
            <th class="py-2 px-2">XP</th>
            <th class="py-2 px-2">Bits</th>
            <th class="py-2 px-2">Baú de Recompensa</th>
            <th class="py-2 px-2">Ativo</th>
            <th class="py-2 px-2">Acoes</th>
          </tr>
        </thead>
        <tbody>
          ${adminBosses.map(b => `
            <tr class="border-b border-slate-800 hover:bg-slate-800/50">
              <td class="py-2 px-2 text-slate-500">${b.id}</td>
              <td class="py-2 px-2 font-bold">${b.name}</td>
              <td class="py-2 px-2"><span class="px-2 py-0.5 rounded text-xs font-bold ${typeColors[b.bossType] || ""}">${b.bossType}</span></td>
              <td class="py-2 px-2">${b.requiredStage}</td>
              <td class="py-2 px-2">${b.requiredLevel}</td>
              <td class="py-2 px-2 text-xs">${b.hp}/${b.atk}/${b.def}</td>
              <td class="py-2 px-2 text-yellow-400">${b.baseXpReward}</td>
              <td class="py-2 px-2 text-amber-400">${b.baseBitsReward}</td>
              <td class="py-2 px-2">
                <div class="font-semibold text-slate-300">${escapeHtml(b.chestName || "Sem Baú")}</div>
                <div class="text-[10px] text-slate-500 font-mono">${escapeHtml(b.chestCode || "-")}</div>
              </td>
              <td class="py-2 px-2">${b.active ? '<span class="text-green-400">Sim</span>' : '<span class="text-red-400">Nao</span>'}</td>
              <td class="py-2 px-2">
                <div class="flex gap-1">
                  <button class="px-2 py-1 bg-slate-700 hover:bg-slate-600 rounded text-xs" onclick="openBossForm(${b.id})">Editar</button>
                  <button class="px-2 py-1 bg-purple-700 hover:bg-purple-600 rounded text-xs" onclick="openBossDrops(${b.id})">Drops</button>
                  <button class="px-2 py-1 bg-red-800 hover:bg-red-700 rounded text-xs" onclick="deleteBoss(${b.id})">X</button>
                </div>
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function openBossForm(id = null) {
  adminBossEditId = id;
  const boss = id ? adminBosses.find(b => b.id === id) : null;
  const container = document.getElementById("boss-form-container");

  container.innerHTML = `
    <div class="mt-6 p-4 bg-slate-800 rounded-xl border border-slate-700">
      <h4 class="font-bold mb-3">${boss ? "Editar Boss" : "Novo Boss"}</h4>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
        <div>
          <label class="text-xs text-slate-400">Code</label>
          <input id="bf-code" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.code : ""}" ${boss ? "disabled" : ""}>
        </div>
        <div>
          <label class="text-xs text-slate-400">Nome</label>
          <input id="bf-name" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.name : ""}">
        </div>
        <div>
          <label class="text-xs text-slate-400">Tipo</label>
          <select id="bf-type" onchange="updateBossChestRequirement()" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
            <option value="NORMAL" ${boss && boss.bossType === "NORMAL" ? "selected" : ""}>Normal</option>
            <option value="DAILY" ${boss && boss.bossType === "DAILY" ? "selected" : ""}>Diário</option>
            <option value="WEEKLY" ${boss && boss.bossType === "WEEKLY" ? "selected" : ""}>Semanal</option>
            <option value="MONTHLY" ${boss && boss.bossType === "MONTHLY" ? "selected" : ""}>Mensal</option>
          </select>
        </div>
        <div>
          <label class="text-xs text-slate-400">Stage Minimo</label>
          <select id="bf-stage" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
            <option value="BABY" ${boss && boss.requiredStage === "BABY" ? "selected" : ""}>Baby</option>
            <option value="ROOKIE" ${boss && boss.requiredStage === "ROOKIE" ? "selected" : ""}>Rookie</option>
            <option value="CHAMPION" ${boss && boss.requiredStage === "CHAMPION" ? "selected" : ""}>Champion</option>
            <option value="ULTIMATE" ${boss && boss.requiredStage === "ULTIMATE" ? "selected" : ""}>Ultimate</option>
            <option value="MEGA" ${boss && boss.requiredStage === "MEGA" ? "selected" : ""}>Mega</option>
          </select>
        </div>
        <div>
          <label class="text-xs text-slate-400">Level Minimo</label>
          <input id="bf-level" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.requiredLevel : 1}">
        </div>
        <div>
          <label class="text-xs text-slate-400">Rebirths</label>
          <input id="bf-rebirths" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.requiredRebirths : 0}">
        </div>
        <div>
          <label class="text-xs text-slate-400">HP</label>
          <input id="bf-hp" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.hp : 500}">
        </div>
        <div>
          <label class="text-xs text-slate-400">ATK</label>
          <input id="bf-atk" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.atk : 80}">
        </div>
        <div>
          <label class="text-xs text-slate-400">DEF</label>
          <input id="bf-def" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.def : 50}">
        </div>
        <div>
          <label class="text-xs text-slate-400">Energia</label>
          <input id="bf-energy" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.energyCost : 5}">
        </div>
        <div>
          <label class="text-xs text-slate-400">Cooldown (min)</label>
          <input id="bf-cooldown" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.cooldownMinutes : 360}">
        </div>
        <div>
          <label class="text-xs text-slate-400">XP Reward</label>
          <input id="bf-xp" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.baseXpReward : 200}">
        </div>
        <div>
          <label class="text-xs text-slate-400">Bits Reward</label>
          <input id="bf-bits" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.baseBitsReward : 100}">
        </div>
        <div>
          <label class="text-xs text-slate-400">Defeat XP %</label>
          <input id="bf-defeatxp" type="number" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? boss.defeatXpPercent : 10}">
        </div>
        <div class="col-span-2">
          <label class="text-xs text-slate-400">Image URL</label>
          <input id="bf-image" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss ? (boss.imageUrl || "") : ""}">
        </div>
        <div class="col-span-2">
          <label class="text-xs text-slate-400">Baú de Recompensa (Vitória)</label>
          <select id="bf-chest" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
            <option value="">Selecione um baú ativo...</option>
            ${adminBossChestOptions.map(c => `<option value="${escapeAttr(c.code)}" ${boss && boss.chestCode === c.code ? "selected" : ""}>${escapeHtml(c.name)} — ${escapeHtml(c.code)}</option>`).join("")}
          </select>
          <p class="text-[10px] text-slate-500 mt-1">Somente baús ativos com Loot Tables ativas aparecem aqui.</p>
        </div>
      </div>
      <div class="flex gap-2 mt-4">
        <button class="px-4 py-2 bg-cyan-700 hover:bg-cyan-600 rounded text-sm font-bold" onclick="saveBoss()">Salvar</button>
        <button class="px-4 py-2 bg-slate-700 hover:bg-slate-600 rounded text-sm" onclick="closeBossForm()">Cancelar</button>
      </div>
    </div>
  `;
  updateBossChestRequirement();
}

function updateBossChestRequirement() {
  const type = document.getElementById("bf-type")?.value;
  const chest = document.getElementById("bf-chest");
  if (!chest) return;
  const requiresChest = ["NORMAL", "DAILY", "WEEKLY", "MONTHLY"].includes(type);
  chest.required = requiresChest;
  chest.disabled = !requiresChest;
}

function closeBossForm() {
  document.getElementById("boss-form-container").innerHTML = "";
  adminBossEditId = null;
}

async function saveBoss() {
  const body = {
    code: document.getElementById("bf-code").value,
    name: document.getElementById("bf-name").value,
    bossType: document.getElementById("bf-type").value,
    requiredStage: document.getElementById("bf-stage").value,
    requiredLevel: parseInt(document.getElementById("bf-level").value),
    requiredRebirths: parseInt(document.getElementById("bf-rebirths").value),
    hp: parseInt(document.getElementById("bf-hp").value),
    atk: parseInt(document.getElementById("bf-atk").value),
    def: parseInt(document.getElementById("bf-def").value),
    energyCost: parseInt(document.getElementById("bf-energy").value),
    cooldownMinutes: parseInt(document.getElementById("bf-cooldown").value),
    baseXpReward: parseInt(document.getElementById("bf-xp").value),
    baseBitsReward: parseInt(document.getElementById("bf-bits").value),
    defeatXpPercent: parseInt(document.getElementById("bf-defeatxp").value),
    imageUrl: document.getElementById("bf-image").value || null,
    chestCode: document.getElementById("bf-chest").value
  };

  try {
    if (adminBossEditId) {
      await apiPut(`/admin/bosses/${adminBossEditId}`, body);
    } else {
      await apiPost("/admin/bosses", body);
    }
    closeBossForm();
    await loadBosses();
  } catch (err) {
    alert("Erro: " + err.message);
  }
}

async function deleteBoss(id) {
  if (!confirm("Remover boss?")) return;
  try {
    await apiDelete(`/admin/bosses/${id}`);
    await loadBosses();
  } catch (err) {
    alert("Erro: " + err.message);
  }
}

async function openBossDrops(bossId) {
  const boss = adminBosses.find(b => b.id === bossId);
  if (!boss) return;

  const container = document.getElementById("boss-drops-container");
  container.innerHTML = `<div class="mt-6 p-4 bg-slate-800 rounded-xl border border-slate-700">
    <h4 class="font-bold mb-3">Drops de ${boss.name}</h4>
    <p class="text-slate-400 text-sm">Carregando...</p>
  </div>`;

  try {
    const fullBoss = await apiGet(`/admin/bosses/${bossId}`);
    const drops = fullBoss.drops || [];

    container.innerHTML = `
      <div class="mt-6 p-4 bg-slate-800 rounded-xl border border-slate-700">
        <div class="flex justify-between items-center mb-3">
          <h4 class="font-bold">Drops de ${boss.name}</h4>
          <button class="px-3 py-1 bg-cyan-700 hover:bg-cyan-600 rounded text-xs font-bold" onclick="openDropForm(${bossId})">+ Drop</button>
        </div>
        ${drops.length === 0 ? '<p class="text-slate-400 text-sm">Nenhum drop configurado.</p>' : `
          <table class="w-full text-sm">
            <thead>
              <tr class="border-b border-slate-700 text-left text-slate-400">
                <th class="py-1 px-2">Tipo</th>
                <th class="py-1 px-2">Item/Template</th>
                <th class="py-1 px-2">Chance</th>
                <th class="py-1 px-2">Qtd</th>
                <th class="py-1 px-2"></th>
              </tr>
            </thead>
            <tbody>
              ${drops.map(d => `
                <tr class="border-b border-slate-800">
                  <td class="py-1 px-2"><span class="px-1.5 py-0.5 rounded text-xs ${d.dropType === "EQUIPMENT" ? "bg-purple-800" : "bg-slate-700"}">${d.dropType}</span></td>
                  <td class="py-1 px-2">${d.dropType === "EQUIPMENT" ? (d.templateName || "-") : (d.itemCode || "-")}</td>
                  <td class="py-1 px-2">${d.chance}%</td>
                  <td class="py-1 px-2">${d.minQuantity}-${d.maxQuantity}</td>
                  <td class="py-1 px-2"><button class="text-red-400 text-xs hover:text-red-300" onclick="deleteDrop(${d.id}, ${bossId})">X</button></td>
                </tr>
              `).join("")}
            </tbody>
          </table>
        `}
        <div id="drop-form-${bossId}"></div>
        <button class="mt-3 px-3 py-1 bg-slate-700 hover:bg-slate-600 rounded text-xs" onclick="document.getElementById('boss-drops-container').innerHTML=''">Fechar</button>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<p class="text-red-400">${err.message}</p>`;
  }
}

function openDropForm(bossId) {
  const container = document.getElementById(`drop-form-${bossId}`);
  container.innerHTML = `
    <div class="mt-3 p-3 bg-slate-900 rounded border border-slate-700">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-2">
        <div>
          <label class="text-xs text-slate-400">Tipo</label>
          <select id="df-type" class="w-full px-2 py-1 bg-slate-800 border border-slate-700 rounded text-sm" onchange="toggleDropFields()">
            <option value="ITEM">Item</option>
            <option value="EQUIPMENT">Equipment</option>
          </select>
        </div>
        <div id="df-item-field">
          <label class="text-xs text-slate-400">Item Code</label>
          <input id="df-itemcode" class="w-full px-2 py-1 bg-slate-800 border border-slate-700 rounded text-sm" placeholder="REFINEMENT_STONE">
        </div>
        <div id="df-template-field" class="hidden">
          <label class="text-xs text-slate-400">Template Name</label>
          <input id="df-template" class="w-full px-2 py-1 bg-slate-800 border border-slate-700 rounded text-sm" placeholder="Garra Berserker T5">
        </div>
        <div id="df-rarity-field" class="hidden">
          <label class="text-xs text-slate-400">Rarity</label>
          <select id="df-rarity" class="w-full px-2 py-1 bg-slate-800 border border-slate-700 rounded text-sm">
            <option value="COMMON">Common</option>
            <option value="RARE">Rare</option>
            <option value="EPIC">Epic</option>
            <option value="LEGENDARY">Legendary</option>
          </select>
        </div>
        <div>
          <label class="text-xs text-slate-400">Chance %</label>
          <input id="df-chance" type="number" class="w-full px-2 py-1 bg-slate-800 border border-slate-700 rounded text-sm" value="30">
        </div>
        <div>
          <label class="text-xs text-slate-400">Min Qtd</label>
          <input id="df-min" type="number" class="w-full px-2 py-1 bg-slate-800 border border-slate-700 rounded text-sm" value="1">
        </div>
        <div>
          <label class="text-xs text-slate-400">Max Qtd</label>
          <input id="df-max" type="number" class="w-full px-2 py-1 bg-slate-800 border border-slate-700 rounded text-sm" value="1">
        </div>
      </div>
      <button class="mt-2 px-3 py-1 bg-cyan-700 hover:bg-cyan-600 rounded text-xs font-bold" onclick="saveDrop(${bossId})">Adicionar</button>
    </div>
  `;
}

function toggleDropFields() {
  const type = document.getElementById("df-type").value;
  document.getElementById("df-item-field").classList.toggle("hidden", type !== "ITEM");
  document.getElementById("df-template-field").classList.toggle("hidden", type !== "EQUIPMENT");
  document.getElementById("df-rarity-field").classList.toggle("hidden", type !== "EQUIPMENT");
}

async function saveDrop(bossId) {
  const dropType = document.getElementById("df-type").value;
  const body = {
    dropType,
    itemCode: dropType === "ITEM" ? document.getElementById("df-itemcode").value : null,
    templateName: dropType === "EQUIPMENT" ? document.getElementById("df-template").value : null,
    equipmentRarity: dropType === "EQUIPMENT" ? document.getElementById("df-rarity").value : null,
    chance: parseInt(document.getElementById("df-chance").value),
    minQuantity: parseInt(document.getElementById("df-min").value),
    maxQuantity: parseInt(document.getElementById("df-max").value)
  };

  try {
    await apiPost(`/admin/bosses/${bossId}/drops`, body);
    openBossDrops(bossId);
  } catch (err) {
    alert("Erro: " + err.message);
  }
}

async function deleteDrop(dropId, bossId) {
  if (!confirm("Remover drop?")) return;
  try {
    await apiDelete(`/admin/bosses/drops/${dropId}`);
    openBossDrops(bossId);
  } catch (err) {
    alert("Erro: " + err.message);
  }
}
