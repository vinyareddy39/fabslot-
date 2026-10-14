import React, { useState } from "react";
import { useNavigate, Link, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // If already logged in, redirect away from login page
  if (user) {
    return <Navigate to={user.role === "LAB_ADMIN" ? "/admin" : "/student"} replace />;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const data = await login(form.username.trim(), form.password);
      navigate(data.role === "LAB_ADMIN" ? "/admin" : "/student", { replace: true });
    } catch (err) {
      setError(err.message || "Invalid credentials");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card">
      <h2>Login to FabSlot</h2>
      {error && <div className="error-banner">{error}</div>}
      <form onSubmit={handleSubmit}>
        <input
          id="login-username"
          placeholder="Username"
          value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })}
          required
          autoComplete="username"
          disabled={loading}
        />
        <input
          id="login-password"
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
          autoComplete="current-password"
          disabled={loading}
        />
        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
      <p>
        No account? <Link to="/register">Register here</Link>
      </p>
    </div>
  );
}
