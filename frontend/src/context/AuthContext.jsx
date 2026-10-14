import React, { createContext, useContext, useEffect, useState } from "react";
import { api, setOnUnauthorized } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("fabslot_token"));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore session on app load by calling /auth/me
  useEffect(() => {
    let isMounted = true;

    const restoreSession = async () => {
      const storedToken = localStorage.getItem("fabslot_token");
      if (!storedToken) {
        if (isMounted) setLoading(false);
        return;
      }

      try {
        const userData = await api.get("/auth/me");
        if (isMounted) {
          setUser(userData);
          setToken(storedToken);
        }
      } catch (err) {
        if (isMounted) {
          localStorage.removeItem("fabslot_token");
          localStorage.removeItem("fabslot_username");
          localStorage.removeItem("fabslot_role");
          setToken(null);
          setUser(null);
        }
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    restoreSession();

    setOnUnauthorized(() => {
      if (isMounted) {
        setUser(null);
        setToken(null);
      }
    });

    return () => {
      isMounted = false;
    };
  }, []);

  const login = async (username, password) => {
    const data = await api.post("/auth/login", { username, password }, { auth: false });
    localStorage.setItem("fabslot_token", data.token);
    localStorage.setItem("fabslot_username", data.username);
    localStorage.setItem("fabslot_role", data.role);
    setToken(data.token);
    setUser({
      id: data.id,
      username: data.username,
      fullName: data.fullName,
      role: data.role,
    });
    return data;
  };

  const register = async (registerData) => {
    let payload = registerData;
    if (typeof registerData === "string") {
      // Backwards-compatible argument handling if called as (username, password, role)
      const [username, password, role] = arguments;
      payload = { username, password, role };
    }
    return api.post("/auth/register", payload, { auth: false });
  };

  const logout = async () => {
    try {
      await api.post("/auth/logout");
    } catch (e) {
      // Ignore network or logout endpoint errors
    } finally {
      localStorage.removeItem("fabslot_token");
      localStorage.removeItem("fabslot_username");
      localStorage.removeItem("fabslot_role");
      setToken(null);
      setUser(null);
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
  };

  // Provide both 'user' & 'auth' (for backwards-compatibility with existing components)
  const auth = user ? { ...user, token } : null;

  return (
    <AuthContext.Provider value={{ user, token, auth, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
