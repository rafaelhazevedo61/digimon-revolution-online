(() => {
  'use strict';

  const WEIGHTS = { HP: 0.30, ATK: 0.20, DEF: 0.20 };

  const RARITY = {
    COMMON: { mult: 1.00 },
    RARE: { mult: 1.10 },
    EPIC: { mult: 1.25 },
    LEGENDARY: { mult: 1.50 }
  };

  const STAGE = {
    BABY: { mult: 1.0 },
    BABY_II: { mult: 1.1 },
    ROOKIE: { mult: 1.2 },
    CHAMPION: { mult: 1.5 },
    ULTIMATE: { mult: 2.0 },
    MEGA: { mult: 2.8 }
  };

  const PERSONALITY = {
    DURABLE: { HP: 0.10, ATK: 0, DEF: 0, XP: 0 },
    FIGHTER: { HP: 0, ATK: 0.10, DEF: 0, XP: 0 },
    DEFENDER: { HP: 0, ATK: 0, DEF: 0.10, XP: 0 },
    BRAINY: { HP: 0, ATK: 0.05, DEF: 0, XP: 0.05 },
    NIMBLE: { HP: 0, ATK: 0.05, DEF: 0.05, XP: 0 },
    LIVELY: { HP: 0, ATK: 0, DEF: 0, XP: 0.10 }
  };

  const TRAIT = {
    NONE: {},
    FAST_LEARNER: { XP: 0.10 },
    ENERGETIC: { energy: 5 },
    VITALITY: { HP: 0.10 },
    BERSERKER: { ATK: 0.10 },
    IRON_BODY: { DEF: 0.10 }
  };

  const LEVEL_BONUS = { HP: 2, ATK: 1, DEF: 1 };

  const container = document.querySelector('[data-wiki-stats-calc]');
  if (!container) return;

  const inputs = {
    stat: container.querySelector('[data-calc-stat]'),
    base: container.querySelector('[data-calc-base]'),
    iv: container.querySelector('[data-calc-iv]'),
    level: container.querySelector('[data-calc-level]'),
    rarity: container.querySelector('[data-calc-rarity]'),
    stage: container.querySelector('[data-calc-stage]'),
    personality: container.querySelector('[data-calc-personality]'),
    trait: container.querySelector('[data-calc-trait]'),
    rebirth: container.querySelector('[data-calc-rebirth]'),
    equipment: container.querySelector('[data-calc-equipment]')
  };

  const resultValue = container.querySelector('[data-calc-result]');
  const resultDesc = container.querySelector('[data-calc-desc]');

  function getNumber(input, min, max) {
    const value = Number(input?.value);
    if (Number.isNaN(value)) return 0;
    if (min !== undefined && value < min) return min;
    if (max !== undefined && value > max) return max;
    return value;
  }

  function calculate() {
    const stat = inputs.stat?.value || 'HP';
    const base = getNumber(inputs.base, 0);
    const iv = getNumber(inputs.iv, 0, 100);
    const level = getNumber(inputs.level, 1, 100);
    const rarity = inputs.rarity?.value || 'COMMON';
    const stage = inputs.stage?.value || 'BABY';
    const personality = inputs.personality?.value || 'DURABLE';
    const trait = inputs.trait?.value || 'NONE';
    const rebirth = getNumber(inputs.rebirth, 0, 999);
    const equipment = getNumber(inputs.equipment, 0);

    const weight = WEIGHTS[stat] || 0;
    const ivValue = iv * weight;

    const rarityMult = RARITY[rarity]?.mult ?? 1;
    const stageMult = STAGE[stage]?.mult ?? 1;

    const pers = PERSONALITY[personality] || PERSONALITY.DURABLE;
    const personalityMult = 1 + (pers[stat] || 0);

    const tr = TRAIT[trait] || {};
    const traitMult = 1 + (tr[stat] || 0);

    const rebirthPercent = Math.min(rebirth * 2, 20);
    const rebirthMult = 1 + rebirthPercent / 100;

    const raw = (base + ivValue) * rarityMult * stageMult * personalityMult * traitMult * rebirthMult;
    const floored = Math.floor(raw);
    const levelBonus = Math.max(0, level - 1) * (LEVEL_BONUS[stat] || 0);
    const final = floored + levelBonus + equipment;

    const breakdown = [
      `Base: ${base}`,
      `IV × peso: ${ivValue.toFixed(2)}`,
      `Raridade: ×${rarityMult.toFixed(2)}`,
      `Estágio: ×${stageMult.toFixed(1)}`,
      `Personalidade: ×${personalityMult.toFixed(2)}`,
      `Trait: ×${traitMult.toFixed(2)}`,
      `Rebirth: +${rebirthPercent}%`,
      `Level: +${levelBonus}`,
      `Equipamento: +${equipment}`
    ].join(' · ');

    if (resultValue) resultValue.textContent = final.toLocaleString('pt-BR');
    if (resultDesc) resultDesc.textContent = breakdown;
  }

  Object.values(inputs).forEach((input) => {
    if (input) input.addEventListener('input', calculate);
  });

  calculate();
})();
