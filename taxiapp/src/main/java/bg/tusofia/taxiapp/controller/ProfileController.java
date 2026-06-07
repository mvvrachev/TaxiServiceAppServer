package bg.tusofia.taxiapp.controller;

import bg.tusofia.taxiapp.dto.request.UpdatePhoneRequest;
import bg.tusofia.taxiapp.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping("/phone")
    public ResponseEntity<Void> updatePhone(
            @Valid @RequestBody UpdatePhoneRequest request,
            Authentication authentication) {
        profileService.updatePhone(authentication.getName(), request.phoneNumber());
        return ResponseEntity.noContent().build();
    }
}
