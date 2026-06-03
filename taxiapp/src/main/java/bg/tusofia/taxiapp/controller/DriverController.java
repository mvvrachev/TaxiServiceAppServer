package bg.tusofia.taxiapp.controller;

import bg.tusofia.taxiapp.dto.request.CarRequest;
import bg.tusofia.taxiapp.dto.response.CarResponse;
import bg.tusofia.taxiapp.dto.response.DriverProfileResponse;
import bg.tusofia.taxiapp.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DriverProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(driverService.getProfile(authentication.getName()));
    }

    @PutMapping("/car")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<CarResponse> updateCar(
            @Valid @RequestBody CarRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(driverService.updateCar(authentication.getName(), request));
    }
}
