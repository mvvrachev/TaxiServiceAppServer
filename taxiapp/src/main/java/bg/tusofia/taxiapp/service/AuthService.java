package bg.tusofia.taxiapp.service;

import bg.tusofia.taxiapp.dto.request.LoginRequest;
import bg.tusofia.taxiapp.dto.request.RegisterCustomerRequest;
import bg.tusofia.taxiapp.dto.request.RegisterDriverRequest;
import bg.tusofia.taxiapp.dto.response.AuthResponse;
import bg.tusofia.taxiapp.dto.response.UserResponse;
import bg.tusofia.taxiapp.entity.Car;
import bg.tusofia.taxiapp.entity.Driver;
import bg.tusofia.taxiapp.entity.User;
import bg.tusofia.taxiapp.enums.Role;
import bg.tusofia.taxiapp.exception.BusinessRuleException;
import bg.tusofia.taxiapp.repository.CarRepository;
import bg.tusofia.taxiapp.repository.DriverRepository;
import bg.tusofia.taxiapp.repository.UserRepository;
import bg.tusofia.taxiapp.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final CarRepository carRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       DriverRepository driverRepository,
                       CarRepository carRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.carRepository = carRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UserResponse registerCustomer(RegisterCustomerRequest request) {
        validateUniqueness(request.username(), request.phoneNumber());

        User user = new User();
        user.setUsername(request.username());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);

        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse registerDriver(RegisterDriverRequest request) {
        validateUniqueness(request.username(), request.phoneNumber());
        if (carRepository.existsByPlateNumber(request.car().plateNumber().toUpperCase())) {
            throw new BusinessRuleException("Plate number is already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.DRIVER);
        User savedUser = userRepository.save(user);

        Driver driver = new Driver();
        driver.setUser(savedUser);
        Driver savedDriver = driverRepository.save(driver);

        Car car = new Car();
        car.setDriver(savedDriver);
        car.setBrand(request.car().brand());
        car.setModel(request.car().model());
        car.setPlateNumber(request.car().plateNumber().toUpperCase());
        carRepository.save(car);

        return toUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        String token = jwtService.generateToken(
                user.getUsername(), user.getRole().name(), user.getId());

        return new AuthResponse(token, user.getRole(), user.getId());
    }

    private void validateUniqueness(String username, String phoneNumber) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessRuleException("Username is already taken");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessRuleException("Phone number is already registered");
        }
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(), user.getUsername(), user.getFirstName(),
                user.getLastName(), user.getPhoneNumber(), user.getRole());
    }
}