package com.fabslot.controller;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabslot.entity.Booking;
import com.fabslot.entity.BookingStatus;
import com.fabslot.repository.BookingRepository;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('LAB_ADMIN')")
public class AnalyticsController {

    private final BookingRepository bookingRepository;

    public AnalyticsController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/bookings-per-machine")
    public Map<String, Long> bookingsPerMachine() {
        return bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(b -> b.getMachine().getName(), Collectors.counting()));
    }

    @GetMapping("/hours-per-machine")
    public Map<String, Double> hoursPerMachine() {
        List<Booking> completed = bookingRepository.findByStatus(BookingStatus.COMPLETED);

        return completed.stream().collect(Collectors.groupingBy(
                b -> b.getMachine().getName(),
                Collectors.summingDouble(b -> Duration.between(b.getStartTime(), b.getEndTime()).toMinutes() / 60.0)));
    }

    @GetMapping("/top-members")
    public Map<String, Long> topMembersByBookingCount() {
        return bookingRepository.findAll().stream()
                .collect(Collectors.groupingBy(b -> b.getMember().getUsername(), Collectors.counting()));
    }
}
