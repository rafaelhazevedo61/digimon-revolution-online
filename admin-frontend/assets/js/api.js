async function apiGet(path, params = {}) {
    const url = new URL(`${CONFIG.API_BASE_URL}${path}`);
  
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== "") {
        url.searchParams.append(key, value);
      }
    });
  
    const response = await fetch(url.toString());
  
    if (!response.ok) {
      const message = await safelyReadError(response);
      throw new Error(message || `Erro HTTP ${response.status}`);
    }
  
    return response.json();
  }

  async function apiPost(path, body) {
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

  async function apiPut(path, body) {
    const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    if (!response.ok) {
      const message = await safelyReadError(response);
      throw new Error(message || `Erro HTTP ${response.status}`);
    }

    return response.json();
  }

  async function apiPatch(path) {
    const response = await fetch(`${CONFIG.API_BASE_URL}${path}`, {
      method: "PATCH"
    });

    if (!response.ok) {
      const message = await safelyReadError(response);
      throw new Error(message || `Erro HTTP ${response.status}`);
    }

    return response.json();
  }
  
  async function safelyReadError(response) {
    try {
      const data = await response.json();
      return data.message || data.error || JSON.stringify(data);
    } catch {
      return response.statusText;
    }
  }