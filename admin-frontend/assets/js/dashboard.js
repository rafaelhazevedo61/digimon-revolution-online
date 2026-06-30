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

      <div class="card mt-4" style="border: 1px solid #ef4444;">
        <h3 class="text-xl font-bold mb-2 text-red-400">Zona de Perigo</h3>
        <p class="text-slate-400 leading-relaxed mb-4">
          O wipe apaga todos os jogadores, digimons, inventários, equipamentos, missões, 
          incubações e tentativas de boss. As tabelas de conteúdo do jogo (bosses, missões, 
          equipamentos, shop, digimon infos, etc.) são mantidas.
        </p>
        <button id="btn-wipe" class="px-4 py-2 rounded font-semibold text-white"
                style="background-color: #ef4444;"
                onmouseover="this.style.backgroundColor='#dc2626'"
                onmouseout="this.style.backgroundColor='#ef4444'">
          Executar Wipe de Jogadores
        </button>
      </div>
    `;

    document.getElementById("btn-wipe").addEventListener("click", async () => {
      const confirmed = confirm(
        "ATENÇÃO: Isso irá apagar TODOS os jogadores, digimons, inventários, " +
        "equipamentos, missões, incubações e tentativas de boss.\n\n" +
        "As tabelas de conteúdo do jogo serão mantidas.\n\n" +
        "Tem certeza que deseja continuar?"
      );

      if (!confirmed) return;

      const doubleConfirm = confirm("Confirme novamente: deseja realmente executar o WIPE?");
      if (!doubleConfirm) return;

      try {
        await apiPostVoid("/admin/players/wipe");
        alert("Wipe executado com sucesso! Todos os dados de jogadores foram removidos.");
      } catch (err) {
        alert("Erro ao executar wipe: " + err.message);
      }
    });
  }