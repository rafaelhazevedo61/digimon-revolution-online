let adminBosses = [];
let adminBossEditId = null;
let adminBossChestOptions = [];
let adminBossRarityProfiles = [];

async function renderBossesAdminPage() {
  const app = document.getElementById("app");
  document.getElementById("page-title").textContent = "Bosses";
  document.getElementById("page-subtitle").textContent = "Gerenciar definicoes de bosses e drops";

  app.innerHTML = `
    <div class="mb-4 flex flex-wrap gap-3 justify-between items-center">
      <div>
        <h3 class="text-lg font-bold">Boss Definitions</h3>
        <p class="text-sm text-slate-400">Configure Bosses, equipamentos legados e recompensas em Baús.</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <button type="button" class="px-4 py-2 bg-slate-700 hover:bg-slate-600 rounded-lg text-sm font-bold" onclick="openBossRarityProfiles()">Raridade de Equipamentos</button>
        <button type="button" class="px-4 py-2 bg-cyan-700 hover:bg-cyan-600 rounded-lg text-sm font-bold" onclick="openBossForm()">+ Novo Boss</button>
      </div>
    </div>
    <div id="bosses-table-container">
      <p class="text-slate-400">Carregando...</p>
    </div>
    <div id="boss-rarity-modal"></div>
    <div id="boss-form-modal"></div>
    <div id="boss-drops-modal"></div>
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
    document.getElementById("bosses-table-container").innerHTML = `<p class="text-red-400">${escapeHtml(err.message)}</p>`;
  }
}

function equipmentPoolChance(boss) {
  const equipmentDrops = (boss.drops || []).filter(drop => drop.dropType === "EQUIPMENT");
  return equipmentDrops.length > 0 ? Number(equipmentDrops[0].chance) : null;
}

function cooldownSummary(boss) {
  if (["WORLD", "CLAN"].includes(boss.bossType)) {
    return `<span class="text-cyan-300">Por ambiente</span><div class="text-[10px] text-slate-500">${boss.cooldownMinutes} min · YAML</div>`;
  }
  return boss.cooldownEnabled === false
    ? '<span class="text-slate-500">Desligado</span>'
    : `<span class="text-green-400">Ativo</span><div class="text-[10px] text-slate-500">${boss.cooldownMinutes} min</div>`;
}

function equipmentPoolSummary(boss) {
  const poolChance = equipmentPoolChance(boss);
  const equipmentDrops = (boss.drops || []).filter(drop => drop.dropType === "EQUIPMENT");
  if (poolChance === null) {
    return '<span class="text-slate-500">Sem equipamento</span>';
  }
  const optionLabel = equipmentDrops.length === 1 ? "1 opção" : `${equipmentDrops.length} opções`;
  return `<div class="font-semibold text-purple-300">${poolChance}%</div><div class="text-[10px] text-slate-500">Pool · ${optionLabel}</div>`;
}

function bossChestSummary(boss) {
  if (boss.bossType === "WORLD") {
    const entries = [
      ["Tentativa", boss.worldAttemptChestName, boss.worldAttemptChestCode],
      ["Maior dano", boss.worldTopDamageChestName, boss.worldTopDamageChestCode],
      ["Golpe final", boss.worldFinalBlowChestName, boss.worldFinalBlowChestCode]
    ];
    return entries.map(([label, name, code]) => `
      <div class="mb-1">
        <span class="text-[10px] uppercase text-slate-500">${label}</span>
        <div class="font-semibold text-slate-300">${escapeHtml(name || "Sem Baú")}</div>
        <div class="text-[10px] text-slate-500 font-mono">${escapeHtml(code || "-")}</div>
      </div>
    `).join("");
  }
  return `
    <div class="font-semibold text-slate-300">${escapeHtml(boss.chestName || "Sem Baú")}</div>
    <div class="text-[10px] text-slate-500 font-mono">${escapeHtml(boss.chestCode || "-")}</div>
  `;
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
    MONTHLY: "bg-yellow-700 text-yellow-100",
    CLAN: "bg-emerald-700 text-emerald-100",
    WORLD: "bg-red-700 text-red-100"
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
            <th class="py-2 px-2">Chance Equipamento</th>
            <th class="py-2 px-2">Cooldown</th>
            <th class="py-2 px-2">Baú de Recompensa</th>
            <th class="py-2 px-2">Ativo</th>
            <th class="py-2 px-2">Acoes</th>
          </tr>
        </thead>
        <tbody>
          ${adminBosses.map(b => `
            <tr class="border-b border-slate-800 hover:bg-slate-800/50">
              <td class="py-2 px-2 text-slate-500">${b.id}</td>
              <td class="py-2 px-2 font-bold">${escapeHtml(b.name)}</td>
              <td class="py-2 px-2"><span class="px-2 py-0.5 rounded text-xs font-bold ${typeColors[b.bossType] || ""}">${b.bossType}</span></td>
              <td class="py-2 px-2">${escapeHtml(b.requiredStage)}</td>
              <td class="py-2 px-2">${b.requiredLevel}</td>
              <td class="py-2 px-2 text-xs">${b.hp}/${b.atk}/${b.def}</td>
              <td class="py-2 px-2 text-yellow-400">${b.baseXpReward}</td>
              <td class="py-2 px-2 text-amber-400">${b.baseBitsReward}</td>
              <td class="py-2 px-2">${equipmentPoolSummary(b)}</td>
              <td class="py-2 px-2">${cooldownSummary(b)}</td>
              <td class="py-2 px-2">${bossChestSummary(b)}</td>
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

const bossRarityLabels = {
  BOSS_NORMAL: "Boss Normal",
  BOSS_DAILY: "Boss Diário",
  BOSS_WEEKLY: "Boss Semanal",
  BOSS_MONTHLY: "Boss Mensal"
};

async function openBossRarityProfiles() {
  const root = document.getElementById("boss-rarity-modal");
  if (!root) return;

  root.innerHTML = `
    <div class="modal-overlay" onclick="closeBossRarityProfiles()">
      <div class="modal-content modal-wide" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold">Raridade de Equipamentos</h3>
            <p class="text-sm text-slate-400 mt-1">Percentuais usados no roll de raridade após a queda do equipamento.</p>
          </div>
          <button type="button" class="text-slate-400 hover:text-white text-2xl" onclick="closeBossRarityProfiles()">&times;</button>
        </div>
        <p class="text-slate-400 text-sm">Carregando...</p>
      </div>
    </div>
  `;

  try {
    adminBossRarityProfiles = await apiGet("/admin/bosses/rarity-profiles");
    renderBossRarityProfiles();
  } catch (err) {
    root.innerHTML = `
      <div class="modal-overlay" onclick="closeBossRarityProfiles()">
        <div class="modal-content" onclick="event.stopPropagation()">
          <p class="text-red-400">${escapeHtml(err.message)}</p>
          <button type="button" class="mt-4 px-3 py-1 bg-slate-700 hover:bg-slate-600 rounded text-xs" onclick="closeBossRarityProfiles()">Fechar</button>
        </div>
      </div>
    `;
  }
}

function renderBossRarityProfiles() {
  const root = document.getElementById("boss-rarity-modal");
  if (!root) return;

  root.innerHTML = `
    <div class="modal-overlay" onclick="closeBossRarityProfiles()">
      <div class="modal-content modal-wide" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold">Raridade de Equipamentos</h3>
            <p class="text-sm text-slate-400 mt-1">A soma de cada perfil deve ser exatamente 100%.</p>
          </div>
          <button type="button" class="text-slate-400 hover:text-white text-2xl" onclick="closeBossRarityProfiles()">&times;</button>
        </div>
        <div class="space-y-4">
          ${adminBossRarityProfiles.map(profile => `
            <form class="p-4 bg-slate-800 rounded-xl border border-slate-700" onsubmit="saveBossRarityProfile(event, '${escapeAttr(profile.profileKey)}')">
              <div class="flex flex-wrap items-center justify-between gap-2 mb-3">
                <div>
                  <h4 class="font-bold">${escapeHtml(bossRarityLabels[profile.profileKey] || profile.displayName)}</h4>
                  <p class="text-xs text-slate-500 font-mono">${escapeHtml(profile.profileKey)}</p>
                </div>
                <span class="text-xs text-slate-400">Atualizado por ${escapeHtml(profile.updatedBy || "-")}</span>
              </div>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <label class="text-xs text-slate-400">Common %<input id="rp-${escapeAttr(profile.profileKey)}-common" type="number" min="0" max="100" class="w-full mt-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm text-slate-100" value="${profile.commonPercent}" required></label>
                <label class="text-xs text-slate-400">Rare %<input id="rp-${escapeAttr(profile.profileKey)}-rare" type="number" min="0" max="100" class="w-full mt-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm text-slate-100" value="${profile.rarePercent}" required></label>
                <label class="text-xs text-slate-400">Epic %<input id="rp-${escapeAttr(profile.profileKey)}-epic" type="number" min="0" max="100" class="w-full mt-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm text-slate-100" value="${profile.epicPercent}" required></label>
                <label class="text-xs text-slate-400">Legendary %<input id="rp-${escapeAttr(profile.profileKey)}-legendary" type="number" min="0" max="100" class="w-full mt-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm text-slate-100" value="${profile.legendaryPercent}" required></label>
              </div>
              <div id="rp-${escapeAttr(profile.profileKey)}-error" class="hidden mt-3 p-2 rounded bg-red-950/30 border border-red-900 text-red-200 text-xs"></div>
              <div class="flex justify-end mt-3"><button type="submit" class="px-3 py-1 bg-cyan-700 hover:bg-cyan-600 rounded text-xs font-bold">Salvar perfil</button></div>
            </form>
          `).join("")}
        </div>
        <div class="flex justify-end mt-6"><button type="button" class="px-3 py-1 bg-slate-700 hover:bg-slate-600 rounded text-xs" onclick="closeBossRarityProfiles()">Fechar</button></div>
      </div>
    </div>
  `;
}

async function saveBossRarityProfile(event, profileKey) {
  event.preventDefault();
  const normalizedKey = profileKey.replace(/[^A-Z_]/g, "");
  const values = {
    commonPercent: parseInt(document.getElementById(`rp-${normalizedKey}-common`).value, 10),
    rarePercent: parseInt(document.getElementById(`rp-${normalizedKey}-rare`).value, 10),
    epicPercent: parseInt(document.getElementById(`rp-${normalizedKey}-epic`).value, 10),
    legendaryPercent: parseInt(document.getElementById(`rp-${normalizedKey}-legendary`).value, 10)
  };
  const error = document.getElementById(`rp-${normalizedKey}-error`);
  const total = Object.values(values).reduce((sum, value) => sum + value, 0);
  if (total !== 100) {
    error.textContent = `Os percentuais devem somar 100%. Soma atual: ${total}%.`;
    error.classList.remove("hidden");
    return;
  }
  error.classList.add("hidden");

  try {
    const updated = await apiPut(`/admin/bosses/rarity-profiles/${encodeURIComponent(profileKey)}`, values);
    adminBossRarityProfiles = adminBossRarityProfiles.map(profile => profile.profileKey === updated.profileKey ? updated : profile);
    renderBossRarityProfiles();
  } catch (err) {
    error.textContent = err.message;
    error.classList.remove("hidden");
  }
}

function closeBossRarityProfiles() {
  const root = document.getElementById("boss-rarity-modal");
  if (root) root.innerHTML = "";
}

function chestOptionsHtml(selectedCode) {
  return adminBossChestOptions.map(c => `
    <option value="${escapeAttr(c.code)}" ${selectedCode === c.code ? "selected" : ""}>
      ${escapeHtml(c.name)} — ${escapeHtml(c.code)}
    </option>
  `).join("");
}

function openBossForm(id = null) {
  adminBossEditId = id;
  const boss = id ? adminBosses.find(b => b.id === id) : null;
  const equipmentChance = boss ? equipmentPoolChance(boss) : null;
  const root = document.getElementById("boss-form-modal");
  if (!root) return;

  root.innerHTML = `
    <div class="modal-overlay" onclick="closeBossForm()">
      <div class="modal-content modal-wide" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold">${boss ? "Editar Boss" : "Novo Boss"}</h3>
            <p class="text-sm text-slate-400 mt-1">Configure os dados de combate e o Baú de recompensa.</p>
          </div>
          <button type="button" class="text-slate-400 hover:text-white text-2xl" onclick="closeBossForm()">&times;</button>
        </div>
        <form id="boss-form" onsubmit="saveBoss(event)">
          <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
            <div>
              <label class="text-xs text-slate-400">Code</label>
              <input id="bf-code" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${escapeAttr(boss?.code || "")}" ${boss ? "disabled" : ""}>
            </div>
            <div>
              <label class="text-xs text-slate-400">Nome</label>
              <input id="bf-name" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${escapeAttr(boss?.name || "")}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">Tipo</label>
              <select id="bf-type" onchange="updateBossChestRequirement(); updateBossCooldownControls()" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
                <option value="NORMAL" ${boss?.bossType === "NORMAL" || !boss ? "selected" : ""}>Normal</option>
                <option value="DAILY" ${boss?.bossType === "DAILY" ? "selected" : ""}>Diário</option>
                <option value="WEEKLY" ${boss?.bossType === "WEEKLY" ? "selected" : ""}>Semanal</option>
                <option value="MONTHLY" ${boss?.bossType === "MONTHLY" ? "selected" : ""}>Mensal</option>
                <option value="CLAN" ${boss?.bossType === "CLAN" ? "selected" : ""}>Clã</option>
                <option value="WORLD" ${boss?.bossType === "WORLD" ? "selected" : ""}>Mundial</option>
              </select>
            </div>
            <div>
              <label class="text-xs text-slate-400">Stage Mínimo</label>
              <select id="bf-stage" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
                <option value="BABY" ${boss?.requiredStage === "BABY" ? "selected" : ""}>Baby</option>
                <option value="ROOKIE" ${boss?.requiredStage === "ROOKIE" || !boss ? "selected" : ""}>Rookie</option>
                <option value="CHAMPION" ${boss?.requiredStage === "CHAMPION" ? "selected" : ""}>Champion</option>
                <option value="ULTIMATE" ${boss?.requiredStage === "ULTIMATE" ? "selected" : ""}>Ultimate</option>
                <option value="MEGA" ${boss?.requiredStage === "MEGA" ? "selected" : ""}>Mega</option>
              </select>
            </div>
            <div>
              <label class="text-xs text-slate-400">Level Mínimo</label>
              <input id="bf-level" type="number" min="1" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.requiredLevel ?? 1}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">Rebirths</label>
              <input id="bf-rebirths" type="number" min="0" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.requiredRebirths ?? 0}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">HP</label>
              <input id="bf-hp" type="number" min="1" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.hp ?? 500}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">ATK</label>
              <input id="bf-atk" type="number" min="1" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.atk ?? 80}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">DEF</label>
              <input id="bf-def" type="number" min="1" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.def ?? 50}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">Energia</label>
              <input id="bf-energy" type="number" min="0" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.energyCost ?? 5}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">Cooldown (min)</label>
              <input id="bf-cooldown" type="number" min="0" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.cooldownMinutes ?? 5}" required>
            </div>
            <div id="bf-cooldown-enabled-container" class="flex flex-col justify-end">
              <label class="flex items-center gap-2 text-sm text-slate-200 cursor-pointer">
                <input id="bf-cooldown-enabled" type="checkbox" class="accent-cyan-500" ${!boss || boss.cooldownEnabled !== false ? "checked" : ""}>
                Ativar cooldown individual
              </label>
              <p class="text-[10px] text-slate-500 mt-1">Disponível apenas para bosses comuns e periódicos.</p>
            </div>
            <div>
              <label class="text-xs text-slate-400">XP Reward</label>
              <input id="bf-xp" type="number" min="0" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.baseXpReward ?? 200}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">Bits Reward</label>
              <input id="bf-bits" type="number" min="0" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.baseBitsReward ?? 100}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">Defeat XP %</label>
              <input id="bf-defeatxp" type="number" min="0" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${boss?.defeatXpPercent ?? 10}" required>
            </div>
            <div>
              <label class="text-xs text-slate-400">Chance Equipamento %</label>
              <input id="bf-equipment-chance" type="number" min="0" max="100" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${equipmentChance ?? ""}" ${equipmentChance === null ? "disabled" : ""}>
              <p class="text-[10px] text-slate-500 mt-1">${equipmentChance === null ? "Adicione um equipamento no modal Drops para configurar a pool." : "Aplica-se a todos os templates de equipamento desta pool."}</p>
            </div>
            <div class="col-span-2">
              <label class="text-xs text-slate-400">Image URL</label>
              <input id="bf-image" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm" value="${escapeAttr(boss?.imageUrl || "")}" placeholder="https://...">
            </div>
            <div id="bf-standard-chest-container" class="col-span-2">
              <label class="text-xs text-slate-400">Baú de Recompensa (Vitória)</label>
              <select id="bf-chest" class="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
                <option value="">Selecione um baú ativo...</option>
                ${chestOptionsHtml(boss?.chestCode)}
              </select>
              <p class="text-[10px] text-slate-500 mt-1">Usado por Bosses normais e periódicos. Somente baús ativos com Loot Tables ativas aparecem aqui.</p>
            </div>
            <div id="bf-world-chests-container" class="col-span-2 hidden">
              <div class="mb-3">
                <p class="text-sm font-semibold text-slate-200">Baús do Boss Mundial</p>
                <p class="text-[10px] text-slate-500 mt-1">Cada situação deve apontar para um Baú próprio com sua Loot Table. Os três Baús precisam ser diferentes.</p>
              </div>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
                <label class="text-xs text-slate-400">Baú por tentativa
                  <select id="bf-world-attempt-chest" class="w-full mt-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
                    <option value="">Selecione um baú ativo...</option>
                    ${chestOptionsHtml(boss?.worldAttemptChestCode)}
                  </select>
                </label>
                <label class="text-xs text-slate-400">Baú de maior dano
                  <select id="bf-world-top-damage-chest" class="w-full mt-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
                    <option value="">Selecione um baú ativo...</option>
                    ${chestOptionsHtml(boss?.worldTopDamageChestCode)}
                  </select>
                </label>
                <label class="text-xs text-slate-400">Baú do golpe final
                  <select id="bf-world-final-blow-chest" class="w-full mt-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded text-sm">
                    <option value="">Selecione um baú ativo...</option>
                    ${chestOptionsHtml(boss?.worldFinalBlowChestCode)}
                  </select>
                </label>
              </div>
            </div>
          </div>
          <div class="flex gap-2 mt-6">
            <button type="submit" class="px-4 py-2 bg-cyan-700 hover:bg-cyan-600 rounded text-sm font-bold flex-1">Salvar</button>
            <button type="button" class="px-4 py-2 bg-slate-700 hover:bg-slate-600 rounded text-sm flex-1" onclick="closeBossForm()">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  `;
  updateBossChestRequirement();
  updateBossCooldownControls();
  document.getElementById("bf-cooldown-enabled")?.addEventListener("change", updateBossCooldownControls);
}

function updateBossCooldownControls() {
  const type = document.getElementById("bf-type")?.value;
  const supportsIndividualCooldown = !["WORLD", "CLAN"].includes(type);
  const container = document.getElementById("bf-cooldown-enabled-container");
  const enabled = document.getElementById("bf-cooldown-enabled")?.checked ?? true;
  const cooldown = document.getElementById("bf-cooldown");
  if (!cooldown) return;
  container?.classList.toggle("hidden", !supportsIndividualCooldown);
  cooldown.disabled = supportsIndividualCooldown ? !enabled : false;
  cooldown.required = true;
}

function updateBossChestRequirement() {
  const type = document.getElementById("bf-type")?.value;
  const chest = document.getElementById("bf-chest");
  const standardContainer = document.getElementById("bf-standard-chest-container");
  const worldContainer = document.getElementById("bf-world-chests-container");
  if (!chest || !standardContainer || !worldContainer) return;

  const requiresStandardChest = ["NORMAL", "DAILY", "WEEKLY", "MONTHLY"].includes(type);
  const isWorldBoss = type === "WORLD";
  standardContainer.classList.toggle("hidden", !requiresStandardChest);
  worldContainer.classList.toggle("hidden", !isWorldBoss);
  chest.required = requiresStandardChest;
  chest.disabled = !requiresStandardChest;

  [
    document.getElementById("bf-world-attempt-chest"),
    document.getElementById("bf-world-top-damage-chest"),
    document.getElementById("bf-world-final-blow-chest")
  ].forEach(worldChest => {
    if (!worldChest) return;
    worldChest.required = isWorldBoss;
    worldChest.disabled = !isWorldBoss;
  });
}

function closeBossForm() {
  const root = document.getElementById("boss-form-modal");
  if (root) root.innerHTML = "";
  adminBossEditId = null;
}

async function saveBoss(event) {
  event?.preventDefault();
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
    chestCode: document.getElementById("bf-chest").value || null,
    worldAttemptChestCode: document.getElementById("bf-world-attempt-chest")?.value || null,
    worldTopDamageChestCode: document.getElementById("bf-world-top-damage-chest")?.value || null,
    worldFinalBlowChestCode: document.getElementById("bf-world-final-blow-chest")?.value || null
  };

  if (!["WORLD", "CLAN"].includes(body.bossType)) {
    body.cooldownEnabled = document.getElementById("bf-cooldown-enabled")?.checked ?? true;
  }

  if (body.bossType === "WORLD") {
    const worldChestCodes = [
      body.worldAttemptChestCode,
      body.worldTopDamageChestCode,
      body.worldFinalBlowChestCode
    ];
    if (worldChestCodes.some(code => !code)) {
      alert("Selecione os Baús de tentativa, maior dano e golpe final.");
      return;
    }
    if (new Set(worldChestCodes).size !== worldChestCodes.length) {
      alert("Os três Baús do Boss Mundial devem ser diferentes.");
      return;
    }
  }
  const equipmentChanceField = document.getElementById("bf-equipment-chance");
  if (equipmentChanceField && !equipmentChanceField.disabled && equipmentChanceField.value !== "") {
    body.equipmentChance = parseInt(equipmentChanceField.value, 10);
  }

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
  const root = document.getElementById("boss-drops-modal");
  if (!boss || !root) return;

  root.innerHTML = `
    <div class="modal-overlay" onclick="closeBossDrops()">
      <div class="modal-content modal-wide" onclick="event.stopPropagation()">
        <div class="flex items-center justify-between mb-6">
          <div>
            <h3 class="text-xl font-bold">Drops de ${escapeHtml(boss.name)}</h3>
            <p class="text-sm text-slate-400 mt-1">Equipamentos permanecem no fluxo legado; itens devem usar a Loot Table do Baú.</p>
          </div>
          <button type="button" class="text-slate-400 hover:text-white text-2xl" onclick="closeBossDrops()">&times;</button>
        </div>
        <p class="text-slate-400 text-sm">Carregando...</p>
      </div>
    </div>
  `;

  try {
    const fullBoss = await apiGet(`/admin/bosses/${bossId}`);
    const drops = fullBoss.drops || [];

    root.innerHTML = `
      <div class="modal-overlay" onclick="closeBossDrops()">
        <div class="modal-content modal-wide" onclick="event.stopPropagation()">
          <div class="flex items-center justify-between mb-6">
            <div>
              <h3 class="text-xl font-bold">Drops de ${escapeHtml(boss.name)}</h3>
              <p class="text-sm text-slate-400 mt-1">Equipamentos permanecem no fluxo legado; itens devem usar a Loot Table do Baú.</p>
            </div>
            <button type="button" class="text-slate-400 hover:text-white text-2xl" onclick="closeBossDrops()">&times;</button>
          </div>
          <div class="flex justify-end mb-3">
            <button type="button" class="px-3 py-1 bg-cyan-700 hover:bg-cyan-600 rounded text-xs font-bold" onclick="openDropForm(${bossId})">+ Drop</button>
          </div>
          ${drops.length === 0 ? '<p class="text-slate-400 text-sm">Nenhum drop configurado.</p>' : `
            <div class="overflow-x-auto">
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
                      <td class="py-1 px-2"><span class="px-1.5 py-0.5 rounded text-xs ${d.dropType === "EQUIPMENT" ? "bg-purple-800" : "bg-slate-700"}">${escapeHtml(d.dropType)}</span></td>
                      <td class="py-1 px-2">${escapeHtml(d.dropType === "EQUIPMENT" ? (d.templateName || "-") : (d.itemCode || "-"))}</td>
                      <td class="py-1 px-2">${d.chance}%</td>
                      <td class="py-1 px-2">${d.minQuantity}-${d.maxQuantity}</td>
                      <td class="py-1 px-2"><button type="button" class="text-red-400 text-xs hover:text-red-300" onclick="deleteDrop(${d.id}, ${bossId})">Remover</button></td>
                    </tr>
                  `).join("")}
                </tbody>
              </table>
            </div>
          `}
          <div id="drop-form-${bossId}"></div>
          <div class="flex justify-end mt-6">
            <button type="button" class="px-3 py-1 bg-slate-700 hover:bg-slate-600 rounded text-xs" onclick="closeBossDrops()">Fechar</button>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    root.innerHTML = `
      <div class="modal-overlay" onclick="closeBossDrops()">
        <div class="modal-content" onclick="event.stopPropagation()">
          <p class="text-red-400">${escapeHtml(err.message)}</p>
          <button type="button" class="mt-4 px-3 py-1 bg-slate-700 hover:bg-slate-600 rounded text-xs" onclick="closeBossDrops()">Fechar</button>
        </div>
      </div>
    `;
  }
}

function closeBossDrops() {
  const root = document.getElementById("boss-drops-modal");
  if (root) root.innerHTML = "";
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
