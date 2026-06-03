package bg.tusofia.taxiapp.service;

import bg.tusofia.taxiapp.dto.response.RatingResponse;
import bg.tusofia.taxiapp.entity.Driver;
import bg.tusofia.taxiapp.entity.Rating;
import bg.tusofia.taxiapp.entity.Ride;
import bg.tusofia.taxiapp.entity.User;
import bg.tusofia.taxiapp.enums.RideStatus;
import bg.tusofia.taxiapp.exception.BusinessRuleException;
import bg.tusofia.taxiapp.exception.ForbiddenException;
import bg.tusofia.taxiapp.exception.ResourceNotFoundException;
import bg.tusofia.taxiapp.repository.DriverRepository;
import bg.tusofia.taxiapp.repository.RatingRepository;
import bg.tusofia.taxiapp.repository.RideRepository;
import bg.tusofia.taxiapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

    private final RideRepository rideRepository;
    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;

    public RatingService(RideRepository rideRepository,
                         RatingRepository ratingRepository,
                         DriverRepository driverRepository,
                         UserRepository userRepository) {
        this.rideRepository = rideRepository;
        this.ratingRepository = ratingRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RatingResponse rateRide(Long rideId, int score, String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (!ride.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("This is not your ride");
        }
        if (ride.getStatus() != RideStatus.COMPLETED) {
            throw new BusinessRuleException("You can only rate a completed ride");
        }
        if (ratingRepository.existsByRide_Id(rideId)) {
            throw new BusinessRuleException("This ride has already been rated");
        }

        Driver driver = ride.getDriver();

        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setDriver(driver);
        rating.setScore(score);
        Rating saved = ratingRepository.save(rating);

        // Update the driver's running average incrementally
        double newAverage =
                ((driver.getAverageRating() * driver.getRatingsCount()) + score)
                        / (driver.getRatingsCount() + 1);
        driver.setAverageRating(newAverage);
        driver.setRatingsCount(driver.getRatingsCount() + 1);
        driverRepository.save(driver);

        return new RatingResponse(saved.getId(), ride.getId(), saved.getScore());
    }
}
