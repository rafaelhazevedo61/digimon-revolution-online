const routes = {
  login: renderLoginPage,
  starter: renderStarterPage,
  dashboard: renderDashboardPage,
  missions: renderMissionsPage,
  "mission-area": renderMissionAreaPage,
  "digimon-select": renderDigimonSelectPage,
  shop: renderShopPage,
  inventory: renderInventoryPage,
  evolution: renderEvolutionPage,
  rebirth: renderRebirthPage,
  ranking: renderRankingPage,
  incubation: renderIncubationPage,
  pokedex: renderPokedexPage,
  bosses: renderBossesPage,
  "boss-history": renderBossHistoryPage,
  arena: renderArenaPage,
  "arena-ranking": renderArenaRankingPage,
  "arena-history": renderArenaHistoryPage,
  storage: renderStoragePage,
  more: renderMorePage
};

function navigateTo(route, params = {}) {
  window._routeParams = params;

  if (route !== "login" && !isLoggedIn()) {
    route = "login";
  }

  const renderer = routes[route] || routes.dashboard;
  window.location.hash = route;
  renderer(params);
}

function setupRouter() {
  const hash = window.location.hash.replace("#", "") || (isLoggedIn() ? "dashboard" : "login");
  navigateTo(hash);
}

function updateNavActive(route) {
  document.querySelectorAll(".nav-btn").forEach(btn => {
    btn.classList.remove("active");
    if (btn.dataset.route === route) btn.classList.add("active");
  });
}
