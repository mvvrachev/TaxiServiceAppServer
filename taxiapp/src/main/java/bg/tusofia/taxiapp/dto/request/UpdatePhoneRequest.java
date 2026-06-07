package bg.tusofia.taxiapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePhoneRequest(
        @NotBlank
        @Pattern(regexp = "^\\+359\\d{9}$",
                message = "Phone number must start with +359 followed by 9 digits")
        String phoneNumber
) {}
