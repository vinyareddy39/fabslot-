import React, { useEffect, useState } from "react";
import { api } from "../api/client";
import BookingList from "../components/BookingList";

const emptyMachineForm = { name: "", type: "CNC_ROUTER", status: "AVAILABLE", location: "" };

export default function AdminDashboard() {
  const [tab, setTab] = useState("pending");
  const [machines, setMachines] = useState([]);
  const [pending, setPending] = useState([]);
  const [allBookings, setAllBookings] = useState([]);
  const [analytics, setAnalytics] = useState({ bookingsPerMachine: {}, hoursPerMachine: {}, topMembers: {} });
  const [machineForm, setMachineForm] = useState(emptyMachineForm);
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState("");

  const loadAll = async () => {
    try {
      const [m, p, b] = await Promise.all([
        api.get("/api/machines"),
        api.get("/api/bookings/pending"),
        api.get("/api/bookings"),
      ]);
      setMachines(m);
      setPending(p);
      setAllBookings(b);
    } catch (err) {
      setMessage(err.message);
    }
  };

  const loadAnalytics = async () => {
    try {
      const [bpm, hpm, top] = await Promise.all([
        api.get("/api/admin/analytics/bookings-per-machine"),
        api.get("/api/admin/analytics/hours-per-machine"),
        api.get("/api/admin/analytics/top-members"),
      ]);
      setAnalytics({ bookingsPerMachine: bpm, hoursPerMachine: hpm, topMembers: top });
    } catch (err) {
      setMessage(err.message);
    }
  };

  useEffect(() => {
    loadAll();
    loadAnalytics();
  }, []);

  const handleApprove = async (id) => {
    try {
      await api.put(`/api/bookings/${id}/approve`);
      setMessage(`Booking #${id} approved.`);
      loadAll();
    } catch (err) {
      setMessage(err.message);
    }
  };

  const handleReject = async (id) => {
    try {
      await api.put(`/api/bookings/${id}/reject`);
      setMessage(`Booking #${id} rejected.`);
      loadAll();
    } catch (err) {
      setMessage(err.message);
    }
  };

  const handleComplete = async (id) => {
    try {
      await api.put(`/api/bookings/${id}/complete`);
      setMessage(`Booking #${id} marked complete.`);
      loadAll();
      loadAnalytics();
    } catch (err) {
      setMessage(err.message);
    }
  };

  const handleMachineSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await api.put(`/api/machines/${editingId}`, machineForm);
        setMessage("Machine updated.");
      } else {
        await api.post("/api/machines", machineForm);
        setMessage("Machine added.");
      }
      setMachineForm(emptyMachineForm);
      setEditingId(null);
      loadAll();
    } catch (err) {
      setMessage(err.message);
    }
  };

  const editMachine = (m) => {
    setMachineForm({ name: m.name, type: m.type, status: m.status, location: m.location });
    setEditingId(m.id);
  };

  const deleteMachine = async (id) => {
    try {
      await api.del(`/api/machines/${id}`);
      setMessage("Machine deleted.");
      loadAll();
    } catch (err) {
      setMessage(err.message);
    }
  };

  return (
    <div className="dashboard">
      <h1>Lab Admin Console</h1>
      {message && <div className="info-banner">{message}</div>}

      <div className="tabs">
        <button className={tab === "pending" ? "active" : ""} onClick={() => setTab("pending")}>
          Pending Requests ({pending.length})
        </button>
        <button className={tab === "machines" ? "active" : ""} onClick={() => setTab("machines")}>
          Machines
        </button>
        <button className={tab === "all" ? "active" : ""} onClick={() => setTab("all")}>
          All Bookings
        </button>
        <button className={tab === "analytics" ? "active" : ""} onClick={() => setTab("analytics")}>
          Analytics
        </button>
      </div>

      {tab === "pending" && (
        <BookingList
          bookings={pending}
          showMember
          actions={(b) => (
            <>
              <button onClick={() => handleApprove(b.id)}>Approve</button>
              <button className="danger" onClick={() => handleReject(b.id)}>Reject</button>
            </>
          )}
        />
      )}

      {tab === "machines" && (
        <div className="machines-admin">
          <form className="machine-form" onSubmit={handleMachineSubmit}>
            <h3>{editingId ? "Edit Machine" : "Add Machine"}</h3>
            <input
              placeholder="Name"
              value={machineForm.name}
              onChange={(e) => setMachineForm({ ...machineForm, name: e.target.value })}
              required
            />
            <select
              value={machineForm.type}
              onChange={(e) => setMachineForm({ ...machineForm, type: e.target.value })}
            >
              <option value="CNC_ROUTER">CNC Router</option>
              <option value="THREE_D_PRINTER">3D Printer</option>
              <option value="LASER_CUTTER">Laser Cutter</option>
              <option value="OTHER">Other</option>
            </select>
            <select
              value={machineForm.status}
              onChange={(e) => setMachineForm({ ...machineForm, status: e.target.value })}
            >
              <option value="AVAILABLE">Available</option>
              <option value="MAINTENANCE">Maintenance</option>
              <option value="RETIRED">Retired</option>
            </select>
            <input
              placeholder="Location"
              value={machineForm.location}
              onChange={(e) => setMachineForm({ ...machineForm, location: e.target.value })}
            />
            <div className="modal-actions">
              {editingId && (
                <button
                  type="button"
                  className="secondary"
                  onClick={() => {
                    setEditingId(null);
                    setMachineForm(emptyMachineForm);
                  }}
                >
                  Cancel edit
                </button>
              )}
              <button type="submit">{editingId ? "Save" : "Add"}</button>
            </div>
          </form>

          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th><th>Type</th><th>Status</th><th>Location</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {machines.map((m) => (
                <tr key={m.id}>
                  <td>{m.name}</td>
                  <td>{m.type.replaceAll("_", " ")}</td>
                  <td><span className={`status-badge status-${m.status.toLowerCase()}`}>{m.status}</span></td>
                  <td>{m.location}</td>
                  <td className="actions-cell">
                    <button onClick={() => editMachine(m)}>Edit</button>
                    <button className="danger" onClick={() => deleteMachine(m.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "all" && (
        <BookingList
          bookings={allBookings}
          showMember
          actions={(b) =>
            b.status === "APPROVED" && (
              <button onClick={() => handleComplete(b.id)}>Mark Complete</button>
            )
          }
        />
      )}

      {tab === "analytics" && (
        <div className="analytics-grid">
          <div className="analytics-card">
            <h3>Bookings per Machine</h3>
            <ul>
              {Object.entries(analytics.bookingsPerMachine).map(([k, v]) => (
                <li key={k}>{k}: {v}</li>
              ))}
            </ul>
          </div>
          <div className="analytics-card">
            <h3>Completed Hours per Machine</h3>
            <ul>
              {Object.entries(analytics.hoursPerMachine).map(([k, v]) => (
                <li key={k}>{k}: {v.toFixed(1)} hrs</li>
              ))}
            </ul>
          </div>
          <div className="analytics-card">
            <h3>Top Students by Bookings</h3>
            <ul>
              {Object.entries(analytics.topMembers).map(([k, v]) => (
                <li key={k}>{k}: {v}</li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </div>
  );
}
