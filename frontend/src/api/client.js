// Reads the deployed backend URL from a Vite env var at build time.
// Falls back to localhost for local development so `npm run dev` keeps working unchanged.
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

function getToken() {
  return localStorage.getItem("fabslot_token");
}

let onUnauthorizedCallback = null;

export function setOnUnauthorized(cb) {
  onUnauthorizedCallback = cb;
}

async function request(path, { method = "GET", body, auth = true } = {}) {
  const headers = { "Content-Type": "application/json" };

  if (auth) {
    const token = getToken();
    if (token) headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const contentType = res.headers.get("content-type") || "";
  const data = contentType.includes("application/json") ? await res.json() : null;

  if (res.status === 401 && auth) {
    localStorage.removeItem("fabslot_token");
    localStorage.removeItem("fabslot_username");
    localStorage.removeItem("fabslot_role");
    if (typeof onUnauthorizedCallback === "function") {
      onUnauthorizedCallback();
    } else if (window.location.pathname !== "/login" && window.location.pathname !== "/register") {
      window.location.href = "/login";
    }
  }

  if (!res.ok) {
    let message = (data && (data.message || data.error)) || `Request failed (${res.status})`;
    if (data && data.errors && typeof data.errors === "object") {
      const fieldErrors = Object.values(data.errors).join(". ");
      if (fieldErrors) {
        message = `${message}: ${fieldErrors}`;
      }
    }
    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    throw err;
  }

  return data;
}

export const api = {
  get: (path) => request(path),
  post: (path, body, opts = {}) => request(path, { method: "POST", body, ...opts }),
  put: (path, body) => request(path, { method: "PUT", body }),
  del: (path) => request(path, { method: "DELETE" }),
};
