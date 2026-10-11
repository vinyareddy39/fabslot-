package com.fabslot.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabslot.entity.Booking;
import com.fabslot.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByMemberId(Long memberId);

    List<Booking> findByMachineId(Long machineId);

    List<Booking> findByStatus(BookingStatus status);

    // Finds APPROVED bookings on the same machine whose time range overlaps
    // the requested [start, end) window. Used to block double-booking.
    @Query("""
            SELECT b FROM Booking b
            WHERE b.machine.id = :machineId
              AND b.status = com.fabslot.entity.BookingStatus.APPROVED
              AND b.startTime < :end
              AND b.endTime > :start
            """)
    List<Booking> findOverlappingApproved(
            @Param("machineId") Long machineId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
