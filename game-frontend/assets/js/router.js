const routes = {
  login: renderLoginPage,
  starter: renderStarterPage,
  dashboard: renderDashboardPage,
  "activity-calendar": renderActivityCalendarPage,
  missions: renderMissionsPage,
  "mission-area": renderMissionAreaPage,
  "mission-teams": renderMissionTeamsPage,
  "digimon-select": renderDigimonSelectPage,
  shop: renderShopPage,
  forge: renderForgePage,
  "auction-house": renderAuctionHousePage,
  mail: renderMailPage,
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
  "arena-shop": renderArenaShopPage,
  storage: renderStoragePage,
  collection: renderCollectionPage,
  clans: renderClansPage,
  "clan-ranking": renderClanRanking,
  "world-boss": renderWorldBossPage,
  more: renderMorePage,
  settings: renderSettingsPage
};

function navigateTo(route, params = {}) {
  if (route !== "login" && !isLoggedIn()) {
    route = "login";
    params = {};
  }

  const query = new URLSearchParams(params).toString();
  const targetHash = query ? `${route}?${query}` : route;
  const currentHash = window.location.hash.replace("#", "");

  window._routeParams = params;

  if (currentHash !== targetHash) {
    window.location.hash = targetHash;
    return;
  }

  const renderer = routes[route] || routes.dashboard;
  renderer(params);
}

async function refreshCurrentPage() {
  const rawHash = window.location.hash.replace("#", "");
  const [route, queryString] = rawHash.split("?");
  const params = Object.fromEntries(new URLSearchParams(queryString || ""));
  const renderer = routes[route] || routes.dashboard;
  window._routeParams = params;
  await renderer(params);
}

function setupRouter() {
  const rawHash = window.location.hash.replace("#", "");

  if (!rawHash) {
    navigateTo(isLoggedIn() ? "dashboard" : "login");
    return;
  }

  const [route, queryString] = rawHash.split("?");

  const params = Object.fromEntries(
    new URLSearchParams(queryString || "")
  );

  window._routeParams = params;

  if (route !== "login" && !isLoggedIn()) {
    navigateTo("login");
    return;
  }

  const renderer = routes[route] || routes.dashboard;
  renderer(params);
}

function updateNavActive(route) {
  document.querySelectorAll(".nav-btn").forEach(btn => {
    btn.classList.remove("active");

    if (btn.dataset.route === route) {
      btn.classList.add("active");
    }
  });
}
