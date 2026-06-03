package bg.tusofia.taxiapp.dto.response;

import bg.tusofia.taxiapp.enums.RideStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record RideResponse(
        Long id,
        RideStatus status,
        LocalDate rideDate,
        LocalTime rideTime,
        String startLocation,
        String endLocation,
        int numPeople,
        DriverSummaryResponse driver,
        Integer rating
) {}
