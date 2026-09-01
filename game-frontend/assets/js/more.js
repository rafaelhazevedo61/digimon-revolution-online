async function renderMorePage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  const groups = [
    {
      label: "Combate",
      items: [
        {
          route: "bosses",
          iconImage: "assets/img/batalhadochefe.webp",
          title: "Chefes",
          desc: "Desafie chefes poderosos",
        },
        {
          route: "arena",
          icon: "⚔️",
          iconImage: "assets/img/arena.webp",
          title: "Arena",
          desc: "Duele contra outros jogadores",
        },
        {
          route: "world-boss",
          icon: "🌍",
          iconImage: "assets/img/chefe-mundial.webp",
          title: "Chefe Mundial",
          desc: "Boss compartilhado do servidor",
        },
      ],
    },
    {
      label: "Atividades",
      items: [
        {
          route: "mission-teams",
          icon: "◈",
          title: "Meus Times",
          desc: "Monte formações de até três Digimons para missões",
        },
        {
          route: "activity-calendar",
          icon: "📅",
          iconImage: "assets/img/calendario-atividades.webp",
          title: "Calendário de Atividades",
          desc: "Complete metas diárias e ganhe baús",
        },
        {
          route: "rebirth",
          iconImage: "assets/img/rebirth.webp",
          title: "Renascimento",
          desc: "Renasça seu Digimon e aprimore seus atributos",
        },
        {
          route: "forge",
          icon: "🔨",
          title: "Ferreiro",
          desc: "Refine equipamentos e prepare-se para novos desafios",
        },
      ],
    },
    {
      label: "Comunidade",
      items: [
        {
          route: "mail",
          icon: "✉️",
          iconImage: "assets/img/correio.webp",
          title: "Correio",
          badgeId: "mail-more-unread",
          desc: "Mensagens entre jogadores e comunicados",
        },
        {
          route: "clans",
          iconImage: "assets/img/cla.webp",
          title: "Clãs",
          desc: "Crie ou entre em um clã",
        },
        {
          route: "ranking",
          iconImage: "assets/img/ranking.webp",
          title: "Classificação",
          desc: "Top jogadores",
        },
      ],
    },
    {
      label: "Comércio",
      items: [
        {
          route: "auction-house",
          iconImage: "assets/img/casaleilao.webp",
          title: "Casa de Leilão",
          desc: "Compre e venda itens com jogadores",
        },
      ],
    },
    {
      label: "Digimon",
      items: [
        {
          route: "incubation",
          iconImage: "assets/img/incubacao.webp",
          title: "Incubação",
          desc: "Chocar novas digitamas",
        },
        {
          route: "storage",
          iconImage: "assets/img/armazemdigimon.webp",
          title: "Armazém Digimon",
          desc: "Gerenciar Digimons armazenados e tornar um parceiro ativo",
        },
        {
          route: "pokedex",
          iconImage: "assets/img/bibliotecadigimon.webp",
          title: "Biblioteca Digimon",
          desc: "Catálogo de todos os Digimons",
        },
        {
          route: "collection",
          icon: "📚",
          title: "Coleção",
          desc: "Registre Digimons e alcance marcos",
        },
      ],
    },
    {
      label: "Conta",
      items: [
        {
          route: "settings",
          iconClass: "more-icon-settings",
          iconImage: "assets/img/configuracoes.webp",
          title: "Configurações",
          desc: "Conta e preferências",
        },
      ],
    },
  ];

  const groupDescriptions = {
    Combate: "Desafios e confrontos para testar seu parceiro",
    Atividades: "Rotinas, progressão e recompensas especiais",
    Comunidade: "Conecte-se com jogadores e acompanhe o servidor",
    Comércio: "Negocie recursos e equipamentos",
    Digimon: "Gerencie sua coleção e evolução",
    Conta: "Preferências, acesso e segurança",
  };

  const renderItem = (item) => `
        <button class="more-menu-item" onclick="navigateTo('${item.route}')" aria-label="Abrir ${item.title}">
          <span class="more-menu-icon">
            ${item.iconImage ? `<img src="${item.iconImage}" alt="" class="more-menu-image ${item.iconClass || ""}" />` : `<span class="more-menu-emoji">${item.icon}</span>`}
          </span>
          <span class="more-menu-copy">
            <span class="more-menu-title">${item.title}${item.badgeId ? ` <span id="${item.badgeId}" class="badge hidden align-middle"></span>` : ""}</span>
            <span class="more-menu-description">${item.desc}</span>
          </span>
          <span class="more-menu-arrow" aria-hidden="true">→</span>
        </button>`;

  const renderGroup = (group, index) => `
      <section class="more-section">
        <div class="more-section-heading">
          <span class="more-section-number">${String(index + 1).padStart(2, "0")}</span>
          <div>
            <h3 class="more-section-title">${group.label}</h3>
            <p class="more-section-description">${groupDescriptions[group.label] || "Ações disponíveis"}</p>
          </div>
        </div>
        <div class="more-menu-list">
${group.items.map(renderItem).join("\n")}
        </div>
      </section>`;

  app.innerHTML = `
    <div class="page-container more-page-container">
      <header class="more-page-header">
        <div class="more-page-kicker"><span class="more-page-kicker-dot"></span> Central de navegação</div>
        <div class="more-page-heading-row">
          <div>
            <h2 class="more-page-title">Mais</h2>
            <p class="more-page-subtitle">Tudo o que você precisa para explorar o mundo de Digimon.</p>
          </div>
          <span class="more-page-count">${groups.reduce((total, group) => total + group.items.length, 0)}<small> opções</small></span>
        </div>
      </header>
      <div class="more-sections-grid">
${groups.map(renderGroup).join("\n")}
      </div>
    </div>
  `;
  moreLoadMailUnread();
}

async function moreLoadMailUnread() {
  const badge = document.getElementById("mail-more-unread");
  if (!badge) return;
  try {
    const result = await apiGet("/mail/unread-count");
    const count = Number(result.count || 0);
    badge.textContent = count > 99 ? "99+" : String(count);
    badge.classList.toggle("hidden", count === 0);
  } catch (err) {
    badge.classList.add("hidden");
  }
}
