package bg.tusofia.taxiapp.repository;

import bg.tusofia.taxiapp.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByPlateNumber(String plateNumber);
}
