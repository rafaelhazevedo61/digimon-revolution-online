// ==================== DIGIMON SIMULATOR ====================

const DSIM_STAGES = [
  { value: "BABY", label: "Baby", mult: 1.0 },
  { value: "BABY_II", label: "Baby II", mult: 1.1 },
  { value: "ROOKIE", label: "Rookie", mult: 1.2 },
  { value: "CHAMPION", label: "Champion", mult: 1.5 },
  { value: "ULTIMATE", label: "Ultimate", mult: 2.0 },
  { value: "MEGA", label: "Mega", mult: 2.8 }
];

const DSIM_RARITIES = [
  { value: "COMMON", label: "Common", statMult: 1.0, minIv: 0, xpMult: 1.0 },
  { value: "RARE", label: "Rare", statMult: 1.1, minIv: 25, xpMult: 1.05 },
  { value: "EPIC", label: "Epic", statMult: 1.25, minIv: 50, xpMult: 1.1 },
  { value: "LEGENDARY", label: "Legendary", statMult: 1.5, minIv: 75, xpMult: 1.2 }
];

const DSIM_PERSONALITIES = [
  { value: "DURABLE", label: "Durable", hp: 1.10, atk: 1.0, def: 1.0, xp: 1.0 },
  { value: "LIVELY", label: "Lively", hp: 1.0, atk: 1.0, def: 1.0, xp: 1.10 },
  { value: "FIGHTER", label: "Fighter", hp: 1.0, atk: 1.10, def: 1.0, xp: 1.0 },
  { value: "DEFENDER", label: "Defender", hp: 1.0, atk: 1.0, def: 1.10, xp: 1.0 },
  { value: "BRAINY", label: "Brainy", hp: 1.0, atk: 1.05, def: 1.0, xp: 1.05 },
  { value: "NIMBLE", label: "Nimble", hp: 1.0, atk: 1.05, def: 1.05, xp: 1.0 }
];

const DSIM_TRAITS = [
  { value: "", label: "Nenhuma", hp: 1.0, atk: 1.0, def: 1.0, xp: 1.0, energy: 0 },
  { value: "FAST_LEARNER", label: "Fast Learner (+10% XP)", hp: 1.0, atk: 1.0, def: 1.0, xp: 1.10, energy: 0 },
  { value: "ENERGETIC", label: "Energetic (+5 energia)", hp: 1.0, atk: 1.0, def: 1.0, xp: 1.0, energy: 5 },
  { value: "VITALITY", label: "Vitality (+10% HP)", hp: 1.10, atk: 1.0, def: 1.0, xp: 1.0, energy: 0 },
  { value: "BERSERKER", label: "Berserker (+10% ATK)", hp: 1.0, atk: 1.10, def: 1.0, xp: 1.0, energy: 0 },
  { value: "IRON_BODY", label: "Iron Body (+10% DEF)", hp: 1.0, atk: 1.0, def: 1.10, xp: 1.0, energy: 0 }
];

const DSIM_IV_WEIGHT = { hp: 0.30, atk: 0.20, def: 0.20 };
const DSIM_LEVEL_UP_BONUS = { hp: 2, atk: 1, def: 1 };
const DSIM_MAX_LEVEL = 100;

let dsimDigimonInfos = [];

function dsimGetRarity(value) { return DSIM_RARITIES.find(r => r.value === value); }
function dsimGetPersonality(value) { return DSIM_PERSONALITIES.find(p => p.value === value); }
function dsimGetTrait(value) { return DSIM_TRAITS.find(t => t.value === value); }
function dsimGetStage(value) { return DSIM_STAGES.find(s => s.value === value); }

function dsimCalcGrade(ivHp, ivAtk, ivDef) {
  const perfect = (ivHp === 100 ? 1 : 0) + (ivAtk === 100 ? 1 : 0) + (ivDef === 100 ? 1 : 0);
  if (perfect === 3) return "SSS";
  if (perfect === 2) return "SS";
  if (perfect === 1) return "S";
  const avg = Math.floor((ivHp + ivAtk + ivDef) / 3);
  if (avg >= 85) return "A";
  if (avg >= 70) return "B";
  if (avg >= 55) return "C";
  if (avg >= 40) return "D";
  return "E";
}

function dsimGradeColor(grade) {
  const map = {
    SSS: "text-yellow-300", SS: "text-yellow-400", S: "text-orange-400",
    A: "text-purple-400", B: "text-blue-400", C: "text-green-400",
    D: "text-slate-300", E: "text-slate-500"
  };
  return map[grade] || "text-slate-300";
}

function dsimRarityBadge(rarity) {
  const map = {
    COMMON: "bg-slate-600 text-slate-200",
    RARE: "bg-blue-900 text-blue-300",
    EPIC: "bg-purple-900 text-purple-300",
    LEGENDARY: "bg-yellow-900 text-yellow-300"
  };
  return map[rarity] || "bg-slate-600 text-slate-200";
}

function dsimCalcStats(digimonInfo, rarity, personality, trait, stage, ivHp, ivAtk, ivDef, level) {
  if (!digimonInfo) return null;

  const rarData = dsimGetRarity(rarity);
  const persData = dsimGetPersonality(personality);
  const traitData = dsimGetTrait(trait);
  const stageData = dsimGetStage(stage);

  const hpMult = rarData.statMult * stageData.mult * persData.hp * traitData.hp;
  const atkMult = rarData.statMult * stageData.mult * persData.atk * traitData.atk;
  const defMult = rarData.statMult * stageData.mult * persData.def * traitData.def;

  const baseHp = Math.floor((digimonInfo.baseHp + (ivHp * DSIM_IV_WEIGHT.hp)) * hpMult);
  const baseAtk = Math.floor((digimonInfo.baseAtk + (ivAtk * DSIM_IV_WEIGHT.atk)) * atkMult);
  const baseDef = Math.floor((digimonInfo.baseDef + (ivDef * DSIM_IV_WEIGHT.def)) * defMult);

  const levelBonus = level - 1;
  const hp = baseHp + (levelBonus * DSIM_LEVEL_UP_BONUS.hp);
  const atk = baseAtk + (levelBonus * DSIM_LEVEL_UP_BONUS.atk);
  const def = baseDef + (levelBonus * DSIM_LEVEL_UP_BONUS.def);

  const grade = dsimCalcGrade(ivHp, ivAtk, ivDef);
  const maxEnergy = 20 + traitData.energy;

  const xpMult = rarData.xpMult * persData.xp * traitData.xp;

  return { hp, atk, def, baseHp, baseAtk, baseDef, grade, maxEnergy, xpMult, hpMult, atkMult, defMult };
}

async function renderDigimonSimulatorPage() {
  setPageHeader("Simulador de Digimon", "Simule a criacao de Digimons com diferentes configuracoes");

  const app = document.getElementById("app");
  app.innerHTML = `<div class="card"><p class="text-slate-400">Carregando Digimon Infos...</p></div>`;

  try {
    const result = await apiGet("/digimon-infos", { page: 0, size: 500 });
    dsimDigimonInfos = result.items || [];
  } catch (e) {
    app.innerHTML = `<div class="card border-red-900 bg-red-950/30">
      <p class="text-red-300 font-bold">Erro ao carregar Digimon Infos</p>
      <p class="text-red-200 text-sm">${e.message}</p>
    </div>`;
    return;
  }

  app.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="space-y-4">
        <div class="card">
          <h3 class="text-lg font-bold mb-4 text-cyan-400">Configuracao do Digimon</h3>

          <div class="space-y-4">
            <div>
              <label class="text-xs text-slate-400">Digimon (Especie)</label>
              <select id="dsim-digimon" onchange="dsimUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
                <option value="">-- Selecione --</option>
                ${dsimDigimonInfos.map(d => `<option value="${d.id}">${d.name} (${d.stage} | HP:${d.baseHp} ATK:${d.baseAtk} DEF:${d.baseDef})</option>`).join("")}
              </select>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-xs text-slate-400">Raridade</label>
                <select id="dsim-rarity" onchange="dsimUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
                  ${DSIM_RARITIES.map(r => `<option value="${r.value}">${r.label} (x${r.statMult})</option>`).join("")}
                </select>
              </div>
              <div>
                <label class="text-xs text-slate-400">Stage</label>
                <select id="dsim-stage" onchange="dsimUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
                  ${DSIM_STAGES.map(s => `<option value="${s.value}">${s.label} (x${s.mult})</option>`).join("")}
                </select>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-xs text-slate-400">Personalidade</label>
                <select id="dsim-personality" onchange="dsimUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
                  ${DSIM_PERSONALITIES.map(p => `<option value="${p.value}">${p.label}</option>`).join("")}
                </select>
              </div>
              <div>
                <label class="text-xs text-slate-400">Trait</label>
                <select id="dsim-trait" onchange="dsimUpdate()" class="w-full bg-slate-800 border border-slate-700 rounded px-3 py-2 text-sm">
                  ${DSIM_TRAITS.map(t => `<option value="${t.value}">${t.label}</option>`).join("")}
                </select>
              </div>
            </div>

            <div>
              <label class="text-xs text-slate-400">Level: <span id="dsim-level-label">1</span></label>
              <input type="range" id="dsim-level" min="1" max="${DSIM_MAX_LEVEL}" value="1"
                oninput="dsimUpdate()" class="w-full accent-cyan-500" />
            </div>

            <div class="border-t border-slate-700 pt-4">
              <h4 class="text-sm font-bold text-slate-300 mb-3">IVs (Valores Individuais)</h4>
              <div class="space-y-3">
                <div>
                  <label class="text-xs text-slate-400">IV HP: <span id="dsim-iv-hp-label" class="text-red-400 font-bold">50</span></label>
                  <input type="range" id="dsim-iv-hp" min="0" max="100" value="50"
                    oninput="dsimUpdate()" class="w-full accent-red-500" />
                </div>
                <div>
                  <label class="text-xs text-slate-400">IV ATK: <span id="dsim-iv-atk-label" class="text-orange-400 font-bold">50</span></label>
                  <input type="range" id="dsim-iv-atk" min="0" max="100" value="50"
                    oninput="dsimUpdate()" class="w-full accent-orange-500" />
                </div>
                <div>
                  <label class="text-xs text-slate-400">IV DEF: <span id="dsim-iv-def-label" class="text-blue-400 font-bold">50</span></label>
                  <input type="range" id="dsim-iv-def" min="0" max="100" value="50"
                    oninput="dsimUpdate()" class="w-full accent-blue-500" />
                </div>
              </div>
              <div class="flex gap-2 mt-3">
                <button onclick="dsimSetIvs(0)" class="text-xs px-3 py-1 rounded bg-slate-700 hover:bg-slate-600 text-slate-300">Min (0)</button>
                <button onclick="dsimSetIvs(50)" class="text-xs px-3 py-1 rounded bg-slate-700 hover:bg-slate-600 text-slate-300">Mid (50)</button>
                <button onclick="dsimSetIvs(100)" class="text-xs px-3 py-1 rounded bg-slate-700 hover:bg-slate-600 text-slate-300">Max (100)</button>
                <button onclick="dsimRandomIvs()" class="text-xs px-3 py-1 rounded bg-cyan-800 hover:bg-cyan-700 text-cyan-200">Aleatorio</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <div id="dsim-result" class="card">
          <p class="text-slate-500 text-center">Selecione um Digimon para ver o resultado</p>
        </div>
        <div id="dsim-compare"></div>
      </div>
    </div>
  `;

  dsimUpdate();
}

function dsimSetIvs(val) {
  document.getElementById("dsim-iv-hp").value = val;
  document.getElementById("dsim-iv-atk").value = val;
  document.getElementById("dsim-iv-def").value = val;
  dsimUpdate();
}

function dsimRandomIvs() {
  const rarity = document.getElementById("dsim-rarity").value;
  const minIv = dsimGetRarity(rarity).minIv;
  document.getElementById("dsim-iv-hp").value = minIv + Math.floor(Math.random() * (101 - minIv));
  document.getElementById("dsim-iv-atk").value = minIv + Math.floor(Math.random() * (101 - minIv));
  document.getElementById("dsim-iv-def").value = minIv + Math.floor(Math.random() * (101 - minIv));
  dsimUpdate();
}

function dsimUpdate() {
  const digimonId = document.getElementById("dsim-digimon").value;
  const rarity = document.getElementById("dsim-rarity").value;
  const stage = document.getElementById("dsim-stage").value;
  const personality = document.getElementById("dsim-personality").value;
  const trait = document.getElementById("dsim-trait").value;
  const level = parseInt(document.getElementById("dsim-level").value);
  const ivHp = parseInt(document.getElementById("dsim-iv-hp").value);
  const ivAtk = parseInt(document.getElementById("dsim-iv-atk").value);
  const ivDef = parseInt(document.getElementById("dsim-iv-def").value);

  document.getElementById("dsim-level-label").textContent = level;
  document.getElementById("dsim-iv-hp-label").textContent = ivHp;
  document.getElementById("dsim-iv-atk-label").textContent = ivAtk;
  document.getElementById("dsim-iv-def-label").textContent = ivDef;

  const resultDiv = document.getElementById("dsim-result");
  const compareDiv = document.getElementById("dsim-compare");

  if (!digimonId) {
    resultDiv.innerHTML = `<p class="text-slate-500 text-center">Selecione um Digimon para ver o resultado</p>`;
    compareDiv.innerHTML = "";
    return;
  }

  const digimonInfo = dsimDigimonInfos.find(d => String(d.id) === digimonId);
  if (!digimonInfo) return;

  const stats = dsimCalcStats(digimonInfo, rarity, personality, trait, stage, ivHp, ivAtk, ivDef, level);
  const grade = stats.grade;
  const total = stats.hp + stats.atk + stats.def;

  const rarData = dsimGetRarity(rarity);

  resultDiv.innerHTML = `
    <div class="space-y-4">
      <div class="text-center">
        <h3 class="text-xl font-bold text-cyan-400">${digimonInfo.name}</h3>
        <div class="flex items-center justify-center gap-2 mt-2 flex-wrap">
          <span class="text-xs px-2 py-0.5 rounded bg-slate-700">${dsimGetStage(stage).label}</span>
          <span class="text-xs px-2 py-0.5 rounded ${dsimRarityBadge(rarity)}">${rarity}</span>
          <span class="text-sm font-bold ${dsimGradeColor(grade)}">Grade ${grade}</span>
        </div>
        <p class="text-xs text-slate-500 mt-1">Level ${level} | Energia Max: ${stats.maxEnergy}</p>
      </div>

      <div class="grid grid-cols-3 gap-3 text-center">
        <div class="bg-slate-800 rounded-lg p-3">
          <p class="text-xs text-slate-400">HP</p>
          <p class="text-2xl font-bold text-red-400">${stats.hp}</p>
          <p class="text-xs text-slate-500">base ${stats.baseHp} + ${(level - 1) * DSIM_LEVEL_UP_BONUS.hp} lvl</p>
        </div>
        <div class="bg-slate-800 rounded-lg p-3">
          <p class="text-xs text-slate-400">ATK</p>
          <p class="text-2xl font-bold text-orange-400">${stats.atk}</p>
          <p class="text-xs text-slate-500">base ${stats.baseAtk} + ${(level - 1) * DSIM_LEVEL_UP_BONUS.atk} lvl</p>
        </div>
        <div class="bg-slate-800 rounded-lg p-3">
          <p class="text-xs text-slate-400">DEF</p>
          <p class="text-2xl font-bold text-blue-400">${stats.def}</p>
          <p class="text-xs text-slate-500">base ${stats.baseDef} + ${(level - 1) * DSIM_LEVEL_UP_BONUS.def} lvl</p>
        </div>
      </div>

      <div class="bg-slate-800 rounded-lg p-3 text-center">
        <p class="text-xs text-slate-400">Poder Total</p>
        <p class="text-3xl font-bold text-cyan-400">${total}</p>
        <p class="text-xs text-slate-500">HP*0.30 + ATK*1.50 + DEF*1.00 = <span class="text-cyan-300 font-bold">${Math.floor(stats.hp * 0.30 + stats.atk * 1.50 + stats.def * 1.00)}</span> (combat power)</p>
      </div>

      <div class="border-t border-slate-700 pt-3">
        <h4 class="text-sm font-bold text-slate-300 mb-2">Detalhes do Calculo</h4>
        <div class="text-xs text-slate-400 space-y-1 font-mono bg-slate-800 rounded-lg p-3">
          <p>Base Stats: HP=${digimonInfo.baseHp} ATK=${digimonInfo.baseAtk} DEF=${digimonInfo.baseDef}</p>
          <p>IVs: HP=${ivHp} (peso x${DSIM_IV_WEIGHT.hp}) ATK=${ivAtk} (peso x${DSIM_IV_WEIGHT.atk}) DEF=${ivDef} (peso x${DSIM_IV_WEIGHT.def})</p>
          <p>IV Media: ${Math.floor((ivHp + ivAtk + ivDef) / 3)} | Perfeitos: ${(ivHp === 100 ? 1 : 0) + (ivAtk === 100 ? 1 : 0) + (ivDef === 100 ? 1 : 0)}/3</p>
          <p class="border-t border-slate-700 pt-1">Mult Raridade: x${rarData.statMult} | Mult Stage: x${dsimGetStage(stage).mult}</p>
          <p>Mult Personalidade: HP x${dsimGetPersonality(personality).hp} ATK x${dsimGetPersonality(personality).atk} DEF x${dsimGetPersonality(personality).def}</p>
          <p>Mult Trait: HP x${dsimGetTrait(trait).hp} ATK x${dsimGetTrait(trait).atk} DEF x${dsimGetTrait(trait).def}</p>
          <p class="border-t border-slate-700 pt-1">Mult Final: HP x${stats.hpMult.toFixed(4)} ATK x${stats.atkMult.toFixed(4)} DEF x${stats.defMult.toFixed(4)}</p>
          <p>Mult XP: x${stats.xpMult.toFixed(4)}</p>
          <p class="border-t border-slate-700 pt-1">
            HP = floor((${digimonInfo.baseHp} + ${ivHp}*${DSIM_IV_WEIGHT.hp}) * ${stats.hpMult.toFixed(4)}) + ${(level - 1) * DSIM_LEVEL_UP_BONUS.hp} = <span class="text-red-400">${stats.hp}</span>
          </p>
          <p>
            ATK = floor((${digimonInfo.baseAtk} + ${ivAtk}*${DSIM_IV_WEIGHT.atk}) * ${stats.atkMult.toFixed(4)}) + ${(level - 1) * DSIM_LEVEL_UP_BONUS.atk} = <span class="text-orange-400">${stats.atk}</span>
          </p>
          <p>
            DEF = floor((${digimonInfo.baseDef} + ${ivDef}*${DSIM_IV_WEIGHT.def}) * ${stats.defMult.toFixed(4)}) + ${(level - 1) * DSIM_LEVEL_UP_BONUS.def} = <span class="text-blue-400">${stats.def}</span>
          </p>
        </div>
      </div>

      <div class="border-t border-slate-700 pt-3">
        <h4 class="text-sm font-bold text-slate-300 mb-2">IV Minimo por Raridade</h4>
        <div class="grid grid-cols-4 gap-2 text-center text-xs">
          ${DSIM_RARITIES.map(r => `
            <div class="rounded p-2 ${r.value === rarity ? 'bg-cyan-900/40 border border-cyan-700' : 'bg-slate-800'}">
              <p class="${dsimRarityBadge(r.value)} px-1 py-0.5 rounded inline-block text-xs">${r.label}</p>
              <p class="mt-1">Min IV: <span class="font-bold">${r.minIv}</span></p>
              <p>Stats: x${r.statMult}</p>
            </div>
          `).join("")}
        </div>
      </div>
    </div>
  `;

  dsimRenderCompare(digimonInfo, personality, trait, stage, ivHp, ivAtk, ivDef, level);
}

function dsimRenderCompare(digimonInfo, personality, trait, stage, ivHp, ivAtk, ivDef, level) {
  const compareDiv = document.getElementById("dsim-compare");

  let html = `
    <div class="card">
      <h3 class="text-lg font-bold mb-3 text-center">Comparativo por Raridade</h3>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-slate-400 border-b border-slate-700">
              <th class="text-left py-2 px-2">Raridade</th>
              <th class="text-center py-2 px-2">HP</th>
              <th class="text-center py-2 px-2">ATK</th>
              <th class="text-center py-2 px-2">DEF</th>
              <th class="text-center py-2 px-2">Total</th>
              <th class="text-center py-2 px-2">Combat</th>
            </tr>
          </thead>
          <tbody>
  `;

  DSIM_RARITIES.forEach(r => {
    const s = dsimCalcStats(digimonInfo, r.value, personality, trait, stage, ivHp, ivAtk, ivDef, level);
    const total = s.hp + s.atk + s.def;
    const combat = Math.floor(s.hp * 0.30 + s.atk * 1.50 + s.def * 1.00);
    html += `
      <tr class="border-b border-slate-800">
        <td class="py-2 px-2"><span class="${dsimRarityBadge(r.value)} px-2 py-0.5 rounded text-xs">${r.label}</span></td>
        <td class="py-2 px-2 text-center text-red-400">${s.hp}</td>
        <td class="py-2 px-2 text-center text-orange-400">${s.atk}</td>
        <td class="py-2 px-2 text-center text-blue-400">${s.def}</td>
        <td class="py-2 px-2 text-center font-bold">${total}</td>
        <td class="py-2 px-2 text-center text-cyan-400 font-bold">${combat}</td>
      </tr>
    `;
  });

  html += `</tbody></table></div>`;

  html += `
      <h4 class="text-sm font-bold text-slate-300 mt-4 mb-3 text-center">Comparativo por Stage</h4>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="text-slate-400 border-b border-slate-700">
              <th class="text-left py-2 px-2">Stage</th>
              <th class="text-center py-2 px-2">HP</th>
              <th class="text-center py-2 px-2">ATK</th>
              <th class="text-center py-2 px-2">DEF</th>
              <th class="text-center py-2 px-2">Total</th>
              <th class="text-center py-2 px-2">Combat</th>
            </tr>
          </thead>
          <tbody>
  `;

  const currentRarity = document.getElementById("dsim-rarity").value;
  DSIM_STAGES.forEach(st => {
    const s = dsimCalcStats(digimonInfo, currentRarity, personality, trait, st.value, ivHp, ivAtk, ivDef, level);
    const total = s.hp + s.atk + s.def;
    const combat = Math.floor(s.hp * 0.30 + s.atk * 1.50 + s.def * 1.00);
    const isActive = st.value === stage;
    html += `
      <tr class="border-b border-slate-800 ${isActive ? 'bg-cyan-900/20' : ''}">
        <td class="py-2 px-2 font-bold ${isActive ? 'text-cyan-400' : ''}">${st.label} (x${st.mult})</td>
        <td class="py-2 px-2 text-center text-red-400">${s.hp}</td>
        <td class="py-2 px-2 text-center text-orange-400">${s.atk}</td>
        <td class="py-2 px-2 text-center text-blue-400">${s.def}</td>
        <td class="py-2 px-2 text-center font-bold">${total}</td>
        <td class="py-2 px-2 text-center text-cyan-400 font-bold">${combat}</td>
      </tr>
    `;
  });

  html += `</tbody></table></div>
    <p class="text-xs text-slate-500 mt-2 text-center">* Comparativos usam IVs, personalidade, trait e level atuais</p>
    </div>
  `;

  compareDiv.innerHTML = html;
}
