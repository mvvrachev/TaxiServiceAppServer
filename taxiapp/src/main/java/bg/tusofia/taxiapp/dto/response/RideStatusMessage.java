package bg.tusofia.taxiapp.dto.response;

import bg.tusofia.taxiapp.enums.RideStatus;

public record RideStatusMessage(Long rideId, RideStatus status) {}
