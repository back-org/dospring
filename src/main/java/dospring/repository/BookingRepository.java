package com.java.dospring.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.dospring.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByDepartureDate(Date departureDate);
}
