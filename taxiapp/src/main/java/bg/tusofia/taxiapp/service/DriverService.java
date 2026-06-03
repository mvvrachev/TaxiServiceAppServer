package bg.tusofia.taxiapp.service;

import bg.tusofia.taxiapp.dto.request.CarRequest;
import bg.tusofia.taxiapp.dto.response.CarResponse;
import bg.tusofia.taxiapp.dto.response.DriverProfileResponse;
import bg.tusofia.taxiapp.entity.Car;
import bg.tusofia.taxiapp.entity.Driver;
import bg.tusofia.taxiapp.entity.User;
import bg.tusofia.taxiapp.exception.BusinessRuleException;
import bg.tusofia.taxiapp.exception.ResourceNotFoundException;
import bg.tusofia.taxiapp.repository.CarRepository;
import bg.tusofia.taxiapp.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final CarRepository carRepository;

    public DriverService(DriverRepository driverRepository, CarRepository carRepository) {
        this.driverRepository = driverRepository;
        this.carRepository = carRepository;
    }

    public DriverProfileResponse getProfile(String username) {
        Driver driver = driverRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        User user = driver.getUser();
        Car car = driver.getCar();
        CarResponse carResponse = (car == null) ? null
                : new CarResponse(car.getBrand(), car.getModel(), car.getPlateNumber());

        return new DriverProfileResponse(
                user.getUsername(), user.getFirstName(), user.getLastName(),
                user.getPhoneNumber(), driver.getAverageRating(),
                driver.getRatingsCount(), carResponse);
    }

    @Transactional
    public CarResponse updateCar(String username, CarRequest request) {
        Driver driver = driverRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Car car = driver.getCar();
        if (car == null) {
            throw new ResourceNotFoundException("Car not found");
        }

        String newPlate = request.plateNumber().toUpperCase();
        // Only conflict if the plate actually changed and the new one is taken by another car
        if (!car.getPlateNumber().equals(newPlate) && carRepository.existsByPlateNumber(newPlate)) {
            throw new BusinessRuleException("Plate number is already registered");
        }

        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setPlateNumber(newPlate);
        Car saved = carRepository.save(car);

        return new CarResponse(saved.getBrand(), saved.getModel(), saved.getPlateNumber());
    }
}
