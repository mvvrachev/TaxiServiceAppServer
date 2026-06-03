package bg.tusofia.taxiapp.dto.response;

public record DriverSummaryResponse(
        String firstName,
        String lastName,
        String phoneNumber,
        double averageRating,
        CarResponse car
) {}
