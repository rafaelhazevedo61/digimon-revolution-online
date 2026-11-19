const routes = {
    dashboard: renderDashboard,
    items: renderItemsPage,
    "digimon-infos": renderDigimonInfosPage,
    "evolution-lines": renderEvolutionLinesPage,
    players: renderPlayersPage,
    "alpha-invites": renderAlphaInvitesPage,
    "equipment-templates": renderEquipmentTemplatesPage,
    "shop-products": renderShopProductsPage,
    missions: renderMissionsPage,
    "loot-tables": renderLootTablesPage,
    "area-chests": renderAreaChestsPage,
    "equipment-simulator": renderEquipmentSimulatorPage,
    bosses: renderBossesAdminPage,
    "digimon-simulator": renderDigimonSimulatorPage,
    tools: renderToolsPage,
    "mail-announcements": renderMailAnnouncementsPage,
    "event-rewards": renderEventRewardsPage
  };
  
  function navigateTo(route) {
    const renderer = routes[route] || routes.dashboard;
  
    document.querySelectorAll(".nav-link").forEach(link => {
      link.classList.remove("active");
  
      if (link.dataset.route === route) {
        link.classList.add("active");
      }
    });
  
    window.location.hash = route;
    renderer();
  }
  
  function setupRouter() {
    document.querySelectorAll(".nav-link[data-route]").forEach(link => {
      link.addEventListener("click", () => {
        navigateTo(link.dataset.route);
      });
    });
  
    const initialRoute = window.location.hash.replace("#", "") || "dashboard";
    navigateTo(initialRoute);
  }