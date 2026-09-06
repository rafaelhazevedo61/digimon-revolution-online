const invUseItemBase = invUseItem;

function invIsStorageBatchItem(itemType) {
  return ["STORAGE_SLOT_1", "STORAGE_SLOT_5", "STORAGE_SLOT_10"].includes(String(itemType || ""));
}

function invStorageExpansionPerItem(itemType) {
  return {
    STORAGE_SLOT_1: 1,
    STORAGE_SLOT_5: 5,
    STORAGE_SLOT_10: 10
  }[itemType] || 0;
}

function invStorageAvailableQuantity(itemType) {
  const source = Array.isArray(invItems) ? invItems : [];
  return source
    .filter(item => item.itemType === itemType)
    .reduce((total, item) => total + (Number(item.quantity) || 0), 0);
}

function invCloseStorageBatchModal() {
  document.getElementById("inventory-storage-batch-modal")?.remove();
}

function invOpenStorageBatchModal(itemType) {
  invCloseStorageBatchModal();

  const available = Math.max(1, Math.min(999, invStorageAvailableQuantity(itemType)));
  const expansionPerItem = invStorageExpansionPerItem(itemType);
  const overlay = document.createElement("div");
  overlay.id = "inventory-storage-batch-modal";
  overlay.className = "fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/80";
  overlay.setAttribute("role", "dialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.setAttribute("aria-labelledby", "inventory-storage-batch-title");

  overlay.innerHTML = `
    <div class="card w-full max-w-md" onclick="event.stopPropagation()">
      <div class="flex items-start justify-between gap-3">
        <div>
          <p class="text-xs uppercase tracking-wider text-cyan-300 font-bold">Expansão de Storage</p>
          <h3 id="inventory-storage-batch-title" class="text-xl font-bold mt-1">Usar vários itens</h3>
          <p class="text-sm text-slate-400 mt-1">${escapeHtml(invItemName(itemType))} · ${available} disponível(is)</p>
        </div>
        <button type="button" class="text-slate-400 hover:text-white text-2xl leading-none" aria-label="Fechar" onclick="invCloseStorageBatchModal()">&times;</button>
      </div>

      <div class="mt-5 rounded-xl border border-cyan-900/60 bg-cyan-950/20 p-4">
        <label class="block text-xs font-bold uppercase tracking-wider text-slate-400" for="inventory-storage-batch-quantity">Quantidade</label>
        <div class="mt-2 flex items-center gap-2">
          <input id="inventory-storage-batch-quantity" class="input flex-1 text-center" type="number" min="1" max="${available}" value="1" />
          <button type="button" class="btn-sm btn-secondary" onclick="document.getElementById('inventory-storage-batch-quantity').value = ${available}; invUpdateStorageBatchPreview('${itemType}')">Máx.</button>
        </div>
        <p id="inventory-storage-batch-preview" class="mt-3 text-sm text-cyan-200">Expansão total: +${expansionPerItem} Storage</p>
      </div>

      <div class="mt-5 flex gap-2">
        <button type="button" class="btn-secondary flex-1" onclick="invCloseStorageBatchModal()">Cancelar</button>
        <button id="inventory-storage-batch-confirm" type="button" class="btn-primary flex-1">Usar</button>
      </div>
    </div>
  `;

  overlay.addEventListener("click", event => {
    if (event.target === overlay) invCloseStorageBatchModal();
  });

  document.body.appendChild(overlay);

  const input = overlay.querySelector("#inventory-storage-batch-quantity");
  input?.addEventListener("input", () => invUpdateStorageBatchPreview(itemType));
  input?.focus();

  overlay.querySelector("#inventory-storage-batch-confirm")?.addEventListener("click", () => {
    invSubmitStorageBatchUse(itemType);
  });
}

function invUpdateStorageBatchPreview(itemType) {
  const input = document.getElementById("inventory-storage-batch-quantity");
  const preview = document.getElementById("inventory-storage-batch-preview");
  if (!input || !preview) return;
  const available = Math.max(1, Math.min(999, invStorageAvailableQuantity(itemType)));
  const quantity = Math.max(1, Math.min(available, Number.parseInt(input.value, 10) || 1));
  const totalExpansion = quantity * invStorageExpansionPerItem(itemType);
  preview.textContent = `Expansão total: +${totalExpansion} Storage`;
}

async function invSubmitStorageBatchUse(itemType, explicitQuantity = null) {
  const input = document.getElementById("inventory-storage-batch-quantity");
  const confirmButton = document.getElementById("inventory-storage-batch-confirm");
  const available = Math.max(1, Math.min(999, invStorageAvailableQuantity(itemType)));
  const quantity = explicitQuantity == null
    ? Number.parseInt(input?.value, 10)
    : Number.parseInt(explicitQuantity, 10);

  if (!Number.isInteger(quantity) || quantity < 1 || quantity > available) {
    showToast(`Informe uma quantidade válida entre 1 e ${available}.`, "error");
    return;
  }
  if (invItemUseInProgress) return;

  invItemUseInProgress = true;
  if (confirmButton) {
    confirmButton.disabled = true;
    confirmButton.textContent = "Usando...";
  }

  try {
    const result = await apiPost("/inventory/use", { itemType, quantity });
    invCloseStorageBatchModal();
    showToast(result?.message || `${quantity} expansor(es) utilizado(s)!`);
    await invReloadItems();
  } catch (err) {
    showToast(err.message, "error");
    if (confirmButton) {
      confirmButton.disabled = false;
      confirmButton.textContent = "Usar";
    }
  } finally {
    invItemUseInProgress = false;
  }
}

invUseItem = async function(itemType, quantity = null) {
  if (!invIsStorageBatchItem(itemType)) {
    return invUseItemBase(itemType, quantity);
  }

  if (quantity != null) {
    return invSubmitStorageBatchUse(itemType, quantity);
  }

  invOpenStorageBatchModal(itemType);
};
