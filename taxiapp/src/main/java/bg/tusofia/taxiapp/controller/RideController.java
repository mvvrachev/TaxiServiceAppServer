package bg.tusofia.taxiapp.controller;

import bg.tusofia.taxiapp.dto.request.EditRideRequest;
import bg.tusofia.taxiapp.dto.request.RatingRequest;
import bg.tusofia.taxiapp.dto.request.RideRequest;
import bg.tusofia.taxiapp.dto.request.UpdateRideStatusRequest;
import bg.tusofia.taxiapp.dto.response.RatingResponse;
import bg.tusofia.taxiapp.dto.response.RideResponse;
import bg.tusofia.taxiapp.service.RatingService;
import bg.tusofia.taxiapp.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;
    private final RatingService ratingService;

    public RideController(RideService rideService, RatingService ratingService) {
        this.rideService = rideService;
        this.ratingService = ratingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RideResponse> requestRide(
            @Valid @RequestBody RideRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.requestRide(request, username));
    }

    @GetMapping("/open")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<RideResponse>> getOpenRides() {
        return ResponseEntity.ok(rideService.getOpenRides());
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.acceptRide(id, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RideResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRideStatusRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                rideService.updateStatus(id, request.status(), authentication.getName()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.cancelRide(id, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RideResponse> editRide(
            @PathVariable Long id,
            @Valid @RequestBody EditRideRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.editRide(id, request, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRide(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.getRideById(id, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<RideResponse>> getMyRides(
            @RequestParam String filter,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.getMyRides(authentication.getName(), filter));
    }

    @PostMapping("/{id}/rating")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<RatingResponse> rateRide(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ratingService.rateRide(id, request.score(), authentication.getName()));
    }
}
