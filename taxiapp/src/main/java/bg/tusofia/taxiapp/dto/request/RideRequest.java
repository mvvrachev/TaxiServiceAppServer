package bg.tusofia.taxiapp.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record RideRequest(
        @NotNull LocalDate rideDate,
        @NotNull LocalTime rideTime,
        @NotBlank String startLocation,
        @NotBlank String endLocation,
        @Min(1) @Max(6) int numPeople
) {}