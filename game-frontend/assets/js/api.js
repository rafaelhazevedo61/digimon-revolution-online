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

function parseJwtPayload(token) {
  try {
    const value = token.replace(/^Bearer\s+/i, "");
    const parts = value.split(".");
    if (parts.length !== 3) return null;

    let base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    base64 += "=".repeat((4 - base64.length % 4) % 4);
    const jsonPayload = decodeURIComponent(
      atob(base64).split("").map(c => `%${("00" + c.charCodeAt(0).toString(16)).slice(-2)}`).join("")
    );
    const payload = JSON.parse(jsonPayload);
    return payload && typeof payload === "object" ? payload : null;
  } catch {
    return null;
  }
}

function isTokenExpired(token) {
  const payload = parseJwtPayload(token);
  return !payload || !Number.isFinite(payload.exp) || payload.exp <= Math.floor(Date.now() / 1000);
}

function isLoggedIn() {
  const token = getToken();
  if (!token || isTokenExpired(token)) {
    if (token) clearAuth();
    return false;
  }
  return true;
}

function authHeaders() {
  const token = getToken();
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = token.startsWith("Bearer ") ? token : `Bearer ${token}`;
  return headers;
}

let authRedirectScheduled = false;

function handleAuthError(response) {
  if (response.status === 401 || response.status === 403) {
    clearAuth();
    if (!authRedirectScheduled) {
      authRedirectScheduled = true;
      setTimeout(() => {
        authRedirectScheduled = false;
        if (typeof navigateTo === "function") {
          navigateTo("login");
        } else {
          window.location.hash = "#login";
        }
        if (typeof showToast === "function") {
          showToast("Sua sessão expirou. Faça login novamente.", "error");
        }
      }, 0);
    }
    return true;
  }
  return false;
}

const NETWORK_ERROR_MESSAGE = "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.";

function isPublicAuthPath(path) {
  return path === "/auth/login" || path === "/auth/register";
}

async function apiRequest(method, path, { params = {}, body, headers = {} } = {}) {
  const url = new URL(`${CONFIG.API_BASE_URL}${path}`);
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      url.searchParams.append(key, value);
    }
  });

  const options = {
    method,
    // Login e cadastro devem funcionar mesmo com um JWT antigo, expirado
    // ou revogado armazenado no navegador.
    headers: { ...(isPublicAuthPath(path) ? { "Content-Type": "application/json" } : authHeaders()), ...headers }
  };
  if (body !== undefined && body !== null) options.body = JSON.stringify(body);

  let response;
  try {
    response = await fetch(url.toString(), options);
  } catch {
    throw new Error(NETWORK_ERROR_MESSAGE);
  }

  if (!response.ok) {
    if (handleAuthError(response)) throw new Error("Sessão expirada. Faça login novamente.");
    const msg = await safeReadError(response);
    throw new Error(msg);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function apiGet(path, params = {}) {
  return apiRequest("GET", path, { params });
}

async function apiPost(path, body = null, extraHeaders = {}) {
  return apiRequest("POST", path, { body, headers: extraHeaders });
}

async function apiPut(path, body) {
  return apiRequest("PUT", path, { body });
}

async function apiPatch(path, body) {
  return apiRequest("PATCH", path, { body });
}

async function apiDelete(path) {
  return apiRequest("DELETE", path);
}

async function safeReadError(response) {
  try {
    const data = await response.json();
    return data.message || data.error || JSON.stringify(data);
  } catch {
    return `Erro HTTP ${response.status}`;
  }
}