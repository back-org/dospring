package dospring.repository;

import java.util.Date;
import java.util.List;

import dospring.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;



/**
 * BookingRepository.
 *
 * <p>Enterprise V4+ documentation block.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {
  List<Booking> findByDepartureDate(Date departureDate);
}
