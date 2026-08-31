function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

function escapeAttr(value) {
  return escapeHtml(value);
}

const PLAYER_SHORTCUT_DEFAULTS = ["dashboard", "missions", "inventory", "shop"];
const PLAYER_SHORTCUT_MOBILE_LIMIT = 4;
const PLAYER_SHORTCUT_DESKTOP_LIMIT = 8;
let playerShortcutRoutes = null;
let playerShortcutPreferenceLoading = false;
let playerShortcutResizeBound = false;

const PLAYER_SHORTCUT_CATALOG = [
  { route: "dashboard", label: "Home", icon: "🏠" },
  { route: "activity-calendar", label: "Atividades", image: "assets/img/calendario-atividades.webp" },
  { route: "missions", label: "Missões", image: "assets/img/missoes.webp" },
  { route: "shop", label: "Loja", image: "assets/img/loja.webp" },
  { route: "forge", label: "Ferreiro", icon: "🔨" },
  { route: "auction-house", label: "Leilão", icon: "🔨" },
  { route: "mail", label: "Correio", icon: "✉️" },
  { route: "inventory", label: "Mochila", image: "assets/img/mochila.webp", imageClass: "nav-backpack" },
  { route: "evolution", label: "Digimon", icon: "🧬" },
  { route: "rebirth", label: "Renascimento", icon: "↻" },
  { route: "incubation", label: "Incubação", image: "assets/img/incubacao.webp" },
  { route: "pokedex", label: "Biblioteca", image: "assets/img/bibliotecadigimon.webp" },
  { route: "bosses", label: "Chefes", image: "assets/img/batalhadochefe.webp" },
  { route: "arena", label: "Arena", image: "assets/img/arena.webp" },
  { route: "ranking", label: "Ranking", image: "assets/img/ranking.webp" },
  { route: "storage", label: "Armazém", image: "assets/img/armazemdigimon.webp" },
  { route: "collection", label: "Coleção", icon: "📚" },
  { route: "clans", label: "Clãs", image: "assets/img/cla.webp" },
  { route: "world-boss", label: "Chefe mundial", image: "assets/img/chefe-mundial.webp" }
];

function getPlayerShortcutCatalog() {
  return PLAYER_SHORTCUT_CATALOG.map(item => ({ ...item }));
}

function normalizePlayerShortcutRoutes(routes) {
  const allowed = new Set(PLAYER_SHORTCUT_CATALOG.map(item => item.route));
  return [...new Set((Array.isArray(routes) ? routes : []).filter(route => allowed.has(route)))].slice(0, PLAYER_SHORTCUT_DESKTOP_LIMIT);
}

function isShortcutMobileViewport() {
  return window.matchMedia ? window.matchMedia("(max-width: 640px)").matches : window.innerWidth <= 640;
}

function renderShortcutBar(activeRoute) {
  const nav = document.getElementById("bottom-nav");
  if (!nav) return;
  const routes = normalizePlayerShortcutRoutes(playerShortcutRoutes || PLAYER_SHORTCUT_DEFAULTS);
  const visibleRoutes = routes.slice(0, isShortcutMobileViewport() ? PLAYER_SHORTCUT_MOBILE_LIMIT : PLAYER_SHORTCUT_DESKTOP_LIMIT);
  const items = visibleRoutes.map(route => PLAYER_SHORTCUT_CATALOG.find(item => item.route === route)).filter(Boolean);
  items.push({ route: "more", label: "Mais", icon: "⋯", required: true });
  nav.innerHTML = `<div class="bottom-nav-inner">${items.map(shortcutNavItem).join("")}</div>`;
  nav.classList.remove("hidden");
  nav.querySelectorAll(".nav-btn").forEach(btn => btn.classList.toggle("active", btn.dataset.route === activeRoute));
  if (!playerShortcutResizeBound) {
    window.addEventListener("resize", () => renderShortcutBar(window.location.hash.replace("#", "").split("?")[0] || "dashboard"));
    playerShortcutResizeBound = true;
  }
}

function shortcutNavItem(item) {
  const icon = item.image
    ? `<img class="nav-icon nav-image ${item.imageClass || ""}" src="${escapeAttr(item.image)}" alt="" />`
    : `<span class="nav-icon">${item.icon || "•"}</span>`;
  return `<button class="nav-btn" data-route="${escapeAttr(item.route)}" onclick="navigateTo('${escapeAttr(item.route)}')">${icon}<span>${escapeHtml(item.label)}</span></button>`;
}

async function loadPlayerShortcutPreference() {
  if (playerShortcutRoutes !== null) return playerShortcutRoutes;
  if (playerShortcutPreferenceLoading) return PLAYER_SHORTCUT_DEFAULTS;
  playerShortcutPreferenceLoading = true;
  try {
    const preference = await apiGet("/players/me/preferences/shortcuts");
    playerShortcutRoutes = normalizePlayerShortcutRoutes(preference?.routes);
  } catch (_) {
    playerShortcutRoutes = [...PLAYER_SHORTCUT_DEFAULTS];
  } finally {
    playerShortcutPreferenceLoading = false;
  }
  renderShortcutBar(window.location.hash.replace("#", "").split("?")[0] || "dashboard");
  return playerShortcutRoutes;
}

async function savePlayerShortcutPreference(routes) {
  const normalized = normalizePlayerShortcutRoutes(routes);
  const preference = await apiPut("/players/me/preferences/shortcuts", { routes: normalized });
  playerShortcutRoutes = normalizePlayerShortcutRoutes(preference?.routes || normalized);
  try {
    renderShortcutBar(window.location.hash.replace("#", "").split("?")[0] || "dashboard");
  } catch (error) {
    // A preferência já foi persistida. Um erro visual da barra não deve
    // fazer o editor informar que o salvamento falhou.
    console.warn("Preferência de atalhos salva, mas a barra não foi atualizada visualmente.", error);
  }
  return playerShortcutRoutes;
}

function showBottomNav(activeRoute) {
  const nav = document.getElementById("bottom-nav");
  if (!nav) return;
  renderShortcutBar(activeRoute);
  if (playerShortcutRoutes === null && !playerShortcutPreferenceLoading) loadPlayerShortcutPreference();
}

function localizeGameMessage(message) {
  const translations = {
    "The world boss has already been defeated today": "O Chefe Mundial foi derrotado. O próximo renascimento ocorrerá uma hora após a derrota."
  };
  return translations[String(message || "")] || message;
}

function renderChestIcon(sizeClass = "w-10 h-10") {
  return `<img src="assets/img/baus.webp" alt="Baú" class="${sizeClass} object-contain shrink-0" />`;
}

let playerPaginationEnabled = true;

async function loadPlayerPaginationPreference() {
  try {
    const preference = await apiGet("/players/me/preferences/pagination");
    playerPaginationEnabled = preference?.paginationEnabled !== false;
  } catch (_) {
    playerPaginationEnabled = true;
  }
  return playerPaginationEnabled;
}

async function savePlayerPaginationPreference(enabled) {
  const preference = await apiPut("/players/me/preferences/pagination", { paginationEnabled: Boolean(enabled) });
  playerPaginationEnabled = preference?.paginationEnabled !== false;
  return playerPaginationEnabled;
}

function showToast(message, type = "success") {
  message = localizeGameMessage(message);
  const existing = document.querySelector(".toast");
  if (existing) existing.remove();

  const colors = {
    success: "bg-green-900 border-green-700 text-green-200",
    error: "bg-red-900 border-red-700 text-red-200",
    info: "bg-cyan-900 border-cyan-700 text-cyan-200"
  };

  const toast = document.createElement("div");
  toast.className = `toast fixed top-4 left-1/2 -translate-x-1/2 px-4 py-2 rounded-lg border text-sm z-50 ${colors[type] || colors.info} animate-fade-in`;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add("animate-fade-out");
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

/**
 * Visual replacement for window.confirm().
 * Usage: if (!(await showConfirm("Tem certeza?"))) return;
 * Optional second argument overrides title/button labels/danger style:
 * showConfirm("Dissolver o clã?", { title: "Dissolver Clã", confirmText: "Dissolver", danger: true })
 */
function showNewlyUnlockedContent(content) {
  const missions = Array.isArray(content && content.missions) ? content.missions : [];
  const areas = Array.isArray(content && content.areas) ? content.areas : [];
  if (missions.length === 0 && areas.length === 0) return;

  const existing = document.getElementById("newly-unlocked-content-modal");
  if (existing) existing.remove();
  const areaNames = { NATIVE_FOREST: "Floresta Nativa", GEAR_SAVANNA: "Savana Gear", FACTORIAL_TOWN: "Cidade Fatorial", FREEZELAND: "Terra Congelada", SERVER_DESERT: "Deserto Server", INFINITY_MOUNTAIN: "Montanha Infinita" };
  const areaLabel = area => areaNames[String(area || "")] || String(area || "Área desconhecida");
  const stageLabel = stage => typeof formatStage === "function" ? formatStage(stage) : String(stage || "não informado");
  const missionRows = missions.map(m => `<div class="rounded-lg border border-cyan-800 bg-cyan-950/40 px-3 py-2"><p class="font-semibold text-cyan-100">${escapeHtml(m.name || m.id)}</p><p class="text-xs text-cyan-300 mt-1">${escapeHtml(areaLabel(m.area))} · Disponível no nível ${Number(m.requiredLevel) || 0}</p></div>`).join("");
  const areaRows = areas.map(a => `<div class="rounded-lg border border-purple-800 bg-purple-950/40 px-3 py-2"><p class="font-semibold text-purple-100">${escapeHtml(areaLabel(a.area))}</p><p class="text-xs text-purple-300 mt-1">Área liberada · Stage mínimo: ${escapeHtml(stageLabel(a.requiredStage))}</p></div>`).join("");
  const overlay = document.createElement("div");
  overlay.id = "newly-unlocked-content-modal";
  overlay.className = "fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/75";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.innerHTML = `<div class="card w-full max-w-lg max-h-[88vh] overflow-y-auto" onclick="event.stopPropagation()"><div class="flex items-start justify-between gap-4 mb-5"><div><p class="text-xs uppercase tracking-wider text-emerald-400 font-bold">Novo conteúdo disponível</p><h3 class="text-xl font-bold mt-1">Você desbloqueou novidades!</h3><p class="text-sm text-slate-400 mt-1">Confira o que ficou disponível após o avanço do seu Digimon.</p></div><button class="text-slate-400 hover:text-white text-2xl leading-none" aria-label="Fechar" onclick="document.getElementById('newly-unlocked-content-modal')?.remove()">&times;</button></div>${areas.length ? `<h4 class="text-sm font-bold text-purple-300 mb-2">Novas áreas</h4><div class="space-y-2 mb-5">${areaRows}</div>` : ""}${missions.length ? `<h4 class="text-sm font-bold text-cyan-300 mb-2">Novas missões</h4><div class="space-y-2">${missionRows}</div>` : ""}<button class="btn-primary w-full mt-5" onclick="document.getElementById('newly-unlocked-content-modal')?.remove()">Continuar</button></div>`;
  overlay.addEventListener("click", event => { if (event.target === overlay) overlay.remove(); });
  document.body.appendChild(overlay);
}

function showConfirm(message, options = {}) {
  const {
    title = "Confirmação",
    confirmText = "Confirmar",
    cancelText = "Cancelar",
    danger = false
  } = options;

  return new Promise(resolve => {
    const existing = document.getElementById("confirm-modal-overlay");
    if (existing) existing.remove();

    const overlay = document.createElement("div");
    overlay.id = "confirm-modal-overlay";
    overlay.className = "shop-modal-overlay";
    overlay.style.zIndex = "60";

    const close = result => {
      overlay.remove();
      resolve(result);
    };

    overlay.addEventListener("click", event => {
      if (event.target === overlay) close(false);
    });

    overlay.innerHTML = `
      <div class="shop-modal">
        <p class="font-bold text-base mb-2">${escapeHtml(title)}</p>
        <p class="text-sm text-slate-300 mb-5">${escapeHtml(message)}</p>
        <div class="flex gap-2">
          <button class="btn-secondary flex-1" data-action="cancel">${escapeHtml(cancelText)}</button>
          <button class="${danger ? "btn-red" : "btn-primary"} flex-1" data-action="confirm">${escapeHtml(confirmText)}</button>
        </div>
      </div>
    `;

    overlay.querySelector('[data-action="cancel"]').addEventListener("click", () => close(false));
    overlay.querySelector('[data-action="confirm"]').addEventListener("click", () => close(true));

    document.body.appendChild(overlay);
  });
}

function formatRarity(rarity) {
  const normalized = String(rarity || "COMMON").toUpperCase();
  return {
    COMMON: "Comum",
    RARE: "Rara",
    EPIC: "Épica",
    LEGENDARY: "Lendária"
  }[normalized] || rarity || "Comum";
}

function formatDigimonType(type) {
  const normalized = String(type || "UNKNOWN").toUpperCase();
  return {
    STARTER: "Starter",
    BASIC: "Basic",
    WATER: "Water",
    FIRE: "Fire",
    EARTH: "Earth",
    WIND: "Wind",
    UNKNOWN: "Unknown"
  }[normalized] || type || "Desconhecido";
}

function formatAttribute(attribute) {
  const normalized = String(attribute || "UNKNOWN").toUpperCase();
  return {
    DATA: "Data",
    NONE: "None",
    VACCINE: "Vaccine",
    VIRUS: "Virus",
    UNKNOWN: "Unknown"
  }[normalized] || attribute || "Desconhecido";
}

function formatElement(element) {
  const normalized = String(element || "NEUTRAL").toUpperCase();
  return {
    EARTH: "Terra",
    FIRE: "Fogo",
    ICE: "Gelo",
    LIGHT: "Luz",
    NEUTRAL: "Neutro",
    PITCH_BLACK: "Negro",
    STEEL: "Metal",
    THUNDER: "Trovão",
    WATER: "Água",
    WIND: "Vento",
    WOOD: "Madeira",
    DARK: "Sombrio"
  }[normalized] || element || "Neutro";
}


function digimonStageEmoji(stage) {
  const map = { BABY: "🥒", BABY_II: "🐣", ROOKIE: "🐉", CHAMPION: "⚔️", ULTIMATE: "🔥", MEGA: "👑" };
  return map[String(stage || "").toUpperCase()] || "🐉";
}

function renderDigimonVisual(imageUrl, stage, sizeClass = "w-14 h-14", emojiClass = "text-4xl") {
  const emoji = digimonStageEmoji(stage);
  if (!imageUrl) {
    return `<div class="${sizeClass} flex items-center justify-center ${emojiClass}">${emoji}</div>`;
  }
  return `
    <div class="${sizeClass} shrink-0 rounded-xl overflow-hidden bg-slate-900/60 flex items-center justify-center">
      <img src="${escapeAttr(imageUrl)}" alt="Digimon" class="w-full h-full object-contain" onerror="this.style.display='none';this.nextElementSibling.style.display='flex'" />
      <span class="w-full h-full items-center justify-center ${emojiClass}" style="display:none">${emoji}</span>
    </div>
  `;
}

function renderRarityDieIndicator(digimon) {
  if (!digimon?.rarityChangedByDie) return "";
  const original = digimon.originalRarityBeforeDie ? formatRarity(digimon.originalRarityBeforeDie) : "não registrada";
  const date = digimon.rarityChangedByDieAt ? new Date(digimon.rarityChangedByDieAt).toLocaleDateString("pt-BR") : "data não registrada";
  return `<span class="rarity-die-indicator" title="Raridade alterada pelo Dado de Raridade. Original: ${escapeHtml(original)}. Data: ${date}. IVs e atributos originais não foram recalculados." aria-label="Raridade alterada pelo Dado de Raridade">🎲</span>`;
}

function renderRarityDieDetails(digimon) {
  if (!digimon?.rarityChangedByDie) return "";
  const original = digimon.originalRarityBeforeDie ? formatRarity(digimon.originalRarityBeforeDie) : "Não registrada";
  const date = digimon.rarityChangedByDieAt ? new Date(digimon.rarityChangedByDieAt).toLocaleString("pt-BR") : "Não registrada";
  return `<div class="rarity-die-details mt-3" role="note"><p class="font-semibold text-amber-200">🎲 Raridade alterada pelo Dado de Raridade</p><p class="mt-1">Raridade original: <strong>${escapeHtml(original)}</strong></p><p>Alterada em: <strong>${escapeHtml(date)}</strong></p><p class="mt-1 text-slate-400">Os IVs e atributos definidos no nascimento não foram recalculados.</p></div>`;
}

function rarityDieIndicator(digimon) {
  return renderRarityDieIndicator(digimon);
}
