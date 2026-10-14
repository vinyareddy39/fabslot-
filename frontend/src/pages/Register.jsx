import React, { useState } from "react";
import { useNavigate, Link, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Register() {
  const { user, register, login } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: "",
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    role: "STUDENT",
    adminCode: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  // Redirect away if already logged in
  if (user) {
    return <Navigate to={user.role === "LAB_ADMIN" ? "/admin" : "/student"} replace />;
  }

  const validate = () => {
    if (!form.fullName.trim()) {
      return "Full name is required.";
    }
    if (form.username.trim().length < 3 || form.username.trim().length > 30) {
      return "Username must be between 3 and 30 characters.";
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email.trim())) {
      return "Please provide a valid email address.";
    }
    if (form.password.length < 8) {
      return "Password must be at least 8 characters.";
    }
    if (form.password !== form.confirmPassword) {
      return "Passwords do not match.";
    }
    if (form.role === "LAB_ADMIN" && !form.adminCode.trim()) {
      return "Admin registration secret code is required for Lab Admin role.";
    }
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);

    try {
      await register({
        fullName: form.fullName.trim(),
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        role: form.role,
        adminCode: form.role === "LAB_ADMIN" ? form.adminCode.trim() : undefined,
      });

      setSuccess("Account registered successfully! Logging you in...");

      // Auto-login on success
      try {
        const loginData = await login(form.username.trim(), form.password);
        setTimeout(() => {
          navigate(loginData.role === "LAB_ADMIN" ? "/admin" : "/student", { replace: true });
        }, 800);
      } catch (loginErr) {
        setSuccess("Account registered successfully! Redirecting to login...");
        setTimeout(() => navigate("/login"), 1200);
      }
    } catch (err) {
      setError(err.message || "Registration failed. Please check your details.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-card">
      <h2>Create a FabSlot account</h2>
      {error && <div className="error-banner">{error}</div>}
      {success && <div className="success-banner">{success}</div>}

      <form onSubmit={handleSubmit}>
        <input
          id="reg-fullname"
          placeholder="Full Name (e.g. Alex Smith)"
          value={form.fullName}
          onChange={(e) => setForm({ ...form, fullName: e.target.value })}
          required
          disabled={loading}
        />

        <input
          id="reg-username"
          placeholder="Username (3-30 characters)"
          value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })}
          required
          autoComplete="username"
          disabled={loading}
        />

        <input
          id="reg-email"
          type="email"
          placeholder="Email address"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          required
          autoComplete="email"
          disabled={loading}
        />

        <input
          id="reg-password"
          type="password"
          placeholder="Password (min 8 characters)"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
          autoComplete="new-password"
          disabled={loading}
        />

        <input
          id="reg-confirm-password"
          type="password"
          placeholder="Confirm password"
          value={form.confirmPassword}
          onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
          required
          autoComplete="new-password"
          disabled={loading}
        />

        <select
          id="reg-role"
          value={form.role}
          onChange={(e) => setForm({ ...form, role: e.target.value })}
          disabled={loading}
        >
          <option value="STUDENT">Student</option>
          <option value="LAB_ADMIN">Lab Admin</option>
        </select>

        {form.role === "LAB_ADMIN" && (
          <input
            id="reg-admin-code"
            type="password"
            placeholder="Admin Secret Code"
            value={form.adminCode}
            onChange={(e) => setForm({ ...form, adminCode: e.target.value })}
            required
            disabled={loading}
          />
        )}

        <button type="submit" disabled={loading}>
          {loading ? "Registering..." : "Register"}
        </button>
      </form>

      <p>
        Already have an account? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}
