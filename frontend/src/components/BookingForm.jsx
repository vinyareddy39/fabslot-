import React, { useState } from "react";

export default function BookingForm({ machine, onSubmit, onCancel }) {
  const [form, setForm] = useState({ startTime: "", endTime: "", purpose: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await onSubmit({
        machineId: machine.id,
        startTime: form.startTime,
        endTime: form.endTime,
        purpose: form.purpose,
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card">
        <h3>Book {machine.name}</h3>
        {error && <div className="error-banner">{error}</div>}
        <form onSubmit={handleSubmit}>
          <label>
            Start time
            <input
              type="datetime-local"
              value={form.startTime}
              onChange={(e) => setForm({ ...form, startTime: e.target.value })}
              required
            />
          </label>
          <label>
            End time
            <input
              type="datetime-local"
              value={form.endTime}
              onChange={(e) => setForm({ ...form, endTime: e.target.value })}
              required
            />
          </label>
          <label>
            Purpose
            <input
              type="text"
              placeholder="e.g. Final year project part"
              value={form.purpose}
              onChange={(e) => setForm({ ...form, purpose: e.target.value })}
            />
          </label>
          <div className="modal-actions">
            <button type="button" className="secondary" onClick={onCancel}>
              Cancel
            </button>
            <button type="submit" disabled={submitting}>
              {submitting ? "Submitting..." : "Request Slot"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
