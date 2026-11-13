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

      <section class="card mb-4">
        <h3 class="font-semibold">Carga de Energia Total</h3>
        <p class="text-sm text-slate-300 mt-2">Resgate uma unidade por dia com VIP ativo. Use no inventário para encher a barra de energia do Digimon ativo.</p>
        <p class="text-sm text-slate-400 mt-2">O resgate renova à meia-noite UTC. As cargas resgatadas ficam no inventário.</p>
        <button id="vip-energy-claim" type="button" class="btn-secondary mt-3" disabled>Resgatar Carga de Energia Total</button>
        <p id="vip-energy-status" class="text-sm text-slate-300 mt-2" aria-live="polite"></p>
      </section>

      <h3 class="font-semibold mb-2">Benefícios</h3>
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-4">
        <section class="card">
          <h4 class="font-semibold">Missões</h4>
          <p class="text-sm text-amber-200 mt-2">50% menos tempo nas missões iniciadas com VIP ativo. Uma missão de 10 minutos leva 5 minutos.</p>
          <p class="text-sm text-slate-400 mt-2">Em missões de equipe, resgate recompensas e repita missões automaticamente.</p>
        </section>
        <section class="card">
          <h4 class="font-semibold">Incubação</h4>
          <p class="text-sm text-slate-400 mt-2">Colete Digitamas chocados e reinicie incubações automaticamente quando houver recursos.</p>
        </section>
        <section class="card">
          <h4 class="font-semibold">Chefe Mundial e Incursão de Clã</h4>
          <p class="text-sm text-amber-200 mt-2">50% menos tempo entre ataques com VIP ativo. Um intervalo de 10 minutos passa a 5 minutos.</p>
        </section>
      </div>

      <section class="card">
        <h3 class="font-semibold">Custo e disponibilidade</h3>
        <p class="text-sm text-slate-400 mt-2">Preço ainda não definido. Nesta fase, o acesso VIP é concedido pela administração.</p>
      </section>
    </div>`;

  const status = document.getElementById("vip-status");
  const claimButton = document.getElementById("vip-energy-claim");
  const claimStatus = document.getElementById("vip-energy-status");
  const endpoint = "/players/me/vip/energy-charge";

  function showStatus(data) {
    const expires = new Date(data.vipExpiresAt).toLocaleString("pt-BR");
    status.textContent = data.vipActive ? `VIP ativo até ${expires}`
      : data.vipExpiresAt ? `VIP vencido em ${expires}` : "VIP inativo";
    status.classList.toggle("text-emerald-300", data.vipActive);
    status.classList.toggle("font-semibold", data.vipActive);
    claimButton.disabled = !data.vipActive || data.claimedToday;
    claimStatus.textContent = data.claimedToday ? "Carga de hoje já resgatada."
      : data.vipActive ? "Resgate disponível." : "Disponível com VIP ativo.";
  }

  claimButton.addEventListener("click", async () => {
    claimButton.disabled = true;
    try {
      const result = await apiPost(endpoint);
      if (!claimButton.isConnected) return;
      showStatus(result);
      claimStatus.textContent = "Carga de Energia Total adicionada ao inventário.";
    } catch (error) {
      if (!claimButton.isConnected) return;
      try {
        const current = await apiGet(endpoint);
        if (claimButton.isConnected) showStatus(current);
      } catch {
        if (claimButton.isConnected) claimButton.disabled = false;
      }
      if (claimButton.isConnected) claimStatus.textContent = error.message;
    }
  });

  try {
    const data = await apiGet(endpoint);
    if (status.isConnected) showStatus(data);
  } catch {
    if (status.isConnected) status.textContent = "Não foi possível consultar seu VIP.";
  }
}
