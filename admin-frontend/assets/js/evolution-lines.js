const evolutionLineState = {
    page: 0,
    size: 20,
    code: "",
    name: "",
    active: "",
    lastResult: null
  };
  
  function renderEvolutionLinesPage() {
    setPageHeader(
      "Linhas Evolutivas",
      "Valide as cadeias evolutivas cadastradas no jogo"
    );
  
    const app = document.getElementById("app");
  
    app.innerHTML = `
      <div class="card mb-6">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
  
          <div>
            <label class="text-sm text-slate-400">Código</label>
            <input id="evolution-filter-code" class="input mt-1" placeholder="Ex: AGUMON" value="${evolutionLineState.code}" />
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Nome</label>
            <input id="evolution-filter-name" class="input mt-1" placeholder="Ex: Agumon" value="${evolutionLineState.name}" />
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Ativa</label>
            <select id="evolution-filter-active" class="input mt-1">
              ${booleanOptions(evolutionLineState.active)}
            </select>
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Itens por página</label>
            <select id="evolution-filter-size" class="input mt-1">
              ${sizeOptions(evolutionLineState.size)}
            </select>
          </div>
  
        </div>
  
        <div class="flex flex-col md:flex-row gap-3 mt-6">
          <button class="btn-primary" onclick="applyEvolutionLineFilters()">
            Buscar
          </button>
  
          <button class="btn-secondary" onclick="clearEvolutionLineFilters()">
            Limpar filtros
          </button>
        </div>
      </div>
  
      <div id="evolution-lines-result"></div>
    `;
  
    loadEvolutionLines();
  }
  
  async function loadEvolutionLines() {
    const container = document.getElementById("evolution-lines-result");
  
    container.innerHTML = `
      <div class="card">
        <p class="text-slate-400">Carregando linhas evolutivas...</p>
      </div>
    `;
  
    try {
      const result = await apiGet("/evolution-lines", {
        page: evolutionLineState.page,
        size: evolutionLineState.size,
        code: evolutionLineState.code,
        name: evolutionLineState.name,
        active: evolutionLineState.active
      });
  
      evolutionLineState.lastResult = result;
      renderEvolutionLinesResult(result);
    } catch (error) {
      container.innerHTML = `
        <div class="card border-red-900 bg-red-950/30">
          <h3 class="font-bold text-red-300 mb-2">Erro ao carregar linhas evolutivas</h3>
          <p class="text-red-200">${error.message}</p>
          <p class="text-sm text-slate-400 mt-4">
            Verifique se o backend está rodando e se o endpoint GET /evolution-lines existe.
          </p>
        </div>
      `;
    }
  }
  
  function renderEvolutionLinesResult(result) {
    const container = document.getElementById("evolution-lines-result");
    const items = result.items || [];
  
    container.innerHTML = `
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-4">
        <div>
          <h3 class="text-lg font-bold">Linhas encontradas</h3>
          <p class="text-sm text-slate-400">
            Total: ${result.totalItems ?? 0} | Página ${result.page + 1} de ${result.totalPages || 1}
          </p>
        </div>
  
        <div class="flex gap-2">
          <button class="btn-secondary" ${!result.hasPrevious ? "disabled" : ""} onclick="previousEvolutionLinePage()">
            Anterior
          </button>
  
          <button class="btn-secondary" ${!result.hasNext ? "disabled" : ""} onclick="nextEvolutionLinePage()">
            Próxima
          </button>
        </div>
      </div>
  
      <div class="space-y-4">
        ${items.map(renderEvolutionLineCard).join("")}
      </div>
  
      ${items.length === 0 ? renderEmptyEvolutionLines() : ""}
    `;
  }
  
  function renderEvolutionLineCard(line) {
    return `
      <div class="card">
        <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-3 mb-5">
          <div>
            <div class="flex flex-wrap gap-2 mb-2">
              <span class="badge">${line.code}</span>
              ${line.active ? `<span class="badge">Ativa</span>` : `<span class="badge">Inativa</span>`}
            </div>
  
            <h3 class="text-xl font-bold text-cyan-300">${line.name}</h3>
            <p class="text-sm text-slate-500">ID ${line.id}</p>
          </div>
  
          <div class="text-sm text-slate-400">
            ${line.steps?.length || 0} steps cadastrados
          </div>
        </div>
  
        <div class="overflow-x-auto">
          <div class="flex items-center gap-3 min-w-max pb-2">
            ${(line.steps || []).map(renderEvolutionStep).join(renderArrow())}
          </div>
        </div>
      </div>
    `;
  }
  
  function renderEvolutionStep(step) {
    return `
      <div class="bg-slate-950 border border-slate-800 rounded-xl p-4 min-w-64">
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs text-slate-500">Step ${step.stepOrder}</span>
          <span class="badge">${step.stage}</span>
        </div>
  
        <h4 class="font-bold text-slate-100">${step.digimonName}</h4>
  
        <p class="text-xs text-slate-500 mt-1">
          DigimonInfo ID ${step.digimonInfoId}
        </p>
  
        <div class="mt-4 pt-3 border-t border-slate-800">
          <p class="text-xs font-semibold text-slate-400 mb-2">
            Requisitos para este step
          </p>
  
          <p class="text-xs text-slate-300">
            Level necessário:
            <strong class="text-cyan-300">${step.requiredLevel ?? "-"}</strong>
          </p>
  
          ${renderStepMaterials(step.materials)}
        </div>
      </div>
    `;
  }

  function renderStepMaterials(materials) {
    if (!materials || materials.length === 0) {
      return `
        <p class="text-xs text-slate-500 mt-2">
          Nenhum material necessário
        </p>
      `;
    }
  
    return `
      <div class="mt-2 space-y-1">
        <p class="text-xs text-slate-400">
          Materiais necessários:
        </p>
  
        ${materials.map(material => `
          <div class="text-xs text-slate-300 bg-slate-900 border border-slate-800 rounded-lg px-2 py-1">
            <div class="font-semibold text-slate-200">
              ${material.itemName || "Item não encontrado"}
            </div>
  
            <div class="text-slate-500 font-mono">
              ${material.itemCode || "-"}
            </div>
  
            <div class="text-cyan-300 font-bold">
              Quantidade: x${material.quantity ?? 0}
            </div>
  
            ${material.itemDefinitionId === null || material.itemDefinitionId === undefined ? `
              <div class="text-yellow-300 mt-1">
                Atenção: item_definition não encontrado
              </div>
            ` : ""}
          </div>
        `).join("")}
      </div>
    `;
  }
  
  function renderArrow() {
    return `
      <div class="text-slate-600 font-bold text-xl">
        →
      </div>
    `;
  }
  
  function renderEmptyEvolutionLines() {
    return `
      <div class="card mt-4">
        <p class="text-slate-400">Nenhuma linha evolutiva encontrada com os filtros atuais.</p>
      </div>
    `;
  }
  
  function applyEvolutionLineFilters() {
    evolutionLineState.code = document.getElementById("evolution-filter-code").value;
    evolutionLineState.name = document.getElementById("evolution-filter-name").value;
    evolutionLineState.active = document.getElementById("evolution-filter-active").value;
    evolutionLineState.size = Number(document.getElementById("evolution-filter-size").value);
    evolutionLineState.page = 0;
  
    loadEvolutionLines();
  }
  
  function clearEvolutionLineFilters() {
    evolutionLineState.page = 0;
    evolutionLineState.size = 20;
    evolutionLineState.code = "";
    evolutionLineState.name = "";
    evolutionLineState.active = "";
  
    renderEvolutionLinesPage();
  }
  
  function previousEvolutionLinePage() {
    if (evolutionLineState.page > 0) {
      evolutionLineState.page--;
      loadEvolutionLines();
    }
  }
  
  function nextEvolutionLinePage() {
    if (evolutionLineState.lastResult?.hasNext) {
      evolutionLineState.page++;
      loadEvolutionLines();
    }
  }