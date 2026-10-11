import React from "react";

function formatDate(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString();
}

export default function BookingList({ bookings, showMember = false, actions }) {
  if (!bookings || bookings.length === 0) {
    return <p className="empty-state">No bookings to show.</p>;
  }

  return (
    <table className="data-table">
      <thead>
        <tr>
          <th>ID</th>
          {showMember && <th>Student</th>}
          <th>Machine</th>
          <th>Start</th>
          <th>End</th>
          <th>Purpose</th>
          <th>Status</th>
          {actions && <th>Actions</th>}
        </tr>
      </thead>
      <tbody>
        {bookings.map((b) => (
          <tr key={b.id}>
            <td>{b.id}</td>
            {showMember && <td>{b.member?.username}</td>}
            <td>{b.machine?.name}</td>
            <td>{formatDate(b.startTime)}</td>
            <td>{formatDate(b.endTime)}</td>
            <td>{b.purpose}</td>
            <td>
              <span className={`status-badge status-${b.status?.toLowerCase()}`}>
                {b.status}
              </span>
            </td>
            {actions && <td className="actions-cell">{actions(b)}</td>}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
