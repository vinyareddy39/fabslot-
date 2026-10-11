import React from "react";

export default function MachineCard({ machine, onBookClick }) {
  const isAvailable = machine.status === "AVAILABLE";

  return (
    <div className={`machine-card ${isAvailable ? "" : "unavailable"}`}>
      <h3>{machine.name}</h3>
      <p className="machine-type">{machine.type.replaceAll("_", " ")}</p>
      <p className="machine-location">{machine.location}</p>
      <span className={`status-badge status-${machine.status.toLowerCase()}`}>
        {machine.status}
      </span>
      {onBookClick && (
        <button disabled={!isAvailable} onClick={() => onBookClick(machine)}>
          Book Slot
        </button>
      )}
    </div>
  );
}
