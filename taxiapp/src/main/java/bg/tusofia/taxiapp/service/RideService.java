package bg.tusofia.taxiapp.service;

import bg.tusofia.taxiapp.dto.request.EditRideRequest;
import bg.tusofia.taxiapp.dto.request.RideRequest;
import bg.tusofia.taxiapp.dto.response.CarResponse;
import bg.tusofia.taxiapp.dto.response.DriverSummaryResponse;
import bg.tusofia.taxiapp.dto.response.RideResponse;
import bg.tusofia.taxiapp.dto.response.RideStatusMessage;
import bg.tusofia.taxiapp.entity.*;
import bg.tusofia.taxiapp.enums.RideStatus;
import bg.tusofia.taxiapp.enums.Role;
import bg.tusofia.taxiapp.exception.BusinessRuleException;
import bg.tusofia.taxiapp.exception.ForbiddenException;
import bg.tusofia.taxiapp.exception.ResourceNotFoundException;
import bg.tusofia.taxiapp.repository.DriverRepository;
import bg.tusofia.taxiapp.repository.RatingRepository;
import bg.tusofia.taxiapp.repository.RideRepository;
import bg.tusofia.taxiapp.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RatingRepository ratingRepository;

    public RideService(RideRepository rideRepository, UserRepository userRepository, DriverRepository driverRepository, SimpMessagingTemplate messagingTemplate, RatingRepository ratingRepository) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.messagingTemplate = messagingTemplate;
        this.ratingRepository = ratingRepository;
    }

    public RideResponse requestRide(RideRequest request, String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Business rule: the combined date + time must be in the future
        LocalDateTime rideDateTime = LocalDateTime.of(request.rideDate(), request.rideTime());
        if (rideDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Ride date and time must be in the future");
        }

        Ride ride = new Ride();
        ride.setCustomer(customer);
        ride.setStatus(RideStatus.PENDING);
        ride.setRideDate(request.rideDate());
        ride.setRideTime(request.rideTime());
        ride.setStartLocation(request.startLocation());
        ride.setEndLocation(request.endLocation());
        ride.setNumPeople(request.numPeople());

        return toRideResponse(rideRepository.save(ride));
    }

    public List<RideResponse> getOpenRides() {
        LocalDate today = LocalDate.now();
        Sort sort = Sort.by(Sort.Direction.ASC, "rideDate", "rideTime");
        return rideRepository.findByStatus(RideStatus.PENDING, sort).stream()
                .filter(ride -> !ride.getRideDate().isBefore(today))
                .map(this::toRideResponse)
                .toList();
    }

    @Transactional
    public RideResponse acceptRide(Long rideId, String driverUsername) {
        Driver driver = driverRepository.findByUser_Username(driverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (ride.getStatus() != RideStatus.PENDING) {
            throw new BusinessRuleException("This ride is no longer available");
        }

        ride.setDriver(driver);
        ride.setStatus(RideStatus.ACCEPTED);

        return toRideResponse(rideRepository.save(ride));
    }

    // The only valid forward transitions a driver can make
    private static final Map<RideStatus, RideStatus> NEXT_STATUS = Map.of(
            RideStatus.ACCEPTED,    RideStatus.ON_THE_WAY,
            RideStatus.ON_THE_WAY,  RideStatus.ARRIVED,
            RideStatus.ARRIVED,     RideStatus.IN_PROGRESS,
            RideStatus.IN_PROGRESS, RideStatus.COMPLETED
    );

    @Transactional
    public RideResponse updateStatus(Long rideId, RideStatus newStatus, String driverUsername) {
        Driver driver = driverRepository.findByUser_Username(driverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        // Ownership: only the assigned driver can advance this ride
        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
            throw new ForbiddenException("You are not assigned to this ride");
        }

        // Transition must be the valid next step
        RideStatus expected = NEXT_STATUS.get(ride.getStatus());
        if (expected == null || expected != newStatus) {
            throw new BusinessRuleException(
                    "Cannot change status from " + ride.getStatus() + " to " + newStatus);
        }

        ride.setStatus(newStatus);
        Ride saved = rideRepository.save(ride);

        // Push the new status to whoever is watching this ride
        messagingTemplate.convertAndSend(
                "/topic/rides/" + saved.getId(),
                new RideStatusMessage(saved.getId(), saved.getStatus()));

        return toRideResponse(saved);
    }

    @Transactional
    public RideResponse cancelRide(Long rideId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (user.getRole() == Role.CUSTOMER) {
            return cancelAsCustomer(ride, user);
        } else {
            return cancelAsDriver(ride, user);
        }
    }

    private RideResponse cancelAsCustomer(Ride ride, User customer) {
        if (!ride.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("This is not your ride");
        }
        if (ride.getStatus() != RideStatus.PENDING && ride.getStatus() != RideStatus.ACCEPTED) {
            throw new BusinessRuleException("This ride can no longer be cancelled");
        }
        ensureMoreThan24HoursBefore(ride);

        ride.setStatus(RideStatus.CANCELLED);
        ride.setCancelledBy("CUSTOMER");
        return toRideResponse(rideRepository.save(ride));
    }

    private RideResponse cancelAsDriver(Ride ride, User driverUser) {
        Driver driver = driverRepository.findByUser_Username(driverUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (ride.getDriver() == null || !ride.getDriver().getId().equals(driver.getId())) {
            throw new ForbiddenException("You are not assigned to this ride");
        }
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new BusinessRuleException("This ride can no longer be cancelled");
        }
        ensureMoreThan24HoursBefore(ride);

        // Revert to open so other drivers can pick it up
        ride.setStatus(RideStatus.PENDING);
        ride.setDriver(null);
        return toRideResponse(rideRepository.save(ride));
    }

    private void ensureMoreThan24HoursBefore(Ride ride) {
        LocalDateTime rideDateTime = LocalDateTime.of(ride.getRideDate(), ride.getRideTime());
        if (LocalDateTime.now().isAfter(rideDateTime.minusHours(24))) {
            throw new BusinessRuleException(
                    "A ride can only be cancelled more than 24 hours before its scheduled time");
        }
    }

    @Transactional
    public RideResponse editRide(Long rideId, EditRideRequest request, String username) {
        User customer = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        if (!ride.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("This is not your ride");
        }
        if (ride.getStatus() != RideStatus.PENDING) {
            throw new BusinessRuleException("A ride can only be edited before a driver accepts it");
        }
        ensureFuture(request.rideDate(), request.rideTime());

        ride.setRideDate(request.rideDate());
        ride.setRideTime(request.rideTime());
        ride.setStartLocation(request.startLocation());
        ride.setEndLocation(request.endLocation());
        ride.setNumPeople(request.numPeople());

        return toRideResponse(rideRepository.save(ride));
    }

    private void ensureFuture(LocalDate date, LocalTime time) {
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Ride date and time must be in the future");
        }
    }

    private static final List<RideStatus> UPCOMING_STATUSES = List.of(
            RideStatus.PENDING, RideStatus.ACCEPTED, RideStatus.ON_THE_WAY,
            RideStatus.ARRIVED, RideStatus.IN_PROGRESS);

    private static final List<RideStatus> HISTORY_STATUSES = List.of(
            RideStatus.COMPLETED, RideStatus.CANCELLED);

    public RideResponse getRideById(Long rideId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));

        boolean isOwningCustomer = ride.getCustomer().getId().equals(user.getId());
        boolean isAssignedDriver = ride.getDriver() != null
                && ride.getDriver().getUser().getId().equals(user.getId());

        if (!isOwningCustomer && !isAssignedDriver) {
            throw new ForbiddenException("You do not have access to this ride");
        }

        return toRideResponse(ride);
    }

    public List<RideResponse> getMyRides(String username, String filter) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<RideStatus> statuses;
        Sort sort;
        boolean upcoming;
        if ("upcoming".equalsIgnoreCase(filter)) {
            upcoming = true;
            statuses = UPCOMING_STATUSES;
            sort = Sort.by(Sort.Direction.ASC, "rideDate", "rideTime");
        } else if ("history".equalsIgnoreCase(filter)) {
            upcoming = false;
            statuses = HISTORY_STATUSES;
            sort = Sort.by(Sort.Direction.DESC, "rideDate", "rideTime");
        } else {
            throw new BusinessRuleException("Filter must be 'upcoming' or 'history'");
        }

        List<Ride> rides;
        if (user.getRole() == Role.CUSTOMER) {
            rides = rideRepository.findByCustomerAndStatusIn(user, statuses, sort);
        } else {
            Driver driver = driverRepository.findByUser_Username(username)
                    .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
            rides = rideRepository.findByDriverAndStatusIn(driver, statuses, sort);
        }

        LocalDate today = LocalDate.now();
        return rides.stream()
                .filter(ride -> !upcoming || !ride.getRideDate().isBefore(today))
                .map(this::toRideResponse)
                .toList();
    }

    // Reusable mapper — every ride endpoint will return RideResponse through this
    RideResponse toRideResponse(Ride ride) {
        DriverSummaryResponse driverSummary = null;

        if (ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            Car car = driver.getCar();
            CarResponse carResponse = (car == null) ? null
                    : new CarResponse(car.getBrand(), car.getModel(), car.getPlateNumber());
            driverSummary = new DriverSummaryResponse(
                    driver.getUser().getFirstName(),
                    driver.getUser().getLastName(),
                    driver.getUser().getPhoneNumber(),
                    driver.getAverageRating(),
                    carResponse);
        }

        Integer rating = null;
        if (ride.getStatus() == RideStatus.COMPLETED) {
            rating = ratingRepository.findByRide_Id(ride.getId())
                    .map(Rating::getScore)
                    .orElse(null);
        }

        return new RideResponse(
                ride.getId(), ride.getStatus(), ride.getRideDate(), ride.getRideTime(),
                ride.getStartLocation(), ride.getEndLocation(), ride.getNumPeople(),
                driverSummary, rating);
    }
}
