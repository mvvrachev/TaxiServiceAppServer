package bg.tusofia.taxiapp.controller;

import bg.tusofia.taxiapp.dto.request.LoginRequest;
import bg.tusofia.taxiapp.dto.request.RegisterCustomerRequest;
import bg.tusofia.taxiapp.dto.request.RegisterDriverRequest;
import bg.tusofia.taxiapp.dto.response.AuthResponse;
import bg.tusofia.taxiapp.dto.response.UserResponse;
import bg.tusofia.taxiapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    public ResponseEntity<UserResponse> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerCustomer(request));
    }

    @PostMapping("/register/driver")
    public ResponseEntity<UserResponse> registerDriver(
            @Valid @RequestBody RegisterDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerDriver(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
