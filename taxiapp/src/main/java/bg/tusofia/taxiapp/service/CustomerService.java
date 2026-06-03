package bg.tusofia.taxiapp.service;

import bg.tusofia.taxiapp.dto.response.CustomerProfileResponse;
import bg.tusofia.taxiapp.entity.User;
import bg.tusofia.taxiapp.exception.ResourceNotFoundException;
import bg.tusofia.taxiapp.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final UserRepository userRepository;

    public CustomerService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CustomerProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new CustomerProfileResponse(
                user.getUsername(), user.getFirstName(),
                user.getLastName(), user.getPhoneNumber());
    }
}
