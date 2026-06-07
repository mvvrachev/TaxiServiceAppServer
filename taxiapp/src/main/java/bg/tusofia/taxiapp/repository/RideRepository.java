package bg.tusofia.taxiapp.repository;

import bg.tusofia.taxiapp.entity.Driver;
import bg.tusofia.taxiapp.entity.Ride;
import bg.tusofia.taxiapp.entity.User;
import bg.tusofia.taxiapp.enums.RideStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByCustomerAndStatusIn(User customer, Collection<RideStatus> statuses, Sort sort);
    List<Ride> findByDriverAndStatusIn(Driver driver, Collection<RideStatus> statuses, Sort sort);
    List<Ride> findByStatus(RideStatus status, Sort sort);
}
