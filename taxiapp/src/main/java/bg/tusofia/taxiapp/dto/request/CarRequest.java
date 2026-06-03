package bg.tusofia.taxiapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CarRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank @Pattern(regexp = "^[A-Z]{1,2}\\d{4}[A-Z]{2}$", message = "Invalid plate number format") String plateNumber
) {}