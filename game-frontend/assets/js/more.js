async function renderMorePage() {
  const app = document.getElementById("app");
  showBottomNav("more");

  const groups = [
    {
      label: "Combate",
      items: [
        {
          route: "activity-calendar",
          icon: "📅",
          title: "Calendário de Atividades",
          desc: "Complete metas diárias e ganhe baús",
        },
        {
          route: "bosses",
          icon: "👹",
          title: "Bosses",
          desc: "Desafie bosses poderosos",
        },
        {
          route: "arena",
          icon: "⚔️",
          title: "Arena",
          desc: "Duele contra outros jogadores",
        },
        {
          route: "world-boss",
          icon: "🌍",
          title: "Boss Mundial",
          desc: "Boss compartilhado do servidor",
        },
      ],
    },
    {
      label: "Comunidade",
      items: [
        {
          route: "mail",
          icon: "✉️",
          title: "Correio",
          badgeId: "mail-more-unread",
          desc: "Mensagens entre jogadores e comunicados",
        },
        {
          route: "clans",
          icon: "🛡️",
          title: "Clãs",
          desc: "Crie ou entre em um clã",
        },
        {
          route: "ranking",
          icon: "🏆",
          title: "Ranking",
          desc: "Top jogadores",
        },
      ],
    },
    {
      label: "Mercado",
      items: [
        {
          route: "auction-house",
          icon: "🏪",
          title: "Casa de Leilões",
          desc: "Compre e venda itens com jogadores",
        },
      ],
    },
    {
      label: "Digimon",
      items: [
        {
          route: "incubation",
          icon: "🥚",
          title: "Incubação",
          desc: "Chocar novas digitamas",
        },
        {
          route: "storage",
          icon: "📦",
          title: "Armazém Digimon",
          desc: "Gerenciar Digimons armazenados e tornar um parceiro ativo",
        },
        {
          route: "pokedex",
          icon: "📖",
          title: "Digimon Info",
          desc: "Catálogo de todos os Digimons",
        },
      ],
    },
    {
      label: "Conta",
      items: [
        {
          route: "settings",
          icon: "⚙️",
          title: "Configurações",
          desc: "Conta e preferências",
        },
      ],
    },
  ];

  const renderItem = (item) => `
        <button class="card-sm flex items-center gap-3 text-left w-full" onclick="navigateTo('${item.route}')">
          <span class="text-2xl">${item.icon}</span>
          <div class="flex-1">
            <p class="font-bold text-sm">${item.title}${item.badgeId ? ` <span id="${item.badgeId}" class="badge hidden align-middle"></span>` : ""}</p>
            <p class="text-xs text-slate-400">${item.desc}</p>
          </div>
        </button>`;

  const renderGroup = (group) => `
      <div class="mb-4">
        <p class="text-xs uppercase tracking-wider text-slate-500 font-semibold mb-2 px-1">${group.label}</p>
        <div class="flex flex-col gap-2">
${group.items.map(renderItem).join("\n")}
        </div>
      </div>`;

  app.innerHTML = `
    <div class="page-container">
      <h2 class="text-lg font-bold mb-4 px-1">Mais</h2>

${groups.map(renderGroup).join("\n")}
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
