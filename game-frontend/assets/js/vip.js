async function renderVipPage() {
  const app = document.getElementById("app");
  showBottomNav("more");
  app.innerHTML = `
    <div class="page-container max-w-3xl mx-auto">
      <header class="flex flex-wrap items-center justify-between gap-3 mb-5">
        <h2 class="progression-page-title">VIP</h2>
        <button type="button" class="btn-secondary" onclick="navigateTo('more')">Voltar</button>
      </header>

      <section class="card border-amber-500/30 bg-amber-950/20 mb-4">
        <h3 class="font-semibold text-amber-200">Seu VIP</h3>
        <p id="vip-status" class="text-sm text-slate-300 mt-2" aria-live="polite">Consultando status…</p>
      </section>

      <h3 class="font-semibold mb-2">Benefícios</h3>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-4">
        <section class="card">
          <h4 class="font-semibold">Missões de equipe</h4>
          <p class="text-sm text-slate-400 mt-2">Resgate recompensas e repita missões automaticamente.</p>
        </section>
        <section class="card">
          <h4 class="font-semibold">Incubação</h4>
          <p class="text-sm text-slate-400 mt-2">Colete Digitamas chocados e reinicie incubações automaticamente quando houver recursos.</p>
        </section>
      </div>

      <section class="card">
        <h3 class="font-semibold">Custo e disponibilidade</h3>
        <p class="text-sm text-slate-400 mt-2">Preço ainda não definido. Nesta fase, o acesso VIP é concedido pela administração.</p>
      </section>
    </div>`;

  const status = document.getElementById("vip-status");
  try {
    const player = await apiGet("/players/me");
    if (!status.isConnected) return;
    const expires = new Date(player.vipExpiresAt);
    if (!player.vipExpiresAt) {
      status.textContent = "VIP inativo";
    } else if (expires.getTime() > Date.now()) {
      status.textContent = `VIP ativo até ${expires.toLocaleString("pt-BR")}`;
      status.classList.add("text-emerald-300", "font-semibold");
    } else {
      status.textContent = `VIP vencido em ${expires.toLocaleString("pt-BR")}`;
    }
  } catch {
    if (status.isConnected) status.textContent = "Não foi possível consultar seu VIP.";
  }
}
