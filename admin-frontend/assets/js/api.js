function getAdminToken() {
  return localStorage.getItem("dro_admin_token");
}

function setAdminToken(token) {
  localStorage.setItem("dro_admin_token", token);
}

function clearAdminAuth() {
  localStorage.removeItem("dro_admin_token");
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

function isAdminLoggedIn() {
  const token = getAdminToken();
  if (!token || isTokenExpired(token)) {
    if (token) clearAdminAuth();
    return false;
  }
  return true;
}

function adminAuthHeaders() {
  const token = getAdminToken();
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = token.startsWith("Bearer ") ? token : `Bearer ${token}`;
  return headers;
}

let authRedirectScheduled = false;

function handleAuthError(response) {
  if (response.status === 401 || response.status === 403) {
    clearAdminAuth();
    if (!authRedirectScheduled) {
      authRedirectScheduled = true;
      setTimeout(() => {
        authRedirectScheduled = false;
        showAdminLogin();
        const errorDiv = document.getElementById("admin-login-error");
        if (errorDiv) {
          errorDiv.textContent = "Sua sessão expirou. Faça login novamente.";
          errorDiv.classList.remove("hidden");
        }
      }, 0);
    }
    return true;
  }
  return false;
}

const NETWORK_ERROR_MESSAGE = "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.";

async function apiRequest(method, path, { params = {}, body, headers = {}, authenticate = true } = {}) {
  const url = new URL(`${CONFIG.API_BASE_URL}${path}`);
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      url.searchParams.append(key, value);
    }
  });

  const options = {
    method,
    headers: { ...(authenticate ? adminAuthHeaders() : { "Content-Type": "application/json" }), ...headers }
  };
  if (body !== undefined) options.body = JSON.stringify(body);

  let response;
  try {
    response = await fetch(url.toString(), options);
  } catch {
    throw new Error(NETWORK_ERROR_MESSAGE);
  }

  if (!response.ok) {
    if (handleAuthError(response)) {
      throw new Error("Sua sessão expirou. Faça login novamente.");
    }
    const message = await safelyReadError(response);
    throw new Error(message || `Erro HTTP ${response.status}`);
  }

  const text = await response.text();
  if (!text) return null;
  try { return JSON.parse(text); } catch { return text; }
}

async function apiGet(path, params = {}) {
  return apiRequest("GET", path, { params });
}

async function apiPost(path, body) {
  return apiRequest("POST", path, { body });
}

async function apiPostNoAuth(path, body) {
  return apiRequest("POST", path, { body, authenticate: false });
}

async function apiPostVoid(path, body) {
  await apiRequest("POST", path, { body });
}

async function apiPut(path, body) {
  return apiRequest("PUT", path, { body });
}

async function apiPatch(path) {
  return apiRequest("PATCH", path);
}

async function apiDelete(path) {
  return apiRequest("DELETE", path);
}

async function safelyReadError(response) {
  try {
    const data = await response.json();
    return data.message || data.error || JSON.stringify(data);
  } catch {
    return response.statusText;
  }
}