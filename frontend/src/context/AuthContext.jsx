import React, { createContext, useContext, useEffect, useState } from "react";
import { api } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem("fabslot_token");
    const username = localStorage.getItem("fabslot_username");
    const role = localStorage.getItem("fabslot_role");
    return token ? { token, username, role } : null;
  });

  useEffect(() => {
    if (auth) {
      localStorage.setItem("fabslot_token", auth.token);
      localStorage.setItem("fabslot_username", auth.username);
      localStorage.setItem("fabslot_role", auth.role);
    }
  }, [auth]);

  const login = async (username, password) => {
    const data = await api.post("/auth/login", { username, password }, { auth: false });
    setAuth({ token: data.token, username: data.username, role: data.role });
    return data;
  };

  const register = async (username, password, role) => {
    return api.post("/auth/register", { username, password, role }, { auth: false });
  };

  const logout = () => {
    localStorage.removeItem("fabslot_token");
    localStorage.removeItem("fabslot_username");
    localStorage.removeItem("fabslot_role");
    setAuth(null);
  };

  return (
    <AuthContext.Provider value={{ auth, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
