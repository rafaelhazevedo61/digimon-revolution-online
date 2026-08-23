const digimonInfoState = {
    page: 0,
    size: 20,
    name: "",
    stage: "",
    attribute: "",
    element: "",
    specie: "",
    lastResult: null
  };
  
  function renderDigimonInfosPage() {
    setPageHeader(
      "Digimon Infos",
      "Consulte os Digimons cadastrados, stages e stats base"
    );
  
    const app = document.getElementById("app");
  
    app.innerHTML = `
      <div class="card mb-6">
        <div class="grid grid-cols-1 md:grid-cols-6 gap-4">
  
          <div>
            <label class="text-sm text-slate-400">Nome</label>
            <input id="digimon-filter-name" class="input mt-1" placeholder="Ex: Agumon" value="${escapeAttr(digimonInfoState.name)}" />
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Stage</label>
            <select id="digimon-filter-stage" class="input mt-1">
              ${stageOptions(digimonInfoState.stage)}
            </select>
          </div>
  
          <div>
            <label class="text-sm text-slate-400">Atributo</label>
            <select id="digimon-filter-attribute" class="input mt-1">
              ${attributeOptions(digimonInfoState.attribute)}
            </select>
          </div>
  
          <div>
          <label class="text-sm text-slate-400">Elemento</label>
          <select id="digimon-filter-element" class="input mt-1">
            ${elementOptions(digimonInfoState.element)}
          </select>
        </div>
  
        <div>
        <label class="text-sm text-slate-400">Espécie</label>
        <select id="digimon-filter-specie" class="input mt-1">
          ${specieOptions(digimonInfoState.specie)}
        </select>
      </div>
  
          <div>
            <label class="text-sm text-slate-400">Itens por página</label>
            <select id="digimon-filter-size" class="input mt-1">
              ${sizeOptions(digimonInfoState.size)}
            </select>
          </div>
  
        </div>
  
        <div class="flex flex-col md:flex-row gap-3 mt-6">
          <button class="btn-primary" onclick="applyDigimonInfoFilters()">
            Buscar
          </button>
  
          <button class="btn-secondary" onclick="clearDigimonInfoFilters()">
            Limpar filtros
          </button>
        </div>
      </div>
  
      <div id="digimon-infos-result"></div>
    `;
  
    loadDigimonInfos();
  }
  
  async function loadDigimonInfos() {
    const container = document.getElementById("digimon-infos-result");
  
    container.innerHTML = `
      <div class="card">
        <p class="text-slate-400">Carregando Digimon Infos...</p>
      </div>
    `;
  
    try {
      const result = await apiGet("/digimon-infos", {
        page: digimonInfoState.page,
        size: digimonInfoState.size,
        name: digimonInfoState.name,
        stage: digimonInfoState.stage,
        attribute: digimonInfoState.attribute,
        element: digimonInfoState.element,
        specie: digimonInfoState.specie
      });
  
      digimonInfoState.lastResult = result;
      renderDigimonInfosResult(result);
    } catch (error) {
      container.innerHTML = `
        <div class="card border-red-900 bg-red-950/30">
          <h3 class="font-bold text-red-300 mb-2">Erro ao carregar Digimon Infos</h3>
          <p class="text-red-200">${escapeHtml(error.message)}</p>
          <p class="text-sm text-slate-400 mt-4">
            Verifique se o backend está rodando e se o endpoint GET /digimon-infos existe.
          </p>
        </div>
      `;
    }
  }
  
  function renderDigimonInfosResult(result) {
    const container = document.getElementById("digimon-infos-result");
    const items = result.items || [];
  
    container.innerHTML = `
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-4">
        <div>
          <h3 class="text-lg font-bold">Digimons encontrados</h3>
          <p class="text-sm text-slate-400">
            Total: ${result.totalItems ?? 0} | Página ${result.page + 1} de ${result.totalPages || 1}
          </p>
        </div>
  
        <div class="flex gap-2">
          <button class="btn-secondary" ${!result.hasPrevious ? "disabled" : ""} onclick="previousDigimonInfoPage()">
            Anterior
          </button>
  
          <button class="btn-secondary" ${!result.hasNext ? "disabled" : ""} onclick="nextDigimonInfoPage()">
            Próxima
          </button>
        </div>
      </div>
  
      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>Imagem</th>
              <th>ID</th>
              <th>Nome</th>
              <th>Stage</th>
              <th>Atributo</th>
              <th>Elemento</th>
              <th>Espécie</th>
              <th>Base HP</th>
              <th>Base ATK</th>
              <th>Base DEF</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            ${items.map(renderDigimonInfoRow).join("")}
          </tbody>
        </table>
      </div>
  
      ${items.length === 0 ? renderEmptyDigimonInfos() : ""}
    `;
  }
  
  function renderDigimonInfoRow(item) {
    return `
      <tr>
        <td>
          ${renderAdminDigimonImage(item)}
        </td>
        <td class="font-mono text-slate-400">${item.id}</td>
  
        <td>
          <div class="font-semibold text-cyan-300">${escapeHtml(item.name)}</div>
        </td>
  
        <td>
          <span class="badge">${escapeHtml(item.stage)}</span>
        </td>
  
        <td>
          <span class="badge">${item.attribute}</span>
        </td>
  
        <td>
          <span class="badge">${item.element}</span>
        </td>
  
        <td>
          <span class="badge">${escapeHtml(item.specie)}</span>
        </td>
  
        <td>${item.baseHp}</td>
        <td>${item.baseAtk}</td>
        <td>${item.baseDef}</td>
        <td>
          <button class="btn-secondary" onclick="openDigimonImageModal(${item.id})">Editar imagem</button>
        </td>
      </tr>
    `;
  }
  
  function renderEmptyDigimonInfos() {
    return `
      <div class="card mt-4">
        <p class="text-slate-400">Nenhum Digimon encontrado com os filtros atuais.</p>
      </div>
    `;
  }
  
  function applyDigimonInfoFilters() {
    digimonInfoState.name = document.getElementById("digimon-filter-name").value;
    digimonInfoState.stage = document.getElementById("digimon-filter-stage").value;
    digimonInfoState.attribute = document.getElementById("digimon-filter-attribute").value;
    digimonInfoState.element = document.getElementById("digimon-filter-element").value;
    digimonInfoState.specie = document.getElementById("digimon-filter-specie").value;
    digimonInfoState.size = Number(document.getElementById("digimon-filter-size").value);
    digimonInfoState.page = 0;
  
    loadDigimonInfos();
  }
  
  function clearDigimonInfoFilters() {
    digimonInfoState.page = 0;
    digimonInfoState.size = 20;
    digimonInfoState.name = "";
    digimonInfoState.stage = "";
    digimonInfoState.attribute = "";
    digimonInfoState.element = "";
    digimonInfoState.specie = "";
  
    renderDigimonInfosPage();
  }
  
  function previousDigimonInfoPage() {
    if (digimonInfoState.page > 0) {
      digimonInfoState.page--;
      loadDigimonInfos();
    }
  }
  
  function nextDigimonInfoPage() {
    if (digimonInfoState.lastResult?.hasNext) {
      digimonInfoState.page++;
      loadDigimonInfos();
    }
  }
  
  function stageOptions(selectedValue) {
    const options = [
      { label: "Todos", value: "" },
      { label: "Baby", value: "BABY" },
      { label: "Baby II", value: "BABY_II" },
      { label: "Rookie", value: "ROOKIE" },
      { label: "Champion", value: "CHAMPION" },
      { label: "Ultimate", value: "ULTIMATE" },
      { label: "Mega", value: "MEGA" }
    ];
  
    return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
  }
  
  function attributeOptions(selectedValue) {
    const options = [
      { label: "Todos", value: "" },
      { label: "Vaccine", value: "VACCINE" },
      { label: "Data", value: "DATA" },
      { label: "Virus", value: "VIRUS" },
      { label: "Free", value: "FREE" },
      { label: "Unknown", value: "UNKNOWN" }
    ];
  
    return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
  }

  function elementOptions(selectedValue) {
    const options = [
      { label: "Todos", value: "" },
      { label: "Fogo", value: "FIRE" },
      { label: "Água", value: "WATER" },
      { label: "Planta", value: "PLANT" },
      { label: "Terra", value: "EARTH" },
      { label: "Vento", value: "WIND" },
      { label: "Luz", value: "LIGHT" },
      { label: "Trevas", value: "DARK" },
      { label: "Trovão", value: "THUNDER" },
      { label: "Neutro", value: "NEUTRAL" },
      { label: "Gelo", value: "ICE" },
      { label: "Metal", value: "METAL" }
    ];
  
    return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
  }
  
  function specieOptions(selectedValue) {
    const options = [
      { label: "Todos", value: "" },
      { label: "Dragão", value: "DRAGON" },
      { label: "Besta", value: "BEAST" },
      { label: "Pássaro", value: "BIRD" },
      { label: "Inseto", value: "INSECT" },
      { label: "Aquático", value: "AQUATIC" },
      { label: "Máquina", value: "MACHINE" },
      { label: "Demônio", value: "DEMON" },
      { label: "Anjo", value: "ANGEL" },
      { label: "Planta", value: "PLANT" },
      { label: "Humanoide", value: "HUMANOID" },
      { label: "Mutante", value: "MUTANT" },
      { label: "Sagrado", value: "HOLY" },
      { label: "Sombrio", value: "DARK" },
      { label: "Desconhecido", value: "UNKNOWN" }
    ];
  
    return options.map(option => `
      <option value="${option.value}" ${String(selectedValue) === option.value ? "selected" : ""}>
        ${option.label}
      </option>
    `).join("");
  }

function renderAdminDigimonImage(item) {
  const fallback = `<div class="w-12 h-12 rounded-lg flex items-center justify-center text-xl bg-slate-800 border border-slate-700">🐉</div>`;
  if (!item.imageUrl) return fallback;
  return `
    <div class="w-12 h-12 rounded-lg overflow-hidden bg-slate-800 border border-slate-700 flex items-center justify-center">
      <img src="${escapeAttr(item.imageUrl)}" alt="${escapeAttr(item.name)}" class="w-full h-full object-contain" onerror="this.style.display='none';this.nextElementSibling.style.display='flex'" />
      <span class="w-full h-full items-center justify-center text-xl" style="display:none">🐉</span>
    </div>
  `;
}

function openDigimonImageModal(id) {
  const item = (digimonInfoState.lastResult?.items || []).find(entry => Number(entry.id) === Number(id));
  if (!item) return;

  const overlay = document.createElement("div");
  overlay.id = "digimon-image-modal";
  overlay.className = "modal-overlay";
  overlay.onclick = event => { if (event.target === overlay) closeDigimonImageModal(); };
  overlay.innerHTML = `
    <div class="modal-content max-w-2xl">
      <div class="flex items-center justify-between gap-4 mb-5">
        <div>
          <h3 class="text-xl font-bold">Imagem de ${escapeHtml(item.name)}</h3>
          <p class="text-sm text-slate-400 mt-1">Informe uma URL externa ou um caminho de asset do projeto.</p>
        </div>
        <button class="btn-secondary" onclick="closeDigimonImageModal()">Fechar</button>
      </div>

      <label class="text-sm text-slate-400">URL / caminho da imagem</label>
      <input id="digimon-image-url" class="input mt-1" maxlength="1000" value="${escapeAttr(item.imageUrl || '')}" placeholder="https://... ou /assets/img/digimons/agumon.png" oninput="previewDigimonImage()" />

      <div class="mt-4">
        <p class="text-sm text-slate-400 mb-2">Pré-visualização</p>
        <div id="digimon-image-preview" class="h-48 rounded-xl border border-slate-700 bg-slate-950 flex items-center justify-center overflow-hidden"></div>
      </div>

      <div id="digimon-image-error" class="hidden mt-4 p-3 rounded-lg border border-red-900 bg-red-950/30 text-red-300 text-sm"></div>

      <div class="flex flex-wrap justify-end gap-2 mt-5">
        <button class="btn-secondary" onclick="removeDigimonImage(${item.id})">Remover imagem</button>
        <button class="btn-primary" onclick="saveDigimonImage(${item.id})">Salvar</button>
      </div>
    </div>
  `;
  document.body.appendChild(overlay);
  previewDigimonImage();
}

function previewDigimonImage() {
  const input = document.getElementById("digimon-image-url");
  const preview = document.getElementById("digimon-image-preview");
  if (!input || !preview) return;
  const url = input.value.trim();
  if (!url) {
    preview.innerHTML = `<span class="text-5xl">🐉</span>`;
    return;
  }
  preview.innerHTML = `<img src="${escapeAttr(url)}" alt="Pré-visualização" class="max-w-full max-h-full object-contain" onerror="this.style.display='none';this.nextElementSibling.style.display='block'" /><span class="text-sm text-red-300" style="display:none">Não foi possível carregar esta imagem.</span>`;
}

async function saveDigimonImage(id) {
  const input = document.getElementById("digimon-image-url");
  const error = document.getElementById("digimon-image-error");
  try {
    await apiPut(`/admin/digimon-infos/${encodeURIComponent(id)}/image`, { imageUrl: input.value.trim() || null });
    closeDigimonImageModal();
    await loadDigimonInfos();
  } catch (err) {
    error.textContent = err.message;
    error.classList.remove("hidden");
  }
}

async function removeDigimonImage(id) {
  const input = document.getElementById("digimon-image-url");
  if (input) input.value = "";
  await saveDigimonImage(id);
}

function closeDigimonImageModal() {
  document.getElementById("digimon-image-modal")?.remove();
}
