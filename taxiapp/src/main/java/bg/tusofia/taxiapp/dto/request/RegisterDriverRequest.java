package bg.tusofia.taxiapp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterDriverRequest(
        @NotBlank String username,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters long") String password,
        @NotBlank @Pattern(regexp = "^\\+359\\d{9}$", message = "Phone number must start with +359 followed by 9 digits") String phoneNumber,
        @NotNull @Valid CarRequest car
) {}
