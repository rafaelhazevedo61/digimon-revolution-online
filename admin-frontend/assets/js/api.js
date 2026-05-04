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
  
  async function safelyReadError(response) {
    try {
      const data = await response.json();
      return data.message || data.error || JSON.stringify(data);
    } catch {
      return response.statusText;
    }
  }