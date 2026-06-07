package bg.tusofia.taxiapp.service;

import bg.tusofia.taxiapp.entity.User;
import bg.tusofia.taxiapp.exception.BusinessRuleException;
import bg.tusofia.taxiapp.exception.ResourceNotFoundException;
import bg.tusofia.taxiapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void updatePhone(String username, String newPhone) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getPhoneNumber().equals(newPhone)
                && userRepository.existsByPhoneNumber(newPhone)) {
            throw new BusinessRuleException("Phone number is already registered");
        }

        user.setPhoneNumber(newPhone);
        userRepository.save(user);
    }
}
