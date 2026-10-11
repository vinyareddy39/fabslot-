package com.fabslot.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fabslot.dto.BookingRequest;
import com.fabslot.entity.Booking;
import com.fabslot.entity.BookingStatus;
import com.fabslot.entity.Machine;
import com.fabslot.entity.MachineStatus;
import com.fabslot.entity.Member;
import com.fabslot.entity.UsageLog;
import com.fabslot.repository.BookingRepository;
import com.fabslot.repository.MachineRepository;
import com.fabslot.repository.MemberRepository;
import com.fabslot.repository.UsageLogRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MachineRepository machineRepository;
    private final MemberRepository memberRepository;
    private final UsageLogRepository usageLogRepository;

    public BookingService(BookingRepository bookingRepository,
                           MachineRepository machineRepository,
                           MemberRepository memberRepository,
                           UsageLogRepository usageLogRepository) {
        this.bookingRepository = bookingRepository;
        this.machineRepository = machineRepository;
        this.memberRepository = memberRepository;
        this.usageLogRepository = usageLogRepository;
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public List<Booking> findByMember(Long memberId) {
        return bookingRepository.findByMemberId(memberId);
    }

    public List<Booking> findPending() {
        return bookingRepository.findByStatus(BookingStatus.PENDING);
    }

    /**
     * Creates a booking request in PENDING state.
     * Rejects up front if the machine is not AVAILABLE, or if the requested
     * window overlaps an already-APPROVED booking on the same machine.
     */
    public Booking requestBooking(String username, BookingRequest req) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Machine machine = machineRepository.findById(req.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine not found"));

        if (machine.getStatus() != MachineStatus.AVAILABLE) {
            throw new IllegalStateException("Machine is not available for booking (" + machine.getStatus() + ")");
        }

        List<Booking> overlaps = bookingRepository.findOverlappingApproved(
                machine.getId(), req.getStartTime(), req.getEndTime());

        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Requested slot conflicts with an existing approved booking");
        }

        Booking booking = new Booking();
        booking.setMember(member);
        booking.setMachine(machine);
        booking.setStartTime(req.getStartTime());
        booking.setEndTime(req.getEndTime());
        booking.setPurpose(req.getPurpose());
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    /**
     * Admin approves a PENDING booking. Re-checks for conflicts at approval
     * time in case another request was approved in the meantime.
     */
    public Booking approve(Long bookingId) {
        Booking booking = getOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be approved");
        }

        List<Booking> overlaps = bookingRepository.findOverlappingApproved(
                booking.getMachine().getId(), booking.getStartTime(), booking.getEndTime());

        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Cannot approve: slot now conflicts with another approved booking");
        }

        booking.setStatus(BookingStatus.APPROVED);
        return bookingRepository.save(booking);
    }

    public Booking reject(Long bookingId) {
        Booking booking = getOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    public Booking cancel(Long bookingId, String username) {
        Booking booking = getOrThrow(bookingId);

        if (!booking.getMember().getUsername().equals(username)) {
            throw new IllegalStateException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed bookings cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Marks an APPROVED booking as COMPLETED and writes a usage log entry.
     * In this simplified flow, actual usage is assumed to match the
     * scheduled window.
     */
    public Booking complete(Long bookingId, String notes) {
        Booking booking = getOrThrow(bookingId);

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalStateException("Only approved bookings can be marked complete");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        UsageLog log = new UsageLog(booking, booking.getStartTime(), booking.getEndTime(), notes);
        usageLogRepository.save(log);

        return booking;
    }

    private Booking getOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }
}
