import React from "react";
import { Routes, Route, Navigate, Link } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import Login from "./pages/Login";
import Register from "./pages/Register";
import StudentDashboard from "./pages/StudentDashboard";
import AdminDashboard from "./pages/AdminDashboard";
import ProtectedRoute from "./components/ProtectedRoute";
import "./App.css";

function NavBar() {
  const { user, logout } = useAuth();

  return (
    <nav className="navbar">
      <Link to="/" className="brand" style={{ textDecoration: "none" }}>
        FabSlot
      </Link>
      <div className="nav-links">
        {user ? (
          <>
            <span className="user-chip">
              {user.fullName ? user.fullName : user.username} ({user.role})
            </span>
            <button onClick={logout} style={{ cursor: "pointer" }}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

function HomeRedirect() {
  const { user, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return user.role === "LAB_ADMIN" ? (
    <Navigate to="/admin" replace />
  ) : (
    <Navigate to="/student" replace />
  );
}

function App() {
  return (
    <div className="app-shell">
      <NavBar />
      <main className="content">
        <Routes>
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/student"
            element={
              <ProtectedRoute allowedRoles={["STUDENT"]}>
                <StudentDashboard />
              </ProtectedRoute>
            }
          />
          {/* Backwards-compatibility alias */}
          <Route
            path="/dashboard"
            element={<Navigate to="/student" replace />}
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={["LAB_ADMIN"]}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
