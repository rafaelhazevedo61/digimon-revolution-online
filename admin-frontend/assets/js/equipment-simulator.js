// ==================== EQUIPMENT SIMULATOR ====================

const SIM_SETS = ["BERSERKER", "GUARDIAN", "VITALITY", "BALANCED"];
const SIM_SLOTS = ["WEAPON", "ARMOR", "ACCESSORY"];
const SIM_RARITIES = ["COMMON", "RARE", "EPIC", "LEGENDARY"];
const SIM_MAX_TIER = 10;
const SIM_MAX_REFINE = 10;

const SIM_TIER_MULTIPLIERS = [1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 5.5];
const SIM_RARITY_MULTIPLIERS = { COMMON: 1.0, RARE: 1.15, EPIC: 1.3, LEGENDARY: 1.5 };

const SIM_REFINE_SUCCESS = [100, 95, 90, 80, 70, 60, 50, 40, 30, 20];
const SIM_REFINE_COST = (level) => 1000 + (level * 500);

// Set bonuses: { 2: [hp%, atk%, def%], 3: [hp%, atk%, def%] }
const SIM_SET_BONUSES = {
  BERSERKER:  { 2: [0, 10, 0],  3: [0, 20, 0]  },
  GUARDIAN:   { 2: [5, 0, 10],  3: [10, 0, 20]  },
  VITALITY:   { 2: [10, 0, 0],  3: [20, 0, 0]   },
  BALANCED:   { 2: [5, 5, 5],   3: [10, 10, 10]  }
};

const SIM_BASE_STATS = {
  BERSERKER: {
    WEAPON:    { hp: 0,  atk: 10, def: 0 },
    ARMOR:     { hp: 5,  atk: 4,  def: 3 },
    ACCESSORY: { hp: 0,  atk: 6,  def: 2 }
  },
  GUARDIAN: {
    WEAPON:    { hp: 0,  atk: 4,  def: 4 },
    ARMOR:     { hp: 15, atk: 0,  def: 8 },
    ACCESSORY: { hp: 8,  atk: 0,  def: 5 }
  },
  VITALITY: {
    WEAPON:    { hp: 8,  atk: 3,  def: 0 },
    ARMOR:     { hp: 20, atk: 0,  def: 4 },
    ACCESSORY: { hp: 15, atk: 0,  def: 2 }
  },
  BALANCED: {
    WEAPON:    { hp: 3,  atk: 5,  def: 3 },
    ARMOR:     { hp: 10, atk: 2,  def: 6 },
    ACCESSORY: { hp: 5,  atk: 3,  def: 4 }
  }
};

const SIM_SET_LABELS = {
  BERSERKER: "Berserker", GUARDIAN: "Guardiao",
  VITALITY: "Vitalidade", BALANCED: "Equilibrado"
};

const SIM_SLOT_LABELS = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessorio" };

const SIM_SET_NAMES = {
  BERSERKER: { WEAPON: "Garra Berserker", ARMOR: "Couraca Berserker", ACCESSORY: "Emblema Berserker" },
  GUARDIAN:  { WEAPON: "Lanca Guardia", ARMOR: "Armadura Guardia", ACCESSORY: "Medalha Guardia" },
  VITALITY:  { WEAPON: "Cetro da Vitalidade", ARMOR: "Vestes da Vitalidade", ACCESSORY: "Emblema da Vitalidade" },
  BALANCED:  { WEAPON: "Lamina do Equilibrio", ARMOR: "Armadura do Equilibrio", ACCESSORY: "Simbolo do Equilibrio" }
};

const SIM_SLOT_EMOJI = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };

function simCalcStats(set, slot, tier, rarity, refine) {
  const base = SIM_BASE_STATS[set][slot];
  const tierMult = SIM_TIER_MULTIPLIERS[tier - 1];
  const rarMult = SIM_RARITY_MULTIPLIERS[rarity];
  return {
    hp:  base.hp  > 0 ? Math.round(Math.round(base.hp * tierMult) * rarMult) + (refine * 2) : 0,
    atk: base.atk > 0 ? Math.round(Math.round(base.atk * tierMult) * rarMult) + (refine * 2) : 0,
    def: base.def > 0 ? Math.round(Math.round(base.def * tierMult) * rarMult) + (refine * 2) : 0
  };
}

function renderEquipmentSimulatorPage() {
  setPageHeader("Simulador de Equipamentos", "Configure os 3 slots e veja os stats totais");

  const app = document.getElementById("app");

  app.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6" id="sim-slots"></div>
    <div id="sim-totals"></div>
  `;

  const slotsContainer = document.getElementById("sim-slots");

  SIM_SLOTS.forEach(slot => {
    slotsContainer.innerHTML += `
      <div class="card" id="sim-slot-${slot}">
        <h3 class="text-lg font-bold mb-3">${SIM_SLOT_EMOJI[slot]} ${SIM_SLOT_LABELS[slot]}</h3>

        <div class="space-y-3">
          <div>
            <label class="text-xs text-slate-400">Set</label>
            <select id="sim-${slot}-set" onchange="simUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
              ${SIM_SETS.map(s => `<option value="${s}">${SIM_SET_LABELS[s]}</option>`).join("")}
            </select>
          </div>

          <div>
            <label class="text-xs text-slate-400">Tier</label>
            <select id="sim-${slot}-tier" onchange="simUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
              ${Array.from({length: SIM_MAX_TIER}, (_, i) => `<option value="${i+1}">T${i+1}</option>`).join("")}
            </select>
          </div>

          <div>
            <label class="text-xs text-slate-400">Raridade</label>
            <select id="sim-${slot}-rarity" onchange="simUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
              ${SIM_RARITIES.map(r => `<option value="${r}">${r}</option>`).join("")}
            </select>
          </div>

          <div>
            <label class="text-xs text-slate-400">Refinamento: <span id="sim-${slot}-ref-label">+0</span></label>
            <input type="range" id="sim-${slot}-refine" min="0" max="${SIM_MAX_REFINE}" value="0"
              oninput="simUpdate()" class="w-full accent-cyan-500" />
          </div>
        </div>

        <div id="sim-${slot}-stats" class="mt-4"></div>
      </div>
    `;
  });

  simUpdate();
}

function simUpdate() {
  let totalHp = 0, totalAtk = 0, totalDef = 0;
  const setCounts = {};

  SIM_SLOTS.forEach(slot => {
    const set = document.getElementById(`sim-${slot}-set`).value;
    setCounts[set] = (setCounts[set] || 0) + 1;
    const tier = parseInt(document.getElementById(`sim-${slot}-tier`).value);
    const rarity = document.getElementById(`sim-${slot}-rarity`).value;
    const refine = parseInt(document.getElementById(`sim-${slot}-refine`).value);

    document.getElementById(`sim-${slot}-ref-label`).textContent = `+${refine}`;

    const stats = simCalcStats(set, slot, tier, rarity, refine);
    totalHp += stats.hp;
    totalAtk += stats.atk;
    totalDef += stats.def;

    const name = SIM_SET_NAMES[set][slot];
    const refLabel = refine > 0 ? ` +${refine}` : "";
    const successRate = refine < SIM_MAX_REFINE ? SIM_REFINE_SUCCESS[refine] : null;
    const nextCost = refine < SIM_MAX_REFINE ? SIM_REFINE_COST(refine) : null;

    const base = SIM_BASE_STATS[set][slot];
    const tierMult = SIM_TIER_MULTIPLIERS[tier - 1];

    const statsDiv = document.getElementById(`sim-${slot}-stats`);
    statsDiv.innerHTML = `
      <div class="bg-slate-800 rounded-lg p-3">
        <p class="text-sm font-bold text-cyan-400 mb-2">${name} T${tier}${refLabel}</p>
        <div class="flex gap-1 mb-2 flex-wrap">
          <span class="text-xs px-2 py-0.5 rounded bg-slate-700 text-slate-300">${SIM_SET_LABELS[set]}</span>
          <span class="text-xs px-2 py-0.5 rounded ${simRarityClass(rarity)}">${rarity}</span>
        </div>
        <div class="grid grid-cols-3 gap-2 text-center text-sm mb-2">
          ${stats.hp > 0 ? `<div><span class="text-slate-400 text-xs">HP</span><br><span class="text-red-400 font-bold">${stats.hp}</span><br><span class="text-slate-500 text-xs">base ${Math.round(base.hp * tierMult)}</span></div>` : `<div class="text-slate-600 text-xs">-</div>`}
          ${stats.atk > 0 ? `<div><span class="text-slate-400 text-xs">ATK</span><br><span class="text-orange-400 font-bold">${stats.atk}</span><br><span class="text-slate-500 text-xs">base ${Math.round(base.atk * tierMult)}</span></div>` : `<div class="text-slate-600 text-xs">-</div>`}
          ${stats.def > 0 ? `<div><span class="text-slate-400 text-xs">DEF</span><br><span class="text-orange-400 font-bold">${stats.def}</span><br><span class="text-slate-500 text-xs">base ${Math.round(base.def * tierMult)}</span></div>` : `<div class="text-slate-600 text-xs">-</div>`}
        </div>
        ${refine < SIM_MAX_REFINE ? `
        <div class="border-t border-slate-700 pt-2 mt-2">
          <p class="text-xs text-slate-400">Proximo refinamento (+${refine + 1}): <span class="${successRate >= 70 ? 'text-green-400' : successRate >= 40 ? 'text-yellow-400' : 'text-red-400'} font-bold">${successRate}%</span> de chance</p>
          <p class="text-xs text-slate-400">Custo: <span class="text-yellow-400">${nextCost} Bits</span> + <span class="text-purple-400">1 Pedra</span></p>
        </div>
        ` : `<p class="text-xs text-green-400 mt-2">Refinamento maximo!</p>`}
      </div>
    `;
  });

  // Compute set bonus
  let dominantSet = null;
  let dominantCount = 0;
  for (const [s, c] of Object.entries(setCounts)) {
    if (c >= 2 && c > dominantCount) { dominantSet = s; dominantCount = c; }
  }

  let setBonusHp = 0, setBonusAtk = 0, setBonusDef = 0;
  let setBonusLabel = "";
  if (dominantSet && dominantCount >= 2) {
    const tier = dominantCount >= 3 ? 3 : 2;
    const b = SIM_SET_BONUSES[dominantSet][tier];
    setBonusHp = Math.round(totalHp * b[0] / 100);
    setBonusAtk = Math.round(totalAtk * b[1] / 100);
    setBonusDef = Math.round(totalDef * b[2] / 100);
    const parts = [];
    if (b[0] > 0) parts.push(`HP +${b[0]}%`);
    if (b[1] > 0) parts.push(`ATK +${b[1]}%`);
    if (b[2] > 0) parts.push(`DEF +${b[2]}%`);
    setBonusLabel = `Set ${SIM_SET_LABELS[dominantSet]} (${dominantCount}/3): ${parts.join(", ")}`;
  }

  const finalHp = totalHp + setBonusHp;
  const finalAtk = totalAtk + setBonusAtk;
  const finalDef = totalDef + setBonusDef;
  const finalTotal = finalHp + finalAtk + finalDef;

  const totalsDiv = document.getElementById("sim-totals");

  totalsDiv.innerHTML = `
    <div class="card">
      <h3 class="text-lg font-bold mb-4 text-center">Bonus Total de Equipamentos</h3>

      ${setBonusLabel ? `
      <div class="mb-4 p-3 rounded-lg bg-yellow-900/30 border border-yellow-700 text-center">
        <p class="text-sm font-bold text-yellow-400">${setBonusLabel}</p>
        <p class="text-xs text-slate-400 mt-1">
          ${setBonusHp > 0 ? `<span class="text-red-400">HP +${setBonusHp}</span> ` : ''}
          ${setBonusAtk > 0 ? `<span class="text-orange-400">ATK +${setBonusAtk}</span> ` : ''}
          ${setBonusDef > 0 ? `<span class="text-blue-400">DEF +${setBonusDef}</span> ` : ''}
        </p>
      </div>
      ` : `
      <div class="mb-4 p-3 rounded-lg bg-slate-800 text-center">
        <p class="text-xs text-slate-500">Equipe 2+ pecas do mesmo set para ativar bonus</p>
      </div>
      `}

      <div class="grid grid-cols-4 gap-4 text-center">
        <div class="bg-slate-800 rounded-lg p-4">
          <p class="text-xs text-slate-400 mb-1">HP</p>
          <p class="text-2xl font-bold text-red-400">+${finalHp}</p>
          ${setBonusHp > 0 ? `<p class="text-xs text-yellow-400">(+${setBonusHp} set)</p>` : ''}
        </div>
        <div class="bg-slate-800 rounded-lg p-4">
          <p class="text-xs text-slate-400 mb-1">ATK</p>
          <p class="text-2xl font-bold text-orange-400">+${finalAtk}</p>
          ${setBonusAtk > 0 ? `<p class="text-xs text-yellow-400">(+${setBonusAtk} set)</p>` : ''}
        </div>
        <div class="bg-slate-800 rounded-lg p-4">
          <p class="text-xs text-slate-400 mb-1">DEF</p>
          <p class="text-2xl font-bold text-blue-400">+${finalDef}</p>
          ${setBonusDef > 0 ? `<p class="text-xs text-yellow-400">(+${setBonusDef} set)</p>` : ''}
        </div>
        <div class="bg-slate-800 rounded-lg p-4">
          <p class="text-xs text-slate-400 mb-1">TOTAL</p>
          <p class="text-2xl font-bold text-cyan-400">+${finalTotal}</p>
        </div>
      </div>

      <div class="mt-4">
        <h4 class="text-sm font-bold text-slate-300 mb-2">Comparativo por Set (mesmo Tier/Raridade/Refinamento)</h4>
        <div id="sim-compare"></div>
      </div>
    </div>
  `;

  simRenderCompare();
}

function simRenderCompare() {
  const tier = parseInt(document.getElementById("sim-WEAPON-tier").value);
  const rarity = document.getElementById("sim-WEAPON-rarity").value;
  const refine = parseInt(document.getElementById("sim-WEAPON-refine").value);

  const container = document.getElementById("sim-compare");

  let html = `
    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead>
          <tr class="text-slate-400 border-b border-slate-700">
            <th class="text-left py-2 px-2">Set</th>
            <th class="text-center py-2 px-2">HP</th>
            <th class="text-center py-2 px-2">ATK</th>
            <th class="text-center py-2 px-2">DEF</th>
            <th class="text-center py-2 px-2">Total</th>
          </tr>
        </thead>
        <tbody>
  `;

  SIM_SETS.forEach(set => {
    let hp = 0, atk = 0, def = 0;
    SIM_SLOTS.forEach(slot => {
      const s = simCalcStats(set, slot, tier, rarity, refine);
      hp += s.hp; atk += s.atk; def += s.def;
    });
    // Apply 3-piece set bonus (full set in compare)
    const b3 = SIM_SET_BONUSES[set][3];
    hp += Math.round(hp * b3[0] / 100);
    atk += Math.round(atk * b3[1] / 100);
    def += Math.round(def * b3[2] / 100);
    const total = hp + atk + def;
    html += `
      <tr class="border-b border-slate-800">
        <td class="py-2 px-2 font-bold">${SIM_SET_LABELS[set]}</td>
        <td class="py-2 px-2 text-center text-red-400">${hp}</td>
        <td class="py-2 px-2 text-center text-orange-400">${atk}</td>
        <td class="py-2 px-2 text-center text-blue-400">${def}</td>
        <td class="py-2 px-2 text-center text-cyan-400 font-bold">${total}</td>
      </tr>
    `;
  });

  html += `</tbody></table></div>`;

  html += `<p class="text-xs text-slate-500 mt-2">* Comparativo usa T${tier}, ${rarity}, +${refine} da Arma para todos os slots (com bonus de 3 pecas)</p>`;

  container.innerHTML = html;
}

function simRarityClass(rarity) {
  const map = {
    COMMON: "bg-slate-600 text-slate-200",
    RARE: "bg-blue-900 text-blue-300",
    EPIC: "bg-purple-900 text-purple-300",
    LEGENDARY: "bg-yellow-900 text-yellow-300"
  };
  return map[rarity] || "bg-slate-600 text-slate-200";
}
