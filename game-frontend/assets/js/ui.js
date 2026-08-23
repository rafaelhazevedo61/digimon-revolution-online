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

function showBottomNav(activeRoute) {
  const nav = document.getElementById("bottom-nav");
  if (nav) {
    nav.classList.remove("hidden");
    nav.querySelectorAll(".nav-btn").forEach(btn => {
      btn.classList.remove("active");
      if (btn.dataset.route === activeRoute) btn.classList.add("active");
    });
  }
}

function showToast(message, type = "success") {
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
