function renderDashboard() {
    setPageHeader(
      "Dashboard",
      "Visão geral administrativa do Digimon Revolution Online"
    );
  
    const app = document.getElementById("app");
  
    app.innerHTML = `
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
        <div class="card">
          <p class="text-sm text-slate-400">Jogadores</p>
          <h3 class="text-3xl font-bold mt-2">--</h3>
          <p class="text-xs text-slate-500 mt-2">Disponível em etapa futura</p>
        </div>
  
        <div class="card">
          <p class="text-sm text-slate-400">Digimons</p>
          <h3 class="text-3xl font-bold mt-2">--</h3>
          <p class="text-xs text-slate-500 mt-2">Disponível em etapa futura</p>
        </div>
  
        <div class="card">
          <p class="text-sm text-slate-400">Itens cadastrados</p>
          <h3 class="text-3xl font-bold mt-2">--</h3>
          <p class="text-xs text-slate-500 mt-2">Consulte na tela de catálogo</p>
        </div>
      </div>
  
      <div class="card">
        <h3 class="text-xl font-bold mb-2">Admin MVP</h3>
        <p class="text-slate-400 leading-relaxed">
          Este painel administrativo será usado para validar os dados do jogo,
          consultar catálogos, visualizar jogadores, Digimons, inventários e linhas evolutivas.
        </p>
  
        <button class="btn-primary mt-6" onclick="navigateTo('items')">
          Ver catálogo de itens
        </button>
      </div>
    `;
  }