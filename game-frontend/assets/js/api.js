function getToken() {
  return localStorage.getItem("dro_token");
}

function setToken(token) {
  localStorage.setItem("dro_token", token);
}

function setPlayerId(id) {
  localStorage.setItem("dro_player_id", id);
}

function getPlayerId() {
  return localStorage.getItem("dro_player_id");
}

function clearAuth() {
  localStorage.removeItem("dro_token");
  localStorage.removeItem("dro_player_id");
}

function isLoggedIn() {
  return !!getToken();
}

function authHeaders() {
  const token = getToken();
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = token.startsWith("Bearer ") ? token : `Bearer ${token}`;
  return headers;
}

function handleAuthError(response) {
  if (response.status === 401 || response.status === 403) {
    clearAuth();
    if (typeof navigateTo === "function") {
      navigateTo("login");
    } else {
      window.location.hash = "#login";
    }
    return true;
  }
  return false;
}

async function apiGet(path, params = {}) {
  const url = new URL(`${CONFIG.API_BASE_URL}${path}`);
  Object.entries(params).forEach(([k, v]) => {
    if (v !== null && v !== undefined && v !== "") url.searchParams.append(k, v);
  });

  const response = await fetch(url.toString(), { headers: authHeaders() });

  if (!response.ok) {
    if (handleAuthError(response)) throw new Error("Sessão expirada. Faça login novamente.");
    const msg = await safeReadError(response);
    throw new Error(msg);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function apiPost(path, body = null) {
  const opts = { method: "POST", headers: authHeaders() };
  if (body !== null) opts.body = JSON.stringify(body);

  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, opts);

  if (!response.ok) {
    if (handleAuthError(response)) throw new Error("Sessão expirada. Faça login novamente.");
    const msg = await safeReadError(response);
    throw new Error(msg);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function apiPut(path, body) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "PUT",
    headers: authHeaders(),
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    if (handleAuthError(response)) throw new Error("Sessão expirada. Faça login novamente.");
    const msg = await safeReadError(response);
    throw new Error(msg);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function apiPatch(path, body) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "PATCH",
    headers: authHeaders(),
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    if (handleAuthError(response)) throw new Error("Sessão expirada. Faça login novamente.");
    const msg = await safeReadError(response);
    throw new Error(msg);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function apiDelete(path) {
  const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
    method: "DELETE",
    headers: authHeaders()
  });

  if (!response.ok) {
    if (handleAuthError(response)) throw new Error("Sessão expirada. Faça login novamente.");
    const msg = await safeReadError(response);
    throw new Error(msg);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function safeReadError(response) {
  try {
    const data = await response.json();
    return data.message || data.error || JSON.stringify(data);
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}