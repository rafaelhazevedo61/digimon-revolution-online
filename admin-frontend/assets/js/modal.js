function openConfirmModal({
  title = "Confirmar ação",
  message = "Deseja continuar?",
  confirmText = "Confirmar",
  cancelText = "Cancelar",
  onConfirm
}) {
  const modal = document.getElementById("confirm-modal");
  const titleElement = document.getElementById("confirm-modal-title");
  const messageElement = document.getElementById("confirm-modal-message");
  const confirmButton = document.getElementById("confirm-modal-confirm");
  const cancelButton = document.getElementById("confirm-modal-cancel");

  titleElement.textContent = title;
  messageElement.textContent = message;
  confirmButton.textContent = confirmText;
  cancelButton.textContent = cancelText;

  modal.classList.remove("hidden");
  modal.classList.add("flex");

  const closeModal = () => {
    modal.classList.add("hidden");
    modal.classList.remove("flex");

    confirmButton.onclick = null;
    cancelButton.onclick = null;
  };

  cancelButton.onclick = closeModal;

  confirmButton.onclick = async () => {
    try {
      confirmButton.disabled = true;
      confirmButton.textContent = "Processando...";

      await onConfirm();

      closeModal();
    } finally {
      confirmButton.disabled = false;
      confirmButton.textContent = confirmText;
    }
  };
}