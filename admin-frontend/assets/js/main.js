document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("api-status").textContent = CONFIG.API_BASE_URL;

  if (isAdminLoggedIn()) {
    showAdminPanel();
  } else {
    showAdminLogin();
  }
});

function setPageHeader(title, subtitle) {
  document.getElementById("page-title").textContent = title;
  document.getElementById("page-subtitle").textContent = subtitle;
}
