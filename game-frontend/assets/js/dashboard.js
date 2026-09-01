let dashEquippedItems = [];

async function renderDashboardPage() {
  const app = document.getElementById("app");
  showBottomNav("dashboard");

  app.innerHTML = `
    <div class="page-container dashboard-page">
      <div id="dash-content">
        <div class="card animate-pulse mb-4"><div class="h-40"></div></div>
      </div>
    </div>
  `;

  try {
    const data = await apiGet("/players/me/dashboard");
    renderDashContent(data);
  } catch (err) {
    document.getElementById("dash-content").innerHTML = `
      <div class="card border-red-900">
        <p class="text-red-300">${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function renderDashContent(data) {
  const container = document.getElementById("dash-content");
  const d = data.activeDigimon;
  dashEquippedItems = data.equippedItems || [];

  container.innerHTML = `
    <!-- Player header -->
    <header class="dashboard-header mb-4">
      <div>
        <p class="dashboard-eyebrow">Painel do treinador</p>
        <h2 class="dashboard-title">${escapeHtml(data.username)}</h2>
        <p class="dashboard-subtitle">Tamer · visão geral da sua jornada</p>
      </div>
      <button class="dashboard-logout" onclick="authLogout()" aria-label="Sair da conta" title="Sair">
        <span aria-hidden="true">↗</span>
        <span>Sair</span>
      </button>
    </header>

    <div class="dashboard-notices">
      <div id="dash-mail-notice"></div>
      <div id="dash-weekend-double-reward"></div>
      <div id="tutorial-card"></div>
    </div>

    <div class="dashboard-content-grid">
      <main class="dashboard-primary-column">
        ${d ? renderDigimonCard(d) : `
          <div class="card text-center mb-4">
            <p class="text-slate-400">Nenhum Digimon ativo</p>
          </div>
        `}

        <!-- Resources -->
        <div class="dashboard-resource-grid ${d ? "" : "dashboard-resource-grid-single"} mb-4">
          ${d ? `
          <div class="dashboard-resource-card dashboard-resource-bits">
            <span class="dashboard-resource-icon" aria-hidden="true">◈</span>
            <div><p class="dashboard-resource-label">Bits</p><p class="dashboard-resource-value">${Number(d.bits || 0).toLocaleString("pt-BR")}</p></div>
          </div>
          <div class="dashboard-resource-card dashboard-resource-energy">
            <span class="dashboard-resource-icon" aria-hidden="true">ϟ</span>
            <div><p class="dashboard-resource-label">Energia</p><p class="dashboard-resource-value">${d.energy}/${d.maxEnergy}${d.clanBonusMaxEnergy ? `<span class="dashboard-resource-bonus">+${d.clanBonusMaxEnergy}</span>` : ""}</p></div>
          </div>
          ` : ""}
          <div class="dashboard-resource-card dashboard-resource-data">
            <span class="dashboard-resource-icon" aria-hidden="true">⌁</span>
            <div><p class="dashboard-resource-label">Dados digitais</p><p class="dashboard-resource-value">${Number(data.digitalData || 0).toLocaleString("pt-BR")}</p></div>
          </div>
        </div>

        <!-- Equipped items -->
        ${d ? `
        <section class="dashboard-section mb-4">
          <div class="dashboard-section-heading">
            <div><p class="dashboard-eyebrow">Loadout ativo</p><h3 class="dashboard-section-title">Equipamentos</h3></div>
            <span class="dashboard-section-count">${(data.equippedItems || []).length}/3</span>
          </div>
          <p class="dashboard-section-note">Toque em um item para ver atributos, ascensão e alternativas.</p>
          <div class="dashboard-equipment-grid">
            ${renderEquipSlots(data.equippedItems || [])}
          </div>
          ${renderSetBonus(data.setBonus)}
        </section>
        ` : ""}
      </main>

      <aside class="dashboard-secondary-column">
        <!-- Actions -->
        ${d ? `
        <section class="dashboard-actions mb-4" aria-label="Ações rápidas">
          <div class="dashboard-sidebar-heading"><div><p class="dashboard-eyebrow dashboard-eyebrow-amber">Atalhos do treinador</p><h3 class="dashboard-section-title">Acesso rápido</h3></div><span class="dashboard-sidebar-mark">↗</span></div>
          <button class="dashboard-action dashboard-action-primary" onclick="navigateTo('evolution')"><span class="dashboard-action-icon">ϟ</span><span><strong>Evoluir</strong><small>Fortaleça seu Digimon</small></span><span class="dashboard-action-arrow">›</span></button>
          <button class="dashboard-action dashboard-action-amber" onclick="navigateTo('rebirth')"><span class="dashboard-action-icon">↻</span><span><strong>Renascer</strong><small>Recomece mais forte</small></span><span class="dashboard-action-arrow">›</span></button>
          <button class="dashboard-action dashboard-action-cyan" onclick="navigateTo('storage')"><span class="dashboard-action-icon">▣</span><span><strong>Armazém</strong><small>Gerencie seus itens</small></span><span class="dashboard-action-arrow">›</span></button>
        </section>
        ` : ""}

        <!-- Active missions -->
        ${data.activeMissions && data.activeMissions.length > 0 ? `
        <section class="dashboard-missions-section mb-4">
          <div class="dashboard-section-heading">
            <div><p class="dashboard-eyebrow dashboard-eyebrow-blue">Atividade em campo</p><h3 class="dashboard-section-title">Missões ativas</h3></div>
            <span class="dashboard-section-count dashboard-section-count-blue">${data.activeMissions.length}</span>
          </div>
          <p class="dashboard-section-note">Acompanhe seus objetivos e resgate suas recompensas.</p>
          <div class="dashboard-missions-list">
            ${data.activeMissions.map(renderActiveMission).join("")}
          </div>
        </section>
        ` : ""}

        <!-- Incubation -->
        ${data.incubation ? renderIncubation(data.incubation) : ""}
      </aside>
    </div>
  `;

  container.querySelectorAll("[data-rename-digimon-id]").forEach(button => {
    button.addEventListener("click", event => {
      event.stopPropagation();
      openRenameModal(button.dataset.renameDigimonId, button.dataset.renameDigimonName);
    });
  });
  startMissionTimers();
  startIncubationTimer(data.incubation);
  loadTutorialCard();
  loadDashboardMailNotice();
  loadDashboardWeekendDoubleReward();
}

async function loadDashboardWeekendDoubleReward() {
  const banner = document.getElementById("dash-weekend-double-reward");
  if (!banner) return;

  try {
    const result = await apiGet("/events/weekend-double-reward");
    if (!result || !result.active) {
      banner.innerHTML = "";
      return;
    }

    banner.innerHTML = `
      <div
        class="card-sm w-full mb-4 text-left border-amber-400/80 bg-gradient-to-r from-amber-950/90 via-yellow-950/70 to-amber-900/50 shadow-lg shadow-amber-950/40"
        role="status"
        aria-label="Evento de Double XP e Double Bits ativo"
      >
        <div class="flex items-center justify-between gap-3">
          <div class="min-w-0">
            <p class="text-[0.65rem] uppercase tracking-[0.18em] font-bold text-amber-300">Evento ativo</p>
            <p class="font-bold text-sm text-yellow-100 mt-1">Dobro de XP &amp; Bits</p>
            <p class="text-xs text-amber-200/80 mt-1">Apenas XP e Bits recebem bônus neste fim de semana</p>
          </div>
          <span class="shrink-0 rounded-lg border border-yellow-300/60 bg-yellow-400/20 px-2 py-1 text-lg font-black text-yellow-200">2×</span>
        </div>
      </div>
    `;
  } catch (err) {
    banner.innerHTML = "";
  }
}

async function loadDashboardMailNotice() {
  const notice = document.getElementById("dash-mail-notice");
  if (!notice) return;

  try {
    const result = await apiGet("/mail/unread-count");
    const count = Number(result?.count || 0);
    if (count <= 0) {
      notice.innerHTML = "";
      return;
    }

    const label = count === 1 ? "mensagem não lida" : "mensagens não lidas";
    const badge = count > 99 ? "99+" : String(count);
    notice.innerHTML = `
      <button class="card-sm w-full mb-4 text-left border-cyan-700 bg-cyan-950/30 hover:bg-cyan-950/50" onclick="navigateTo('mail')" aria-label="Abrir Correio com ${count} ${label}">
        <div class="flex items-center justify-between gap-3">
          <div class="min-w-0">
            <p class="text-xs uppercase tracking-wider text-cyan-300">Correio</p>
            <p class="font-bold text-sm text-slate-100 mt-1">Você tem ${count} ${label}</p>
            <p class="text-xs text-slate-400 mt-1">Toque aqui para abrir sua Entrada.</p>
          </div>
          <span class="badge text-cyan-200 shrink-0">${badge}</span>
        </div>
      </button>
    `;
  } catch (err) {
    notice.innerHTML = "";
  }
}

function renderDigimonCard(d) {
  const rarityColors = {
    COMMON: "border-slate-600",
    RARE: "border-blue-500",
    EPIC: "border-purple-500",
    LEGENDARY: "border-yellow-500"
  };
  const borderClass = rarityColors[d.rarity] || "border-slate-700";

  const xpNeeded = getXpForLevel(d.level);
  const xpPercent = d.level >= 100 ? 100 : Math.min(100, Math.round((d.experience / xpNeeded) * 100));

  return `
    <div class="card ${borderClass} dashboard-digimon-card mb-4">
      <div class="dashboard-digimon-grid">
        <div class="dashboard-digimon-main">
          <div class="dashboard-digimon-identity flex items-center gap-3">
            ${renderDigimonVisual(d.imageUrl, d.stage, "w-16 h-16", "text-5xl")}
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mt-1">
                <h3 class="font-bold text-lg truncate">${escapeHtml(d.name)}</h3>
                <button class="text-slate-500 hover:text-slate-300 text-xs" data-rename-digimon-id="${escapeAttr(d.id)}" data-rename-digimon-name="${escapeAttr(d.name)}" title="Renomear">✏️</button>
                <span class="text-sm font-bold text-cyan-400">Lv.${d.level}</span>
              </div>
              <div class="flex gap-2 mt-1 flex-wrap">
                <span class="badge dashboard-stage-badge badge-${d.stage.toLowerCase()}">${escapeHtml(formatStage(d.stage))}</span>
                ${renderRarityDieIndicator(d)}
                ${d.attribute ? `<span class="badge dashboard-attribute-badge badge-common">${escapeHtml(formatAttribute(d.attribute))}</span>` : ""}
                ${d.element ? `<span class="badge dashboard-element-badge badge-common">${escapeHtml(formatElement(d.element))}</span>` : ""}
              </div>
            </div>
          </div>

          <!-- XP bar -->
          <div class="dashboard-digimon-progress mt-4 mb-1">
            <div class="flex justify-between text-xs text-slate-500 mb-1">
              <span>XP para o próximo nível</span>
              <span>${d.experience} / ${xpNeeded}</span>
            </div>
            <div class="xp-bar xp-bar-with-label" role="progressbar" aria-valuenow="${xpPercent}" aria-valuemin="0" aria-valuemax="100" aria-label="${xpPercent}% da experiência para o próximo nível">
              <div class="xp-bar-fill" style="width: ${xpPercent}%"></div>
              <span class="xp-bar-label">${xpPercent}%</span>
                        </div>
          </div>
          <div class="dashboard-digimon-desktop-info" aria-label="Informações gerais do Digimon">
            <div class="dashboard-digimon-profile-strip">
              <div class="dashboard-digimon-profile-stat"><span>Estágio</span><strong>${escapeHtml(formatStage(d.stage))}</strong></div>
              <div class="dashboard-digimon-profile-stat"><span>Renascimentos</span><strong>${Number(d.rebirthCount || 0)}</strong></div>
              <div class="dashboard-digimon-profile-stat"><span>Personalidade</span><strong>${escapeHtml(formatPersonality(d.personality))}</strong><small>${escapeHtml(formatPersonalityEffect(d.personality))}</small></div>
              <div class="dashboard-digimon-profile-stat"><span>Especialidade</span><strong>${escapeHtml(formatTraitName(d.trait))}</strong><small>${escapeHtml(formatTraitEffect(d.trait))}</small></div>
              <div class="dashboard-digimon-profile-stat"><span>Atributo</span><strong>${d.attribute ? escapeHtml(formatAttribute(d.attribute)) : "—"}</strong></div>
              <div class="dashboard-digimon-profile-stat"><span>Elemento</span><strong>${d.element ? escapeHtml(formatElement(d.element)) : "—"}</strong></div>
            </div>
            <div class="dashboard-digimon-potential-panel">
              <div class="dashboard-digimon-potential-heading">
                <span>Potencial base</span>
                <div class="dashboard-digimon-potential-meta">
                  <strong>Média ${dashboardAverageIv(d)}%</strong>
                  <span class="dashboard-digimon-tier-indicator">Tier ${escapeHtml(d.grade || "—")}</span>
                </div>
              </div>
              <div class="dashboard-digimon-potential-grid">
                ${renderDashboardPotential("HP", d.ivHp, "dashboard-potential-hp")}
                ${renderDashboardPotential("ATK", d.ivAttack, "dashboard-potential-atk")}
                ${renderDashboardPotential("DEF", d.ivDefense, "dashboard-potential-def")}
              </div>
            </div>
          </div>
        </div>
        <!-- Effective stats -->
        <div class="dashboard-digimon-stats-panel">
          <div class="dashboard-digimon-stats-heading"><span class="dashboard-eyebrow dashboard-eyebrow-blue">Leitura de combate</span><span class="dashboard-sidebar-mark">◎</span></div>
          <div class="dashboard-digimon-stat-grid grid grid-cols-3 gap-2 text-center text-sm">
            ${renderDashboardStat("HP", d.hp, d.equipBonusHp, d.clanBonusHp, "text-red-400")}
            ${renderDashboardStat("ATK", d.attack, d.equipBonusAttack, d.clanBonusAttack, "text-orange-400")}
            ${renderDashboardStat("DEF", d.defense, d.equipBonusDefense, d.clanBonusDefense, "text-blue-400")}
          </div>
          <details open class="dashboard-digimon-details mt-3 rounded-lg border border-slate-800 bg-slate-950/40 px-3 py-2">
            <summary class="cursor-pointer select-none text-xs font-semibold text-slate-400 hover:text-slate-200">Ver detalhes dos atributos</summary>
            <div class="mt-3 grid grid-cols-1 gap-2">
              ${renderDashboardStatBreakdown("HP", d.hp, d.equipBonusHp, d.clanBonusHp, "text-red-400")}
              ${renderDashboardStatBreakdown("ATK", d.attack, d.equipBonusAttack, d.clanBonusAttack, "text-orange-400")}
              ${renderDashboardStatBreakdown("DEF", d.defense, d.equipBonusDefense, d.clanBonusDefense, "text-blue-400")}
            </div>
          </details>
        </div>
      </div>

      <!-- Traits -->
      <div class="dashboard-digimon-traits flex gap-2 mt-3 flex-wrap">
        <span class="badge-xs dashboard-digimon-tier-badge">${d.grade}</span>
        <span class="badge-xs dashboard-personality-badge">${formatPersonality(d.personality)}</span>
        ${d.trait ? `<span class="badge-xs dashboard-trait-badge badge-trait">${formatTrait(d.trait)}</span>` : ""}
        ${d.rebirthCount > 0 ? `<span class="badge-xs dashboard-rebirth-badge badge-rebirth">Rebirth ×${d.rebirthCount}</span>` : ""}
      </div>
    </div>
  `;
}

function dashboardStatNumber(value) {
  const number = Number(value);
  return Number.isFinite(number) ? number : 0;
}

function dashboardAverageIv(d) {
  const total = dashboardStatNumber(d.ivHp) + dashboardStatNumber(d.ivAttack) + dashboardStatNumber(d.ivDefense);
  return Math.round(total / 3);
}

function renderDashboardPotential(label, value, toneClass) {
  const percent = Math.min(100, Math.max(0, dashboardStatNumber(value)));
  return `
    <div class="dashboard-digimon-potential-card">
      <div class="dashboard-digimon-potential-label"><span>${label}</span><strong>${percent}%</strong></div>
      <div class="dashboard-digimon-potential-track" role="progressbar" aria-label="Potencial ${label}" aria-valuenow="${percent}" aria-valuemin="0" aria-valuemax="100"><span class="${toneClass}" style="width:${percent}%"></span></div>
    </div>
  `;
}

function dashboardFormatStat(value) {
  return dashboardStatNumber(value).toLocaleString("pt-BR");
}

function renderDashboardStat(label, base, equipmentBonus, clanBonus, colorClass) {
  const total = dashboardStatNumber(base) + dashboardStatNumber(equipmentBonus) + dashboardStatNumber(clanBonus);
  return `<div><p class="text-xs text-slate-500">${label}</p><p class="font-bold ${colorClass}">${dashboardFormatStat(total)}</p></div>`;
}

function renderDashboardStatBreakdown(label, base, equipmentBonus, clanBonus, colorClass) {
  const baseValue = dashboardStatNumber(base);
  const equipmentValue = dashboardStatNumber(equipmentBonus);
  const clanValue = dashboardStatNumber(clanBonus);
  const total = baseValue + equipmentValue + clanValue;
  return `
    <div class="rounded-md border border-slate-800/80 px-2 py-2">
      <div class="flex items-center justify-between gap-2 mb-1">
        <span class="text-xs font-bold ${colorClass}">${label}</span>
        <span class="text-xs font-bold text-slate-200">${dashboardFormatStat(total)}</span>
      </div>
      <div class="flex flex-wrap gap-x-3 gap-y-1 text-[10px] text-slate-500">
        <span>Digimon: ${dashboardFormatStat(baseValue)}</span>
        ${equipmentValue ? `<span class="text-green-400">Equipamentos: +${dashboardFormatStat(equipmentValue)}</span>` : ""}
        ${clanValue ? `<span class="text-cyan-400">Clã: +${dashboardFormatStat(clanValue)}</span>` : ""}
      </div>
    </div>
  `;
}

function renderEquipSlots(items) {
  const slots = ["WEAPON", "ARMOR", "ACCESSORY"];
  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
  const rarityClass = { COMMON: "dashboard-rarity-common", RARE: "dashboard-rarity-rare", EPIC: "dashboard-rarity-epic", LEGENDARY: "dashboard-rarity-legendary" };

  return slots.map(slot => {
    const item = items.find(i => i.slot === slot);
    if (item) {
      const rarity = String(item.rarity || "COMMON").toUpperCase();
      const refLabel = item.refinementLevel > 0 ? ` +${item.refinementLevel}` : "";
      const ascension = Number(item.ascensionLevel) || 0;
      const stats = [];
      if (Number(item.effectiveBonusHp) > 0) stats.push(`<span class="text-red-300">HP ${item.effectiveBonusHp}</span>`);
      if (Number(item.effectiveBonusAttack) > 0) stats.push(`<span class="text-orange-300">ATK ${item.effectiveBonusAttack}</span>`);
      if (Number(item.effectiveBonusDefense) > 0) stats.push(`<span class="text-blue-300">DEF ${item.effectiveBonusDefense}</span>`);
      return `
        <article class="dashboard-equipment-slot ${rarityClass[rarity] || rarityClass.COMMON}" role="button" tabindex="0" aria-label="Ver detalhes de ${escapeAttr(item.name)}" onclick="showEquipDetailModal('${item.id}')" onkeydown="if (event.key === 'Enter' || event.key === ' ') showEquipDetailModal('${item.id}')">
          <div class="dashboard-slot-topline"><span class="dashboard-slot-label">${slotName[slot]}</span><span class="dashboard-slot-status">Equipado</span></div>
          <div class="dashboard-slot-icon" aria-hidden="true">${slotEmoji[slot]}</div>
          <p class="dashboard-slot-name" title="${escapeAttr(`${item.name}${refLabel}`)}">${escapeHtml(item.name)}${refLabel}</p>
          <div class="dashboard-slot-meta"><span class="badge badge-${rarity.toLowerCase()}">T${item.tier || '?'}</span>${ascension > 0 ? `<span class="badge badge-legendary">Asc. ${ascension}</span>` : ''}</div>
          ${stats.length ? `<div class="dashboard-slot-stats">${stats.join('')}</div>` : '<div class="dashboard-slot-stats dashboard-slot-stats-muted">Toque para ver detalhes</div>'}
          <div class="dashboard-slot-action" onclick="event.stopPropagation()"><button class="btn-sm w-full" onclick="invUnequip('${item.id}')">Desequipar</button></div>
        </article>
      `;
    }
    return `
      <article class="dashboard-equipment-slot dashboard-equipment-slot-empty" role="button" tabindex="0" aria-label="Equipar ${slotName[slot]}" onclick="dashboardOpenEmptyEquipmentSlot('${slot}')" onkeydown="if (event.key === 'Enter' || event.key === ' ') dashboardOpenEmptyEquipmentSlot('${slot}')">
        <div class="dashboard-slot-topline"><span class="dashboard-slot-label">${slotName[slot]}</span><span class="dashboard-slot-status dashboard-slot-status-empty">Livre</span></div>
        <div class="dashboard-slot-icon dashboard-slot-icon-empty" aria-hidden="true">${slotEmoji[slot]}</div>
        <p class="dashboard-slot-name dashboard-slot-name-empty">Nenhum item equipado</p>
        <div class="dashboard-slot-stats dashboard-slot-stats-muted">Escolher equipamento</div>
        <div class="dashboard-slot-action"><span class="dashboard-slot-add">+ Adicionar</span></div>
      </article>
    `;
  }).join("");
}

function renderSetBonus(sb) {
  if (!sb || !sb.setCode || sb.pieceCount < 2) return "";
  const setLabels = { BERSERKER: "Berserker", GUARDIAN: "Guardiao", VITALITY: "Vitalidade", BALANCED: "Equilibrado" };
  const label = setLabels[sb.setCode] || sb.setCode;
  const bonuses = [];
  if (sb.bonusHpPercent > 0) bonuses.push(`<span class="text-red-400">HP +${sb.bonusHpPercent}%</span>`);
  if (sb.bonusAtkPercent > 0) bonuses.push(`<span class="text-orange-400">ATK +${sb.bonusAtkPercent}%</span>`);
  if (sb.bonusDefPercent > 0) bonuses.push(`<span class="text-blue-400">DEF +${sb.bonusDefPercent}%</span>`);
  if (bonuses.length === 0) return "";
  return `
    <div class="mt-2 px-2 py-1.5 rounded-lg bg-slate-800 text-xs text-center">
      <span class="text-yellow-400 font-bold">Set ${escapeHtml(label)} (${sb.pieceCount}/3)</span>: ${bonuses.join(" ")}
    </div>
  `;
}

function renderActiveMission(m) {
  const now = Date.now();
  const endsAt = new Date(m.endsAt).getTime();
  const remaining = Math.max(0, Math.floor((endsAt - now) / 1000));
  const done = remaining <= 0;

  return `
    <article class="dashboard-mission-card ${done ? "dashboard-mission-card-ready" : ""}" data-mission-instance="${m.instanceId}" data-ends-at="${m.endsAt}" data-auto-claim="${m.autoClaimEnabled ? "true" : "false"}">
      <div class="dashboard-mission-icon" aria-hidden="true">✦</div>
      <div class="dashboard-mission-main">
        <p class="dashboard-mission-label">Objetivo em campo</p>
        <p class="dashboard-mission-name">${escapeHtml(m.missionName)}</p>
        <p class="dashboard-mission-team">${escapeHtml(m.teamName || (m.teamId ? "Time de missão" : "Missão legada"))}${m.teamId ? " · 3 Digimons" : ""}</p>
        <div class="dashboard-mission-state"><span class="dashboard-mission-dot ${done ? "dashboard-mission-dot-ready" : ""}"></span><span class="mission-timer">${done ? "Concluída!" : `Retorno em ${formatTime(remaining)}`}</span></div>
      </div>
      <div class="dashboard-mission-action">${done ? `<button class="btn-sm btn-primary" onclick="claimMission('${m.instanceId}')">Resgatar</button>` : `<span class="dashboard-mission-badge">Em andamento</span>`}</div>
    </article>
  `;
}

function renderIncubation(inc) {
  if (!inc || !Array.isArray(inc.slots)) return "";
  const activeSlots = inc.slots.filter(slot => slot.incubation);
  if (activeSlots.length === 0) return "";

  return `
    <section class="dashboard-incubation-section mb-4" id="dash-incubation">
      <div class="dashboard-section-heading">
        <div><p class="dashboard-eyebrow dashboard-eyebrow-amber">Ciclo ativo</p><h3 class="dashboard-section-title">Incubação</h3></div>
        <button class="dashboard-section-link" onclick="navigateTo('incubation')">Ver slots <span aria-hidden="true">›</span></button>
      </div>
      <p class="dashboard-section-note">Acompanhe seus Digitamas enquanto eles evoluem.</p>
      <div class="dashboard-incubation-list">
        ${activeSlots.map(slot => renderDashboardIncubationSlot(slot)).join("")}
      </div>
    </section>
  `;
}

function renderDashboardIncubationSlot(slot) {
  const slotNumber = Number(slot.slotNumber);
  if (!slot.unlocked) {
    return `
      <div class="dashboard-incubation-card dashboard-incubation-card-locked" data-dash-incub-slot="${slotNumber}">
        <div class="dashboard-incubation-icon" aria-hidden="true">🔒</div>
        <div class="min-w-0"><p class="dashboard-incubation-label">Slot ${slotNumber}</p><p class="dashboard-incubation-name">Slot bloqueado</p></div>
        <span class="badge">Bloqueado</span>
      </div>
    `;
  }

  if (!slot.incubation) {
    return `
      <div class="dashboard-incubation-card dashboard-incubation-card-free" data-dash-incub-slot="${slotNumber}" onclick="navigateTo('incubation')">
        <div class="dashboard-incubation-icon" aria-hidden="true">🥚</div>
        <div class="min-w-0"><p class="dashboard-incubation-label">Slot ${slotNumber}</p><p class="dashboard-incubation-name">Disponível para uso</p></div>
        <span class="dashboard-incubation-link">Abrir <span aria-hidden="true">›</span></span>
      </div>
    `;
  }

  const incubation = slot.incubation;
  const remaining = Math.max(0, Number(incubation.remainingSeconds) || 0);
  const done = incubation.status === "READY" || remaining <= 0;
  const progress = dashboardIncubationProgress(incubation);
  const digitamaEmoji = dashboardIncubationEmoji(incubation.digitamaType);
  return `
    <article class="dashboard-incubation-card ${done ? "dashboard-incubation-card-ready" : "dashboard-incubation-card-active"}" data-dash-incub-slot="${slotNumber}" data-finish-at="${escapeAttr(incubation.finishAt)}" data-started-at="${escapeAttr(incubation.startedAt)}" data-remaining-seconds="${remaining}" onclick="navigateTo('incubation')">
      <div class="dashboard-incubation-icon" aria-hidden="true">${digitamaEmoji}</div>
      <div class="dashboard-incubation-main">
        <div class="dashboard-incubation-topline"><span class="dashboard-incubation-label">Slot ${slotNumber}</span><span class="dashboard-incubation-status">${done ? "Pronto" : "Em andamento"}</span></div>
        <p class="dashboard-incubation-name">${escapeHtml(formatItemType(incubation.digitamaType))}</p>
        <p class="dashboard-incubation-meta">${escapeHtml(formatItemType(incubation.incubatorType))}</p>
        <div class="dashboard-incubation-progress" role="progressbar" aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100" aria-label="${progress}% da incubação concluída"><span id="incub-dash-bar-${slotNumber}" style="width:${progress}%"></span></div>
      </div>
      <div class="dashboard-incubation-timer-wrap"><p class="dashboard-incubation-timer ${done ? "dashboard-incubation-timer-ready" : ""}" id="incub-dash-timer-${slotNumber}">${done ? "Pronta!" : formatTime(remaining)}</p>${done ? `<button class="btn-sm btn-primary" onclick="event.stopPropagation(); navigateTo('incubation')">Chocar</button>` : `<span class="dashboard-incubation-open">Ver detalhes</span>`}</div>
    </article>
  `;
}

function dashboardIncubationProgress(incubation) {
  const total = (new Date(incubation.finishAt) - new Date(incubation.startedAt)) / 1000;
  if (!Number.isFinite(total) || total <= 0) return 0;
  const remaining = Math.max(0, Number(incubation.remainingSeconds) || 0);
  return Math.min(100, Math.round(((total - remaining) / total) * 100));
}

function dashboardIncubationEmoji(type) {
  const map = { DIGITAMA_FIRE: "🔥", DIGITAMA_WATER: "💧", DIGITAMA_NATURE: "🌿", DIGITAMA_EARTH: "🪨", DIGITAMA_WIND: "🌪️", DIGITAMA_LIGHT: "✨", DIGITAMA_DARK: "🌑", DIGITAMA_THUNDER: "⚡", DIGITAMA_ICE: "❄️", DIGITAMA_STEEL: "⚙️" };
  return map[type] || "🥚";
}

async function claimMission(instanceId) {
  try {
    const result = await apiPost(`/missions/${instanceId}/claim`);
    const fullAutomatic = Boolean(result && result.autoClaimEnabled);
    if (!fullAutomatic) showMissionClaimModal(result);
    if (typeof startMissionAutoRepeat === "function") await startMissionAutoRepeat(result, fullAutomatic);
    renderDashboardPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}

// Incubation timers
function startIncubationTimer(inc = null) {
  if (typeof incubStopTimer === "function") incubStopTimer();
  if (!inc || !Array.isArray(inc.slots)) return;

  inc.slots.forEach(slot => {
    const incubation = slot.incubation;
    if (!slot.unlocked || !incubation || incubation.status === "READY") return;

    const remaining = Math.max(0, Number(incubation.remainingSeconds) || 0);
    if (remaining <= 0) {
      dashboardMarkIncubationReady(Number(slot.slotNumber));
      return;
    }

    incubStartTimer({
      key: `dashboard-slot-${Number(slot.slotNumber)}`,
      finishAt: incubation.finishAt,
      startedAt: incubation.startedAt,
      remainingSeconds: remaining,
      timerId: `incub-dash-timer-${Number(slot.slotNumber)}`,
      barId: `incub-dash-bar-${Number(slot.slotNumber)}`,
      formatter: formatTime,
      onComplete: () => dashboardMarkIncubationReady(Number(slot.slotNumber))
    });
  });
}

function dashboardMarkIncubationReady(slotNumber) {
  const timerEl = document.getElementById(`incub-dash-timer-${slotNumber}`);
  if (!timerEl) return;

  timerEl.textContent = "Pronta!";
  timerEl.className = "dashboard-incubation-timer dashboard-incubation-timer-ready";
  const barEl = document.getElementById(`incub-dash-bar-${slotNumber}`);
  if (barEl) barEl.style.width = "100%";
  const card = timerEl.closest(".dashboard-incubation-card");
  if (card) {
    card.classList.remove("dashboard-incubation-card-active");
    card.classList.add("dashboard-incubation-card-ready");
  }
  const parent = timerEl.parentElement;
  if (parent && !parent.querySelector("button")) {
    const details = parent.querySelector(".dashboard-incubation-open");
    if (details) details.remove();
    parent.insertAdjacentHTML("beforeend", `<button class="btn-sm btn-primary" onclick="event.stopPropagation(); navigateTo('incubation')">Chocar</button>`);
  }
}

// Mission timer logic
let missionTimerInterval = null;

function startMissionTimers() {
  if (missionTimerInterval) clearInterval(missionTimerInterval);
  missionTimerInterval = setInterval(() => {
    document.querySelectorAll("[data-mission-instance]").forEach(el => {
      const endsAt = new Date(el.dataset.endsAt).getTime();
      const remaining = Math.max(0, Math.floor((endsAt - Date.now()) / 1000));
      const timerEl = el.querySelector(".mission-timer");
      if (!timerEl) return;

      if (remaining <= 0) {
        if (el.dataset.autoClaim === "true" && el.dataset.autoClaiming !== "true" && typeof claimMissionAutomatically === "function") {
          el.dataset.autoClaiming = "true";
          timerEl.textContent = "Resgatando...";
          claimMissionAutomatically(el.dataset.missionInstance)
            .then(() => renderDashboardPage())
            .catch(err => {
              el.dataset.autoClaiming = "false";
              showToast(`Automático completo pausado: ${err.message}`, "error");
            });
          return;
        }

        timerEl.textContent = "Concluída!";
        el.classList.add("dashboard-mission-card-ready");
        const dot = el.querySelector(".dashboard-mission-dot");
        if (dot) dot.classList.add("dashboard-mission-dot-ready");
        const btn = el.querySelector("button");
        if (!btn) {
          const badgeEl = el.querySelector(".dashboard-mission-badge");
          if (badgeEl) {
            badgeEl.outerHTML = `<button class="btn-sm btn-primary" onclick="claimMission('${el.dataset.missionInstance}')">Resgatar</button>`;
          }
        }
      } else {
        timerEl.textContent = `Retorno em ${formatTime(remaining)}`;
      }
    });
  }, 1000);
}

// Helpers
function formatStage(stage) {
  const map = { BABY: "Baby", BABY_II: "Baby II", ROOKIE: "Rookie", CHAMPION: "Champion", ULTIMATE: "Ultimate", MEGA: "Mega" };
  return map[stage] || stage;
}

function formatPersonality(p) {
  const map = { DURABLE: "Durável", LIVELY: "Vivaz", FIGHTER: "Lutador", DEFENDER: "Defensor", BRAINY: "Esperto", NIMBLE: "Ágil" };
  return map[p] || p || "—";
}

function formatPersonalityEffect(p) {
  const map = {
    DURABLE: "HP +10%",
    LIVELY: "XP +10%",
    FIGHTER: "ATK +10%",
    DEFENDER: "DEF +10%",
    BRAINY: "ATK +5% · XP +5%",
    NIMBLE: "ATK +5% · DEF +5%"
  };
  return map[p] || "Sem efeito registrado";
}

function formatTraitName(t) {
  const map = { FAST_LEARNER: "Aprendiz rápido", ENERGETIC: "Energético", VITALITY: "Vitalidade", BERSERKER: "Berserker", IRON_BODY: "Corpo de ferro" };
  return map[t] || t || "—";
}

function formatTraitEffect(t) {
  const map = { FAST_LEARNER: "XP +10%", ENERGETIC: "Energia +5", VITALITY: "HP +10%", BERSERKER: "ATK +10%", IRON_BODY: "DEF +10%" };
  return map[t] || "Sem efeito registrado";
}

function formatTrait(t) {
  return formatTraitEffect(t);
}

function formatItemType(t) {
  if (!t) return "";
  const map = {
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
    INCUBATOR_LEGENDARY: "Incubadora Lendária"
  };
  return map[t] || t.replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase());
}

function formatTime(seconds) {
  if (seconds <= 0) return "0s";
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}

function getXpForLevel(level) {
  if (level >= 100) return 0;
  return level * 100;
}

function openRenameModal(digimonId, currentName) {
  const overlay = document.createElement("div");
  overlay.id = "rename-overlay";
  overlay.className = "fixed inset-0 bg-black/70 z-50 flex items-center justify-center animate-fade-in";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  overlay.innerHTML = `
    <div class="card" style="max-width:360px;width:90%;">
      <h3 class="font-bold text-lg mb-3 text-center">Renomear Digimon</h3>
      <input id="rename-input" type="text" maxlength="20" value="${escapeHtml(currentName)}"
        class="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-600 text-white text-sm mb-1 outline-none focus:border-cyan-500"
        placeholder="Novo nome (max 20 caracteres)">
      <p class="text-xs text-slate-500 mb-3 text-right"><span id="rename-char-count">${currentName.length}</span>/20</p>
      <div class="flex gap-2">
        <button class="flex-1 py-2 rounded-lg font-bold text-sm bg-slate-700 text-slate-300" onclick="document.getElementById('rename-overlay').remove()">Cancelar</button>
        <button id="rename-confirm-btn" class="flex-1 py-2 rounded-lg font-bold text-sm btn-primary" onclick="confirmRename('${digimonId}')">Salvar</button>
      </div>
    </div>
  `;

  document.body.appendChild(overlay);

  const input = document.getElementById("rename-input");
  input.focus();
  input.select();
  input.addEventListener("input", () => {
    document.getElementById("rename-char-count").textContent = input.value.length;
  });
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") confirmRename(digimonId);
  });
}

async function confirmRename(digimonId) {
  const input = document.getElementById("rename-input");
  const newName = input.value.trim();
  if (!newName || newName.length > 20) {
    showToast("Nome deve ter entre 1 e 20 caracteres", "error");
    return;
  }

  const btn = document.getElementById("rename-confirm-btn");
  btn.disabled = true;
  btn.textContent = "Salvando...";

  try {
    await apiPut("/digimon/rename", { digimonId, newName });
    document.getElementById("rename-overlay").remove();
    showToast("Digimon renomeado!");
    renderDashboardPage();
  } catch (err) {
    showToast(err.message, "error");
    btn.disabled = false;
    btn.textContent = "Salvar";
  }
}

function showEquipDetailModal(equipmentId) {
  const eq = dashEquippedItems.find(e => e.id === equipmentId);
  if (!eq) return;

  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
  const emoji = slotEmoji[eq.slot] || "⚔️";
  const refLabel = eq.refinementLevel > 0 ? ` +${eq.refinementLevel}` : "";
  const setLabel = typeof invSetLabel === "function" ? invSetLabel(eq.setCode) : (eq.setCode || "");
  const rarityLabel = { COMMON: "Comum", RARE: "Rara", EPIC: "Épica", LEGENDARY: "Lendária" };
  const rarityPercent = { COMMON: 0, RARE: 15, EPIC: 30, LEGENDARY: 50 };
  const rarityMultiplier = { COMMON: 1, RARE: 1.15, EPIC: 1.30, LEGENDARY: 1.50 };
  const ascensionLevel = Number(eq.ascensionLevel) || 0;
  const ascensionBonus = { 0: "Nenhum", 1: "+30%", 2: "+50%", 3: "+100%" }[ascensionLevel];
  const requiredRebirths = [0, 1, 10, 20][ascensionLevel] || 0;
  const statImpact = (values) => Object.entries(values).filter(([, value]) => value > 0).map(([label, value]) => `+${label} ${value}`).join(" · ") || "nenhum";
  const rarityImpact = statImpact({ HP: Math.round((eq.bonusHp || 0) * ((rarityMultiplier[eq.rarity] || 1) - 1)), ATK: Math.round((eq.bonusAttack || 0) * ((rarityMultiplier[eq.rarity] || 1) - 1)), DEF: Math.round((eq.bonusDefense || 0) * ((rarityMultiplier[eq.rarity] || 1) - 1)) });
  const refinedStat = (base) => Math.round(base * (rarityMultiplier[eq.rarity] || 1)) + ((Number(eq.refinementLevel) || 0) * 2);
  const ascensionMultiplier = { 0: 1, 1: 1.30, 2: 1.50, 3: 2.00 }[ascensionLevel] || 1;
  const ascensionImpact = statImpact({ HP: Math.round(refinedStat(eq.bonusHp || 0) * (ascensionMultiplier - 1)), ATK: Math.round(refinedStat(eq.bonusAttack || 0) * (ascensionMultiplier - 1)), DEF: Math.round(refinedStat(eq.bonusDefense || 0) * (ascensionMultiplier - 1)) });
  const refinementImpact = statImpact({ HP: eq.bonusHp > 0 ? (Number(eq.refinementLevel) || 0) * 2 : 0, ATK: eq.bonusAttack > 0 ? (Number(eq.refinementLevel) || 0) * 2 : 0, DEF: eq.bonusDefense > 0 ? (Number(eq.refinementLevel) || 0) * 2 : 0 });
  const refinementImpactDisplay = refinementImpact.replaceAll(" · ", " - ");

  const overlay = document.createElement("div");
  overlay.id = "dashboard-equipment-detail-overlay";
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(0,0,0,0.7);z-index:50;display:flex;align-items:center;justify-content:center;padding:1rem;";
  overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };

  overlay.innerHTML = `
    <div class="card dashboard-equipment-detail-card" style="max-width:560px;width:100%;max-height:92vh;overflow:hidden;border-radius:1rem;margin:0 auto;">
      <div class="flex items-center gap-3 mb-4">
        <div class="text-3xl shrink-0">${emoji}</div>
        <div class="min-w-0"><h3 class="text-lg font-bold truncate">${escapeHtml(eq.name)}${refLabel}</h3><p class="text-xs text-slate-400 mt-1">${slotName[eq.slot] || eq.slot}</p></div>
      </div>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Atributos finais</p>
        <div class="grid grid-cols-3 gap-2 text-center text-xs">
          <div class="rounded-lg border border-red-900/50 bg-red-950/20 p-2"><span class="text-slate-400">HP</span><br><strong class="text-red-300 text-base">${eq.effectiveBonusHp || 0}</strong></div>
          <div class="rounded-lg border border-orange-900/50 bg-orange-950/20 p-2"><span class="text-slate-400">ATK</span><br><strong class="text-orange-300 text-base">${eq.effectiveBonusAttack || 0}</strong></div>
          <div class="rounded-lg border border-blue-900/50 bg-blue-950/20 p-2"><span class="text-slate-400">DEF</span><br><strong class="text-blue-300 text-base">${eq.effectiveBonusDefense || 0}</strong></div>
        </div>
      </div>

      <details class="card-sm mb-3 group">
        <summary class="cursor-pointer list-none flex items-center justify-between text-xs text-slate-400"><span>Detalhes do equipamento</span><span class="text-slate-500 group-open:rotate-180 transition-transform">⌄</span></summary>
        <div class="mt-3 pt-3 border-t border-slate-700/70 grid grid-cols-2 gap-2 text-xs">
          <div><span class="text-slate-500">Tier</span><p class="font-semibold">T${eq.tier || '?'}</p></div>
          <div><span class="text-slate-500">Raridade</span><p class="font-semibold">${rarityLabel[eq.rarity] || eq.rarity} <span class="text-amber-300">(+${rarityPercent[eq.rarity] || 0}%)</span></p><p class="text-[10px] text-slate-500">${rarityImpact}</p></div>
          <div><span class="text-slate-500">Conjunto</span><p class="font-semibold">${eq.setCode ? escapeHtml(setLabel) : 'Sem conjunto'}</p></div>
          <div><span class="text-slate-500">Refinamento</span><p class="font-semibold text-yellow-400">${eq.refinementLevel}</p><p class="text-[10px] text-slate-500">${refinementImpactDisplay}</p></div>
          <div><span class="text-slate-500">Ascensão</span><p class="font-semibold text-amber-300">${ascensionLevel} (${ascensionBonus})</p><p class="text-[10px] text-slate-500">${ascensionImpact}</p></div>
          <div><span class="text-slate-500">Uso mínimo</span><p class="font-semibold">${requiredRebirths} ${requiredRebirths === 1 ? 'Renascimento' : 'Renascimentos'}</p></div>
          <div class="col-span-2"><span class="text-slate-500">Bônus base</span><p class="font-semibold text-slate-300">HP ${eq.bonusHp || 0} · ATK ${eq.bonusAttack || 0} · DEF ${eq.bonusDefense || 0}</p></div>
        </div>
      </details>

      <div class="card-sm mb-3">
        <p class="text-xs text-slate-400 mb-2">Outros equipamentos para este slot</p>
        <div id="dashboard-equipment-alternatives" class="dashboard-equipment-alternatives space-y-2 max-h-56 overflow-y-auto pr-1">
          <p class="text-xs text-slate-500">Carregando equipamentos disponíveis...</p>
        </div>
      </div>

      <button class="btn-primary w-full py-3 text-base" onclick="document.getElementById('dashboard-equipment-detail-overlay')?.remove()">Fechar</button>
    </div>
  `;

  document.body.appendChild(overlay);
  dashboardLoadEquipmentAlternatives(eq.slot, eq.id);
}

async function dashboardLoadEquipmentAlternatives(slot, equippedId) {
  const container = document.getElementById("dashboard-equipment-alternatives");
  if (!container) return;
  try {
    const equipment = (await apiGet("/equipment/inventory") || [])
      .filter(item => item.slot === slot && item.id !== equippedId);
    if (!equipment.length) {
      container.innerHTML = '<p class="text-xs text-slate-500">Nenhum outro equipamento disponível para este slot.</p>';
      return;
    }
    container.innerHTML = equipment.map(dashboardEquipmentAlternativeCard).join("");
  } catch (err) {
    container.innerHTML = `<p class="text-xs text-red-300">${escapeHtml(err.message)}</p>`;
  }
}

function dashboardEquipmentAlternativeCard(item) {
  const slotEmoji = { WEAPON: "⚔️", ARMOR: "🛡️", ACCESSORY: "💍" };
  const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
  const rarity = String(item.rarity || "COMMON").toLowerCase();
  const refLabel = Number(item.refinementLevel) > 0 ? ` +${item.refinementLevel}` : "";
  const stats = [];
  if (Number(item.effectiveBonusHp) > 0) stats.push(`<span class="text-red-400">HP+${item.effectiveBonusHp}</span>`);
  if (Number(item.effectiveBonusAttack) > 0) stats.push(`<span class="text-orange-400">ATK+${item.effectiveBonusAttack}</span>`);
  if (Number(item.effectiveBonusDefense) > 0) stats.push(`<span class="text-blue-400">DEF+${item.effectiveBonusDefense}</span>`);
  const setLabel = typeof invSetLabel === "function" ? invSetLabel(item.setCode) : item.setCode;
  const setBadge = typeof invSetBadge === "function" ? invSetBadge(item.setCode) : "common";
  return `
    <div class="rounded-lg border border-slate-700 bg-slate-900/60 p-2">
      <div class="flex items-center gap-2">
        <div class="text-xl shrink-0">${slotEmoji[item.slot] || "⚔️"}</div>
        <div class="min-w-0 flex-1">
          <p class="font-bold text-xs truncate">${escapeHtml(item.name)}${refLabel}</p>
          <div class="flex gap-1 mt-1 flex-wrap items-center">
            ${item.setCode ? `<span class="badge badge-${setBadge}">${escapeHtml(setLabel)}</span>` : ""}
            <span class="badge badge-${rarity}">T${item.tier || "?"}</span>
            <span class="text-[10px] text-slate-500">${slotName[item.slot] || item.slot}</span>
          </div>
          ${stats.length ? `<div class="flex gap-2 mt-1 text-[10px] font-bold">${stats.join(" ")}</div>` : ""}
        </div>
        <button class="btn-sm btn-primary shrink-0 text-xs" onclick="dashboardEquipAlternative('${item.id}')">Equipar</button>
      </div>
    </div>
  `;
}

function dashboardOpenEmptyEquipmentSlot(slot) {
  const slotName = { WEAPON: "Arma", ARMOR: "Armadura", ACCESSORY: "Acessório" };
  const overlay = document.createElement("div");
  overlay.id = "dashboard-equipment-detail-overlay";
  overlay.style.cssText = "position:fixed;inset:0;background:rgba(0,0,0,0.7);z-index:50;display:flex;align-items:center;justify-content:center;padding:1rem;";
  overlay.onclick = (event) => { if (event.target === overlay) overlay.remove(); };
  overlay.innerHTML = `
    <div class="card dashboard-equipment-detail-card" style="max-width:560px;width:100%;max-height:92vh;overflow:hidden;border-radius:1rem;margin:0 auto;">
      <div class="flex items-center justify-between mb-3">
        <div>
          <h3 class="text-lg font-bold">Equipar ${slotName[slot] || slot}</h3>
          <p class="text-xs text-slate-400 mt-1">Escolha um equipamento disponível para este slot.</p>
        </div>
        <button class="text-slate-400 text-xl" onclick="document.getElementById('dashboard-equipment-detail-overlay')?.remove()" aria-label="Fechar">×</button>
      </div>
      <div class="card-sm mb-3">
        <div id="dashboard-equipment-alternatives" class="dashboard-equipment-alternatives space-y-2 max-h-56 overflow-y-auto pr-1">
          <p class="text-xs text-slate-500">Carregando equipamentos disponíveis...</p>
        </div>
      </div>
      <button class="btn-primary w-full py-3 text-base" onclick="document.getElementById('dashboard-equipment-detail-overlay')?.remove()">Fechar</button>
    </div>
  `;
  document.body.appendChild(overlay);
  dashboardLoadEquipmentAlternatives(slot, null);
}

async function dashboardEquipAlternative(equipmentId) {
  try {
    await apiPost("/equipment/equip", { equipmentId });
    document.getElementById("dashboard-equipment-detail-overlay")?.remove();
    showToast("Equipamento equipado!");
    await renderDashboardPage();
  } catch (err) {
    showToast(err.message, "error");
  }
}
