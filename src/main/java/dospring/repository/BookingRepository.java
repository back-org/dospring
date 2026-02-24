package com.java.dospring.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.dospring.model.Booking;

/**
 * BookingRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByDepartureDate(Date departureDate);
}
