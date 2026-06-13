function showBottomNav(activeRoute) {
  const nav = document.getElementById("bottom-nav");
  if (nav) {
    nav.classList.remove("hidden");
    nav.querySelectorAll(".nav-btn").forEach(btn => {
      btn.classList.remove("active");
      if (btn.dataset.route === activeRoute) btn.classList.add("active");
    });
  }
}

function showToast(message, type = "success") {
  const existing = document.querySelector(".toast");
  if (existing) existing.remove();

  const colors = {
    success: "bg-green-900 border-green-700 text-green-200",
    error: "bg-red-900 border-red-700 text-red-200",
    info: "bg-cyan-900 border-cyan-700 text-cyan-200"
  };

  const toast = document.createElement("div");
  toast.className = `toast fixed top-4 left-1/2 -translate-x-1/2 px-4 py-2 rounded-lg border text-sm z-50 ${colors[type] || colors.info} animate-fade-in`;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add("animate-fade-out");
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}
