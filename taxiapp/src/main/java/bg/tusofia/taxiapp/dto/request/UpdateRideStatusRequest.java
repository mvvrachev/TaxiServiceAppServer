package bg.tusofia.taxiapp.dto.request;

import bg.tusofia.taxiapp.enums.RideStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRideStatusRequest(
        @NotNull RideStatus status
) {}
