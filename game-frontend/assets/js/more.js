async function renderMorePage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">Mais</h2>

      <div class="flex flex-col gap-2">
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('bosses')">
          <span class="text-2xl">👹</span>
          <div>
            <p class="font-bold text-sm">Bosses</p>
            <p class="text-xs text-slate-400">Desafie bosses poderosos</p>
          </div>
        </button>
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('arena')">
          <span class="text-2xl">⚔️</span>
          <div>
            <p class="font-bold text-sm">Arena</p>
            <p class="text-xs text-slate-400">Duele contra outros jogadores</p>
          </div>
        </button>
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('ranking')">
          <span class="text-2xl">🏆</span>
          <div>
            <p class="font-bold text-sm">Ranking</p>
            <p class="text-xs text-slate-400">Top jogadores</p>
          </div>
        </button>
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('clans')">
          <span class="text-2xl">🛡️</span>
          <div>
            <p class="font-bold text-sm">Clãs</p>
            <p class="text-xs text-slate-400">Crie ou entre em um clã</p>
          </div>
        </button>
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('incubation')">
          <span class="text-2xl">🥚</span>
          <div>
            <p class="font-bold text-sm">Incubação</p>
            <p class="text-xs text-slate-400">Chocar novas digitamas</p>
          </div>
        </button>
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('storage')">
          <span class="text-2xl">📦</span>
          <div>
            <p class="font-bold text-sm">Storage</p>
            <p class="text-xs text-slate-400">Guardar e retirar Digimons</p>
          </div>
        </button>
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('pokedex')">
          <span class="text-2xl">📖</span>
          <div>
            <p class="font-bold text-sm">Digimon Info</p>
            <p class="text-xs text-slate-400">Catálogo de todos os Digimons</p>
          </div>
        </button>
        <button class="card-sm flex items-center gap-3 text-left w-full opacity-50" disabled>
          <span class="text-2xl">⚙️</span>
          <div>
            <p class="font-bold text-sm">Configurações</p>
            <p class="text-xs text-slate-400">Conta e preferências — em breve</p>
          </div>
        </button>
      </div>
    </div>
  `;
}
