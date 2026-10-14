import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ children, allowedRoles, requireRole }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", padding: "4rem", color: "#64748b" }}>
        Loading session...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const roles = allowedRoles 
    ? (Array.isArray(allowedRoles) ? allowedRoles : [allowedRoles])
    : (requireRole ? [requireRole] : null);

  if (roles && !roles.includes(user.role)) {
    // If unauthorized for this specific role, redirect to their home
    return <Navigate to="/" replace />;
  }

  return children;
}
