import React, { useEffect, useState } from "react";
import { api } from "../api/client";
import MachineCard from "../components/MachineCard";
import BookingForm from "../components/BookingForm";
import BookingList from "../components/BookingList";

export default function StudentDashboard() {
  const [machines, setMachines] = useState([]);
  const [myBookings, setMyBookings] = useState([]);
  const [selectedMachine, setSelectedMachine] = useState(null);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const [machinesData, bookingsData] = await Promise.all([
        api.get("/api/machines"),
        api.get("/api/bookings/mine"),
      ]);
      setMachines(machinesData);
      setMyBookings(bookingsData);
    } catch (err) {
      setMessage(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleBookingSubmit = async (payload) => {
    await api.post("/api/bookings", payload);
    setSelectedMachine(null);
    setMessage("Booking request submitted — pending admin approval.");
    loadData();
  };

  const handleCancel = async (id) => {
    try {
      await api.del(`/api/bookings/${id}`);
      setMessage("Booking cancelled.");
      loadData();
    } catch (err) {
      setMessage(err.message);
    }
  };

  return (
    <div className="dashboard">
      <h1>Fab Lab Machines</h1>
      <p className="subtitle">Browse available equipment and request a slot.</p>

      {message && <div className="info-banner">{message}</div>}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <div className="machine-grid">
          {machines.map((m) => (
            <MachineCard key={m.id} machine={m} onBookClick={setSelectedMachine} />
          ))}
        </div>
      )}

      <h2>My Bookings</h2>
      <BookingList
        bookings={myBookings}
        actions={(b) =>
          (b.status === "PENDING" || b.status === "APPROVED") && (
            <button className="danger" onClick={() => handleCancel(b.id)}>
              Cancel
            </button>
          )
        }
      />

      {selectedMachine && (
        <BookingForm
          machine={selectedMachine}
          onSubmit={handleBookingSubmit}
          onCancel={() => setSelectedMachine(null)}
        />
      )}
    </div>
  );
}
