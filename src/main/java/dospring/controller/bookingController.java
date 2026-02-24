package com.java.dospring.controller;

import com.java.dospring.model.Booking;
import com.java.dospring.repository.BookingRepository;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Booking API (minimal, clean).
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "${app.security.cors-origins}", maxAge = 3600)
public class BookingController {

  private final BookingRepository bookingRepository;

  public BookingController(BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  @PostMapping
  public ResponseEntity<Booking> create(@Valid @RequestBody Booking booking) {
    Booking saved = bookingRepository.save(booking);
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Booking> get(@PathVariable Long id) {
    Optional<Booking> booking = bookingRepository.findById(id);
    return booking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<Page<Booking>> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(defaultValue = "createdAt") String sortBy,
                                            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
    Page<Booking> res = bookingRepository.findAll(PageRequest.of(page, size, Sort.by(direction, sortBy)));
    return ResponseEntity.ok(res);
  }
}
