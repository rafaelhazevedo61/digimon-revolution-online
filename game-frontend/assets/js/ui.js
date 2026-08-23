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
