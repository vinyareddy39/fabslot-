package com.fabslot.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fabslot.dto.ApiResponse;
import com.fabslot.dto.BookingRequest;
import com.fabslot.entity.Booking;
import com.fabslot.repository.MemberRepository;
import com.fabslot.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final MemberRepository memberRepository;

    public BookingController(BookingService bookingService, MemberRepository memberRepository) {
        this.bookingService = bookingService;
        this.memberRepository = memberRepository;
    }

    // A student requests a slot; a lab admin can also test-create if needed
    @PostMapping
    public ResponseEntity<?> requestBooking(@Valid @RequestBody BookingRequest req, Principal principal) {
        try {
            Booking booking = bookingService.requestBooking(principal.getName(), req);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    // Logged-in member's own bookings ("my bookings" view)
    @GetMapping("/mine")
    public ResponseEntity<?> myBookings(Principal principal) {
        var member = memberRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return ResponseEntity.ok(bookingService.findByMember(member.getId()));
    }

    @PreAuthorize("hasRole('LAB_ADMIN')")
    @GetMapping
    public List<Booking> allBookings() {
        return bookingService.findAll();
    }

    @PreAuthorize("hasRole('LAB_ADMIN')")
    @GetMapping("/pending")
    public List<Booking> pendingBookings() {
        return bookingService.findPending();
    }

    @PreAuthorize("hasRole('LAB_ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.approve(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('LAB_ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.reject(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('LAB_ADMIN')")
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id, @RequestParam(required = false) String notes) {
        try {
            return ResponseEntity.ok(bookingService.complete(id, notes));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id, Principal principal) {
        try {
            return ResponseEntity.ok(bookingService.cancel(id, principal.getName()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(e.getMessage()));
        }
    }
}
