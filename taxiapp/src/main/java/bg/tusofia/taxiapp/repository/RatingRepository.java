package bg.tusofia.taxiapp.repository;

import bg.tusofia.taxiapp.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByRide_Id(Long rideId);
    Optional<Rating> findByRide_Id(Long rideId);
}
