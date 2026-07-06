function getAdminToken() {
  return localStorage.getItem("dro_admin_token");
}

function setAdminToken(token) {
  localStorage.setItem("dro_admin_token", token);
}

function clearAdminAuth() {
  localStorage.removeItem("dro_admin_token");
}

function isAdminLoggedIn() {
  return !!getAdminToken();
}

function adminAuthHeaders() {
  const token = getAdminToken();
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = token.startsWith("Bearer ") ? token : `Bearer ${token}`;
  return headers;
}

async function handleAuthError(response) {
  if (response.status === 401 || response.status === 403) {
    clearAdminAuth();
    showAdminLogin();
    throw new Error("Sessao expirada ou sem permissao. Faca login novamente.");
  }
}

async function apiGet(path, params = {}) {
  const url = new URL(`${CONFIG.API_BASE_URL}${path}`);

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      url.searchParams.append(key, value);
    }
  });

  const response = await fetch(url.toString(), { headers: adminAuthHeaders() });

  if (!response.ok) {
    await handleAuthError(response);
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }

  return response.json();
}

async function apiPost(path, body) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "POST",
    headers: adminAuthHeaders(),
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    await handleAuthError(response);
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }

  return response.json();
}

async function apiPostNoAuth(path, body) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }

  return response.json();
}

async function apiPostVoid(path) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "POST",
    headers: adminAuthHeaders()
  });

  if (!response.ok) {
    await handleAuthError(response);
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }
}

async function apiPut(path, body) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "PUT",
    headers: adminAuthHeaders(),
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    await handleAuthError(response);
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }

  return response.json();
}

async function apiPatch(path) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "PATCH",
    headers: adminAuthHeaders()
  });

  if (!response.ok) {
    await handleAuthError(response);
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }

  return response.json();
}

async function apiDelete(path) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "DELETE",
    headers: adminAuthHeaders()
  });

  if (!response.ok) {
    await handleAuthError(response);
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function safelyReadError(response) {
  try {
    const data = await response.json();
    return data.message || data.error || JSON.stringify(data);
  } catch {
    return response.statusText;
  }
}